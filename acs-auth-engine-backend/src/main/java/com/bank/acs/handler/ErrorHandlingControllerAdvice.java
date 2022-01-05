package com.bank.acs.handler;

import com.bank.acs.dto.challenge.request.ChallengeRequestDto;
import com.bank.acs.dto.challenge.response.ChallengeResponseDto;
import com.bank.acs.dto.challenge.response.TemplateVariables;
import com.bank.acs.entity.AppSession;
import com.bank.acs.enumeration.ErrorType;
import com.bank.acs.exception.BusinessException;
import com.bank.acs.exception.OldRequestException;
import com.bank.acs.repository.AppSessionRepository;
import com.bank.acs.service.AppNativeTemplatesService;
import com.bank.acs.service.AppStateService;
import com.bank.acs.service.FlowAppService;
import com.bank.acs.service.MultiTransactionResolver;
import com.bank.acs.service.RenderHTMLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import java.util.Optional;

import static com.bank.acs.constant.RequestParams.CREQ_PARAM;
import static com.bank.acs.constant.ResponseParams.ERROR_CODE;
import static com.bank.acs.constant.ResponseParams.ERROR_TRANSLATION;
import static com.bank.acs.constant.ResponseParams.ERROR_TYPE;
import static com.bank.acs.enumeration.AcsErrorCode.GENERAL_EXCEPTION;
import static com.bank.acs.enumeration.AcsErrorCode.INTERNAL_SERVICE_IS_NOT_REACHABLE;
import static com.bank.acs.enumeration.AppState.RENDER_FATAL_ERROR_PAGE;
import static com.bank.acs.enumeration.AppState.RENDER_NON_FATAL_ERROR_PAGE;
import static com.bank.acs.enumeration.ChallengeFlowType.determinateChallengeFlowType;
import static com.bank.acs.enumeration.TransactionStatus.CHALLENGE_REQUIRED;
import static com.bank.acs.util.UrlQueryUtil.convertToQueryString;
import static com.bank.acs.util.UrlQueryUtil.parseQueryParams;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@RequiredArgsConstructor
@ControllerAdvice(basePackages = {"com.bank.acs.controller"})
public class ErrorHandlingControllerAdvice {

    public static final String ERROR_STATUS_CODE = "400";
    public static final HttpStatus ERROR_HTTP_STATUS_CODE = BAD_REQUEST;

    protected final FlowAppService flowAppService;
    protected final RenderHTMLService renderHTMLService;
    protected final AppNativeTemplatesService appNativeTemplatesService;
    protected final AppStateService appStateService;
    protected final AppSessionRepository appSessionRepository;
    protected final RequestContext<ChallengeRequestDto> requestContext;
    protected final MultiTransactionResolver multiTransactionResolver;

    @ExceptionHandler(value = BusinessException.class)
    protected ResponseEntity<ChallengeResponseDto> handleBusinessException(BusinessException ex, WebRequest request) {
        return buildErrorResponse(ex);
    }

    @ExceptionHandler(value = OldRequestException.class)
    protected ResponseEntity<ChallengeResponseDto> handleBusinessException(OldRequestException ex, WebRequest request) {
        log.warn("Handling OldRequestException exception, TransactionId = {}, acsTransactionId = {}",
                ex.getTransactionId(), ex.getAcsTransactionId());
        //No need send any valid response, because nobody already listen to it
        return ResponseEntity
                .status(ERROR_HTTP_STATUS_CODE)
                .contentType(APPLICATION_JSON)
                .body(ChallengeResponseDto.builder().build());
    }

    @ExceptionHandler(value = ResourceAccessException.class)
    protected ResponseEntity<ChallengeResponseDto> handleResourceAccessException(ResourceAccessException ex, WebRequest request) {
        log.error("Handling unexpected exception, exception class = {}, message = {}",
                ex.getClass(), ex.getMessage(), ex);
        return buildErrorResponse(new BusinessException(INTERNAL_SERVICE_IS_NOT_REACHABLE));
    }

    @ExceptionHandler(value = Exception.class)
    protected ResponseEntity<ChallengeResponseDto> handleException(Exception ex, WebRequest request) {
        log.error("Handling unexpected exception, exception class = {}, message = {}",
                ex.getClass(), ex.getMessage(), ex);
        return buildErrorResponse(new BusinessException(GENERAL_EXCEPTION));
    }

