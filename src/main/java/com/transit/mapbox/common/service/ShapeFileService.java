package com.transit.mapbox.common.service;

import org.apache.commons.io.FileUtils;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.GeoJSON;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.referencing.CRS;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ShapeFileService {
    private static File tempDir = new File("C:\\mapbox\\shapefile_temp");

    public String convertZipToGeoJson(MultipartFile zipFile) throws IOException {
        FileUtils.forceMkdir(tempDir);

        try (ZipInputStream zipInputStream = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();
                File entryFile = new File(tempDir, entryName);

                if (!entry.isDirectory()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    try (FileOutputStream fos = new FileOutputStream(entryFile.getPath())) {
                        while ((bytesRead = zipInputStream.read(buffer)) > 0) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                }

                zipInputStream.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        File shpFile = findFile(tempDir, ".shp");

        if (shpFile != null) {
            try {
                File geojsonFile = convertShpToGeoJSON(shpFile, tempDir);
                if (geojsonFile != null) {
                    return FileUtils.readFileToString(geojsonFile, StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Clean up temporary directory
        FileUtils.deleteDirectory(tempDir);
        return null;
    }

    private File convertShpToGeoJSON(File shpFile, File outputDir) throws IOException, FactoryException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("url", shpFile.toURI().toURL());
        map.put("create spatial index", Boolean.TRUE);

        File shxFile = findFile(tempDir, ".shx");
        map.put("shx", shxFile.toURI().toURL());

        File dbfFile = findFile(tempDir, ".dbf");
        map.put("dbf", dbfFile.toURI().toURL());

        File prjFile = findFile(tempDir, ".prj");
        map.put("prj", prjFile.toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];

        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
        CoordinateReferenceSystem targetCRS = CRS.parseWKT(getPrjContent(prjFile));

        SimpleFeatureCollection reprojectedCollection = featureSource.getFeatures();

        File geojsonFile = new File(outputDir, "output.geojson");
        try (OutputStream outputStream = new FileOutputStream(geojsonFile);
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            FeatureJSON featureJSON = new FeatureJSON();
            featureJSON.writeFeatureCollection(reprojectedCollection, writer);
        }

        return geojsonFile;
    }

    private File findFile(File directory, String type) {
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(type));
        if (files != null && files.length > 0) {
            return files[0];
        } else {
            // .shp 파일이 없는 경우에 대한 예외 처리
            throw new IllegalStateException("No .shp file found in the specified directory.");
        }
    }

    private String getPrjContent(File prjFile) throws IOException {
        return org.apache.commons.io.FileUtils.readFileToString(prjFile, "UTF-8");
    }
}
