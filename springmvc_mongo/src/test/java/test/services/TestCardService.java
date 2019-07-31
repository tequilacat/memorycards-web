package test.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.tequilacat.bo.Card;
import org.tequilacat.bo.ConfigOptions;
import org.tequilacat.service.CardService;
import org.tequilacat.utils.StreamUtils;

import test.utils.TestDataUtils;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat; 

@DataMongoTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=TestDbConfig.class)
public class TestCardService {
  private static final Logger log = LoggerFactory.getLogger(TestCardService.class);
  
  @Autowired private CardService cardService;
  
  @BeforeEach
  public void clearDb(@Autowired MongoTemplate mongoTemplate) {
    mongoTemplate.getCollection(mongoTemplate.getCollectionName(ConfigOptions.class)).drop();    
    mongoTemplate.getCollection(mongoTemplate.getCollectionName(Card.class)).drop();    
  }
  
  @Test
  public void testCreateCard(@Autowired MongoTemplate mongoTemplate) {
    var newCard = cardService.createCard("one", "a number", "en");
    
    var allCards = mongoTemplate.findAll(Card.class);
    var primary = allCards.stream().filter(s -> s.getText().equals("one")).findFirst().get();
    assertEquals(newCard.getId(), primary.getId());
    assertEquals("en", primary.getLanguageId());
    assertEquals("a number", primary.getDescription());
    assertNotNull(primary.getWordIdentity());
  }
  
  @Test
  public void testCreateTranslation(@Autowired MongoTemplate mongoTemplate) {
    var newEnCard = cardService.createCard("one", "a number", "en");
    
    var newFrCard = cardService.createTranslation("une", "une numero", "fr", newEnCard);
    
    var allCards = mongoTemplate.findAll(Card.class);
    var translatedFromDb = allCards.stream().filter(s -> s.getText().equals("une")).findFirst().get();
    assertEquals(newFrCard.getId(), translatedFromDb.getId());
    assertEquals("fr", translatedFromDb.getLanguageId());
    assertEquals("une numero", translatedFromDb.getDescription());
    assertEquals(newEnCard.getWordIdentity(), newFrCard.getWordIdentity());
  }
  
  
  @Test
  public void testRemoveCard(@Autowired MongoTemplate mongoTemplate) {
    var newCard = cardService.createCard("one", "a number", "en");
    var foundCard = mongoTemplate.findById(newCard.getId(), Card.class);
    
    assertNotNull(foundCard);
    
    cardService.removeCard(newCard.getId());
    
    assertNull(mongoTemplate.findById(newCard.getId(), Card.class));    
  }
  
  @Test
  public void testGetPerLanguage() {
    var card1 = cardService.createCard("one", "a number", "en");
    cardService.createTranslation("ein", "1", "de", card1);
    cardService.createTranslation("une", "descr", "fr", card1);
    cardService.createTranslation("uno", "descr", "es", card1);
    
    var card2 = cardService.createCard("two", "2", "en");
    cardService.createTranslation("zwei", "the 2", "de", card2); 
    
    var list = cardService.getCardsPerLang();
    assertThat(StreamUtils.mapToList(list, l->l.getLanguageId()), 
        containsInAnyOrder("en", "de", "es", "fr"));
    
    // en has 2 
    
    assertThat(StreamUtils.mapToList(
        StreamUtils.findFirst(list, l->l.getLanguageId().equals("en")).getCards(), 
        c->c.getText()), containsInAnyOrder("one", "two"));
    
    assertThat(StreamUtils.mapToList(
        StreamUtils.findFirst(list, l->l.getLanguageId().equals("de")).getCards(), 
        c->c.getText()), containsInAnyOrder("ein", "zwei"));
    
    assertThat(StreamUtils.mapToList(
        StreamUtils.findFirst(list, l->l.getLanguageId().equals("fr")).getCards(), 
        c->c.getText()), containsInAnyOrder("une"));
    
    assertThat(StreamUtils.mapToList(
        StreamUtils.findFirst(list, l->l.getLanguageId().equals("es")).getCards(), 
        c->c.getText()), containsInAnyOrder("uno"));
  }
  
