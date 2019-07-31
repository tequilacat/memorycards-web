package org.tequilacat.memcard.server.bo;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ConfigOptions {

  private String lastSourceLanguage;
  private String lastTranslationLanguage;

  public String getLastTranslationLanguage() {
    return lastTranslationLanguage;
  }

  public void setLastTranslationLanguage(String lastTranslationLanguage) {
    this.lastTranslationLanguage = lastTranslationLanguage;
  }

  public String getLastSourceLanguage() {
    return lastSourceLanguage;
  }

  public void setLastSourceLanguage(String lastSourceLanguage) {
    this.lastSourceLanguage = lastSourceLanguage;
  }
}
