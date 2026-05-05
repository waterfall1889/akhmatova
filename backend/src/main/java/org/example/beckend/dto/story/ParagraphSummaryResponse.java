package org.example.beckend.dto.story;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ParagraphSummaryResponse(
        String paragraphId,
        String paragraphTitle,
        @JsonProperty("index") int chapterIndex) {
}
