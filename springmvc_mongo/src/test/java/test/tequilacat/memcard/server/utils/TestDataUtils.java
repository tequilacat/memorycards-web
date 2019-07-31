package test.tequilacat.memcard.server.utils;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.tequilacat.memcard.server.bo.Card;
import org.tequilacat.memcard.server.service.CardService;

public class TestDataUtils {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(TestDataUtils.class);
  
  public static void generateCards(CardService cardService, String spaceSeparatedWords) {
    var componentIds = spaceSeparatedWords.split("\\s+");
    Map<String,Card> firstCardsForWord = new HashMap<>();
    Set<String> usedIds = new HashSet<>();
    
    for(var wordText : componentIds) {
      if(usedIds.contains(wordText)) {
        throw new IllegalArgumentException("duplicate key "+wordText);
      }
      // split ID `
      if(wordText.length() < 2) {
        throw new IllegalArgumentException("key must be longer than 1 char but it is not: '"+wordText+"'");
      }
      
      String langId = wordText.substring(0, 1);
      String wordId = wordText.substring(1);      
      final Card newCard;
      
      if (firstCardsForWord.containsKey(wordId)) {
        newCard = cardService.createTranslation(wordText, "", langId, firstCardsForWord.get(wordId));
      } else {
        newCard = cardService.createCard(wordText, "", langId);
        firstCardsForWord.put(wordId, newCard);
      }
      
      usedIds.add(wordText);
    }
  }
  
  public static Writer dumpCollectionJson(MongoTemplate mongoTemplate, Class<?> entityClass, Writer out) {
    var pwr = new PrintWriter(out);
    mongoTemplate.findAll(Document.class, mongoTemplate.getCollectionName(entityClass))
      .forEach(e -> pwr.println(e.toJson()));
    return out;
  }
}
