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
 * Test case for matcher-report-1.0.html template.
 * This test generates a comprehensive PDF report for requirement review matching,
 * ensuring all content, tables, and charts are properly rendered.
 */
public class MatcherReportTest {

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
    public void testMatcherReportWithCompleteData() throws IOException {
        ReportService service = new ReportService();
        service.setUseHtmlPipeline(true);

        // Configure HarmonyOS Sans SC font for Chinese text display
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        ReportData reportData = createMatcherReportData();

        byte[] pdfBytes = service.generatePdf(reportData, "matcher-report-1.0");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Save to test-output directory for manual verification
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "matcher_report_complete.pdf");
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Matcher Report PDF generated: " + outputPath.toAbsolutePath());
        System.out.println("  Chinese characters rendered with HarmonyOS Sans SC font");
    }

    /**
     * Creates comprehensive test data for the matcher report template.
     * Based on the requirement review report content from the problem statement.
     */
    private ReportData createMatcherReportData() throws IOException {
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
        
        // Add summary table
        builder.summaryTable(createSummaryTable());

        // Add conclusion paragraph under section 3
        Section section3Conclusion = new Section("3.1 核心结论");
        section3Conclusion.addParagraph("本次预审针对《智能客户管理系统V2.0需求条目清单.xlsx》（20条有效需求）与《智能客户管理系统V2.0业务功能说明书.docx》（18个核心业务功能）进行语义匹配，核心结果如下：");
        section3Conclusion.addParagraph("整体匹配率80.0%，高优先级需求全部匹配（含精确/语义匹配），2条疑似匹配需人工复核，2条普通需求匹配失败（业务功能未覆盖）。");
        builder.addSection(section3Conclusion);

        // Add charts for visualization
        List<ChartData> charts = createCharts();
        for (ChartData chart : charts) {
            builder.addChart(chart);
        }
        
        // Set chart section title
        builder.chartsSectionTitle("图表");

        // ===== Section 4: Attachments =====
        
        Section section4 = new Section("4.1 附件清单");
        section4.addParagraph("• 附件1：Excel需求清单（去重后版本）.xlsx");
        section4.addParagraph("• 附件2：Word业务功能说明书（待更新版）.docx");
        section4.addParagraph("• 附件3：语义匹配详细得分表（含所有条目相似度数据）.xlsx");
        section4.addParagraph("• 附件4：疑似匹配复核表（空白版）.docx");
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
                "名称一致，需求描述与功能详情均包含密码登录、第三方登录场景"),
            Arrays.asList("C006", "客户信息录入", "3.5.6-客户信息管理", "0.95", 
                "核心场景一致，均支持批量导入和单个录入客户基础信息"),
            Arrays.asList("C007", "客户信息查询", "3.5.7-客户信息查询", "0.93", 
                "均支持姓名、手机号、公司等多条件组合查询，逻辑完全匹配"),
            Arrays.asList("C008", "客户信息修改", "3.5.11-客户信息维护", "0.91", 
                "\"修改\"与\"维护\"语义一致，均覆盖联系人、地址等编辑场景"),
            Arrays.asList("C009", "客户信息删除", "3.5.12-客户信息删除", "0.97", 
                "名称、描述完全一致，均支持逻辑/物理删除及批量操作"),
            Arrays.asList("C010", "客户标签管理", "3.5.10-客户标签管理", "0.96", 
                "均包含标签创建、编辑、删除及分类管理功能"),
            Arrays.asList("C011", "客户统计分析", "3.5.09-客户数据分析", "0.92", 
                "统计维度一致，均覆盖时间、地区、行业等核心维度"),
            Arrays.asList("C019", "系统参数配置", "3.5.16-系统参数设置", "0.90", 
                "\"配置\"与\"设置\"语义完全匹配，均覆盖汇率、税率等参数"),
            Arrays.asList("C003", "个人信息管理", "3.5.18-个人信息管理", "0.99", 
                "名称、功能完全一致，支持头像、昵称等信息查看与修改"),
            Arrays.asList("C004", "密码找回功能", "3.5.19-密码找回功能", "0.98", 
                "均支持手机号/邮箱重置密码，流程描述完全匹配")
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
                "\"数据统计分析\"与\"信息统计及分析\"语义高度一致，均支持多维度报表生成"),
            Arrays.asList("C013", "快速查询客户", "3.5.7-客户信息查询", "0.78", 
                "核心功能均为客户查询，需求强调\"快速\"（模糊查询+联想），功能包含该能力"),
            Arrays.asList("C014", "批量修改客户状态", "3.5.14-客户状态管理", "0.75", 
                "\"批量修改\"是\"状态管理\"的核心场景，功能详情明确支持批量操作"),
            Arrays.asList("C016", "订单状态跟踪", "3.5.14-订单信息批量操作", "0.73", 
                "\"状态跟踪\"属于\"订单信息操作\"的子场景，功能包含状态变更提醒能力"),
            Arrays.asList("C017", "数据导出功能", "3.5.15-数据导出与备份", "0.71", 
                "\"数据导出\"是\"导出与备份\"的核心功能，支持格式完全一致（Excel、PDF）"),
            Arrays.asList("C021", "订单详情查看", "3.5.15-订单信息查询", "0.76", 
                "\"详情查看\"是\"订单查询\"的核心输出，功能描述包含详情展示模块")
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
                "确认\"标签管理\"是否包含\"列表页快速编辑\"能力，需求强调\"快速\"，功能未明确说明"),
            Arrays.asList("C015", "订单状态批量修改", "3.5.14-订单信息批量操作", "0.68", 
                "确认\"批量操作\"是否覆盖\"状态修改\"场景，功能仅提及\"批量处理\"，未明确状态维度")
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
                "Word业务文档未涉及\"第三方集成\"相关功能，属于新增需求场景"),
            Arrays.asList("C020", "数据可视化报表导出", "0.38", 
                "仅匹配到\"数据导出\"功能（F015），但功能未包含\"可视化图表\"生成能力")
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
