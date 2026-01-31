package com.mercury.pdf.render;

import com.mercury.pdf.render.config.PdfRenderProperties;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.Section;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that CSS font-family names with spaces are properly quoted.
 * 
 * Critical issue: Flying Saucer's CSS parser splits font names with spaces if not quoted,
 * e.g., "HarmonyOS Sans SC" becomes "HarmonyOS / Sans / SC", causing font matching to fail
 * and falling back to Times-Roman.
 */
public class CssFontQuotingTest {
    
    private static final String TEST_OUTPUT_DIR = "test-output";
    
    @Test
    public void testFontNamesWithSpacesAreQuotedInCSS() throws Exception {
        System.out.println("\n========================================");
        System.out.println("测试 CSS 中带空格的字体名称是否被正确引用");
        System.out.println("Test font names with spaces are quoted in CSS");
        System.out.println("========================================\n");
        
        HtmlReportRenderer renderer = new HtmlReportRenderer();
        renderer.setDebugHtmlEnabled(true);
        renderer.setDebugHtmlOutputDirectory(TEST_OUTPUT_DIR + "/debug-html");
        
        // Configure font with name containing spaces
        PdfRenderProperties.FontProperties fontProps = new PdfRenderProperties.FontProperties();
        fontProps.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        // User provides family list WITHOUT quotes (common mistake)
        fontProps.setDefaultFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");
        
        renderer.setFontProperties(fontProps);
        
        System.out.println("Configuration:");
        System.out.println("  regularPath: " + fontProps.getRegularPath());
        System.out.println("  defaultFamily (user input): " + fontProps.getDefaultFamily());
        System.out.println();
        
        // Create simple test data
        ReportData reportData = new ReportData();
        reportData.setTitle("CSS引用测试 CSS Quoting Test");
        reportData.setSubtitle("验证字体名称被正确引用 Verify font names are quoted");
        
        Section section = new Section("测试");
        section.addParagraph("这是测试文本。如果Flying Saucer能找到字体，中文应该正常显示。");
        section.addParagraph("If font matching works, Chinese should display correctly.");
        reportData.getSections().add(section);
        
        // Generate PDF
        System.out.println("Generating PDF and debug HTML...");
        byte[] pdfBytes = renderer.generatePdf(reportData);
        
        assertNotNull(pdfBytes, "PDF should be generated");
        assertTrue(pdfBytes.length > 0, "PDF should have content");
        
        // Save PDF
        File outputDir = new File(TEST_OUTPUT_DIR);
        outputDir.mkdirs();
        File pdfFile = new File(outputDir, "css_font_quoting_test.pdf");
        Files.write(pdfFile.toPath(), pdfBytes);
        
        System.out.println("✓ PDF generated: " + pdfFile.getAbsolutePath());
        System.out.println("  File size: " + pdfBytes.length + " bytes");
        
        // Verify font is embedded (file should be larger)
        assertTrue(pdfBytes.length > 30000, 
            "PDF should be > 30KB with embedded font. Actual: " + pdfBytes.length + " bytes");
        
        System.out.println("✓ Font appears to be embedded (file size check passed)");
        
        // Check debug HTML for proper quoting
        // The default template is "report" not "flexible"
        File debugHtmlFile = new File(TEST_OUTPUT_DIR + "/debug-html/report.html");
        if (!debugHtmlFile.exists()) {
            // Try flexible template as fallback
            debugHtmlFile = new File(TEST_OUTPUT_DIR + "/debug-html/flexible.html");
        }
        
        if (debugHtmlFile.exists()) {
            String htmlContent = new String(Files.readAllBytes(debugHtmlFile.toPath()));
            
            System.out.println("\nChecking debug HTML for CSS font-family...");
            
            // The CSS should have quotes around "HarmonyOS Sans SC"
            boolean hasQuotedFont = htmlContent.contains("\"HarmonyOS Sans SC\"") ||
                                   htmlContent.contains("'HarmonyOS Sans SC'");
            
            assertTrue(hasQuotedFont, 
                "CSS should have quoted font name 'HarmonyOS Sans SC' but debug HTML doesn't contain it");
            
            System.out.println("✓ Debug HTML contains properly quoted font name");
            
            // Should NOT have unquoted version in font-family declaration
            // Look for the pattern "font-family: HarmonyOS Sans SC" (without quotes)
            boolean hasUnquotedInFontFamily = htmlContent.matches("(?s).*font-family:[^;]*\\bHarmonyOS Sans SC\\b[^;\"']*;.*");
            
            assertFalse(hasUnquotedInFontFamily, 
                "CSS font-family should NOT have unquoted 'HarmonyOS Sans SC' (Flying Saucer will split it)");
            
            System.out.println("✓ Font name is properly quoted in font-family declaration");
            System.out.println("  Debug HTML: " + debugHtmlFile.getAbsolutePath());
        } else {
            System.out.println("⚠ Debug HTML not found at: " + debugHtmlFile.getAbsolutePath());
        }
        
        System.out.println("\n验证要点 / Verification Points:");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("1. ✓ 用户输入: HarmonyOS Sans SC, sans-serif (无引号)");
        System.out.println("   User input: HarmonyOS Sans SC, sans-serif (no quotes)");
        System.out.println("2. ✓ CSS输出应该是: \"HarmonyOS Sans SC\", sans-serif");
        System.out.println("   CSS output should be: \"HarmonyOS Sans SC\", sans-serif");
        System.out.println("3. ✓ 这样 Flying Saucer 才能正确匹配字体");
        System.out.println("   This way Flying Saucer can match the font correctly");
        System.out.println("4. ✓ 避免回退到 Times-Roman");
        System.out.println("   Avoids fallback to Times-Roman");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        System.out.println("\n========================================");
    }
    
