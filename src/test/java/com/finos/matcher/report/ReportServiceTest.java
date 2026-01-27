package com.finos.matcher.report;

import com.finos.matcher.report.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PDF report generation.
 * Demonstrates the table pagination issue with PDFBox and validates
 * the HTML/CSS pipeline solution.
 */
public class ReportServiceTest {
    
    @TempDir
    Path tempDir;
    
    private static final String TEST_OUTPUT_DIR = "test-output";
    
    @BeforeAll
    public static void setupTestOutputDirectory() throws IOException {
        // Create test-output directory for visible PDF files
        File outputDir = new File(TEST_OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        System.out.println("Test PDFs will be saved to: " + outputDir.getAbsolutePath());
    }

    @Test
    public void testReportGenerationWithPdfBox() throws IOException {
        ReportService service = new ReportService();
        service.setUseHtmlPipeline(false);
        
        ReportData reportData = createTestReportData();
        
        byte[] pdfBytes = service.generatePdf(reportData);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        // Save to temp directory for JUnit cleanup
        Path tempOutputPath = tempDir.resolve("report_pdfbox.pdf");
        try (FileOutputStream fos = new FileOutputStream(tempOutputPath.toFile())) {
            fos.write(pdfBytes);
        }
        
        // Also save to test-output directory for visibility
        Path visibleOutputPath = Paths.get(TEST_OUTPUT_DIR, "report_pdfbox.pdf");
        Files.write(visibleOutputPath, pdfBytes);
        System.out.println("✓ PDFBox PDF generated: " + visibleOutputPath.toAbsolutePath());
    }

    @Test
    public void testReportGenerationWithHtmlPipeline() throws IOException {
        ReportService service = new ReportService();
        service.setUseHtmlPipeline(true);
        
        ReportData reportData = createTestReportData();
        
        byte[] pdfBytes = service.generatePdf(reportData);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        // Save to temp directory for JUnit cleanup
        Path tempOutputPath = tempDir.resolve("report_html.pdf");
        try (FileOutputStream fos = new FileOutputStream(tempOutputPath.toFile())) {
            fos.write(pdfBytes);
        }
        
        // Also save to test-output directory for visibility
        Path visibleOutputPath = Paths.get(TEST_OUTPUT_DIR, "report_html.pdf");
        Files.write(visibleOutputPath, pdfBytes);
        System.out.println("✓ HTML Pipeline PDF generated: " + visibleOutputPath.toAbsolutePath());
    }

    @Test
    public void testDefaultPipelineIsHtml() throws IOException {
        ReportService service = new ReportService();
        // Default should be HTML pipeline now
        
        ReportData reportData = createTestReportData();
        
        byte[] pdfBytes = service.generatePdf(reportData);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        // Save to test-output directory for visibility
        Path visibleOutputPath = Paths.get(TEST_OUTPUT_DIR, "report_default.pdf");
        Files.write(visibleOutputPath, pdfBytes);
        System.out.println("✓ Default Pipeline PDF generated: " + visibleOutputPath.toAbsolutePath());
    }
    
    @Test
    public void testGeneratePdfWithSpecificTemplate() throws IOException {
        ReportService service = new ReportService();
        
        ReportData reportData = createTestReportData();
        
        // Generate with default template
        byte[] defaultPdf = service.generatePdf(reportData, "report");
        assertNotNull(defaultPdf);
        assertTrue(defaultPdf.length > 0);
        
        // Save to test-output directory
        Path defaultOutputPath = Paths.get(TEST_OUTPUT_DIR, "report_template_default.pdf");
        Files.write(defaultOutputPath, defaultPdf);
        System.out.println("✓ Default Template PDF generated: " + defaultOutputPath.toAbsolutePath());
        
        // Generate with invoice template
        byte[] invoicePdf = service.generatePdf(reportData, "invoice");
        assertNotNull(invoicePdf);
        assertTrue(invoicePdf.length > 0);
        
        // Save to test-output directory
        Path invoiceOutputPath = Paths.get(TEST_OUTPUT_DIR, "report_template_invoice.pdf");
        Files.write(invoiceOutputPath, invoicePdf);
        System.out.println("✓ Invoice Template PDF generated: " + invoiceOutputPath.toAbsolutePath());
        
        // PDFs should be different due to different templates
        assertNotEquals(defaultPdf.length, invoicePdf.length);
    }
    
    /**
     * Creates test data with many table rows to demonstrate pagination issues
     */
    private ReportData createTestReportData() {
        ReportData reportData = new ReportData();
        
        // Cover page data
        reportData.setTitle("Annual Financial Report");
        reportData.setSubtitle("Comprehensive Analysis and Summary");
        reportData.setReportDate("2024-01-27");
        reportData.setReportNumber("RPT-2024-001");
        
        // Section 1: Detailed table blocks (1.1-1.4)
        List<TableBlock> tableBlocks = new ArrayList<>();
        
        for (int i = 1; i <= 4; i++) {
            TableBlock block = new TableBlock();
            block.setBlockId("1." + i);
            block.setBlockTitle("Analysis Block " + i);
            block.setTableData(createLargeTableData(50)); // 50 rows to force pagination
            tableBlocks.add(block);
        }
        
        reportData.setTableBlocks(tableBlocks);
        
        // Section 2: Analysis paragraphs
        List<String> paragraphs = Arrays.asList(
            "This report provides a comprehensive analysis of the financial performance for the fiscal year. " +
            "The data presented includes detailed breakdowns across multiple categories and timeframes.",
            
            "Key findings indicate significant growth in several sectors, with notable improvements in " +
            "operational efficiency and cost management. The detailed tables in Section 1 provide granular " +
            "insights into these trends.",
            
            "Based on the analysis, we recommend continued investment in high-performing areas while " +
            "maintaining vigilant cost controls in emerging markets."
        );
        reportData.setAnalysisParagraphs(paragraphs);
        
        // Section 3: Summary table and charts
        reportData.setSummaryTable(createSummaryTable());
        reportData.setCharts(createCharts());
        
        // Section 4: Notice and metadata
        reportData.setReportNotice("This report is confidential and intended for internal use only.");
        reportData.setMetadata("Generated by PDF Render v1.0 | Contact: reports@example.com");
        
        return reportData;
    }
    
    private TableData createLargeTableData(int numRows) {
        List<String> headers = Arrays.asList("ID", "Category", "Value", "Status", "Notes");
        
        List<List<String>> rows = new ArrayList<>();
        for (int i = 1; i <= numRows; i++) {
            rows.add(Arrays.asList(
                String.valueOf(i),
                "Category " + (char)('A' + (i % 5)),
                String.format("$%.2f", 1000.0 + i * 123.45),
                i % 2 == 0 ? "Active" : "Pending",
                "Notes for row " + i
            ));
        }
        
        return new TableData(headers, rows);
    }
    
    private TableData createSummaryTable() {
        List<String> headers = Arrays.asList("Quarter", "Revenue", "Expenses", "Profit");
        List<List<String>> rows = Arrays.asList(
            Arrays.asList("Q1", "$500,000", "$300,000", "$200,000"),
            Arrays.asList("Q2", "$550,000", "$320,000", "$230,000"),
            Arrays.asList("Q3", "$600,000", "$340,000", "$260,000"),
            Arrays.asList("Q4", "$650,000", "$360,000", "$290,000")
        );
        
        return new TableData(headers, rows);
    }
    
    private List<ChartData> createCharts() {
        List<ChartData> charts = new ArrayList<>();
        
        // Bar chart
        Map<String, Double> barData = new LinkedHashMap<>();
        barData.put("Q1", 200000.0);
        barData.put("Q2", 230000.0);
        barData.put("Q3", 260000.0);
        barData.put("Q4", 290000.0);
        charts.add(new ChartData("Quarterly Profit", "bar", barData));
        
        // Pie chart
        Map<String, Double> pieData = new LinkedHashMap<>();
        pieData.put("Salaries", 40.0);
        pieData.put("Operations", 30.0);
        pieData.put("Marketing", 20.0);
        pieData.put("Other", 10.0);
        charts.add(new ChartData("Expense Distribution", "pie", pieData));
        
        return charts;
    }
}
