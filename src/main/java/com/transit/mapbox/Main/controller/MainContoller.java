package com.transit.mapbox.Main.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainContoller {

    @Value("${mapbox.accessToken}")
    private String mapboxAccessToken;

    @GetMapping("/")
    public String main(Model model) {

        model.addAttribute("mapboxAccessToken", mapboxAccessToken);
        return "html/index";
    }

}
