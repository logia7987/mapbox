package com.transit.mapbox.common.service;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GeoFileController {

    @Value("${upload.path}")
    private String uploadPath;

    @PostMapping("/uploadShp")
    public ResponseEntity<Map<String, String>> handleFileUpload(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();

        try {
            // 저장 경로에 파일 저장
            String filePath = uploadPath + file.getOriginalFilename();
            file.transferTo(new File(filePath));

            // SHP 파일을 GeoJSON으로 변환
            SimpleFeatureCollection featureCollection = convertShpToGeoJSON(filePath);

            // 변환된 GeoJSON을 클라이언트에게 반환하거나 저장 등의 작업 수행
            // 여기에서는 일단 변환된 GeoJSON을 로그에 출력
            System.out.println(featureCollection);

            response.put("message", "File uploaded successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            response.put("message", "File upload failed");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private SimpleFeatureCollection convertShpToGeoJSON(String filePath) throws IOException {
        // SHP 파일을 읽어옴
        File file = new File(filePath);
        ShapefileDataStore shpDataStore = new ShapefileDataStore(file.toURI().toURL());
        SimpleFeatureCollection featureCollection = shpDataStore.getFeatureSource().getFeatures();

        // GeoJSON으로 변환
        FeatureJSON featureJSON = new FeatureJSON();
        StringWriter writer = new StringWriter();
        featureJSON.writeFeatureCollection(featureCollection, writer);

        // 반환
        return featureCollection;
    }

}
