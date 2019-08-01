package org.tequilacat.memcard.server.controller;

import java.io.IOException;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.tequilacat.memcard.server.repository.LanguageRepository;
import org.tequilacat.memcard.server.service.CardService;
import org.tequilacat.memcard.server.utils.StreamUtils;

@Controller
public class HomeController {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(HomeController.class);

  private static final String MAINPAGE_VIEW = "welcome";

  @Autowired
  private CardService cardService;

  @Autowired LanguageRepository languageRepository;
  
  private ModelAndView createMainPageView() {
    var view = new ModelAndView(MAINPAGE_VIEW);
    var cardsPerLangs = cardService.getCardsPerLang();
    view.getModel().put("cardGroups", cardsPerLangs);
    view.getModel().put("languageIds", StreamUtils.mapToList(cardsPerLangs, c -> c.getLanguageId()));

    var newCardForm = new NewCardForm();

    if (cardsPerLangs.size() > 0) {
      newCardForm.setOriginalLanguageId(cardsPerLangs.get(0).getLanguageId());
    }

    if (cardsPerLangs.size() > 1) {
      newCardForm.setTranslatedLanguageId(cardsPerLangs.get(1).getLanguageId());
    }

    view.getModel().put("newCardForm", newCardForm);

    return view;
  }
  
  @RequestMapping(value="/")
	public ModelAndView indexPage(final NewCardForm newCardForm) throws IOException{
		return createMainPageView();
	}
	
	@PostMapping(value="/addcard")
  public ModelAndView addNewCard(@Valid final NewCardForm newCardForm, 
      final BindingResult bindingResult,
      final ModelMap model) throws IOException{
	  
    if (bindingResult.hasErrors()) {
      return createMainPageView();
    }
    
    var srcLang = languageRepository.findByCode(newCardForm.getOriginalLanguageId());
    var translLang = languageRepository.findByCode(newCardForm.getTranslatedLanguageId());
    
    var card1 = cardService.createCard(newCardForm.getOriginalText(), 
        newCardForm.getOriginalDescription(), srcLang);
    cardService.createTranslation(newCardForm.getTranslatedText(), 
        newCardForm.getTranslatedDescription(), translLang, card1);
	  
    return new ModelAndView("redirect:/");
	}
}
