package com.golovko.backend.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @ApiOperation(value = "Check service health")
    @GetMapping("/health")
    public void health() {
    }
}