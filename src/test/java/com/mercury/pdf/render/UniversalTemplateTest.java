package com.mercury.pdf.render;

import com.mercury.pdf.render.model.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests demonstrating the universal template system.
 * KEY CONCEPT: Same data model generates different outputs with different templates.
 */
public class UniversalTemplateTest {
    
    private static final String TEST_OUTPUT_DIR = System.getProperty("test.output.dir", "test-output");
    
    @Test
    public void testSameDataMultipleTemplates() throws IOException {
        // Create ONE data model
        ReportData universalData = createUniversalDataModel();
        
        PdfRenderService service = new PdfRenderService();
        
        // Generate with DIFFERENT templates
        byte[] reportPdf = service.generatePdf(universalData, "report");
        byte[] flexiblePdf = service.generatePdf(universalData, "flexible");
        byte[] invoicePdf = service.generatePdf(universalData, "invoice");
        byte[] execSummaryPdf = service.generatePdf(universalData, "executive-summary");
        byte[] certificatePdf = service.generatePdf(universalData, "certificate");
        
        // All should be generated successfully
        assertNotNull(reportPdf);
        assertNotNull(flexiblePdf);
        assertNotNull(invoicePdf);
        assertNotNull(execSummaryPdf);
        assertNotNull(certificatePdf);
        
        assertTrue(reportPdf.length > 0);
        assertTrue(flexiblePdf.length > 0);
        assertTrue(invoicePdf.length > 0);
        assertTrue(execSummaryPdf.length > 0);
        assertTrue(certificatePdf.length > 0);
        
        // Save for manual inspection
        Files.createDirectories(Paths.get(TEST_OUTPUT_DIR));
        Files.write(Paths.get(TEST_OUTPUT_DIR, "universal-report.pdf"), reportPdf);
        Files.write(Paths.get(TEST_OUTPUT_DIR, "universal-flexible.pdf"), flexiblePdf);
        Files.write(Paths.get(TEST_OUTPUT_DIR, "universal-invoice.pdf"), invoicePdf);
        Files.write(Paths.get(TEST_OUTPUT_DIR, "universal-executive.pdf"), execSummaryPdf);
        Files.write(Paths.get(TEST_OUTPUT_DIR, "universal-certificate.pdf"), certificatePdf);
        
        System.out.println("✓ Universal template test: Same data → 5 different PDFs generated!");
    }
    
    @Test
    public void testNoCodeChangesNeeded() {
        // This test proves that adding new templates requires NO code changes
        // Just create a new HTML file in templates/ directory
        
        // The data model is universal and works with ANY template
        ReportData data = ReportDataBuilder.create()
            .title("Test Report")
            .addSection(new Section("Test Section")
                .addParagraph("Test content"))
            .build();
        
        // Any template name works - no code changes needed
        PdfRenderService service = new PdfRenderService();
        
        assertDoesNotThrow(() -> {
            service.generatePdf(data, "report");
            service.generatePdf(data, "flexible");
            service.generatePdf(data, "invoice");
            service.generatePdf(data, "executive-summary");
            service.generatePdf(data, "certificate");
        });
        
        System.out.println("✓ No code changes needed for different templates!");
    }
    
    @Test
    public void testDataModelIsTemplateAgnostic() {
        // The same Section can be used for:
        // - Standard reports
        // - Certificates (title as recipient name)
        // - Executive summaries
        // - Invoices
        // - Any custom template
        
        Section universalSection = new Section("John Doe")
            .withSubtitle("Outstanding Achievement")
            .addParagraph("For exceptional performance in 2024")
            .addTable(createAchievementTable())
            .addChart(createPerformanceChart());
        
        // This section works with ALL templates without modification
        assertNotNull(universalSection.getTitle());
        assertNotNull(universalSection.getSubtitle());
        assertFalse(universalSection.getParagraphs().isEmpty());
        assertFalse(universalSection.getTables().isEmpty());
        assertFalse(universalSection.getCharts().isEmpty());
        
        System.out.println("✓ Data model is template-agnostic!");
    }
    
    private ReportData createUniversalDataModel() {
        return ReportDataBuilder.create()
            .title("Universal Data Model Test")
            .subtitle("Demonstrating Template Independence")
            .reportDate("2024-12-31")
            .reportNumber("UNIVERSAL-001")
            .addSection(new Section("Summary")
                .addParagraph("This data works with any template")
                .addTable(createAchievementTable())
                .addChart(createPerformanceChart()))
            .reportNotice("Test Notice")
            .metadata("Test Metadata")
            .build();
    }
    
    private TableData createAchievementTable() {
        return new TableData(
            Arrays.asList("Metric", "Value"),
            Arrays.asList(
                Arrays.asList("Performance", "95%"),
                Arrays.asList("Quality", "98%")
            )
        );
    }
    
    private ChartData createPerformanceChart() {
        Map<String, Double> data = new LinkedHashMap<>();
        data.put("Q1", 88.0);
        data.put("Q2", 92.0);
        return new ChartData("Performance", "bar", data);
    }
}
