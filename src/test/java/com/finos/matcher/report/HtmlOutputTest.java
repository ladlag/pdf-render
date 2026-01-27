package com.finos.matcher.report;

import com.finos.matcher.report.config.FontConfig;
import com.finos.matcher.report.model.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HtmlOutputTest {
    @Test
    public void testHtmlOutput() throws Exception {
        ReportService service = new ReportService();
        service.setUseHtmlPipeline(true);

        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/NotoSansCJKsc-Regular.otf");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        ReportData reportData = ReportDataBuilder.create()
            .title("中文测试 Chinese Test")
            .subtitle("测试中文字体")
            .reportDate("2024-12-31")
            .addSection(new Section("测试章节")
                .addParagraph("这是中文段落。常用汉字：的一是在"))
            .build();

        // Get the HTML before PDF conversion
        java.lang.reflect.Method method = service.getHtmlRenderer().getClass()
            .getDeclaredMethod("prepareTemplateData", ReportData.class);
        method.setAccessible(true);
        java.util.Map<String, Object> data = (java.util.Map<String, Object>) method.invoke(
            service.getHtmlRenderer(), reportData);
        
        method = service.getHtmlRenderer().getClass()
            .getDeclaredMethod("renderHtml", java.util.Map.class, String.class);
        method.setAccessible(true);
        String html = (String) method.invoke(service.getHtmlRenderer(), data, "flexible");
        
        Files.write(Paths.get("test-output/debug.html"), html.getBytes("UTF-8"));
        System.out.println("HTML saved to test-output/debug.html");
        System.out.println("HTML length: " + html.length());
        System.out.println("Contains 中文: " + html.contains("中文"));
        System.out.println("Contains 测试: " + html.contains("测试"));
    }
}
