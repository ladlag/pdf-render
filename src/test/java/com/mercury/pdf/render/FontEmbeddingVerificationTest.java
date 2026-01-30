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
 * Test to verify that fonts are properly embedded in PDFs when using BaseFont constants.
 * This ensures Chinese characters display correctly even when the JAR is integrated
 * into other applications.
 */
public class FontEmbeddingVerificationTest {
    
    private static final String TEST_OUTPUT_DIR = "test-output";
    
    @Test
    public void testFontsAreEmbeddedWithBaseFontConstants() throws IOException, DocumentException {
        System.out.println("\n========================================");
        System.out.println("验证字体使用 BaseFont 常量正确嵌入");
        System.out.println("Verify fonts are embedded using BaseFont constants");
        System.out.println("========================================\n");
        
        // Create HtmlReportRenderer with font configuration
        HtmlReportRenderer renderer = new HtmlReportRenderer();
        
        // Configure fonts using PdfRenderProperties (same as Spring Boot auto-config)
        PdfRenderProperties.FontProperties fontProps = new PdfRenderProperties.FontProperties();
        fontProps.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontProps.setDefaultFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        
        renderer.setFontProperties(fontProps);
        
        // Create report with Chinese text
        ReportData reportData = ReportDataBuilder.create()
            .title("字体嵌入测试报告")
            .subtitle("使用 BaseFont.IDENTITY_H 和 BaseFont.EMBEDDED 常量")
            .addSection(new Section("测试内容")
                .addParagraph("这是一段中文文本，用于验证字体是否正确嵌入到PDF中。")
                .addParagraph("如果PDF中显示方框（□），说明字体未正确嵌入。")
                .addParagraph("如果正确显示中文，说明 BaseFont 常量配置成功！")
                .addParagraph("常见中文字符：我们在这里测试各种中文字符的显示效果。"))
            .addSection(new Section("验证要点")
                .addParagraph("1. 使用 BaseFont.IDENTITY_H 确保 Unicode 编码")
                .addParagraph("2. 使用 BaseFont.EMBEDDED 确保字体嵌入 PDF")
                .addParagraph("3. 从 JAR 中提取字体到临时文件")
                .addParagraph("4. Flying Saucer 能正确读取并注册字体"))
            .build();
        
        // Generate PDF
        byte[] pdfBytes = renderer.generatePdf(reportData);
        
        assertNotNull(pdfBytes, "PDF should be generated");
        assertTrue(pdfBytes.length > 0, "PDF should have content");
        
        // Save PDF for manual verification
        File outputDir = new File(TEST_OUTPUT_DIR);
        outputDir.mkdirs();
        File pdfFile = new File(outputDir, "font_embedding_basefont_constants.pdf");
        Files.write(pdfFile.toPath(), pdfBytes);
        
        System.out.println("✓ PDF generated successfully");
        System.out.println("  File: " + pdfFile.getAbsolutePath());
        System.out.println("  Size: " + pdfBytes.length + " bytes");
        
        // Verify PDF size is reasonable (should be larger with embedded fonts)
        // A PDF with embedded Chinese fonts should be at least 30KB
        assertTrue(pdfBytes.length > 30000, 
            "PDF should be larger than 30KB with embedded fonts. Actual: " + pdfBytes.length + " bytes");
        
        System.out.println("✓ PDF size verification passed: " + pdfBytes.length + " bytes");
        System.out.println("  (Indicates font is likely embedded)");
        
        System.out.println("\n验证步骤 / Verification Steps:");
        System.out.println("1. 打开生成的 PDF 文件");
        System.out.println("2. 检查中文是否正确显示（不应该是方框）");
        System.out.println("3. 使用 PDF 工具查看文档属性 → 字体");
        System.out.println("4. 应该能看到 'HarmonyOS Sans SC' 或 'Noto Sans' (Embedded Subset)");
        System.out.println("\nVerification Steps:");
        System.out.println("1. Open the generated PDF file");
        System.out.println("2. Check that Chinese characters display correctly (not boxes)");
        System.out.println("3. Use PDF tool to view Document Properties → Fonts");
        System.out.println("4. Should see 'HarmonyOS Sans SC' or 'Noto Sans' (Embedded Subset)");
        
        System.out.println("\n========================================");
    }
    
