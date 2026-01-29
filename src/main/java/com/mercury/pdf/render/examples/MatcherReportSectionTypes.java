package com.mercury.pdf.render.examples;

/**
 * Example section type identifiers for matcher-report-final template.
 * 
 * <p><b>⚠️ IMPORTANT: This is an EXAMPLE class for demonstration purposes only!</b>
 * 
 * <p>This class demonstrates how you might define section types for a specific template.
 * It is NOT part of the core rendering engine and should NOT be used as a required dependency.
 * 
 * <p><b>For your own projects:</b>
 * <ul>
 *   <li>Define your own section type constants based on your template needs</li>
 *   <li>Use simple string values as sectionType in Section objects</li>
 *   <li>Keep template-specific logic in your own code, not in the engine</li>
 * </ul>
 * 
 * <p><b>Usage Example (for matcher-report-final template only):</b>
 * <pre>
 * // Option 1: Use constants from this example class
 * Section section = new Section("精确匹配通过", MatcherReportSectionTypes.CHAPTER_1);
 * 
 * // Option 2: Use plain strings directly (recommended for flexibility)
 * Section section = new Section("精确匹配通过", "chapter1");
 * 
 * // Option 3: Define your own constants in YOUR code
 * public class MyReportTypes {
 *     public static final String INTRO = "intro";
 *     public static final String BODY = "body";
 *     public static final String CONCLUSION = "conclusion";
 * }
 * </pre>
 * 
 * @see com.mercury.pdf.render.model.Section#getSectionType()
 * @see com.mercury.pdf.render.model.Section#withSectionType(String)
 */
public final class MatcherReportSectionTypes {
    
    /**
     * Example: Chapter 1 section type for matcher-report-final template
     */
    public static final String CHAPTER_1 = "chapter1";
    
    /**
     * Example: Chapter 2 section type for matcher-report-final template
     */
    public static final String CHAPTER_2 = "chapter2";
    
    /**
     * Example: Chapter 3 section type for matcher-report-final template
     */
    public static final String CHAPTER_3 = "chapter3";
    
    /**
     * Example: Chapter 4 section type for matcher-report-final template
     */
    public static final String CHAPTER_4 = "chapter4";
    
    /**
     * Example: Other/appendix sections
     */
    public static final String OTHER = "other";
    
    /**
     * Example: Appendix sections (alias for OTHER)
     */
    public static final String APPENDIX = "appendix";
    
    // Private constructor to prevent instantiation
    private MatcherReportSectionTypes() {
        throw new AssertionError("MatcherReportSectionTypes is an example utility class and should not be instantiated");
    }
}
