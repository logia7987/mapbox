package com.transit.mapbox.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

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
                    Long featureId = feature.getFeatureId();

                    feature.setGeometryVo(geometryService.getGeometryByFeature(feature));
                }

                result.put("result", "success");
                result.put("data", convertToGeoJson(features));
//                result.put("data", shpService.getShpDataById(Long.valueOf(shpId)));
//            result.put("data", convertEntityToJson(shpService.getShpDataById(Long.valueOf(shpId))));
            } else {
                result.put("result", "fail");
                result.put("message", "불러오는데 실패했습니다. 관리자에게 문의해주세요.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public ShpVo convertToShpData(JSONObject object) {
        ShpVo shpVo = new ShpVo();
        List<FeatureVo> featureVos = new ArrayList<>();

        JSONArray jsonArray = (JSONArray) object.get("features");
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject aObj = (JSONObject) jsonArray.get(i);

            FeatureVo featureVo = new FeatureVo();
            GeometryVo geometryVo = new GeometryVo();

            // feature 저장
            featureVo.setFeatureId((long) (i+1));
            featureVo.setType((String) aObj.get("type"));
            featureVo.setSeq(i);
            featureVo.setProperties(aObj.get("properties").toString());

            // feature별 geometry 저장
            JSONObject geometryJson = (JSONObject) aObj.get("geometry");
            geometryVo.setType((String) geometryJson.get("type"));
            geometryVo.setCoordinates(geometryJson.get("coordinates").toString());

            featureVos.add(featureVo);
        }
        shpVo.setFeatureVos(featureVos);

        return shpVo;
    }

    @PostMapping(value = "/saveShp")
    @ResponseBody
    public ShpVo saveShpData(@RequestParam(value = "shpName") String shpName) {
        ShpVo shpVo = new ShpVo();
        shpVo.setShpName(shpName);
        return shpService.saveShp(shpVo);

//        try {
//            ShpVo shpVo = convertToShpData(jsonObj);
//            shpVo.setShpName(shpName);
//            shpService.saveShp(shpVo);
//
//            List<FeatureVo> featureVoList = shpVo.getFeatureVos();
//            for (FeatureVo featureVo : featureVoList) {
//                featureVo.setShpVo(shpVo);
//                featureService.saveFeature(featureVo);
//                geometryService.saveGeometry(featureVo.getGeometryVo());
//            }
//
//            Map<String, Object> result = new HashMap<>();
//            result.put("success", true);
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            e.printStackTrace();
//            Map<String, Object> result = new HashMap<>();
//            result.put("success", false);
//            result.put("error", e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
//        }
    }

    @PostMapping(value = "/saveFeature")
    @ResponseBody
    public Map<String, Object> saveFeature(@RequestBody Map<String, Object>params) throws IOException {
        Map<String, Object> resultMap = new HashMap<>();

        Long shpId = Long.valueOf((int) params.get("shpId"));
        ShpVo shpVo = shpService.getShp(Long.valueOf(shpId));

        List<FeatureVo> featureVoList = new ArrayList<>();
        Map<String, Object> dataMap = (Map<String, Object>) params.get("jsonObject");

        FeatureVo featureVo = new FeatureVo();
        featureVo.setShpVo(shpVo);
        featureVo.setType((String) dataMap.get("type"));
        featureVo.setProperties(convertToJSONString(String.valueOf(dataMap.get("properties"))));

        FeatureVo nFeature = featureService.saveFeature(featureVo);

        Map<String, Object> geometry = (Map<String, Object>) dataMap.get("geometry");
        GeometryVo geometryVo = new GeometryVo();
        geometryVo.setFeatureVo(nFeature);
        geometryVo.setType((String) geometry.get("type"));
        geometryVo.setCoordinates(concatenateArray(geometry.get("coordinates").toString()));

        geometryService.saveGeometry(geometryVo);

        return params;
    }


    private boolean checkFileType(String FileName) {
        return FileName.toLowerCase().endsWith(".shp") || FileName.toLowerCase().endsWith(".dbf") || FileName.toLowerCase().endsWith(".shx");
    }

    public JSONObject convertToGeoJson(List<FeatureVo> features) throws ParseException, IOException {
        JSONObject result = new JSONObject();
        result.put("type", "FeatureCollection");

        JSONParser parser = new JSONParser();

        JSONArray jsonFeatures = new JSONArray();
        for (FeatureVo feature : features) {
            JSONObject jsonFeature = new JSONObject();
            jsonFeature.put("type", feature.getType());
            jsonFeature.put("id", feature.getSeq());
            jsonFeature.put("properties", parseStringToMap(feature.getProperties()));

            GeometryVo geometryVos = feature.getGeometryVo();
            JSONObject jsonGeometry = new JSONObject();
            jsonGeometry.put("type", geometryVos.getType());

            JSONArray coordinates = new JSONArray();
            coordinates.add(parser.parse(geometryVos.getCoordinates()));

            jsonGeometry.put("coordinates", coordinates);

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

    private static String convertToJSONString(String input) throws IOException {
        // 키와 값을 더블 쿼트로 둘러싸기
        String jsonString = input.replaceAll("(\\w+)(=)", "\"$1\"$2")
                .replaceAll("([^\\s,={}]+)", "\"$1\"");

        return jsonString;
    }

    private static String concatenateArray(String arrayString) throws IOException {
        // 대괄호 안의 문자열만 추출
        String content = arrayString.substring(1, arrayString.length() - 1);

        // 문자열을 쉼표로 분리하여 리스트로 변환
        List<String> values = Arrays.asList(content.split("\\s*,\\s*"));

        // 각 값을 따옴표로 둘러싸인 문자열로 변환하여 합치기
        String concatenatedString = values.stream()
                .map(value -> "\"" + value + "\"")
                .collect(Collectors.joining(", "));

        return concatenatedString;
    }
}