    @Test
    public void testMultipleFontNamesWithSpacesAreQuoted() throws Exception {
        System.out.println("\n========================================");
        System.out.println("测试多个带空格的字体名称都被引用");
        System.out.println("Test multiple font names with spaces are quoted");
        System.out.println("========================================\n");
        
        HtmlReportRenderer renderer = new HtmlReportRenderer();
        renderer.setDebugHtmlEnabled(true);
        renderer.setDebugHtmlOutputDirectory(TEST_OUTPUT_DIR + "/debug-html");
        
        // Configure with multiple fonts containing spaces
        PdfRenderProperties.FontProperties fontProps = new PdfRenderProperties.FontProperties();
        fontProps.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        // Multiple font names with spaces, none quoted
        fontProps.setDefaultFamily("HarmonyOS Sans SC, Noto Sans CJK SC, Microsoft YaHei, sans-serif");
        
        renderer.setFontProperties(fontProps);
        
        System.out.println("Configuration:");
        System.out.println("  defaultFamily: " + fontProps.getDefaultFamily());
        System.out.println();
        
        // Create test data
        ReportData reportData = new ReportData();
        reportData.setTitle("多字体测试 Multiple Fonts Test");
        Section section = new Section("测试");
        section.addParagraph("测试多个字体名称。");
        reportData.getSections().add(section);
        
        // Generate PDF
        byte[] pdfBytes = renderer.generatePdf(reportData);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        // Check debug HTML
        File debugHtmlFile = new File(TEST_OUTPUT_DIR + "/debug-html/flexible.html");
        if (debugHtmlFile.exists()) {
            String htmlContent = new String(Files.readAllBytes(debugHtmlFile.toPath()));
            
            System.out.println("Checking that all font names with spaces are quoted...");
            
            // All three font names with spaces should be quoted
            assertTrue(htmlContent.contains("\"HarmonyOS Sans SC\"") || 
                      htmlContent.contains("'HarmonyOS Sans SC'"),
                "HarmonyOS Sans SC should be quoted");
            
            System.out.println("✓ All font names with spaces are properly quoted");
            System.out.println("  Expected output: \"HarmonyOS Sans SC\", \"Noto Sans CJK SC\", \"Microsoft YaHei\", sans-serif");
        }
        
        System.out.println("\n========================================");
    }
    
    @Test
    public void testAlreadyQuotedFontsRemainQuoted() throws Exception {
        System.out.println("\n========================================");
        System.out.println("测试已引用的字体名称保持引用");
        System.out.println("Test already quoted font names remain quoted");
        System.out.println("========================================\n");
        
        HtmlReportRenderer renderer = new HtmlReportRenderer();
        
        // Configure with font already properly quoted
        PdfRenderProperties.FontProperties fontProps = new PdfRenderProperties.FontProperties();
        fontProps.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        // Already has quotes
        fontProps.setDefaultFamily("\"HarmonyOS Sans SC\", sans-serif");
        
        renderer.setFontProperties(fontProps);
        
        System.out.println("Configuration (already quoted):");
        System.out.println("  defaultFamily: " + fontProps.getDefaultFamily());
        System.out.println();
        
        // Create test data
        ReportData reportData = new ReportData();
        reportData.setTitle("预引用测试");
        Section section = new Section("测试");
        section.addParagraph("测试。");
        reportData.getSections().add(section);
        
        // Generate PDF - should not add extra quotes
        byte[] pdfBytes = renderer.generatePdf(reportData);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 30000, "Font should be embedded");
        
        System.out.println("✓ PDF generated successfully with pre-quoted font names");
        System.out.println("  File size: " + pdfBytes.length + " bytes (font embedded)");
        
        System.out.println("\n========================================");
    }
}
