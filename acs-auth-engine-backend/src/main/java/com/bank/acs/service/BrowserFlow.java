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
import static com.bank.acs.util.UrlQueryUtil.convertToQueryString;

@Slf4j
@Service
public class BrowserFlow extends FlowBase {

    protected final RenderHTMLService renderHTMLService;

    public BrowserFlow(
            AppSessionRepository appSessionRepository,
            AppStateService appStateService,
            LinkAppService linkAppService,
            UserService userService,
            RenderHTMLService renderHTMLService,
            UiActionService uiActionService,
            AppProperties appProperties
    ) {
        super(appSessionRepository, appStateService, linkAppService, userService, uiActionService, appProperties);
        this.renderHTMLService = renderHTMLService;
    }

    @Override
    public ChallengeFlowType getChallengeFlowType() {
        return ChallengeFlowType.BROWSER;
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

    @Override
    public void handleUiRequest(
            Map<String, String> uiResponse,
            AppSession session,
            ChallengeRequestDto request,
            ChallengeResponseDto.ChallengeResponseDtoBuilder response,
            Map<String, String> params,
            String creq) {
        response.content(uiResponse != null && !uiResponse.isEmpty() ? convertToQueryString(uiResponse) : null);
    }

    @Override
    public void handleAnyRequest(AppSession session,
                                 ChallengeRequestDto request,
                                 ChallengeResponseDto.ChallengeResponseDtoBuilder response,
                                 Map<String, String> params,
                                 String creq) {
        response.content(renderHTMLService.getReactAppHtml(creq, request.getAcsTransID(), Optional.empty()));
        session.setFrontendLoaded(true);
        appSessionRepository.save(session);
    }

}
