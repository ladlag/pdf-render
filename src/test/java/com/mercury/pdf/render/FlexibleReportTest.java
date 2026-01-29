package com.mercury.pdf.render;

import com.mercury.pdf.render.model.*;
import com.mercury.pdf.render.util.ReportDataValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for enhanced PDF report generation with flexible sections.
 */
public class FlexibleReportTest {
    
    @TempDir
    Path tempDir;
    
    private static final String TEST_OUTPUT_DIR = System.getProperty("test.output.dir", "test-output");
    
    @Test
    public void testReportDataBuilder() {
        ReportData report = ReportDataBuilder.create()
            .title("Test Report")
            .subtitle("Test Subtitle")
            .reportDate("2024-12-31")
            .reportNumber("TEST-001")
            .addSection(new Section("Analysis").addParagraph("Test paragraph"))
            .build();
        
        assertNotNull(report);
        assertEquals("Test Report", report.getTitle());
        assertEquals("Test Subtitle", report.getSubtitle());
        assertEquals("2024-12-31", report.getReportDate());
        assertEquals(1, report.getSections().size());
        assertEquals(1, report.getSections().get(0).getParagraphs().size());
    }
    
    @Test
    public void testBuilderValidation() {
        // Should throw exception when title is missing
        assertThrows(IllegalStateException.class, () -> {
            ReportDataBuilder.create()
                .subtitle("No title")
                .build();
        });
        
        // Should throw exception when title is empty
        assertThrows(IllegalArgumentException.class, () -> {
            ReportDataBuilder.create()
                .title("")
                .build();
        });
    }
    
    @Test
    public void testSectionBuilder() {
        Section section = new Section("Test Section")
            .withSubtitle("Test Subtitle")
            .addParagraph("Paragraph 1")
            .addParagraph("Paragraph 2")
            .addTable(createTestTable())
            .withCssClass("custom-class");
        
        assertNotNull(section);
        assertEquals("Test Section", section.getTitle());
        assertEquals("Test Subtitle", section.getSubtitle());
        assertEquals(2, section.getParagraphs().size());
        assertEquals(1, section.getTables().size());
        assertEquals("custom-class", section.getCssClass());
        assertTrue(section.hasContent());
    }
    
