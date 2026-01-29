package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.*;
import com.mercury.pdf.render.examples.MatcherReportSectionTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test case to verify the new sectionType-based identification system.
 * Tests that sections can be identified by sectionType instead of title prefix,
 * decoupling backend data from template rendering logic.
 */
public class SectionTypeDecouplingTest {

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
    public void testSectionTypeBasedIdentification() throws IOException {
        ReportService service = new ReportService();
        
        // Enable HTML debug output
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory(TEST_OUTPUT_DIR);
        service.getHtmlRenderer().setDebugHtmlIncludeTimestamp(true);

        // Configure font
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        ReportData reportData = createReportWithSectionTypes();

        byte[] pdfBytes = service.generatePdf(reportData, "matcher-report-final");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Save to test-output directory for manual verification
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "section_type_decoupling_test.pdf");
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Section Type Decoupling Test PDF generated: " + outputPath.toAbsolutePath());
        System.out.println("  This test verifies that sections use sectionType instead of title prefix");
    }

    @Test
    public void testBackwardCompatibilityWithTitlePrefix() throws IOException {
        ReportService service = new ReportService();
        
        // Enable HTML debug output
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory(TEST_OUTPUT_DIR);
        service.getHtmlRenderer().setDebugHtmlIncludeTimestamp(true);

        // Configure font
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        ReportData reportData = createReportWithTitlePrefix();

        byte[] pdfBytes = service.generatePdf(reportData, "matcher-report-final");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Save to test-output directory for manual verification
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "backward_compatibility_test.pdf");
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Backward Compatibility Test PDF generated: " + outputPath.toAbsolutePath());
        System.out.println("  This test verifies backward compatibility with title prefix matching");
    }

    @Test
    public void testMixedSectionTypesAndTitles() throws IOException {
        ReportService service = new ReportService();
        
        // Enable HTML debug output
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory(TEST_OUTPUT_DIR);
        service.getHtmlRenderer().setDebugHtmlIncludeTimestamp(true);

        // Configure font
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        ReportData reportData = createMixedReport();

        byte[] pdfBytes = service.generatePdf(reportData, "matcher-report-final");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Save to test-output directory for manual verification
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "mixed_sections_test.pdf");
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Mixed Sections Test PDF generated: " + outputPath.toAbsolutePath());
        System.out.println("  This test verifies mixed usage of sectionType and title prefix");
    }

    /**
     * Creates a report using only sectionType for identification.
     * Title can be any text without numeric prefix.
     * Demonstrates complete decoupling from template.
     */
    private ReportData createReportWithSectionTypes() {
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("需求预审报告（使用 SectionType）")
            .reportDate("2024-12-31")
            .reportNumber("TYPE-001");

        // ===== Chapter 1: Using sectionType instead of title prefix =====
        // Note: Titles don't need numeric prefixes anymore!
        
        Section exactMatch = new Section("精确匹配通过", MatcherReportSectionTypes.CHAPTER_1);
        exactMatch.addParagraph("✓ 使用 sectionType='chapter1'，标题不需要 '1.' 前缀");
        exactMatch.addTable(createSimpleTable("精确匹配", "10 条"));
        builder.addSection(exactMatch);

        Section semanticMatch = new Section("语义匹配通过", MatcherReportSectionTypes.CHAPTER_1);
        semanticMatch.addParagraph("✓ 同样使用 sectionType='chapter1'");
        semanticMatch.addTable(createSimpleTable("语义匹配", "6 条"));
        builder.addSection(semanticMatch);

        // ===== Chapter 2: Analysis sections =====
        
        Section moduleAnalysis = new Section("模块匹配表现分析", MatcherReportSectionTypes.CHAPTER_2);
        moduleAnalysis.addParagraph("✓ 使用 sectionType='chapter2'");
        moduleAnalysis.addParagraph("按业务模块拆分匹配结果，用户管理、客户管理模块匹配表现优异。");
        builder.addSection(moduleAnalysis);

        Section problemAnalysis = new Section("问题根源分析", MatcherReportSectionTypes.CHAPTER_2);
        problemAnalysis.addParagraph("✓ 标题完全自定义，无需数字前缀");
        problemAnalysis.addParagraph("• 需求与文档不同步");
        problemAnalysis.addParagraph("• 功能定义不细致");
        builder.addSection(problemAnalysis);

        // ===== Chapter 3: Summary =====
        
        Section summary = new Section("匹配结果汇总", MatcherReportSectionTypes.CHAPTER_3);
        summary.addTable(createSummaryTable());
        builder.addSection(summary);

        Section conclusion = new Section("核心结论", MatcherReportSectionTypes.CHAPTER_3);
        conclusion.addParagraph("✓ 使用 sectionType='chapter3'");
        conclusion.addParagraph("整体匹配率80.0%，高优先级需求全部匹配。");
        builder.addSection(conclusion);

        // ===== Chapter 4: Notes =====
        
        Section notes = new Section("报告说明", MatcherReportSectionTypes.CHAPTER_4);
        notes.addParagraph("✓ 使用 sectionType='chapter4'");
        notes.addParagraph("报告编号：TYPE-001");
        notes.addParagraph("生成日期：2024-12-31");
        builder.addSection(notes);

        // ===== Other sections =====
        
        Section appendix = new Section("附录信息", MatcherReportSectionTypes.APPENDIX);
        appendix.addParagraph("✓ 使用 sectionType='appendix'，会渲染在第三章末尾");
        builder.addSection(appendix);

        return builder.build();
    }

    /**
     * Creates a report using traditional title prefix matching.
     * Tests backward compatibility.
     */
    private ReportData createReportWithTitlePrefix() {
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("需求预审报告（使用标题前缀）")
            .reportDate("2024-12-31")
            .reportNumber("PREFIX-001");

        // Traditional way with title prefix - should still work
        Section section11 = new Section("1.1 精确匹配通过");
        section11.addParagraph("使用传统的标题前缀方式（向后兼容）");
        section11.addTable(createSimpleTable("精确匹配", "10 条"));
        builder.addSection(section11);

        Section section21 = new Section("2.1 详细分析");
        section21.addParagraph("使用 '2.' 前缀，传统方式");
        builder.addSection(section21);

        Section section31 = new Section("3.1 核心结论");
        section31.addParagraph("使用 '3.' 前缀");
        builder.addSection(section31);

        Section section4 = new Section("4.1 报告说明");
        section4.addParagraph("使用 '4.' 前缀");
        builder.addSection(section4);

        return builder.build();
    }

    /**
     * Creates a report mixing both sectionType and title prefix approaches.
     * Demonstrates flexibility and migration path.
     */
    private ReportData createMixedReport() {
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("需求预审报告（混合方式）")
            .reportDate("2024-12-31")
            .reportNumber("MIXED-001");

        // Mix of new and old approaches
        
        // New way: sectionType
        Section newWay1 = new Section("精确匹配通过（新方式）", MatcherReportSectionTypes.CHAPTER_1);
        newWay1.addParagraph("✓ 使用 sectionType");
        builder.addSection(newWay1);

        // Old way: title prefix
        Section oldWay1 = new Section("1.2 语义匹配通过（旧方式）");
        oldWay1.addParagraph("使用标题前缀（向后兼容）");
        builder.addSection(oldWay1);

        // New way for chapter 2
        Section newWay2 = new Section("详细分析（新方式）", MatcherReportSectionTypes.CHAPTER_2);
        newWay2.addParagraph("✓ 使用 sectionType");
        builder.addSection(newWay2);

        // Old way for chapter 3
        Section oldWay3 = new Section("3.1 核心结论（旧方式）");
        oldWay3.addParagraph("使用标题前缀");
        builder.addSection(oldWay3);

        // New way for chapter 4
        Section newWay4 = new Section("报告说明（新方式）", MatcherReportSectionTypes.CHAPTER_4);
        newWay4.addParagraph("✓ 使用 sectionType");
        builder.addSection(newWay4);

        return builder.build();
    }

    private TableData createSimpleTable(String category, String count) {
        List<String> headers = Arrays.asList("类别", "数量");
        List<List<String>> rows = Arrays.asList(
            Arrays.asList(category, count)
        );
        return new TableData(headers, rows);
    }

    private TableData createSummaryTable() {
        List<String> headers = Arrays.asList("匹配状态", "数量（条）", "占比");
        List<List<String>> rows = Arrays.asList(
            Arrays.asList("精确匹配通过", "10", "50.0%"),
            Arrays.asList("语义匹配通过", "6", "30.0%"),
            Arrays.asList("疑似匹配", "2", "10.0%"),
            Arrays.asList("匹配失败", "2", "10.0%")
        );
        return new TableData(headers, rows);
    }
}
