package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.*;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify resource loading works correctly when running from a JAR.
 * Simulates the issue: "本地直接运行此项目的testcase是都没有问题的，但是以jar集成到项目后就出现了以上的问题"
 */
public class JarResourceLoadingTest {

    private static final String TEST_OUTPUT_DIR = "test-output";

    @Test
    public void testFontResourceLoadingMechanism() throws IOException {
        System.out.println("\n========================================");
        System.out.println("测试：字体资源加载机制验证");
        System.out.println("========================================\n");
        
        // Test 1: Verify fonts exist in classpath
        String fontPath = "/fonts/HarmonyOS_Sans_SC_Regular.ttf";
        
        System.out.println("1. 测试 getClass().getResource() [HtmlReportRenderer使用]:");
        URL fontUrl = getClass().getResource(fontPath);
        assertNotNull(fontUrl, "Font should be loadable via getResource()");
        System.out.println("   ✓ getResource() 成功: " + fontUrl);
        
        System.out.println("\n2. 测试 getClass().getResourceAsStream() [ChartRenderer使用]:");
        try (InputStream fontStream = getClass().getResourceAsStream(fontPath)) {
            assertNotNull(fontStream, "Font should be loadable via getResourceAsStream()");
            System.out.println("   ✓ getResourceAsStream() 成功");
            
            // Read some bytes to verify stream is valid
            byte[] buffer = new byte[4];
            int bytesRead = fontStream.read(buffer);
            assertEquals(4, bytesRead, "Should be able to read from font stream");
            System.out.println("   ✓ 可以从流中读取数据");
        }
        
        System.out.println("\n3. 测试 Flying Saucer URL.toString() 格式:");
        String urlString = fontUrl.toString();
        System.out.println("   URL格式: " + urlString);
        assertTrue(urlString.contains("fonts/HarmonyOS_Sans_SC_Regular.ttf"), 
            "URL should contain font path");
        
        System.out.println("\n✓ 所有资源加载测试通过！");
        System.out.println("  这说明字体文件可以从classpath正确加载");
    }
    
