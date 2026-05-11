package org.example.beckend.repository;

import java.util.List;
import java.util.Optional;

import org.example.beckend.entity.StoryParagraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoryParagraphRepository extends JpaRepository<StoryParagraph, Long> {

    List<StoryParagraph> findByStoryIdOrderByChapterOrderAscParagraphIdAsc(Long storyId);

    Optional<StoryParagraph> findByStoryIdAndParagraphId(Long storyId, Long paragraphId);

    Optional<StoryParagraph> findTopByStoryIdOrderByParagraphIdDesc(Long storyId);

    long countByStoryId(Long storyId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update StoryParagraph p set p.chapterOrder = p.chapterOrder + 1 where p.storyId = :storyId and p.chapterOrder > :afterOrder")
    int shiftChapterOrderAfter(@Param("storyId") Long storyId, @Param("afterOrder") int afterOrder);
}
