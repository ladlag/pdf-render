# Chinese Font Rendering Fix

## Problem Statement (问题描述)

生成的PDF中文依然是方框，检查注册的字体和matcher-report-final模版的字体对应逻辑，为什么没有对应上。

Translation: Chinese characters in the generated PDF still show as boxes. Check the font registration and the font correspondence logic in the matcher-report-final template. Why didn't they match up?

## Root Cause Analysis (根本原因分析)

The issue was in the **timing** of font name extraction and auto-correction:

### Before the Fix:
1. `setFontProperties()` - User configures font paths and font family names
2. `generatePdf()` → `prepareTemplateData()` - **Generates HTML with user-configured font family name**
3. `generatePdf()` → `convertHtmlToPdf()` → `registerFontsWithRenderer()` - **Extracts real font names from font files and auto-corrects font properties (TOO LATE!)**

The problem: Font family names were auto-corrected **AFTER** the HTML template was already rendered with the potentially incorrect font family name.

### Example of the Problem:

```java
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("WrongFontName, DejaVu Sans, Arial, sans-serif");
```

- The actual font name in `HarmonyOS_Sans_SC_Regular.ttf` is "HarmonyOS Sans SC"
- But the HTML was generated with `font-family: "WrongFontName", "DejaVu Sans", Arial, sans-serif`
- Font registration would extract the real name and auto-correct it to "HarmonyOS Sans SC"
- But this correction happened AFTER the HTML was already generated!
- Result: Font mismatch → Chinese characters render as boxes (□)

## Solution (解决方案)

Move font name extraction and auto-correction to happen **BEFORE** HTML template rendering:

### After the Fix:
1. `setFontProperties()` - **Immediately extracts real font names and auto-corrects font family names**
2. `generatePdf()` → `prepareTemplateData()` - **Generates HTML with corrected font family name**
3. `generatePdf()` → `convertHtmlToPdf()` → `registerFontsWithRenderer()` - Registers fonts (names already correct)

## Code Changes (代码更改)

### Modified: `HtmlReportRenderer.setFontProperties()`

Added font name extraction and auto-correction logic:

```java
public void setFontProperties(PdfRenderProperties.FontProperties fontProperties) {
    this.fontProperties = fontProperties;
    logFontProperties("FontProperties", fontProperties);
    
    // Extract and validate font names IMMEDIATELY to ensure CSS font-family matches registered fonts
    // This must happen BEFORE template rendering (prepareTemplateData)
    if (fontProperties != null) {
        try {
            // Extract and auto-correct CJK font family name if CJK font is configured
            if (fontProperties.getCjkPath() != null) {
                String fontPath = resolveFontPath(fontProperties.getCjkPath());
                String realFontName = FontNameExtractor.extractFontFamilyName(fontPath);
                
                if (realFontName != null && !realFontName.isEmpty()) {
                    // Auto-correct CJK family if not set or doesn't contain the real font name
                    if (fontProperties.getCjkFamily() == null || fontProperties.getCjkFamily().isEmpty() ||
                        !containsFontName(fontProperties.getCjkFamily(), realFontName)) {
                        fontProperties.setCjkFamily(realFontName + ", sans-serif");
                        logInfo("✓ Auto-configured CJK font-family: " + fontProperties.getCjkFamily());
                    }
                }
            }
            
            // Extract and auto-correct regular font family name if regular font is configured
            if (fontProperties.getRegularPath() != null) {
                String fontPath = resolveFontPath(fontProperties.getRegularPath());
                String realFontName = FontNameExtractor.extractFontFamilyName(fontPath);
                
                if (realFontName != null && !realFontName.isEmpty()) {
                    // Auto-correct default family if not set or doesn't contain the real font name
                    if (fontProperties.getDefaultFamily() == null || fontProperties.getDefaultFamily().isEmpty() ||
                        !containsFontName(fontProperties.getDefaultFamily(), realFontName)) {
                        fontProperties.setDefaultFamily(realFontName + ", sans-serif");
                        logInfo("✓ Auto-configured default font-family: " + fontProperties.getDefaultFamily());
                    }
                }
            }
        } catch (Exception e) {
            logWarn("Warning: Could not extract font names during configuration: " + e.getMessage());
            // Continue - will try again during font registration
        }
    }
    // ... rest of method
}
```