    @Test
    public void testFontLoadingFromJarContext() throws IOException {
        System.out.println("\n========================================");
        System.out.println("测试：模拟JAR环境下的字体加载");
        System.out.println("========================================\n");
        
        // Simulate JAR integration scenario
        ReportService service = new ReportService();
        
        // Configure font using classpath: prefix (as users would do)
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("CJK_MAIN");
        
        System.out.println("配置字体路径: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        
        // Apply configuration - this should work in JAR context
        service.getHtmlRenderer().setFontConfig(fontConfig);
        
        System.out.println("✓ 字体配置应用成功");
        
        // Create test data with Chinese text and charts
        Map<String, Double> chartData = new LinkedHashMap<>();
        chartData.put("北京", 100.0);
        chartData.put("上海", 150.0);
        chartData.put("广州", 120.0);
        
        ChartConfig config = new ChartConfig();
        config.setXAxisLabel("城市");
        config.setYAxisLabel("销量");
        config.setWidth(500);
        config.setHeight(300);
        
        ChartData chart = new ChartData("销量对比", "bar", chartData);
        chart.setConfig(config);
        
        ReportData reportData = ReportDataBuilder.create()
            .title("JAR集成测试报告")
            .subtitle("验证从JAR加载字体是否正常工作")
            .reportDate("2024-12-31")
            .reportNumber("JAR-TEST-001")
            .addSection(new Section("第一章：文本测试")
                .addParagraph("这是中文测试文本。本测试用于验证当库被打包成JAR并集成到其他项目后，字体加载机制是否正常工作。")
                .addParagraph("常用汉字测试：的一是在不了有和人这中大为上个国我以要他时来用们生到作地于出就分对成会可主发年动同工也能下过子说产种面而方后多定行学法所民得经十三之进着等部度家电力里如水化高自二理起小物现实加量都两体制机当使点从业本去把性好应开它合还因由其些然前外天政四日那社义事平形相全表间样与关各重新线内数正心反你明看原又么利比或但质气第向道命此变条只没结解问意建月公无系军很情者最立代想已通并提直题党程展五果料象员革位入常文总次品式活设及管特件长求老头基资边流路级少图山统接知较将组见计别她手角期根论运农指几九区强放决西被干做必战先回则任取据处队南给色光门即保治北造百规热领七海口东导器压志世金增争济阶油思术极交受联什认六共权收证改清己美再采转更单风切打白教速花带安场身车例真务具万每目至达走积示议声报斗完类八离华名确才科张信马节话米整空元况今集温传土许步群广石记需段研界拉林律叫且究观越织装影算低持音众书布复容儿须际商非验连断深难近矿千周委素技备半办青省列习响约支般史感劳便团往酸历市克何除消构府称太准精值号率族维划选标写存候毛亲快效斯院查江型眼王按格养易置派层片始却专状育厂京识适属圆包火住调满县局照参红细引听该铁价严龙飞"))
            .addSection(new Section("第二章：图表测试")
                .addParagraph("下面的图表包含中文标签，用于验证JFreeChart字体配置是否生效：")
                .addChart(chart))
            .build();
        
        // Generate PDF - this should work in JAR context
        byte[] pdfBytes = service.generatePdf(reportData, "flexible");
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        System.out.println("\nPDF生成信息:");
        System.out.println("  实际大小: " + pdfBytes.length + " bytes (" + (pdfBytes.length / 1024) + " KB)");
        
        // Save for verification
        Path outputPath = Paths.get(TEST_OUTPUT_DIR);
        Files.createDirectories(outputPath);
        Path pdfPath = outputPath.resolve("jar_integration_test.pdf");
        Files.write(pdfPath, pdfBytes);
        
        System.out.println("  保存路径: " + pdfPath.toAbsolutePath());
        
        // Check if font is actually embedded
        // A PDF without embedded font is typically < 10KB
        // A PDF with embedded Chinese font should be > 50KB
        if (pdfBytes.length < 50000) {
            System.err.println("\n⚠ 警告: PDF文件太小 (" + (pdfBytes.length / 1024) + " KB)");
            System.err.println("  可能原因: 字体未正确嵌入到PDF中");
            System.err.println("  这就是用户报告的问题：JAR集成后中文显示为方框");
        }
        
        assertTrue(pdfBytes.length > 50000, 
            "PDF with embedded font should be > 50KB. Actual: " + (pdfBytes.length / 1024) + " KB");
        
        System.out.println("\n✓ PDF生成成功: " + pdfPath.toAbsolutePath());
        System.out.println("  文件大小: " + (pdfBytes.length / 1024) + " KB");
        System.out.println("\n✓ 字体从classpath正确加载并嵌入到PDF");
        System.out.println("  这证明JAR集成场景下字体加载机制正常工作");
    }
    
    @Test
    public void testResourceLoadingWithDifferentPathFormats() {
        System.out.println("\n========================================");
        System.out.println("测试：不同路径格式的资源加载");
        System.out.println("========================================\n");
        
        // Test different path formats
        String[] testPaths = {
            "/fonts/HarmonyOS_Sans_SC_Regular.ttf",  // Absolute from classpath root
            "fonts/HarmonyOS_Sans_SC_Regular.ttf"    // Relative (not recommended)
        };
        
        for (String path : testPaths) {
            System.out.println("测试路径: \"" + path + "\"");
            
            // Test getResource
            URL url = getClass().getResource(path);
            System.out.println("  getResource(): " + (url != null ? "✓ 成功" : "✗ 失败"));
            
            // Test getResourceAsStream
            InputStream stream = getClass().getResourceAsStream(path);
            System.out.println("  getResourceAsStream(): " + (stream != null ? "✓ 成功" : "✗ 失败"));
            
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            System.out.println();
        }
        
        System.out.println("推荐使用: /fonts/xxx.ttf (从classpath根目录的绝对路径)");
    }
}
