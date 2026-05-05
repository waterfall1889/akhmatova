package org.example.beckend.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.example.beckend.dto.story.ParagraphAfterCreateRequest;
import org.example.beckend.dto.story.ParagraphCreatedResponse;
import org.example.beckend.dto.story.ParagraphSaveRequest;
import org.example.beckend.dto.story.ParagraphSummaryResponse;
import org.example.beckend.dto.story.StoryCreatedResponse;
import org.example.beckend.dto.story.StoryEditorResponse;
import org.example.beckend.dto.story.StoryListItemResponse;
import org.example.beckend.dto.story.StorySaveResponse;
import org.example.beckend.entity.ParagraphText;
import org.example.beckend.entity.StoryMeta;
import org.example.beckend.entity.StoryParagraph;
import org.example.beckend.repository.ParagraphTextRepository;
import org.example.beckend.repository.StoryMetaRepository;
import org.example.beckend.repository.StoryParagraphRepository;
import org.example.beckend.repository.UserInformationRepository;
import org.example.beckend.story.StoryIdCodec;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoryService {

    private static final int MAX_STORY_NAME_LEN = 200;
    private static final int MAX_PARAGRAPH_TITLE_LEN = 200;
    private static final String DEFAULT_CHAPTER_TITLE = "第 1 章";

    private final StoryMetaRepository storyMetaRepository;
    private final StoryParagraphRepository storyParagraphRepository;
    private final ParagraphTextRepository paragraphTextRepository;
    private final UserInformationRepository userInformationRepository;

    public StoryService(
            StoryMetaRepository storyMetaRepository,
            StoryParagraphRepository storyParagraphRepository,
            ParagraphTextRepository paragraphTextRepository,
            UserInformationRepository userInformationRepository) {
        this.storyMetaRepository = storyMetaRepository;
        this.storyParagraphRepository = storyParagraphRepository;
        this.paragraphTextRepository = paragraphTextRepository;
        this.userInformationRepository = userInformationRepository;
    }

    public List<StoryListItemResponse> listMine(long authorId) {
        List<StoryMeta> rows = storyMetaRepository.findByAuthorIdOrderByStoryIdDesc(authorId);
        String authorName = userInformationRepository.findById(authorId)
                .map(u -> u.getUserName())
                .orElse("");
        List<StoryListItemResponse> out = new ArrayList<>(rows.size());
        for (StoryMeta m : rows) {
            out.add(new StoryListItemResponse(
                    Long.toString(m.getStoryId()),
                    m.getStoryName(),
                    authorName,
                    m.getBuildTime(),
                    m.getLastEditTime(),
                    m.getReadTimes()));
        }
        return out;
    }

    @Transactional
    public StoryCreatedResponse createStory(long authorId, String storyName) {
        String name = storyName == null ? "" : storyName.trim();
        if (name.isEmpty()) {
            return StoryCreatedResponse.fail("请填写小说名称");
        }
        if (name.length() > MAX_STORY_NAME_LEN) {
            return StoryCreatedResponse.fail("小说名称过长");
        }
        Long maxSid = storyMetaRepository.findTopByAuthorIdOrderByStoryIdDesc(authorId)
                .map(StoryMeta::getStoryId)
                .orElse(null);
        long newStoryId = StoryIdCodec.nextStoryId(authorId, maxSid);
        OffsetDateTime now = OffsetDateTime.now();
        StoryMeta meta = new StoryMeta();
        meta.setStoryId(newStoryId);
        meta.setStoryName(name);
        meta.setAuthorId(authorId);
        meta.setBuildTime(now);
        meta.setLastEditTime(now);
        meta.setReadTimes(0L);
        storyMetaRepository.save(meta);

        long paragraphId = StoryIdCodec.paragraphId(newStoryId, 1);
        StoryParagraph p = new StoryParagraph();
        p.setParagraghId(paragraphId);
        p.setParagraphId(paragraphId);
        p.setStoryId(newStoryId);
        p.setChapterOrder(1);
        p.setParagraphTitle(DEFAULT_CHAPTER_TITLE);
        storyParagraphRepository.save(p);

        ParagraphText t = new ParagraphText();
        t.setParagraphId(paragraphId);
        t.setDetails("");
        paragraphTextRepository.save(t);

        return StoryCreatedResponse.ok(newStoryId);
    }

    public Optional<StoryEditorResponse> loadEditor(long authorId, long storyId, Long paragraphIdParam) {
        Optional<StoryMeta> metaOpt = storyMetaRepository.findByStoryIdAndAuthorId(storyId, authorId);
        if (metaOpt.isEmpty()) {
            return Optional.empty();
        }
        StoryMeta meta = metaOpt.get();
        List<StoryParagraph> plist = storyParagraphRepository.findByStoryIdOrderByChapterOrderAscParagraphIdAsc(storyId);
        if (plist.isEmpty()) {
            return Optional.empty();
        }
        List<ParagraphSummaryResponse> summaries = plist.stream()
                .map(p -> new ParagraphSummaryResponse(
                        Long.toString(p.getParagraphId()),
                        p.getParagraphTitle(),
                        p.getChapterOrder()))
                .toList();

        long activeId;
        if (paragraphIdParam != null && paragraphIdParam > 0) {
            boolean ok = plist.stream().anyMatch(p -> p.getParagraphId().equals(paragraphIdParam));
            activeId = ok ? paragraphIdParam : plist.get(0).getParagraphId();
        } else {
            activeId = plist.get(0).getParagraphId();
        }

        StoryParagraph active = plist.stream()
                .filter(p -> p.getParagraphId().equals(activeId))
                .findFirst()
                .orElse(plist.get(0));
        String details = paragraphTextRepository.findById(active.getParagraphId())
                .map(ParagraphText::getDetails)
                .orElse("");

        return Optional.of(new StoryEditorResponse(
                Long.toString(meta.getStoryId()),
                meta.getStoryName(),
                meta.getBuildTime(),
                meta.getLastEditTime(),
                meta.getReadTimes(),
                summaries,
                Long.toString(active.getParagraphId()),
                active.getParagraphTitle(),
                details));
    }

    @Transactional
    public StorySaveResponse saveParagraph(long authorId, long storyId, long paragraphId, ParagraphSaveRequest body) {
        Optional<StoryMeta> metaOpt = storyMetaRepository.findByStoryIdAndAuthorId(storyId, authorId);
        if (metaOpt.isEmpty()) {
            return StorySaveResponse.fail("小说不存在或无权操作");
        }
        StoryMeta meta = metaOpt.get();
        Optional<StoryParagraph> pOpt = storyParagraphRepository.findByStoryIdAndParagraphId(storyId, paragraphId);
        if (pOpt.isEmpty()) {
            return StorySaveResponse.fail("章节不存在");
        }
        StoryParagraph p = pOpt.get();
        if (body.paragraphTitle() != null) {
            String t = body.paragraphTitle().trim();
            if (t.isEmpty()) {
                return StorySaveResponse.fail("章节标题不能为空");
            }
            if (t.length() > MAX_PARAGRAPH_TITLE_LEN) {
                return StorySaveResponse.fail("章节标题过长");
            }
            p.setParagraphTitle(t);
            storyParagraphRepository.save(p);
        }
        if (body.details() != null) {
            ParagraphText text = paragraphTextRepository.findById(paragraphId)
                    .orElseGet(() -> {
                        ParagraphText nt = new ParagraphText();
                        nt.setParagraphId(paragraphId);
                        return nt;
                    });
            text.setDetails(body.details());
            paragraphTextRepository.save(text);
        }
        meta.setLastEditTime(OffsetDateTime.now());
        storyMetaRepository.save(meta);
        return StorySaveResponse.ok();
    }

    @Transactional
    public ParagraphCreatedResponse addParagraphAfter(long authorId, long storyId, ParagraphAfterCreateRequest body) {
        if (body == null || body.afterParagraphId() == null || body.afterParagraphId().isBlank()) {
            return ParagraphCreatedResponse.fail("请指定在哪一章之后新建");
        }
        long afterPid;
        try {
            afterPid = Long.parseLong(body.afterParagraphId().trim());
        } catch (NumberFormatException e) {
            return ParagraphCreatedResponse.fail("章节 id 无效");
        }
        Optional<StoryMeta> metaOpt = storyMetaRepository.findByStoryIdAndAuthorId(storyId, authorId);
        if (metaOpt.isEmpty()) {
            return ParagraphCreatedResponse.fail("小说不存在或无权操作");
        }
        StoryMeta meta = metaOpt.get();
        Optional<StoryParagraph> afterOpt = storyParagraphRepository.findByStoryIdAndParagraphId(storyId, afterPid);
        if (afterOpt.isEmpty()) {
            return ParagraphCreatedResponse.fail("参考章节不存在");
        }
        StoryParagraph after = afterOpt.get();
        int afterOrder = after.getChapterOrder();
        storyParagraphRepository.shiftChapterOrderAfter(storyId, afterOrder);
        storyParagraphRepository.flush();

        Long maxPid = storyParagraphRepository.findTopByStoryIdOrderByParagraphIdDesc(storyId)
                .map(StoryParagraph::getParagraphId)
                .orElse(null);
        int nextSeq = StoryIdCodec.nextChapterSequence(storyId, maxPid);
        long newParagraphId = StoryIdCodec.paragraphId(storyId, nextSeq);
        int newOrder = afterOrder + 1;

        StoryParagraph p = new StoryParagraph();
        p.setParagraghId(newParagraphId);
        p.setParagraphId(newParagraphId);
        p.setStoryId(storyId);
        p.setChapterOrder(newOrder);
        p.setParagraphTitle("第 " + newOrder + " 章");
        storyParagraphRepository.save(p);

        ParagraphText t = new ParagraphText();
        t.setParagraphId(newParagraphId);
        t.setDetails("");
        paragraphTextRepository.save(t);

        meta.setLastEditTime(OffsetDateTime.now());
        storyMetaRepository.save(meta);

        return ParagraphCreatedResponse.ok(newParagraphId);
    }

    @Transactional
    public StorySaveResponse deleteParagraph(long authorId, long storyId, long paragraphId) {
        Optional<StoryMeta> metaOpt = storyMetaRepository.findByStoryIdAndAuthorId(storyId, authorId);
        if (metaOpt.isEmpty()) {
            return StorySaveResponse.fail("小说不存在或无权操作");
        }
        StoryMeta meta = metaOpt.get();
        long n = storyParagraphRepository.countByStoryId(storyId);
        if (n <= 1) {
            return StorySaveResponse.fail("至少保留一章，无法删除");
        }
        Optional<StoryParagraph> pOpt = storyParagraphRepository.findByStoryIdAndParagraphId(storyId, paragraphId);
        if (pOpt.isEmpty()) {
            return StorySaveResponse.fail("章节不存在");
        }
        StoryParagraph p = pOpt.get();
        paragraphTextRepository.deleteById(paragraphId);
        storyParagraphRepository.delete(p);
        renumberChapterOrders(storyId);
        meta.setLastEditTime(OffsetDateTime.now());
        storyMetaRepository.save(meta);
        return StorySaveResponse.ok();
    }

    private void renumberChapterOrders(long storyId) {
        List<StoryParagraph> list = storyParagraphRepository.findByStoryIdOrderByChapterOrderAscParagraphIdAsc(storyId);
        for (int i = 0; i < list.size(); i++) {
            int want = i + 1;
            StoryParagraph row = list.get(i);
            if (row.getChapterOrder() != want) {
                row.setChapterOrder(want);
                storyParagraphRepository.save(row);
            }
        }
    }
}
