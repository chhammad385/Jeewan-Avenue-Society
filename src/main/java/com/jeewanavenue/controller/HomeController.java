package com.jeewanavenue.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/home.html";
    }
    
    @GetMapping("/index")
    public String redirectIndexToHome() {
        return "redirect:/home.html";
    }
}