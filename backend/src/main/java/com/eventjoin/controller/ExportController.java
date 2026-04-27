package com.eventjoin.controller;

import com.eventjoin.service.ExcelExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/admin/export")
public class ExportController {
    
    private final ExcelExportService excelExportService;
    
    public ExportController(ExcelExportService excelExportService) {
        this.excelExportService = excelExportService;
    }
    
    @GetMapping("/registrations/{eventId}")
    public ResponseEntity<byte[]> exportRegistrations(@PathVariable Long eventId) {
        byte[] excelData = excelExportService.exportRegistrationsToExcel(eventId);
        
        String filename = URLEncoder.encode("报名列表_" + System.currentTimeMillis() + ".xlsx", 
                StandardCharsets.UTF_8);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }
}
