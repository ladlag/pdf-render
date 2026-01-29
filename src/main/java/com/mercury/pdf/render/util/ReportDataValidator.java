package com.mercury.pdf.render.util;

import com.mercury.pdf.render.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for validating report data structures.
 * Provides comprehensive validation to ensure data integrity before PDF generation.
 */
public class ReportDataValidator {
    
    /**
     * Validates a ReportData instance and returns a list of validation errors.
     * An empty list indicates the data is valid.
     * 
     * @param reportData The report data to validate
     * @return List of validation error messages (empty if valid)
     */
    public static List<String> validate(ReportData reportData) {
        List<String> errors = new ArrayList<>();
        
        if (reportData == null) {
            errors.add("ReportData cannot be null");
            return errors;
        }
        
        // Validate title
        if (reportData.getTitle() == null || reportData.getTitle().trim().isEmpty()) {
            errors.add("Report title is required and cannot be empty");
        }
        
        // Check for content
        boolean hasContent = reportData.hasLegacyContent() || reportData.hasSections();
        if (!hasContent) {
            errors.add("Report must contain at least one section or content item");
        }
        
        // Validate table blocks
        if (reportData.getTableBlocks() != null) {
            for (int i = 0; i < reportData.getTableBlocks().size(); i++) {
                TableBlock block = reportData.getTableBlocks().get(i);
                List<String> blockErrors = validateTableBlock(block, i);
                errors.addAll(blockErrors);
            }
        }
        
        // Validate summary table
        if (reportData.getSummaryTable() != null) {
            List<String> tableErrors = validateTableData(reportData.getSummaryTable(), "Summary table");
            errors.addAll(tableErrors);
        }
        
        // Validate charts
        if (reportData.getCharts() != null) {
            for (int i = 0; i < reportData.getCharts().size(); i++) {
                ChartData chart = reportData.getCharts().get(i);
                List<String> chartErrors = validateChartData(chart, i);
                errors.addAll(chartErrors);
            }
        }
        
        // Validate sections (new structure)
        if (reportData.getSections() != null) {
            for (int i = 0; i < reportData.getSections().size(); i++) {
                Section section = reportData.getSections().get(i);
                List<String> sectionErrors = validateSection(section, i);
                errors.addAll(sectionErrors);
            }
        }
        
        return errors;
    }
    
    /**
     * Validates a TableBlock and returns validation errors
     */
    private static List<String> validateTableBlock(TableBlock block, int index) {
        List<String> errors = new ArrayList<>();
        
        if (block == null) {
            errors.add("TableBlock at index " + index + " is null");
            return errors;
        }
        
        if (block.getBlockId() == null || block.getBlockId().trim().isEmpty()) {
            errors.add("TableBlock at index " + index + " has empty blockId");
        }
        
        if (block.getTableData() == null) {
            errors.add("TableBlock at index " + index + " has null tableData");
        } else {
            List<String> tableErrors = validateTableData(block.getTableData(), 
                "TableBlock[" + index + "]");
            errors.addAll(tableErrors);
        }
        
        return errors;
    }
    
    /**
     * Validates TableData and returns validation errors
     */
    private static List<String> validateTableData(TableData table, String context) {
        List<String> errors = new ArrayList<>();
        
        if (table.getHeaders() == null || table.getHeaders().isEmpty()) {
            errors.add(context + ": Table headers cannot be null or empty");
        }
        
        if (table.getRows() == null) {
            errors.add(context + ": Table rows cannot be null");
        } else {
            int headerCount = table.getHeaders() != null ? table.getHeaders().size() : 0;
            for (int i = 0; i < table.getRows().size(); i++) {
                List<String> row = table.getRows().get(i);
                if (row == null) {
                    errors.add(context + ": Row " + i + " is null");
                } else if (row.size() != headerCount) {
                    errors.add(context + ": Row " + i + " has " + row.size() + 
                        " columns but headers have " + headerCount + " columns");
                }
            }
        }
        
        return errors;
    }
    
    /**
     * Validates ChartData and returns validation errors
     */
    private static List<String> validateChartData(ChartData chart, int index) {
        List<String> errors = new ArrayList<>();
        
        if (chart == null) {
            errors.add("Chart at index " + index + " is null");
            return errors;
        }
        
        if (chart.getTitle() == null || chart.getTitle().trim().isEmpty()) {
            errors.add("Chart at index " + index + " has empty title");
        }
        
        if (chart.getData() == null || chart.getData().isEmpty()) {
            errors.add("Chart at index " + index + " has no data");
        }
        
        String type = chart.getChartType();
        if (type == null || (!type.equals("bar") && !type.equals("pie"))) {
            errors.add("Chart at index " + index + " has invalid type: " + type + 
                " (must be 'bar' or 'pie')");
        }
        
        return errors;
    }
    
    /**
     * Validates a Section and returns validation errors
     */
    private static List<String> validateSection(Section section, int index) {
        List<String> errors = new ArrayList<>();
        
        if (section == null) {
            errors.add("Section at index " + index + " is null");
            return errors;
        }
        
        if (!section.hasContent()) {
            errors.add("Section at index " + index + " has no content");
        }
        
        // Validate tables in section
        if (section.getTables() != null) {
            for (int i = 0; i < section.getTables().size(); i++) {
                TableData table = section.getTables().get(i);
                List<String> tableErrors = validateTableData(table, 
                    "Section[" + index + "].Table[" + i + "]");
                errors.addAll(tableErrors);
            }
        }
        
        // Validate charts in section
        if (section.getCharts() != null) {
            for (int i = 0; i < section.getCharts().size(); i++) {
                ChartData chart = section.getCharts().get(i);
                List<String> chartErrors = validateChartData(chart, i);
                errors.addAll(chartErrors);
            }
        }
        
        return errors;
    }
    
    /**
     * Validates and throws IllegalArgumentException if invalid
     */
    public static void validateAndThrow(ReportData reportData) {
        List<String> errors = validate(reportData);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Report data validation failed:\n- " + 
                String.join("\n- ", errors));
        }
    }
}
