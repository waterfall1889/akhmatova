package org.example.beckend.dto.story;

/**
 * 在指定章节之后插入新章；{@code afterParagraphId} 为当前章节业务 id（paragraph_id）。
 */
public record ParagraphAfterCreateRequest(String afterParagraphId) {
}
