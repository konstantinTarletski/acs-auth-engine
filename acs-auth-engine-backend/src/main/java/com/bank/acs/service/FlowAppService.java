package com.bank.acs.service;

import com.bank.acs.constant.RequestParams;
import com.bank.acs.entity.AppSession;
import com.bank.acs.entity.UserAuthMethod;
import com.bank.acs.enumeration.AcsErrorCode;
import com.bank.acs.enumeration.AuthMethod;
import com.bank.acs.enumeration.UiAction;
import com.bank.acs.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.bank.acs.constant.ResponseParams.AUTHORIZATION_CODE_PARAM;

@Slf4j
@RequiredArgsConstructor
@Service
public class FlowAppService {

    public <T> T getUi(AppSession session, Map<String, String> uiResponse, Optional<BusinessException> error, UiRender<T> render) {
        log.info("getAppNativeTemplate, AppState = {}, acsTransactionId = {}, error = {}", session.getState(), session.getAcsTransactionId(), error);

        final var language = session.getUsedLanguage();

        switch (session.getState()) {
            case RENDER_ENTER_LOGIN_PAGE: {
                return render.getInternetBankLoginPage(language, error);
            }
            case RENDER_SELECT_AUTH_METHOD_PAGE: {
                final var authMethods = session.getUserAuthMethods().stream().map(UserAuthMethod::getAuthMethod).collect(Collectors.toSet());
                return render.getChooseAuthMethodPage(language, authMethods, error);
            }
            case RENDER_INIT_AUTH_PAGE: {
                switch (session.getUsedAuthMethod()) {
                    case CODE_CALCULATOR: {
                        return render.getAuthEnterCodeForCodeCalculatorPage(language, error);
                    }
                    case SMART_ID:
                    case M_SIGNATURE: {
                        final var authorizationCode = uiResponse != null ? uiResponse.get(AUTHORIZATION_CODE_PARAM) : "-";
                        return render.getSmartIdMSignatureCheckStatusPage(language, authorizationCode, session.getUsedAuthMethod(), error);
                    }
                }
            }
            case AUTHENTICATION_IN_PROGRESS: {
                return render.getSmartIdMSignatureCheckStatusPage(language, session.getAuthorizationCode(), session.getUsedAuthMethod(), error);
            }
            case CHECKING_AUTH_STATUS: {
                final var authorizationCode = uiResponse != null ? uiResponse.get(AUTHORIZATION_CODE_PARAM) : "-";
                return render.getSmartIdMSignatureCheckStatusPage(language, authorizationCode, session.getUsedAuthMethod(), error);
            }
            case RENDER_SUCCESSFUL_PAGE: {
                String amountAndCurrency = session.getPurchaseAmount() + " " + session.getPurchaseCurrency();
                String merchantName = session.getMerchantName();
                return render.getAuthSuccessPage(language, amountAndCurrency, merchantName, error);
            }
            case RENDER_FATAL_ERROR_PAGE: {
                return render.getFatalFailurePage(language, error.orElseGet(() -> new BusinessException(AcsErrorCode.FATAL_EXCEPTION)));
            }
            case RENDER_NON_FATAL_ERROR_PAGE: {
                return render.getNonFatalFailurePage(language, error.orElseGet(() -> new BusinessException(AcsErrorCode.GENERAL_EXCEPTION)));
            }
            default: {
                return render.getFatalFailurePage(language, error.orElseGet(() -> new BusinessException(AcsErrorCode.GENERAL_EXCEPTION)));
            }
        }
    }

    public Map<String, String> getApplicationParameters(UiAction action, String userInput) {
        log.info("getApplicationParameters, action = {}, userInput = {}", action, userInput);
        switch (action) {
            case GET_INITIAL_INFORMATION: {
                return Map.of();
            }
            case CONFIRM_USER_LOGIN: {
                if (userInput != null && !userInput.isBlank()) {
                    return Map.of(RequestParams.ENTERED_LOGIN_PARAM, userInput);
                }else {
                    return Map.of();
                }
            }
            case CHOOSE_AUTH_METHOD: {
                if (userInput != null && !userInput.isBlank()) {
                    return Map.of(RequestParams.SELECTED_AUTH_METHOD_PARAM, userInput);
                }else {
                    return Map.of();
                }
            }
            case INIT_AUTH: {
                List<String> authMethodNames = Arrays.asList(AuthMethod.values()).stream()
                        .map((authMethod -> authMethod.name()))
                        .collect(Collectors.toList());
                if (authMethodNames.contains(userInput)) {
                    return Map.of(RequestParams.SELECTED_AUTH_METHOD_PARAM, userInput);
                } else if (userInput != null && !userInput.isBlank()) {
                    return Map.of(RequestParams.CONFIRMATION_CODE_PARAM, userInput);
                } else {
                    return Map.of();
                }
            }
            case AUTH_STATUS: {
                return Map.of();
            }
            case BACK_TO_MERCHANT_SUCCESS: {
                return Map.of();
            }
            case BACK_TO_MERCHANT_CANCEL: {
                return Map.of();
            }
        }
        return Map.of();
    }

    public UiAction getNextUiAction(AppSession session, String userInput) {
        log.info("getNextUiAction, AppState = {}, acsTransactionId = {}, userInput = {}, usedAuthMethod = {}",
                session.getState(), session.getAcsTransactionId(), userInput, session.getUsedAuthMethod());
        switch (session.getState()) {//State from previous request
            case CHECK_CARD_STATUS:
                CARD_CHECK_SUCCESSFUL:
                {
                    return UiAction.GET_INITIAL_INFORMATION;
                }
            case RENDER_ENTER_LOGIN_PAGE: {
                return UiAction.CONFIRM_USER_LOGIN;
            }
            case RENDER_SELECT_AUTH_METHOD_PAGE: {
                if (!StringUtils.isBlank(userInput)) {
                    AuthMethod selectedAuthMethod = AuthMethod.valueOf(userInput);
                    session.setUsedAuthMethod(selectedAuthMethod);
                    log.info("getNextUiAction, setUsedAuthMethod = {}, acsTransactionId = {}",
                            selectedAuthMethod, session.getAcsTransactionId());
                }
                if (session.getUsedAuthMethod() == null) {
                    return UiAction.CHOOSE_AUTH_METHOD;
                }
                switch (session.getUsedAuthMethod()) {
                    case CODE_CALCULATOR: {
                        return UiAction.CHOOSE_AUTH_METHOD;
                    }
                    case SMART_ID:
                    case M_SIGNATURE: {
                        return UiAction.INIT_AUTH;
                    }
                }
            }
            case RENDER_INIT_AUTH_PAGE: {
                return UiAction.INIT_AUTH;
            }
            case AUTHENTICATION_IN_PROGRESS:
            case CHECKING_AUTH_STATUS: {
                return UiAction.AUTH_STATUS;
            }
            case RENDER_SUCCESSFUL_PAGE: {
                return UiAction.BACK_TO_MERCHANT_SUCCESS;
            }
            case RENDER_FATAL_ERROR_PAGE: {
                return UiAction.BACK_TO_MERCHANT_CANCEL;
            }
            case RENDER_NON_FATAL_ERROR_PAGE: {
                return UiAction.GET_INITIAL_INFORMATION;
            }
            default: {
                return UiAction.BACK_TO_MERCHANT_CANCEL;
            }
        }
    }

}
