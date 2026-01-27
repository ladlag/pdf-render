package com.finos.matcher.report.config;

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
     * Gets the CSS @font-face declaration for custom fonts
     */
    public String getCssFontFaceDeclaration() {
        StringBuilder css = new StringBuilder();
        
        if (regularFontPath != null) {
            css.append("@font-face {\n");
            css.append("  font-family: 'CustomFont';\n");
            css.append("  src: url('").append(regularFontPath).append("');\n");
            css.append("  font-weight: normal;\n");
            css.append("}\n");
        }
        
        if (boldFontPath != null) {
            css.append("@font-face {\n");
            css.append("  font-family: 'CustomFont';\n");
            css.append("  src: url('").append(boldFontPath).append("');\n");
            css.append("  font-weight: bold;\n");
            css.append("}\n");
        }
        
        if (cjkFontPath != null) {
            css.append("@font-face {\n");
            css.append("  font-family: 'CustomCJKFont';\n");
            css.append("  src: url('").append(cjkFontPath).append("');\n");
            css.append("}\n");
        }
        
        return css.toString();
    }
    
    /**
     * Gets the font-family CSS value to use in the template
     */
    public String getFontFamilyCss() {
        StringBuilder fontFamily = new StringBuilder();
        
        // Add custom font if configured
        if (regularFontPath != null) {
            fontFamily.append("'CustomFont', ");
        }
        
        // Add CJK font if configured
        if (cjkFontPath != null) {
            fontFamily.append("'CustomCJKFont', ");
        }
        
        // Add default fallback fonts
        fontFamily.append(defaultFontFamily);
        
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
