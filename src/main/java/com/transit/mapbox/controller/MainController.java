package com.transit.mapbox.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @Value("${mapbox.accessToken}")
    private String mapboxAccessToken;

    @GetMapping("/")
    public String main(Model model) {



        model.addAttribute("mapboxAccessToken", mapboxAccessToken);
        return "html/main/index";
    }


}
