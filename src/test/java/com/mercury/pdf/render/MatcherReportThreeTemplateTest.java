package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class MatcherReportThreeTemplateTest {

    private static final String TEST_OUTPUT_DIR = "test-output";
    private static final String DEBUG_HTML_DIR = "test-output/debug-html";

    @BeforeAll
    public static void setupTestOutputDirectory() throws IOException {
        Path outputDir = Paths.get(TEST_OUTPUT_DIR);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        Path debugDir = Paths.get(DEBUG_HTML_DIR);
        if (!Files.exists(debugDir)) {
            Files.createDirectories(debugDir);
        }
    }

    @Test
    public void testMatcherReportThreeUsesConfiguredFontFamily() throws IOException {
        ReportService service = new ReportService();

        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("CJK_MAIN");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory(DEBUG_HTML_DIR);
        service.getHtmlRenderer().setDebugHtmlIncludeTimestamp(false);

        ReportData reportData = createSimpleReportData();

        byte[] pdfBytes = service.generatePdf(reportData, "matcher-report-3.0");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        Path htmlPath = Paths.get(DEBUG_HTML_DIR, "matcher-report-3.0.html");
        assertTrue(Files.exists(htmlPath), "Debug HTML for matcher-report-3.0 should be created");

        String htmlContent = new String(Files.readAllBytes(htmlPath));
        assertTrue(htmlContent.contains("font-family: CJK_MAIN"),
            "Rendered HTML should use configured font-family");
        assertFalse(htmlContent.contains("[(${fontFamily})]"),
            "fontFamily placeholder should be resolved in matcher-report-3.0 template");
    }

    private ReportData createSimpleReportData() {
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("需求预审报告")
            .reportDate("2024-12-31")
            .reportNumber("TEST-003");

        Section section = new Section("1. 测试章节");
        section.addParagraph("这是 matcher-report-3.0 模板的字体测试内容。");
        builder.addSection(section);

        return builder.build();
    }
}
