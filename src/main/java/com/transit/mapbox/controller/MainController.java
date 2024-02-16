package com.transit.mapbox.controller;

import com.transit.mapbox.service.ShpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController{

    @Value("${mapbox.accessToken}")
    private String mapboxAccessToken;

    @Autowired
    private ShpService shpService;

    @GetMapping("/")
    public String main(Model model) {



        // DB 연결 테스트
//        System.out.println("연결여부 카운트 로그 : 0 일때 완료임 =>" + mainMapper.getCount());
        model.addAttribute("shpList", shpService.selectShp());
        model.addAttribute("mapboxAccessToken", mapboxAccessToken);
        return "html/main/index";
    }
}
