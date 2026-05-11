package org.example.beckend.dto.story;

public record StorySaveResponse(boolean success, String message) {

    public static StorySaveResponse ok() {
        return new StorySaveResponse(true, "已保存");
    }

    public static StorySaveResponse fail(String message) {
        return new StorySaveResponse(false, message);
    }
}