    @Test
    public void testFlexibleSectionReport() throws IOException {
        ReportData report = ReportDataBuilder.create()
            .title("Flexible Section Report")
            .subtitle("Testing Dynamic Sections")
            .reportDate("2024-12-31")
            .addSection(new Section("Section 1")
                .addParagraph("This is section 1")
                .addTable(createTestTable()))
            .addSection(new Section("Section 2")
                .addParagraph("This is section 2")
                .addChart(createTestChart()))
            .build();
        
        assertNotNull(report);
        assertEquals(2, report.getSections().size());
        
        // Generate PDF
        ReportService service = new ReportService();
        service.getHtmlRenderer().setDefaultTemplateName("flexible");
        byte[] pdfBytes = service.generatePdf(report);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        // Save for inspection
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "flexible_section_report.pdf");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Flexible section report generated: " + outputPath.toAbsolutePath());
    }
    
    @Test
    public void testMixedContentSection() throws IOException {
        Section section = new Section("Mixed Content Section")
            .withSubtitle("Contains paragraphs, tables, and charts")
            .addParagraph("Introduction paragraph")
            .addTable(createTestTable())
            .addChart(createTestChart())
            .addParagraph("Conclusion paragraph")
            .withCustomContent("<p style='color: blue;'>Custom HTML content</p>");
        
        ReportData report = ReportDataBuilder.create()
            .title("Mixed Content Test")
            .addSection(section)
            .build();
        
        ReportService service = new ReportService();
        service.getHtmlRenderer().setDefaultTemplateName("flexible");
        byte[] pdfBytes = service.generatePdf(report);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "mixed_content_report.pdf");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Mixed content report generated: " + outputPath.toAbsolutePath());
    }
    
    @Test
    public void testReportDataValidator() {
        // Valid report
        ReportData validReport = ReportDataBuilder.create()
            .title("Valid Report")
            .addSection(new Section("Section 1").addParagraph("Content"))
            .build();
        
        List<String> errors = ReportDataValidator.validate(validReport);
        assertTrue(errors.isEmpty(), "Valid report should have no errors");
        
        // Invalid report - no title
        ReportData invalidReport1 = new ReportData();
        Section testSection = new Section("Test");
        testSection.addParagraph("Paragraph");
        invalidReport1.setSections(Arrays.asList(testSection));
        
        errors = ReportDataValidator.validate(invalidReport1);
        assertFalse(errors.isEmpty(), "Report without title should have errors");
        assertTrue(errors.stream().anyMatch(e -> e.contains("title")));
        
        // Invalid report - no content
        ReportData invalidReport2 = new ReportData();
        invalidReport2.setTitle("Title Only");
        
        errors = ReportDataValidator.validate(invalidReport2);
        assertFalse(errors.isEmpty(), "Report without content should have errors");
        // The error might say "sections" instead of "content" in the new structure
        assertTrue(errors.stream().anyMatch(e -> e.contains("content") || e.contains("section")), 
            "Error should mention missing content or sections");
        
        System.out.println("✓ Validator tests passed");
    }
    
    @Test
    public void testValidatorThrow() {
        ReportData invalidReport = new ReportData();
        invalidReport.setTitle("Title");
        // No content
        
        assertThrows(IllegalArgumentException.class, () -> {
            ReportDataValidator.validateAndThrow(invalidReport);
        });
    }
    
    @Test
    public void testTableValidation() {
        // Create table with mismatched columns
        TableData invalidTable = new TableData(
            Arrays.asList("Col1", "Col2", "Col3"),
            Arrays.asList(
                Arrays.asList("A", "B", "C"),  // 3 columns - OK
                Arrays.asList("D", "E")         // 2 columns - ERROR
            )
        );
        
        ReportData report = new ReportData();
        report.setTitle("Test");
        Section section = new Section("Test Section");
        section.addTable(invalidTable);
        report.setSections(Arrays.asList(section));
        
        List<String> errors = ReportDataValidator.validate(report);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("columns")));
    }
    
    @Test
    public void testChartValidation() {
        // Invalid chart - no data
        ChartData invalidChart = new ChartData("Empty Chart", "bar", new LinkedHashMap<>());
        
        ReportData report = new ReportData();
        report.setTitle("Test");
        Section section = new Section("Test Section");
        section.addChart(invalidChart);
        report.setSections(Arrays.asList(section));
        
        List<String> errors = ReportDataValidator.validate(report);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("no data")));
    }
    
    @Test
    public void testMultipleSectionsWithVariousContent() throws IOException {
        ReportData report = ReportDataBuilder.create()
            .title("Comprehensive Test Report")
            .subtitle("Testing All Features")
            .reportDate("2024-12-31")
            .reportNumber("TEST-COMPREHENSIVE-001")
            
            // Section 1: Text only
            .addSection(new Section("Introduction")
                .addParagraph("This is the introduction section.")
                .addParagraph("It contains multiple paragraphs."))
            
            // Section 2: Table only
            .addSection(new Section("Data Tables")
                .addTable(createTestTable()))
            
            // Section 3: Chart only
            .addSection(new Section("Charts")
                .addChart(createTestChart()))
            
            // Section 4: Mixed content
            .addSection(new Section("Summary")
                .addParagraph("Summary paragraph")
                .addTable(createTestTable())
                .addChart(createTestChart()))
            
            .reportNotice("Test notice")
            .metadata("Test metadata")
            .build();
        
        // Validate
        List<String> errors = ReportDataValidator.validate(report);
        assertTrue(errors.isEmpty(), "Report should be valid: " + errors);
        
        // Generate
        ReportService service = new ReportService();
        service.getHtmlRenderer().setDefaultTemplateName("flexible");
        byte[] pdfBytes = service.generatePdf(report);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "comprehensive_test_report.pdf");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Comprehensive test report generated: " + outputPath.toAbsolutePath());
    }
    
    // Helper methods
    
    private TableData createTestTable() {
        return new TableData(
            Arrays.asList("Column 1", "Column 2", "Column 3"),
            Arrays.asList(
                Arrays.asList("A1", "B1", "C1"),
                Arrays.asList("A2", "B2", "C2"),
                Arrays.asList("A3", "B3", "C3")
            )
        );
    }
    
    private ChartData createTestChart() {
        Map<String, Double> data = new LinkedHashMap<>();
        data.put("Category A", 25.0);
        data.put("Category B", 35.0);
        data.put("Category C", 40.0);
        return new ChartData("Test Chart", "bar", data);
    }
}
