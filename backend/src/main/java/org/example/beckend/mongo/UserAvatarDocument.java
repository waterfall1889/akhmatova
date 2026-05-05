package org.example.beckend.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 用户自定义头像（二进制）；主键为账号 ID 的字符串形式，与 PostgreSQL {@code user_information.id} 对应。
 */
@Document("user_avatars")
public class UserAvatarDocument {

    @Id
    private String userId;

    /** 如 {@code image/png}；缺省时接口按 {@code image/jpeg} 返回 */
    private String contentType;

    private byte[] image;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
