package com.finos.matcher.report.util;

import java.awt.Font;
import java.io.InputStream;
import java.net.URL;

/**
 * Utility class to extract font family name from font files.
 * This helps ensure that CSS font-family matches the actual font's internal name.
 */
public class FontNameExtractor {
    
    /**
     * Extracts the font family name from a font file path.
     * Supports both classpath: and file system paths.
     * 
     * @param fontPath Font file path (e.g., "classpath:/fonts/NotoSansCJKsc-Regular.otf")
     * @return Font family name (e.g., "Noto Sans CJK SC")
     * @throws Exception if font cannot be loaded or read
     */
    public static String extractFontFamilyName(String fontPath) throws Exception {
        InputStream fontStream = null;
        
        try {
            if (fontPath.startsWith("classpath:")) {
                String resourcePath = fontPath.substring("classpath:".length());
                fontStream = FontNameExtractor.class.getResourceAsStream(resourcePath);
                if (fontStream == null) {
                    throw new IllegalArgumentException("Font not found in classpath: " + resourcePath);
                }
            } else if (fontPath.startsWith("file:")) {
                URL url = new URL(fontPath);
                fontStream = url.openStream();
            } else {
                // Assume it's a file system path
                fontStream = new java.io.FileInputStream(fontPath);
            }
            
            // Load font and extract family name
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            String familyName = font.getFamily();
            
            return familyName;
            
        } finally {
            if (fontStream != null) {
                try {
                    fontStream.close();
                } catch (Exception e) {
                    // Ignore close errors
                }
            }
        }
    }
    
    /**
     * Validates that the CSS font family contains the actual font's internal name.
     * 
     * @param fontPath Path to the font file
     * @param cssFontFamily CSS font-family string (may contain multiple fonts)
     * @return true if valid, false if mismatch detected
     */
    public static boolean validateFontFamily(String fontPath, String cssFontFamily) {
        try {
            String internalName = extractFontFamilyName(fontPath);
            
            // Check if CSS font-family contains the internal name
            return cssFontFamily != null && cssFontFamily.contains(internalName);
            
        } catch (Exception e) {
            // If we can't extract the name, assume it's OK
            System.err.println("Warning: Could not validate font family: " + e.getMessage());
            return true;
        }
    }
    
    /**
     * Prints font information for debugging purposes.
     * 
     * @param fontPath Path to the font file
     */
    public static void printFontInfo(String fontPath) {
        try {
            String familyName = extractFontFamilyName(fontPath);
            
            System.out.println("Font Information:");
            System.out.println("  Path: " + fontPath);
            System.out.println("  Internal Family Name: " + familyName);
            System.out.println("\nTo use this font, configure:");
            System.out.println("  fontConfig.setRegularFontPath(\"" + fontPath + "\");");
            System.out.println("  fontConfig.setDefaultFontFamily(\"" + familyName + ", DejaVu Sans, sans-serif\");");
            
        } catch (Exception e) {
            System.err.println("Error reading font: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Command-line tool to extract font names.
     * Usage: java FontNameExtractor <font-path>
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Font Name Extractor");
            System.out.println("===================");
            System.out.println();
            System.out.println("Usage: java FontNameExtractor <font-path>");
            System.out.println();
            System.out.println("Examples:");
            System.out.println("  java FontNameExtractor classpath:/fonts/NotoSansCJKsc-Regular.otf");
            System.out.println("  java FontNameExtractor /path/to/font.ttf");
            System.out.println();
            System.out.println("This tool extracts the internal font family name from a font file,");
            System.out.println("which is needed for proper FontConfig configuration.");
            return;
        }
        
        String fontPath = args[0];
        printFontInfo(fontPath);
    }
}
