package org.tequilacat.repository;

import org.tequilacat.bo.Card;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CardRepository extends MongoRepository<Card, String> {

}
