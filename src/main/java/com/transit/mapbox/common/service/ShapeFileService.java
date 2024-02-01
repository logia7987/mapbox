package com.transit.mapbox.common.service;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@Service
public class ShapeFileService {



    public HashMap<String, Object>  readShapeFileGeometry(String shpFilePath) throws IOException {
        HashMap<String, Object> result = new HashMap<>();

        File shpFile = new File(shpFilePath);
        ShapefileDataStore shpDataStore = new ShapefileDataStore(shpFile.toURI().toURL());
        SimpleFeatureIterator featureIterator  = shpDataStore.getFeatureSource().getFeatures().features();

        int i = 0;
        while (featureIterator.hasNext()) {
            SimpleFeature feature = featureIterator.next();

            // 공간 데이터 출력
            System.out.println("Geometry: " + feature.getDefaultGeometry());

            result.put(i+"", feature.getDefaultGeometry());
        }
        return result;
    }


}
