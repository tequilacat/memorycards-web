package test.tequilacat.memcard.server.services;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "org.tequilacat.memcard.server.repository")
@ComponentScan({"org.tequilacat.memcard.server.bo", "org.tequilacat.memcard.server.service"})
public class TestDbConfig {

}
