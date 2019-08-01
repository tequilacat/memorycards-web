package org.tequilacat.memcard.server.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class ConfigOptions {

  private String lastSourceLanguage;
  private String lastTranslationLanguage;
}
