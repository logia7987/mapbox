package com.transit.mapbox.controller;

import com.transit.mapbox.service.FeatureService;
import com.transit.mapbox.service.ShpService;
import com.transit.mapbox.vo.CoordinateVo;
import com.transit.mapbox.vo.FeatureVo;
import com.transit.mapbox.vo.ShpVo;
import com.transit.mapbox.service.ShapeFileService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private ShpService shpService;

    @Autowired
    private FeatureService featureService;

    @Autowired
    private ShapeFileService shapeFileService;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String name;

    @Value("${spring.datasource.password}")
    private String pw;

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

                JSONArray jsonArray = (JSONArray) jsonObj.get("features");

                ShpVo shpVo = new ShpVo();
                shpVo.setShpName(originalFilename);

                Long shpId= shpService.saveShp(shpVo);

                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject aObj = (JSONObject) jsonArray.get(i);

                    FeatureVo featureVo = new FeatureVo();
                    featureVo.setShpId(shpId);
                    featureVo.setType((String) aObj.get("type"));
                    featureVo.setSeq(i);

                    featureService.saveFeature(featureVo);

//                    JSONArray geoArr = (JSONArray) ((JSONArray) ((JSONArray) ((JSONObject)aObj.get("geometry")).get("coordinates")).get(0)).get(0);
//                    processCoordinates(geoArr, featureVo.getFeatureId());
                }

                result.put("data", jsonObj);
            }

            result.put("result", "success");
            result.put("message", "저장되었습니다.");
        } else {
            // 올바르지 않은 확장자인 경우 에러 메시지 반환
            result.put("result", "fail");
            result.put("message", "파일형식이 올바르지 않습니다.");
        }

        return result;
    }
    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url, name, pw);
    }
    public void processCoordinates(JSONArray geoArr, Long featureId) {
        List<CoordinateVo> coordinateList = new ArrayList<>();

        for (Object o : geoArr) {
            JSONArray aCoor = (JSONArray) o;

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
            coordinateVo.setFeatureId(featureId);

            coordinateList.add(coordinateVo);
        }

        // Batch Insert 수행
        batchInsertCoordinates(coordinateList);
    }

    public void batchInsertCoordinates(List<CoordinateVo> coordinateList) {
        try {
            // Auto-commit 모드를 비활성화하여 수동으로 커밋 관리
            Connection connection = createConnection();
            connection.setAutoCommit(false);

            String insertQuery = "INSERT INTO COORDINATES_TABLE (COORDINATES_ID, COORDINATE_X, COORDINATE_Y, FEATURE_ID) VALUES (COORD_SEQ.NEXTVAL, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                for (CoordinateVo coordinateVo : coordinateList) {
                    preparedStatement.setDouble(1, coordinateVo.getCoordinateX());
                    preparedStatement.setDouble(2, coordinateVo.getCoordinateY());
                    preparedStatement.setLong(3, coordinateVo.getFeatureId());
                    preparedStatement.addBatch();
                }

                // Batch 수행
                int[] result = preparedStatement.executeBatch();

                // Batch 수행 후 수동으로 커밋
                connection.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
