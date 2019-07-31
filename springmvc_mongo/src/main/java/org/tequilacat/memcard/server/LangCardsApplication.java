package org.tequilacat.memcard.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = { LangCardsApplication.class })
public class LangCardsApplication {

  public static void main(String[] args) {
    SpringApplication.run(LangCardsApplication.class, args);
  }
}