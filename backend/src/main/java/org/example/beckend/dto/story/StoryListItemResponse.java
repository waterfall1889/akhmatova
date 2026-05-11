package org.example.beckend.dto.story;

import java.time.OffsetDateTime;

public record StoryListItemResponse(
        String storyId,
        String storyName,
        String authorName,
        OffsetDateTime buildTime,
        OffsetDateTime lastEditTime,
        long readTimes) {
}
