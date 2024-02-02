package com.transit.mapbox.Api.controller;

import com.transit.mapbox.Api.mapper.ShpMapper;
import com.transit.mapbox.Api.vo.ShpVo;
import com.transit.mapbox.common.service.ShapeFileService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MapperScan(value = {"com.transit.mapbox.Api.mapper"})
@Controller
@RequestMapping("/api")
public class ApiController {

    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    private ShpMapper shpMapper;

    @Autowired
    private ShapeFileService shapeFileService;

    @PostMapping(value = "/uploadShp", consumes = "multipart/form-data")
    @ResponseBody
    public Map<String, Object> uploadShp(@RequestParam("shpData") MultipartFile file) throws IOException {
        Map<String, Object> result = new HashMap<>();

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.toLowerCase().endsWith(".zip")) {
            shapeFileService.convertZipToGeoJson(file, uploadPath + "geojson" + File.separator );
//            ShpVo shpVo = new ShpVo();

//            String filePath = uploadPath + file.getOriginalFilename();
//            file.transferTo(new File(filePath));

//            shpVo.setShpname(originalFilename);

//            shpMapper.insert(shpVo);
//            System.out.println("New Shp ID : "+shpVo.getShpid());

//            List<Map<String, Object>> geoMap = shapeFileService.readShapeFileGeometry(filePath);

//            result.put("data", geoMap);
            result.put("result", "success");
            result.put("message", "저장되었습니다.");
        } else {
            // 올바르지 않은 확장자인 경우 에러 메시지 반환
            result.put("result", "fail");
            result.put("message", "파일형식이 올바르지 않습니다.");
        }

        return result;
    }
}
