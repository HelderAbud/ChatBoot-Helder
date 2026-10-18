package com.chatboot.documents;

import java.util.ArrayList;
import java.util.List;

public class MarkdownChunker {
    public List<MarkdownChunk> chunk(String markdown) {
        if (markdown.startsWith("\uFEFF")) {
            markdown = markdown.substring(1);
        }
        List<MarkdownChunk> chunks = new ArrayList<>();
        String heading = null;
        StringBuilder body = new StringBuilder();
        boolean insideFence = false;
        for (String line : markdown.split("\\R", -1)) {
            if (line.startsWith("```")) {
                insideFence = !insideFence;
                appendLine(body, line);
                continue;
            }
            if (!insideFence && line.startsWith("#")) {
                flush(chunks, heading, body);
                heading = line.replaceFirst("^#+\\s*", "");
                continue;
            }
            appendLine(body, line);
        }
        flush(chunks, heading, body);
        return chunks;
    }

    private static void appendLine(StringBuilder body, String line) {
        if (!body.isEmpty()) {
            body.append('\n');
        }
        body.append(line);
    }

    private static void flush(List<MarkdownChunk> chunks, String heading, StringBuilder body) {
        if (heading == null) {
            body.setLength(0);
            return;
        }
        chunks.add(new MarkdownChunk(heading, body.toString().trim()));
        body.setLength(0);
    }
}
