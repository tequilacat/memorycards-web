package org.tequilacat.memcard.server.bo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class Card {

  @NotBlank
  private String text;
  
  private String description;
  
  // same ID for all translations of same word
  @NotNull
  private ObjectId wordIdentity;
  
  @NotNull
  private ObjectId cardId;
}
