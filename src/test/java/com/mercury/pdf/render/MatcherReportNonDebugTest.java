package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test case for matcher-report-final.html template WITHOUT debug mode.
 * This test verifies that tables and charts are rendered correctly in production mode
 * (when debug HTML output is disabled).
 */
public class MatcherReportNonDebugTest {

    private static final String TEST_OUTPUT_DIR = "test-output";

    @BeforeAll
    public static void setupTestOutputDirectory() throws IOException {
        Path outputDir = Paths.get(TEST_OUTPUT_DIR);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        System.out.println("Test PDFs will be saved to: " + outputDir.toAbsolutePath());
    }

    @Test
    public void testMatcherReportFinalWithoutDebugMode() throws IOException {
        ReportService service = new ReportService();
        
        // IMPORTANT: Debug mode is DISABLED (production mode)
        service.getHtmlRenderer().setDebugHtmlEnabled(false);

        // Configure HarmonyOS Sans SC font for Chinese text display
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        ReportData reportData = createMatcherReportFinalData();

        byte[] pdfBytes = service.generatePdf(reportData, "matcher-report-final");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Save to test-output directory for manual verification
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "matcher_report_final_no_debug.pdf");
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Matcher Report Final PDF generated (NO DEBUG MODE): " + outputPath.toAbsolutePath());
        System.out.println("  This PDF should contain all tables and charts in Section 3 (匹配汇总)");
        System.out.println("  Chinese characters rendered with HarmonyOS Sans SC font");
    }

    /**
     * Creates comprehensive test data for the matcher report final template.
     * Based on the requirement review report content from the problem statement.
     */
    private ReportData createMatcherReportFinalData() throws IOException {
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("需求预审报告")
            .reportDate("2024-12-31")
            .reportNumber("AI-PRE-2024-001");

        // ===== Section 1: Match Results (1.1 - 1.4) =====
        
        // 1.1 精确匹配通过（匹配度≥0.90）
        Section section11 = new Section("1.1 精确匹配通过（匹配度≥0.90）");
        section11.addTable(createExactMatchTable());
        builder.addSection(section11);

        // 1.2 语义匹配通过（0.70≤匹配度＜0.90）
        Section section12 = new Section("1.2 语义匹配通过（0.70≤匹配度＜0.90）");
        section12.addTable(createSemanticMatchTable());
        builder.addSection(section12);

        // 1.3 疑似匹配（需人工复核，0.50≤匹配度＜0.70）
        Section section13 = new Section("1.3 疑似匹配（需人工复核，0.50≤匹配度＜0.70）");
        section13.addTable(createSuspectedMatchTable());
        builder.addSection(section13);

        // 1.4 匹配失败（匹配度＜0.50）
        Section section14 = new Section("1.4 匹配失败（匹配度＜0.50）");
        section14.addTable(createFailedMatchTable());
        builder.addSection(section14);

        // ===== Section 2: Detailed Analysis (2.1 - 2.3) =====
        
        // 2.1 分模块匹配表现
        Section section21 = new Section("2.1 分模块匹配表现");
        section21.addParagraph("按业务模块拆分匹配结果，用户管理、客户管理模块匹配表现优异，支付对接、数据统计模块存在功能覆盖不足问题：");
        builder.addSection(section21);

        // 2.2 匹配失败问题根源
        Section section22 = new Section("2.2 匹配失败问题根源");
        section22.addParagraph("• 需求与文档不同步：匹配失败的2条需求（C018、C020）均为V2.0新增场景，产品经理在收集需求后未同步更新Word业务功能说明书，导致文档滞后于需求。");
        section22.addParagraph("• 功能定义不细致：现有业务功能描述偏通用（如\"数据导出\"未明确是否含可视化），未覆盖\"快速操作\"\"批量精准修改\"等细化场景，导致疑似匹配产生。");
        builder.addSection(section22);

        // 2.3 文档规范性问题
        Section section23 = new Section("2.3 文档规范性问题");
        section23.addParagraph("预审中发现Excel需求清单存在3条重复条目（已预处理删除），Word功能编号存在重叠（如订单管理与客户管理均有\"F005\"编号），影响匹配效率与准确性。");
        builder.addSection(section23);

        // ===== Section 3: Summary Results =====
        
        // Add summary table in a section
        Section summarySection = new Section("3. 匹配汇总");
        TableData summaryTable = createSummaryTable();
        summarySection.addTable(summaryTable);
        builder.addSection(summarySection);

        // Add conclusion paragraph under section 3
        Section section3Conclusion = new Section("3.1 核心结论");
        section3Conclusion.addParagraph("本次预审针对《智能客户管理系统V2.0需求条目清单.xlsx》（20条有效需求）与《智能客户管理系统V2.0业务功能说明书.docx》（18个核心业务功能）进行语义匹配，核心结果如下：");
        section3Conclusion.addParagraph("整体匹配率80.0%，高优先级需求全部匹配（含精确/语义匹配），2条疑似匹配需人工复核，2条普通需求匹配失败（业务功能未覆盖）。");
        builder.addSection(section3Conclusion);

        // Add charts in a section with custom title
        Section chartSection = new Section("图表");
        List<ChartData> charts = createCharts();
        for (ChartData chart : charts) {
            chartSection.addChart(chart);
        }
        builder.addSection(chartSection);

        // ===== Section 4: Attachments =====
        
        Section section4 = new Section("4.1 报告说明");
        section4.addParagraph("报告说明：文档部分内容由 业技融合共创平台- BA助手 生成");
        section4.addParagraph("报告编号：AI-PRE-2024-001");
        section4.addParagraph("报告日期：2024年12月31日");
        builder.addSection(section4);

        return builder.build();
    }

    /**
     * Creates exact match table (matching degree ≥ 0.90)
     */
    private TableData createExactMatchTable() {
        List<String> headers = Arrays.asList(
            "需求编号", "需求名称", "匹配的Word功能（编号/名称）", "匹配度", "匹配说明"
        );

        List<List<String>> rows = Arrays.asList(
            Arrays.asList("C001", "用户注册功能", "3.5.1-用户注册功能", "1.00", 
                "需求名称、描述完全一致，含手机号/邮箱注册、验证码验证等核心场景"),
            Arrays.asList("C002", "用户登录功能", "3.5.2-用户登录功能", "0.98", 
                "名称一致，需求描述与功能详情均包含密码登录、第三方登录场景")
        );

        return new TableData(headers, rows);
    }

    /**
     * Creates semantic match table (0.70 ≤ matching degree < 0.90)
     */
    private TableData createSemanticMatchTable() {
        List<String> headers = Arrays.asList(
            "需求编号", "需求名称", "匹配的Word功能（编号/名称）", "匹配度", "匹配说明"
        );

        List<List<String>> rows = Arrays.asList(
            Arrays.asList("C005", "订单数据统计分析", "3.5.5-订单信息统计及分析", "0.82", 
                "\"数据统计分析\"与\"信息统计及分析\"语义高度一致，均支持多维度报表生成")
        );

        return new TableData(headers, rows);
    }

    /**
     * Creates suspected match table (0.50 ≤ matching degree < 0.70)
     */
    private TableData createSuspectedMatchTable() {
        List<String> headers = Arrays.asList(
            "需求编号", "需求名称", "疑似匹配的Word功能（编号/名称）", "匹配度", "复核重点"
        );

        List<List<String>> rows = Arrays.asList(
            Arrays.asList("C012", "客户标签快速编辑", "3.5.12-客户标签管理", "0.65", 
                "确认\"标签管理\"是否包含\"列表页快速编辑\"能力，需求强调\"快速\"，功能未明确说明")
        );

        return new TableData(headers, rows);
    }

    /**
     * Creates failed match table (matching degree < 0.50)
     */
    private TableData createFailedMatchTable() {
        List<String> headers = Arrays.asList(
            "需求编号", "需求名称", "匹配度（最高相似度）", "失败原因"
        );

        List<List<String>> rows = Arrays.asList(
            Arrays.asList("C018", "第三方支付接口对接", "0.42", 
                "Word业务文档未涉及\"第三方集成\"相关功能，属于新增需求场景")
        );

        return new TableData(headers, rows);
    }

    /**
     * Creates summary table for Section 3
     */
    private TableData createSummaryTable() {
        List<String> headers = Arrays.asList(
            "匹配状态", "数量（条）", "占比", "高优先级需求数量（条）", "高优先级匹配率"
        );

        List<List<String>> rows = Arrays.asList(
            Arrays.asList("精确匹配通过", "10", "50.0%", "4", "100%"),
            Arrays.asList("语义匹配通过", "6", "30.0%", "3", "100%"),
            Arrays.asList("疑似匹配（需复核）", "2", "10.0%", "1", "—"),
            Arrays.asList("匹配失败", "2", "10.0%", "0", "—"),
            Arrays.asList("合计", "20", "100.0%", "8", "100%")
        );

        return new TableData(headers, rows);
    }

    /**
     * Creates charts for data visualization
     */
    private List<ChartData> createCharts() throws IOException {
        List<ChartData> charts = new ArrayList<>();
        ChartRenderer renderer = new ChartRenderer();

        // Chart 1: Matching status distribution (Pie chart)
        Map<String, Double> pieData = new LinkedHashMap<>();
        pieData.put("精确匹配通过", 50.0);
        pieData.put("语义匹配通过", 30.0);
        pieData.put("疑似匹配", 10.0);
        pieData.put("匹配失败", 10.0);
        ChartData pieChart = new ChartData("匹配状态分布", "pie", pieData);
        
        // Generate and set base64 image (data URI format)
        String pieBase64 = renderer.generateChartAsDataUri(pieChart);
        pieChart.setBase64Image(pieBase64);
        charts.add(pieChart);

        // Chart 2: Requirement counts by status (Bar chart)
        Map<String, Double> barData = new LinkedHashMap<>();
        barData.put("精确匹配", 10.0);
        barData.put("语义匹配", 6.0);
        barData.put("疑似匹配", 2.0);
        barData.put("匹配失败", 2.0);
        ChartData barChart = new ChartData("各状态需求数量", "bar", barData);
        
        // Generate and set base64 image (data URI format)
        String barBase64 = renderer.generateChartAsDataUri(barChart);
        barChart.setBase64Image(barBase64);
        charts.add(barChart);

        return charts;
    }
}
