package com.ibn.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/core")
public class CoreController {

    @GetMapping("/status")
    public String getStatus() {
        return "Core Service is up and running!";
    }
}