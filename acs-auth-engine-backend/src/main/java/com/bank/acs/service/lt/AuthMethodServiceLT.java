package com.bank.acs.service.lt;

import com.bank.acs.config.property.AppProperties;
import com.bank.acs.config.property.MaxAttemptsProperties;
import com.bank.acs.entity.AppSession;
import com.bank.acs.enumeration.AcsErrorCode;
import com.bank.acs.enumeration.AppState;
import com.bank.acs.enumeration.AuthMethod;
import com.bank.acs.enumeration.banktron.BanktronAuthMethod;
import com.bank.acs.exception.BusinessException;
import com.bank.acs.repository.AppSessionRepository;
import com.bank.acs.repository.BanktronRepository;
import com.bank.acs.service.AppStateService;
import com.bank.acs.service.AuthMethodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;

import static com.bank.acs.Profile.COUNTRY_LT_PROFILE;
import static com.bank.acs.enumeration.AcsErrorCode.SMART_ID_FALSE;
import static com.bank.acs.enumeration.AcsErrorCode.WRONG_CODE_FOR_CODE_CALCULATOR;
import static com.bank.acs.enumeration.AppState.RENDER_SUCCESSFUL_PAGE;
import static com.bank.acs.enumeration.AuthMethod.*;

@Slf4j
@Profile(COUNTRY_LT_PROFILE)
@Service
public class AuthMethodServiceLT extends AuthMethodService {

    protected final BanktronService banktronService;
    protected final BanktronRepository banktronRepository;
    protected final UserServiceLT userServiceLT;

    public AuthMethodServiceLT(MaxAttemptsProperties maxAttemptsProperties,
                               AppSessionRepository appSessionRepository,
                               AppStateService appStateService,
                               AppProperties appProperties,
                               BanktronService banktronService,
                               BanktronRepository banktronRepository,
                               UserServiceLT userServiceLT
    ) {
        super(maxAttemptsProperties, appSessionRepository, appStateService, appProperties);
        this.banktronService = banktronService;
        this.banktronRepository = banktronRepository;
        this.userServiceLT = userServiceLT;
    }

    @Override
    public void handleSelectedAuthMethod(AppSession session, AuthMethod selectedAuthMethod) {
        //Re-init AUTH (need re-login to change AUTH method)
        if (session.getUsedAuthMethod() != null) {
            log.info("There is already auth method saved, going to reset banktron session");
            userServiceLT.resetBanktronSession(session);
        }
        super.handleSelectedAuthMethod(session, selectedAuthMethod);
    }

    @Override
    public String initSmartId(AppSession session) {
        return initAuth(session, SMART_ID);
    }

    @Override
    public String initMSignature(AppSession session) {
        return initAuth(session, M_SIGNATURE);
    }

    @Override
    public String initIDCard(AppSession session) {
        log.info("initIDCard, LT does not have {}, AcsTransactionId = {}", ID_CARD, session.getAcsTransactionId());
        throw new BusinessException(AcsErrorCode.AUTHENTICATION_FAILED);
    }

    @Override
    public AppState confirmCodeCalculator(AppSession session, String confirmationCode) {
        initAuth(session, CODE_CALCULATOR);
        final var banktronData = banktronRepository.findById(session.getAcsTransactionId()).orElseThrow();
        final var sessionToken = banktronData.getSessionToken();
        try {
            banktronService.notifyConfirmCredentials(sessionToken, confirmationCode);
        } catch (BusinessException businessException) {
            if (WRONG_CODE_FOR_CODE_CALCULATOR == businessException.getErrorCode()) {
                userServiceLT.resetBanktronSession(session);
            }
            throw businessException;
        }
        return RENDER_SUCCESSFUL_PAGE;
    }

    @Override
    public Optional<Boolean> checkAuthMethodStatus(AppSession session) {
        log.info("checkSmartIdResponseImplementation AcsTransactionId = {}", session.getAcsTransactionId());

        try {
            banktronRepository.findById(session.getAcsTransactionId()).ifPresent(
                    banktronData -> {
                        final var response = banktronService.checkAuthMethodStatus(banktronData.getSessionToken());
                        if (!response.getSigned()) {
                            log.warn("Banktron respond with signed = false, AcsTransactionId = {}, throw {} exception",
                                    session.getAcsTransactionId(), SMART_ID_FALSE);
                            throw new BusinessException(SMART_ID_FALSE);
                        }
                        //This is very important call !!
                        //It reset attempt counter to Smart-ID, so after that user will have 5 attempts for Smart-ID again.
                        banktronService.notifyConfirmCredentials(banktronData.getSessionToken(), response.getSignedString());
                    }
            );
            return Optional.of(true);
        } catch (BusinessException e) {
            log.info("checkAuthMethodStatus error = {} AcsTransactionId = {}", e, session.getAcsTransactionId());
            if (e.getErrorCode() == AcsErrorCode.AUTHENTICATION_IN_PROGRESS) {
                log.info("Authentication is in progress for AcsTransactionId = {}", session.getAcsTransactionId());
                return Optional.empty();
            }
            throw e;
        }
    }

    protected String initAuth(AppSession session, AuthMethod authMethod) {
        log.info("initAuth: AuthMethod = {}, AcsTransactionId = {}", authMethod, session.getAcsTransactionId());

        Assert.isTrue(session.getUsedAuthMethod() == authMethod, "Session auth method is not " + authMethod);

        final var banktronData = banktronRepository.findById(session.getAcsTransactionId()).orElseThrow();
        var authResponse =
                banktronService.authenticate(banktronData.getSessionToken(),
                        session.getChosenUsername(),
                        BanktronAuthMethod.toBanktronAuthMethod(authMethod));

        return authResponse.getAuthenticationToken();
    }

    @Override
    public AppState confirmIDCard(AppSession session, String authorizationCode) {
        log.info("confirmIDCard, LT does not have {}, AcsTransactionId = {}", ID_CARD, session.getAcsTransactionId());
        throw new BusinessException(AcsErrorCode.AUTHENTICATION_FAILED);
    }

}
