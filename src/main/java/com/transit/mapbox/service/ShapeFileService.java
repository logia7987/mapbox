package com.transit.mapbox.service;

import org.apache.commons.io.FileUtils;

import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CRSAuthorityFactory;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.factory.Hints;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
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

                String result = convertShpToGeoJSON(shpFile, tempDir);

                FileUtils.deleteDirectory(tempDir);

                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        FileUtils.deleteDirectory(tempDir);
        return null;
    }
    public String convertShpToGeoJSON(File shpFile, File outputDir) throws IOException, FactoryException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("url", shpFile.toURI().toURL());
        map.put("create spatial index", Boolean.TRUE);

        File shxFile = findFile(tempDir, ".shx");
        if (shxFile != null) {
            map.put("shx", shxFile.toURI().toURL());
        }

        File dbfFile = findFile(tempDir, ".dbf");
        if (dbfFile != null) {
            map.put("dbf", dbfFile.toURI().toURL());
        }

        File prjFile = findFile(tempDir, ".prj");
        if (prjFile != null) {
            map.put("prj", prjFile.toURI().toURL());
            // 추출된 좌표계를 WGS 84로 변환
            CoordinateReferenceSystem sourceCRS = extractCRS(prjFile);
            String geoJson = getString(shpFile, sourceCRS);
            return geoJson;
        }

        // .prj 파일이 없는 경우
        String geoJson = getString(shpFile, null); // 원래 좌표계를 명시하지 않음
        FileUtils.deleteDirectory(tempDir);
        return geoJson;
    }

    private static String getString(File shpFile, CoordinateReferenceSystem sourceCRS) throws IOException {
        String geoJson;
        shpFile.setReadOnly();
        ShapefileDataStore store = new ShapefileDataStore(shpFile.toURI().toURL());
        SimpleFeatureSource source = store.getFeatureSource();
        SimpleFeatureCollection featureCollection = source.getFeatures();
        FeatureJSON fjson = new FeatureJSON();

        try (StringWriter writer = new StringWriter()) {
            fjson.writeFeatureCollection(featureCollection, writer);
            geoJson = new String(writer.toString().getBytes(StandardCharsets.ISO_8859_1), "EUC-KR");

            // 원래 좌표계가 있는 경우, WGS 84로 변환
            if (sourceCRS != null) {
                geoJson = convertToWGS84(geoJson, sourceCRS);
            }

            // 기본적의로 Feature ID 에 파일명과 INDEX를 같이 붙이고 있기때문에 파일명을 제거하기 위한 작업
            String targetText = shpFile.getName().replace("shp", "");
            geoJson = geoJson.replace(targetText, "");
        } catch (FactoryException | TransformException e) {
            throw new RuntimeException(e);
        }
        return geoJson;
    }

    private static CoordinateReferenceSystem extractCRS(File prjFile) throws IOException, FactoryException {
        String prjText = FileUtils.readFileToString(prjFile, StandardCharsets.UTF_8);
        return CRS.parseWKT(prjText);
    }

    private static String convertToWGS84(String geoJson, CoordinateReferenceSystem sourceCRS) throws IOException, FactoryException, TransformException {
        System.setProperty("org.geotools.referencing.forceXY", "true");

        CoordinateReferenceSystem targetCRS = org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;

        MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
        System.out.println(sourceCRS + " -> " + targetCRS);

        // GeoJSON을 FeatureCollection으로 변환
        FeatureJSON fjson = new FeatureJSON();
        SimpleFeatureCollection featureCollection = (SimpleFeatureCollection) fjson.readFeatureCollection(new StringReader(geoJson));

        // 좌표계 변환
        StringWriter writer = new StringWriter();
        fjson.writeFeatureCollection(transformFeatureCollection(featureCollection, transform), writer);
        return writer.toString();
    }

    private static SimpleFeatureCollection transformFeatureCollection(SimpleFeatureCollection featureCollection, MathTransform transform) throws TransformException {
        DefaultFeatureCollection transformedCollection = new DefaultFeatureCollection();
        SimpleFeatureIterator iterator = featureCollection.features();
        while (iterator.hasNext()) {
            SimpleFeature feature = iterator.next();
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            Geometry transformedGeometry = JTS.transform(geometry, transform);
            feature.setDefaultGeometry(transformedGeometry);
            transformedCollection.add(feature);
        }
        iterator.close();
        return transformedCollection;
    }

    public File findFile(File directory, String type) {
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(type));
        if (files != null && files.length > 0) {
            return files[0];
        } else {
            return null;
        }
    }
    public void convertFileEncoding(File sourceFile, File targetFile, String sourceEncoding, String targetEncoding) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), sourceEncoding));
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), targetEncoding))) {
            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line);
                bw.newLine();  // 개행 문자 추가 (optional, 파일에 따라 다르게 처리될 수 있음)
            }
        }
    }

    private String checkDbfEncoding(File file) {
        try {
            URL url = file.toURI().toURL();
            ShapefileDataStore ds = new ShapefileDataStore(url);
            SimpleFeatureCollection fc = ds.getFeatureSource(ds.getTypeNames()[0]).getFeatures();

            String encoding = ds.getCharset().toString();
            System.out.println("shp schema encoding : "+encoding);
            return encoding;
        } catch (IOException ie) {
            ie.printStackTrace();
        }

        return "";
    }

    public File convertToFile(MultipartFile zipfile) throws IOException {
        File file = new File(tempDir, zipfile.getOriginalFilename());
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(zipfile.getBytes());
        fos.close();
        return file;
    }
}