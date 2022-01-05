package com.bank.acs.controller;

import com.bank.acs.dto.challenge.request.ChallengeRequestDto;
import com.bank.acs.dto.challenge.response.ChallengeResponseDto;
import com.bank.acs.enumeration.AcsErrorCode;
import com.bank.acs.exception.BusinessException;
import com.bank.acs.handler.RequestContext;
import com.bank.acs.service.ChallengeService;
import com.bank.acs.service.MultiTransactionResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "${app.api.challenge.url}", produces = APPLICATION_JSON_VALUE)
public class ApiController {

    protected final ChallengeService challengeService;
    protected final RequestContext requestContext;
    protected final Validator validator;
    protected final MultiTransactionResolver multiTransactionResolver;

    @PostMapping
    public ChallengeResponseDto handleRequestFromAcsTieto(@RequestBody ChallengeRequestDto dto) {
        long startTime = System.currentTimeMillis();
        log.info("RequestBody {}", dto);
        requestContext.setObject(dto);
        requestContext.setId(UUID.randomUUID());

        try {
            multiTransactionResolver.registerTransaction(dto.getAcsTransID(), requestContext.getId());
            Set<ConstraintViolation<ChallengeRequestDto>> validation = validator.validate(dto);
            if (!validation.isEmpty()) {
                log.error("ChallengeRequestDto with have acsTransactionId = {} errors = {}", dto.getAcsTransID(), validation);
                throw new BusinessException(AcsErrorCode.JSON_BODY_VALIDATION_EXCEPTION, validation.toString());
            }
            final var response = challengeService.handleRequestFromAcsTieto(dto);
            log.info("Response ChallengeResponseDto {}", response);
            return response;
        } finally {
            multiTransactionResolver.deleteTransactionMonitor(requestContext.getId());
            long finishTime = System.currentTimeMillis();
            long timeElapsed = finishTime - startTime;
            log.info("handleRequestFromAcsTieto execution time is {}ms", timeElapsed);
        }
    }
}
