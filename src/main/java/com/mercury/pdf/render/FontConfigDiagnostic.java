package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.config.PdfRenderProperties;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.Section;
import com.mercury.pdf.render.util.FontNameExtractor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 字体配置诊断工具 / Font Configuration Diagnostic Tool
 * 
 * 这个工具帮助诊断为什么PDF中文显示为方框（□）
 * This tool helps diagnose why Chinese characters appear as boxes (□) in PDF
 * 
 * 运行此工具将：
 * Running this tool will:
 * 1. 检查字体文件是否存在 / Check if font files exist
 * 2. 验证字体路径配置 / Validate font path configuration
 * 3. 测试字体提取和内部名称 / Test font extraction and internal name
 * 4. 生成测试PDF / Generate test PDF
 * 5. 提供修复建议 / Provide fix suggestions
 */
public class FontConfigDiagnostic {
    
    private static final String[] FONT_PATHS = {
        "classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf",
        "classpath:/fonts/NotoSansCJKsc-Regular.otf"
    };
    
    private static int errorCount = 0;
    private static int warningCount = 0;

    public static void main(String[] args) {
        printHeader();
        
        System.out.println("开始诊断... Starting diagnostic...\n");
        
        // Step 1: Check font files
        checkFontFiles();
        
        // Step 2: Test font extraction
        testFontExtraction();
        
        // Step 3: Test PDF generation
        testPdfGeneration();
        
        // Summary
        printSummary();
    }
    
    private static void printHeader() {
        System.out.println("╔══════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  字体配置诊断工具 Font Configuration Diagnostic Tool                ║");
        System.out.println("║  帮助您找出为什么PDF中文显示为方框 Help fix Chinese character boxes   ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════╝\n");
    }
    
    private static void checkFontFiles() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("步骤 1: 检查字体文件 Step 1: Check Font Files");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        boolean foundAnyFont = false;
        
        for (String fontPath : FONT_PATHS) {
            String resourcePath = fontPath.substring("classpath:".length());
            InputStream stream = FontConfigDiagnostic.class.getResourceAsStream(resourcePath);
            
            if (stream != null) {
                try {
                    stream.close();
                    System.out.println("✓ 找到字体 Font found: " + fontPath);
                    foundAnyFont = true;
                } catch (IOException e) {
                    // Ignore
                }
            } else {
                System.out.println("✗ 未找到字体 Font not found: " + fontPath);
                warningCount++;
            }
        }
        
        System.out.println();
        
