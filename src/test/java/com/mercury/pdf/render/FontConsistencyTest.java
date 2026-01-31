package com.mercury.pdf.render;

import com.mercury.pdf.render.util.FontNameExtractor;
import org.junit.jupiter.api.Test;

import java.awt.Font;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class FontConsistencyTest {

    @Test
    public void testFontFamilyNameNormalization() throws Exception {
        String familyName = FontNameExtractor.extractFontFamilyName("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");

        assertNotNull(familyName);
        assertFalse(familyName.trim().isEmpty(), "Font family name should not be empty");
        assertFalse(familyName.contains("_"), "Font family name should not contain underscores");
        assertFalse(familyName.toLowerCase(Locale.ROOT).endsWith(" regular"), "Font family name should not include style suffix");
        assertNotEquals("Dialog", familyName, "Font family name should not be generic");
    }

    @Test
    public void testChartFontLoadsWithWindowsStylePath() throws Exception {
        ChartRenderer renderer = new ChartRenderer();
        Path tempFont = Files.createTempFile("chart-font-", ".ttf");
        tempFont.toFile().deleteOnExit();

        try (InputStream stream = getClass().getResourceAsStream("/fonts/HarmonyOS_Sans_SC_Regular.ttf")) {
            assertNotNull(stream, "Test font should be available on classpath");
            Files.copy(stream, tempFont, StandardCopyOption.REPLACE_EXISTING);
        }

        String unixStylePath = tempFont.toAbsolutePath().toString().replace('\\', '/');
        String windowsStylePath = unixStylePath.replace('/', '\\');

        renderer.setChartFont(windowsStylePath);
        renderer.setChartFont(unixStylePath);
        assertNotNull(renderer.getChartFont(), "Chart font should be loaded from normalized paths");
    }

    @Test
    public void testFormatFontFamilyName() throws Exception {
        HtmlReportRenderer renderer = new HtmlReportRenderer();

        assertEquals("", renderer.formatFontFamilyName(null), "Null input should return empty string");
        assertEquals("", renderer.formatFontFamilyName(""), "Empty string should remain empty");
        assertEquals("HarmonyOS", renderer.formatFontFamilyName("HarmonyOS"), "Unquoted single word should remain unchanged");
        assertEquals("\"HarmonyOS Sans\"", renderer.formatFontFamilyName("HarmonyOS Sans"),
            "Names with spaces should be quoted");
        assertEquals("\"HarmonyOS, Sans\"", renderer.formatFontFamilyName("HarmonyOS, Sans"),
            "Names with commas should be quoted");
        assertEquals("\"HarmonyOS Sans\"", renderer.formatFontFamilyName("\"HarmonyOS Sans\""),
            "Already double-quoted names should remain unchanged");
        assertEquals("'HarmonyOS Sans'", renderer.formatFontFamilyName("'HarmonyOS Sans'"),
            "Already single-quoted names should remain unchanged");
        assertEquals("HarmonyOS-2", renderer.formatFontFamilyName("HarmonyOS-2"),
            "Names with numbers should remain unchanged");
        assertEquals("\"HarmonyOS Sans\"", renderer.formatFontFamilyName("  HarmonyOS Sans  "),
            "Leading/trailing spaces should be trimmed before quoting");
        assertEquals("\"HarmonyOS   Sans\"", renderer.formatFontFamilyName("HarmonyOS   Sans"),
            "Multiple internal spaces should be preserved when quoted");
        assertEquals("\"\"HarmonyOS Sans'\"", renderer.formatFontFamilyName("\"HarmonyOS Sans'"),
            "Mismatched quotes should be treated as part of the name and quoted");
        assertEquals("HarmonyOS_Sans", renderer.formatFontFamilyName("HarmonyOS_Sans"),
            "Underscore names should remain unchanged");
    }
}
