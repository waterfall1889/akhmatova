package org.example.beckend.dto.story;

public record StoryCreatedResponse(boolean success, String message, String storyId) {

    public static StoryCreatedResponse ok(long storyId) {
        return new StoryCreatedResponse(true, "已创建", Long.toString(storyId));
    }

    public static StoryCreatedResponse fail(String message) {
        return new StoryCreatedResponse(false, message, null);
    }
}