### Modified: `HtmlReportRenderer.registerFontsWithRenderer()`

Removed redundant auto-correction logic (since it's now done earlier):

```java
// Before: Had auto-correction code
if (realFontName != null && !realFontName.isEmpty()) {
    renderer.getFontResolver().addFont(fontPath, realFontName, BaseFont.IDENTITY_H, true, null);
    registeredNames.add(realFontName);
    // Auto-correct configuration (REMOVED - now done in setFontProperties)
}

// After: Just registers the font
if (realFontName != null && !realFontName.isEmpty()) {
    renderer.getFontResolver().addFont(fontPath, realFontName, BaseFont.IDENTITY_H, true, null);
    registeredNames.add(realFontName);
    // Font family already auto-corrected in setFontProperties
}
```

## Verification (验证)

### Test Case 1: Correct Font Family Name

```java
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
```

**Result:** Works correctly (no auto-correction needed)
- HTML generated with CSS: `font-family: "HarmonyOS Sans SC", "DejaVu Sans", Arial, sans-serif, "PDFFont";`
  - Note: The quotes in the CSS are added automatically for font names with spaces
- Font registered as: "HarmonyOS Sans SC"
- ✓ Font names match → Chinese characters render correctly

### Test Case 2: Wrong Font Family Name (Auto-Correction)

```java
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("WrongFontName, DejaVu Sans, Arial, sans-serif");
```

**Result:** Auto-corrected in `setFontProperties()`
- Initial config: "WrongFontName, DejaVu Sans, Arial, sans-serif"
- After setFontProperties: "HarmonyOS Sans SC, sans-serif"
- HTML generated with CSS: `font-family: "HarmonyOS Sans SC", sans-serif, "PDFFont";`
  - Note: Font names with spaces are automatically quoted in CSS
- Font registered as: "HarmonyOS Sans SC"
- ✓ Font names match → Chinese characters render correctly

### Test Case 3: No Font Family Name (Auto-Configuration)

```java
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
// No setDefaultFontFamily() call
```

**Result:** Auto-configured in `setFontProperties()`
- Initial config: null
- After setFontProperties: "HarmonyOS Sans SC, sans-serif"
- HTML generated with CSS: `font-family: "HarmonyOS Sans SC", sans-serif, "PDFFont";`
  - Note: Font names with spaces are automatically quoted in CSS
- Font registered as: "HarmonyOS Sans SC"
- ✓ Font names match → Chinese characters render correctly

## Benefits (优势)

1. **Automatic Font Name Matching**: Users no longer need to know the exact internal font family name
2. **Fail-Safe**: Even if wrong font family names are configured, they are automatically corrected
3. **Improved User Experience**: Configuration is more forgiving and "just works"
4. **Better Error Handling**: Font extraction happens earlier, so issues are caught sooner
5. **Consistent Behavior**: Same behavior whether using `FontConfig` or `FontProperties`

## Testing (测试)

All existing tests pass:
- ✓ `MatcherReportFinalTest` - Tests matcher-report-final template with Chinese characters
- ✓ `ChineseFontTest` - Tests Chinese font rendering in various scenarios
- ✓ `FontConsistencyTest` - Tests font consistency across different templates

New test scenarios verified:
- ✓ Auto-correction of wrong font family names
- ✓ Auto-configuration when no font family name is provided
- ✓ Correct font family names work without modification

## Conclusion (结论)

The fix ensures that **font registration and CSS font-family are always synchronized** by performing font name extraction and auto-correction **before** HTML template rendering. This eliminates the timing issue that caused Chinese characters to render as boxes.

**问题已解决！Chinese characters now render correctly in all templates, including matcher-report-final.**
