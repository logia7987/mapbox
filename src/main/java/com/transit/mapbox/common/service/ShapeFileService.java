package com.transit.mapbox.common.service;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class ShapeFileService {
    public void readShapeFileGeometry(String shpFilePath) throws IOException {
        File shpFile = new File(shpFilePath);
        ShapefileDataStore shpDataStore = new ShapefileDataStore(shpFile.toURI().toURL());
        SimpleFeatureIterator featureIterator  = shpDataStore.getFeatureSource().getFeatures().features();

        while (featureIterator.hasNext()) {
            SimpleFeature feature = featureIterator.next()    ;

            // 공간 데이터 출력
            System.out.println("Geometry: " + feature.getDefaultGeometry());
        }
    }
}
