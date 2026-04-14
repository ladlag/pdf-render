package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.Section;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 最小化测试程序 - 验证字体配置是否正确
 * Minimal Test Program - Verify Font Configuration
 * 
 * 运行这个程序应该看到字体注册日志
 * Running this program should show font registration logs
 * 
 * 使用方法 Usage:
 * mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.MinimalFontTest"
 */
public class MinimalFontTest {
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║  最小化字体测试 Minimal Font Test                               ║");
        System.out.println("║  验证字体配置是否正确 Verify font configuration                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝\n");
        
        try {
            System.out.println("步骤 1/5: 创建 PdfRenderService");
            System.out.println("Step 1/5: Creating PdfRenderService");
            PdfRenderService service = new PdfRenderService();
            System.out.println("✓ PdfRenderService 创建成功\n");
            
            System.out.println("步骤 2/5: 配置 FontConfig");
            System.out.println("Step 2/5: Configuring FontConfig");
            FontConfig fontConfig = new FontConfig();
            fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
            fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");
            System.out.println("✓ FontConfig 配置完成");
            System.out.println("  字体路径 Font path: " + fontConfig.getRegularFontPath());
            System.out.println("  字体族 Font family: " + fontConfig.getDefaultFontFamily());
            System.out.println();
            
            System.out.println("步骤 3/5: 应用 FontConfig 到 PdfRenderService");
            System.out.println("Step 3/5: Applying FontConfig to PdfRenderService");
            service.getHtmlRenderer().setFontConfig(fontConfig);
            
            // 验证配置是否成功设置
            FontConfig retrieved = service.getHtmlRenderer().getFontConfig();
            if (retrieved != null && retrieved.getRegularFontPath() != null) {
                System.out.println("✓ FontConfig 已成功应用");
                System.out.println("  已验证配置存在 Configuration verified");
            } else {
                System.err.println("✗ 错误：FontConfig 未能正确应用！");
                System.err.println("✗ ERROR: FontConfig not properly applied!");
                return;
            }
            System.out.println();
            
            System.out.println("步骤 4/5: 创建测试数据");
            System.out.println("Step 4/5: Creating test data");
            ReportData data = createTestData();
            System.out.println("✓ 测试数据创建完成\n");
            
            System.out.println("步骤 5/5: 生成 PDF");
            System.out.println("Step 5/5: Generating PDF");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("⚠️  如果配置正确，你应该看到以下日志：");
            System.out.println("⚠️  If configured correctly, you should see these logs:");
            System.out.println("   ✓ Font registered with Flying Saucer: /tmp/pdf-render-font-xxx.ttf");
            System.out.println("     Encoding: Identity-H | Embedded: true");
            System.out.println("     Font family name (for CSS): HarmonyOS Sans SC");
            System.out.println("   ✓ Total fonts registered for PDF: 1");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            byte[] pdfBytes = service.generatePdf(data, "flexible");
            
            System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("✓ PDF 生成成功！PDF Generated Successfully!");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            // 保存PDF
            String outputPath = "test-output/minimal_font_test.pdf";
            Files.createDirectories(Paths.get("test-output"));
            Files.write(Paths.get(outputPath), pdfBytes);
            
            System.out.println("结果分析 Result Analysis:");
            System.out.println("  文件路径 File path: " + outputPath);
            System.out.println("  文件大小 File size: " + (pdfBytes.length / 1024) + " KB");
            
            if (pdfBytes.length > 50000) {
                System.out.println("  状态 Status: ✓ 字体已嵌入（文件较大）Font embedded (large file)");
                System.out.println();
                System.out.println("╔══════════════════════════════════════════════════════════════════╗");
                System.out.println("║  ✓✓✓ 成功！SUCCESS!                                            ║");
                System.out.println("║  字体配置正确，中文应该能正常显示                                ║");
                System.out.println("║  Font configuration correct, Chinese should display properly    ║");
                System.out.println("╚══════════════════════════════════════════════════════════════════╝");
            } else {
                System.out.println("  状态 Status: ✗ 字体未嵌入（文件过小）Font NOT embedded (file too small)");
                System.out.println();
                System.err.println("╔══════════════════════════════════════════════════════════════════╗");
                System.err.println("║  ✗✗✗ 警告！WARNING!                                            ║");
                System.err.println("║  字体似乎没有正确嵌入，请检查上面的日志                         ║");
                System.err.println("║  Font doesn't seem to be embedded, check logs above             ║");
                System.err.println("╚══════════════════════════════════════════════════════════════════╝");
                System.err.println();
                System.err.println("如果你没有看到字体注册日志，请查看：");
                System.err.println("If you didn't see font registration logs, check:");
                System.err.println("  - NO_FONT_LOGS_TROUBLESHOOTING.md");
            }
            
            System.out.println();
            System.out.println("请打开 PDF 文件验证中文显示：");
            System.out.println("Please open the PDF file to verify Chinese display:");
            System.out.println("  " + outputPath);
            
        } catch (Exception e) {
            System.err.println("\n✗ 错误 Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println();
            System.err.println("请查看故障排除文档 Please check troubleshooting docs:");
            System.err.println("  - NO_FONT_LOGS_TROUBLESHOOTING.md");
            System.err.println("  - CHINESE_QUICKSTART.md");
        }
    }
    
    /**
     * 创建简单的测试数据
     */
    private static ReportData createTestData() {
        ReportData data = new ReportData();
        data.setTitle("中文字体测试报告");
        data.setSubtitle("Chinese Font Test Report");
        data.setReportDate("2024-12-31");
        data.setReportNumber("TEST-001");
        
        Section section = new Section("测试章节 Test Section");
        section.addParagraph("这是一段中文测试文本。如果你能看到这些汉字而不是方框（□），说明字体配置成功！");
        section.addParagraph("This is Chinese test text. If you can see these characters instead of boxes (□), font configuration succeeded!");
        section.addParagraph("常用汉字：的一是在不了有和人这中大为上个国我以要他时来用们生到作地于出就分对成会可主发年动同工也能下过子说产种面而方后多定行学法所民得经十三之进着等部度家电力里如水化高自二理起小物现实加量都两体制机当使点从业本去把性好应开它合还因由其些然前外天政四日那社义事平形相全表间样与关各重新线内数正心反你明看原又么利比或但质气第向道命此变条只没结解问意建月公无系军很情者最立代想已通并提直题党程展五果料象员革位入常文总次品式活设及管特件长求老头基资边流路级少图山统接知较将组见计别她手角期根论运农指几九区强放决西被干做必战先回则任取据处队南给色光门即保治北造百规热领七海口东导器压志世金增争济阶油思术极交受联什认六共权收证改清己美再采转更单风切打白教速花带安场身车例真务具万每目至达走积示议声报斗完类八离华名确才科张信马节话米整空元况今集温传土许步群广石记需段研界拉林律叫且究观越织装影算低持音众书布复容儿须际商非验连断深难近矿千周委素技备半办青省列习响约支般史感劳便团往酸历市克何除消构府称太准精值号率族维划选标写存候毛亲快效斯院查江型眼王按格养易置派层片始却专状育厂京识适属圆包火住调满县局照参红细引听该铁价严");
        
        data.getSections().add(section);
        
        return data;
    }
}
