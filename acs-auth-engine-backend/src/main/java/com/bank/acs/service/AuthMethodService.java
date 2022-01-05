package com.bank.acs.service;

import com.bank.acs.config.property.AppProperties;
import com.bank.acs.config.property.MaxAttemptsProperties;
import com.bank.acs.entity.AppSession;
import com.bank.acs.enumeration.AppState;
import com.bank.acs.enumeration.AuthMethod;
import com.bank.acs.exception.BusinessException;
import com.bank.acs.repository.AppSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static com.bank.acs.enumeration.AcsErrorCode.EXCEEDED_MAX_ATTEMPTS_FOR_AUTH_METHOD_CHANGE;

@Slf4j
@RequiredArgsConstructor
public abstract class AuthMethodService {

    protected final MaxAttemptsProperties maxAttemptsProperties;
    protected final AppSessionRepository appSessionRepository;
    protected final AppStateService appStateService;
    protected final AppProperties appProperties;

    public abstract String initSmartId(AppSession session);
    public abstract String initMSignature(AppSession session);
    public abstract String initIDCard(AppSession session);
    public abstract Optional<Boolean> checkAuthMethodStatus(AppSession session);
    public abstract AppState confirmCodeCalculator(AppSession session, String confirmationCode);
    public abstract AppState confirmIDCard(AppSession session, String authorizationCode);

    public void handleSelectedAuthMethod(AppSession session, AuthMethod selectedAuthMethod) {
        validateAndIncrementAuthAttempt(session);
        session.setUsedAuthMethod(selectedAuthMethod);
        appSessionRepository.save(session);
    }

    protected void validateAndIncrementAuthAttempt(AppSession session) {
        final var currentAuthMethodAttempt = session.getCurrentAuthMethodAttempt();

        if (currentAuthMethodAttempt > maxAttemptsProperties.getForChangeAuthMethod()) {
            throw new BusinessException(EXCEEDED_MAX_ATTEMPTS_FOR_AUTH_METHOD_CHANGE);
        }
        session.setCurrentAuthMethodAttempt(currentAuthMethodAttempt + 1);
        appSessionRepository.save(session);
    }

}
