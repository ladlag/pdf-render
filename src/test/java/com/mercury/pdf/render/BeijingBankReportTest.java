package com.mercury.pdf.render;

import com.mercury.pdf.render.config.PdfRenderProperties;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test case for generating a PDF from the Beijing Bank 2025 H1 financial analysis report.
 * <p>
 * The report content is converted from the original Word document to markdown format,
 * covering profitability, asset scale, business structure, asset quality, and
 * development recommendations — with extensive comparison tables across listed banks.
 * </p>
 */
public class BeijingBankReportTest {

    private static final String TEST_OUTPUT_DIR = System.getProperty("test.output.dir", "test-output");

    @Test
    public void testBeijingBankFinancialReportPdf() throws IOException {
        // Load the full markdown content from test resources
        String markdown = loadMarkdownResource("beijing_bank_report.md");
        assertNotNull(markdown, "Markdown resource should be loadable");
        assertTrue(markdown.length() > 10000, "Full report markdown should be substantial");

        // Build the report using markdown content
        ReportData report = ReportDataBuilder.create()
            .title("北京银行财报分析报告")
            .subtitle("2025年上半年财务分析")
            .reportDate("2025-06-30")
            .addSection(new Section("报告正文")
                .withMarkdownContent(markdown))
            .build();

        ReportService service = new ReportService();
        service.getHtmlRenderer().setDefaultTemplateName("financial-report");

        // Configure Chinese font
        configureChineseFont(service);

        // Enable debug HTML output
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory("test-output/debug-html");
        service.getHtmlRenderer().setDebugHtmlIncludeTimestamp(true);

        byte[] pdfBytes = service.generatePdf(report);

        assertNotNull(pdfBytes, "PDF bytes should not be null");
        assertTrue(pdfBytes.length > 0, "PDF should not be empty");

        // Verify that the CJK font is embedded with Identity-H encoding
        String pdfContent = new String(pdfBytes, StandardCharsets.ISO_8859_1);
        assertTrue(pdfContent.contains("Identity-H"),
            "PDF must contain Identity-H encoding for CJK font support");

        // Save the output PDF
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "beijing_bank_financial_report.pdf");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Beijing Bank financial report PDF generated: " + outputPath.toAbsolutePath());
        System.out.println("  PDF size: " + pdfBytes.length + " bytes (" + (pdfBytes.length / 1024) + " KB)");

        // Verify the PDF is substantial (should be large with full font + many pages)
        assertTrue(pdfBytes.length > 50000,
            "Financial report PDF should be substantial (>50KB). Actual: " + pdfBytes.length);
    }

    /**
     * Loads a markdown file from the test classpath resources.
     */
    private String loadMarkdownResource(String resourceName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourceName);
            }
            byte[] bytes = new byte[is.available()];
            int totalRead = 0;
            while (totalRead < bytes.length) {
                int read = is.read(bytes, totalRead, bytes.length - totalRead);
                if (read < 0) break;
                totalRead += read;
            }
            return new String(bytes, 0, totalRead, StandardCharsets.UTF_8);
        }
    }

    /**
     * Configures Chinese font using PdfRenderProperties.FontProperties directly.
     */
    private void configureChineseFont(ReportService service) {
        PdfRenderProperties.FontProperties fontProps = new PdfRenderProperties.FontProperties();
        fontProps.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontProps.setCjkPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        service.getHtmlRenderer().setFontProperties(fontProps);
    }
}
