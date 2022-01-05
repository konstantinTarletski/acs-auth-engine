package com.bank.acs.controller;

import com.bank.acs.service.TranslationService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "${app.allowed.translations.origin.url}")
@RequestMapping(value = "${app.api.translations.url}", produces = APPLICATION_JSON_VALUE)
public class TranslationController {

    private final TranslationService translationService;

    @SneakyThrows
    @GetMapping
    public String getTranslations() {
        log.info("Reading translations");
        return translationService.getTranslations();
    }

}
