package com.mercury.pdf.render.model;

/**
 * Standard section type identifiers for matcher-report-final template.
 * 
 * <p>These constants provide a template-independent way to identify sections,
 * decoupling the backend data structure from template rendering logic.
 * 
 * <p><b>Benefits of using SectionType:</b>
 * <ul>
 *   <li>Backend data no longer depends on title prefixes (e.g., "1.", "2.")</li>
 *   <li>Section titles can be any text without numeric restrictions</li>
 *   <li>Sections can be added in any order</li>
 *   <li>Template changes don't require backend code changes</li>
 *   <li>Clear semantic meaning of each section</li>
 * </ul>
 * 
 * <p><b>Usage Example:</b>
 * <pre>
 * // Old way (tight coupling with title prefix)
 * Section section = new Section("1.1 精确匹配通过");
 * 
 * // New way (decoupled with sectionType)
 * Section section = new Section("精确匹配通过", SectionType.CHAPTER_1);
 * // or
 * Section section = new Section("精确匹配通过")
 *     .withSectionType(SectionType.CHAPTER_1);
 * </pre>
 * 
 * @see Section#getSectionType()
 * @see Section#withSectionType(String)
 */
public final class SectionType {
    
    /**
     * Chapter 1: Match Results (匹配结果详细列表)
     * Used for sections that should appear in the first chapter of the report.
     */
    public static final String CHAPTER_1 = "chapter1";
    
    /**
     * Chapter 2: Detailed Analysis (详细分析内容)
     * Used for sections containing analysis and insights.
     */
    public static final String CHAPTER_2 = "chapter2";
    
    /**
     * Chapter 3: Summary (预审结果总结)
     * Used for summary sections and conclusions.
     */
    public static final String CHAPTER_3 = "chapter3";
    
    /**
     * Chapter 4: Notes (报告说明)
     * Used for report notes, disclaimers, and additional information.
     */
    public static final String CHAPTER_4 = "chapter4";
    
    /**
     * Other sections (其他内容)
     * Used for miscellaneous sections that don't fit into chapters 1-4.
     * These will be rendered at the end of Chapter 3 in the matcher-report-final template.
     */
    public static final String OTHER = "other";
    
    /**
     * Appendix sections (附录)
     * Alias for OTHER, used for appendix content.
     */
    public static final String APPENDIX = "appendix";
    
    // Private constructor to prevent instantiation
    private SectionType() {
        throw new AssertionError("SectionType is a utility class and should not be instantiated");
    }
    
    /**
     * Checks if the given section type is a standard chapter type (chapter1-4).
     * 
     * @param sectionType The section type to check
     * @return true if the section type is one of chapter1, chapter2, chapter3, or chapter4
     */
    public static boolean isChapterType(String sectionType) {
        return CHAPTER_1.equals(sectionType) 
            || CHAPTER_2.equals(sectionType) 
            || CHAPTER_3.equals(sectionType) 
            || CHAPTER_4.equals(sectionType);
    }
    
    /**
     * Infers the section type from a title using the legacy title prefix pattern.
     * This method provides backward compatibility for existing code that uses title prefixes.
     * 
     * @param title The section title
     * @return The inferred section type, or null if no pattern matches
     */
    public static String inferFromTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return null;
        }
        
        String trimmedTitle = title.trim();
        if (trimmedTitle.startsWith("1.")) {
            return CHAPTER_1;
        } else if (trimmedTitle.startsWith("2.")) {
            return CHAPTER_2;
        } else if (trimmedTitle.startsWith("3.")) {
            return CHAPTER_3;
        } else if (trimmedTitle.startsWith("4.")) {
            return CHAPTER_4;
        }
        
        return null; // No standard pattern found
    }
}
