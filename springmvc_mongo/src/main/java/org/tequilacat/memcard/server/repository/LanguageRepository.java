package org.tequilacat.memcard.server.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.tequilacat.memcard.server.bo.Language;

public interface LanguageRepository extends MongoRepository<Language, String> {
  Language findByCode(String code);
  //List<Language> getByCardsWordIdentity(ObjectId wordIdentity);
  List<Language> getByCardsCardId(ObjectId wordIdentity);
  
  @Query("{'cards.cardId': ?0}")
  List<Language> getLanguageOfCard(ObjectId cardId);  
}
