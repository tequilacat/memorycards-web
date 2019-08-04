package org.tequilacat.memcard.server.bo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class ConfigOptions {

  private ObjectId lastSourceLanguage;
  private ObjectId lastTranslationLanguage;
}
