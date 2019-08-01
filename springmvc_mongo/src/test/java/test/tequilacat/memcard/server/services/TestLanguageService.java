package test.tequilacat.memcard.server.services;

import static org.junit.jupiter.api.Assertions.assertEquals;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

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
import org.tequilacat.memcard.server.bo.ConfigOptions;
import org.tequilacat.memcard.server.bo.Language;
import org.tequilacat.memcard.server.repository.LanguageRepository;
import org.tequilacat.memcard.server.service.LanguageService;

import static org.tequilacat.memcard.server.utils.StreamUtils.*;

@DataMongoTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=TestDbConfig.class)
public class TestLanguageService {
  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(TestLanguageService.class);

  @Autowired private LanguageService languageService;

  @Autowired  private LanguageRepository languageRepository;
  
  @BeforeEach
  public void clearDb(@Autowired MongoTemplate mongoTemplate) {
    mongoTemplate.getCollection(mongoTemplate.getCollectionName(ConfigOptions.class)).drop();    
    mongoTemplate.getCollection(mongoTemplate.getCollectionName(Language.class)).drop();    
  }

  @Test  
  public void test_getCollection(@Autowired MongoTemplate mongoTemplate) {    
    assertEquals(0L, languageService.getCollection().countDocuments());
    
    var l = new Language();
    l.setCode("de");
    l.setGenericName("German");
    mongoTemplate.insert(l);
    
    assertEquals(1L, languageService.getCollection().countDocuments());
  }
  
  @Test  
  public void test_getAllLanguages(@Autowired MongoTemplate mongoTemplate) {    
    {
      var l = new Language();
      l.setCode("de");
      l.setGenericName("German");
      mongoTemplate.insert(l);
    }
    {
      var l = new Language();
      l.setCode("en");
      l.setGenericName("English");
      mongoTemplate.insert(l);
    }

    var allLangs = languageService.getAllLanguages();
    assertThat(allLangs, hasSize(2));

    assertEquals("English", findFirst(allLangs, l->l.getCode().equals("en")).getGenericName());
    assertEquals("German", findFirst(allLangs, l->l.getCode().equals("de")).getGenericName());    
  }
  
  @Test  
  public void test_find(@Autowired MongoTemplate mongoTemplate) {
    var l = new Language();
    l.setCode("de");
    l.setGenericName("German");
    var inserted = mongoTemplate.insert(l);
    
    var found = languageService.find("de");
    assertNotNull(found.getId());
    assertEquals(inserted.getId(), found.getId());
  } 
  
  @Test
  public void test_createLanguage(@Autowired MongoTemplate mongoTemplate) {
    languageService.createLanguage("en", "English", "English");
    var de = languageService.createLanguage("de", "German", "Deutsch");
    
    var all = languageRepository.findAll();
    var found = findFirst(all, l->l.getCode().equals("de"));
    assertEquals("German", found.getGenericName());
    assertEquals("Deutsch", found.getOwnName());
    assertEquals(de.getId(), found.getId());
  }
}
