package com.mercury.pdf.render.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for PDF rendering.
 * When used in Spring Boot, these can be configured via application.yml.
 * 
 * Example application.yml:
 * <pre>
 * pdf-render:
 *   template:
 *     location: classpath:/templates/
 *     default-name: report
 *     cache-enabled: true
 *   output:
 *     directory: /var/pdfs
 *   fonts:
 *     regular-path: classpath:/fonts/custom-regular.ttf
 *     bold-path: classpath:/fonts/custom-bold.ttf
 *     cjk-path: classpath:/fonts/NotoSansCJK-Regular.otf
 *   debug:
 *     enabled: true
 *     output-directory: debug-html
 *     include-timestamp: false
 * </pre>
 */
@ConfigurationProperties(prefix = "pdf-render")
public class PdfRenderProperties {
    
    private TemplateProperties template = new TemplateProperties();
    private OutputProperties output = new OutputProperties();
    private FontProperties fonts = new FontProperties();
    private DebugProperties debug = new DebugProperties();
    
    public TemplateProperties getTemplate() {
        return template;
    }
    
    public void setTemplate(TemplateProperties template) {
        this.template = template;
    }
    
    public OutputProperties getOutput() {
        return output;
    }
    
    public void setOutput(OutputProperties output) {
        this.output = output;
    }
    
    public FontProperties getFonts() {
        return fonts;
    }
    
    public void setFonts(FontProperties fonts) {
        this.fonts = fonts;
    }
    
    public DebugProperties getDebug() {
        return debug;
    }
    
    public void setDebug(DebugProperties debug) {
        this.debug = debug;
    }
    
    /**
     * Template configuration properties
     */
    public static class TemplateProperties {
        /**
         * Location of template files (default: classpath:/templates/)
         */
        private String location = "classpath:/templates/";
        
        /**
         * Default template name without .html extension (default: report)
         */
        private String defaultName = "report";
        
        /**
         * Whether to cache templates (default: true)
         */
        private boolean cacheEnabled = true;
        
        public String getLocation() {
            return location;
        }
        
        public void setLocation(String location) {
            this.location = location;
        }
        
        public String getDefaultName() {
            return defaultName;
        }
        
        public void setDefaultName(String defaultName) {
            this.defaultName = defaultName;
        }
        
        public boolean isCacheEnabled() {
            return cacheEnabled;
        }
        
        public void setCacheEnabled(boolean cacheEnabled) {
            this.cacheEnabled = cacheEnabled;
        }
    }
    
    /**
     * Output configuration properties
     */
    public static class OutputProperties {
        /**
         * Directory for generated PDF files (optional, default: no persistent storage)
         */
        private String directory;
        
        /**
         * Whether to save generated PDFs to the output directory (default: false)
         */
        private boolean saveToDirectory = false;
        
        public String getDirectory() {
            return directory;
        }
        
        public void setDirectory(String directory) {
            this.directory = directory;
        }
        
        public boolean isSaveToDirectory() {
            return saveToDirectory;
        }
        
        public void setSaveToDirectory(boolean saveToDirectory) {
            this.saveToDirectory = saveToDirectory;
        }
    }
    
    /**
     * Font configuration properties
     */
    public static class FontProperties {
        /**
         * Path to regular font file.
         * Defaults to the bundled HarmonyOS Sans SC font for out-of-the-box CJK support.
         */
        private String regularPath = "classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf";
        
        /**
         * Path to bold font file
         */
        private String boldPath;
        
        /**
         * Path to CJK (Chinese/Japanese/Korean) font file
         */
        private String cjkPath;
        
        /**
         * Default font family CSS
         * NOTE: This will be auto-configured to match the registered font's internal name.
         * If you set this manually, ensure it matches the actual font file's family name.
         * Set to empty/null to let the system auto-configure from the font file.
         */
        private String defaultFamily = "";
        
        /**
         * CJK font family CSS
         * NOTE: This will be auto-configured to match the registered CJK font's internal name.
         * If you set this manually, ensure it matches the actual CJK font file's family name.
         * Set to empty/null to let the system auto-configure from the font file.
         */
        private String cjkFamily = "";
        
        public String getRegularPath() {
            return regularPath;
        }
        
        public void setRegularPath(String regularPath) {
            this.regularPath = regularPath;
        }
        
        public String getBoldPath() {
            return boldPath;
        }
        
        public void setBoldPath(String boldPath) {
            this.boldPath = boldPath;
        }
        
        public String getCjkPath() {
            return cjkPath;
        }
        
        public void setCjkPath(String cjkPath) {
            this.cjkPath = cjkPath;
        }
        
        public String getDefaultFamily() {
            return defaultFamily;
        }
        
        public void setDefaultFamily(String defaultFamily) {
            this.defaultFamily = defaultFamily;
        }
        
        public String getCjkFamily() {
            return cjkFamily;
        }
        
        public void setCjkFamily(String cjkFamily) {
            this.cjkFamily = cjkFamily;
        }
    }
    
    /**
     * Debug configuration properties
     */
    public static class DebugProperties {
        /**
         * Whether to enable debug HTML output (default: false)
         */
        private boolean enabled = false;
        
        /**
         * Directory where debug HTML files will be saved (default: debug-html)
         */
        private String outputDirectory = "debug-html";
        
        /**
         * Whether to include timestamp in HTML filename (default: false)
         */
        private boolean includeTimestamp = false;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getOutputDirectory() {
            return outputDirectory;
        }
        
        public void setOutputDirectory(String outputDirectory) {
            this.outputDirectory = outputDirectory;
        }
        
        public boolean isIncludeTimestamp() {
            return includeTimestamp;
        }
        
        public void setIncludeTimestamp(boolean includeTimestamp) {
            this.includeTimestamp = includeTimestamp;
        }
    }
}
