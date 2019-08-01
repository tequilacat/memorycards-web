package test.tequilacat.memcard.server.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.StringWriter;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
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
import org.tequilacat.memcard.server.bo.Card;
import org.tequilacat.memcard.server.bo.ConfigOptions;
import org.tequilacat.memcard.server.bo.Language;
//import org.tequilacat.memcard.server.repository.LanguageRepository;
import org.tequilacat.memcard.server.service.CardService;
import org.tequilacat.memcard.server.service.LanguageService;
import org.tequilacat.memcard.server.utils.StreamUtils;

import test.tequilacat.memcard.server.utils.TestDataUtils; 

import static org.tequilacat.memcard.server.utils.StreamUtils.*;

@DataMongoTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=TestDbConfig.class)
public class TestCardService {
  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(TestCardService.class);
  
  @Autowired private CardService cardService;

  @Autowired private LanguageService languageService;
  
  //@Autowired private LanguageRepository languageRepository;

  @BeforeEach
  public void clearDb(@Autowired MongoTemplate mongoTemplate) {
    mongoTemplate.getCollection(mongoTemplate.getCollectionName(ConfigOptions.class)).drop();    
    mongoTemplate.getCollection(mongoTemplate.getCollectionName(Language.class)).drop();    
  }
  
  
  @Test
  public void test_createCard(@Autowired MongoTemplate mongoTemplate) {
    var en = languageService.createLanguage("en", "English", "English");
    
    cardService.createCard("one", "a number", en);
    
    
    // get from languages - imply it works 
    var allCards = languageService.find("en").getCards();
    var primary = allCards.stream().filter(s -> s.getText().equals("one")).findFirst().get();
    //assertEquals(newCard.getId(), primary.getId());
    //assertEquals("en", primary.getLanguageId());
    assertEquals("a number", primary.getDescription());
    assertNotNull(primary.getWordIdentity());
  }

  @Test 
  public void test_createTranslation_samelanguage() {
    var en = languageService.createLanguage("en", "English", "English");

    var card1 = cardService.createCard("one", "a number", en);
    var ex = assertThrows(IllegalArgumentException.class,
        () -> cardService.createTranslation("one1", "a number", en, card1));
    assertTrue(ex.getMessage().contains("Same"));
  }

  @Test 
  public void test_createTranslation_badId() {
    var en = languageService.createLanguage("en", "English", "English");
    var card1 = cardService.createCard("one", "a number", en);
    var fake = new Card();
    fake.setText("nosuch");
    fake.setWordIdentity(new ObjectId()); // new id wihch does not exist
    
    var ex = assertThrows(IllegalArgumentException.class,
        () -> cardService.createTranslation("one1", "a number", en, card1));
    assertTrue(ex.getMessage().contains("Same"));
  }

  @Test
  public void test_createTranslation(@Autowired MongoTemplate mongoTemplate) {
    var en = languageService.createLanguage("en", "English", "English");
    var fr = languageService.createLanguage("fr", "French", "Francaise");
    
    var newEnCard = cardService.createCard("one", "a number", en);
    
    cardService.createTranslation("une", "une numero", fr, newEnCard);
    
    var foundEnCard = languageService.find("en").getCards().stream().filter(s -> s.getText().equals("one")).findFirst().get();
    var foundFrCard = languageService.find("fr").getCards().stream().filter(s -> s.getText().equals("une")).findFirst().get();
    
    assertEquals("une numero", foundFrCard.getDescription());
    assertEquals(newEnCard.getWordIdentity(), foundFrCard.getWordIdentity());
    assertEquals(foundEnCard.getWordIdentity(), foundFrCard.getWordIdentity());
  }
  
  @Test
  public void test_retrieveCardById(@Autowired MongoTemplate mongoTemplate) {
    // langs: a, b
    TestDataUtils.generateCards(cardService, languageService, "a1 a2 a3 b1 b2 b3");
    var all = languageService.getAllLanguages();
    var srcLangB = findFirst(all, l->l.getCode().equals("b"));
    var b2 = findFirst(srcLangB.getCards(), c->c.getText().equals("b2"));

    // no such card in DB
    assertNull(languageService.getCardInLanguage(new ObjectId().toHexString()));    
    
    // check we found single language with single card
    var foundCardLang = languageService.getCardInLanguage(b2.getCardId().toHexString());
    assertEquals(srcLangB.getId(), foundCardLang.getId());    
    assertThat(foundCardLang.getCards(), hasSize(1));    
    assertEquals(b2.getCardId(), foundCardLang.getCards().get(0).getCardId());
  }

