package com.bank.acs.service.lt;

import com.bank.acs.config.property.AppProperties;
import com.bank.acs.config.property.MaxAttemptsProperties;
import com.bank.acs.entity.AppSession;
import com.bank.acs.entity.banktron.BanktronData;
import com.bank.acs.entity.banktron.BanktronLogin;
import com.bank.acs.entity.banktron.BanktronPerson;
import com.bank.acs.enumeration.AcsErrorCode;
import com.bank.acs.enumeration.AppState;
import com.bank.acs.enumeration.banktron.BanktronAuthMethod;
import com.bank.acs.exception.BusinessException;
import com.bank.acs.mapper.BanktronMapper;
import com.bank.acs.repository.AppSessionRepository;
import com.bank.acs.repository.BanktronRepository;
import com.bank.acs.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.bank.acs.Profile.COUNTRY_LT_PROFILE;
import static com.bank.acs.enumeration.AcsErrorCode.BLOCKED_LOGIN_CODE;
import static com.bank.acs.enumeration.AcsErrorCode.LOGIN_NOT_ACTIVE;
import static com.bank.acs.enumeration.AcsErrorCode.NO_ACTIVE_PERSON_ACCOUNTS_FOUND;
import static com.bank.acs.enumeration.AcsErrorCode.NO_LOGINS_AVAILABLE;
import static com.bank.acs.enumeration.AcsErrorCode.NO_MATCHING_LOGIN_FOUND;
import static com.bank.acs.enumeration.AcsErrorCode.OPERATION_FAILED;
import static com.bank.acs.enumeration.AppState.RENDER_ENTER_LOGIN_PAGE;
import static com.bank.acs.enumeration.AppState.RENDER_SELECT_AUTH_METHOD_PAGE;
import static com.bank.acs.enumeration.banktron.BanktronStatus.ACTIVE;
import static java.util.stream.Collectors.toList;

@Slf4j
@Profile(COUNTRY_LT_PROFILE)
@Service
public class UserServiceLT extends UserService {

    protected final BanktronRepository banktronRepository;
    protected final BanktronService banktronService;
    protected final BanktronMapper banktronMapper;

    public UserServiceLT(
            MaxAttemptsProperties maxAttemptsProperties,
            AppProperties appProperties,
            AppSessionRepository appSessionRepository,
            BanktronRepository banktronRepository,
            BanktronService banktronService,
            BanktronMapper banktronMapper
    ) {
        super(maxAttemptsProperties, appProperties, appSessionRepository);
        this.banktronRepository = banktronRepository;
        this.banktronService = banktronService;
        this.banktronMapper = banktronMapper;
    }

    @Override
    public AppState initUserInformation(AppSession session) {
        log.info("initUserInformation : AppSession = {}", session);

        final var responseBody = banktronService.createSessionAndGetPersonLogins(session.getCardHolderPersonalCode(), session.getCardCountry());
        session.setLogoutDone(false);
        appSessionRepository.save(session);
        log.info("Response for createSessionAndGetPersonLogins {}", responseBody);

        if (responseBody.getSessionToken() == null) {
            log.error("No Banktron SessionToken, AppSession = {}", session);
            throw new BusinessException(OPERATION_FAILED);
        }

        if (responseBody.getPersonList() == null) {
            log.error("No Banktron active person accounts found, AppSession = {}", session);
            throw new BusinessException(NO_ACTIVE_PERSON_ACCOUNTS_FOUND);
        }

        final var personDtos = responseBody.getPersonList().getPerson().stream().filter(person -> person.getStatus().isActive()).collect(toList());
        if (personDtos.size() == 0) {
            log.error("No Banktron active person accounts found, AppSession = {}", session);
            throw new BusinessException(NO_ACTIVE_PERSON_ACCOUNTS_FOUND);
        }

        var banktronData = banktronRepository.findById(session.getAcsTransactionId())
                .orElse(BanktronData.builder()
                        .acsTransactionId(session.getAcsTransactionId())
                        .build()
                );

        banktronData.setSessionToken(responseBody.getSessionToken());

        final var persons = personDtos.stream()
                .map(banktronMapper::map)
                .peek(person -> person.setBanktronData(banktronData))
                .collect(toList());
        if (banktronData.getPersons() == null) {
            banktronData.setPersons(new ArrayList<>());
        }
        banktronData.getPersons().addAll(persons);
        banktronRepository.save(banktronData);

        var personList = findActivePersons(banktronData);
        if (personList.size() == 0) {
            log.error("No Banktron active person found, AppSession = {}", session);
            throw new BusinessException(NO_ACTIVE_PERSON_ACCOUNTS_FOUND);
        }
        if (personList.size() == 1) {
            this.savePersonInformation(session, personList.get(0));
        }

        final var logins = findLogins(personList);
        if (logins.size() == 0) {
            log.error("No Banktron logins found, AppSession = {}", session);
            throw new BusinessException(NO_LOGINS_AVAILABLE);
        }

        final var activeLogins = logins.stream().filter(login -> ACTIVE == login.getStatus()).collect(toList());
        if (activeLogins.size() == 0) {
            log.error("No Banktron active logins found, AppSession = {}", session);
            throw new BusinessException(BLOCKED_LOGIN_CODE);
        }

        //BDBCDVIEU-668 In there should not be "activeLogins.size()" but count of all logins
        // because of "foreigners" do not have "Person Code" and somebody can get "Person Code" from another person
        if (countTotalLogins(banktronData.getPersons()) > 1) {
            return RENDER_ENTER_LOGIN_PAGE;
        }

        this.savePersonInformation(session, logins.get(0).getBanktronPerson());
        this.saveChosenLogin(session, logins.get(0));
        return RENDER_SELECT_AUTH_METHOD_PAGE;
    }

