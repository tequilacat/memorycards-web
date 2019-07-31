package org.tequilacat.memcard.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.tequilacat.memcard.server.bo.Card;
import org.tequilacat.memcard.server.service.CardService;

@Component
public class DbInitializer implements CommandLineRunner {

  @Autowired private MongoTemplate mongoTemplate;
  @Autowired CardService cardService;
  
  @Override
  public void run(String... args) throws Exception {
    initDb();
  }

  private void initDb() {
    // if initial does not exist create default filling
    if(!mongoTemplate.collectionExists(Card.class)) {
      var card1 = cardService.createCard("one", "a number", "en");
      cardService.createTranslation("ein", "1", "de", card1);
      cardService.createTranslation("une", "descr", "fr", card1);
      cardService.createTranslation("uno", "descr", "es", card1);
      
      var card2 = cardService.createCard("two", "2", "en");
      cardService.createTranslation("zwei", "the 2", "de", card2); 
    }
  }
}
