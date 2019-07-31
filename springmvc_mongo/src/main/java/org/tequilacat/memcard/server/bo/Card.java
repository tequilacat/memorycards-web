package org.tequilacat.memcard.server.bo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Card {

  @Id private String id;
  
  @NotBlank
  private String text;
  
  @NotNull  
  @Size(min = 2, max = 2)
  private String languageId;
  
  private String description;
  
  // same ID for all translations of same word
  @NotNull
  private ObjectId wordIdentity;
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getLanguageId() {
    return languageId;
  }

  public void setLanguageId(String languageId) {
    this.languageId = languageId;
  }

  public ObjectId getWordIdentity() {
    return wordIdentity;
  }

  public void setWordIdentity(ObjectId wordIdentity) {
    this.wordIdentity = wordIdentity;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
