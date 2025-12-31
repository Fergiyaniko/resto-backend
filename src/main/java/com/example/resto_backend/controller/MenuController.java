package com.example.resto_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Menu", description = "Operations related to menu management")
@RestController
@RequestMapping("/api/v1/menu")
public class MenuController {

    @GetMapping
    public String getMenu() {
        return "getMenu()";
    }

}
