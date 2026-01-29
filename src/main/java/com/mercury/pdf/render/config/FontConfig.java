package com.mercury.pdf.render.config;

/**
 * Configuration for fonts used in PDF generation.
 * Allows customization of font paths and families for both regular and CJK text.
 */
public class FontConfig {
    
    private String regularFontPath;
    private String boldFontPath;
    private String cjkFontPath;
    
    // Default fonts (system fonts or embedded)
    private String defaultFontFamily = "DejaVu Sans, Arial, sans-serif";
    private String cjkFontFamily = "Noto Sans CJK, SimSun, sans-serif";
    
    public FontConfig() {
    }
    
    /**
     * Gets the CSS @font-face declaration for custom fonts.
     * 
     * When using Flying Saucer with ITextRenderer and Identity-H encoding,
     * we don't need @font-face declarations. The fonts registered via addFont()
     * are available directly by their family name override.
     * 
     * Returns empty string as no CSS font declarations are needed.
     */
    public String getCssFontFaceDeclaration() {
        // No @font-face needed - fonts registered via addFont() with family name override
        // are automatically available in CSS by their specified names
        return "";
    }
    
    /**
     * Gets the font-family CSS value to use in the template.
     * 
     * When fonts are registered with addFont() using Identity-H encoding,
     * they become available by their internal font family name (from the font file).
     * We use the defaultFontFamily which should be set to match the registered font names.
     */
    public String getFontFamilyCss() {
        StringBuilder fontFamily = new StringBuilder();
        
        // Use the configured default font family (which should include the registered font names)
        fontFamily.append(defaultFontFamily);
        
        // Always add standard fallback fonts
        if (!defaultFontFamily.contains("DejaVu Sans")) {
            fontFamily.append(", DejaVu Sans");
        }
        if (!defaultFontFamily.contains("Arial")) {
            fontFamily.append(", Arial");
        }
        if (!defaultFontFamily.contains("sans-serif")) {
            fontFamily.append(", sans-serif");
        }
        
        return fontFamily.toString();
    }
    
    public String getRegularFontPath() {
        return regularFontPath;
    }
    
    /**
     * Sets the path to the regular font file (e.g., "classpath:/fonts/custom-regular.ttf")
     */
    public void setRegularFontPath(String regularFontPath) {
        this.regularFontPath = regularFontPath;
    }
    
    public String getBoldFontPath() {
        return boldFontPath;
    }
    
    /**
     * Sets the path to the bold font file (e.g., "classpath:/fonts/custom-bold.ttf")
     */
    public void setBoldFontPath(String boldFontPath) {
        this.boldFontPath = boldFontPath;
    }
    
    public String getCjkFontPath() {
        return cjkFontPath;
    }
    
    /**
     * Sets the path to the CJK font file for Chinese/Japanese/Korean text
     * (e.g., "classpath:/fonts/NotoSansCJK-Regular.otf")
     */
    public void setCjkFontPath(String cjkFontPath) {
        this.cjkFontPath = cjkFontPath;
    }
    
    public String getDefaultFontFamily() {
        return defaultFontFamily;
    }
    
    public void setDefaultFontFamily(String defaultFontFamily) {
        this.defaultFontFamily = defaultFontFamily;
    }
    
    public String getCjkFontFamily() {
        return cjkFontFamily;
    }
    
    public void setCjkFontFamily(String cjkFontFamily) {
        this.cjkFontFamily = cjkFontFamily;
    }
}