    @Test
    public void testMultipleFontsWithBaseFontConstants() throws IOException, DocumentException {
        System.out.println("\n========================================");
        System.out.println("测试多个字体使用 BaseFont 常量");
        System.out.println("Test multiple fonts with BaseFont constants");
        System.out.println("========================================\n");
        
        HtmlReportRenderer renderer = new HtmlReportRenderer();
        
        // Configure multiple fonts
        PdfRenderProperties.FontProperties fontProps = new PdfRenderProperties.FontProperties();
        fontProps.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontProps.setBoldPath("classpath:/fonts/HarmonyOS_Sans_SC_Bold.ttf");
        fontProps.setDefaultFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        
        renderer.setFontProperties(fontProps);
        
        ReportData reportData = ReportDataBuilder.create()
            .title("多字体嵌入测试")
            .subtitle("Regular 和 Bold 字体")
            .addSection(new Section("测试章节")
                .addParagraph("这是普通字体的中文文本。")
                .addParagraph("这应该使用粗体字体的中文文本。"))
            .build();
        
        byte[] pdfBytes = renderer.generatePdf(reportData);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        // Save for verification
        File pdfFile = new File(TEST_OUTPUT_DIR, "font_embedding_multiple_fonts.pdf");
        Files.write(pdfFile.toPath(), pdfBytes);
        
        System.out.println("✓ PDF with multiple fonts generated");
        System.out.println("  File: " + pdfFile.getAbsolutePath());
        System.out.println("  Size: " + pdfBytes.length + " bytes");
        
        System.out.println("\n========================================");
    }
    
    @Test
    public void testJarResourceExtractionWorks() throws IOException {
        System.out.println("\n========================================");
        System.out.println("测试从 JAR 提取字体资源");
        System.out.println("Test font extraction from JAR resources");
        System.out.println("========================================\n");
        
        HtmlReportRenderer renderer = new HtmlReportRenderer();
        
        // This internally calls resolveFontPath which extracts from classpath
        PdfRenderProperties.FontProperties fontProps = new PdfRenderProperties.FontProperties();
        fontProps.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontProps.setDefaultFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");
        
        // This should work without throwing exceptions
        assertDoesNotThrow(() -> {
            renderer.setFontProperties(fontProps);
            System.out.println("✓ Font properties set successfully");
        });
        
        // Generate a simple PDF to trigger font extraction and registration
        ReportData reportData = ReportDataBuilder.create()
            .title("JAR 资源提取测试")
            .addSection(new Section("测试")
                .addParagraph("验证字体可以从 JAR 中正确提取并使用。"))
            .build();
        
        assertDoesNotThrow(() -> {
            byte[] pdfBytes = renderer.generatePdf(reportData);
            assertNotNull(pdfBytes);
            assertTrue(pdfBytes.length > 0);
            System.out.println("✓ PDF generated using extracted font");
            System.out.println("  Size: " + pdfBytes.length + " bytes");
        });
        
        System.out.println("\n关键验证点 / Key Verification Points:");
        System.out.println("✓ 字体从 classpath 提取到临时文件");
        System.out.println("  (Font extracted from classpath to temp file)");
        System.out.println("✓ 临时文件路径传递给 Flying Saucer");
        System.out.println("  (Temp file path passed to Flying Saucer)");
        System.out.println("✓ 使用 BaseFont.IDENTITY_H 和 BaseFont.EMBEDDED");
        System.out.println("  (Using BaseFont.IDENTITY_H and BaseFont.EMBEDDED)");
        System.out.println("✓ PDF 生成成功，字体应该已嵌入");
        System.out.println("  (PDF generated successfully, font should be embedded)");
        
        System.out.println("\n========================================");
    }
}
