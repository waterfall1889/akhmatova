package org.example.beckend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "org.example.beckend.mongo")
public class BeckendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeckendApplication.class, args);
    }

}
