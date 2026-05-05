package org.example.beckend.controller;

import java.util.List;
import java.util.Optional;

import org.example.beckend.auth.AuthSessionConstants;
import org.example.beckend.dto.story.ParagraphAfterCreateRequest;
import org.example.beckend.dto.story.ParagraphCreatedResponse;
import org.example.beckend.dto.story.ParagraphSaveRequest;
import org.example.beckend.dto.story.StoryCreatedResponse;
import org.example.beckend.dto.story.StoryCreateRequest;
import org.example.beckend.dto.story.StoryEditorResponse;
import org.example.beckend.dto.story.StoryListItemResponse;
import org.example.beckend.dto.story.StorySaveResponse;
import org.example.beckend.service.StoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/stories")
public class StoryController {

    private final StoryService storyService;

    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StoryListItemResponse>> list(HttpServletRequest request) {
        Optional<Long> uid = currentUserId(request);
        if (uid.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(storyService.listMine(uid.get()));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StoryCreatedResponse> create(@RequestBody StoryCreateRequest body, HttpServletRequest request) {
        Optional<Long> uid = currentUserId(request);
        if (uid.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        StoryCreatedResponse r = storyService.createStory(uid.get(), body.storyName());
        if (!r.success()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(r);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(r);
    }

    @GetMapping(value = "/{storyId}/editor", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StoryEditorResponse> editor(
            @PathVariable("storyId") long storyId,
            @RequestParam(value = "paragraphId", required = false) Long paragraphId,
            HttpServletRequest request) {
        Optional<Long> uid = currentUserId(request);
        if (uid.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return storyService.loadEditor(uid.get(), storyId, paragraphId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/{storyId}/paragraphs/{paragraphId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StorySaveResponse> saveParagraph(
            @PathVariable("storyId") long storyId,
            @PathVariable("paragraphId") long paragraphId,
            @RequestBody ParagraphSaveRequest body,
            HttpServletRequest request) {
        Optional<Long> uid = currentUserId(request);
        if (uid.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        StorySaveResponse r = storyService.saveParagraph(uid.get(), storyId, paragraphId, body);
        if (!r.success()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(r);
        }
        return ResponseEntity.ok(r);
    }

    @PostMapping(value = "/{storyId}/paragraphs", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ParagraphCreatedResponse> addParagraphAfter(
            @PathVariable("storyId") long storyId,
            @RequestBody ParagraphAfterCreateRequest body,
            HttpServletRequest request) {
        Optional<Long> uid = currentUserId(request);
        if (uid.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ParagraphCreatedResponse r = storyService.addParagraphAfter(uid.get(), storyId, body);
        if (!r.success()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(r);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(r);
    }

    @DeleteMapping(value = "/{storyId}/paragraphs/{paragraphId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StorySaveResponse> deleteParagraph(
            @PathVariable("storyId") long storyId,
            @PathVariable("paragraphId") long paragraphId,
            HttpServletRequest request) {
        Optional<Long> uid = currentUserId(request);
        if (uid.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        StorySaveResponse r = storyService.deleteParagraph(uid.get(), storyId, paragraphId);
        if (!r.success()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(r);
        }
        return ResponseEntity.ok(r);
    }

    private static Optional<Long> currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Optional.empty();
        }
        Object raw = session.getAttribute(AuthSessionConstants.USER_ID);
        if (raw instanceof Number num) {
            return Optional.of(num.longValue());
        }
        return Optional.empty();
    }
}
