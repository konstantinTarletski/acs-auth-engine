package com.bank.acs.service;

import com.bank.acs.config.property.AppProperties;
import com.bank.acs.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static com.bank.acs.Profile.*;
import static com.bank.acs.enumeration.AcsErrorCode.FATAL_EXCEPTION;
import static com.bank.acs.util.UrlQueryUtil.resourceToString;

@Slf4j
@RequiredArgsConstructor
@Service
public class TranslationService {

    public static final String DEFAULT_LANGUAGE = "en";

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value(value = "classpath:translations-lt.json")
    private Resource translationsInternalLT;
    @Value(value = "classpath:translations-lv.json")
    private Resource translationsInternalLV;
    @Value(value = "classpath:translations-ee.json")
    private Resource translationsInternalEE;

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    public String getTranslations() {

        final var fileName = appProperties.getTranslationsPath() + FileSystems.getDefault().getSeparator() + appProperties.getTranslationsFilename();

        try {
            log.info("Trying to read translation from = {}", fileName);
            String text = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
            Map<String, String> map = objectMapper.readValue(text, Map.class);

            if (map != null && !map.isEmpty()) {
                log.info("Reading from file {} success", fileName);
                return text;
            }
        } catch (Exception e) {
            log.warn("Translations can not be loaded from filename = {}", fileName);
        }

        try {
            Resource internalTranslations = getInternalTranslations();
            log.info("Reading internal translations from file {}", internalTranslations.getURL());
            return resourceToString(internalTranslations);
        } catch (Exception e) {
            log.info("Internal translations can not be loaded");
        }

        log.info("Translations can not be loaded");
        return null;
    }

    private Resource getInternalTranslations() {
        switch(activeProfile) {
            case COUNTRY_LT_PROFILE:
                return translationsInternalLT;
            case COUNTRY_LV_PROFILE:
                return translationsInternalLV;
            case COUNTRY_EE_PROFILE:
                return translationsInternalEE;
            default:
                throw new BusinessException(FATAL_EXCEPTION);
        }
    }

    public String getMessageByJsonPath(String jsonPath, String defaultValue) {
        try {
            final var translations = getTranslations();
            if (translations != null && !translations.isBlank() && jsonPath != null && !jsonPath.isBlank()) {

                ObjectNode rootNode = objectMapper.readValue(translations, ObjectNode.class);
                final var nodes = jsonPath.split("\\.");

                JsonNode lastNode = rootNode;
                for (String node : nodes) {
                    lastNode = lastNode.path(node);
                }
                return lastNode.textValue() != null ? lastNode.textValue() : defaultValue;
            }
        } catch (Exception e) {
            log.warn("Could not get translation for JSON path = {}", jsonPath);
        }
        return null;
    }

    public String translate(String language, String translationPath, String defaultValue) {
        final var jsonPath = new StringBuilder(language != null ? language : DEFAULT_LANGUAGE).append(".").append(translationPath).toString();
        return getMessageByJsonPath(jsonPath, defaultValue);
    }

    public String translate(String language, String translationPath, String defaultValue, Map<String, String> variables) {
        final var jsonPath = new StringBuilder(language != null ? language : DEFAULT_LANGUAGE).append(".").append(translationPath).toString();
        String message = getMessageByJsonPath(jsonPath, defaultValue);
        for (Map.Entry<String,String> entry : variables.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        return message;
    }

    public String translateError(String language, BusinessException error) {
        String errorTitle = translateErrorTitle(language, error);
        String errorDescription = translateErrorDescription(language, error);
        return new StringBuilder(errorTitle).append("\n").append(errorDescription).toString();
    }

    public String translateErrorTitle(String language, BusinessException error) {
        String errorTitleJsonPath = new StringBuilder(language != null ? language : DEFAULT_LANGUAGE)
                .append(".errors.")
                .append(error.getErrorCode())
                .append(".title")
                .toString();
        return getMessageByJsonPath(errorTitleJsonPath, "Payment was not authenticated");
    }

    public String translateErrorDescription(String language, BusinessException error) {
        String errorDescriptionJsonPath = new StringBuilder(language != null ? language : DEFAULT_LANGUAGE)
                .append(".errors.")
                .append(error.getErrorCode())
                .append(".description")
                .toString();
        return getMessageByJsonPath(errorDescriptionJsonPath, "Please try again.");
    }

}
