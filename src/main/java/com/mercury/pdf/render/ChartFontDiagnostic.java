package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Simple diagnostic to check if chart fonts are being loaded correctly.
 */
public class ChartFontDiagnostic {
    
    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║  图表字体诊断 Chart Font Diagnostic                             ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝\n");
        
        ReportService service = new ReportService();
        
        // Enable debug HTML
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory("test-output");
        
        System.out.println("步骤 1: 配置字体");
        System.out.println("Step 1: Configure font\n");
        
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");
        
        System.out.println("  regularFontPath: " + fontConfig.getRegularFontPath());
        System.out.println("  defaultFontFamily: " + fontConfig.getDefaultFontFamily());
        System.out.println();
        
        System.out.println("步骤 2: 应用字体配置");
        System.out.println("Step 2: Apply font configuration\n");
        
        service.getHtmlRenderer().setFontConfig(fontConfig);
        
        System.out.println("  ⚠️  注意查看上面是否有字体加载日志");
        System.out.println("  ⚠️  Check above for font loading logs");
        System.out.println("  应该看到:");
        System.out.println("  Should see:");
        System.out.println("    ✓ Configuring chart font from regular font: ...");
        System.out.println("    ✓ Chart font loaded successfully: ...");
        System.out.println("    Chart font family: ...");
        System.out.println();
        
        System.out.println("步骤 3: 创建包含中文的图表");
        System.out.println("Step 3: Create chart with Chinese text\n");
        
        Map<String, Double> data = new LinkedHashMap<>();
        data.put("北京", 100.0);
        data.put("上海", 150.0);
        data.put("深圳", 120.0);
        
        ChartConfig config = new ChartConfig();
        config.setXAxisLabel("城市");
        config.setYAxisLabel("数值");
        config.setWidth(600);
        config.setHeight(400);
        
        ChartData chart = new ChartData("测试图表", "bar", data);
        chart.setConfig(config);
        
        Section section = new Section("图表测试");
        section.addParagraph("这是正文中的中文。如果能看到，说明PDF字体配置正确。");
        section.addChart(chart);
        
        ReportData reportData = ReportDataBuilder.create()
            .title("图表字体诊断测试")
            .subtitle("Chart Font Diagnostic Test")
            .addSection(section)
            .build();
        
        System.out.println("步骤 4: 生成 PDF");
        System.out.println("Step 4: Generate PDF\n");
        
        byte[] pdf = service.generatePdf(reportData, "flexible");
        
        Files.createDirectories(Paths.get("test-output"));
        Files.write(Paths.get("test-output/chart_font_diagnostic.pdf"), pdf);
        
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("结果 Results:");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        System.out.println("PDF 文件: test-output/chart_font_diagnostic.pdf");
        System.out.println("PDF file: test-output/chart_font_diagnostic.pdf");
        System.out.println("文件大小: " + (pdf.length / 1024) + " KB");
        System.out.println("File size: " + (pdf.length / 1024) + " KB");
        System.out.println();
        System.out.println("请检查 PDF 中:");
        System.out.println("Please check in the PDF:");
        System.out.println("  1. 正文中文是否正常显示");
        System.out.println("     Body text Chinese displays correctly");
        System.out.println("  2. 图表标签中文是否正常显示");
        System.out.println("     Chart labels Chinese displays correctly");
        System.out.println("  3. 图表坐标轴中文是否正常显示");
        System.out.println("     Chart axis labels Chinese displays correctly");
        System.out.println();
        System.out.println("如果图表中文显示为方框:");
        System.out.println("If chart Chinese shows as boxes:");
        System.out.println("  - 检查上面是否有 'Chart font loaded successfully' 日志");
        System.out.println("  - Check above for 'Chart font loaded successfully' log");
        System.out.println("  - 如果没有，说明图表字体加载失败");
        System.out.println("  - If not present, chart font loading failed");
        System.out.println();
    }
}
