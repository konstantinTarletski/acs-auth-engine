package com.bank.acs.service;

import com.bank.acs.config.property.AppProperties;
import com.bank.acs.constant.ResponseParams;
import com.bank.acs.enumeration.AuthMethod;
import com.bank.acs.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.bank.acs.enumeration.AuthMethod.SMART_ID;
import static java.util.Locale.ENGLISH;

@Slf4j
@RequiredArgsConstructor
@Service
public class RenderHTMLService implements UiRender<String> {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public static final String BASE_URL_KEY = "baseUrl";
    public static final String CREQ_VALUE_KEY = "creqValue";
    public static final String DEFAULT_LANGUAGE_KEY = "defaultLanguage";
    public static final String ACS_TRANSACTION_ID = "acsTransactionId";
    public static final String ERROR = "error";
    public static final String AUTHORIZATION_CODE_FROM_CIAM = "authorizationCodeFromCIAM";

    public static final String AUTH_METHODS_KEY = "authMethods";
    public static final String TITLE_KEY = "title";
    public static final String DESCRIPTION_KEY = "description";
    public static final String ADDITIONAL_DESCRIPTION_KEY = "additionalDescription";
    public static final String CONTINUE_BUTTON_LABEL_KEY = "continueButtonLabel";
    public static final String CANCEL_BUTTON_LABEL_KEY = "cancelButtonLabel";
    public static final String BACK_BUTTON_LABEL_KEY = "backButtonLabel";
    public static final String BACK_TO_MERCHANT_BUTTON_LABEL_KEY = "backToMerchantButtonLabel";
    public static final String TRY_AGAIN_BUTTON_LABEL_KEY = "tryAgainButtonLabel";
    public static final String FORM_ACTION_KEY = "formAction";
    public static final String FORM_ACTION_VALUE = "HTTPS://EMV3DS/challenge";
    public static final String ERROR_TITLE_KEY = "errorTitle";
    public static final String ERROR_DESCRIPTION_KEY = "errorDescription";
    public static final String AUTHORIZATION_CODE_KEY = "authorizationCode";

    protected final AppProperties appProperties;
    protected final SpringTemplateEngine templateEngine;
    protected final ObjectMapper objectMapper;
    protected final TranslationService translationService;

    @Override
    public String getInternetBankLoginPage(String language, Optional<BusinessException> error) {
        final Map<String, Object> variables = Map.of(
                BASE_URL_KEY, buildBaseUrl(),
                TITLE_KEY, translationService.translate(language, "screens.internetBankLogin.enterInternetBankLogin", "Please enter your Internet bank login name."),
                CONTINUE_BUTTON_LABEL_KEY, translationService.translate(language, "screens.internetBankLogin.buttons.continue", "CONFIRM"),
                CANCEL_BUTTON_LABEL_KEY, translationService.translate(language, "screens.internetBankLogin.buttons.cancel", "CANCEL"),
                FORM_ACTION_KEY, FORM_ACTION_VALUE,
                ERROR_TITLE_KEY, error.isPresent() ? translationService.translateErrorTitle(language, error.get()) : "",
                ERROR_DESCRIPTION_KEY, error.isPresent() ? translationService.translateErrorDescription(language, error.get()) : ""
        );
        return templateEngine.process("internet_bank_login.html", new Context(ENGLISH, variables));
    }

    @Override
    public String getChooseAuthMethodPage(String language, Set<AuthMethod> authMethods, Optional<BusinessException> error) {
        fillTranslationsForAuthMethods(language, authMethods);
        final Map<String, Object> variables = Map.of(
                TITLE_KEY, translationService.translate(language, "screens.choose.title", "Keep Your Account Safe"),
                DESCRIPTION_KEY, translationService.translate(language, "screens.choose.description", "Please authenticate the payment"),
                CANCEL_BUTTON_LABEL_KEY, translationService.translate(language, "screens.choose.buttons.cancel", "CANCEL PAYMENT"),
                AUTH_METHODS_KEY, authMethods,
                BASE_URL_KEY, buildBaseUrl(),
                FORM_ACTION_KEY, FORM_ACTION_VALUE,
                ERROR_TITLE_KEY, error.isPresent() ? translationService.translateErrorTitle(language, error.get()) : "",
                ERROR_DESCRIPTION_KEY, error.isPresent() ? translationService.translateErrorDescription(language, error.get()) : ""
        );
        return templateEngine.process("choose_auth_method.html", new Context(ENGLISH, variables));
    }

