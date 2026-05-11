package org.example.beckend.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "story_meta")
public class StoryMeta {

    @Id
    @Column(name = "story_id")
    private Long storyId;

    @Column(name = "story_name", nullable = false, columnDefinition = "TEXT")
    private String storyName;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "build_time", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime buildTime;

    @Column(name = "last_edit_time", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime lastEditTime;

    @Column(name = "read_times", nullable = false)
    private long readTimes;

    public Long getStoryId() {
        return storyId;
    }

    public void setStoryId(Long storyId) {
        this.storyId = storyId;
    }

    public String getStoryName() {
        return storyName;
    }

    public void setStoryName(String storyName) {
        this.storyName = storyName;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public OffsetDateTime getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(OffsetDateTime buildTime) {
        this.buildTime = buildTime;
    }

    public OffsetDateTime getLastEditTime() {
        return lastEditTime;
    }

    public void setLastEditTime(OffsetDateTime lastEditTime) {
        this.lastEditTime = lastEditTime;
    }

    public long getReadTimes() {
        return readTimes;
    }

    public void setReadTimes(long readTimes) {
        this.readTimes = readTimes;
    }
}
