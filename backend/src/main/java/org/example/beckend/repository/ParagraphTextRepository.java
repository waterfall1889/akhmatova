package org.example.beckend.repository;

import org.example.beckend.entity.ParagraphText;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParagraphTextRepository extends JpaRepository<ParagraphText, Long> {
}
