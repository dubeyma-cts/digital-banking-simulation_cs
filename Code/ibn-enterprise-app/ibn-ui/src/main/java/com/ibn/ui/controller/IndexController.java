package com.ibn.ui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
    
    @GetMapping({"/{path:[^\\.]*}", "/**/{path:[^\\.]*}"})
    public String catchAll() {
        return "forward:/index.html";
    }
}
