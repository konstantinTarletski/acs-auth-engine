package com.bank.acs.service;

import com.bank.acs.dto.challenge.request.ChallengeRequestDto;
import com.bank.acs.dto.challenge.response.ChallengeResponseDto;
import com.bank.acs.enumeration.ChallengeFlowType;
import com.bank.acs.exception.BusinessException;
import com.bank.acs.exception.OldRequestException;
import com.bank.acs.handler.RequestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Transactional(noRollbackFor = BusinessException.class)
@Service
public class ChallengeService {

    protected final BrowserFlow browserFlow;
    protected final AppHtmlFlow appHtmlFlow;
    protected final AppNativeFlow appNativeFlow;
    protected final MultiTransactionResolver multiTransactionResolver;
    protected final RequestContext requestContext;

    public ChallengeResponseDto handleRequestFromAcsTieto(ChallengeRequestDto dto) {
        final var flowType = ChallengeFlowType.determinateChallengeFlowType(dto.getDeviceChannel(), dto.getAcsRenderingType().getAcsInterface())
                .orElse(null);

        log.info("ChallengeFlowType is {}, acsTransactionId = {}", flowType, dto.getAcsTransID());
        ChallengeResponseDto response = null;

        final var flowMap = Map.of(
                browserFlow.getChallengeFlowType(), browserFlow,
                appHtmlFlow.getChallengeFlowType(), appHtmlFlow,
                appNativeFlow.getChallengeFlowType(), appNativeFlow);

        final var flow = flowMap.get(flowType);
        if(flow != null){
            response = flow.handleRequestFromAcsTieto(dto);
        } else {
            log.warn("Unknown Challenge Flow Type, deviceChannel = {}, acsInterface = {}, acsTransactionId = {}",
                    dto.getDeviceChannel(),
                    dto.getAcsRenderingType().getAcsInterface(),
                    dto.getAcsTransID());
        }

        if (multiTransactionResolver.isTransactionToRollback(dto.getAcsTransID(), requestContext.getId())){
            log.warn("handleRequestFromAcsTieto, need to rollback transaction, acsTransactionId = {}", dto.getAcsTransID());
            throw new OldRequestException(dto.getAcsTransID(), requestContext.getId());
        }
        return response;
    }

}
