package org.example.beckend.dto.story;

public record ParagraphCreatedResponse(boolean success, String message, String paragraphId) {

    public static ParagraphCreatedResponse ok(long paragraphId) {
        return new ParagraphCreatedResponse(true, "已新建章节", Long.toString(paragraphId));
    }

    public static ParagraphCreatedResponse fail(String message) {
        return new ParagraphCreatedResponse(false, message, null);
    }
}
