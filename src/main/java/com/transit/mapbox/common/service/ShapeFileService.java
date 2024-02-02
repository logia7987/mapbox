package com.transit.mapbox.common.service;

import org.apache.commons.io.FileUtils;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ShapeFileService {

    public List<Map<String, Object>> readShapeFileGeometry(String shpFilePath) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        ShapefileReader reader = null;

        try {
            ShpFiles shpFile = new ShpFiles(shpFilePath);
            GeometryFactory geometryFactory = new GeometryFactory();
            reader = new ShapefileReader(shpFile, true, false, geometryFactory);

            int i = 0;

            while (reader.hasNext()) {
                Map<String, Object> result = new HashMap<>();
                ShapefileReader.Record record = reader.nextRecord();
                Geometry shape = (Geometry)record.shape();
                Point centroid = shape.getCentroid();

                result.put("geoX", centroid.getX());
                result.put("geoY", centroid.getY());

                resultList.add(result);
            }
            reader.close();
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (ShapefileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        };

        return resultList;
    }

    public void convertZipToGeoJson(MultipartFile zipFile, String geoJsonFilePath) throws IOException {
        // Create a temporary directory to extract shapefile contents
        File tempDir = new File(System.getProperty("java.io.tmpdir") + "/shapefile_temp");
        FileUtils.forceMkdir(tempDir);

        try (ZipInputStream zipInputStream = new ZipInputStream(zipFile.getInputStream(), StandardCharsets.ISO_8859_1)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry())  != null) {
                String entryName = entry.getName();
                File entryFile = new File(tempDir, entryName);

                if (!entry.isDirectory()) {
//                    FileUtils.copyInputStreamToFile(zipInputStream, entryFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    try (FileOutputStream fos = new FileOutputStream(entryFile)) {
                        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                }

                zipInputStream.closeEntry();
            }
        }

        // Find the .shp file in the extracted directory
        File shpFile = findShpFile(tempDir);

        // Convert .shp file to GeoJSON
        if (shpFile != null) {
            ShapefileDataStore dataStore = new ShapefileDataStore(shpFile.toURI().toURL());
            SimpleFeatureCollection features = dataStore.getFeatureSource().getFeatures();

            // Convert SimpleFeatureCollection to GeoJSON
            FeatureJSON featureJSON = new FeatureJSON();
            try (FileOutputStream outputStream = new FileOutputStream(geoJsonFilePath)) {
                featureJSON.writeFeatureCollection(features, outputStream);
            }
        }

        // Clean up temporary directory
        FileUtils.deleteDirectory(tempDir);
    }

    private File findShpFile(File directory) {
        // Find the .shp file in the directory
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".shp"));
        return (files != null && files.length > 0) ? files[0] : null;
    }
}
