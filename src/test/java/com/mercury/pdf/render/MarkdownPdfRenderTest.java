package com.mercury.pdf.render;

import com.mercury.pdf.render.config.PdfRenderProperties;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;
import com.mercury.pdf.render.model.TableData;
import com.mercury.pdf.render.util.MarkdownRenderer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for markdown-to-HTML content rendering in PDF generation.
 * Validates GFM table support, allowHtml option, and end-to-end PDF output.
 */
public class MarkdownPdfRenderTest {

    private static final String TEST_OUTPUT_DIR = System.getProperty("test.output.dir", "test-output");
    private static final String DEBUG_HTML_DIR = "test-output/debug-html";

    @Test
    public void testGfmTableRendering() {
        String markdown = "| Name | Score |\n|------|-------|\n| Alice | 90 |\n| Bob | 85 |";
        String html = MarkdownRenderer.toHtml(markdown);

        assertNotNull(html);
        assertTrue(html.contains("<table>"), "Should contain table element");
        assertTrue(html.contains("<th>Name</th>"), "Should contain header");
        assertTrue(html.contains("<td>Alice</td>"), "Should contain cell data");
    }

    @Test
    public void testAllowHtmlPreservesRawHtml() {
        String markdown = "Hello <strong>world</strong>";
        String htmlAllowed = MarkdownRenderer.toHtml(markdown, true);
        String htmlEscaped = MarkdownRenderer.toHtml(markdown, false);

        assertTrue(htmlAllowed.contains("<strong>world</strong>"),
            "allowHtml=true should preserve raw HTML tags");
        assertTrue(htmlEscaped.contains("&lt;strong&gt;"),
            "allowHtml=false should escape HTML tags");
    }

    @Test
    public void testDefaultEscapesHtml() {
        String markdown = "Text <em>italic</em>";
        String html = MarkdownRenderer.toHtml(markdown);

        assertTrue(html.contains("&lt;em&gt;"), "Default should escape HTML");
        assertFalse(html.contains("<em>italic</em>"), "Should not contain raw HTML");
    }

    @Test
    public void testSectionAllowHtmlInMarkdown() {
        Section section = new Section("Test")
            .withMarkdownContent("Hello <b>bold</b>", true);

        assertTrue(section.isAllowHtmlInMarkdown());
        String html = section.getMarkdownHtml();
        assertNotNull(html);
        assertTrue(html.contains("<b>bold</b>"), "Should preserve raw HTML");
    }

    @Test
    public void testSectionDefaultEscapesMarkdownHtml() {
        Section section = new Section("Test")
            .withMarkdownContent("Hello <b>bold</b>");

        assertFalse(section.isAllowHtmlInMarkdown());
        String html = section.getMarkdownHtml();
        assertNotNull(html);
        assertTrue(html.contains("&lt;b&gt;"), "Default should escape HTML in markdown");
    }

    @Test
    public void testSectionMarkdownWithGfmTable() {
        String md = "## 数据表\n\n| 项目 | 值 |\n|------|----|\n| A | 100 |\n| B | 200 |";
        Section section = new Section("Table Section")
            .withMarkdownContent(md);

        String html = section.getMarkdownHtml();
        assertNotNull(html);
        assertTrue(html.contains("<table>"), "Should render GFM table");
        assertTrue(html.contains("<h2>数据表</h2>"), "Should render heading");
    }

    @Test
    public void testLargeMarkdownPdfGeneration() throws IOException {
        StringBuilder md = new StringBuilder();
        md.append("# 综合分析报告\n\n");
        md.append("## 一、项目概述\n\n");
        md.append("本报告旨在对项目进行全面分析，包括**关键指标**和*详细数据*。\n\n");
        md.append("## 二、数据分析\n\n");
        md.append("| 指标 | Q1 | Q2 | Q3 | Q4 |\n");
        md.append("|------|-----|-----|-----|-----|\n");
        md.append("| 收入 | 100 | 150 | 200 | 250 |\n");
        md.append("| 成本 | 80 | 90 | 100 | 110 |\n");
        md.append("| 利润 | 20 | 60 | 100 | 140 |\n\n");
        md.append("## 三、关键发现\n\n");
        md.append("1. 收入持续增长\n");
        md.append("2. 利润率不断提高\n");
        md.append("3. 成本控制良好\n\n");
        md.append("> 注意：以上数据仅供参考\n\n");
        md.append("## 四、代码示例\n\n");
        md.append("```\nfunction calculate(a, b) {\n  return a + b;\n}\n```\n\n");
        md.append("## 五、结论\n\n");
        md.append("项目整体运行良好，建议继续当前策略。\n");

        ReportData report = ReportDataBuilder.create()
            .title("Markdown分析报告")
            .subtitle("2024年度综合分析")
            .reportDate("2024-12-31")
            .addSection(new Section("报告内容")
                .withMarkdownContent(md.toString()))
            .build();

        ReportService service = new ReportService();
        service.getHtmlRenderer().setDefaultTemplateName("flexible");

        configureChineseFont(service);

        // Enable debug HTML output for troubleshooting
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory(DEBUG_HTML_DIR);
        service.getHtmlRenderer().setDebugHtmlIncludeTimestamp(true);

        byte[] pdfBytes = service.generatePdf(report);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "PDF should not be empty");

