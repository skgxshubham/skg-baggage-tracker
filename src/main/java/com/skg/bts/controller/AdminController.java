package com.skg.bts.controller;

import com.skg.bts.dto.BaggageResponse;
import com.skg.bts.service.BaggageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final BaggageService baggageService;

    @GetMapping("/baggage/mishandled")
    public List<BaggageResponse> mishandled() {
        return baggageService.findMishandled();
    }
}
