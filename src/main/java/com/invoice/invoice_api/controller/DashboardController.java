package com.invoice.invoice_api.controller;

import com.invoice.invoice_api.dto.dashboard.DashboardSummaryResponseDTO;
import com.invoice.invoice_api.service.dashboard.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponseDTO> summary() {
        return ResponseEntity.ok(dashboardService.summary());
    }
}
