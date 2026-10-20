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
        char fenceMarker = 0;
        int fenceLength = 0;
        for (String line : markdown.split("\\R", -1)) {
            if (fenceMarker != 0) {
                if (closesFence(line, fenceMarker, fenceLength)) {
                    fenceMarker = 0;
                }
                appendLine(body, line);
                continue;
            }
            FenceMark opening = openingFence(line);
            if (opening != null) {
                fenceMarker = opening.marker();
                fenceLength = opening.length();
                appendLine(body, line);
                continue;
            }
            if (isHeading(line)) {
                flush(chunks, heading, body);
                heading = line.replaceFirst("^#+\\s*", "");
                continue;
            }
            appendLine(body, line);
        }
        flush(chunks, heading, body);
        return chunks;
    }

    private record FenceMark(char marker, int length) {}

    private static FenceMark openingFence(String line) {
        int index = indent(line);
        if (index >= line.length()) {
            return null;
        }
        char marker = line.charAt(index);
        if (marker != '`' && marker != '~') {
            return null;
        }
        int length = run(line, index, marker);
        if (length < 3) {
            return null;
        }
        if (marker == '`' && line.indexOf('`', index + length) >= 0) {
            return null;
        }
        return new FenceMark(marker, length);
    }

    private static boolean closesFence(String line, char marker, int length) {
        int index = indent(line);
        int count = run(line, index, marker);
        if (count < length) {
            return false;
        }
        for (int rest = index + count; rest < line.length(); rest++) {
            if (!Character.isWhitespace(line.charAt(rest))) {
                return false;
            }
        }
        return true;
    }

    private static int indent(String line) {
        int spaces = 0;
        while (spaces < line.length() && spaces < 3 && line.charAt(spaces) == ' ') {
            spaces++;
        }
        return spaces;
    }

    private static int run(String line, int start, char marker) {
        int count = 0;
        while (start + count < line.length() && line.charAt(start + count) == marker) {
            count++;
        }
        return count;
    }

    private static boolean isHeading(String line) {
        int marks = 0;
        while (marks < line.length() && line.charAt(marks) == '#') {
            marks++;
        }
        if (marks < 1 || marks > 6) {
            return false;
        }
        return marks == line.length() || Character.isWhitespace(line.charAt(marks));
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
