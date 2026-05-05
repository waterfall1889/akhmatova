package org.example.beckend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "paragragh_text")
public class ParagraphText {

    @Id
    @Column(name = "paragraph_id")
    private Long paragraphId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String details;

    public Long getParagraphId() {
        return paragraphId;
    }

    public void setParagraphId(Long paragraphId) {
        this.paragraphId = paragraphId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
