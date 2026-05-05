package org.example.beckend.dto.story;

import java.time.OffsetDateTime;
import java.util.List;

public record StoryEditorResponse(
        String storyId,
        String storyName,
        OffsetDateTime buildTime,
        OffsetDateTime lastEditTime,
        long readTimes,
        List<ParagraphSummaryResponse> paragraphs,
        String activeParagraphId,
        String activeParagraphTitle,
        String details) {
}