  //  2 related cards of same language should not cause crashes
  @Test 
  public void testNoAddSameLanguage() {
    var card1 = cardService.createCard("one", "a number", "en");
    var ex = assertThrows(IllegalArgumentException.class,
        () -> cardService.createTranslation("one1", "a number", "en", card1));
    assertTrue(ex.getMessage().contains("Same"));
  }
  
  /**
   * the groups should be ordered by last used languages including edge cases
   *  - no cards, one card, 2 unrelated cards should not cause crashes, 
   */
  @Test
  public void testLastUsedLanguagePair_EdgeCases(@Autowired MongoTemplate mongoTemplate) {
    assertThat(cardService.getCardsPerLang(), is(empty()));
    
    {
      var card1 = cardService.createCard("one", "a number", "en");
      assertThat(StreamUtils.mapToList(cardService.getCardsPerLang(), s -> s.getLanguageId()), 
          containsInAnyOrder("en"));
      
      var card2 = cardService.createCard("un mot", "not a num", "fr");
      // just check that unrelated words don't crash algo
      assertThat(StreamUtils.mapToList(cardService.getCardsPerLang(), s -> s.getLanguageId()), 
          containsInAnyOrder("en", "fr"));      
    }
    
    { 
      clearDb(mongoTemplate);
      // same lang unrelated 
      var card1 = cardService.createCard("one", "a number", "en");
      var card2 = cardService.createCard("two", "a number", "en");
      var langs = cardService.getCardsPerLang();
      assertThat(StreamUtils.mapToList(langs, s -> s.getLanguageId()), 
          containsInAnyOrder("en"));
      assertThat(langs.get(0).getCards(), hasSize(2));
    }
  }

  @Test
  public void testLastTranslationLanguages(@Autowired MongoTemplate mongoTemplate) {
    // none
    assertThat(mongoTemplate.findAll(ConfigOptions.class), hasSize(0));
    
    var e1 = cardService.createCard("e1", "", "en");
    assertThat(mongoTemplate.findAll(ConfigOptions.class), hasSize(0));
    
    var f1 = cardService.createCard("f1", "", "fr");
    // same - no translations added yet
    assertThat(mongoTemplate.findAll(ConfigOptions.class), hasSize(0));
    
    
    // add first time
    {
      cardService.createTranslation("d1", "", "de", e1);

      var configs = mongoTemplate.findAll(ConfigOptions.class);
      assertThat(configs, hasSize(1));
      assertEquals("en", configs.get(0).getLastSourceLanguage());
      assertEquals("de", configs.get(0).getLastTranslationLanguage());
    }
    
    // change first and last translation language 
    {
      cardService.createTranslation("esp1", "", "es", f1);

      var configs = mongoTemplate.findAll(ConfigOptions.class);
      assertThat(configs, hasSize(1));
      assertEquals("fr", configs.get(0).getLastSourceLanguage());
      assertEquals("es", configs.get(0).getLastTranslationLanguage());
    }
  }

  @Test
  public void testLastUsedLanguagePair_dataset(@Autowired MongoTemplate mongoTemplate) {
    // first 2 groups contain the langs of pair added to the translation
    // langs are a b c
    TestDataUtils.generateCards(cardService,"a1 a2 a3 b1 b2 b3 c1 c2 c3 f1");
 
    // last added word is c3, means first is a3, should be  a c b
    var cardsPerLang = cardService.getCardsPerLang();
    var langs = StreamUtils.mapToList(cardsPerLang, s -> s.getLanguageId());
    assertThat(langs, hasSize(4));
    assertEquals("a", langs.get(0));
    assertEquals("f", langs.get(1));
    //assertThat(langs, contains("a", "f", "c", "b"));
        //contains("b", "c", "a"));
  }
  
  
  
  @Test
  public void testCreateCardsSet(@Autowired MongoTemplate mongoTemplate) {
    TestDataUtils.generateCards(cardService,"e1 e2 d1 d2 f1");
    
    log.info("Collection:\n{}", TestDataUtils.dumpCollectionJson(mongoTemplate, Card.class, new StringWriter()).toString());
  }
  
}