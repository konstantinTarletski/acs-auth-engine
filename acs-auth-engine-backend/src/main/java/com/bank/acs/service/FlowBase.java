package com.bank.acs.service;

import com.bank.acs.config.property.AppProperties;
import com.bank.acs.dto.challenge.ChallengeAuthDto;
import com.bank.acs.dto.challenge.request.ChallengeRequestDto;
import com.bank.acs.dto.challenge.response.ChallengeResponseDto;
import com.bank.acs.entity.AppSession;
import com.bank.acs.enumeration.AcsErrorCode;
import com.bank.acs.enumeration.AppState;
import com.bank.acs.enumeration.ChallengeFlowType;
import com.bank.acs.enumeration.TransactionStatus;
import com.bank.acs.enumeration.UiAction;
import com.bank.acs.exception.BusinessException;
import com.bank.acs.repository.AppSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.bank.acs.constant.RequestParams.CREQ_PARAM;
import static com.bank.acs.enumeration.AppState.CHECK_CARD_STATUS;
import static com.bank.acs.enumeration.TransactionStatus.*;
import static com.bank.acs.util.StringUtil.removeLineBreaks;
import static com.bank.acs.util.StringUtil.truncate;
import static com.bank.acs.util.UrlQueryUtil.parseQueryParams;

@Slf4j
@RequiredArgsConstructor
public abstract class FlowBase {

    public static final String AUTH_DEFAULT_AUTHENTICATION_KEY = "4314002";
    public static final String AUTH_DEFAULT_OTP = "empty";
    public static final String AUTH_DEFAULT_LANG = "en";
    public static final String AUTH_DEFAULT_ACS_PAN_RANGE = "reference";

    public static final String STATUS_CODE_200 = "200";
    public static final String STATUS_CODE_400 = "400";

    protected final AppSessionRepository appSessionRepository;
    protected final AppStateService appStateService;
    protected final LinkAppService linkAppService;
    protected final UserService userService;
    protected final UiActionService uiActionService;
    protected final AppProperties appProperties;

    public abstract ChallengeFlowType getChallengeFlowType();

    public abstract void handleAnyRequest(
            AppSession session,
            ChallengeRequestDto request,
            ChallengeResponseDto.ChallengeResponseDtoBuilder response,
            Map<String, String> params,
            String creq);

    public abstract void handleUiRequest(
            Map<String, String> uiResponse,
            AppSession session,
            ChallengeRequestDto request,
            ChallengeResponseDto.ChallengeResponseDtoBuilder response,
            Map<String, String> params,
            String creq);

    public abstract Map<String, String> getApplicationParameters(
            AppSession session,
            ChallengeRequestDto request,
            Map<String, String> parsedContent,
            String creq);

    public abstract UiAction getUiAction(
            AppSession session,
            ChallengeRequestDto request,
            Map<String, String> params,
            String creq
    );

    public ChallengeResponseDto handleRequestFromAcsTieto(ChallengeRequestDto request) {
        log.info("Handling Request from AcsTieto, acsTransactionId = {}, content = {}", request.getAcsTransID(), request.getContent());
        final var authDto = request.getAuthenticationData() != null ? request.getAuthenticationData() : new ChallengeAuthDto();
        final var response = ChallengeResponseDto.builder()
                .authenticationData(request.getAuthenticationData())
                .acsTransID(request.getAcsTransID())
                .authenticationData(getChallengeAuthDto(authDto));
        final var parsedContent = parseQueryParams(request.getContent());
        final var creq = parsedContent.get(CREQ_PARAM);
        final var session = appSessionRepository.findById(request.getAcsTransID())
                .orElseGet(() -> createNewSession(request, creq));

        final var params = getApplicationParameters(session, request, parsedContent, creq);
        log.info("ApplicationParameters = {} for acsTransactionId = {}", params, request.getAcsTransID());
        final var uiAction = getUiAction(session, request, params, creq);

        if (uiAction != null) {
            log.info("Handling UI Request for acsTransactionId = {}", request.getAcsTransID());
            final var uiResponse = handleUiAction(session, uiAction, request, response, params, creq);
            handleUiRequest(uiResponse, session, request, response, params, creq);
        } else {
            log.info("Handling default Request for acsTransactionId = {}", request.getAcsTransID());
            handleAnyRequest(session, request, response, params, creq);
            response.transStatus(TransactionStatus.CHALLENGE_REQUIRED);
            response.statusCode(STATUS_CODE_200);
        }

        final var toSend = response.build();

        String body = toSend.getContent();
        if(appProperties.isTruncateResponse()){
            body = truncate(toSend.getContent(), " ...");
        }

        log.info("sending response to TIETO ACS, acsTransactionId = {}, statusCode = {}, transStatus = {}, authenticationData = {}, content = {}",
                toSend.getAcsTransID(),
                toSend.getStatusCode(),
                toSend.getTransStatus(),
                toSend.getAuthenticationData(),
                removeLineBreaks(body)
        );
        return toSend;
    }

