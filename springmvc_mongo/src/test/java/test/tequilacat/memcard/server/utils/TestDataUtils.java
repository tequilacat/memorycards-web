package test.tequilacat.memcard.server.utils;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.tequilacat.memcard.server.bo.Card;
import org.tequilacat.memcard.server.bo.Language;
import org.tequilacat.memcard.server.service.CardService;
import org.tequilacat.memcard.server.service.LanguageService;
import org.tequilacat.memcard.server.utils.StreamUtils;

public class TestDataUtils {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(TestDataUtils.class);
  
  public static Map<String,Language> createLanguageMap(LanguageService langService, 
      String packedLanguageIds) {
    return createLanguages(langService, packedLanguageIds).stream().collect(Collectors.toMap(
        l -> l.getCode(), l -> l));
  }
  
  public static List<Language> createLanguages(LanguageService langService, 
      String packedLanguageIds) {
    
    return Stream.of(packedLanguageIds.split("\\s+"))
      .map(langId -> langService.createLanguage(langId, "The " + langId, "Local " + langId))
      .collect(Collectors.toList());
  }
  
  public static void generateCards(CardService cardService, LanguageService langService, String spaceSeparatedWords) {
    var componentIds = spaceSeparatedWords.split("\\s+");
    Map<String,Card> firstCardsForWord = new HashMap<>();
    Set<String> usedIds = new HashSet<>();
    Map<String, Language> languages = StreamUtils.toMap(langService.getAllLanguages(), l->l.getCode(),l->l);
    
    for(var wordText : componentIds) {
      if(usedIds.contains(wordText)) {
        throw new IllegalArgumentException("duplicate key "+wordText);
      }
      // split ID `
      if(wordText.length() < 2) {
        throw new IllegalArgumentException("key must be longer than 1 char but it is not: '"+wordText+"'");
      }
      
      final String langId, wordId;
      int pos = wordText.indexOf('.');

      if (pos == -1) {
        langId = wordText.substring(0, 1);
        wordId = wordText.substring(1);
      } else {
        langId = wordText.substring(0, pos);
        wordId = wordText.substring(pos + 1);
      }
      
      Language language;
      
      if(languages.containsKey(langId)) {
        language = languages.get(langId);
      }else {
        language = langService.createLanguage(langId, langId, langId);
        languages.put(langId, language);
        
      }
            
      final Card newCard;
      
      if (firstCardsForWord.containsKey(wordId)) {
        newCard = cardService.createTranslation(wordText, "", language.getId(), firstCardsForWord.get(wordId).getCardId());
      } else {
        newCard = cardService.createCard(wordText, "", language.getId());
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
