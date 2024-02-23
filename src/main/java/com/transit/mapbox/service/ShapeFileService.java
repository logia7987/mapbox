package com.transit.mapbox.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
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

                String result = FileUtils.readFileToString(geojsonFile, StandardCharsets.UTF_8);

                FileUtils.deleteDirectory(tempDir);

                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        FileUtils.deleteDirectory(tempDir);
        return null;
    }
    private File convertShpToGeoJSON(File shpFile, File outputDir) throws IOException, FactoryException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("url", shpFile.toURI().toURL());
        map.put("create spatial index", Boolean.TRUE);

        File shxFile = findFile(tempDir, ".shx");
        if (shxFile != null) {
            map.put("shx", shxFile.toURI().toURL());
        }

        File dbfFile = findFile(tempDir, ".dbf");
        if (dbfFile != null) {
            checkDbfEncoding(shpFile);
            checkDbfEncoding(dbfFile);
            map.put("dbf", dbfFile.toURI().toURL());
//            try {
//                File nbfFile = new File(outputDir, "output.dbf");
//
//                FileInputStream fis = new FileInputStream(dbfFile);
//                DbaseFileReader dbfReader = new DbaseFileReader(fis.getChannel(), true, Charset.forName("Windows-949"));
//                int colSize = dbfReader.getHeader().getNumFields();
//                String[] headers = new String[colSize];
//
//                for(int i=0;i<colSize;i++) {
//                    headers[i]=dbfReader.getHeader().getFieldName(i);
//                }
//
//                FileWriter fileWriter = new FileWriter(nbfFile);
//                CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withHeader(headers));
//
//                // 데이터 추가
//                while (dbfReader.hasNext()) {
//                    Object[] values = dbfReader.readEntry();
//                    csvPrinter.printRecord(values);
//                }
//                csvPrinter.flush();
//
//                dbfReader.close();
//
//                map.put("dbf", nbfFile.toURI().toURL());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }

        File prjFile = findFile(tempDir, ".prj");
        if (prjFile != null) {
            map.put("prj", prjFile.toURI().toURL());
        }

        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];

        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
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
}