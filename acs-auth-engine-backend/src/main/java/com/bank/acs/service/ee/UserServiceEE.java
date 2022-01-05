package com.bank.acs.service.ee;

import com.bank.acs.config.property.AppProperties;
import com.bank.acs.config.property.MaxAttemptsProperties;
import com.bank.acs.dto.ccc.CCCLanguageDO;
import com.bank.acs.dto.ccc.CCCUsernameAndLanguagesDO;
import com.bank.acs.entity.AppSession;
import com.bank.acs.enumeration.AppState;
import com.bank.acs.enumeration.AuthMethod;
import com.bank.acs.enumeration.ChallengeFlowType;
import com.bank.acs.exception.BusinessException;
import com.bank.acs.repository.AppSessionRepository;
import com.bank.acs.repository.CiamRepository;
import com.bank.acs.service.UserService;
import com.bank.acs.service.ccc.CccService;
import com.bank.acs.service.ciam.CiamService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

import static com.bank.acs.Profile.COUNTRY_EE_PROFILE;
import static com.bank.acs.enumeration.AppState.RENDER_SELECT_AUTH_METHOD_PAGE;
import static com.bank.acs.service.ccc.CccConstants.DESKTOP_LANGUAGE;
import static com.bank.acs.service.ccc.CccConstants.MOBILE_LANGUAGE;

@Slf4j
@Profile(COUNTRY_EE_PROFILE)
@Service
public class UserServiceEE extends UserService {

    protected final CiamRepository ciamRepository;
    protected final CiamService ciamService;
    protected final CccService cccService;

    public UserServiceEE(
            MaxAttemptsProperties maxAttemptsProperties,
            AppProperties appProperties,
            AppSessionRepository appSessionRepository,
            CiamRepository ciamRepository,
            CiamService ciamService,
            CccService cccService
    ) {
        super(maxAttemptsProperties, appProperties, appSessionRepository);
        this.ciamRepository = ciamRepository;
        this.ciamService = ciamService;
        this.cccService = cccService;
    }

    @Override
    @SneakyThrows
    public AppState initUserInformation(AppSession session) {

        //There is no way to get AUTH methods in EE (reading them from properties)
        var authMethodList = appProperties.getAllowedAuthMethods();
        var authMethodListCopy = new ArrayList<>(authMethodList);

        log.info("initUserInformation acsTransactionId = {}, challengeFlowType = {}, authMethodList from properties = {}",
                session.getAcsTransactionId(), session.getChallengeFlowType(), authMethodListCopy);

        if(session.getChallengeFlowType() != null && !ChallengeFlowType.BROWSER.equals(session.getChallengeFlowType())){
            log.info("initUserInformation acsTransactionId = {}, challengeFlowType = {}, removing {} from authMethodList",
                    session.getAcsTransactionId(), session.getChallengeFlowType(), AuthMethod.ID_CARD);
            authMethodListCopy.remove(AuthMethod.ID_CARD);
        }
        addUserAuthMethodsToSession(session, authMethodListCopy);

        final var token = ciamService.obtainSTSAccessToken();
        final var user = cccService.getUserNameAndLanguage(
                token.getAccessToken(),
                session.getCardHolderPersonalCode(),
                session.getCardCountry());

        session.setUsedLanguage(parseLanguage(getLanguage(user)));
        session.setChosenUsername(user.getUsername());
        session.setLogoutDone(false);
        appSessionRepository.save(session);
        getAllowedUserAuthMethods(session, false);
        log.info("validateUserHasSingleLogin success, clientId = {}, language = {}, last auth method {}",
                session.getChosenUsername(), session.getUsedLanguage(), session.getUsedAuthMethod());
        return RENDER_SELECT_AUTH_METHOD_PAGE;
    }

    protected String getLanguage(CCCUsernameAndLanguagesDO user) {
        Optional<CCCLanguageDO> desktopLanguage = user.getLanguages().stream()
                .filter(CCCLanguageDO -> DESKTOP_LANGUAGE.equals(CCCLanguageDO.getSettingCode()))
                .findFirst();
        Optional<CCCLanguageDO> mobileLanguage = user.getLanguages().stream()
                .filter(CCCLanguageDO -> MOBILE_LANGUAGE.equals(CCCLanguageDO.getSettingCode()))
                .findFirst();
        return desktopLanguage.isPresent() ?
                desktopLanguage.get().getSettingPayload() :
                (mobileLanguage.isPresent() ? mobileLanguage.get().getSettingPayload() : null);
    }

    @Override
    public AppState handleEnteredLogin(AppSession session, String loginFromUser) {
        //Nothing to do
        return AppState.RENDER_SELECT_AUTH_METHOD_PAGE;
    }

    @Override
    public void deleteAppSession(AppSession session) {
        super.deleteAppSession(session);
        ciamRepository.findById(session.getAcsTransactionId())
                .ifPresent(item -> ciamRepository.delete(item));
    }

    @Override
    public void logout(AppSession session) {
        try {
            ciamService.logoutFromCiam(session.getCardCountry(), session.getChosenUsername());
        } catch (BusinessException e) {
            log.warn("Ignoring logout error {}", e.getErrorCode());
        }
        super.logout(session);
    }

    public void resetCiamSession(AppSession session) {
        log.info("resetCiamSession : AppSession = {}", session);

        try {
            logout(session);
        } catch (BusinessException e) {
            log.warn("Ignoring logout error = {}, acsTransactionId = {}", e.getErrorCode(), session.getAcsTransactionId());
        }
        initUserInformation(session);
    }

}
