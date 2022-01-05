package com.bank.acs.service.lt;

import com.bank.acs.config.property.AppProperties;
import com.bank.acs.config.property.AppPropertiesLT;
import com.bank.acs.dto.banktron.BanktronAuthenticateResponseDto;
import com.bank.acs.dto.banktron.BanktronCheckStatusResponseDto;
import com.bank.acs.dto.banktron.BanktronGetPersonsResponseDto;
import com.bank.acs.dto.banktron.BanktronResponseDto;
import com.bank.acs.enumeration.AcsErrorCode;
import com.bank.acs.enumeration.banktron.BanktronAuthMethod;
import com.bank.acs.enumeration.banktron.BanktronEndpoint;
import com.bank.acs.enumeration.banktron.BanktronErrorCode;
import com.bank.acs.exception.BusinessException;
import com.bank.acs.service.RestServiceBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.bank.acs.Profile.COUNTRY_LT_PROFILE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Profile(COUNTRY_LT_PROFILE)
@Service
public class BanktronService extends RestServiceBase {

    protected final AppPropertiesLT appPropertiesLT;

    public BanktronService(RestTemplate restTemplate, ObjectMapper objectMapper, AppPropertiesLT appPropertiesLT,
                           AppProperties appProperties) {
        super(restTemplate, objectMapper, appProperties);
        this.appPropertiesLT = appPropertiesLT;
        log.info("Banktron starts on link = {}", appPropertiesLT.getSonicUrl());
    }

    public BanktronGetPersonsResponseDto createSessionAndGetPersonLogins(String cardHolderPersonalCode, String cardCountry) {

        /*
		    CARD_BLOCKED_ECOMMERCE, CARD_BLOCKED_RMS, CARD_BLOCKED_CMS, MAX_TIME_TO_AUTHENTICATE_HAS_PASSED
		    Person Status != Active WRONG_IB_STATUS
		    {"error_code":"-192","error_message":"You have not activated mobile e-signature in Interneto linija"}
		    {"error_code":"-40013","error_message":"Login is settings is incorrect. The most common reasons are: 1.Incorrect user ID. 2. Person is not certificated for signing with Smart-ID."}
        */
        final var banktronEndpoint = BanktronEndpoint.PERSONLOGINS;

        final var requestData = getDefaultRequestWrapper(BanktronGetPersonsResponseDto.class)
                .url(appPropertiesLT.getSonicUrl() + banktronEndpoint.getLink())
                .data(Map.of(BanktronRequestKeys.PERSON_CODE, cardHolderPersonalCode, BanktronRequestKeys.COUNTRY, cardCountry))
                .build();

        final var responseBody = sendToBanktron(requestData, banktronEndpoint);
        Assert.notNull(responseBody, "Response body must not be null");
        return responseBody;
    }

    public BanktronAuthenticateResponseDto authenticate(String banktronSessionToken, String apiUsername, BanktronAuthMethod authMethod) {

        final var banktronEndpoint = BanktronEndpoint.AUTHENTICATE;

        final var requestData = getDefaultRequestWrapper(BanktronAuthenticateResponseDto.class)
                .url(appPropertiesLT.getSonicUrl() + banktronEndpoint.getLink())
                .data(Map.of(
                        BanktronRequestKeys.SESSION_TOKEN, banktronSessionToken,
                        BanktronRequestKeys.LOGIN, apiUsername,
                        BanktronRequestKeys.AUTHENTICATION_TYPE_ID, String.valueOf(authMethod.getCode())
                ))
                .build();

        final var responseBody = sendToBanktron(requestData, banktronEndpoint);
        Assert.notNull(responseBody, "Response body must not be null");
        return responseBody;
    }

    public void logout(String banktronSessionToken) {
        if (banktronSessionToken == null) {
            log.warn("Banktron Logout: session token is missing");
            return;
        }

        final var banktronEndpoint = BanktronEndpoint.LOGOUT;

        final var requestData = getDefaultRequestWrapper(BanktronResponseDto.class)
                .url(appPropertiesLT.getSonicUrl() + banktronEndpoint.getLink())
                .data(Map.of(BanktronRequestKeys.SESSION_TOKEN, banktronSessionToken))
                .build();

        final var responseBody = sendToBanktron(requestData, banktronEndpoint);
    }

    /**
     * Check whether request was signed (m-signature and smart-ID)
     */
    public BanktronCheckStatusResponseDto checkAuthMethodStatus(String banktronSessionToken) {

        final var banktronEndpoint = BanktronEndpoint.CHECKSTATUS;

        final var requestData = getDefaultRequestWrapper(BanktronCheckStatusResponseDto.class)
                .url(appPropertiesLT.getSonicUrl() + banktronEndpoint.getLink())
                .data(Map.of(BanktronRequestKeys.SESSION_TOKEN, banktronSessionToken))
                .build();

        return sendToBanktron(requestData, banktronEndpoint);
    }

    public void notifyConfirmCredentials(String sessionToken, String calculatorCode) {

        final var banktronEndpoint = BanktronEndpoint.CONFIRMCREDENTIALS;

        final var requestData = getDefaultRequestWrapper(BanktronResponseDto.class)
                .url(appPropertiesLT.getSonicUrl() + banktronEndpoint.getLink())
                .data(Map.of(
                        BanktronRequestKeys.SESSION_TOKEN, sessionToken,
                        BanktronRequestKeys.AUTHENTICATION_CODE, calculatorCode
                ))
                .build();
        final var responseBody = sendToBanktron(requestData, banktronEndpoint);
    }

    protected <T extends BanktronResponseDto> T sendToBanktron(RequestWrapper<T, BanktronResponseDto, Map> requestData, BanktronEndpoint banktronEndpoint) {
        final var response = sendPost(requestData, error ->
                new BusinessException(BanktronErrorCode.findAcsErrorByBanktronErrorCode(error.getErrorCode(), banktronEndpoint))
        );
        if (response.getErrorCode() != null && !response.getErrorCode().isBlank()) {
            log.info("Banktron respond with errorCode = {} and message = {}", response.getErrorCode(), response.getErrorMessage());
            throw new BusinessException(BanktronErrorCode.findAcsErrorByBanktronErrorCode(response.getErrorCode(), banktronEndpoint));
        }
        log.info("Banktron respond OK");
        return response;
    }

    protected <T> RequestWrapper.RequestWrapperBuilder<T, BanktronResponseDto, Map> getDefaultRequestWrapper(Class<T> responseDto) {
        return RestServiceBase.RequestWrapper.<T, BanktronResponseDto, Map>builder()
                .headers(buildHeaders())
                .responseDto(responseDto)
                .errorDto(BanktronResponseDto.class);
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = getDefaultHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.setAccept(List.of(APPLICATION_JSON));
        headers.setCacheControl(CacheControl.noCache());
        headers.setHost(new InetSocketAddress(appPropertiesLT.getSonicHostname(), appPropertiesLT.getSonicPort()));
        return headers;
    }

}