        if (!foundAnyFont) {
            System.err.println("❌ 错误：没有找到任何中文字体文件！");
            System.err.println("❌ ERROR: No Chinese font files found!");
            System.err.println();
            System.err.println("解决方法 Fix:");
            System.err.println("  1. 将中文字体文件放入 src/main/resources/fonts/ 目录");
            System.err.println("     Put Chinese font files in src/main/resources/fonts/");
            System.err.println("  2. 推荐使用 HarmonyOS Sans SC 或 Noto Sans CJK SC");
            System.err.println("     Recommended: HarmonyOS Sans SC or Noto Sans CJK SC");
            System.err.println();
            errorCount++;
        } else {
            System.out.println("✓ 字体文件检查通过 Font files check passed\n");
        }
    }
    
    private static void testFontExtraction() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("步骤 2: 测试字体提取 Step 2: Test Font Extraction");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        String testFontPath = FONT_PATHS[0]; // Use first available font
        
        try {
            System.out.println("测试字体路径 Testing font path: " + testFontPath);
            
            // Test if we can extract the font
            String resourcePath = testFontPath.substring("classpath:".length());
            InputStream fontStream = FontConfigDiagnostic.class.getResourceAsStream(resourcePath);
            
            if (fontStream == null) {
                System.err.println("✗ 无法加载字体文件 Cannot load font file");
                errorCount++;
                return;
            }
            
            // Extract to temp file
            File tempFile = File.createTempFile("diagnostic-font-", ".ttf");
            tempFile.deleteOnExit();
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile)) {
                while ((bytesRead = fontStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            fontStream.close();
            
            System.out.println("✓ 字体提取成功 Font extracted successfully");
            System.out.println("  临时文件 Temp file: " + tempFile.getAbsolutePath());
            
            // Extract font internal name
            String internalName = FontNameExtractor.extractFontFamilyName(tempFile.getAbsolutePath());
            System.out.println("✓ 字体内部名称 Font internal name: " + internalName);
            System.out.println();
            System.out.println("⚠️  重要 IMPORTANT:");
            System.out.println("   在配置时必须使用这个内部名称！");
            System.out.println("   You MUST use this internal name in configuration!");
            System.out.println();
            System.out.println("   正确配置示例 Correct configuration example:");
            System.out.println("   fontConfig.setDefaultFontFamily(\"" + internalName + ", sans-serif\");");
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("✗ 字体提取失败 Font extraction failed: " + e.getMessage());
            e.printStackTrace();
            errorCount++;
        }
    }
    
    private static void testPdfGeneration() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("步骤 3: 测试PDF生成 Step 3: Test PDF Generation");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        try {
            // Test 1: Without font config
            System.out.println("测试 1: 不配置字体 Test 1: Without font configuration");
            ReportService service1 = new ReportService();
            ReportData data = createTestData();
            byte[] pdf1 = service1.generatePdf(data, "flexible");
            
            String file1 = "test-output/diagnostic_NO_font.pdf";
            Files.createDirectories(Paths.get("test-output"));
            Files.write(Paths.get(file1), pdf1);
            
            System.out.println("  文件生成 File generated: " + file1);
            System.out.println("  文件大小 File size: " + (pdf1.length / 1024) + " KB");
            System.out.println("  结果 Result: 中文应该显示为方框 Chinese should show as boxes (□)");
            System.out.println();
            
            // Test 2: With font config
            System.out.println("测试 2: 配置字体 Test 2: With font configuration");
            ReportService service2 = new ReportService();
            
            FontConfig fontConfig = new FontConfig();
            fontConfig.setRegularFontPath(FONT_PATHS[0]);
            fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");
            service2.getHtmlRenderer().setFontConfig(fontConfig);
            
            byte[] pdf2 = service2.generatePdf(data, "flexible");
            
            String file2 = "test-output/diagnostic_WITH_font.pdf";
            Files.write(Paths.get(file2), pdf2);
            
            System.out.println("  文件生成 File generated: " + file2);
            System.out.println("  文件大小 File size: " + (pdf2.length / 1024) + " KB");
            System.out.println("  结果 Result: 中文应该正常显示 Chinese should display correctly ✓");
            System.out.println();
            
            // Compare file sizes
            if (pdf2.length > pdf1.length * 3) {
                System.out.println("✓ 文件大小对比通过 File size comparison passed");
                System.out.println("  配置字体的PDF明显更大（包含字体数据）");
                System.out.println("  PDF with font is significantly larger (contains font data)");
            } else {
                System.err.println("⚠️  警告：配置字体的PDF不够大");
                System.err.println("⚠️  WARNING: PDF with font is not large enough");
                System.err.println("   这可能表示字体没有正确嵌入");
                System.err.println("   This may indicate font was not properly embedded");
                warningCount++;
            }
            System.out.println();
            
            System.out.println("请打开两个PDF文件进行对比：");
            System.out.println("Please open both PDF files to compare:");
            System.out.println("  1. " + file1 + " - 无字体配置 No font config");
            System.out.println("  2. " + file2 + " - 有字体配置 With font config");
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("✗ PDF生成失败 PDF generation failed: " + e.getMessage());
            e.printStackTrace();
            errorCount++;
        }
    }
    
    private static ReportData createTestData() {
        ReportData data = new ReportData();
        data.setTitle("中文字体诊断测试 Chinese Font Diagnostic Test");
        data.setSubtitle("如果能看到这些中文，说明字体配置正确");
        data.setReportDate("2024-12-31");
        
        Section section = new Section("测试章节 Test Section");
        section.addParagraph("这是一段中文测试文本。如果您能正确看到这些汉字而不是方框（□），说明字体配置成功。");
        section.addParagraph("This is Chinese test text. If you can see these characters correctly instead of boxes (□), the font configuration is successful.");
        section.addParagraph("常用汉字测试：的一是在不了有和人这中大为上个国我以要他时来用们生到作地于出就分对成会可主发年动同工也能下过子说产种面而方后多定行学法所民得经十三之进着等部度家电力里如水化高自二理起小物现实加量都两体制机当使点从业本去把性好应开它合还因由其些然前外天政四日那社义事平形相全表间样与关各重新线内数正心反你明看原又么利比或但质气第向道命此变条只没结解问意建月公无系军很情者最立代想已通并提直题党程展五果料象员革位入常文总次品式活设及管特件长求老头基资边流路级少图山统接知较将组见计别她手角期根论运农指几九区强放决西被干做必战先回则任取据处队南给色光门即保治北造百规热领七海口东导器压志世金增争济阶油思术极交受联什认六共权收证改清己美再采转更单风切打白教速花带安场身车例真务具万每目至达走积示议声报斗完类八离华名确才科张信马节话米整空元况今集温传土许步群广石记需段研界拉林律叫且究观越织装影算低持音众书布复容儿须际商非验连断深难近矿千周委素技备半办青省列习响约支般史感劳便团往酸历市克何除消构府称太准精值号率族维划选标写存候毛亲快效斯院查江型眼王按格养易置派层片始却专状育厂京识适属圆包火住调满县局照参红细引听该铁价严");
        
        data.getSections().add(section);
        
        return data;
    }
    
    private static void printSummary() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("诊断总结 Diagnostic Summary");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        if (errorCount == 0 && warningCount == 0) {
            System.out.println("✓ 所有检查通过！All checks passed!");
            System.out.println("  您的字体配置应该是正确的。");
            System.out.println("  Your font configuration should be correct.");
            System.out.println();
            System.out.println("如果PDF中文仍然显示为方框，请检查：");
            System.out.println("If Chinese still shows as boxes in PDF, check:");
            System.out.println("  1. 是否真的调用了 setFontConfig()");
            System.out.println("     Did you really call setFontConfig()?");
            System.out.println("  2. Spring Boot项目是否使用了 @PostConstruct 覆盖配置");
            System.out.println("     Did @PostConstruct override the config in Spring Boot?");
            System.out.println("  3. font-family 名称是否与字体内部名称完全匹配");
            System.out.println("     Does font-family name exactly match font's internal name?");
        } else {
            System.err.println("发现 " + errorCount + " 个错误和 " + warningCount + " 个警告");
            System.err.println("Found " + errorCount + " error(s) and " + warningCount + " warning(s)");
            System.err.println();
            System.err.println("请根据上述诊断信息修复问题。");
            System.err.println("Please fix issues based on diagnostic information above.");
            System.err.println();
            System.err.println("需要帮助？查看文档 Need help? Check documentation:");
            System.err.println("  - CHINESE_QUICKSTART.md");
            System.err.println("  - FONT_CONFIGURATION.md");
            System.err.println("  - TROUBLESHOOTING.md");
        }
        
        System.out.println();
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}
