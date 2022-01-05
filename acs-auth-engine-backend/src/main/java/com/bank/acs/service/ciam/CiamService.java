package com.bank.acs.service.ciam;

import com.bank.acs.config.property.AppProperties;
import com.bank.acs.config.property.AppPropertiesEE;
import com.bank.acs.dto.ciam.CIAMAccessTokenResponseDO;
import com.bank.acs.dto.ciam.CIAMAuthenticationResponseDO;
import com.bank.acs.dto.ciam.CiamError;
import com.bank.acs.enumeration.ciam.CiamAuthMethod;
import com.bank.acs.exception.BusinessException;
import com.bank.acs.service.RestServiceBase;
import com.bank.acs.service.ciam.jw.JWEBuilder;
import com.bank.acs.service.ciam.jw.JwsAssertionSigner;
import com.bank.acs.service.ciam.jw.NimbusJWKSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.bank.acs.Profile.COUNTRY_EE_PROFILE;
import static com.bank.acs.service.ciam.OauthRequestKeys.*;
import static com.bank.acs.service.ciam.jw.JwConstants.*;
import static com.bank.acs.util.UrlQueryUtil.resourceToString;

@Slf4j
@Profile(COUNTRY_EE_PROFILE)
@Service
public class CiamService extends RestServiceBase {

    private static final String ACCEPT_API_VERSION_HEADER_KEY = "Accept-API-Version";
    private static final String ACCEPT_API_VERSION_HEADER_VALUE = "protocol=1.0,resource=2.0";

    protected final AppPropertiesEE appPropertiesEE;

    private static final String AUTHORIZATION_VALUE_PREFIX = "Bearer ";

    public CiamService(RestTemplate restTemplate, ObjectMapper objectMapper, AppPropertiesEE appPropertiesEE, AppProperties appProperties) {
        super(restTemplate, objectMapper, appProperties);
        this.appPropertiesEE = appPropertiesEE;
        log.info("CIAM Authenticate starts on url = {}", appPropertiesEE.getCiamAuthenticateUrl());
        log.info("CIAM jwkUri starts on url = {}", appPropertiesEE.getCiamJwkUriUrl());
        log.info("CIAM AccessToken starts on url = {}", appPropertiesEE.getCiamAccessTokenUrl());
        log.info("CIAM Logout, url = {}", appPropertiesEE.getCiamLogoutUrl());
    }

    public CIAMAccessTokenResponseDO obtainSTSAccessToken() {

        log.info("CIAM STS AccessToken url = {}", appPropertiesEE.getCiamAccessTokenUrl());
        log.info("CIAM STS AccessToken aud = {}", appPropertiesEE.getCiamStsAud());

        Map<String, Object> assertionMap = Map.of(
                SUBJECT_PARAM_KEY, appPropertiesEE.getStsOAuthClientId(),
                ISSUER_PARAM_KEY, appPropertiesEE.getStsOAuthClientId(),
                AUDIENCE_PARAM_KEY, appPropertiesEE.getCiamStsAud()
        );

        final var clientAssertion = createJws(assertionMap, appPropertiesEE.getCiamStsRsaPrivateKey());

        final var resultUri = UriComponentsBuilder.fromUriString(appPropertiesEE.getCiamAccessTokenUrl())
                .queryParam(REALM_KEY, appPropertiesEE.getStsRealmValue())
                .queryParam(GRANT_TYPE_KEY, CLIENT_CREDENTIALS)
                .queryParam(CLIENT_ID_KEY, appPropertiesEE.getStsOAuthClientId())
                .queryParam(CLIENT_ASSERTION_TYPE_KEY, appPropertiesEE.getClientAssertionTypeJwtBearer())
                .queryParam(CLIENT_ASSERTION_KEY, clientAssertion)
                .build().toUri();

        final var requestData = RequestWrapper.<CIAMAccessTokenResponseDO, CiamError, CIAMAccessTokenResponseDO>builder()
                .headers(buildHeaders())
                .url(resultUri.toString())
                .data(null)
                .responseDto(CIAMAccessTokenResponseDO.class)
                .errorDto(CiamError.class)
                .build();

        return super.sendPost(requestData, error -> new BusinessException(error.getErrorDescription()));
    }

