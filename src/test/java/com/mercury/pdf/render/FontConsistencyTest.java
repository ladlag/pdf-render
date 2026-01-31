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
        java.lang.reflect.Method method = HtmlReportRenderer.class.getDeclaredMethod("formatFontFamilyName", String.class);
        method.setAccessible(true);

        assertEquals("", method.invoke(renderer, new Object[] { null }), "Null input should return empty string");
        assertEquals("", method.invoke(renderer, ""), "Empty string should remain empty");
        assertEquals("HarmonyOS", method.invoke(renderer, "HarmonyOS"), "Unquoted single word should remain unchanged");
        assertEquals("\"HarmonyOS Sans\"", method.invoke(renderer, "HarmonyOS Sans"),
            "Names with spaces should be quoted");
        assertEquals("\"HarmonyOS, Sans\"", method.invoke(renderer, "HarmonyOS, Sans"),
            "Names with commas should be quoted");
        assertEquals("\"HarmonyOS Sans\"", method.invoke(renderer, "\"HarmonyOS Sans\""),
            "Already double-quoted names should remain unchanged");
        assertEquals("'HarmonyOS Sans'", method.invoke(renderer, "'HarmonyOS Sans'"),
            "Already single-quoted names should remain unchanged");
    }
}
