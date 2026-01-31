package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.Section;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Test to verify that defaultFontFamily setting is properly respected.
 * This addresses the issue where fonts were registered but Chinese still showed as boxes.
 */
public class DefaultFamilyTest {
    
    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║  Test: defaultFontFamily Configuration                          ║");
        System.out.println("║  Verifying that user-configured font family is respected        ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝\n");
        
        Files.createDirectories(Paths.get("test-output"));
        
        // Test 1: With defaultFontFamily set to font's internal name
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 1: With defaultFontFamily = \"HarmonyOS Sans SC\"");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        ReportService service1 = new ReportService();
        FontConfig config1 = new FontConfig();
        config1.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        config1.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");
        
        System.out.println("Configuration:");
        System.out.println("  regularFontPath: " + config1.getRegularFontPath());
        System.out.println("  defaultFontFamily: " + config1.getDefaultFontFamily());
        System.out.println();
        
        service1.getHtmlRenderer().setFontConfig(config1);
        
        ReportData data1 = createTestData("Test 1: 使用配置的字体族名");
        byte[] pdf1 = service1.generatePdf(data1, "flexible");
        
        String file1 = "test-output/test1_with_family.pdf";
        Files.write(Paths.get(file1), pdf1);
        System.out.println("\n✓ Generated: " + file1);
        System.out.println("  File size: " + (pdf1.length/1024) + " KB");
        System.out.println();
        
        // Test 2: Without defaultFontFamily (should use PDFFont alias)
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 2: Without defaultFontFamily (should use 'PDFFont' alias)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        ReportService service2 = new ReportService();
        FontConfig config2 = new FontConfig();
        config2.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        // NOT setting defaultFontFamily - should default to "PDFFont"
        
        System.out.println("Configuration:");
        System.out.println("  regularFontPath: " + config2.getRegularFontPath());
        System.out.println("  defaultFontFamily: (not set - will use 'PDFFont' alias)");
        System.out.println();
        
        service2.getHtmlRenderer().setFontConfig(config2);
        
        ReportData data2 = createTestData("Test 2: 使用默认字体别名");
        byte[] pdf2 = service2.generatePdf(data2, "flexible");
        
        String file2 = "test-output/test2_without_family.pdf";
        Files.write(Paths.get(file2), pdf2);
        System.out.println("\n✓ Generated: " + file2);
        System.out.println("  File size: " + (pdf2.length/1024) + " KB");
        System.out.println();
        
        // Verify both work
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("RESULTS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        boolean test1Success = pdf1.length > 50000;
        boolean test2Success = pdf2.length > 50000;
        
        System.out.println("Test 1 (with defaultFontFamily): " + 
            (test1Success ? "✓ PASS - Font embedded" : "✗ FAIL - Font not embedded"));
        System.out.println("Test 2 (without defaultFontFamily): " + 
            (test2Success ? "✓ PASS - Font embedded" : "✗ FAIL - Font not embedded"));
        System.out.println();
        
        if (test1Success && test2Success) {
            System.out.println("╔══════════════════════════════════════════════════════════════════╗");
            System.out.println("║  ✓✓✓ ALL TESTS PASSED!                                         ║");
            System.out.println("║  Both configurations work correctly.                            ║");
            System.out.println("║  Chinese characters should display in both PDFs.                ║");
            System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        } else {
            System.err.println("╔══════════════════════════════════════════════════════════════════╗");
            System.err.println("║  ✗✗✗ TEST FAILURE                                              ║");
            System.err.println("║  One or more tests failed. Check logs above.                    ║");
            System.err.println("╚══════════════════════════════════════════════════════════════════╝");
            System.exit(1);
        }
        
        System.out.println("\nPlease verify Chinese characters display correctly in both files:");
        System.out.println("  " + file1);
        System.out.println("  " + file2);
    }
    
    private static ReportData createTestData(String title) {
        ReportData data = new ReportData();
        data.setTitle(title);
        data.setSubtitle("Subtitle 副标题");
        data.setReportDate("2024-01-31");
        
        Section section = new Section("测试章节 Test Section");
        section.addParagraph("这是中文测试文本。如果能看到这些汉字而不是方框（□），说明字体配置正确。");
        section.addParagraph("This is a test paragraph. If Chinese characters above display correctly (not as boxes), the font configuration works!");
        section.addParagraph("常用汉字：的一是在不了有和人这中大为上个国我以要他时来用们生到作地于出就分对成会可主发年动同工也能下过子说产种面而方后多定行学法所民得经十三之进着等部度家电力里如水化高自二理起小物现实加量都两体制机当使点从业本去把性好应开它合还因由其些然前外天政四日那社义事平形相全表间样与关各重新线内数正心反你明看原又么利比或但质气第向道命此变条只没结解问意建月公无系军很情者最立代想已通并提直题党程展五果料象员革位入常文总次品式活设及管特件长求老头基资边流路级少图山统接知较将组见计别她手角期根论运农指几九区强放决西被干做必战先回则任取据处队南给色光门即保治北造百规热领七海口东导器压志世金增争济阶油思术极交受联什认六共权收证改清己美再采转更单风切打白教速花带安场身车例真务具万每目至达走积示议声报斗完类八离华名确才科张信马节话米整空元况今集温传土许步群广石记需段研界拉林律叫且究观越织装影算低持音众书布复容儿须际商非验连断深难近");
        
        data.getSections().add(section);
        
        return data;
    }
}
