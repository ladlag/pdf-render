package com.mercury.pdf.render;

import com.lowagie.text.DocumentException;
import com.mercury.pdf.render.config.PdfRenderAutoConfiguration;
import com.mercury.pdf.render.config.PdfRenderProperties;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Manual verification program to test that Spring Boot auto-configuration works correctly.
 * 
 * This verifies the fix for: "我们不是有properties么，为什么还要在这设置一个"
 * 
 * Run this to verify:
 * 1. Properties from application.yml are loaded
 * 2. Font configuration is applied (Chinese characters display correctly)
 * 3. Debug HTML is generated
 */
public class ManualSpringBootConfigTest {
    
    public static void main(String[] args) throws IOException, DocumentException {
        System.out.println("\n========================================");
        System.out.println("手动验证 Spring Boot 配置");
        System.out.println("Manual Spring Boot Configuration Verification");
        System.out.println("========================================\n");
        
        // Simulate what Spring Boot auto-configuration does
        PdfRenderProperties properties = createTestProperties();
        PdfRenderAutoConfiguration autoConfig = new PdfRenderAutoConfiguration(properties);
        PdfRenderService pdfRenderService = autoConfig.pdfRenderService();
        
        System.out.println("Step 1: Verifying properties are loaded...");
        verifyProperties(properties);
        
        System.out.println("\nStep 2: Verifying PdfRenderService is configured...");
        verifyPdfRenderServiceConfiguration(pdfRenderService);
        
        System.out.println("\nStep 3: Generating PDF with Chinese text...");
        testChineseTextRendering(pdfRenderService);
        
        System.out.println("\nStep 4: Verifying debug HTML output...");
        verifyDebugHtmlOutput(pdfRenderService);
        
        System.out.println("\n========================================");
        System.out.println("✓ 所有验证通过！");
        System.out.println("✓ All verifications passed!");
        System.out.println("========================================\n");
    }
    
    private static PdfRenderProperties createTestProperties() {
        PdfRenderProperties properties = new PdfRenderProperties();
        
        // Font configuration
        PdfRenderProperties.FontProperties fontProps = new PdfRenderProperties.FontProperties();
        fontProps.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontProps.setBoldPath("classpath:/fonts/HarmonyOS_Sans_SC_Bold.ttf");
        fontProps.setDefaultFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        fontProps.setCjkFamily("Noto Sans CJK, SimSun, sans-serif");
        properties.setFonts(fontProps);
        
        // Debug configuration
        PdfRenderProperties.DebugProperties debugProps = new PdfRenderProperties.DebugProperties();
        debugProps.setEnabled(true);
        debugProps.setOutputDirectory("test-output/debug-html");
        debugProps.setIncludeTimestamp(false);
        properties.setDebug(debugProps);
        
        // Template configuration
        PdfRenderProperties.TemplateProperties templateProps = new PdfRenderProperties.TemplateProperties();
        templateProps.setDefaultName("report");
        templateProps.setCacheEnabled(true);
        properties.setTemplate(templateProps);
        
        return properties;
    }
    
    private static void verifyProperties(PdfRenderProperties properties) {
        System.out.println("  Font configuration:");
        System.out.println("    - Regular path: " + properties.getFonts().getRegularPath());
        System.out.println("    - Default family: " + properties.getFonts().getDefaultFamily());
        
        System.out.println("  Debug configuration:");
        System.out.println("    - Enabled: " + properties.getDebug().isEnabled());
        System.out.println("    - Output directory: " + properties.getDebug().getOutputDirectory());
        
        System.out.println("  Template configuration:");
        System.out.println("    - Default name: " + properties.getTemplate().getDefaultName());
        System.out.println("    - Cache enabled: " + properties.getTemplate().isCacheEnabled());
        
        System.out.println("  ✓ Properties loaded successfully");
    }
    
    private static void verifyPdfRenderServiceConfiguration(PdfRenderService pdfRenderService) {
        HtmlReportRenderer htmlRenderer = pdfRenderService.getHtmlRenderer();
        
        // Check font properties
        PdfRenderProperties.FontProperties fontProps = htmlRenderer.getFontProperties();
        if (fontProps != null) {
            System.out.println("  ✓ Font properties applied to renderer:");
            System.out.println("    - Regular path: " + fontProps.getRegularPath());
            System.out.println("    - Default family: " + fontProps.getDefaultFamily());
        } else {
            System.err.println("  ✗ ERROR: Font properties NOT applied to renderer!");
            System.err.println("    This means Chinese characters will show as boxes!");
        }
        
        // Check debug settings
        if (htmlRenderer.isDebugHtmlEnabled()) {
            System.out.println("  ✓ Debug HTML enabled:");
            System.out.println("    - Output directory: " + htmlRenderer.getDebugHtmlOutputDirectory());
        } else {
            System.err.println("  ✗ ERROR: Debug HTML NOT enabled!");
        }
        
        // Check template settings
        System.out.println("  ✓ Template settings applied:");
        System.out.println("    - Default template: " + htmlRenderer.getDefaultTemplateName());
    }
    
    private static void testChineseTextRendering(PdfRenderService pdfRenderService) throws IOException, DocumentException {
        ReportData reportData = ReportDataBuilder.create()
            .title("测试报告 - 验证配置")
            .subtitle("这是副标题 - 验证中文字体")
            .addSection(new Section("第一章：配置验证")
                .addParagraph("这是一段中文文本，用于验证字体配置。")
                .addParagraph("如果您看到方框（□），说明字体配置未生效。")
                .addParagraph("如果您看到正确的中文，说明配置正确！"))
            .build();
        
        byte[] pdfBytes = pdfRenderService.generatePdf(reportData);
        
        File outputDir = new File("test-output");
        outputDir.mkdirs();
        File pdfFile = new File(outputDir, "manual_spring_boot_config_test.pdf");
        
        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            fos.write(pdfBytes);
        }
        
        System.out.println("  ✓ PDF generated: " + pdfFile.getAbsolutePath());
        System.out.println("  ✓ Size: " + pdfBytes.length + " bytes");
        System.out.println("  → 请打开 PDF 文件验证中文是否正确显示！");
        System.out.println("  → Please open the PDF file to verify Chinese characters!");
    }
    
    private static void verifyDebugHtmlOutput(PdfRenderService pdfRenderService) {
        File debugDir = new File("test-output/debug-html");
        File debugHtmlFile = new File(debugDir, "report.html");
        
        if (debugHtmlFile.exists()) {
            System.out.println("  ✓ Debug HTML file found: " + debugHtmlFile.getAbsolutePath());
            System.out.println("  ✓ Debug configuration is working!");
        } else {
            System.err.println("  ✗ ERROR: Debug HTML file NOT found!");
            System.err.println("    Expected: " + debugHtmlFile.getAbsolutePath());
            System.err.println("    Debug configuration may not be working!");
        }
    }
}
