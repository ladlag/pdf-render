package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 完整验证PDF中文渲染的测试
 * 用于确认用户问题：生成的pdf所有的中文都是方框
 */
public class PdfChineseVerificationTest {

    private static final String TEST_OUTPUT_DIR = "test-output";

    @BeforeAll
    public static void setupTestOutputDirectory() throws IOException {
        Path outputDir = Paths.get(TEST_OUTPUT_DIR);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
    }

    @Test
    public void testPdfChineseRendering_WithFontConfig() throws IOException {
        System.out.println("\n========================================");
        System.out.println("测试：配置字体后的PDF中文渲染");
        System.out.println("========================================\n");
        
        ReportService service = new ReportService();
        
        // 配置中文字体
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        
        service.getHtmlRenderer().setFontConfig(fontConfig);
        
        // 创建包含中文的报告（文本 + 图表）
        Map<String, Double> chartData = new LinkedHashMap<>();
        chartData.put("北京", 100.0);
        chartData.put("上海", 150.0);
        chartData.put("广州", 120.0);
        chartData.put("深圳", 180.0);
        
        ChartConfig config = new ChartConfig();
        config.setXAxisLabel("城市");
        config.setYAxisLabel("销量（万）");
        config.setWidth(600);
        config.setHeight(400);
        
        ChartData chart = new ChartData("各城市销量对比", "bar", chartData);
        chart.setConfig(config);
        
        ReportData reportData = ReportDataBuilder.create()
            .title("中文PDF测试报告")
            .subtitle("验证PDF中所有中文都能正确显示")
            .reportDate("2024-12-31")
            .reportNumber("TEST-PDF-CN-001")
            .addSection(new Section("第一章：文本测试")
                .addParagraph("这是中文段落测试。如果您能看到这些文字而不是方框，说明PDF文本渲染正常。")
                .addParagraph("常用汉字：的一是在不了有和人这中大为上个国我以要他时来用们生到作。")
                .addParagraph("标点符号：，。！？、；：\"\"''《》（）【】…—"))
            .addSection(new Section("第二章：图表测试")
                .addParagraph("下面的图表应该显示中文标签：")
                .addChart(chart))
            .addSection(new Section("第三章：表格测试")
                .addTable(new TableData(
                    Arrays.asList("编号", "名称", "状态", "备注"),
                    Arrays.asList(
                        Arrays.asList("1", "项目一", "进行中", "优先级高"),
                        Arrays.asList("2", "项目二", "已完成", "已验收"),
                        Arrays.asList("3", "项目三", "计划中", "待启动")
                    )
                )))
            .build();
        
        // 生成PDF
        byte[] pdfBytes = service.generatePdf(reportData, "flexible");
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        // 保存PDF
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "pdf_chinese_with_font.pdf");
        Files.write(outputPath, pdfBytes);
        
        System.out.println("✓ PDF已生成: " + outputPath.toAbsolutePath());
        System.out.println("  文件大小: " + (pdfBytes.length / 1024) + " KB");
        System.out.println("\n请打开PDF文件验证：");
        System.out.println("  1. 标题、副标题、章节标题中的中文是否正确显示");
        System.out.println("  2. 段落文本中的中文是否正确显示");
        System.out.println("  3. 图表的坐标轴标签（城市、销量）是否正确显示");
        System.out.println("  4. 图表的数据标签（北京、上海等）是否正确显示");
        System.out.println("  5. 表格中的中文是否正确显示");
        System.out.println("\n✓ 如果以上全部正确，则问题已修复！");
    }
    
    @Test
    public void testPdfChineseRendering_WithoutFontConfig() throws IOException {
        System.out.println("\n========================================");
        System.out.println("测试：不配置字体的PDF中文渲染（对照组）");
        System.out.println("========================================\n");
        
        ReportService service = new ReportService();
        // 故意不配置字体
        
        // 创建相同的报告
        Map<String, Double> chartData = new LinkedHashMap<>();
        chartData.put("北京", 100.0);
        chartData.put("上海", 150.0);
        
        ChartConfig config = new ChartConfig();
        config.setXAxisLabel("城市");
        config.setYAxisLabel("销量");
        
        ChartData chart = new ChartData("销量对比", "bar", chartData);
        chart.setConfig(config);
        
        ReportData reportData = ReportDataBuilder.create()
            .title("中文PDF测试（无字体配置）")
            .addSection(new Section("测试章节")
                .addParagraph("这些中文应该显示为方框。")
                .addChart(chart))
            .build();
        
        byte[] pdfBytes = service.generatePdf(reportData, "flexible");
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "pdf_chinese_without_font.pdf");
        Files.write(outputPath, pdfBytes);
        
        System.out.println("✓ PDF已生成（无字体配置）: " + outputPath.toAbsolutePath());
        System.out.println("  文件大小: " + (pdfBytes.length / 1024) + " KB");
        System.out.println("\n预期结果：");
        System.out.println("  ✗ 所有中文字符显示为方框 (□)");
        System.out.println("  ✗ 图表标签也显示为方框");
        System.out.println("\n这个PDF用于对比，显示没有配置字体时的问题。");
    }
}
