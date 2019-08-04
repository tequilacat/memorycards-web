package org.tequilacat.memcard.server.bo;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class Language {

  @Id
  private ObjectId id;

  @NotBlank
  @Size(min = 2, max = 2)
  private String code;

  @NotBlank
  private String ownName;

  @NotBlank
  private String genericName;
  
  private List<Card> cards = new ArrayList<>();
}
