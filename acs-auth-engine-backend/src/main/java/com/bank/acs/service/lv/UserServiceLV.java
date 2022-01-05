package com.bank.acs.service.lv;

import com.bank.acs.config.property.AppProperties;
import com.bank.acs.config.property.MaxAttemptsProperties;
import com.bank.acs.entity.AppSession;
import com.bank.acs.entity.roofid.RoofIdClient;
import com.bank.acs.entity.roofid.RoofIdData;
import com.bank.acs.enumeration.AppState;
import com.bank.acs.enumeration.AuthMethod;
import com.bank.acs.enumeration.roofid.RoofIdAuthMethod;
import com.bank.acs.exception.BusinessException;
import com.bank.acs.mapper.RoofIdMapper;
import com.bank.acs.repository.AppSessionRepository;
import com.bank.acs.repository.RoofIdRepository;
import com.bank.acs.service.UserService;
import lombok.extern.slf4j.Slf4j;
import lv.ays.rid.RidClientDTO;
import lv.ays.rid.RidClientParamDTO;
import lv.ays.rid.SimpleInterfaceRemote;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.bank.acs.Profile.COUNTRY_LV_PROFILE;
import static com.bank.acs.enumeration.AcsErrorCode.*;
import static com.bank.acs.enumeration.AppState.RENDER_ENTER_LOGIN_PAGE;
import static com.bank.acs.enumeration.AppState.RENDER_SELECT_AUTH_METHOD_PAGE;
import static com.bank.acs.service.lv.RoofIdRequestKeys.*;
import static com.bank.acs.util.CardUtil.maskSensitiveInformation;
import static java.util.stream.Collectors.toList;

@Slf4j
@Profile(COUNTRY_LV_PROFILE)
@Service
public class UserServiceLV extends UserService {

    protected final RoofIdRepository roofIdRepository;
    protected final SimpleInterfaceRemote simpleInterfaceRemote;
    protected final RoofIdMapper roofIdMapper;

    public UserServiceLV(
            MaxAttemptsProperties maxAttemptsProperties,
            AppProperties appProperties,
            AppSessionRepository appSessionRepository,
            RoofIdRepository roofIdRepository,
            SimpleInterfaceRemote simpleInterfaceRemote,
            RoofIdMapper roofIdMapper
    ) {
        super(maxAttemptsProperties, appProperties, appSessionRepository);
        this.roofIdRepository = roofIdRepository;
        this.simpleInterfaceRemote = simpleInterfaceRemote;
        this.roofIdMapper = roofIdMapper;
    }

    @Override
    public AppState initUserInformation(AppSession session) {
        final var pan = session.getAcctNumber();
        final var params = simpleInterfaceRemote.findRidClientParams(null, null, CARD, PERCENT + pan + PERCENT);

        log.info("findRidClientParams response size = {}", params.size());

        if (params == null || params.size() == 0) {
            log.error("No RoofId active clients found, AcsTransactionId = {}", session.getAcsTransactionId());
            throw new BusinessException(NO_ACTIVE_PERSON_ACCOUNTS_FOUND);
        }

        final var validClientDtos = getRoofIdValidClients(params, session);

        if (validClientDtos.size() == 0) {
            log.error("No RoofId active clients accounts found, AcsTransactionId = {}", session.getAcsTransactionId());
            throw new BusinessException(BLOCKED_LOGIN_CODE);
        }

        var roofIdData = roofIdRepository.findById(session.getAcsTransactionId())
                .orElse(RoofIdData.builder()
                        .acsTransactionId(session.getAcsTransactionId())
                        .build()
                );

        final var clients = validClientDtos.entrySet().stream()
                .map(item -> roofIdMapper.map(item.getValue()))
                .peek(client -> client.setRoofIdData(roofIdData))
                .collect(toList());

        if (roofIdData.getClients() == null) {
            roofIdData.setClients(new ArrayList<>());
        }
        roofIdData.getClients().addAll(clients);
        roofIdRepository.save(roofIdData);

        if (clients.size() > 1) {
            return RENDER_ENTER_LOGIN_PAGE;
        }

        this.saveChosenLogin(session, clients.get(0));
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

        this.saveChosenLogin(session, login);
        return RENDER_SELECT_AUTH_METHOD_PAGE;
    }

