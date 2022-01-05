package com.bank.acs.service;

import com.bank.acs.entity.AppSession;
import com.bank.acs.enumeration.AcsErrorCode;
import com.bank.acs.enumeration.AppState;
import com.bank.acs.enumeration.AuthMethod;
import com.bank.acs.enumeration.UiAction;
import com.bank.acs.exception.BusinessException;
import com.bank.acs.repository.AppSessionRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bank.acs.constant.RequestParams.*;
import static com.bank.acs.constant.ResponseParams.*;
import static com.bank.acs.enumeration.AppState.*;
import static com.bank.acs.enumeration.AuthMethod.CODE_CALCULATOR;
import static com.bank.acs.enumeration.Country.getCountryFromProfiles;
import static com.bank.acs.util.CardUtil.maskCardNumber;
import static java.util.stream.Collectors.joining;

@Slf4j
@RequiredArgsConstructor
@Service
public class UiActionService {

    public static final String SEPARATOR = ",";

    protected final UserService userService;
    protected final AuthMethodService authMethodService;
    protected final AppStateService appStateService;
    protected final Environment environment;
    protected final AppSessionRepository appSessionRepository;

    public Map<String, String> handleRequestFromUi(AppSession session, UiAction uiAction, Map<String, String> uiParams) {

        log.info("{} request", uiAction.name());

        switch (uiAction) {
            case GET_INITIAL_INFORMATION: {
                final var response = handleGetInitialInformation(session);
                return handleUiResponse(session, response);
            }
            case CONFIRM_USER_LOGIN: {
                final var response = handleConfirmUserLogin(session, uiParams);
                return handleUiResponse(session, response);
            }
            case CHOOSE_AUTH_METHOD: {
                final var authMethodStr = uiParams.get(SELECTED_AUTH_METHOD_PARAM);
                final var authMethod = AuthMethod.valueOf(authMethodStr);
                if (authMethod == CODE_CALCULATOR) {
                    final var response = handleChoseAuth(session, uiParams);
                    return handleUiResponse(session, response);
                }
                // if its not CODE_CALCULATOR, we need initiate authentication right away,
                // so moving to next case INIT_AUTH
            }
            case INIT_AUTH: {
                final var response = handleInitAuth(session, uiParams);
                return handleUiResponse(session, response);
            }
            case AUTH_STATUS: {
                final var response = handleAuthStatus(session);
                return handleUiResponse(session, response);
            }
            case CHANGE_CURRENT_LANGUAGE: {
                final var response = handleChangeCurrentLanguage(session, uiParams);
                return handleUiResponse(session, response);
            }
            default: {
                log.error("handleRequestFromUi, Unsupported uiAction = {}", uiAction);
                throw new BusinessException(AcsErrorCode.GENERAL_EXCEPTION);
            }
        }
    }

    @SneakyThrows
    protected UiResponse handleGetInitialInformation(AppSession session) {
        final var output = new HashMap<String, String>();
        var country = getCountryFromProfiles(environment.getActiveProfiles()).name();
        output.put(ACCT_NUMBER, maskCardNumber(session.getAcctNumber(), true));
        output.put(PURCHASE_DATE, session.getPurchaseDate());
        output.put(PURCHASE_CURRENCY, session.getPurchaseCurrency());
        output.put(MERCHANT_NAME,
                session.getMerchantName() != null
                        ? URLEncoder.encode(session.getMerchantName(), StandardCharsets.UTF_8.name()) : "");
        output.put(PURCHASE_AMOUNT, session.getPurchaseAmount());
        output.put(STATE_PARAM, session.getState().name());
        output.put(COUNTRY, country);
        fillAuthAndLanguageInformation(session, output, true);
        log.info("Session id = {} Chosen county = {}, userSate = {}",
                session.getAcsTransactionId(), country, session.getState());

        if (List.of(RENDER_FATAL_ERROR_PAGE, REDIRECTED_TO_EXTERNAL_SYSTEM).contains(session.getState())) {
            return new UiResponse(output, session.getState());
        } else if (!StringUtils.isEmpty(session.getChosenUsername())) {
            return new UiResponse(output, RENDER_SELECT_AUTH_METHOD_PAGE);
        }
        return new UiResponse(output, RENDER_ENTER_LOGIN_PAGE);
    }

    private void fillAuthAndLanguageInformation(AppSession session, Map<String, String> output, boolean emptyAllowed) {
        var availableAuthMethods = userService.getAllowedUserAuthMethods(session, emptyAllowed).stream()
                .map(Enum::name)
                .collect(joining(SEPARATOR));
        output.put(DEFAULT_AUTH_METHOD_PARAM, session.getUsedAuthMethod() != null ? session.getUsedAuthMethod().name() : null);
        output.put(AVAILABLE_AUTH_METHODS_PARAM, availableAuthMethods);
        output.put(USER_LANGUAGE, session.getUsedLanguage());
    }

    private UiResponse handleConfirmUserLogin(AppSession session, Map<String, String> uiParams) {
        final var enteredLogin = uiParams.get(ENTERED_LOGIN_PARAM);
        log.info("handleConfirmUserLogin, login = {}", enteredLogin);
        final var output = new HashMap<String, String>();
        final var response = userService.handleEnteredLogin(session, enteredLogin);
        fillAuthAndLanguageInformation(session, output, false);
        return new UiResponse(output, response);
    }

    protected UiResponse handleChoseAuth(AppSession session, Map<String, String> uiParams) {
        final var authMethodStr = uiParams.get(SELECTED_AUTH_METHOD_PARAM);
        final var authMethod = AuthMethod.valueOf(authMethodStr);
        authMethodService.handleSelectedAuthMethod(session, authMethod);
        log.info("handleChoseAuth, authMethod = {}", authMethod);
        return new UiResponse(Map.of(), RENDER_INIT_AUTH_PAGE);
    }

