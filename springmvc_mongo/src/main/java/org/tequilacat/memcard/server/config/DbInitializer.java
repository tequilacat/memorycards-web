package org.tequilacat.memcard.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.tequilacat.memcard.server.service.CardService;
import org.tequilacat.memcard.server.service.LanguageService;

@Component
public class DbInitializer implements CommandLineRunner {

  @Autowired CardService cardService;
  @Autowired LanguageService languageService;
  
  @Override
  public void run(String... args) throws Exception {
    initDb();
  }

  private void initDb() {
    if(languageService.getAllLanguages().isEmpty()) {
      var german = languageService.createLanguage("de", "German", "Deutsch");
      var english = languageService.createLanguage("en", "English", "English");
      var french = languageService.createLanguage("fr", "French", "Francaise");
      
      var card1 = cardService.createCard("one", "a number", english);
      cardService.createTranslation("ein", "1", german, card1);
      cardService.createTranslation("une", "descr", french, card1);
      
      var card2 = cardService.createCard("two", "2", english);
      cardService.createTranslation("zwei", "the 2", german, card2); 
    }
  }
}
