-- 已有库：为 story_paragragh 增加章节顺位列 index（从 1 起），与后端实体 chapterOrder 对应。
-- 在 psql 中按需执行（新建库可直接用根目录 schema.sql 中的完整建表语句）。

ALTER TABLE story_paragragh ADD COLUMN IF NOT EXISTS index integer;

UPDATE story_paragragh sp
SET index = sub.rn
FROM (
    SELECT paragraph_id,
           ROW_NUMBER() OVER (PARTITION BY story_id ORDER BY paragraph_id) AS rn
    FROM story_paragragh
) AS sub
WHERE sp.paragraph_id = sub.paragraph_id
  AND (sp.index IS NULL OR sp.index < 1);

ALTER TABLE story_paragragh ALTER COLUMN index SET NOT NULL;
