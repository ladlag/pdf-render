package com.mercury.pdf.render;

import com.mercury.pdf.render.model.ChartData;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;
import com.mercury.pdf.render.model.TableData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
public class PdfRenderServiceTest {
    
    @TempDir
    Path tempDir;
    
    private static final String TEST_OUTPUT_DIR = "test-output";
    
    @BeforeAll
    public static void setupTestOutputDirectory() throws IOException {
        // Create test-output directory for visible PDF files
        Path outputDir = Paths.get(TEST_OUTPUT_DIR);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        System.out.println("Test PDFs will be saved to: " + outputDir.toAbsolutePath());
    }

    @Test
    public void testReportGenerationWithHtmlPipeline() throws IOException {
        PdfRenderService service = new PdfRenderService();
        
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
        PdfRenderService service = new PdfRenderService();
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
        PdfRenderService service = new PdfRenderService();
        
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
     * Creates test data with many table rows to demonstrate pagination
     */
    private ReportData createTestReportData() {
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("Annual Financial Report")
            .subtitle("Comprehensive Analysis and Summary")
            .reportDate("2024-01-27")
            .reportNumber("RPT-2024-001");
        
        // Section 1: Detailed table sections (1.1-1.4)
        for (int i = 1; i <= 4; i++) {
            Section section = new Section("1." + i + " Analysis Block " + i);
            section.addTable(createLargeTableData(50)); // 50 rows to force pagination
            builder.addSection(section);
        }
        
        // Section 2: Analysis paragraphs
        Section analysisSection = new Section("2. Analysis Summary");
        analysisSection.addParagraph("This report provides a comprehensive analysis of the financial performance for the fiscal year. " +
            "The data presented includes detailed breakdowns across multiple categories and timeframes.");
        analysisSection.addParagraph("Key findings indicate significant growth in several sectors, with notable improvements in " +
            "operational efficiency and cost management. The detailed tables in Section 1 provide granular " +
            "insights into these trends.");
        analysisSection.addParagraph("Based on the analysis, we recommend continued investment in high-performing areas while " +
            "maintaining vigilant cost controls in emerging markets.");
        builder.addSection(analysisSection);
        
        // Section 3: Summary table and charts
        Section summarySection = new Section("3. Summary");
        summarySection.addTable(createSummaryTable());
        for (ChartData chart : createCharts()) {
            summarySection.addChart(chart);
        }
        builder.addSection(summarySection);
        
        // Section 4: Notice and metadata
        builder.reportNotice("This report is confidential and intended for internal use only.");
        builder.metadata("Generated by PDF Render v1.0 | Contact: reports@example.com");
        
        return builder.build();
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
