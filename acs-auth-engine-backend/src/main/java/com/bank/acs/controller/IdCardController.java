package com.bank.acs.controller;

import com.bank.acs.exception.BusinessException;
import com.bank.acs.service.RenderHTMLService;
import com.bank.acs.service.TranslationService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static com.bank.acs.Profile.COUNTRY_EE_PROFILE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "${app.allowed.id-card-redirect.origin.url}")
@RequestMapping(value = "${app.api.id-card-redirect.url}", produces = APPLICATION_JSON_VALUE)
public class IdCardController {

    private final TranslationService translationService;
    private final RenderHTMLService renderHTMLService;

    @SneakyThrows
    @Profile(COUNTRY_EE_PROFILE)
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String handleRedirectFromCIAM(
            @CookieValue(name = "creq") String creq,
            @CookieValue(name = "acs-transaction-id") String acsTransactionId,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message
    ) {
        log.info("Redirected from CIAM with code = {}, error message = {}, acsTransactionId = {}, creq = {}", code, message, acsTransactionId, creq);
        return renderHTMLService.getReactAppHtml(creq, acsTransactionId, Optional.empty(), Optional.ofNullable(code));
    }

}
