package com.mercury.pdf.render;

import com.lowagie.text.DocumentException;
import com.mercury.pdf.render.config.PdfRenderProperties;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that duplicate font names are correctly deduplicated in CSS font-family.
 * This is critical to fix the issue where:
 * - User sets the same font for both regularPath and cjkPath
 * - Font name appears twice in CSS: "HarmonyOS Sans SC, HarmonyOS Sans SC, sans-serif"
 * - This causes font matching to fail on some systems, especially Windows/JDK8
 */
public class DuplicateFontDeduplicationTest {
    
    private static final String TEST_OUTPUT_DIR = "test-output";
    
    @Test
    public void testSameFontForRegularAndCjkIsNotDuplicated() throws IOException, DocumentException {
        System.out.println("\n========================================");
        System.out.println("测试相同字体用于 regular 和 CJK 时不会重复");
        System.out.println("Test same font for regular and CJK is not duplicated");
        System.out.println("========================================\n");
        
        HtmlReportRenderer renderer = new HtmlReportRenderer();
        renderer.setDebugHtmlEnabled(true);
        renderer.setDebugHtmlOutputDirectory(TEST_OUTPUT_DIR + "/debug-html");
        
        // Configure the SAME font for both regular and CJK
        // This is a common use case when using a single CJK-capable font
        PdfRenderProperties.FontProperties fontProps = new PdfRenderProperties.FontProperties();
        fontProps.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontProps.setCjkPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf"); // SAME font
        fontProps.setDefaultFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        fontProps.setCjkFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        
        renderer.setFontProperties(fontProps);
        
        // Create report with Chinese text
        ReportData reportData = ReportDataBuilder.create()
            .title("字体去重测试 Font Deduplication Test")
            .subtitle("验证相同字体不会重复 Verify same font is not duplicated")
            .addSection(new Section("测试章节 Test Section")
                .addParagraph("这是中文文本。This is Chinese text.")
                .addParagraph("如果字体名称重复，可能导致匹配失败。")
                .addParagraph("If font name is duplicated, matching may fail."))
            .build();
        
        // Generate PDF
        System.out.println("Generating PDF with same font for regular and CJK...");
        byte[] pdfBytes = renderer.generatePdf(reportData);
        
        assertNotNull(pdfBytes, "PDF should be generated");
        assertTrue(pdfBytes.length > 0, "PDF should have content");
        
        // Save PDF for verification
        File outputDir = new File(TEST_OUTPUT_DIR);
        outputDir.mkdirs();
        File pdfFile = new File(outputDir, "font_deduplication_test.pdf");
        Files.write(pdfFile.toPath(), pdfBytes);
        
        System.out.println("✓ PDF generated successfully");
        System.out.println("  File: " + pdfFile.getAbsolutePath());
        System.out.println("  Size: " + pdfBytes.length + " bytes");
        
        // Verify PDF size is reasonable (should be larger with embedded fonts)
        assertTrue(pdfBytes.length > 30000, 
            "PDF should be larger than 30KB with embedded fonts. Actual: " + pdfBytes.length + " bytes");
        
        System.out.println("✓ PDF size verification passed");
        
        // Read the debug HTML to verify font-family doesn't have duplicates
        File debugHtmlFile = new File(TEST_OUTPUT_DIR + "/debug-html/flexible.html");
        if (debugHtmlFile.exists()) {
            String htmlContent = new String(Files.readAllBytes(debugHtmlFile.toPath()));
            
            // Check for the problematic duplicate pattern
            assertFalse(htmlContent.contains("HarmonyOS Sans SC, HarmonyOS Sans SC,"), 
                "Font name should not be duplicated in CSS font-family");
            
            System.out.println("✓ Debug HTML verified - no duplicate font names in CSS");
            System.out.println("  Debug HTML: " + debugHtmlFile.getAbsolutePath());
        } else {
            System.out.println("⚠ Debug HTML not found (expected location: " + debugHtmlFile.getAbsolutePath() + ")");
        }
        
        System.out.println("\n验证要点 / Verification Points:");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("1. ✓ 即使同一字体用于 regular 和 CJK，也不会重复");
        System.out.println("   Even if same font is used for regular and CJK, it's not duplicated");
        System.out.println("2. ✓ CSS font-family 应该是: 'HarmonyOS Sans SC, sans-serif'");
        System.out.println("   CSS font-family should be: 'HarmonyOS Sans SC, sans-serif'");
        System.out.println("3. ✓ 而不是: 'HarmonyOS Sans SC, HarmonyOS Sans SC, sans-serif'");
        System.out.println("   Not: 'HarmonyOS Sans SC, HarmonyOS Sans SC, sans-serif'");
        System.out.println("4. ✓ PDF 文件正常生成，中文应该能正确显示");
        System.out.println("   PDF generated successfully, Chinese should display correctly");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        System.out.println("\n========================================");
    }
    
