package com.research.project.controllers;


import com.research.project.dtos.ExtractionRequest;
import com.research.project.services.ExcelService;
import com.research.project.services.ExtractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin("*") // Allow frontend access
public class ExtractionController {

    @Autowired
    private ExtractionService extractionService;

    @Autowired
    private ExcelService excelService;

//    // Upload user stories file
//    @PostMapping("/upload")
//    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
//        try {
//            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
//            return ResponseEntity.ok(content);
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body("Error reading file");
//        }
//    }

    @PostMapping("/extract")
    public ResponseEntity<Map<String, Object>> extractEntities(
            @RequestParam("file") MultipartFile file,
            @RequestParam("model") String model,
            @RequestParam("options") List<String> options) {

        ExtractionRequest request = new ExtractionRequest();
        request.setFile(file);
        request.setModel(model);
        request.setOptions(options);

        Map<String, Object> extractedData = extractionService.extractEntities(request);
        return ResponseEntity.ok(extractedData);
    }



    // Download extracted data as Excel
    @PostMapping("/download")
    public ResponseEntity<InputStreamResource> downloadExcel(@RequestBody Map<String, Object> extractedData) {
        InputStreamResource file = new InputStreamResource(excelService.generateExcel(extractedData));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=extracted_data.xlsx")
                .body(file);
    }
}