    public CIAMAccessTokenResponseDO obtainAccessToken(String authorizationCode) {

        log.info("CIAM AccessToken url = {}", appPropertiesEE.getCiamAccessTokenUrl());
        log.info("CIAM AccessToken aud = {}", appPropertiesEE.getCiamAud());

        Map<String, Object> assertionMap = Map.of(
                SUBJECT_PARAM_KEY, appPropertiesEE.getOAuthClientId(),
                ISSUER_PARAM_KEY, appPropertiesEE.getOAuthClientId(),
                AUDIENCE_PARAM_KEY, appPropertiesEE.getCiamAud()
        );

        final var clientAssertion = createJws(assertionMap, appPropertiesEE.getCiamRsaPrivateKey());

        final var resultUri = UriComponentsBuilder.fromUriString(appPropertiesEE.getCiamAccessTokenUrl())
                .queryParam(REALM_KEY, appPropertiesEE.getRealmValue())
                .queryParam(GRANT_TYPE_KEY, AUTHORIZATION_CODE)
                .queryParam(CLIENT_ID_KEY, appPropertiesEE.getOAuthClientId())
                .queryParam(CLIENT_ASSERTION_TYPE_KEY, appPropertiesEE.getClientAssertionTypeJwtBearer())
                .queryParam(CLIENT_ASSERTION_KEY, clientAssertion)
                .queryParam(REDIRECT_URI_KEY,
                        appProperties.getApplicationHostBaseUrl() +
                                appPropertiesEE.getContextPath() +
                                appProperties.getIdCardRedirectUrl()
                )
                .queryParam(CODE, authorizationCode)
                .build().toUri();

        final var requestData = RequestWrapper.<CIAMAccessTokenResponseDO, CiamError, CIAMAccessTokenResponseDO>builder()
                .headers(buildHeaders())
                .url(resultUri.toString())
                .data(null)
                .responseDto(CIAMAccessTokenResponseDO.class)
                .errorDto(CiamError.class)
                .build();

        return super.sendPost(requestData, error -> new BusinessException(error.getErrorDescription()));
    }

    @SneakyThrows
    public CIAMAuthenticationResponseDO authenticate(
            CiamAuthMethod ciamAuthMethod,
            CIAMAuthenticationResponseDO authenticateRequestBody,
            String language,
            String username,
            String country,
            String confirmationCode,
            boolean withAuth
    ) {

        log.info("CIAM Authenticate, url = {}", appPropertiesEE.getCiamAuthenticateUrl());

        final var resultUriBuilder = UriComponentsBuilder.fromUriString(appPropertiesEE.getCiamAuthenticateUrl())
                .queryParam(REALM_KEY, appPropertiesEE.getRealmValue())
                .queryParam(LOCALE_KEY, language)
                .queryParam(BYPASS_KEY, false);

        if (withAuth) {
            final var auth = createdAuth(ciamAuthMethod, username, country, confirmationCode);
            resultUriBuilder.queryParam(AUTH_KEY, auth);
        }

        final var resultUri = resultUriBuilder.build().toUri();

        final var requestData = RequestWrapper.<CIAMAuthenticationResponseDO, CiamError, CIAMAuthenticationResponseDO>builder()
                .headers(buildHeaders())
                .url(resultUri.toString())
                .data(authenticateRequestBody)
                .responseDto(CIAMAuthenticationResponseDO.class)
                .errorDto(CiamError.class)
                .build();

        return super.sendPost(requestData, error -> new BusinessException(error.getErrorDescription()));
    }