    @Override
    public String getAuthEnterCodeForCodeCalculatorPage(String language, Optional<BusinessException> error) {
        final Map<String, Object> variables = Map.of(
                BASE_URL_KEY, buildBaseUrl(),
                TITLE_KEY, translationService.translate(language, "screens.authEnterCode.enterResponseCode", "Enter one-time code, which is displayed on the generator screen (after the message “APPLI-“ appears, press 1)."),
                CONTINUE_BUTTON_LABEL_KEY, translationService.translate(language, "screens.authEnterCode.buttons.continue", "CONFIRM"),
                BACK_BUTTON_LABEL_KEY, translationService.translate(language, "screens.authEnterCode.buttons.back", "BACK"),
                FORM_ACTION_KEY, FORM_ACTION_VALUE,
                ERROR_TITLE_KEY, error.isPresent() ? translationService.translateErrorTitle(language, error.get()) : "",
                ERROR_DESCRIPTION_KEY, error.isPresent() ? translationService.translateErrorDescription(language, error.get()) : ""
        );
        return templateEngine.process("auth_enter_code_for_code_calculator.html", new Context(ENGLISH, variables));
    }

    @Override
    public String getSmartIdMSignatureCheckStatusPage(String language, String authorizationCode, AuthMethod authMethod, Optional<BusinessException> error){
        String descriptionDefaultValue = authMethod == SMART_ID
                ? "Login message was sent to your Smart-ID device.\n\nPLEASE NOTE: enter the PIN code only after making sure that the control code matches the code displayed here!"
                : "Login message was sent to your mobile phone.\n\nPLEASE NOTE: enter the PIN code only after making sure that the control code matches the code displayed here!";
        String additionalDescriptionDefaultValue = authMethod == SMART_ID
                ? "After entering PIN code, return to this application and finish the authentication process by pressing \"Confirm\" button.\n\n{{PIN}}"
                : "After entering PIN code, return to this application and finish the authentication process by pressing \"Confirm\" button.\n\n{{PIN}}";
        final Map<String, Object> variables = Map.of(
                BASE_URL_KEY, buildBaseUrl(),
                TITLE_KEY, translationService.translate(language, "screens.auth.code.title", "Control code:"),
                DESCRIPTION_KEY, translationService.translate(language, "screens.auth.code.appBasedDescription." + authMethod.name(), descriptionDefaultValue),
                ADDITIONAL_DESCRIPTION_KEY, translationService.translate(language, "screens.auth.code.appBasedAdditionalDescription." + authMethod.name(), additionalDescriptionDefaultValue, Map.of("\n\n{{PIN}}", "")),
                CONTINUE_BUTTON_LABEL_KEY, translationService.translate(language, "screens.auth.appBasedContinue", "CONFIRM"),
                BACK_BUTTON_LABEL_KEY, translationService.translate(language, "screens.auth.backButton", "BACK"),
                AUTHORIZATION_CODE_KEY, authorizationCode,
                FORM_ACTION_KEY, FORM_ACTION_VALUE,
                ERROR_TITLE_KEY, error.isPresent() ? translationService.translateErrorTitle(language, error.get()) : "",
                ERROR_DESCRIPTION_KEY, error.isPresent() ? translationService.translateErrorDescription(language, error.get()) : ""
        );
        return templateEngine.process("smart_id_m_signature_check_status.html", new Context(ENGLISH, variables));
    }

