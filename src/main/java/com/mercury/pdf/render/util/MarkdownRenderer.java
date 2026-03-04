package com.mercury.pdf.render.util;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.Collections;
import java.util.List;

/**
 * Renders Markdown into HTML for PDF templates.
 * Supports GFM tables extension. HTML escaping is configurable.
 */
public final class MarkdownRenderer {
    private static final List<Extension> EXTENSIONS = Collections.<Extension>singletonList(
        TablesExtension.create()
    );
    private static final Parser PARSER = Parser.builder()
        .extensions(EXTENSIONS)
        .build();
    private static final HtmlRenderer ESCAPED_RENDERER = HtmlRenderer.builder()
        .extensions(EXTENSIONS)
        .escapeHtml(true)
        .build();
    private static final HtmlRenderer UNESCAPED_RENDERER = HtmlRenderer.builder()
        .extensions(EXTENSIONS)
        .escapeHtml(false)
        .build();

    private MarkdownRenderer() {
    }

    /**
     * Converts Markdown to HTML with HTML escaping enabled.
     * Returns an empty string for blank input.
     */
    public static String toHtml(String markdown) {
        return toHtml(markdown, false);
    }

    /**
     * Converts Markdown to HTML. Returns an empty string for blank input.
     *
     * @param markdown the markdown text to convert
     * @param allowHtml when {@code true}, raw HTML embedded in the markdown is
     *                  preserved in the output; when {@code false}, it is escaped
     * @return the rendered HTML string
     */
    public static String toHtml(String markdown, boolean allowHtml) {
        if (markdown == null || markdown.trim().isEmpty()) {
            return "";
        }
        Node document = PARSER.parse(markdown);
        HtmlRenderer renderer = allowHtml ? UNESCAPED_RENDERER : ESCAPED_RENDERER;
        return renderer.render(document);
    }
}