    public URI buildIDCardRedirectUri(String language, String username, String country) {
        final var auth = createdAuth(CiamAuthMethod.IDCARD, username, country, null);
        final UriComponentsBuilder resultUriBuilder = UriComponentsBuilder.fromUriString(appPropertiesEE.getCiamIdCardRedirectUrl())
                .queryParam(RESPONSE_TYPE_KEY, CODE)
                .queryParam(CLIENT_ID_KEY, appPropertiesEE.getOAuthClientId())
                .queryParam(REDIRECT_URI_KEY,
                        appProperties.getApplicationHostBaseUrl() +
                                appPropertiesEE.getContextPath()  +
                                appProperties.getIdCardRedirectUrl()
                )
                .queryParam(AUTH_KEY, auth)
                .queryParam(REALM_KEY, appPropertiesEE.getRealmValue())
                .queryParam(LOCALE_KEY, language)
                .queryParam(NONCE_KEY, UUID.randomUUID().toString());

        return resultUriBuilder.build().toUri();
    }

    public String createdAuth(CiamAuthMethod ciamAuthMethod, String username, String country, String confirmationCode) {

        log.info("CIAM jwkUri, url = {}", appPropertiesEE.getCiamJwkUriUrl());

        final var authprmsMap = new HashMap<String, Object>();
        authprmsMap.put(ISSUER_PARAM_KEY, appPropertiesEE.getOAuthClientId());
        authprmsMap.put(USERNAME_PARAM_KEY, username);
        authprmsMap.put(AUTH_METHOD_PARAM_KEY, ciamAuthMethod.name().toLowerCase());
        authprmsMap.put(BANK_COUNTRY_PARAM_KEY, country);
        if (confirmationCode != null && !confirmationCode.isBlank()) {
            authprmsMap.put(PIN_CODE_PARAM_KEY, confirmationCode);
        }

        final var authprms = createJws(authprmsMap, appPropertiesEE.getCiamRsaPrivateKey());
        Map<String, Object> authMap = Map.of(TOKEN_AUTHPRMS_BODY_PARAM_KEY, authprms);

        final var jwkUri = UriComponentsBuilder.fromUriString(appPropertiesEE.getCiamJwkUriUrl())
                .queryParam(REALM_KEY, appPropertiesEE.getRealmValue())
                .build();
        final var keySource = new NimbusJWKSupport(jwkUri.toUri());
        final var jweBuilder = JWEBuilder.newInstance(keySource, appPropertiesEE.getCiamEncKid());

        return jweBuilder.setClaims(authMap).build();
    }

    public void logoutFromCiam(String cardCountry, String username) {

        final var token = obtainSTSAccessToken();

        log.info("CIAM Logout, url = {}", appPropertiesEE.getCiamLogoutUrl());

        final Map<String, Object> body = Map.of(
                BANK_COUNTRY_KEY, cardCountry,
                USERNAME_KEY, username
        );

        final var resultUri = UriComponentsBuilder.fromUriString(appPropertiesEE.getCiamLogoutUrl())
                .build().toUri();

        final var headers = buildHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(appPropertiesEE.getCccAuthorizationHeader(), AUTHORIZATION_VALUE_PREFIX + token.getAccessToken());

        final var requestData = RequestWrapper.<String, CiamError, Map>builder()
                .headers(headers)
                .url(resultUri.toString())
                .data(body)
                .responseDto(String.class)
                .errorDto(CiamError.class)
                .build();

        super.sendPost(requestData, error -> new BusinessException(error.getErrorDescription()));
    }

    protected String createJws(Map<String, Object> toSign, Resource ciamPrivateKey) {
        final var signerRSAPrivateKey = resourceToString(ciamPrivateKey);
        log.info("createJws: ciamRsaPrivateKey ({}) length = {}", ciamPrivateKey.getFilename(), signerRSAPrivateKey.length());
        log.info("createJws: Map to sign = {}", toSign);
        final var rsaKey = JwsAssertionSigner.getRSAPrivateKey(signerRSAPrivateKey);
        return JwsAssertionSigner.newJws(appPropertiesEE.getCiamTokenTtl(), toSign, rsaKey);
    }

    protected HttpHeaders buildHeaders() {
        HttpHeaders headers = getDefaultHeaders();
        headers.set(ACCEPT_API_VERSION_HEADER_KEY, ACCEPT_API_VERSION_HEADER_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return headers;
    }

}
