package com.finos.matcher.report;

import com.finos.matcher.report.config.FontConfig;
import com.finos.matcher.report.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Debug test for Chinese font rendering
 */
public class ChineseFontDebugTest {

    private static final String TEST_OUTPUT_DIR = "test-output";

    @BeforeAll
    public static void setupTestOutputDirectory() throws IOException {
        Path outputDir = Paths.get(TEST_OUTPUT_DIR);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
    }

    @Test
    public void testWithNotoSansCJK() throws IOException {
        ReportService service = new ReportService();
        service.setUseHtmlPipeline(true);

        // Configure with the downloaded Noto Sans CJK font
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/NotoSansCJKsc-Regular.otf");
        fontConfig.setDefaultFontFamily("Noto Sans CJK SC, DejaVu Sans, sans-serif");

        service.getHtmlRenderer().setFontConfig(fontConfig);

        ReportData reportData = createSimpleChineseReport();

        byte[] pdfBytes = service.generatePdf(reportData, "flexible");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "chinese_noto_sans.pdf");
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ PDF with Noto Sans CJK generated: " + outputPath.toAbsolutePath());
    }

    private ReportData createSimpleChineseReport() {
        return ReportDataBuilder.create()
            .title("中文测试 Chinese Test")
            .subtitle("测试中文字体 Testing Chinese Fonts")
            .reportDate("2024-12-31")
            
            .addSection(new Section("测试章节 Test Section")
                .addParagraph("这是中文段落。This is Chinese paragraph.")
                .addParagraph("常用汉字：的一是在不了有和人这中大为上个国"))
            
            .build();
    }
}
