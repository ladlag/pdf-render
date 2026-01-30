package com.mercury.pdf.render;

import com.lowagie.text.DocumentException;
import com.mercury.pdf.render.config.PdfRenderProperties;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 诊断工具：验证字体是否正确嵌入到PDF中
 * Diagnostic tool: Verify that fonts are correctly embedded in PDF
 * 
 * 运行此工具后，请检查控制台输出和生成的PDF文件
 * After running this tool, check console output and generated PDF file
 * 
 * 运行方法 / How to run:
 * java -cp "target/classes:..." com.mercury.pdf.render.FontEmbeddingDiagnostic
 */
public class FontEmbeddingDiagnostic {
    
    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║         字体嵌入诊断工具 / Font Embedding Diagnostic         ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
        
        try {
            runDiagnostic();
        } catch (Exception e) {
            System.err.println("\n✗ 诊断过程中出现错误 / Error during diagnostic:");
            e.printStackTrace();
        }
    }
    
    private static void runDiagnostic() throws IOException, DocumentException {
        System.out.println("步骤 1/5: 创建 HtmlReportRenderer");
        System.out.println("Step 1/5: Creating HtmlReportRenderer");
        System.out.println("─────────────────────────────────────────────────────────────────");
        
        HtmlReportRenderer renderer = new HtmlReportRenderer();
        System.out.println("✓ HtmlReportRenderer 创建成功\n");
        
        System.out.println("步骤 2/5: 配置字体属性");
        System.out.println("Step 2/5: Configuring font properties");
        System.out.println("─────────────────────────────────────────────────────────────────");
        
        PdfRenderProperties.FontProperties fontProps = new PdfRenderProperties.FontProperties();
        fontProps.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontProps.setDefaultFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        
        System.out.println("配置详情 / Configuration:");
        System.out.println("  Regular Path: " + fontProps.getRegularPath());
        System.out.println("  Default Family: " + fontProps.getDefaultFamily());
        System.out.println();
        
        renderer.setFontProperties(fontProps);
        System.out.println("✓ 字体属性配置完成\n");
        
        System.out.println("步骤 3/5: 创建测试报告数据");
        System.out.println("Step 3/5: Creating test report data");
        System.out.println("─────────────────────────────────────────────────────────────────");
        
        ReportData reportData = ReportDataBuilder.create()
            .title("字体嵌入诊断测试 Font Embedding Diagnostic Test")
            .subtitle("验证中文字符是否正确显示 Verify Chinese Character Display")
            .addSection(new Section("诊断说明 Diagnostic Instructions")
                .addParagraph("如果您看到这段中文而不是方框，说明字体嵌入成功！")
                .addParagraph("If you see Chinese characters instead of boxes, font embedding succeeded!")
                .addParagraph("测试字符：汉字、中文、PDF、渲染、Flying Saucer"))
            .addSection(new Section("常见问题排查 Troubleshooting")
                .addParagraph("问题1：如果看到方框（□），检查控制台是否有字体验证警告")
                .addParagraph("问题2：确认 CSS font-family 与字体内部名称匹配")
                .addParagraph("问题3：使用 PDF 工具查看文档属性中的嵌入字体列表"))
            .build();
        
        System.out.println("✓ 测试报告数据创建完成");
        System.out.println("  包含中文字符用于测试显示效果\n");
        
        System.out.println("步骤 4/5: 生成 PDF（此时会提取和注册字体）");
        System.out.println("Step 4/5: Generating PDF (font extraction and registration)");
        System.out.println("─────────────────────────────────────────────────────────────────");
        System.out.println("请注意以下日志输出 / Watch for the following logs:\n");
        
        byte[] pdfBytes = renderer.generatePdf(reportData);
        
        System.out.println("\n✓ PDF 生成完成");
        System.out.println("  文件大小: " + pdfBytes.length + " bytes");
        
        if (pdfBytes.length < 30000) {
            System.out.println("  ⚠️  警告：PDF 文件较小（< 30KB），可能字体未嵌入");
        } else if (pdfBytes.length > 40000) {
            System.out.println("  ✓ PDF 文件较大（> 40KB），很可能字体已嵌入");
        }
        System.out.println();
        
        System.out.println("步骤 5/5: 保存 PDF 文件");
        System.out.println("Step 5/5: Saving PDF file");
        System.out.println("─────────────────────────────────────────────────────────────────");
        
        File outputDir = new File("test-output");
        outputDir.mkdirs();
        File pdfFile = new File(outputDir, "font_embedding_diagnostic.pdf");
        
        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            fos.write(pdfBytes);
        }
        
        System.out.println("✓ PDF 已保存到: " + pdfFile.getAbsolutePath());
        System.out.println();
        
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    诊断完成 Diagnostic Complete                ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
        
        System.out.println("下一步验证 / Next Steps for Verification:");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        System.out.println("1️⃣  打开生成的 PDF 文件");
        System.out.println("   Open the generated PDF file:");
        System.out.println("   " + pdfFile.getAbsolutePath());
        System.out.println();
        System.out.println("2️⃣  检查中文是否正确显示");
        System.out.println("   Check if Chinese characters display correctly");
        System.out.println("   ✓ 正确：看到清晰的中文字符");
        System.out.println("   ✗ 错误：看到方框（□□□）");
        System.out.println();
        System.out.println("3️⃣  使用 PDF 阅读器查看文档属性");
        System.out.println("   Use PDF reader to view document properties:");
        System.out.println("   - Adobe Reader: 文件 → 属性 → 字体");
        System.out.println("   - PDF Reader: File → Properties → Fonts");
        System.out.println();
        System.out.println("4️⃣  应该看到嵌入的字体");
        System.out.println("   Should see embedded fonts:");
        System.out.println("   ✓ 'HarmonyOS Sans SC' (Embedded Subset)");
        System.out.println("   或 'Noto Sans CJK SC' (Embedded Subset)");
        System.out.println();
        System.out.println("5️⃣  如果看到的是系统字体（非嵌入）");
        System.out.println("   If you see system fonts (not embedded):");
        System.out.println("   ✗ Helvetica, Times-Roman, DejaVu Sans");
        System.out.println("   → 说明字体未成功嵌入，请检查控制台警告");
        System.out.println();
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        System.out.println("查看上方日志输出，应该看到：");
        System.out.println("Check the logs above, you should see:");
        System.out.println("  ✓ Font extracted for PDF rendering: HarmonyOS_Sans_SC_Regular.ttf");
        System.out.println("  ✓ Font registered with Flying Saucer: /tmp/pdf-render-font-xxx.ttf");
        System.out.println("  ✓ Total fonts registered for PDF: 1");
        System.out.println();
        System.out.println("如果没有看到这些日志，或者看到错误信息，请：");
        System.out.println("If you don't see these logs or see errors:");
        System.out.println("  1. 检查字体文件是否在 JAR 中");
        System.out.println("     Check if font file is in JAR");
        System.out.println("  2. 检查 classpath 路径是否正确（必须以 / 开头）");
        System.out.println("     Check classpath path is correct (must start with /)");
        System.out.println("  3. 检查 CSS font-family 是否与字体内部名称匹配");
        System.out.println("     Check CSS font-family matches font's internal name");
        System.out.println();
    }
}