    protected Map<String, String> handleUiAction(
            AppSession session,
            UiAction uiAction,
            ChallengeRequestDto dto,
            ChallengeResponseDto.ChallengeResponseDtoBuilder response,
            Map<String, String> params,
            String creq
    ) {
        log.info("Handling UI_ACTION_PARAM {} for acsTransactionId = {}", uiAction, dto.getAcsTransID());

        checkIsCurrentActionAllowed(uiAction, session.getState());
        Map<String, String> uiResponse = null;

        if (UiAction.BACK_TO_MERCHANT_SUCCESS == uiAction) {
            response.transStatus(VERIFICATION_SUCCESSFUL);
            response.statusCode(STATUS_CODE_200);
            //response.whiteListStatus("Y");
            //response.whitelist("confirmed");
            userService.deleteAppSession(session);
        } else if (UiAction.BACK_TO_MERCHANT_CANCEL == uiAction) {
            response.transStatus(VERIFICATION_REJECTED);
            response.statusCode(STATUS_CODE_400);
            userService.deleteAppSession(session);
        } else {
            uiResponse = uiActionService.handleRequestFromUi(session, uiAction, params);
            response.transStatus(CHALLENGE_REQUIRED);
            response.statusCode(STATUS_CODE_200);

            if (creq != null && (session.getCreq() == null || !session.getCreq().equals(creq))) {
                session.setCreq(creq);
                appSessionRepository.save(session);
                log.info("Overriding 'creq', acsTransactionId = {}", session.getAcsTransactionId());
            }
        }
        return uiResponse;
    }

    protected void checkIsCurrentActionAllowed(UiAction uiAction, AppState appState) {
        if (!uiAction.getAvailableAppStates().contains(appState)) {
            log.warn("getCheckIsCurrentActionAllowed FALSE uiAction = {}, AppState = {}", uiAction, appState);
            throw new BusinessException(AcsErrorCode.WRONG_APPLICATION_STATE);
        }
    }

    protected AppSession createNewSession(ChallengeRequestDto dto, String creq) {
        log.info("Handling new session Request for acsTransactionId = {}", dto.getAcsTransID());
        final var purchaseData = dto.getPurchaseData();
        final var newSession = AppSession.builder()
                .acsTransactionId(dto.getAcsTransID())
                .frontendLoaded(false)
                .creq(creq)
                .state(CHECK_CARD_STATUS)
                .cardExpiryDate(dto.getCardExpiryDate())
                .acctNumber(dto.getAcctNumber())
                .purchaseDate(purchaseData != null ? purchaseData.getPurchaseDate() : null)
                .purchaseCurrency(purchaseData != null ? purchaseData.getPurchaseCurrency() : null)
                .merchantName(purchaseData != null ? purchaseData.getMerchantName() : null)
                .purchaseAmount(purchaseData != null ? purchaseData.getPurchaseAmount() : null)
                .usedLanguage(appProperties.getDefaultLanguage())
                .challengeFlowType(this.getChallengeFlowType())
                .build();

        final var session = appSessionRepository.save(newSession);

        // If we get any unexpected exception on initial call, we have to handle it as FATAL exception
        // and not to rollback transaction for session creation
        try {
            appStateService.saveState(session.getAcsTransactionId(), linkAppService.executeCardStatusCheck(session, dto.getAcctNumber(), dto.getCardExpiryDate()));
            appStateService.saveState(session.getAcsTransactionId(), userService.initUserInformation(session));
        } catch (Exception ex) {
            if (ex instanceof BusinessException) {
                throw ex;
            }
            log.error("Caught unexpected exception during initial call, exception class = {}, message = {}",
                    ex.getClass(), ex.getMessage(), ex);
            throw new BusinessException(AcsErrorCode.FATAL_EXCEPTION);
        }
        return session;
    }

    protected ChallengeAuthDto getChallengeAuthDto(ChallengeAuthDto input) {
        if (input.getAuthenticationKey() == null) {
            input.setAuthenticationKey(AUTH_DEFAULT_AUTHENTICATION_KEY);
        }
        if (input.getOtp() == null) {
            input.setOtp(AUTH_DEFAULT_OTP);
        }
        if (input.getOtpGeneratedDateTime() == null) {
            input.setOtpGeneratedDateTime(System.currentTimeMillis());
        }
        if (input.getLang() == null) {
            input.setLang(AUTH_DEFAULT_LANG);
        }
        if (input.getAcsPanRange() == null) {
            input.setAcsPanRange(AUTH_DEFAULT_ACS_PAN_RANGE);
        }
        return input;
    }

}
