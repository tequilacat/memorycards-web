package org.tequilacat.memcard.server.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.tequilacat.memcard.server.bo.Card;

public interface CardRepository extends MongoRepository<Card, String> {

}
