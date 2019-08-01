package org.tequilacat.memcard.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.tequilacat.memcard.server.bo.Card;
import org.tequilacat.memcard.server.bo.ConfigOptions;
import org.tequilacat.memcard.server.bo.Language;
import org.tequilacat.memcard.server.repository.LanguageRepository;
import org.tequilacat.memcard.server.utils.NotImplementedException;
import org.tequilacat.memcard.server.utils.StreamUtils;

@Service
public class CardService {

  private static final Logger log = LoggerFactory.getLogger(CardService.class);
  
  // @Autowired CardRepository cardRepository;
  @Autowired LanguageRepository languageRepository;
  @Autowired LanguageService languageService;
  
  @Autowired private MongoTemplate mongoTemplate;
  
  public List<Card> getAllCards() {
    throw new NotImplementedException();
  }

  
  public Card createCard(String text, String description, Language language) {
    return createCardInternal(text, description, language, null);
  }
  
  public Card createTranslation(String text, String description, Language language, Card sourceCard) {
    return createCardInternal(text, description, language, sourceCard);
  }
  
  private Card createCardInternal(String text, String description, Language language, Card sourceCard) {
    final Language sourceLanguage;
    
    if(sourceCard == null) {
      log.debug("Create new card {} [{}]", text, language.getCode());      
      sourceLanguage = null;
    } else {
      var found = languageRepository.getByCardsCardId(sourceCard.getCardId());
      if(found.size() == 0) {
        throw new IllegalArgumentException("No card found for card ID '" + sourceCard.getCardId() + "'");
      }
      
      sourceLanguage = found.get(0);
      
      if(sourceLanguage.getId().contentEquals(language.getId())) {
        throw new IllegalArgumentException("Same language ID for linked translations: '" + sourceLanguage.getCode() + "'");
      }
      
      log.debug("Add translation {} [{}] of card {} [{}]", text, language.getCode(), sourceCard.getText(), sourceLanguage.getCode());
    }
    
    var card = new Card();
    card.setText(text);
    card.setWordIdentity(sourceCard == null ? new ObjectId() : sourceCard.getWordIdentity());
    card.setDescription(description);
    card.setCardId(new ObjectId());
    
    if (sourceCard != null) {
      Update update = new Update();
      update.set("lastSourceLanguage", sourceLanguage.getCode());
      update.set("lastTranslationLanguage", language.getCode());
      mongoTemplate.upsert(new Query(), update, ConfigOptions.class);
    }
    
    language.getCards().add(card);  
    languageRepository.save(language);
    return card;
  }
  
  public void removeCard(String cardId) {
    var langAndCard = languageService.getCardInLanguage(cardId);
    
    if (langAndCard == null) {
      throw new IllegalArgumentException("No card found for cardId='" + cardId + "'");
    }
    
    mongoTemplate.updateMulti(new Query(),
        new Update().pull("cards", 
            Query.query(Criteria.where("cardId").is(new ObjectId(cardId)))), 
        Language.class);
  }
  
  public static class CardLanguage {
    private Language language;
    
    public CardLanguage(Language language) {
      this.language = language; 
    }
    
    public String getLanguageId() {
      return language.getCode();
    }

    public List<Card> getCards() {
      return language.getCards();
    }
  }
  
  public List<CardLanguage> getCardsPerLang() {
    List<CardLanguage> languageCards = new ArrayList<>();

    // TODO consider smaller data amount to be requested and projected, not findAll 

    languageRepository.findAll().forEach(l -> languageCards.add(new CardLanguage(l)));
    
    var config = mongoTemplate.findOne(new Query(), ConfigOptions.class);
    
    if (config != null && config.getLastSourceLanguage() != null && config.getLastTranslationLanguage() != null) {
      var lastSourceGrp = StreamUtils.findFirst(languageCards,
          lc -> Objects.equals(lc.getLanguageId(), config.getLastSourceLanguage()));
      var lastTranslationGroup = StreamUtils.findFirst(languageCards,
          lc -> Objects.equals(lc.getLanguageId(), config.getLastTranslationLanguage()));
      
      if (lastSourceGrp!= null && lastTranslationGroup != null
          && !lastSourceGrp.equals(lastTranslationGroup)) {
        
        languageCards.remove(lastSourceGrp);
        languageCards.remove(lastTranslationGroup);
        languageCards.add(0, lastTranslationGroup);
        languageCards.add(0, lastSourceGrp);
      }
    }
    
    return languageCards;
  }
}
