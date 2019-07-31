package org.tequilacat.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.tequilacat.bo.Card;
import org.tequilacat.bo.ConfigOptions;
import org.tequilacat.repository.CardRepository;
import org.tequilacat.utils.NotImplementedException;
import org.tequilacat.utils.StreamUtils;

import com.mongodb.DBObject;

import org.slf4j.Logger;

@Service
public class CardService {

  private static final Logger log = LoggerFactory.getLogger(CardService.class);
  
  @Autowired CardRepository cardRepository;
  
  @Autowired private MongoTemplate mongoTemplate;
  
  public List<Card> getAllCards() {
    throw new NotImplementedException();
  }

  
  public Card createCard(String text, String description, String langCode) {
    log.debug("Create new card {} [{}]", text, langCode);
    return createCardInternal(text, description, langCode, null);
  }
  
  public Card createTranslation(String text, String description, String langCode, Card sourceCard) {
    log.debug("Add translation {} [{}] of card {} [{}]", text, langCode, sourceCard.getText(), sourceCard.getLanguageId());
    return createCardInternal(text, description, langCode, sourceCard);
  }
  
  private Card createCardInternal(String text, String description, String langCode, Card sourceCard) {
    if (sourceCard != null && Objects.equals(sourceCard.getLanguageId(), langCode)) {
      throw new IllegalArgumentException("Same language ID for linked translations: '" + langCode + "'");
    }

    var card = new Card();
    card.setText(text);
    card.setLanguageId(langCode);
    card.setWordIdentity(sourceCard == null ? new ObjectId() : sourceCard.getWordIdentity());
    card.setDescription(description);
    
    if (sourceCard != null) {
      Update update = new Update();
      update.set("lastSourceLanguage", sourceCard.getLanguageId());
      update.set("lastTranslationLanguage", langCode);
      mongoTemplate.upsert(new Query(), update, ConfigOptions.class);
    }
    
    return cardRepository.insert(card);
  }
  
  public void removeCard(String cardId) {
    cardRepository.deleteById(cardId);
  }
  
  public static class CardLanguage {
    @Id private String languageId;

    private List<Card> cards = new ArrayList<>();
    
    public String getLanguageId() {
      return languageId;
    }

    public void setLanguageId(String languageId) {
      this.languageId = languageId;
    }

    public List<Card> getCards() {
      return cards;
    }

    public void setCards(List<Card> cards) {
      this.cards = cards;
    }
  }
  
  public List<CardLanguage> getCardsPerLang() {
    var group = Aggregation.group("languageId").push("$$ROOT").as("cards");    
    Aggregation aggregation = Aggregation.newAggregation(group);
    AggregationResults<CardLanguage> result = mongoTemplate.aggregate(aggregation, Card.class, CardLanguage.class);
    
    // if prevously stored last used languages in config, swap them
    var languageCards = StreamUtils.mapToList(result, r -> r);
    var config = mongoTemplate.findOne(new Query(), ConfigOptions.class);
    
    if (config != null && config.getLastSourceLanguage() != null && config.getLastTranslationLanguage() != null) {
      var lastSourceGrp = StreamUtils.findFirst(languageCards,
          lc -> Objects.equals(lc.getLanguageId(), config.getLastSourceLanguage()));
      var lastTranslationGroup = StreamUtils.findFirst(languageCards,
          lc -> Objects.equals(lc.getLanguageId(), config.getLastTranslationLanguage()));
      
//      var lastSourceGrp = StreamUtils.findFirstIndexOf(languageCards,
//          lc -> Objects.equals(lc.getLanguageId(), config.getLastSourceLanguage()));
//      var lastTranslationGroup = StreamUtils.findFirstIndexOf(languageCards,
//          lc -> Objects.equals(lc.getLanguageId(), config.getLastTranslationLanguage()));
      
      if (lastSourceGrp!= null && lastTranslationGroup != null
          && !lastSourceGrp.equals(lastTranslationGroup)) {
        List<CardLanguage> newList = new ArrayList<>();
        newList.add(lastSourceGrp);
        newList.add(lastTranslationGroup);
        languageCards.stream().filter(lc -> lc != lastTranslationGroup && lc != lastSourceGrp).forEach(newList::add);
        languageCards = newList;
        // swap
        //Collections.swap(languageCards, lastSourceGrp.get(), lastTranslationGroup.get());
      }
    }
    
    return languageCards;
  }
}
