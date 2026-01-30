package com.mercury.pdf.render;

import com.mercury.pdf.render.util.FontNameExtractor;
import java.io.File;
import java.io.InputStream;

/**
 * 字体文件验证工具 Font File Verifier
 * 
 * 检查项目中的字体文件，显示每个字体的：
 * - 文件大小
 * - 内部名称
 * - 建议的配置
 * 
 * Check font files in project and show:
 * - File size
 * - Internal name
 * - Recommended configuration
 * 
 * 运行 Usage:
 * mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.FontFileVerifier"
 */
public class FontFileVerifier {
    
    private static final String[] FONT_PATHS = {
        "classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf",
        "classpath:/fonts/NotoSansCJKsc-Regular.otf"
    };
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  字体文件验证工具 Font File Verifier                              ║");
        System.out.println("║  检查项目中的中文字体 Check Chinese fonts in project              ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝\n");
        
        boolean foundAny = false;
        
        for (String fontPath : FONT_PATHS) {
            if (checkFont(fontPath)) {
                foundAny = true;
            }
            System.out.println();
        }
        
        if (!foundAny) {
            System.err.println("❌ 没有找到任何中文字体文件！");
            System.err.println("❌ No Chinese font files found!");
            System.err.println();
            System.err.println("请将字体文件放入 src/main/resources/fonts/ 目录");
            System.err.println("Please put font files in src/main/resources/fonts/");
        }
        
        System.out.println("═══════════════════════════════════════════════════════════════════");
        System.out.println("如何使用 How to Use");
        System.out.println("═══════════════════════════════════════════════════════════════════");
        System.out.println();
        System.out.println("根据上面显示的字体信息，选择一个字体并配置：");
        System.out.println("Based on font info above, choose a font and configure:");
        System.out.println();
        System.out.println("纯Java Pure Java:");
        System.out.println("  FontConfig fontConfig = new FontConfig();");
        System.out.println("  fontConfig.setRegularFontPath(\"[上面显示的路径 path shown above]\");");
        System.out.println("  fontConfig.setDefaultFontFamily(\"[上面显示的内部名称 internal name shown above]\");");
        System.out.println("  service.getHtmlRenderer().setFontConfig(fontConfig);");
        System.out.println();
        System.out.println("Spring Boot:");
        System.out.println("  pdf-render:");
        System.out.println("    fonts:");
        System.out.println("      regular-path: [上面显示的路径 path shown above]");
        System.out.println("      default-family: [上面显示的内部名称 internal name shown above]");
        System.out.println();
    }
    
    private static boolean checkFont(String fontPath) {
        String fontLabel = extractFontLabel(fontPath);
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("字体 Font: " + fontLabel);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        try {
            String resourcePath = fontPath.substring("classpath:".length());
            
            // 1. 检查字体是否存在
            InputStream stream = FontFileVerifier.class.getResourceAsStream(resourcePath);
            if (stream == null) {
                System.out.println("✗ 字体文件不存在 Font file not found");
                System.out.println("  路径 Path: " + fontPath);
                return false;
            }
            
            System.out.println("✓ 字体文件存在 Font file exists");
            System.out.println("  路径 Path: " + fontPath);
            
            // 2. 提取字体到临时文件并检查大小
            File tempFile = File.createTempFile("verify-font-", getExtension(fontPath));
            tempFile.deleteOnExit();
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile)) {
                while ((bytesRead = stream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            stream.close();
            
            long actualSize = tempFile.length();
            double actualSizeMB = actualSize / (1024.0 * 1024.0);
            System.out.printf("  文件大小 File size: %.2f MB (%.0f bytes)\n", actualSizeMB, (double)actualSize);
            
            // 3. 提取字体内部名称
            String internalName = FontNameExtractor.extractFontFamilyName(tempFile.getAbsolutePath());
            System.out.println("  字体内部名称 Internal name: " + internalName);
            
            // 4. 分析和建议
            System.out.println();
            System.out.println("分析 Analysis:");
            analyzeFontSize(actualSizeMB, internalName);
            
            System.out.println();
            System.out.println("推荐配置 Recommended Configuration:");
            System.out.println("  regularFontPath: " + fontPath);
            System.out.println("  defaultFontFamily: " + internalName + ", sans-serif");
            
            return true;
            
        } catch (Exception e) {
            System.err.println("✗ 错误 Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private static String extractFontLabel(String fontPath) {
        int lastSlash = fontPath.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < fontPath.length() - 1) {
            return fontPath.substring(lastSlash + 1);
        }
        return fontPath;
    }
    
    private static String getExtension(String fontPath) {
        int lastDot = fontPath.lastIndexOf('.');
        if (lastDot >= 0) {
            return fontPath.substring(lastDot);
        }
        return ".ttf";
    }
    
    private static void analyzeFontSize(double sizeMB, String internalName) {
        if (sizeMB > 15) {
            System.out.println("  📦 超大字体文件 Very large font file (>15MB)");
            System.out.println("     通常是完整的CJK字体，包含中日韩全部字符");
            System.out.println("     Usually full CJK font with all Chinese/Japanese/Korean characters");
            if (internalName.contains("Noto") || internalName.contains("CJK") || internalName.contains("Source Han")) {
                System.out.println("     ✓ 这是Noto Sans CJK或思源黑体完整版");
                System.out.println("     ✓ This is Noto Sans CJK or Source Han Sans full version");
            }
        } else if (sizeMB > 10) {
            System.out.println("  📦 大字体文件 Large font file (10-15MB)");
            System.out.println("     包含扩展字符集或多个字重");
            System.out.println("     Contains extended character set or multiple weights");
        } else if (sizeMB > 5) {
            System.out.println("  📦 中等大小字体 Medium size font (5-10MB)");
            if (internalName.contains("HarmonyOS")) {
                System.out.println("     ℹ️  这可能是HarmonyOS完整版");
                System.out.println("     ℹ️  This may be HarmonyOS full version");
                System.out.println("     ✓ 正常大小，足够支持简体中文显示");
                System.out.println("     ✓ Normal size, sufficient for Simplified Chinese");
            }
        } else if (sizeMB > 2) {
            System.out.println("  📦 标准大小字体 Standard size font (2-5MB)");
            System.out.println("     ✓ 适合简体中文使用");
            System.out.println("     ✓ Suitable for Simplified Chinese");
        } else {
            System.out.println("  📦 小字体文件 Small font file (<2MB)");
            System.out.println("     ⚠️  可能只包含部分常用字，生僻字可能显示不出来");
            System.out.println("     ⚠️  May only contain common characters, rare characters may not display");
        }
        
        System.out.println();
        System.out.println("  PDF文件预计大小 Expected PDF size:");
        System.out.printf("     包含此字体的PDF约增加 %.1f-%.1f MB\n", sizeMB * 0.3, sizeMB * 0.5);
        System.out.printf("     PDF with this font will increase by ~%.1f-%.1f MB\n", sizeMB * 0.3, sizeMB * 0.5);
    }
}
