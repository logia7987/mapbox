package com.transit.mapbox.controller;

import com.transit.mapbox.service.CoordinateService;
import com.transit.mapbox.service.FeatureService;
import com.transit.mapbox.service.ShpService;
import com.transit.mapbox.vo.CoordinateVo;
import com.transit.mapbox.vo.FeatureVo;
import com.transit.mapbox.vo.ShpVo;
import com.transit.mapbox.service.ShapeFileService;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private ShpService shpService;

    @Autowired
    private FeatureService featureService;

    @Autowired
    private CoordinateService coordinateService;

    @Autowired
    private ShapeFileService shapeFileService;
    private JSONObject jsonObj;
    private static File tempDir = new File("C:\\mapbox\\shapefile_temp");
    @RequestMapping(value = "/uploadShapeFiles", consumes = "multipart/form-data", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public Map<String, Object> uploadShapeFiles(@RequestParam("shpData") List<MultipartFile> files) throws IOException, ParseException {
        Map<String, Object> result = new HashMap<>();

        System.out.println(files.size());

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
                    jsonObj = (JSONObject) obj;

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
                jsonObj = (JSONObject) obj;

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
        System.out.println("============시작==============");
        System.currentTimeMillis();
//        shpService.getShpDataById(Long.valueOf(shpId));

        if (shpId != null) {
            result.put("result", "success");
//            result.put("data", shpService.getShpDataById(Long.valueOf(shpId)));
        } else {
            result.put("result", "fail");
            result.put("message", "불러오는데 실패했습니다. 관리자에게 문의해주세요.");
        }
        System.currentTimeMillis();
        System.out.println("============종료==============");
        return result;
    }

    public ShpVo convertToShpData(JSONObject object) {
        ShpVo shpVo = new ShpVo();
        List<FeatureVo> featureVos = new ArrayList<>();

        JSONArray jsonArray = (JSONArray) object.get("features");
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject aObj = (JSONObject) jsonArray.get(i);

            FeatureVo featureVo = new FeatureVo();
            featureVo.setType((String) aObj.get("type"));
            featureVo.setSeq(i);

            JSONArray geoArr = (JSONArray) ((JSONArray) ((JSONArray) ((JSONObject)aObj.get("geometry")).get("coordinates")).get(0)).get(0);

            List<CoordinateVo> coordinateVos = new ArrayList<>();
            for (Object o : geoArr) {
                CoordinateVo coordinateVo = getCoordinateVo((JSONArray) o, featureVo);
                coordinateVos.add(coordinateVo);
            }

            featureVo.setCoordinateVos(coordinateVos);
            featureVos.add(featureVo);
        }
        shpVo.setFeatureVos(featureVos);

        return shpVo;
    }

    @PostMapping(value = "/saveShp")
    public Map<String, Object> saveShpData(@RequestParam(value = "filename") String shpName) {
        ShpVo shpVo = convertToShpData(jsonObj);
        shpVo.setShpName(shpName);
        shpService.saveShp(shpVo);

        Map<String, Object> result = new HashMap<>();

        List<FeatureVo> featureVoList = shpVo.getFeatureVos();
         int i = 0;
        for (FeatureVo featureVo : featureVoList) {
            featureVo.setShpVo(shpVo);

            featureService.saveFeature(featureVo);
            System.out.println("feature " + i + ": " + featureVo.getCoordinateVos().size());

            coordinateService.saveAllCoordinates(featureVo.getCoordinateVos());
        }
        return result;
    }
    private static CoordinateVo getCoordinateVo(JSONArray o, FeatureVo featureVo) {
        JSONArray aCoor = o;

        Object aX = aCoor.get(0);
        Object aY = aCoor.get(1);

        if (aX instanceof Long) {
            aX = ((Long) aX).doubleValue();
        }

        if (aY instanceof Long) {
            aY = ((Long) aY).doubleValue();
        }

        CoordinateVo coordinateVo = new CoordinateVo();
        coordinateVo.setCoordinateX((Double) aX);
        coordinateVo.setCoordinateY((Double) aY);
        coordinateVo.setFeatureVo(featureVo);
        return coordinateVo;
    }

    private boolean checkFileType(String FileName) {
        return FileName.toLowerCase().endsWith(".shp") || FileName.toLowerCase().endsWith(".dbf") || FileName.toLowerCase().endsWith(".shx");
    }
}