    @Override
    public void deleteAppSession(AppSession session) {
        super.deleteAppSession(session);
        roofIdRepository.findById(session.getAcsTransactionId())
                .ifPresent(item -> roofIdRepository.delete(item));
    }

    protected RoofIdClient findLoginByUsername(AppSession session, String loginFromUser) {
        return roofIdRepository.findById(session.getAcsTransactionId())
                .stream()
                .map(RoofIdData::getClients)
                .flatMap(data -> data.stream())
                .filter(client -> client.getClientnumber().equals(loginFromUser))
                .findFirst().orElse(null);
    }

    protected HashMap<String, RidClientDTO> getRoofIdValidClients(List<RidClientParamDTO> ridClientList, AppSession session) {

        final var validClients = new HashMap<String, RidClientDTO>();

        for (RidClientParamDTO param : ridClientList) {
            if (validClients.containsKey(param.getRidClient())) {
                log.info("getRoofIdValidClients skip duplicate client = {}, acsTransactionId = {}", param.getRidClient(), session.getAcsTransactionId());
                continue;
            }
            log.info("getRoofIdValidClients RidClientParamDTO = {}, acsTransactionId = {}", maskSensitiveInformation(param.getValue()), session.getAcsTransactionId());
            final var thisCard = new RidCardDTO(param);
            log.info("getRoofIdValidClients RidCardDTO = {}", thisCard);

            final var thisClient = simpleInterfaceRemote.getRidClientRemote(param.getRidClient(), true /* regular user */);
            log.info("getRoofIdValidClients: getRidClientRemote RidClientDTO = {}, acsTransactionId = {}", thisClient.getClientId(), session.getAcsTransactionId());

            //User is blocked
            if (!thisClient.getStatus().equalsIgnoreCase(USER_ACTIVE_KEY)) {
                log.info("User is blocked, clientId = {}, acsTransactionId = {}", thisClient.getClientId(), session.getAcsTransactionId());
                continue;
            }

            //"Can't use this card, because it is readonly"
            List<RidClientParamDTO> jf = simpleInterfaceRemote.findRidClientParams(param.getRidClient(), JF, OPTIONS, P);
            log.info("getRoofIdValidClients for 'readonly' = {}, acsTransactionId = {}", jf, session.getAcsTransactionId());

            if ((thisCard.isReadonly()) && (jf != null) && (!jf.isEmpty())) {
                log.info("Card is read only, clientId = {} acsTransactionId = {}", thisClient.getClientId(), session.getAcsTransactionId());
                continue;
            }
            validClients.put(param.getRidClient(), thisClient);
        }

        log.info("getRoofIdValidClients validClients = {}", validClients);

        return validClients;
    }

    protected void saveChosenLogin(AppSession session, RoofIdClient client) {

        List<AuthMethod> authMethodList = new ArrayList<>();
        log.info("client.getSmartId = {}", client.getSmartId());
        if (client.getSmartId()) {
            authMethodList.add(AuthMethod.SMART_ID);
        }
        //thisClient.defaultCodetableId != null && thisClient.getDefaultCodetableType() != null and "N" or "V" ; --- CODE_CALCULATOR
        //Other types not supported any more, only "N" or "V"
        if (client.getCodetableType() != null && client.getCodetableId() != null &&
                RoofIdAuthMethod.fromString(client.getCodetableType()) != null) {
            authMethodList.add(AuthMethod.CODE_CALCULATOR);
        }
        addUserAuthMethodsToSession(session, authMethodList);
        session.setUsedLanguage(parseLanguage(client.getLanguage()));
        session.setUsedAuthMethod(client.getLastUsedAuthMethod());
        session.setChosenUsername(client.getClientnumber());

        log.info("Used username = {}, acsTransactionId = {}", client.getClientnumber(), session.getAcsTransactionId());
        appSessionRepository.save(session);

        roofIdRepository.findById(session.getAcsTransactionId()).ifPresent(roofIdData -> {
            roofIdData.setUsedLogin(client);
            roofIdRepository.save(roofIdData);
        });
        getAllowedUserAuthMethods(session, false);
    }

}
