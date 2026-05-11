package org.example.beckend.story;

import java.util.Locale;

/**
 * 小说 / 章节 ID 规则（见 notes/story）：<br>
 * story_id = 作者 id（至多 9 位有效段）× 100_000 + 小说序号（0–99999）；<br>
 * paragraph_id = 14 位十进制（不足补零的 story_id）与 5 位章节序号拼接后的十进制 long。
 */
public final class StoryIdCodec {

    private static final long AUTHOR_MOD = 1_000_000_000L;
    private static final int MAX_SUFFIX = 99_999;

    private StoryIdCodec() {
    }

    public static long authorPart(long authorId) {
        long ap = authorId % AUTHOR_MOD;
        if (ap < 0) {
            ap += AUTHOR_MOD;
        }
        return ap;
    }

    public static long nextStoryId(long authorId, Long currentMaxStoryId) {
        long ap = authorPart(authorId);
        int nextSeq = 0;
        if (currentMaxStoryId != null && currentMaxStoryId > 0) {
            nextSeq = storySequence(currentMaxStoryId) + 1;
        }
        if (nextSeq > MAX_SUFFIX) {
            throw new IllegalStateException("该作者下小说序号已用尽");
        }
        return ap * 100_000L + nextSeq;
    }

    /** story_id 中低 5 位为小说序号（与 {@link #nextStoryId} 一致）。 */
    public static int storySequence(long storyId) {
        return (int) (Math.floorMod(storyId, 100_000L));
    }

    public static long paragraphId(long storyId, int chapterSeq) {
        if (chapterSeq < 1 || chapterSeq > MAX_SUFFIX) {
            throw new IllegalArgumentException("章节序号非法");
        }
        String s14 = String.format(Locale.US, "%014d", storyId);
        return Long.parseLong(s14 + String.format(Locale.US, "%05d", chapterSeq));
    }

    public static int nextChapterSequence(long storyId, Long currentMaxParagraphId) {
        if (currentMaxParagraphId == null || currentMaxParagraphId <= 0) {
            return 1;
        }
        String p19 = String.format(Locale.US, "%019d", currentMaxParagraphId);
        return Integer.parseInt(p19.substring(14)) + 1;
    }
}
