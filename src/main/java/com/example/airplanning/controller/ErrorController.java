package com.example.airplanning.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ErrorController {

    @GetMapping("/error/redirect")
    public String accessDenied(){
        return "error/redirect";
    }

}
