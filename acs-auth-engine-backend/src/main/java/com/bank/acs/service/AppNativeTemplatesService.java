package com.bank.acs.service;

import com.bank.acs.config.property.AppProperties;
import com.bank.acs.dto.challenge.response.Image;
import com.bank.acs.dto.challenge.response.TemplateVariables;
import com.bank.acs.enumeration.AuthMethod;
import com.bank.acs.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.bank.acs.enumeration.AcsUiType.OOB;
import static com.bank.acs.enumeration.AcsUiType.SINGLE_SELECT;
import static com.bank.acs.enumeration.AcsUiType.TEXT;
import static com.bank.acs.enumeration.AuthMethod.SMART_ID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AppNativeTemplatesService implements UiRender<TemplateVariables.TemplateVariablesBuilder>{

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public static final String ERROR_INDICATOR = "Y";
    public static final String DEFAULT_INDICATOR = "N";

    protected final TranslationService translationService;
    protected final AppProperties appProperties;

    @Override
    public TemplateVariables.TemplateVariablesBuilder getInternetBankLoginPage(String language, Optional<BusinessException> error) {

        final var response = getBaseTemplate(language, error);
        StringBuilder challengeInfoTextSb = new StringBuilder();
        if (error.isPresent()) {
            challengeInfoTextSb.append(translationService.translateError(language, error.get()))
                    .append("\n\n");
        }
        challengeInfoTextSb.append(translationService.translate(language, "screens.internetBankLogin.enterInternetBankLogin", "Please enter your Internet bank login code."));
        response.challengeInfoText(challengeInfoTextSb.toString());
        response.challengeInfoLabel(translationService.translate(language, "screens.internetBankLogin.loginCodeInputLabel", "Login code:"))
                .acsUiType(TEXT)
                .submitAuthenticationLabel(translationService.translate(language, "screens.internetBankLogin.buttons.continue", "CONFIRM"));
        return response;
    }

    @Override
    public TemplateVariables.TemplateVariablesBuilder getChooseAuthMethodPage(String language, Set<AuthMethod> authMethods, Optional<BusinessException> error) {
        final var response = getBaseTemplate(language, error);
        response.challengeInfoText(translationService.translate(language, "screens.choose.title", "Keep Your Account Safe"))
                .challengeInfoLabel(translationService.translate(language, "screens.choose.description", "Please authenticate the payment"))
                .challengeSelectInfo(getListOfAvailableAuthMethods(language, authMethods))
                .acsUiType(SINGLE_SELECT)
                .submitAuthenticationLabel(translationService.translate(language, "screens.choose.buttons.continue", "CONFIRM"));
        return response;
    }

    @Override
    public TemplateVariables.TemplateVariablesBuilder getAuthEnterCodeForCodeCalculatorPage(String language, Optional<BusinessException> error) {
        final var response = getBaseTemplate(language, error);

        StringBuilder challengeInfoTextSb = new StringBuilder();
        if (error.isPresent()) {
            challengeInfoTextSb.append(translationService.translateError(language, error.get()))
                    .append("\n\n");
        }
        challengeInfoTextSb.append(translationService.translate(language, "screens.authEnterCode.enterResponseCode", "Enter one-time code, which is displayed on the generator screen (after the message “APPLI-“ appears, press 1)."));
        response.challengeInfoText(challengeInfoTextSb.toString())
                .challengeInfoLabel(translationService.translate(language, "screens.authEnterCode.confirmationCodeLabel", "Generator code:"))
                .acsUiType(TEXT)
                .submitAuthenticationLabel(translationService.translate(language, "screens.authEnterCode.buttons.continue", "CONFIRM"));
        return response;
    }

    @Override
    public TemplateVariables.TemplateVariablesBuilder getSmartIdMSignatureCheckStatusPage(String language, String authorizationCode, AuthMethod authMethod, Optional<BusinessException> error){
        final var response = getBaseTemplate(language, error);
        String descriptionDefaultValue = authMethod == SMART_ID
                ? "Login message was sent to your Smart-ID device.\n\nPLEASE NOTE: enter the PIN code only after making sure that the control code matches the code displayed here!"
                : "Login message was sent to your mobile phone.\n\nPLEASE NOTE: enter the PIN code only after making sure that the control code matches the code displayed here!";
        String description = translationService.translate(language, "screens.auth.code.appBasedDescription." + authMethod.name(), descriptionDefaultValue);
        String additionalDescriptionDefaultValue = authMethod == SMART_ID
                ? "After entering PIN code, return to this application and finish the authentication process by pressing \"Confirm\" button.\n\n{{PIN}}"
                : "After entering PIN code, return to this application and finish the authentication process by pressing \"Confirm\" button.\n\n{{PIN}}";
        String additionalDescription = translationService.translate(language, "screens.auth.code.appBasedAdditionalDescription." + authMethod.name(), additionalDescriptionDefaultValue, Map.of("{{PIN}}", authorizationCode));

        response.challengeInfoText(description + "\n\n" + additionalDescription)
                .acsUiType(OOB)
                .oobContinueLabel(translationService.translate(language, "screens.auth.appBasedContinue", "CONFIRM"));
        return response;
    }

    @Override
    public TemplateVariables.TemplateVariablesBuilder getAuthSuccessPage(String language, String amountAndCurrency,
                                                                         String merchantName,
                                                                         Optional<BusinessException> error) {
        final var response = getBaseTemplate(language, error);
        response.challengeInfoText(
                translationService.translate(
                        language,
                        "screens.authSuccess.justPaid",
                        "Payment for <bold>{{sum}}</bold> has been confirmed",
                        Map.of(
                                "<bold>{{sum}}</bold>", amountAndCurrency,
                                "<bold>{{merchantName}}</bold>", merchantName
                        )
                )
        ).acsUiType(OOB).oobContinueLabel(translationService.translate(language, "screens.authSuccess.backToMerchantButton", "BACK TO THE MERCHANT"));
        return response;
    }

    @Override
    public TemplateVariables.TemplateVariablesBuilder getFatalFailurePage(String language, BusinessException error) {
        final var response = getBaseTemplate(language, Optional.of(error));
        response.challengeInfoText(translationService.translateError(language, error))
                .acsUiType(OOB)
                .oobContinueLabel(translationService.translate(language, "screens.failure.backToMerchantButton", "BACK TO THE MERCHANT"));
        return response;
    }

    @Override
    public TemplateVariables.TemplateVariablesBuilder getNonFatalFailurePage(String language, BusinessException error) {
        final var response = getBaseTemplate(language, Optional.of(error));
        response.challengeInfoText(translationService.translateError(language, error))
                .acsUiType(OOB)
                .oobContinueLabel(translationService.translate(language, "screens.authFailure.buttons.tryAgain", "BACK"));
        return response;
    }

    protected List<Map<String, String>> getListOfAvailableAuthMethods(String language, Collection<AuthMethod> authMethods) {
        List<Map<String, String>> translatedAuthMethods = new ArrayList();
        authMethods.forEach(
            method -> translatedAuthMethods.add(
                Map.of(
                    method.name(),
                    translationService.translate(language, method.getTranslationJsonPath(), method.getInternationalName()))
            )
        );
        return translatedAuthMethods;
    }

    protected TemplateVariables.TemplateVariablesBuilder getBaseTemplate(String language, Optional<BusinessException> error) {
        String baseUrl = buildBaseUrl();
        String visaLogoUrl = baseUrl + "/content/images/app-visa-secure.png";
        String bankLogoUrl = baseUrl + "/content/images/app-bank-logo.png";
        return TemplateVariables.builder()
                .challengeInfoHeader(translationService.translate(language, "appBasedFlow.header", "Payment Authentication"))
                .challengeInfoTextIndicator(error.isPresent() ? ERROR_INDICATOR : DEFAULT_INDICATOR)
                .psImage(Image.builder().medium(visaLogoUrl).high(visaLogoUrl).extraHigh(visaLogoUrl).build())
                .psImageURL(imageToBase64String("/static/images/app-visa-secure.png"))
                .issuerImageURL(imageToBase64String("/static/images/app-bank-logo.png"))
                .issuerImage(Image.builder().medium(bankLogoUrl).high(bankLogoUrl).extraHigh(bankLogoUrl).build());
    }

    protected String imageToBase64String(String imageName) {
        byte[] fileContent;
        String encodedString = "";
        try (InputStream in = getClass().getResourceAsStream(imageName)) {
            fileContent = IOUtils.toByteArray(in);
            encodedString = Base64.getEncoder().encodeToString(fileContent);
        } catch (Exception e) {
            log.error("Failed to get image as base64 string {}", e.getMessage(), e);
        }
        return "data:image/png;base64, " + encodedString;
    }

    private String buildBaseUrl() {
        return appProperties.getApplicationHostBaseUrl() + contextPath;
    }

}
