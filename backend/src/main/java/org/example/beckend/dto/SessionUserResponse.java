package org.example.beckend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** 会话用户信息；字段名与前端约定一致（避免全局命名策略导致 snake_case 等）。 */
public record SessionUserResponse(
        @JsonProperty("id") long id,
        @JsonProperty("userName") String userName,
        @JsonProperty("userEmail") String userEmail) {
}
