package com.mercury.pdf.render;

import com.mercury.pdf.render.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to demonstrate and verify that the library supports unlimited sections,
 * addressing the issue about addSection1-4 naming suggesting a 4-section limit.
 */
public class UnlimitedSectionsTest {
    
    private static final String TEST_OUTPUT_DIR = "test-output";
    
    @BeforeAll
    public static void setupTestOutputDirectory() throws IOException {
        Path outputDir = Paths.get(TEST_OUTPUT_DIR);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
    }
    
    @Test
    public void testUnlimitedSections() throws IOException {
        System.out.println("Testing unlimited sections support...");
        
        // Create a report with MORE THAN 4 sections to prove there's no limit
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("Report with Unlimited Sections")
            .subtitle("Demonstrating No 4-Section Limit")
            .reportDate("2024-12-31")
            .reportNumber("UNLIMITED-001");
        
        // Add 10 sections to prove the point
        for (int i = 1; i <= 10; i++) {
            builder.addSection(new Section("Section " + i)
                .withSubtitle("Subtitle for section " + i)
                .addParagraph("This is the content for section " + i + ". " +
                    "The library supports unlimited sections, not just 4.")
                .addTable(createTestTable(i))
                .addChart(createTestChart(i)));
        }
        
        ReportData report = builder.build();
        
        // Verify we have 10 sections
        assertEquals(10, report.getSections().size(), 
            "Report should have 10 sections, not limited to 4");
        
        // Generate PDF
        PdfRenderService service = new PdfRenderService();
        service.getHtmlRenderer().setDefaultTemplateName("flexible");
        byte[] pdfBytes = service.generatePdf(report);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        // Save for inspection
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "unlimited_sections_report.pdf");
        Files.write(outputPath, pdfBytes);
        
        System.out.println("✓ Successfully generated PDF with 10 sections");
        System.out.println("  Output: " + outputPath.toAbsolutePath());
        System.out.println("  Size: " + pdfBytes.length + " bytes");
        System.out.println("  This proves there is NO 4-section limit!");
    }
    
    @Test
    public void testVeryLargeNumberOfSections() throws IOException {
        System.out.println("Testing with a very large number of sections (20)...");
        
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("Report with 20 Sections")
            .subtitle("Stress Test")
            .reportDate("2024-12-31");
        
        // Add 20 sections
        for (int i = 1; i <= 20; i++) {
            builder.addSection(new Section("Section " + i)
                .addParagraph("Content for section " + i));
        }
        
        ReportData report = builder.build();
        
        assertEquals(20, report.getSections().size());
        
        // Generate PDF
        PdfRenderService service = new PdfRenderService();
        service.getHtmlRenderer().setDefaultTemplateName("flexible");
        byte[] pdfBytes = service.generatePdf(report);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        // Save for inspection
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "twenty_sections_report.pdf");
        Files.write(outputPath, pdfBytes);
        
        System.out.println("✓ Successfully generated PDF with 20 sections");
        System.out.println("  Output: " + outputPath.toAbsolutePath());
    }
    
    @Test
    public void testSectionsWithVariedContent() throws IOException {
        System.out.println("Testing sections with varied content types...");
        
        // Create a report with more than 4 sections, each with different content types
        ReportData report = ReportDataBuilder.create()
            .title("Varied Content Report")
            .subtitle("More Than 4 Sections with Different Content")
            .reportDate("2024-12-31")
            
            // Section 1: Text only
            .addSection(new Section("Introduction")
                .addParagraph("Introduction text"))
            
            // Section 2: Table only
            .addSection(new Section("Data Table")
                .addTable(createTestTable(1)))
            
            // Section 3: Chart only
            .addSection(new Section("Chart")
                .addChart(createTestChart(1)))
            
            // Section 4: Mixed - Text + Table
            .addSection(new Section("Analysis")
                .addParagraph("Analysis text")
                .addTable(createTestTable(2)))
            
            // Section 5: Mixed - Text + Chart
            .addSection(new Section("Visualization")
                .addParagraph("Visualization text")
                .addChart(createTestChart(2)))
            
            // Section 6: Mixed - Table + Chart
            .addSection(new Section("Metrics")
                .addTable(createTestTable(3))
                .addChart(createTestChart(3)))
            
            // Section 7: Everything
            .addSection(new Section("Summary")
                .addParagraph("Summary text")
                .addTable(createTestTable(4))
                .addChart(createTestChart(4)))
            
            // Section 8: Custom HTML
            .addSection(new Section("Recommendations")
                .withCustomContent("<ul><li>Point 1</li><li>Point 2</li></ul>"))
            
            .build();
        
        // Verify we have 8 sections (more than 4!)
        assertEquals(8, report.getSections().size(), 
            "Report should have 8 sections");
        
        // Generate PDF
        PdfRenderService service = new PdfRenderService();
        service.getHtmlRenderer().setDefaultTemplateName("flexible");
        byte[] pdfBytes = service.generatePdf(report);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        // Save for inspection
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "varied_content_report.pdf");
        Files.write(outputPath, pdfBytes);
        
        System.out.println("✓ Successfully generated PDF with 8 varied sections");
        System.out.println("  Output: " + outputPath.toAbsolutePath());
    }
    
    @Test
    public void testSectionStructureWorks() throws IOException {
        System.out.println("Testing clean section-based structure...");
        
        // Use the Section structure
        ReportData report = ReportDataBuilder.create()
            .title("Section-Based Report")
            .subtitle("Clean Architecture Test")
            .reportDate("2024-12-31")
            
            // Section 1: Table sections
            .addSection(new Section("1.1 Block 1")
                .addTable(createTestTable(1)))
            
            // Section 2: Analysis paragraphs
            .addSection(new Section("Analysis")
                .addParagraph("Analysis paragraph"))
            
            // Section 3: Summary table and charts
            .addSection(new Section("Summary")
                .addTable(createTestTable(2))
                .addChart(createTestChart(1)))
            
            // Section 4: Notice and metadata
            .reportNotice("Notice text")
            .metadata("Metadata text")
            .build();
        
        assertEquals(3, report.getSections().size(), 
            "Report should have 3 sections");
        
        // Generate PDF
        PdfRenderService service = new PdfRenderService();
        byte[] pdfBytes = service.generatePdf(report);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        // Save for inspection
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "section_structure_report.pdf");
        Files.write(outputPath, pdfBytes);
        
        System.out.println("✓ Section-based structure works correctly");
        System.out.println("  Output: " + outputPath.toAbsolutePath());
        System.out.println("  Use unlimited sections for maximum flexibility!");
    }
    
    // Helper methods
    
    private TableData createTestTable(int id) {
        return new TableData(
            Arrays.asList("Column 1", "Column 2", "Column 3"),
            Arrays.asList(
                Arrays.asList("Row " + id + ".1", "Data 1", "Value 1"),
                Arrays.asList("Row " + id + ".2", "Data 2", "Value 2"),
                Arrays.asList("Row " + id + ".3", "Data 3", "Value 3")
            )
        );
    }
    
    private ChartData createTestChart(int id) {
        Map<String, Double> data = new LinkedHashMap<>();
        data.put("Category A", 20.0 + id * 5);
        data.put("Category B", 30.0 + id * 5);
        data.put("Category C", 50.0 - id * 5);
        return new ChartData("Chart " + id, "bar", data);
    }
}
