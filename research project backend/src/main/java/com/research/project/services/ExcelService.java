package com.research.project.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ExcelService {

    // Generates an Excel file from extracted data
    public ByteArrayInputStream generateExcel(Map<String, Object> extractedData) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Extracted Data");
            int rowNum = 0;

            // Create header row
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("Entity");
            headerRow.createCell(1).setCellValue("Extracted Values");

            // Extract inner keys and values from "extractedEntities"
            Object extractedEntities = extractedData.get("extractedEntities");
            if (extractedEntities instanceof Map<?, ?> entityMap) {
                for (Map.Entry<?, ?> entry : entityMap.entrySet()) {
                    String category = entry.getKey().toString();
                    Object values = entry.getValue();

                    if (values instanceof List<?>) {
                        List<?> valueList = (List<?>) values;
                        for (Object value : valueList) {
                            Row row = sheet.createRow(rowNum++);
                            row.createCell(0).setCellValue(category);
                            row.createCell(1).setCellValue(value.toString());
                        }
                    }
                }
            }

            // Auto-size columns for readability
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error generating Excel file", e);
        }
    }
}