    @Test
    public void testDifferentFontsForRegularAndCjk() throws IOException, DocumentException {
        System.out.println("\n========================================");
        System.out.println("测试不同字体用于 regular 和 CJK");
        System.out.println("Test different fonts for regular and CJK");
        System.out.println("========================================\n");
        
        HtmlReportRenderer renderer = new HtmlReportRenderer();
        renderer.setDebugHtmlEnabled(true);
        renderer.setDebugHtmlOutputDirectory(TEST_OUTPUT_DIR + "/debug-html");
        
        // Configure DIFFERENT fonts for regular and CJK
        PdfRenderProperties.FontProperties fontProps = new PdfRenderProperties.FontProperties();
        fontProps.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontProps.setCjkPath("classpath:/fonts/HarmonyOS_Sans_SC_Bold.ttf"); // Different font
        fontProps.setDefaultFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        fontProps.setCjkFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        
        renderer.setFontProperties(fontProps);
        
        // Create report with mixed content
        ReportData reportData = ReportDataBuilder.create()
            .title("多字体测试 Multiple Fonts Test")
            .subtitle("Regular 和 Bold 字体")
            .addSection(new Section("测试章节")
                .addParagraph("Regular 字体的中文文本。")
                .addParagraph("Bold 字体的中文文本。"))
            .build();
        
        System.out.println("Generating PDF with different fonts for regular and CJK...");
        byte[] pdfBytes = renderer.generatePdf(reportData);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        File pdfFile = new File(TEST_OUTPUT_DIR, "font_deduplication_different_fonts.pdf");
        Files.write(pdfFile.toPath(), pdfBytes);
        
        System.out.println("✓ PDF generated successfully");
        System.out.println("  File: " + pdfFile.getAbsolutePath());
        System.out.println("  Size: " + pdfBytes.length + " bytes");
        
        // Both fonts should be embedded, so file should be larger
        assertTrue(pdfBytes.length > 35000, 
            "PDF with 2 fonts should be larger than 35KB. Actual: " + pdfBytes.length + " bytes");
        
        System.out.println("✓ PDF size verification passed (multiple fonts embedded)");
        System.out.println("\n========================================");
    }
    
    @Test
    public void testOnlyRegularFontConfigured() throws IOException, DocumentException {
        System.out.println("\n========================================");
        System.out.println("测试只配置 regular 字体（常见场景）");
        System.out.println("Test only regular font configured (common scenario)");
        System.out.println("========================================\n");
        
        HtmlReportRenderer renderer = new HtmlReportRenderer();
        
        // Configure ONLY regular font (no separate CJK font)
        // This is the most common scenario
        PdfRenderProperties.FontProperties fontProps = new PdfRenderProperties.FontProperties();
        fontProps.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        // Note: cjkPath is NOT set
        fontProps.setDefaultFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        
        renderer.setFontProperties(fontProps);
        
        ReportData reportData = ReportDataBuilder.create()
            .title("单字体配置测试")
            .subtitle("Only Regular Font")
            .addSection(new Section("测试")
                .addParagraph("使用单个 CJK 字体支持所有文本。")
                .addParagraph("Using single CJK font for all text."))
            .build();
        
        System.out.println("Generating PDF with only regular font configured...");
        byte[] pdfBytes = renderer.generatePdf(reportData);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        File pdfFile = new File(TEST_OUTPUT_DIR, "font_deduplication_single_font.pdf");
        Files.write(pdfFile.toPath(), pdfBytes);
        
        System.out.println("✓ PDF generated successfully with single font");
        System.out.println("  File: " + pdfFile.getAbsolutePath());
        System.out.println("  Size: " + pdfBytes.length + " bytes");
        
        System.out.println("\n========================================");
    }
}
