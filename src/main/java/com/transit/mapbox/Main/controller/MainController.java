package com.transit.mapbox.Main.controller;

import com.transit.mapbox.Main.mapper.MainMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @Autowired
    private MainMapper mainMapper;

    @Value("${mapbox.accessToken}")
    private String mapboxAccessToken;

    @GetMapping("/")
    public String main(Model model) {

        // DB 연결 테스트
//        System.out.println("연결여부 카운트 로그 : 0 일때 완료임 =>" + mainMapper.getCount());

        model.addAttribute("mapboxAccessToken", mapboxAccessToken);
        return "html/main/index";
    }


}
