package com.bank.acs.service;

import com.bank.acs.config.property.AppProperties;
import com.bank.acs.dto.challenge.request.ChallengeRequestDto;
import com.bank.acs.dto.challenge.response.ChallengeResponseDto;
import com.bank.acs.dto.challenge.response.TemplateVariables;
import com.bank.acs.entity.AppSession;
import com.bank.acs.enumeration.ChallengeFlowType;
import com.bank.acs.enumeration.UiAction;
import com.bank.acs.repository.AppSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class AppNativeFlow extends FlowBase {

    protected final FlowAppService flowAppService;
    protected final ObjectMapper objectMapper;
    protected final AppNativeTemplatesService appNativeTemplatesService;

    public AppNativeFlow(
            AppSessionRepository appSessionRepository,
            AppStateService appStateService,
            LinkAppService linkAppService,
            UserService userService,
            UiActionService uiActionService,
            AppProperties appProperties,
            FlowAppService flowAppService,
            ObjectMapper objectMapper,
            AppNativeTemplatesService appNativeTemplatesService
    ) {
        super(appSessionRepository, appStateService, linkAppService, userService, uiActionService, appProperties);
        this.flowAppService = flowAppService;
        this.objectMapper = objectMapper;
        this.appNativeTemplatesService = appNativeTemplatesService;
    }

    @Override
    public ChallengeFlowType getChallengeFlowType() {
        return ChallengeFlowType.APP_NATIVE;
    }

    @Override
    public void handleAnyRequest(AppSession session,
                                 ChallengeRequestDto request,
                                 ChallengeResponseDto.ChallengeResponseDtoBuilder response,
                                 Map<String, String> params,
                                 String creq) {
        log.info("handleAnyRequest, acsTransactionId = {}, params = {}", session.getAcsTransactionId(), params);
        TemplateVariables templateVariables = flowAppService.getUi(session, null, Optional.empty(), appNativeTemplatesService).build();
        response.templateVariables(templateVariables);
        response.acsUiType(templateVariables.getAcsUiType().getCode());
    }

    @Override
    public void handleUiRequest(
            Map<String, String> uiResponse,
            AppSession session,
            ChallengeRequestDto request,
            ChallengeResponseDto.ChallengeResponseDtoBuilder response,
            Map<String, String> params,
            String creq) {
        log.info("handleUiRequest, acsTransactionId = {}, uiResponse = {}, params = {}", session.getAcsTransactionId(), uiResponse, params);
        TemplateVariables templateVariables = flowAppService.getUi(session, uiResponse, Optional.empty(), appNativeTemplatesService).build();
        response.templateVariables(templateVariables);
        response.acsUiType(templateVariables.getAcsUiType().getCode());
    }

    @Override
    public Map<String, String> getApplicationParameters(AppSession session,
                                                        ChallengeRequestDto request,
                                                        Map<String, String> parsedContent,
                                                        String creq) {
        log.info("getApplicationParameters, acsTransactionId = {}", session.getAcsTransactionId());
        final var parsedCreq = parseCreq(creq);
        log.info("getApplicationParameters, acsTransactionId = {}, parsedCreq = {}", session.getAcsTransactionId(), parsedCreq);
        UiAction action = flowAppService.getNextUiAction(session, getUserInput(parsedCreq));
        final var calculatedParameters = flowAppService.getApplicationParameters(action, getUserInput(parsedCreq));
        log.info("getApplicationParameters, acsTransactionId = {}, parsedCreq = {}, UiAction = {}, calculatedParameters = {}",
                session.getAcsTransactionId(), parsedCreq, action, calculatedParameters);
        return calculatedParameters;
    }

    @Override
    public UiAction getUiAction(AppSession session, ChallengeRequestDto request, Map<String, String> params, String creq) {
        final var parsedCreq = parseCreq(creq);
        log.info("getUiAction, acsTransactionId = {}, parsed Creq = {}", session.getAcsTransactionId(), parsedCreq);
        UiAction calculatedUiAction = null;
        if (isContinue(parsedCreq)) {// Empty parameter means 1 request
            calculatedUiAction = flowAppService.getNextUiAction(session, getUserInput(parsedCreq));
        }
        log.info("getUiAction, acsTransactionId = {}, parsed Creq = {}, calculatedUiAction = {}",
                session.getAcsTransactionId(), parsedCreq, calculatedUiAction);
        return calculatedUiAction;
    }

    protected static boolean isContinue(Creq parsedCreq) {
        return !isUserInputEmpty(parsedCreq) && (parsedCreq.getOobContinue() == null ||
                parsedCreq.getOobContinue() != null && parsedCreq.getOobContinue());
    }

    protected static boolean isUserInputEmpty(Creq parsedCreq) {
        return parsedCreq == null || (parsedCreq.getChallengeDataEntry() == null && parsedCreq.getOobContinue() == null);
    }

    protected static String getUserInput(Creq parsedCreq) {
        return parsedCreq != null && parsedCreq.getChallengeDataEntry() != null ? parsedCreq.getChallengeDataEntry() : null;
    }

    @SneakyThrows
    protected Creq parseCreq(String creq) {
        if (creq != null && !creq.isBlank()) {
            byte[] decodedBytes = Base64.getDecoder().decode(creq.getBytes());
            final var creqDecoded = new String(decodedBytes);
            return objectMapper.readValue(creqDecoded, Creq.class);
        }
        return null;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Creq {
        private String sdkCounterStoA;
        private String acsTransID;
        private String messageType;
        private String challengeDataEntry;
        private Boolean oobContinue;
        private String messageVersion;
        private String sdkTransID;
        private String threeDSServerTransID;
    }

}
