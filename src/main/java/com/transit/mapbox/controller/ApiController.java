package com.transit.mapbox.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transit.mapbox.service.*;
import com.transit.mapbox.vo.FeatureVo;
import com.transit.mapbox.vo.GeometryVo;
import com.transit.mapbox.vo.ShpVo;
import org.apache.commons.io.FileUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Controller
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private ShpService shpService;

    @Autowired
    private FeatureService featureService;

    @Autowired
    private GeometryService geometryService;

    @Autowired
    private ShapeFileService shapeFileService;
    private static File tempDir = new File("C:\\mapbox\\shapefile_temp");
    @RequestMapping(value = "/uploadShapeFiles", consumes = "multipart/form-data", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public Map<String, Object> uploadShapeFiles(@RequestParam("shpData") List<MultipartFile> files) throws IOException, ParseException {
        Map<String, Object> result = new HashMap<>();

        try {
            FileUtils.forceMkdir(tempDir);
            for (MultipartFile aFile : files) {
                Path filePath = new File(tempDir, aFile.getOriginalFilename()).toPath();
                Files.copy(aFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            File shpFile = shapeFileService.findFile(tempDir, ".shp");

            if (shpFile != null) {
                try {
                    String jsonResult = shapeFileService.convertShpToGeoJSON(shpFile, tempDir);
                    JSONParser jsonParser = new JSONParser();
                    Object obj = jsonParser.parse(jsonResult);
                    JSONObject jsonObj = (JSONObject) obj;

                    result.put("fileName", shpFile.getName().replace(".shp", ""));
                    result.put("data", jsonObj);
                } catch (Exception e) {
                    e.printStackTrace();
                    result.put("error", "Shp to GeoJSON 변환 중 오류 발생");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            result.put("error", "파일 처리 중 오류 발생");
        }

        return result;
    }

    @PostMapping(value = "/uploadShp", consumes = "multipart/form-data", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public Map<String, Object> uploadShp(@RequestParam("shpData") MultipartFile file) throws IOException, ParseException {
        Map<String, Object> result = new HashMap<>();

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.toLowerCase().endsWith(".zip")) {
            String geoJson = shapeFileService.convertZipToGeoJson(file);

            if (geoJson != null && !geoJson.equals("")) {
                JSONParser jsonParser = new JSONParser();
                Object obj = jsonParser.parse(geoJson);
                JSONObject jsonObj = (JSONObject) obj;

                result.put("data", jsonObj);
                result.put("result", "success");
            } else {
                result.put("result", "fail");
                result.put("message", "파일형식이 올바르지 않습니다.");
            }
        } else {
            // 올바르지 않은 확장자인 경우 에러 메시지 반환
            result.put("result", "fail");
            result.put("message", "파일형식이 올바르지 않습니다.");
        }

        return result;
    }

    @PostMapping(value = "/getShp", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public Map<String, Object>getShp(@RequestParam("shpId") String shpId) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (shpId != null) {
                ShpVo shpVo = shpService.getShp(Long.valueOf(shpId));
                List<FeatureVo> features = featureService.getFeatures(shpVo);
                for (FeatureVo feature: features) {
                    feature.setGeometryVo(geometryService.getGeometryByFeature(feature));
                }

                result.put("result", "success");
                result.put("data", convertToGeoJson(features));
            } else {
                result.put("result", "fail");
                result.put("message", "불러오는데 실패했습니다. 관리자에게 문의해주세요.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @PostMapping(value = "/saveShp")
    @ResponseBody
    public ShpVo saveShpData(@RequestParam(value = "shpName") String shpName) {
        ShpVo shpVo = new ShpVo();
        shpVo.setShpName(shpName);
        return shpService.saveShp(shpVo);
    }

    @PostMapping(value = "/saveFeature")
    @ResponseBody
    public Map<String, Object> saveFeature(@RequestBody Map<String, Object>params) throws IOException {
        Map<String, Object> resultMap = new HashMap<>();

        Long shpId = Long.valueOf((int) params.get("shpId"));
        int seq = Integer.valueOf(params.get("seq")+"");

        ShpVo shpVo = shpService.getShp(Long.valueOf(shpId));

        Map<String, Object> dataMap = (Map<String, Object>) params.get("jsonObject");

        FeatureVo featureVo = new FeatureVo();
        featureVo.setShpVo(shpVo);
        featureVo.setSeq(seq);
        featureVo.setType((String) dataMap.get("type"));
        featureVo.setProperties(convertToJSONString(String.valueOf(dataMap.get("properties"))));

        FeatureVo nFeature = featureService.saveFeature(featureVo);

        Map<String, Object> geometry = (Map<String, Object>) dataMap.get("geometry");
        GeometryVo geometryVo = new GeometryVo();
        geometryVo.setFeatureVo(nFeature);
        geometryVo.setType((String) geometry.get("type"));
//        geometryVo.setCoordinates(geometry.toString());
        geometryVo.setCoordinates(geometry.get("coordinates").toString());

        geometryService.saveGeometry(geometryVo);

        return params;
    }

    public JSONObject convertToGeoJson(List<FeatureVo> features) throws ParseException, IOException {
        JSONObject result = new JSONObject();
        result.put("type", "FeatureCollection");

        JSONArray jsonFeatures = new JSONArray();
        for (FeatureVo feature : features) {
            JSONObject jsonFeature = new JSONObject();
            jsonFeature.put("type", feature.getType());
            jsonFeature.put("id", feature.getSeq());
            jsonFeature.put("properties", parseStringToMap(feature.getProperties()));

            GeometryVo geometryVos = feature.getGeometryVo();
            JSONObject jsonGeometry = new JSONObject();

            jsonGeometry.put("type", geometryVos.getType());
            jsonGeometry.put("coordinates", parseStringToArray(geometryVos.getCoordinates(), String.valueOf(feature.getSeq())));

            jsonFeature.put("geometry", jsonGeometry);
            jsonFeatures.add(jsonFeature);
        }
        result.put("features", jsonFeatures);

        return result;
    }

    private static Map<String, Object> parseStringToMap(String input) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(input, Map.class);
    }

    private static JSONArray parseStringToArray(String jsonString, String id) throws IOException {
        try {

            JSONArray coordinateInArr = new JSONArray();
            JSONArray coordinateOutArr = new JSONArray();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);

            // Coordinates에 접근
            // 더 깊게 접근하려면 반복문 사용
            for (JsonNode coordinatesNode : jsonNode) {
                for (JsonNode node : coordinatesNode) {
                    JSONArray coordinateArr = new JSONArray();
                    for (JsonNode subNode : node) {
                        JSONArray aCoordinate = new JSONArray();

                        double longitude = subNode.get(0).asDouble();
                        double latitude = subNode.get(1).asDouble();

                        aCoordinate.add(longitude);
                        aCoordinate.add(latitude);

                        coordinateArr.add(aCoordinate);
                    }

//                    if (jsonNode.size() > 1) {
//                        JSONArray skinArr = new JSONArray();
//                        skinArr.add(coordinateArr);
//                        coordinateOutArr.add(skinArr);
//                    } else {
                        coordinateOutArr.add(coordinateArr);
//                    }
                }
            }
            coordinateInArr.add(coordinateOutArr);

            return coordinateInArr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String convertToJSONString(String input) throws IOException {
        // 키와 값을 더블 쿼트로 둘러싸기
        return input.replaceAll("(\\w+)=([^,{}]+)", "\"$1\":\"$2\"");
    }

    private static JsonArray convertToJsonArray(String jsonCoordinates) {
        // JsonArrayBuilder 생성
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        // JSON 문자열을 파싱하여 JsonArray로 변환
        // 여기서는 간단하게 JsonArray의 첫 번째 요소로만 변환하였습니다.
        jsonArrayBuilder.add(Json.createReader(new StringReader(jsonCoordinates)).readArray());

        // 완성된 JsonArray 반환
        return jsonArrayBuilder.build();
    }
}

