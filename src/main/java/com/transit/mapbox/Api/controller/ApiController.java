package com.transit.mapbox.Api.controller;

import com.transit.mapbox.common.service.ShapeFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    private ShpMapper mapper;

    @Autowired
    private ShapeFileService shapeFileService;

    @PostMapping(value = "/uploadShp", consumes = "multipart/form-data")
    public Map<String, Object> uploadShp(@RequestParam("shpData") MultipartFile file) throws IOException {
        Map<String, Object> result = new HashMap<>();

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.toLowerCase().endsWith(".shp")) {
            ShpVo shpVo = new ShpVo();

            String filePath = uploadPath + file.getOriginalFilename();
            file.transferTo(new File(filePath));

            shpVo.setShpname(originalFilename);

            mapper.insert(shpVo);

            result.put("result", "success");
            result.put("message", "저장되었습니다.");
            result.put("data", shapeFileService.readShapeFileGeometry(filePath));
        } else {
            // 올바르지 않은 확장자인 경우 에러 메시지 반환
            result.put("result", "fail");
            result.put("message", "파일형식이 올바르지 않습니다.");
        }

        return result;
    }
}
