package org.example.beckend.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserAvatarMongoRepository extends MongoRepository<UserAvatarDocument, String> {
}
