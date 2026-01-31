package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestWrongFont {
    public static void main(String[] args) throws IOException {
        ReportService service = new ReportService();
        
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory("test-output");

        // Configure with WRONG font family name (should be auto-corrected)
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("WrongFontName, DejaVu Sans, Arial, sans-serif");
        System.out.println("BEFORE setFontConfig: " + fontConfig.getDefaultFontFamily());
        
        service.getHtmlRenderer().setFontConfig(fontConfig);
        
        System.out.println("AFTER setFontConfig: " + fontConfig.getDefaultFontFamily());

        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("测试中文字体");
        
        Section section = new Section("1.1 测试");
        section.addParagraph("这是中文测试。");
        builder.addSection(section);

        byte[] pdfBytes = service.generatePdf(builder.build(), "matcher-report-final");
        
        Files.write(Paths.get("test-output/wrong_font_test.pdf"), pdfBytes);
        System.out.println("✓ Test PDF generated");
    }
}