    @Override
    public String getAuthSuccessPage(String language, String amountAndCurrency, String merchantName, Optional<BusinessException> error) {
        final Map<String, Object> variables = Map.of(
                BASE_URL_KEY, buildBaseUrl(),
                TITLE_KEY, translationService.translate(language,
                        "screens.authSuccess.justPaid",
                        "Payment for <bold>{{sum}}</bold> has been confirmed",
                        Map.of(
                                "<bold>{{sum}}</bold>", "<b>" + amountAndCurrency + "</b>",
                                "<bold>{{merchantName}}</bold>", "<b>" + merchantName + "</b>"
                        )
                ),
                BACK_TO_MERCHANT_BUTTON_LABEL_KEY, translationService.translate(language, "screens.authSuccess.backToMerchantButton", "BACK TO THE MERCHANT"),
                FORM_ACTION_KEY, FORM_ACTION_VALUE
        );
        return templateEngine.process("success.html", new Context(ENGLISH, variables));
    }

    @Override
    public String getFatalFailurePage(String language, BusinessException error) {
        final Map<String, Object> variables = Map.of(
                BASE_URL_KEY, buildBaseUrl(),
                BACK_TO_MERCHANT_BUTTON_LABEL_KEY, translationService.translate(language, "screens.failure.backToMerchantButton", "BACK TO THE MERCHANT"),
                FORM_ACTION_KEY, FORM_ACTION_VALUE,
                ERROR_TITLE_KEY, translationService.translateErrorTitle(language, error),
                ERROR_DESCRIPTION_KEY, translationService.translateErrorDescription(language, error)
        );
        return templateEngine.process("fatal_error.html", new Context(ENGLISH, variables));
    }

    @Override
    public String getNonFatalFailurePage(String language, BusinessException error) {
        final Map<String, Object> variables = Map.of(
                BASE_URL_KEY, buildBaseUrl(),
                TRY_AGAIN_BUTTON_LABEL_KEY, translationService.translate(language, "screens.authFailure.buttons.tryAgain", "BACK"),
                FORM_ACTION_KEY, FORM_ACTION_VALUE,
                ERROR_TITLE_KEY, translationService.translateErrorTitle(language, error),
                ERROR_DESCRIPTION_KEY, translationService.translateErrorDescription(language, error)
        );
        return templateEngine.process("non_fatal_error.html", new Context(ENGLISH, variables));
    }

    public String getReactAppHtml(String creqValue, String acsTransactionId, Optional<BusinessException> errorObject) {
        return getReactAppHtml(creqValue, acsTransactionId, errorObject, Optional.empty());
    }

    public String getReactAppHtml(
            String creqValue,
            String acsTransactionId,
            Optional<BusinessException> errorObject,
            Optional<String> authorizationCodeFromCIAM
    ) {
        final var defaultLanguage = appProperties.getDefaultLanguage();
        final Map<String, Object> variables = Map.of(
                BASE_URL_KEY, buildBaseUrl(),
                CREQ_VALUE_KEY, creqValue,
                DEFAULT_LANGUAGE_KEY, defaultLanguage,
                ACS_TRANSACTION_ID, acsTransactionId,
                ERROR, getErrorAsJson(errorObject),
                AUTHORIZATION_CODE_FROM_CIAM, authorizationCodeFromCIAM.orElse("")
        );
        log.info("Rendering index.html, with variables = {}", variables);
        return templateEngine.process("index.html", new Context(ENGLISH, variables));
    }

    protected String getErrorAsJson(Optional<BusinessException> errorObject) {
        final var errorMap = new HashMap<String, String>();
        errorObject.ifPresent(error -> {
            errorMap.put(ResponseParams.ERROR_CODE, error.getErrorCode().name());
            errorMap.put(ResponseParams.ERROR_TYPE, error.getErrorCode().getErrorType().name());
            errorMap.put(ResponseParams.ERROR_TRANSLATION, error.getTranslatedMessage() != null ? error.getTranslatedMessage() : "");
        });
        try {
            return !errorMap.isEmpty() ? objectMapper.writeValueAsString(errorMap) : "";
        } catch (Exception e) {
            return "";
        }
    }

    private String buildBaseUrl() {
        return appProperties.getApplicationHostBaseUrl() + contextPath;
    }

    private void fillTranslationsForAuthMethods(String language, Set<AuthMethod> authMethods) {
        authMethods.stream().forEach(
                authMethod -> authMethod.setTranslationValue(
                        translationService.translate(language, authMethod.getTranslationJsonPath(), authMethod.getInternationalName())
                )
        );
    }

}
