package org.tequilacat.memcard.server.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.tequilacat.memcard.server.bo.Language;
import org.tequilacat.memcard.server.repository.LanguageRepository;

import com.mongodb.client.MongoCollection;

@Service
public class LanguageService {
  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(LanguageService.class);
  @Autowired  private LanguageRepository languageRepository;
  
  @Autowired MongoTemplate mongoTemplate;
  
  public MongoCollection<Document> getCollection() {
    return mongoTemplate.getCollection(mongoTemplate.getCollectionName(Language.class));
  }
  
  public Language find(String code) {
    return languageRepository.findByCode(code);
  }
  
  public List<Language> getAllLanguages() {
    return languageRepository.findAll();
  } 
  
  public Language createLanguage(String code, String genericLabel, String ownName) {
    var l = new Language();
    l.setCode(code);
    l.setGenericName(genericLabel);
    l.setOwnName(ownName);
    return mongoTemplate.insert(l);
  }

  private static final AggregationOperation reunwindOp;
  
  static {
    var reunwindOpDocument = Document.parse("{ $addFields: { cards: { $concatArrays: [ [\"$cards\"]] } } }");
    reunwindOp = new AggregationOperation() {
      @Override
      public Document toDocument(AggregationOperationContext aoc) {
        return reunwindOpDocument;
      }
    };    
  }
  
  public Language getCardInLanguage(String cardId) {
    // db.language.aggregate({ "$match" : { "cards.cardId" : ObjectId( "5d46232a9f72b22a7c934b8a" ) }} ,
    //      { "$unwind" : "$cards"}, { "$match" : { "cards.cardId" : ObjectId( "5d46232a9f72b22a7c934b8a" ) }}, 
    //      { $addFields: { cards: { $concatArrays: [ ["$cards"]] } } } ).pretty()
    
    var match = Aggregation.match(Criteria.where("cards.cardId").is(new ObjectId(cardId)));
    var unwind = Aggregation.unwind("cards");
    var aggregation = Aggregation.newAggregation(match, unwind, match, reunwindOp);    
    var foundLangs = StreamSupport.stream(
        mongoTemplate.aggregate(aggregation, Language.class, Language.class).spliterator(), false)
        .collect(Collectors.toList());
    return foundLangs.size() == 1 && foundLangs.get(0).getCards().size() == 1 ?
        foundLangs.get(0) : null;
  }
}
