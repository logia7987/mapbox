package com.transit.mapbox.common.service;

import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
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
import java.io.Serializable;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GeoFileController {

    @Value("${upload.path}")
    private String uploadPath;

    @PostMapping("/uploadShp")
    public ResponseEntity<Map<String, String>> handleFileUpload(@RequestParam("shpData") List<MultipartFile> file) {
        Map<String, String> response = new HashMap<>();

        for (int i = 0; i < file.size(); i++) {
            try {
                // 저장 경로에 파일 저장
                String filePath = uploadPath + file.get(i).getOriginalFilename();
                if (filePath.indexOf(".shp") > 0) {
                    // shp
//                    File shpFile = new File(shpFilePath);
//                    ShapefileDataStore shpDataStore = new ShapefileDataStore(shpFile.toURI().toURL());
//                    SimpleFeatureCollection shpFeatureCollection = shpDataStore.getFeatureSource().getFeatures();
                } else {
                    // shx
//                    File shxFile = new File(shxFilePath);
//                    ShapefileDataStoreFactory shxDataStoreFactory = new ShapefileDataStoreFactory();
//                    Map<String, Serializable> shxParams = new HashMap<>();
//                    shxParams.put("url", shxFile.toURI().toURL());
//                    ShapefileDataStore shxDataStore = (ShapefileDataStore) shxDataStoreFactory.createNewDataStore(shxParams);
//                    SimpleFeatureCollection shxFeatureCollection = shxDataStore.getFeatureSource().getFeatures();
                }
                file.get(i).transferTo(new File(filePath));

                // SHP 파일을 GeoJSON으로 변환
                // SimpleFeatureCollection featureCollection = convertShpToGeoJSON(filePath);

            } catch (IOException e) {
                e.printStackTrace();
                response.put("message", "File upload failed");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        // 변환된 GeoJSON을 클라이언트에게 반환하거나 저장 등의 작업 수행
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void printFeatureAttributes(SimpleFeatureSource featureSource) throws IOException {
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = featureSource.getFeatures();
        FeatureIterator<SimpleFeature> iterator = collection.features();

        try {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                System.out.println("Feature ID: " + feature.getID());
                System.out.println("Attributes: " + feature.getAttributes());
            }
        } finally {
            iterator.close();
        }
    }

}