    protected UiResponse handleInitAuth(AppSession session, Map<String, String> uiParams) {

        final var authMethodStr = uiParams.get(SELECTED_AUTH_METHOD_PARAM);
        if (authMethodStr != null && !authMethodStr.isBlank()) {
            handleChoseAuth(session, uiParams);
        }

        final var authMethod = session.getUsedAuthMethod();
        log.info("handleInitAuth, authMethod = {}", authMethod);

        switch (authMethod) {
            case CODE_CALCULATOR: {
                final var confirmationCode = uiParams.get(CONFIRMATION_CODE_PARAM);
                final var response = authMethodService.confirmCodeCalculator(session, confirmationCode);
                log.info("handleInitAuth, authMethod = {}, with response = {}", authMethod, response);
                return new UiResponse(Map.of(), response);
            }
            case SMART_ID: {
                final var authorizationCode = authMethodService.initSmartId(session);
                session.setAuthorizationCode(authorizationCode);
                appSessionRepository.save(session);
                log.info("handleInitAuth, authMethod = {}, with response = {}", authMethod, authorizationCode);
                return new UiResponse(Map.of(AUTHORIZATION_CODE_PARAM, authorizationCode), CHECKING_AUTH_STATUS);
            }
            case M_SIGNATURE: {
                final var authorizationCode = authMethodService.initMSignature(session);
                session.setAuthorizationCode(authorizationCode);
                appSessionRepository.save(session);
                log.info("handleInitAuth, authMethod = {}, with response = {}", authMethod, authorizationCode);
                return new UiResponse(Map.of(AUTHORIZATION_CODE_PARAM, authorizationCode), CHECKING_AUTH_STATUS);
            }
            case ID_CARD: {
                if (session.getState() == REDIRECTED_TO_EXTERNAL_SYSTEM) {
                    final var ciamAuthorizationCode = uiParams.get(CONFIRMATION_CODE_PARAM);
                    final var response = authMethodService.confirmIDCard(session, ciamAuthorizationCode);
                    log.info("handleInitAuth (redirect from CIAM), authMethod = {}, with response = {}", authMethod, response);
                    return new UiResponse(Map.of(), response);
                } else {
                    final var redirectUrl = authMethodService.initIDCard(session);
                    session.setRedirectUrl(redirectUrl);
                    appSessionRepository.save(session);
                    log.info("handleInitAuth (redirect to CIAM), authMethod = {}, with redirectUrl = {}", authMethod, redirectUrl);
                    return new UiResponse(Map.of(REDIRECT_URL_PARAM, redirectUrl), REDIRECTED_TO_EXTERNAL_SYSTEM);
                }
            }
            default: {
                log.error("handleAuthStatus, Unsupported authMethod = {}", authMethod);
                throw new BusinessException(AcsErrorCode.AUTHENTICATION_TYPE_NOT_ALLOWED);
            }
        }
    }

    protected UiResponse handleAuthStatus(AppSession session) {

        final var data = new HashMap<String, String>();
        data.put(AUTH_METHOD_PARAM, session.getUsedAuthMethod().name());

        switch (session.getUsedAuthMethod()) {
            case CODE_CALCULATOR: {
                //CODE_CALCULATOR response we get immediately in "INIT_AUTH" action
                //No need to to this call for "CODE_CALCULATOR"
                data.put(CONFIRMATION_SUCCESSFUL_PARAM, Boolean.TRUE.toString());
                log.info("handleAuthStatus, authMethod = {}, {}={} ",
                        session.getUsedAuthMethod(), CONFIRMATION_SUCCESSFUL_PARAM, Boolean.TRUE.toString());
                return new UiResponse(data, RENDER_SUCCESSFUL_PAGE);
            }
            case SMART_ID:
            case M_SIGNATURE: {
                final var statusResponse = authMethodService.checkAuthMethodStatus(session);
                log.info("handleAuthStatus, authMethod = {}, {}={} ",
                        session.getUsedAuthMethod(), CONFIRMATION_SUCCESSFUL_PARAM, statusResponse);
                if (statusResponse.isPresent()) {
                    data.put(CONFIRMATION_SUCCESSFUL_PARAM, Boolean.toString(statusResponse.get()));
                    return new UiResponse(data, statusResponse.get() ? RENDER_SUCCESSFUL_PAGE : RENDER_NON_FATAL_ERROR_PAGE);
                }
                return new UiResponse(data, AUTHENTICATION_IN_PROGRESS);
            }
            default: {
                log.error("handleAuthStatus, Unsupported authMethod = {}", session.getUsedAuthMethod());
                throw new BusinessException(AcsErrorCode.AUTHENTICATION_TYPE_NOT_ALLOWED);
            }
        }
    }

    protected UiResponse handleChangeCurrentLanguage(AppSession session, Map<String, String> uiParams) {
        final var language = uiParams.get(LANGUAGE_PARAM);
        session.setUsedLanguage(language);
        appSessionRepository.save(session);
        log.info("handleChangeCurrentLanguage, language = {}", language);
        return new UiResponse(Map.of(), session.getState());
    }

    private Map<String, String> handleUiResponse(AppSession session, UiResponse uiResponse) {
        log.info("handleUiResponse, acsTransactionId = {}, new appState {} ", session.getAcsTransactionId(), uiResponse.getAppState());
        appStateService.saveState(session.getAcsTransactionId(), uiResponse.getAppState());
        return uiResponse.getContent();
    }

    @Data
    public static class UiResponse {
        private final Map<String, String> content;
        private final AppState appState;
    }

}
