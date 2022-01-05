package com.bank.acs.service;

import com.bank.acs.config.property.AppProperties;
import com.bank.acs.dto.challenge.request.ChallengeRequestDto;
import com.bank.acs.dto.challenge.response.ChallengeResponseDto;
import com.bank.acs.entity.AppSession;
import com.bank.acs.enumeration.ChallengeFlowType;
import com.bank.acs.enumeration.UiAction;
import com.bank.acs.repository.AppSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static com.bank.acs.constant.RequestParams.UI_ACTION_PARAM;

@Slf4j
@Service
public class AppHtmlFlow extends FlowBase {

    protected final FlowAppService flowAppService;
    protected final RenderHTMLService renderHTMLService;

    public AppHtmlFlow(
            AppSessionRepository appSessionRepository,
            AppStateService appStateService,
            LinkAppService linkAppService,
            UserService userService,
            UiActionService uiActionService,
            AppProperties appProperties,
            FlowAppService flowAppService,
            RenderHTMLService renderHTMLService
    ) {
        super(appSessionRepository, appStateService, linkAppService, userService, uiActionService, appProperties);
        this.flowAppService = flowAppService;
        this.renderHTMLService = renderHTMLService;
    }

    @Override
    public ChallengeFlowType getChallengeFlowType() {
        return ChallengeFlowType.APP_HTML;
    }

    @Override
    public void handleAnyRequest(AppSession session,
                                 ChallengeRequestDto request,
                                 ChallengeResponseDto.ChallengeResponseDtoBuilder response,
                                 Map<String, String> params,
                                 String creq) {
        log.info("handleAnyRequest, acsTransactionId = {}, params = {}", session.getAcsTransactionId(), params);
        response.content(flowAppService.getUi(session, null, Optional.empty(), renderHTMLService));
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
        if (uiResponse != null) {
            response.content(flowAppService.getUi(session, uiResponse, Optional.empty(), renderHTMLService));
        }
    }

    @Override
    public UiAction getUiAction(AppSession session, ChallengeRequestDto request, Map<String, String> params, String creq) {
        final var uiActionStr = params.get(UI_ACTION_PARAM);
        return uiActionStr != null ? UiAction.valueOf(uiActionStr) : null;
    }

    @Override
    public Map<String, String> getApplicationParameters(AppSession session,
                                                        ChallengeRequestDto request,
                                                        Map<String, String> parsedContent,
                                                        String creq) {
        return parsedContent;
    }

}
