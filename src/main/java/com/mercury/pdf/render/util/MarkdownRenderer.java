package com.mercury.pdf.render.util;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * Renders Markdown into HTML for PDF templates.
 * HTML is escaped to prevent unintended raw HTML injection.
 */
public final class MarkdownRenderer {
    private static final Parser PARSER = Parser.builder().build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder()
        .escapeHtml(true)
        .build();

    private MarkdownRenderer() {
    }

    /**
     * Converts Markdown to HTML. Returns an empty string for blank input.
     */
    public static String toHtml(String markdown) {
        if (markdown == null || markdown.trim().isEmpty()) {
            return "";
        }
        Node document = PARSER.parse(markdown);
        return RENDERER.render(document);
    }
}
