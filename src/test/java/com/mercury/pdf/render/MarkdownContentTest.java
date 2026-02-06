package com.mercury.pdf.render;

import com.mercury.pdf.render.model.Section;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MarkdownContentTest {

    @Test
    public void testSectionMarkdownConvertsToHtml() {
        Section section = new Section("Markdown Section")
            .withMarkdownContent("# 标题\n\n- 项目1\n- 项目2");

        String customContent = section.getCustomContent();

        assertNotNull(customContent);
        assertTrue(customContent.contains("<h1>标题</h1>"));
        assertTrue(customContent.contains("<li>项目1</li>"));
    }

    @Test
    public void testMarkdownEscapesRawHtml() {
        Section section = new Section("Security")
            .withMarkdownContent("Hello <script>alert('x')</script>");

        String customContent = section.getCustomContent();

        assertNotNull(customContent);
        assertTrue(customContent.contains("&lt;script&gt;alert('x')&lt;/script&gt;"));
    }
}
