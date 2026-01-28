package com.finos.matcher.report;

import com.finos.matcher.report.config.FontConfig;
import com.finos.matcher.report.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify whether baseUrl parameter actually affects PDF rendering.
 * Tests with identical data but different baseUrl configurations.
 */
public class BaseUrlVerificationTest {
    
    private static final String TEST_OUTPUT_DIR = "test-output";
    
    @BeforeAll
    public static void setupTestOutputDirectory() throws IOException {
        Path outputDir = Paths.get(TEST_OUTPUT_DIR);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
    }
    
    @Test
    public void testWithCurrentBaseUrl() throws IOException {
        System.out.println("\n=== Test 1: WITH baseUrl (current implementation) ===");
        ReportService service = new ReportService();
        service.setUseHtmlPipeline(true);
        
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);
        
        ReportData reportData = createTestData();
        byte[] pdfBytes = service.generatePdf(reportData, "matcher-report-1.0");
        
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "baseurl_test_WITH_baseurl.pdf");
        Files.write(outputPath, pdfBytes);
        
        System.out.println("✓ PDF WITH baseUrl: " + outputPath.toAbsolutePath());
        System.out.println("  Size: " + pdfBytes.length + " bytes");
        System.out.println("  MD5: " + getMD5(pdfBytes));
    }
    
    private ReportData createTestData() {
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("BaseUrl Verification Test")
            .reportDate("2024-12-31")
            .reportNumber("VERIFY-001");
        
        Section section1 = new Section("1.1 Test Section with Border");
        section1.addParagraph("This is a test paragraph to check if borders appear.");
        
        TableData table = new TableData(
            Arrays.asList("Column 1", "Column 2", "Column 3"),
            Arrays.asList(
                Arrays.asList("Data 1", "Data 2", "Data 3"),
                Arrays.asList("Data 4", "Data 5", "Data 6")
            )
        );
        section1.addTable(table);
        builder.addSection(section1);
        
        Section section2 = new Section("1.2 Another Section");
        section2.addParagraph("Another paragraph with Chinese characters: 中文测试");
        builder.addSection(section2);
        
        return builder.build();
    }
    
    private String getMD5(byte[] data) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "error";
        }
    }
}
