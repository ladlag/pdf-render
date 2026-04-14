package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Demonstration showing the importance of font configuration for Chinese text.
 * 
 * This demo generates TWO PDFs:
 * 1. WITHOUT font configuration - Chinese shows as boxes (□)
 * 2. WITH font configuration - Chinese displays correctly
 * 
 * Run this to understand why FontConfig is essential for Chinese support.
 */
public class ChineseFontConfigurationDemo {

    public static void main(String[] args) {
        try {
            System.out.println("╔═══════════════════════════════════════════════════════════════╗");
            System.out.println("║  中文字体配置演示 Chinese Font Configuration Demo            ║");
            System.out.println("╚═══════════════════════════════════════════════════════════════╝\n");
            
            // Create test data with Chinese content
            ReportData chineseData = createChineseReportData();
            
            // Demo 1: WITHOUT font configuration (WRONG way - Chinese will show as boxes)
            System.out.println("演示 1: 不配置字体 (错误方式)");
            System.out.println("Demo 1: WITHOUT Font Configuration (WRONG)");
            System.out.println("─────────────────────────────────────────────────────────────");
            demonstrateWithoutFontConfig(chineseData);
            
            System.out.println("\n");
            
            // Demo 2: WITH font configuration (CORRECT way - Chinese will display properly)
            System.out.println("演示 2: 正确配置字体 (正确方式)");
            System.out.println("Demo 2: WITH Font Configuration (CORRECT)");
            System.out.println("─────────────────────────────────────────────────────────────");
            demonstrateWithFontConfig(chineseData);
            
            System.out.println("\n╔═══════════════════════════════════════════════════════════════╗");
            System.out.println("║  对比结果 Comparison Results                                  ║");
            System.out.println("╚═══════════════════════════════════════════════════════════════╝");
            System.out.println("\n请打开两个PDF文件进行比较：");
            System.out.println("Please open both PDF files to compare:");
            System.out.println("  1. demo_WITHOUT_font_config.pdf - 中文显示为方框 □");
            System.out.println("  2. demo_WITH_font_config.pdf    - 中文正常显示 ✓");
            System.out.println("\n结论 Conclusion:");
            System.out.println("  为了显示中文，必须使用 FontConfig！");
            System.out.println("  FontConfig is ESSENTIAL for Chinese character display!");
            
        } catch (Exception e) {
            System.err.println("错误 Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Demo 1: Generate PDF WITHOUT font configuration
     * Result: Chinese characters will appear as boxes (□)
     */
    private static void demonstrateWithoutFontConfig(ReportData data) throws IOException {
        System.out.println("代码 Code:");
        System.out.println("  PdfRenderService service = new PdfRenderService();");
        System.out.println("  // ❌ NO FontConfig set!");
        System.out.println("  byte[] pdf = service.generatePdf(data);");
        
        PdfRenderService service = new PdfRenderService();
        // ❌ NO FontConfig configured - this is the WRONG way
        
        byte[] pdf = service.generatePdf(data, "flexible");
        
        String filename = "test-output/demo_WITHOUT_font_config.pdf";
        Files.write(Paths.get(filename), pdf);
        
        System.out.println("\n结果 Result:");
        System.out.println("  ⚠️  文件生成 File generated: " + filename);
        System.out.println("  ⚠️  文件大小 Size: " + (pdf.length / 1024) + " KB (很小 too small)");
        System.out.println("  ⚠️  中文字符将显示为方框 Chinese will show as boxes (□)");
        System.out.println("  ⚠️  原因 Reason: 没有嵌入中文字体 No Chinese font embedded");
    }
    
    /**
     * Demo 2: Generate PDF WITH proper font configuration
     * Result: Chinese characters will display correctly
     */
    private static void demonstrateWithFontConfig(ReportData data) throws IOException {
        System.out.println("代码 Code:");
        System.out.println("  PdfRenderService service = new PdfRenderService();");
        System.out.println("  ");
        System.out.println("  // ✓ Configure FontConfig correctly!");
        System.out.println("  FontConfig fontConfig = new FontConfig();");
        System.out.println("  fontConfig.setRegularFontPath(");
        System.out.println("      \"classpath:/fonts/NotoSansCJKsc-Regular.otf\");");
        System.out.println("  fontConfig.setDefaultFontFamily(");
        System.out.println("      \"Noto Sans CJK SC, DejaVu Sans, sans-serif\");");
        System.out.println("  service.getHtmlRenderer().setFontConfig(fontConfig);");
        System.out.println("  ");
        System.out.println("  byte[] pdf = service.generatePdf(data);");
        
        PdfRenderService service = new PdfRenderService();
        
        // ✓ Configure FontConfig - this is the CORRECT way
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/NotoSansCJKsc-Regular.otf");
        fontConfig.setDefaultFontFamily("Noto Sans CJK SC, DejaVu Sans, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);
        
        byte[] pdf = service.generatePdf(data, "flexible");
        
        String filename = "test-output/demo_WITH_font_config.pdf";
        Files.write(Paths.get(filename), pdf);
        
        System.out.println("\n结果 Result:");
        System.out.println("  ✓ 文件生成 File generated: " + filename);
        System.out.println("  ✓ 文件大小 Size: " + (pdf.length / 1024) + " KB (包含字体 with font)");
        System.out.println("  ✓ 中文字符正常显示 Chinese displays correctly!");
        System.out.println("  ✓ 原因 Reason: 字体已正确嵌入 Font properly embedded");
    }
    
    /**
     * Creates sample report data with Chinese content
     */
    private static ReportData createChineseReportData() {
        return ReportDataBuilder.create()
            .title("中文字体测试报告")
            .subtitle("Chinese Font Test Report")
            .reportDate("2024-12-31")
            .reportNumber("DEMO-CN-001")
            
            .addSection(new Section("第一章：问题说明")
                .addParagraph("在生成PDF时，如果不正确配置字体，中文字符会显示为方框（□）。"))
            
            .addSection(new Section("第二章：解决方案")
                .addParagraph("必须使用 FontConfig 类配置中文字体，包括：")
                .addParagraph("1. 设置字体文件路径（regularFontPath）")
                .addParagraph("2. 设置字体族名称（defaultFontFamily）")
                .addParagraph("3. 调用 setFontConfig() 应用配置"))
            
            .addSection(new Section("第三章：常用汉字测试")
                .addParagraph("这是一些常用汉字：的一是在不了有和人这中大为上个国我以要他时来用们生到作地"))
            
            .reportNotice("本文档用于演示中文字体配置的重要性")
            .metadata("测试系统 | 生成时间：2024-12-31")
            .build();
    }
}
