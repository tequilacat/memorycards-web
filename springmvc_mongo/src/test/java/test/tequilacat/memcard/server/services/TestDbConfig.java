package test.tequilacat.memcard.server.services;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.MongoClient;

@Configuration
@EnableMongoRepositories(basePackages = "org.tequilacat.memcard.server.repository")
@ComponentScan({"org.tequilacat.memcard.server.bo", "org.tequilacat.memcard.server.service"})
public class TestDbConfig {
  @Bean
  public MongoClient mongo() {
    return new MongoClient("localhost");
  }

  @Bean
  public MongoTemplate mongoTemplate() throws Exception {
    return new MongoTemplate(mongo(), "unittest");
  }
}
