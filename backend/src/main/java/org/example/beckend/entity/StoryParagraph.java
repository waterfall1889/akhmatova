package org.example.beckend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 与库表 {@code story_paragragh} 对齐：部分库将主键列命名为 {@code paragragh_id}，
 * 同时保留 {@code paragraph_id} 存业务章节号（与 {@code paragragh_text.paragraph_id} 一致）。
 * 列 {@code index}：章节顺位，从 1 起；Java 属性 {@link #chapterOrder}。
 */
@Entity
@Table(name = "story_paragragh")
public class StoryParagraph {

    @Id
    @Column(name = "paragragh_id")
    private Long paragraghId;

    @Column(name = "paragraph_id", nullable = false)
    private Long paragraphId;

    @Column(name = "story_id", nullable = false)
    private Long storyId;

    @Column(name = "index", nullable = false)
    private int chapterOrder;

    @Column(name = "paragraph_title", nullable = false, columnDefinition = "TEXT")
    private String paragraphTitle;

    public Long getParagraghId() {
        return paragraghId;
    }

    public void setParagraghId(Long paragraghId) {
        this.paragraghId = paragraghId;
    }

    public Long getParagraphId() {
        return paragraphId;
    }

    public void setParagraphId(Long paragraphId) {
        this.paragraphId = paragraphId;
    }

    public Long getStoryId() {
        return storyId;
    }

    public void setStoryId(Long storyId) {
        this.storyId = storyId;
    }

    public int getChapterOrder() {
        return chapterOrder;
    }

    public void setChapterOrder(int chapterOrder) {
        this.chapterOrder = chapterOrder;
    }

    public String getParagraphTitle() {
        return paragraphTitle;
    }

    public void setParagraphTitle(String paragraphTitle) {
        this.paragraphTitle = paragraphTitle;
    }
}
