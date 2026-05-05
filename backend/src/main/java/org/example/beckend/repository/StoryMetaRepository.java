package org.example.beckend.repository;

import java.util.List;
import java.util.Optional;

import org.example.beckend.entity.StoryMeta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryMetaRepository extends JpaRepository<StoryMeta, Long> {

    List<StoryMeta> findByAuthorIdOrderByStoryIdDesc(Long authorId);

    Optional<StoryMeta> findByStoryIdAndAuthorId(Long storyId, Long authorId);

    Optional<StoryMeta> findTopByAuthorIdOrderByStoryIdDesc(Long authorId);
}