        // Verify that the CJK font is embedded with Identity-H encoding
        String pdfContent = new String(pdfBytes, "ISO-8859-1");
        assertTrue(pdfContent.contains("Identity-H"),
            "PDF must contain Identity-H encoding for CJK font support");

        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "markdown_large_content.pdf");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Large markdown PDF generated: " + outputPath.toAbsolutePath());

        // Verify debug HTML was generated (with timestamp in filename)
        Path debugDir = Paths.get(DEBUG_HTML_DIR);
        assertTrue(Files.exists(debugDir), "Debug HTML directory should exist");
        long htmlCount = Files.list(debugDir)
            .filter(f -> f.getFileName().toString().startsWith("flexible-") && f.getFileName().toString().endsWith(".html"))
            .count();
        assertTrue(htmlCount > 0, "Debug HTML file with timestamp should be created");
        System.out.println("✓ Debug HTML saved with timestamp to: " + debugDir.toAbsolutePath());
    }

    @Test
    public void testMarkdownWithEmbeddedHtmlPdf() throws IOException {
        StringBuilder md = new StringBuilder();
        md.append("# Report Title\n\n");
        md.append("Standard markdown paragraph.\n\n");
        md.append("<div style=\"background-color: #f0f0f0; padding: 10px;\">\n");
        md.append("<p>This is embedded HTML content.</p>\n");
        md.append("</div>\n\n");
        md.append("Back to **markdown**.\n");

        ReportData report = ReportDataBuilder.create()
            .title("HTML in Markdown Test")
            .addSection(new Section("Content")
                .withMarkdownContent(md.toString(), true))
            .build();

        ReportService service = new ReportService();
        service.getHtmlRenderer().setDefaultTemplateName("flexible");
        byte[] pdfBytes = service.generatePdf(report);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "PDF should not be empty");

        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "markdown_with_html.pdf");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Markdown with HTML PDF generated: " + outputPath.toAbsolutePath());
    }

    @Test
    public void testGfmTableInSection() {
        String md = "| A | B |\n|---|---|\n| 1 | 2 |";
        Section section = new Section("Table")
            .withMarkdownContent(md);

        String html = section.getMarkdownHtml();
        assertTrue(html.contains("<table>"));
        assertTrue(html.contains("<td>1</td>"));
    }

    /**
     * Demonstrates mixing traditional structured content (paragraphs, tables, charts)
     * with markdown content in the same report and even within the same section.
     * The flexible.html template renders all content types per section in order:
     * title → subtitle → paragraphs → tables → charts → customContent → markdownContent.
     */
    @Test
    public void testMixedTraditionalAndMarkdownContent() throws IOException {
        // Markdown content for the analysis body
        StringBuilder md = new StringBuilder();
        md.append("## 详细分析\n\n");
        md.append("根据以上数据，我们发现以下趋势：\n\n");
        md.append("| 季度 | 增长率 |\n");
        md.append("|------|--------|\n");
        md.append("| Q1 | 10% |\n");
        md.append("| Q2 | 15% |\n\n");
        md.append("> 备注：增长率基于同比数据\n\n");
        md.append("### 结论\n\n");
        md.append("整体表现**良好**，建议继续当前策略。\n");

        // Section 1: traditional structured data (paragraphs + table)
        TableData summaryTable = new TableData(
            Arrays.asList("指标", "值", "状态"),
            Arrays.asList(
                Arrays.asList("收入", "1000万", "达标"),
                Arrays.asList("利润", "200万", "超额")
            )
        );
        Section structuredSection = new Section("一、数据概览")
            .addParagraph("本章节使用传统结构化数据展示关键指标。")
            .addTable(summaryTable);

        // Section 2: markdown-only content
        Section markdownSection = new Section("二、分析报告")
            .withMarkdownContent(md.toString());

        // Section 3: mixed — traditional paragraph + markdown in the SAME section
        Section mixedSection = new Section("三、综合展示")
            .addParagraph("本章节同时包含传统段落和Markdown内容。")
            .addTable(new TableData(
                Arrays.asList("项目", "分数"),
                Arrays.asList(Arrays.asList("A", "95"), Arrays.asList("B", "88"))
            ))
            .withMarkdownContent("### 补充说明\n\n- 以上数据来自最新报告\n- 所有指标均已通过审核\n");

        ReportData report = ReportDataBuilder.create()
            .title("混合内容报告")
            .subtitle("传统用法与Markdown混排示例")
            .reportDate("2024-12-31")
            .reportNumber("MIX-001")
            .addSection(structuredSection)
            .addSection(markdownSection)
            .addSection(mixedSection)
            .build();

        ReportService service = new ReportService();
        service.getHtmlRenderer().setDefaultTemplateName("flexible");

        configureChineseFont(service);

        byte[] pdfBytes = service.generatePdf(report);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "PDF should not be empty");

        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "mixed_traditional_and_markdown.pdf");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Mixed traditional+markdown PDF generated: " + outputPath.toAbsolutePath());
    }

    /**
     * Configures Chinese font using PdfRenderProperties.FontProperties directly,
     * matching the same approach used by Spring Boot auto-configuration.
     * Uses the bundled HarmonyOS Sans SC font with Identity-H encoding for CJK support.
     */
    private void configureChineseFont(ReportService service) {
        PdfRenderProperties.FontProperties fontProps = new PdfRenderProperties.FontProperties();
        fontProps.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontProps.setCjkPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        service.getHtmlRenderer().setFontProperties(fontProps);
    }
}
