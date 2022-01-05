package com.bank.acs.service;

import com.bank.acs.entity.AppSession;
import com.bank.acs.enumeration.AppState;
import com.bank.acs.exception.BusinessException;
import com.bank.acs.repository.AppSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lv.bank.cards.rtcu.util.BankCardsWSWrapperDelegate;
import org.springframework.stereotype.Service;

import static com.bank.acs.enumeration.AcsErrorCode.*;
import static com.bank.acs.enumeration.AppState.CARD_CHECK_SUCCESSFUL;
import static com.bank.acs.util.CardUtil.maskSensitiveInformation;
import static com.bank.acs.util.StringUtil.isPersonalCodeValid;
import static org.apache.commons.lang3.StringUtils.substringBetween;

@Slf4j
@RequiredArgsConstructor
@Service
public class LinkAppService {

    private final AppSessionRepository appSessionRepository;
    private final BankCardsWSWrapperDelegate linkAppWs;

    public AppState executeCardStatusCheck(AppSession session, String pan, String expirity) throws BusinessException {

        final var cardInfoRequest = "<do what=\"card-info-acs\"><card>" + pan + "</card></do>";
        final var cardInfoResponse = performRtcungCall(cardInfoRequest);

        final var personalCode = substringBetween(cardInfoResponse, "<person-code-card-holder>", "</person-code-card-holder>");
        if (personalCode == null || !isPersonalCodeValid(personalCode)) {
            log.warn("LinkApp error, personal code not valid");
            throw new BusinessException(NO_PERSON_ACCOUNTS_FOUND);
        }
        session.setCardHolderPersonalCode(personalCode);
        appSessionRepository.save(session);

        final var country = substringBetween(cardInfoResponse, "<country>", "</country>");
        if (country == null || country.isBlank()) {
            log.warn("LinkApp error, country not valid");
            throw new BusinessException(GENERAL_EXCEPTION);
        }
        session.setCardCountry(country);
        appSessionRepository.save(session);

        final var cardStatusCmsRequest = "<do what=\"card-status-bo\"><card>" + pan + "</card></do>";
        final var cardStatusCmsResponse = performRtcungCall(cardStatusCmsRequest);

        if (!cardStatusCmsResponse.contains("<card-status-1>0</card-status-1>")) {
            log.info("LinkApp: card blocked with cms");
            throw new BusinessException(CARD_BLOCKED_CMS);
        }

        final var cardStatusRmsInCmsRequest = "<do what=\"card-status-rms\"><card>" + pan + "</card></do>";
        final var cardStatusRmsInCmsResponse = performRtcungCall(cardStatusRmsInCmsRequest);

        if (cardStatusRmsInCmsResponse.contains("entry") && isCardBlocked(expirity, cardStatusRmsInCmsResponse)) {
            if (cardStatusRmsInCmsResponse.contains("<action-code>108</action-code>")) {
                log.info("LinkApp: card blocked with E-commerce");
                throw new BusinessException(CARD_BLOCKED_ECOMMERCE);
            }
            log.info("LinkApp: card blocked in RMS");
            throw new BusinessException(CARD_BLOCKED_RMS);
        }
        return CARD_CHECK_SUCCESSFUL;
    }

    protected String performRtcungCall(String request) {
        log.info("LinkApp: request = {}", maskSensitiveInformation(request));
        String cardInfoResponse = linkAppWs.rtcungCall(request);
        log.info("LinkApp: response = {}", maskSensitiveInformation(cardInfoResponse));
        return cardInfoResponse;
    }

    protected boolean isCardBlocked(String expirity, String cardStatusRmsInCms) {
        // There can be delivery block of renewed card. Check if block is for this card
        final String[] entries = cardStatusRmsInCms.split("<entry>");

        for (String entry : entries) {
            if (!entry.contains("description")) {
                continue;
            }
            String description = substringBetween(entry, "<description>", "</description>");
            String rule = substringBetween(entry, "<rule-expression>", "</rule-expression>");

            if (!description.contains("Card blocked for delivery")) {
                return true;
            }
            if (rule.contains("FLD_014=='" + expirity + "'")) {
                return true;
            }
        }
        return false;
    }

}
