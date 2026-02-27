package com.ibn.external.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/external")
public class ExternalController {

    @GetMapping("/status")
    public String getStatus() {
        return "External Service is up and running!";
    }
}