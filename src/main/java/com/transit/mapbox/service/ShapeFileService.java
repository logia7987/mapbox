package com.transit.mapbox.service;

import org.apache.commons.io.FileUtils;

import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.data.DataUtilities;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShapeFileService {

    private static File tempDir = new File("C:\\mapbox\\shapefile_temp");

    public String convertShpToGeoJSON(File shpFile, File outputDir) throws IOException, FactoryException {
        File prjFile = findFile(outputDir, ".prj");

        CoordinateReferenceSystem sourceCRS = prjFile != null ? extractCRS(prjFile) : null;

        String geoJson = getString(shpFile, sourceCRS);

        return geoJson;
    }

    private String getString(File shpFile, CoordinateReferenceSystem sourceCRS) throws IOException, FactoryException {
        String geoJson;
        ShapefileDataStore store = new ShapefileDataStore(shpFile.toURI().toURL());
        SimpleFeatureSource source = store.getFeatureSource();
        SimpleFeatureCollection featureCollection = source.getFeatures();
        FeatureJSON fjson = new FeatureJSON();

        // 원래 좌표계가 WGS84가 아니고, sourceCRS가 null이 아닌 경우에만 변환 수행
        if (sourceCRS != null && !"EPSG:4326".equals(sourceCRS.getName())) {
            // 좌표 변환 수행
            featureCollection = transformFeatureCollection(featureCollection, sourceCRS);
        }

        try (StringWriter writer = new StringWriter()) {
            fjson.writeFeatureCollection(featureCollection, writer);
            geoJson = writer.toString();

            // 파일명에서 .shp 확장자 제거
            String targetText = shpFile.getName().replace(".shp", "");
            geoJson = geoJson.replace(targetText, "");

            return geoJson;
        } finally {
            FileUtils.deleteDirectory(tempDir);
        }
    }

    public CoordinateReferenceSystem extractCRS(File prjFile) throws IOException, FactoryException {
        String prjText = FileUtils.readFileToString(prjFile, StandardCharsets.UTF_8);
        return CRS.parseWKT(prjText);
    }

    public File findFile(File directory, String type) {
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(type));
        if (files != null && files.length > 0) {
            return files[0];
        } else {
            return null;
        }
    }

    // 좌표 변환을 수행하는 메서드
    private SimpleFeatureCollection transformFeatureCollection(SimpleFeatureCollection featureCollection, CoordinateReferenceSystem sourceCRS) {
        // 타겟 좌표계 (WGS84)
        CoordinateReferenceSystem targetCRS = DefaultGeographicCRS.WGS84;

        // 변환을 위한 MathTransform 생성
        MathTransform transform;
        try {
            transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
        } catch (FactoryException e) {
            throw new RuntimeException("좌표 변환을 위한 MathTransform을 생성하는 동안 오류가 발생했습니다.", e);
        }

        // FeatureCollection의 각 Geometry를 변환
        SimpleFeatureIterator iterator = featureCollection.features();
        try {
            List<SimpleFeature> transformedFeatures = new ArrayList<>();
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                Geometry transformedGeometry;
                try {
                    // 좌표 변환 적용
                    transformedGeometry = JTS.transform(geometry, transform);
                } catch (TransformException e) {
                    throw new RuntimeException("Geometry를 변환하는 동안 오류가 발생했습니다.", e);
                }

                // 변환된 Geometry로 SimpleFeature를 업데이트
                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(feature.getFeatureType());
                featureBuilder.addAll(feature.getAttributes());
                featureBuilder.set(feature.getDefaultGeometryProperty().getName(), transformedGeometry);
                SimpleFeature transformedFeature = featureBuilder.buildFeature(null);
                transformedFeatures.add(transformedFeature);
            }

            // 변환된 FeatureCollection 반환
            return DataUtilities.collection(transformedFeatures);
        } finally {

        }
    }
}