    @Override
    public AppState handleEnteredLogin(AppSession session, String loginFromUser) {
        validateAndIncrementLoginAttempt(session, loginFromUser);

        final var login = findLoginByUsername(session, loginFromUser);
        if (login == null) {
            log.error("No matching login found login = {}", loginFromUser);
            throw new BusinessException(NO_MATCHING_LOGIN_FOUND);
        }
        if (login.getStatus() != ACTIVE) {
            log.error("Login not ACTIVE = {}", loginFromUser);
            throw new BusinessException(LOGIN_NOT_ACTIVE);
        }
        if (login.getBanktronPerson().getStatus() != ACTIVE) {
            log.error("Person not ACTIVE = {}", loginFromUser);
            throw new BusinessException(NO_ACTIVE_PERSON_ACCOUNTS_FOUND);
        }
        this.savePersonInformation(session, login.getBanktronPerson());
        this.saveChosenLogin(session, login);
        return AppState.RENDER_SELECT_AUTH_METHOD_PAGE;
    }

    protected int countTotalLogins(List<BanktronPerson> persons) {
        return persons.stream()
                .map(BanktronPerson::getLogins)
                .mapToInt(List::size)
                .sum();
    }

    protected List<BanktronPerson> findActivePersons(BanktronData banktronData) {
        return banktronData.getPersons().stream()
                .filter(person -> ACTIVE == person.getStatus())
                .collect(toList());
    }

    protected List<BanktronLogin> findLogins(List<BanktronPerson> persons) {
        return persons.stream()
                .flatMap(person -> person.getLogins().stream())
                .collect(toList());
    }

    protected BanktronLogin findLoginByUsername(AppSession session, String loginFromUser) {
        return banktronRepository.findById(session.getAcsTransactionId())
                .stream()
                .map(BanktronData::getPersons)
                .flatMap(data -> data.stream())
                .flatMap(person -> person.getLogins().stream())
                .filter(login -> login.getUsername().equals(loginFromUser))
                .findFirst().orElse(null);
    }

    protected void savePersonInformation(AppSession session, BanktronPerson person) {
        final var lang = parseLanguage(person.getLanguage());
        session.setUsedLanguage(lang);
        appSessionRepository.save(session);
        log.info("Used person: firstName {}, lastName = {}, language = {}, acsTransactionId = {}",
                person.getFirstName(), person.getFirstName(), session.getUsedLanguage(), session.getAcsTransactionId());
    }

    protected void saveChosenLogin(AppSession session, BanktronLogin login) {
        if (!login.getStatus().isActive()) {
            throw new BusinessException(AcsErrorCode.LOGIN_NOT_ACTIVE);
        }
        addUserAuthMethodsToSession(session, login.getAuthMethods().stream()
                .filter(authMethod -> authMethod != null)
                .map(BanktronAuthMethod::toAuthMethod)
                .collect(toList())
        );

        session.setUsedAuthMethod(login.getLastAuthMethod() == null ? null : BanktronAuthMethod.toAuthMethod(login.getLastAuthMethod()));
        session.setChosenUsername(login.getUsername());
        log.info("Used username = {}, acsTransactionId = {}", login.getUsername(), session.getAcsTransactionId());
        appSessionRepository.save(session);
        banktronRepository.findById(session.getAcsTransactionId())
                .ifPresent(banktronData -> {
                    banktronData.setUsedLogin(login);
                    banktronRepository.save(banktronData);
                });
        getAllowedUserAuthMethods(session, false);
    }

    @Override
    public void deleteAppSession(AppSession session) {
        super.deleteAppSession(session);
        banktronRepository.findById(session.getAcsTransactionId())
                .ifPresent(item -> banktronRepository.delete(item));
    }

    @Override
    public void logout(AppSession session) {
        log.info("Logout from banktron");
        try {
            banktronRepository.findById(session.getAcsTransactionId())
                    .ifPresent(banktronData -> banktronService.logout(banktronData.getSessionToken()));
        } catch (BusinessException e) {
            log.warn("Ignoring logout error {}", e.getErrorCode());
        }
        super.logout(session);
    }

    public void resetBanktronSession(AppSession session) {
        log.info("resetBanktronSession : AppSession = {}", session);

        try {
            logout(session);
        } catch (BusinessException e) {
            log.warn("Ignoring logout error = {}, acsTransactionId = {}", e.getErrorCode(), session.getAcsTransactionId());
        }
        initUserInformation(session);
    }
}
