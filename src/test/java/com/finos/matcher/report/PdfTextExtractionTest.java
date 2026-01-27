package com.finos.matcher.report;

import com.finos.matcher.report.config.FontConfig;
import com.finos.matcher.report.model.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify Chinese characters are properly rendered in PDF
 */
public class PdfTextExtractionTest {

    @Test
    public void testChineseCharactersInPdf() throws Exception {
        // Generate PDF with Chinese text
        ReportService service = new ReportService();
        service.setUseHtmlPipeline(true);

        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/NotoSansCJKsc-Regular.otf");
        // Set the font family to match the font's internal name
        fontConfig.setDefaultFontFamily("Noto Sans CJK SC, DejaVu Sans, Arial, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        ReportData reportData = ReportDataBuilder.create()
            .title("中文测试报告")
            .subtitle("测试中文字体显示")
            .reportDate("2024-12-31")
            .addSection(new Section("第一章")
                .addParagraph("这是中文段落测试。"))
            .build();

        byte[] pdfBytes = service.generatePdf(reportData, "flexible");
        
        // Save PDF
        Files.write(Paths.get("test-output/chinese_verification.pdf"), pdfBytes);
        
        // Extract text from PDF using PDFBox
        PDDocument document = PDDocument.load(pdfBytes);
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();
        
        System.out.println("=== Extracted PDF Text ===");
        System.out.println(text);
        System.out.println("=== End of Text ===");
        
        // Verify Chinese characters are present
        assertTrue(text.contains("中文"), "PDF should contain '中文'");
        assertTrue(text.contains("测试"), "PDF should contain '测试'");
        assertTrue(text.contains("报告"), "PDF should contain '报告'");
        assertTrue(text.contains("第一章"), "PDF should contain '第一章'");
        assertTrue(text.contains("这是"), "PDF should contain '这是'");
        
        // Count Chinese characters
        long chineseCount = text.chars()
            .filter(c -> c >= 0x4E00 && c <= 0x9FFF)
            .count();
        
        System.out.println("\n✓ Chinese characters found: " + chineseCount);
        assertTrue(chineseCount > 10, "PDF should contain at least 10 Chinese characters, found: " + chineseCount);
    }
}
