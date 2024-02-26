package com.transit.mapbox.service;

import com.transit.mapbox.util.CompressionUtil;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.FileUtils;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.referencing.CRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
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

//        convertToFile(zipFile);
//        CompressionUtil cu = new CompressionUtil();
//
//        // 압축 풀기
//        // zip 파일 경로, 압출을 풀을 폴더를변수로 받음
//        cu.unzip(convertToFile(zipFile), tempDir, "UTF-8");

        // ==============================================
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
        // ==============================================
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
    private String convertShpToGeoJSON(File shpFile, File outputDir) throws IOException, FactoryException {
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
        }

        String geoJson;
        shpFile.setReadOnly();
        ShapefileDataStore store = new ShapefileDataStore(shpFile.toURI().toURL());
        SimpleFeatureSource source = store.getFeatureSource();
        SimpleFeatureCollection featureCollection = source.getFeatures();
        FeatureJSON fjson = new FeatureJSON();

        try (StringWriter writer = new StringWriter()) {
            fjson.writeFeatureCollection(featureCollection, writer);
            geoJson = new String(writer.toString().getBytes(StandardCharsets.ISO_8859_1), "EUC-KR");

            // 기본적의로 Feature ID 에 파일명과 INDEX를 같이 붙이고 있기때문에 파일명을 제거하기 위한 작업
            String targetText = shpFile.getName().replace("shp", "");
            geoJson = geoJson.replace(targetText, "");
        }

        return geoJson;
    }
    private File findFile(File directory, String type) {
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(type));
        if (files != null && files.length > 0) {
            return files[0];
        } else {
            return null;
        }
    }
    private void convertFileEncoding(File sourceFile, File targetFile, String sourceEncoding, String targetEncoding) throws IOException {
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