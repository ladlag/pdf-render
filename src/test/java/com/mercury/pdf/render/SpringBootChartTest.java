package com.mercury.pdf.render;

import com.mercury.pdf.render.config.PdfRenderAutoConfiguration;
import com.mercury.pdf.render.config.PdfRenderProperties;
import com.mercury.pdf.render.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to replicate user's exact scenario:
 * - Spring Boot with application.yml configuration
 * - matcher-report-final template
 * - regularFontPath is set
 * - Charts should display Chinese characters correctly
 */
public class SpringBootChartTest {
    
    private static final String TEST_OUTPUT_DIR = "test-output";
    
    @BeforeAll
    public static void setupTestOutputDirectory() throws IOException {
        Path outputDir = Paths.get(TEST_OUTPUT_DIR);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
    }
    
    @Test
    public void testSpringBootConfigWithMatcherReportFinalAndCharts() throws IOException {
        System.out.println("\n========================================");
        System.out.println("测试 Spring Boot 配置 + matcher-report-final + 图表");
        System.out.println("Test Spring Boot config + matcher-report-final + charts");
        System.out.println("========================================\n");
        
        // Setup properties exactly as user described
        PdfRenderProperties properties = new PdfRenderProperties();
        
        // Enable debug to see the HTML
        properties.getDebug().setEnabled(true);
        properties.getDebug().setOutputDirectory(TEST_OUTPUT_DIR + "/debug-springboot");
        
        // Configure fonts as user would in application.yml
        properties.getFonts().setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        properties.getFonts().setDefaultFamily("HarmonyOS Sans SC, sans-serif");
        
        System.out.println("配置 Configuration:");
        System.out.println("  regularPath: " + properties.getFonts().getRegularPath());
        System.out.println("  defaultFamily: " + properties.getFonts().getDefaultFamily());
        System.out.println("  template: matcher-report-final");
        System.out.println();
        
        // Create service via auto-configuration (simulates Spring Boot)
        PdfRenderAutoConfiguration autoConfig = new PdfRenderAutoConfiguration(properties);
        ReportService service = autoConfig.reportService();
        
        System.out.println("应该看到以下日志:");
        System.out.println("Should see these logs:");
        System.out.println("  ✓ Configuring chart font from regular font: ...");
        System.out.println("  ✓ Chart font loaded successfully: ...");
        System.out.println();
        
        // Create report data with Chinese charts
        Map<String, Double> chartData = new LinkedHashMap<>();
        chartData.put("北京", 100.0);
        chartData.put("上海", 150.0);
        chartData.put("深圳", 120.0);
        chartData.put("广州", 90.0);
        
        ChartConfig chartConfig = new ChartConfig();
        chartConfig.setWidth(600);
        chartConfig.setHeight(400);
        chartConfig.setXAxisLabel("城市");
        chartConfig.setYAxisLabel("数值 (万元)");
        
        ChartData chart = new ChartData("城市数据对比", "bar", chartData);
        chart.setConfig(chartConfig);
        
        Section section = new Section("数据分析");
        section.addParagraph("这是正文中的中文。Body text Chinese.");
        section.addParagraph("下面是图表，图表中的中文应该也能正常显示。");
        section.addChart(chart);
        
        ReportData reportData = ReportDataBuilder.create()
            .title("Spring Boot 图表测试")
            .subtitle("验证 matcher-report-final 模板")
            .reportDate("2024-12-31")
            .addSection(section)
            .build();
        
        System.out.println("生成 PDF (使用 matcher-report-final 模板)...");
        System.out.println("Generating PDF (using matcher-report-final template)...\n");
        
        // Generate PDF with matcher-report-final template (as user mentioned)
        byte[] pdfBytes = service.generatePdf(reportData, "matcher-report-final");
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        // Save PDF
        Path pdfPath = Paths.get(TEST_OUTPUT_DIR, "springboot_matcherfinal_chart_test.pdf");
        Files.write(pdfPath, pdfBytes);
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("结果 Results:");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        System.out.println("PDF 文件: " + pdfPath.toAbsolutePath());
        System.out.println("PDF file: " + pdfPath.toAbsolutePath());
        System.out.println("文件大小: " + (pdfBytes.length / 1024) + " KB");
        System.out.println("File size: " + (pdfBytes.length / 1024) + " KB");
        
        // Verify font is embedded (file should be larger)
        assertTrue(pdfBytes.length > 50000, 
            "PDF should be > 50KB with embedded fonts. Actual: " + (pdfBytes.length / 1024) + " KB");
        
        System.out.println("\n✓ PDF 已生成");
        System.out.println("✓ PDF generated");
        System.out.println();
        
        // Check debug HTML
        Path debugHtmlPath = Paths.get(TEST_OUTPUT_DIR + "/debug-springboot/matcher-report-final.html");
        if (Files.exists(debugHtmlPath)) {
            String htmlContent = new String(Files.readAllBytes(debugHtmlPath));
            
            System.out.println("检查 Debug HTML:");
            System.out.println("Checking Debug HTML:");
            
            // Check if font-family has proper quotes
            boolean hasQuotedFont = htmlContent.contains("\"HarmonyOS Sans SC\"") ||
                                   htmlContent.contains("'HarmonyOS Sans SC'");
            
            if (hasQuotedFont) {
                System.out.println("  ✓ CSS font-family 有正确的引号");
                System.out.println("  ✓ CSS font-family has proper quotes");
            } else {
                System.out.println("  ⚠ CSS font-family 可能缺少引号");
                System.out.println("  ⚠ CSS font-family may be missing quotes");
            }
            
            System.out.println("  Debug HTML: " + debugHtmlPath.toAbsolutePath());
        }
        
        System.out.println();
        System.out.println("请检查 PDF:");
        System.out.println("Please check PDF:");
        System.out.println("  1. 正文中文是否正常显示");
        System.out.println("     Body text Chinese displays correctly");
        System.out.println("  2. 图表标签中文是否正常显示 (关键!)");
        System.out.println("     Chart labels Chinese displays correctly (KEY!)");
        System.out.println("  3. 图表坐标轴中文是否正常显示");
        System.out.println("     Chart axis labels Chinese displays correctly");
        System.out.println();
        System.out.println("如果图表中文是方框:");
        System.out.println("If chart Chinese shows as boxes:");
        System.out.println("  - 检查上面是否有 'Chart font loaded successfully' 日志");
        System.out.println("  - Check above for 'Chart font loaded successfully' log");
        System.out.println();
        
        System.out.println("========================================\n");
    }
    
