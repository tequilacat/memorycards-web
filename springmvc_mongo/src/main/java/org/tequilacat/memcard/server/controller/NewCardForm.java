package org.tequilacat.memcard.server.controller;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.bson.types.ObjectId;

import lombok.Data;

@Data
public class NewCardForm {
  @NotBlank
  private String originalText;

  private String originalDescription;

  @NotNull
  private ObjectId originalLanguageId;

  @NotBlank
  private String translatedText;

  private String translatedDescription;

  @NotNull
  private ObjectId translatedLanguageId;
}