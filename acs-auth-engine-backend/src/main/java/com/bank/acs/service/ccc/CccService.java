package com.bank.acs.service.ccc;

import com.bank.acs.config.property.AppProperties;
import com.bank.acs.config.property.AppPropertiesEE;
import com.bank.acs.dto.ccc.CCCErrorDO;
import com.bank.acs.dto.ccc.CCCUsernameAndLanguagesDO;
import com.bank.acs.enumeration.AcsErrorCode;
import com.bank.acs.exception.BusinessException;
import com.bank.acs.service.RestServiceBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static com.bank.acs.Profile.COUNTRY_EE_PROFILE;

@Slf4j
@Profile(COUNTRY_EE_PROFILE)
@Service
public class CccService extends RestServiceBase {

    public static final String AUTHORIZATION_VALUE_PREFIX = "Bearer ";
    public static final String REG_CODE_PARAM = "regCode";
    public static final String COUNTRY_PARAM = "country";
    public static final String CCC_ERROR_CODE_FOR_NO_IB_LOGIN = "error.customer.notFound";
    public static final String CCC_ERROR_CODE_FOR_USER_BLOCKED = "error.user.blocked";

    protected final AppPropertiesEE appPropertiesEE;

    public CccService(RestTemplate restTemplate, ObjectMapper objectMapper, AppPropertiesEE appPropertiesEE, AppProperties appProperties) {
        super(restTemplate, objectMapper, appProperties);
        this.appPropertiesEE = appPropertiesEE;
        log.info("CCC starts on link = {}", appPropertiesEE.getCccUrl());
    }

    public CCCUsernameAndLanguagesDO getUserNameAndLanguage(String accessToken, String regCode, String country) {

        final var requestData = RequestWrapper.<CCCUsernameAndLanguagesDO, CCCErrorDO, Map>builder()
                .headers(buildHeaders(accessToken))
                .url(appPropertiesEE.getCccUrl())
                .data(Map.of(REG_CODE_PARAM, regCode, COUNTRY_PARAM, country))
                .responseDto(CCCUsernameAndLanguagesDO.class)
                .errorDto(CCCErrorDO.class)
                .build();

        return super.sendPost(requestData, error -> resolveExceptionFromError(error));
    }

    protected HttpHeaders buildHeaders(String accessToken) {
        HttpHeaders headers = getDefaultHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(appPropertiesEE.getCccAuthorizationHeader(), AUTHORIZATION_VALUE_PREFIX + accessToken);
        return headers;
    }

    private BusinessException resolveExceptionFromError(CCCErrorDO error) {
        if (CCC_ERROR_CODE_FOR_NO_IB_LOGIN.equals(error.getCode())) {
            return new BusinessException(AcsErrorCode.NO_LOGINS_AVAILABLE);
        } else if (CCC_ERROR_CODE_FOR_USER_BLOCKED.equals(error.getCode())) {
            return new BusinessException(AcsErrorCode.BLOCKED_LOGIN_CODE);
        }
        return new BusinessException(AcsErrorCode.INTERNAL_SERVICE_IS_NOT_REACHABLE);
    }
}
