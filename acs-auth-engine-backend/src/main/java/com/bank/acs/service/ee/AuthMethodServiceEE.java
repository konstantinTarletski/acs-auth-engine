package com.bank.acs.service.ee;

import com.bank.acs.config.property.AppProperties;
import com.bank.acs.config.property.MaxAttemptsProperties;
import com.bank.acs.dto.ciam.CIAMAccessTokenResponseDO;
import com.bank.acs.dto.ciam.CIAMAuthenticationCallbackDO;
import com.bank.acs.dto.ciam.CIAMAuthenticationInputOutputDO;
import com.bank.acs.dto.ciam.CIAMAuthenticationResponseDO;
import com.bank.acs.entity.AppSession;
import com.bank.acs.entity.ciam.CiamData;
import com.bank.acs.enumeration.AcsErrorCode;
import com.bank.acs.enumeration.AppState;
import com.bank.acs.enumeration.AuthMethod;
import com.bank.acs.enumeration.ciam.CiamAuthMethod;
import com.bank.acs.exception.BusinessException;
import com.bank.acs.repository.AppSessionRepository;
import com.bank.acs.repository.CiamRepository;
import com.bank.acs.service.AppStateService;
import com.bank.acs.service.AuthMethodService;
import com.bank.acs.service.ciam.CiamService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.thymeleaf.util.StringUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;

import static com.bank.acs.Profile.COUNTRY_EE_PROFILE;
import static com.bank.acs.enumeration.AppState.RENDER_SUCCESSFUL_PAGE;
import static com.bank.acs.enumeration.AuthMethod.*;
import static com.bank.acs.service.ciam.CiamConstants.*;

@Slf4j
@Profile(COUNTRY_EE_PROFILE)
@Service
public class AuthMethodServiceEE extends AuthMethodService {

    protected final UserServiceEE userServiceEE;
    protected final CiamService ciamService;
    protected final CiamRepository ciamRepository;
    protected final ObjectMapper objectMapper;

