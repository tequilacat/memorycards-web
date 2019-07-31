package org.tequilacat.controller;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class NewCardForm {
  @NotBlank
  private String originalText;
  
  private String originalDescription;
  
  @NotNull
  @Size(min=2, max=2)
  private String originalLanguageId;
  
  @NotBlank
  private String translatedText;
  
  private String translatedDescription;
  
  @NotNull
  @Size(min=2, max=2)
  private String translatedLanguageId;
  
  public String getOriginalText() {
    return originalText;
  }
  public void setOriginalText(String originalText) {
    this.originalText = originalText;
  }
  public String getOriginalDescription() {
    return originalDescription;
  }
  public void setOriginalDescription(String originalDescription) {
    this.originalDescription = originalDescription;
  }
  public String getOriginalLanguageId() {
    return originalLanguageId;
  }
  public void setOriginalLanguageId(String originalLanguageId) {
    this.originalLanguageId = originalLanguageId;
  }
  public String getTranslatedText() {
    return translatedText;
  }
  public void setTranslatedText(String translatedText) {
    this.translatedText = translatedText;
  }
  public String getTranslatedDescription() {
    return translatedDescription;
  }
  public void setTranslatedDescription(String translatedDescription) {
    this.translatedDescription = translatedDescription;
  }
  public String getTranslatedLanguageId() {
    return translatedLanguageId;
  }
  public void setTranslatedLanguageId(String translatedLanguageId) {
    this.translatedLanguageId = translatedLanguageId;
  }    
}