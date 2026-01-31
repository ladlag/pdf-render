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
 * Test case to verify behavior of sections without numbered titles in matcher-report-final template.
 * Tests that sections without "1.", "2.", "3.", "4." prefixes are rendered at the end of Chapter 3.
 */
public class MatcherReportNoNumberTest {

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
    public void testSectionsWithoutNumberedTitles() throws IOException {
        ReportService service = new ReportService();
        
        // Enable HTML debug output
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory(TEST_OUTPUT_DIR);
        service.getHtmlRenderer().setDebugHtmlIncludeTimestamp(true);

        // Configure font
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("CJK_MAIN");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        ReportData reportData = createReportDataWithMixedTitles();

        byte[] pdfBytes = service.generatePdf(reportData, "matcher-report-final");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Save to test-output directory for manual verification
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "matcher_report_no_number_test.pdf");
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Test PDF generated: " + outputPath.toAbsolutePath());
        System.out.println("  This test verifies sections without numbered titles");
    }

    /**
     * Creates test data with mixed section titles:
     * - Numbered sections (1.x, 2.x, 3.x, 4.x)
     * - Non-numbered sections (should appear at end of Chapter 3)
     * - Sections with non-standard numbering (5.x, A.x)
     */
    private ReportData createReportDataWithMixedTitles() {
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("Section 标题测试报告")
            .reportDate("2024-12-31")
            .reportNumber("TEST-001");

        // ===== Chapter 1 Sections (with "1." prefix) =====
        Section section11 = new Section("1.1 第一章第一节");
        section11.addParagraph("这个 Section 标题以 '1.' 开头，会渲染在第一章。");
        section11.addTable(createSimpleTable("表格 1.1"));
        builder.addSection(section11);

        Section section12 = new Section("1.2 第一章第二节");
        section12.addParagraph("这个 Section 也以 '1.' 开头，同样在第一章。");
        builder.addSection(section12);

        // ===== Chapter 2 Sections (with "2." prefix) =====
        Section section21 = new Section("2.1 第二章第一节");
        section21.addParagraph("这个 Section 标题以 '2.' 开头，会渲染在第二章。");
        builder.addSection(section21);

        // ===== Chapter 3 Sections (with "3." prefix) =====
        Section summarySection = new Section("3. 汇总表");
        TableData summaryTable = createSummaryTable();
        summarySection.addTable(summaryTable);
        builder.addSection(summarySection);

        Section section31 = new Section("3.1 第三章核心内容");
        section31.addParagraph("这个 Section 标题以 '3.' 开头，会渲染在第三章主体部分。");
        builder.addSection(section31);

        // ===== Sections WITHOUT numbered titles (should go to end of Chapter 3) =====
        
        Section noNumberSection1 = new Section("附加信息");
        noNumberSection1.addParagraph("⚠️ 这个 Section 标题没有序号，会被渲染在第三章末尾。");
        noNumberSection1.addParagraph("渲染位置：在汇总表和 3.x Section 之后。");
        builder.addSection(noNumberSection1);

        Section noNumberSection2 = new Section("备注说明");
        noNumberSection2.addParagraph("⚠️ 这个 Section 也没有序号，同样在第三章末尾。");
        builder.addSection(noNumberSection2);

        Section wrongNumberSection = new Section("5.1 第五章内容");
        wrongNumberSection.addParagraph("⚠️ 这个 Section 标题以 '5.' 开头，但模板只识别 1-4，所以也会在第三章末尾。");
        builder.addSection(wrongNumberSection);

        Section letterNumberSection = new Section("A.1 附录内容");
        letterNumberSection.addParagraph("⚠️ 这个 Section 标题以 'A.' 开头，不会被识别，也在第三章末尾。");
        builder.addSection(letterNumberSection);

        Section chineseNumberSection = new Section("一、中文数字章节");
        chineseNumberSection.addParagraph("⚠️ 这个 Section 使用中文数字，不会被识别，也在第三章末尾。");
        builder.addSection(chineseNumberSection);

        // ===== Chapter 4 Sections (with "4." prefix) =====
        Section section4 = new Section("4.1 报告说明");
        section4.addParagraph("这个 Section 标题以 '4.' 开头，会渲染在第四章。");
        section4.addParagraph("报告编号：TEST-001");
        builder.addSection(section4);

        return builder.build();
    }

    private TableData createSimpleTable(String title) {
        List<String> headers = Arrays.asList("项目", "说明");
        List<List<String>> rows = Arrays.asList(
            Arrays.asList(title, "这是一个测试表格")
        );
        return new TableData(headers, rows);
    }

    private TableData createSummaryTable() {
        List<String> headers = Arrays.asList("章节", "Section 数量", "说明");
        List<List<String>> rows = Arrays.asList(
            Arrays.asList("第一章", "2", "标题以 '1.' 开头"),
            Arrays.asList("第二章", "1", "标题以 '2.' 开头"),
            Arrays.asList("第三章", "1", "标题以 '3.' 开头"),
            Arrays.asList("第三章末尾", "5", "没有序号或序号不是 1-4"),
            Arrays.asList("第四章", "1", "标题以 '4.' 开头")
        );
        return new TableData(headers, rows);
    }
}