  @Test
  public void test_removeCard(@Autowired MongoTemplate mongoTemplate) {
    TestDataUtils.generateCards(cardService, languageService, 
        "a1 a2 a3 b1 b2 b3 c1 c2 c3");
    
    var b1card = findFirst(findFirst(cardService.getCardsPerLang(), l->l.getLanguageId().equals("b")).getCards(),
      c->c.getText().contentEquals("b1"));
    cardService.removeCard(b1card.getCardId().toHexString());
    
    // check we don't have b1 anymore    
    assertThat(languageService.getAllLanguages().stream().flatMap(l -> l.getCards().stream())
        .map(c -> c.getText()).collect(Collectors.toList()), 
        containsInAnyOrder("a1", "a2", "a3", "b2", "b3", "c1", "c2", "c3"));
    
    // remove twice - does not exist
    assertThrows(IllegalArgumentException.class,
        () -> cardService.removeCard(b1card.getCardId().toHexString()));   
  }
  
  @Test
  public void test_getCardsPerLang() {
    var en = languageService.createLanguage("en", "English", "English");
    var fr = languageService.createLanguage("fr", "French", "Francaise");
    var es = languageService.createLanguage("es", "es", "es");
    var de = languageService.createLanguage("de", "de", "de");
    
    var card1 = cardService.createCard("one", "a number", en);
    cardService.createTranslation("ein", "1", de, card1);
    cardService.createTranslation("une", "descr", fr, card1);
    cardService.createTranslation("uno", "descr", es, card1);
    
    var card2 = cardService.createCard("two", "2", en);
    cardService.createTranslation("zwei", "the 2", de, card2); 
    
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
  
  @Test
  public void test_storeLastTranslationLanguages(@Autowired MongoTemplate mongoTemplate) {
    // TestDataUtils.generateCards(cardService, languageService, "en.w1 fr.w1");
    var langs = TestDataUtils.createLanguageMap(languageService, "en fr es de");
    var e1 = cardService.createCard("e1", "", langs.get("en"));
    // add first card does not add anything 
    assertThat(mongoTemplate.findAll(ConfigOptions.class), hasSize(0));
    
    // add another non-linked card does not add config options
    cardService.createCard("f2", "", langs.get("fr"));
    assertThat(mongoTemplate.findAll(ConfigOptions.class), hasSize(0));
    
    // add translation - remember the language pair
    var f1 = cardService.createTranslation("f1", "", langs.get("fr"), e1);

    var configs = mongoTemplate.findAll(ConfigOptions.class);
    assertThat(configs, hasSize(1));
    assertEquals("en", configs.get(0).getLastSourceLanguage());
    assertEquals("fr", configs.get(0).getLastTranslationLanguage());
    
    // test query that correctly finds language of provided source card:
    // add translation from word that was already a translation (not first in result)
    // must store fr->de
    cardService.createTranslation("de1", "", langs.get("de"), f1);
    
    configs = mongoTemplate.findAll(ConfigOptions.class);
    assertThat(configs, hasSize(1));
    assertEquals("fr", configs.get(0).getLastSourceLanguage());
    assertEquals("de", configs.get(0).getLastTranslationLanguage());
  }
  
  //@Test
  public void testCreateCardsSet(@Autowired MongoTemplate mongoTemplate) {
    TestDataUtils.generateCards(cardService, languageService, "e1 e2 d1 d2 f1");
    
    log.info("Collection:\n{}", TestDataUtils.dumpCollectionJson(mongoTemplate, Card.class, new StringWriter()).toString());
  }
  
  @Test
  public void cardsReturnedInCreationOrder(@Autowired MongoTemplate mongoTemplate) {
    
    TestDataUtils.generateCards(cardService, languageService, "a1 a2 a3 b2 b3 b1 c3 c1 c2 f1");
    
    // last added word is c3, means first is a3, should be  a c b
    var cardsPerLang = cardService.getCardsPerLang();

    assertThat(mapToList(findFirst(cardsPerLang, cl -> "a".equals(cl.getLanguageId())).getCards(), c -> c.getText()),
        contains("a3", "a2", "a1"));    
  }
}