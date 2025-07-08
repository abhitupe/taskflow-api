package com.taskflow.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "TaskFlow API is running!!!";
    }

    @GetMapping("/health")
    public String health(){
        return "Application is healthy!";
    }

}