    public AuthMethodServiceEE(
            MaxAttemptsProperties maxAttemptsProperties,
            AppSessionRepository appSessionRepository,
            AppStateService appStateService,
            AppProperties appProperties,
            UserServiceEE userServiceEE,
            CiamService ciamService,
            CiamRepository ciamRepository,
            ObjectMapper objectMapper
    ) {
        super(maxAttemptsProperties, appSessionRepository, appStateService, appProperties);
        this.userServiceEE = userServiceEE;
        this.ciamService = ciamService;
        this.ciamRepository = ciamRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handleSelectedAuthMethod(AppSession session, AuthMethod selectedAuthMethod) {
        //Re-init AUTH (need re-login to change AUTH method)
        if (session.getUsedAuthMethod() != null) {
            log.info("There is already auth method saved, going to reset Ciam session");
            userServiceEE.resetCiamSession(session);
        }
        super.handleSelectedAuthMethod(session, selectedAuthMethod);
    }

    @Override
    public String initSmartId(AppSession session) {
        Assert.isTrue(session.getUsedAuthMethod() == SMART_ID, "Session auth method is not SmartID");
        final var response = authInit(session, null);
        return getVerificationCode(response);
    }

    @Override
    public String initMSignature(AppSession session) {
        Assert.isTrue(session.getUsedAuthMethod() == M_SIGNATURE, "Session auth method is not SmartID");
        final var response = authInit(session, null);
        return getVerificationCode(response);
    }

    @SneakyThrows
    @Override
    public String initIDCard(AppSession session) {
        URI ciamIDCardRedirectUri = ciamService.buildIDCardRedirectUri(
                session.getUsedLanguage(), session.getChosenUsername(), session.getCardCountry());
        return URLEncoder.encode(ciamIDCardRedirectUri.toString(), "UTF-8");
    }

    @Override
    public Optional<Boolean> checkAuthMethodStatus(AppSession session) {

        final var response = new CheckStatusDataHolder();

        ciamRepository.findById(session.getAcsTransactionId()).ifPresent(
                ciamData -> {
                    try {
                        final var authBody = objectMapper.readValue(ciamData.getAuthenticateResponse(), CIAMAuthenticationResponseDO.class);
                        final var authResponse = ciamService.authenticate(
                                CiamAuthMethod.toCiamAuthMethod(session.getUsedAuthMethod()),
                                authBody,
                                session.getUsedLanguage(),
                                session.getChosenUsername(),
                                session.getCardCountry(),
                                null,
                                false
                        );
                        final var authenticateResponse = objectMapper.writeValueAsString(authResponse);
                        ciamData.setAuthenticateResponse(authenticateResponse);
                        ciamRepository.save(ciamData);
                        processCIAMAuthenticationResponse(session, authResponse);

                        response.tokenId = authResponse.getTokenId();

                    } catch (JsonProcessingException e) {
                        throw new BusinessException(AcsErrorCode.AUTHENTICATION_FAILED);
                    }
                }
        );
        return response.tokenId != null ? Optional.of(true) : Optional.empty();
    }

    @Override
    public AppState confirmCodeCalculator(AppSession session, String confirmationCode) {
        Assert.isTrue(session.getUsedAuthMethod() == CODE_CALCULATOR, "Session auth method is not Code Calculator");
        final var response = authInit(session, confirmationCode);
        if (!StringUtils.isEmpty(response.getTokenId())) {
            userServiceEE.logout(session);
            return RENDER_SUCCESSFUL_PAGE;
        }
        throw new BusinessException(AcsErrorCode.WRONG_CODE_FOR_CODE_CALCULATOR);
    }

    @Override
    public AppState confirmIDCard(AppSession session, String authorizationCode) {
        if (StringUtils.isEmpty(authorizationCode)) {
            throw new BusinessException(AcsErrorCode.AUTHENTICATION_FAILED);
        }

        CIAMAccessTokenResponseDO accessTokenDO = ciamService.obtainAccessToken(authorizationCode);

        if(!StringUtils.isEmpty(accessTokenDO.getAccessToken())) {
            userServiceEE.logout(session);
            return RENDER_SUCCESSFUL_PAGE;
        }

        throw new BusinessException(AcsErrorCode.AUTHENTICATION_FAILED);
    }

    protected CIAMAuthenticationResponseDO authInit(AppSession session, String confirmationCode) {
        final var result = ciamService.authenticate(
                CiamAuthMethod.toCiamAuthMethod(session.getUsedAuthMethod()),
                null,
                session.getUsedLanguage(),
                session.getChosenUsername(),
                session.getCardCountry(),
                confirmationCode,
                true
        );
        processCIAMAuthenticationResponse(session, result);
        return result;
    }

    protected void processCIAMAuthenticationResponse(AppSession session, CIAMAuthenticationResponseDO input) {
        String message = getErrorMessage(input);

        if (message != null) {
            log.warn("Get error from CIAM, acsTransactionId = {}, message = {}", session.getAcsTransactionId(), message);
            throw new BusinessException(message);
        }
        final var ciamData = ciamRepository.findById(session.getAcsTransactionId()).orElseGet(() -> getCiamData(session));
        try {
            final var authenticateResponse = objectMapper.writeValueAsString(input);
            ciamData.setAuthenticateResponse(authenticateResponse);
            ciamRepository.save(ciamData);
        } catch (JsonProcessingException e) {
            log.warn("Error parsing CIAM authenticate response to JSON, acsTransactionId = {}, authenticateResponse = {}", session.getAcsTransactionId(), input);
            throw new BusinessException(AcsErrorCode.JSON_BODY_VALIDATION_EXCEPTION);
        }
    }

    protected CiamData getCiamData(AppSession session) {
        return CiamData.builder()
                .acsTransactionId(session.getAcsTransactionId())
                .build();
    }

    protected String getVerificationCode(CIAMAuthenticationResponseDO response) {
        return getCallbackOutput(response.getCallbacks(), CALLBACK_PIN, OUTPUT_PIN);
    }

    protected String getWaitTimeStr(CIAMAuthenticationResponseDO response) {
        return getCallbackOutput(response.getCallbacks(), CALLBACK_POLLING_WAIT_TIME, OUTPUT_POLLING_WAIT_TIME);
    }

    protected String getErrorMessage(CIAMAuthenticationResponseDO response) {
        return getCallbackOutput(response.getCallbacks(), CALLBACK_MESSAGE, OUTPUT_MESSAGE);
    }

    protected static String getCallbackOutput(List<CIAMAuthenticationCallbackDO> callbacks, String type, String outputName) {
        if (callbacks == null) {
            return null;
        }
        Optional<CIAMAuthenticationCallbackDO> callback = callbacks.stream().filter(cb -> type.equals(cb.getType())).findFirst();
        if (!callback.isPresent()) {
            return null;
        }
        Optional<CIAMAuthenticationInputOutputDO> output = callback.get().getOutput().stream()
                .filter(o -> outputName.equals(o.getName())).findFirst();
        return output.isPresent() ? output.get().getValue().toString() : null;
    }

    protected static class CheckStatusDataHolder {

        private String tokenId;

    }

}
