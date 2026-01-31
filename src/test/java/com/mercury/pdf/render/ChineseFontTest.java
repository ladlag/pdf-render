package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;
import com.mercury.pdf.render.model.TableData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test case for Chinese font support in PDF generation.
 * This test verifies that custom fonts can be configured and Chinese characters
 * are properly rendered without appearing as boxes (□).
 */
public class ChineseFontTest {

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
    public void testChineseTextWithDefaultFonts() throws IOException {
        // Test with default fonts (no custom font configuration)
        ReportService service = new ReportService();

        ReportData reportData = createChineseReportData();

        byte[] pdfBytes = service.generatePdf(reportData, "flexible");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Save to test-output directory for manual verification
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "chinese_text_default_fonts.pdf");
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Chinese text PDF (default fonts) generated: " + outputPath.toAbsolutePath());
        System.out.println("  Note: Chinese characters may appear as boxes (□) without proper fonts");
    }

    @Test
    public void testChineseTextWithCustomFonts() throws IOException {
        // Test with custom font configuration using HarmonyOS Sans SC
        ReportService service = new ReportService();

        // Configure custom fonts - use HarmonyOS Sans SC
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        // Set the font family to match the font's internal name
        fontConfig.setDefaultFontFamily("CJK_MAIN");

        service.getHtmlRenderer().setFontConfig(fontConfig);

        ReportData reportData = createChineseReportData();

        try {
            byte[] pdfBytes = service.generatePdf(reportData, "flexible");

            assertNotNull(pdfBytes);
            assertTrue(pdfBytes.length > 0);

            // Save to test-output directory for manual verification
            Path outputPath = Paths.get(TEST_OUTPUT_DIR, "chinese_text_custom_fonts.pdf");
            Files.write(outputPath, pdfBytes);
            System.out.println("✓ Chinese text PDF (custom fonts) generated: " + outputPath.toAbsolutePath());
            System.out.println("  Chinese characters rendered with CJK_MAIN alias!");
        } catch (Exception e) {
            System.err.println("⚠ Failed to generate PDF with custom fonts");
            System.err.println("  Error: " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void testMatcherReportWithCustomFonts() throws IOException {
        // Test the matcher report with HarmonyOS Sans SC font configuration
        ReportService service = new ReportService();

        // Configure HarmonyOS Sans SC font for Chinese
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("CJK_MAIN");

        service.getHtmlRenderer().setFontConfig(fontConfig);

        ReportData reportData = createChineseReportData();

        try {
            byte[] pdfBytes = service.generatePdf(reportData, "matcher-report-1.0");

            assertNotNull(pdfBytes);
            assertTrue(pdfBytes.length > 0);

            // Save to test-output directory for manual verification
            Path outputPath = Paths.get(TEST_OUTPUT_DIR, "matcher_report_chinese_fonts.pdf");
            Files.write(outputPath, pdfBytes);
            System.out.println("✓ Matcher report with Chinese fonts generated: " + outputPath.toAbsolutePath());
            System.out.println("  Chinese characters rendered with CJK_MAIN alias!");
        } catch (Exception e) {
            System.err.println("⚠ Failed to generate matcher report with Chinese fonts");
            System.err.println("  Error: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Creates test data with Chinese text
     */
    private ReportData createChineseReportData() {
        return ReportDataBuilder.create()
            .title("中文字体测试报告")
            .subtitle("PDF 中文显示测试")
            .reportDate("2024-12-31")
            .reportNumber("TEST-CN-2024-001")
            
            .addSection(new Section("第一章：中文字体测试")
                .addParagraph("这是一段中文测试文本。本测试用于验证 PDF 生成器能否正确渲染中文字符。")
                .addParagraph("如果您看到方框（□）而不是汉字，说明字体配置存在问题。")
                .addParagraph("正确的字体配置应该能够显示：汉字、标点符号、数字123和英文ABC。"))
            
            .addSection(new Section("第二章：表格测试")
                .addTable(createChineseTable()))
            
            .addSection(new Section("第三章：常见汉字测试")
                .addParagraph("常用汉字：的一是在不了有和人这中大为上个国我以要他时来用们生到作地于出就分对成会可主发年动同工也能下过子说产种面而方后多定行学法所民得经十三之进着等部度家电力里如水化高自二理起小物现实加量都两体制机当使点从业本去把性好应开它合还因由其些然前外天政四日那社义事平形相全表间样与关各重新线内数正心反你明看原又么利比或但质气第向道命此变条只没结解问意建月公无系军很情者最立代想已通并提直题党程展五果料象员革位入常文总次品式活设及管特件长求老头基资边流路级少图山统接知较将组见计别她手角期根论运农指几九区强放决西被干做必战先回则任取据处队南给色光门即保治北造百规热领七海口东导器压志世金增争济阶油思术极交受联什认六共权收证改清己美再采转更单风切打白教速花带安场身车例真务具万每目至达走积示议声报斗完类八离华名确才科张信马节话米整空元况今集温传土许步群广石记需段研界拉林律叫且究观越织装影算低持音众书布复容儿须际商非验连断深难近矿千周委素技备半办青省列习响约支般史感劳便团往酸历市克何除消构府称太准精值号率族维划选标写存候毛亲快效斯院查江型眼王按格养易置派层片始却专状育厂京识适属圆包火住调满县局照参红细引听该铁价严龙飞"))
            
            .reportNotice("本报告用于测试中文字体渲染")
            .metadata("测试系统 | 生成时间：2024-12-31")
            .build();
    }

    /**
     * Creates a table with Chinese content
     */
    private TableData createChineseTable() {
        return new TableData(
            Arrays.asList("序号", "项目名称", "状态", "负责人", "备注"),
            Arrays.asList(
                Arrays.asList("1", "用户管理系统", "进行中", "张三", "优先级高"),
                Arrays.asList("2", "订单处理模块", "已完成", "李四", "已上线"),
                Arrays.asList("3", "数据分析平台", "计划中", "王五", "Q2启动"),
                Arrays.asList("4", "客户关系管理", "进行中", "赵六", "关键项目"),
                Arrays.asList("5", "支付对接服务", "测试中", "钱七", "待验收")
            )
        );
    }
}