    @Test
    public void testSpringBootWithDefaultFamilyNotSet() throws IOException {
        System.out.println("\n========================================");
        System.out.println("测试: Spring Boot 只设置 regularPath, 不设置 defaultFamily");
        System.out.println("Test: Spring Boot with only regularPath, no defaultFamily");
        System.out.println("========================================\n");
        
        // This might be what user is doing - only setting regularPath
        PdfRenderProperties properties = new PdfRenderProperties();
        properties.getFonts().setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        // NOT setting defaultFamily - will use default from FontProperties
        
        System.out.println("配置 Configuration:");
        System.out.println("  regularPath: " + properties.getFonts().getRegularPath());
        System.out.println("  defaultFamily: " + properties.getFonts().getDefaultFamily() + " (default from properties)");
        System.out.println();
        
        PdfRenderAutoConfiguration autoConfig = new PdfRenderAutoConfiguration(properties);
        ReportService service = autoConfig.reportService();
        
        // Create test with chart
        Map<String, Double> data = new LinkedHashMap<>();
        data.put("测试A", 100.0);
        data.put("测试B", 150.0);
        
        ChartConfig config = new ChartConfig();
        config.setXAxisLabel("类别");
        config.setYAxisLabel("数值");
        
        ChartData chart = new ChartData("测试图表", "bar", data);
        chart.setConfig(config);
        
        ReportData reportData = ReportDataBuilder.create()
            .title("测试报告")
            .addSection(new Section("测试").addChart(chart))
            .build();
        
        byte[] pdfBytes = service.generatePdf(reportData, "flexible");
        
        assertNotNull(pdfBytes);
        // Font should be embedded since regularPath is set
        // But the effective font family will be from the default in FontProperties
        assertTrue(pdfBytes.length > 20000, 
            "PDF should be > 20KB. Actual: " + (pdfBytes.length / 1024) + " KB");
        
        Path pdfPath = Paths.get(TEST_OUTPUT_DIR, "springboot_no_defaultfamily.pdf");
        Files.write(pdfPath, pdfBytes);
        
        System.out.println("✓ PDF 已生成: " + pdfPath.toAbsolutePath());
        System.out.println("✓ PDF generated: " + pdfPath.toAbsolutePath());
        System.out.println("  文件大小: " + (pdfBytes.length / 1024) + " KB");
        System.out.println("  File size: " + (pdfBytes.length / 1024) + " KB");
        System.out.println();
        System.out.println("即使不显式设置 defaultFamily，regularPath 也会生效");
        System.out.println("Even without explicit defaultFamily, regularPath takes effect");
        System.out.println();
        
        System.out.println("========================================\n");
    }
}
