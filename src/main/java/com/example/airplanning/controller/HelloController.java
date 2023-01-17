package com.example.airplanning.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HelloController {

    @GetMapping("/hello")
    @ApiOperation(value = "hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok().body("4조의 air planning 입니다.");
    }

}
