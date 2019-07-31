package test.services;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "org.tequilacat.repository")
@ComponentScan({"org.tequilacat.bo", "org.tequilacat.service"})
public class TestDbConfig {

}
