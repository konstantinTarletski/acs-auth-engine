package com.bank.acs.service;

import com.bank.acs.config.property.AppProperties;
import com.bank.acs.config.property.MaxAttemptsProperties;
import com.bank.acs.entity.AppSession;
import com.bank.acs.entity.UserAuthMethod;
import com.bank.acs.enumeration.AppState;
import com.bank.acs.enumeration.AuthMethod;
import com.bank.acs.exception.BusinessException;
import com.bank.acs.repository.AppSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

import static com.bank.acs.enumeration.AcsErrorCode.EXCEEDED_MAX_ATTEMPTS_FOR_ENTERING_LOGIN;
import static com.bank.acs.enumeration.AcsErrorCode.NO_SUPPORTED_AUTH_METHODS_AVAILABLE;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public abstract class UserService {

    protected final MaxAttemptsProperties maxAttemptsProperties;
    protected final AppProperties appProperties;
    protected final AppSessionRepository appSessionRepository;

    public abstract AppState initUserInformation(AppSession session);

    public abstract AppState handleEnteredLogin(AppSession session, String loginFromUser);

    public void logout(AppSession session) {
        try {
            session.setLogoutDone(true);
            appSessionRepository.save(session);
        } catch (BusinessException e) {
            log.warn("Ignoring logout error {}", e.getErrorCode());
        }
    }

    public void deleteAppSession(AppSession session) {
        try {
            log.info("Logout user, acsTransactionId = {}", session.getAcsTransactionId());
            logout(session);
        } catch (Exception e) {
            log.info("Error while Logout for acsTransactionId {}", session.getAcsTransactionId(), e);
        }
        try {
            log.info("Deleting AppSession, acsTransactionId = {}", session.getAcsTransactionId());
            appSessionRepository.deleteById(session.getAcsTransactionId());
        } catch (Exception e) {
            log.warn("Error while deleting AppSession, acsTransactionId {}", session.getAcsTransactionId(), e);
        }
    }

    public String parseLanguage(String lang) {
        return lang != null ? appProperties.getAllowedLanguages().stream()
                .filter(allowedLang -> allowedLang.equalsIgnoreCase(lang)).findFirst()
                .orElseGet(appProperties::getDefaultLanguage)
                : appProperties.getDefaultLanguage();
    }

    protected void addUserAuthMethodsToSession(AppSession session, Collection<AuthMethod> authMethodList) {
        log.info("Saving Auth methods acsTransactionId = {}, authMethodList = {}", session.getAcsTransactionId(), authMethodList);
        final var userAuthMethodList = authMethodList.stream()
                .filter(item -> item != null)
                .map(item -> {
                    var userAuthMethod = new UserAuthMethod();
                    userAuthMethod.setAppSession(session);
                    userAuthMethod.setAuthMethod(item);
                    return userAuthMethod;
                }).collect(toList());

        if (session.getUserAuthMethods() == null) {
            session.setUserAuthMethods(new ArrayList<>());
        }
        session.getUserAuthMethods().addAll(userAuthMethodList);
        log.info("Result Auth methods acsTransactionId = {}, userAuthMethodList = {}", session.getAcsTransactionId(), userAuthMethodList);
    }

    public EnumSet<AuthMethod> getAllowedUserAuthMethods(AppSession session, boolean emptyAllowed) {
        final var authMethods = getUserLoginAuthMethods(session).stream()
                .filter(authMethod -> appProperties.getAllowedAuthMethods().contains(authMethod))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(AuthMethod.class)));

        if (authMethods.isEmpty() && !emptyAllowed) {
            throw new BusinessException(NO_SUPPORTED_AUTH_METHODS_AVAILABLE);
        }
        log.info("getAllowedUserAuthMethods, getAllowedUserAuthMethods = {}, acsTransactionId = {}", authMethods, session.getAcsTransactionId());
        return authMethods;
    }

    protected EnumSet<AuthMethod> getUserLoginAuthMethods(AppSession session) {
        return session.getUserAuthMethods().stream()
                .map(UserAuthMethod::getAuthMethod)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(AuthMethod.class)));
    }

    protected void validateAndIncrementLoginAttempt(AppSession session, String loginFromUser) {
        log.info("Trying to match provided login {} with available account logins", loginFromUser);

        final var currentLoginAttempt = session.getCurrentLoginAttempt();
        if (currentLoginAttempt >= maxAttemptsProperties.getForEnterLogin()) {
            log.error("Exceeded max attempts for entering login = {}", loginFromUser);
            throw new BusinessException(EXCEEDED_MAX_ATTEMPTS_FOR_ENTERING_LOGIN);
        }
        session.setCurrentLoginAttempt(currentLoginAttempt + 1);
        appSessionRepository.save(session);
    }

}