    protected void setErrorContent(BusinessException ex, AppSession session, ChallengeRequestDto dto, ChallengeResponseDto.ChallengeResponseDtoBuilder response) {
        final var flowType = determinateChallengeFlowType(dto.getDeviceChannel(), dto.getAcsRenderingType().getAcsInterface())
                .orElse(null);

        log.info("ChallengeFlowType is {}", flowType);
        switch (flowType) {
            case BROWSER: {
                final var map = Map.of(
                        ERROR_CODE, ex.getErrorCode().name(),
                        ERROR_TYPE, ex.getErrorCode().getErrorType().name(),
                        ERROR_TRANSLATION, ex.getTranslatedMessage() != null ? ex.getTranslatedMessage() : ""
                );

                if (session != null && session.getFrontendLoaded() != null && session.getFrontendLoaded().booleanValue() == true) {
                    response.content(convertToQueryString(map)).build();
                } else {
                    final var params = parseQueryParams(requestContext.getObject().getContent());
                    final var creq = params.get(CREQ_PARAM);
                    response.content(renderHTMLService.getReactAppHtml(creq, requestContext.getObject().getAcsTransID(), Optional.of(ex)));
                    if (session != null) {
                        session.setFrontendLoaded(true);
                        appSessionRepository.save(session);
                    }
                }
            }
            break;
            case APP_HTML: {
                response.content(flowAppService.getUi(session, null, Optional.of(ex), renderHTMLService));
            }
            break;
            case APP_NATIVE: {
                TemplateVariables templateVariables = flowAppService.getUi(session, null, Optional.of(ex), appNativeTemplatesService).build();
                response.templateVariables(templateVariables);
                response.acsUiType(templateVariables.getAcsUiType().getCode());
            }
            break;
            default: {
                log.warn("Unknown Challenge Flow Type, deviceChannel = {}, acsInterface = {}, acsTransactionId = {}",
                        dto.getDeviceChannel(), dto.getAcsRenderingType().getAcsInterface(), requestContext.getObject().getAcsTransID());
            }
        }
    }

    protected ResponseEntity<ChallengeResponseDto> buildErrorResponse(BusinessException ex) {
        final var response = ChallengeResponseDto.builder();
        HttpStatus status;

        if (requestContext.getObject() != null && requestContext.getObject().getAcsTransID() != null) {

            if (ex.getErrorCode() == GENERAL_EXCEPTION) {
                log.error("BusinessException code {}: acsTransactionId = {}", ex.getErrorCode(), requestContext.getObject().getAcsTransID(), ex);
            } else {
                //Avoid stacktrace in LOGS in case known error.
                log.error("BusinessException code {}: acsTransactionId = {}", ex.getErrorCode(), requestContext.getObject().getAcsTransID());
            }

            response.authenticationData(requestContext.getObject().getAuthenticationData())
                    .acsTransID(requestContext.getObject().getAcsTransID())
                    .statusCode(ERROR_STATUS_CODE)
                    .transStatus(CHALLENGE_REQUIRED);

            if (ErrorType.FATAL == ex.getErrorCode().getErrorType()) {
                appStateService.saveErrorState(requestContext.getObject().getAcsTransID(), RENDER_FATAL_ERROR_PAGE, ex.getErrorCode());
            } else if (ErrorType.NON_FATAL == ex.getErrorCode().getErrorType()) {
                appStateService.saveErrorState(requestContext.getObject().getAcsTransID(), RENDER_NON_FATAL_ERROR_PAGE, ex.getErrorCode());
            }
            status = ERROR_HTTP_STATUS_CODE;
        } else {
            status = INTERNAL_SERVER_ERROR;
        }

        final var session = getSession(requestContext.getObject().getAcsTransID());
        setErrorContent(ex, session, requestContext.getObject(), response);

        log.info("AppSession = {}", session);
        final var body = response.build();
        log.info("Sending ERROR response to TIETO ACS, acsTransactionId = {}, statusCode = {}, transStatus = {}, content = {}",
                body.getAcsTransID(),
                body.getStatusCode(),
                body.getTransStatus(),
                body.getContent()
        );

        if (multiTransactionResolver.isTransactionToRollback(requestContext.getObject().getAcsTransID(), requestContext.getId())) {
            log.warn("handleRequestFromAcsTieto, need to rollback transaction, acsTransactionId = {}", requestContext.getObject().getAcsTransID());
            throw new OldRequestException(requestContext.getObject().getAcsTransID(), requestContext.getId());
        }

        return ResponseEntity
                .status(status)
                .contentType(APPLICATION_JSON)
                .body(response.build());
    }

    protected AppSession getSession(String transId) {
        return appSessionRepository.findById(transId).orElse(null);
    }

}
