package com.bank.acs.service.lv;

import com.bank.acs.config.property.AppProperties;
import com.bank.acs.config.property.AppPropertiesLV;
import com.bank.acs.config.property.MaxAttemptsProperties;
import com.bank.acs.entity.AppSession;
import com.bank.acs.enumeration.AcsErrorCode;
import com.bank.acs.enumeration.AppState;
import com.bank.acs.enumeration.AuthMethod;
import com.bank.acs.exception.BusinessException;
import com.bank.acs.repository.AppSessionRepository;
import com.bank.acs.repository.RoofIdRepository;
import com.bank.acs.service.AppStateService;
import com.bank.acs.service.AuthMethodService;
import com.bank.acs.service.TranslationService;
import com.bank.acs.service.UserService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lv.ays.rid.ESmartIdStatus;
import lv.ays.rid.RidSmartIdResponseDTO;
import lv.ays.rid.SimpleInterfaceRemote;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.EnumSet;
import java.util.Optional;

import static com.bank.acs.Profile.COUNTRY_LV_PROFILE;
import static com.bank.acs.enumeration.AppState.RENDER_SUCCESSFUL_PAGE;
import static com.bank.acs.enumeration.AuthMethod.*;

@Slf4j
@Profile(COUNTRY_LV_PROFILE)
@Service
public class AuthMethodServiceLV extends AuthMethodService {

    public static final String MESSAGE_TO_USER_DEFAULT = "Please confirm the payment for %s EUR";

    protected final SimpleInterfaceRemote simpleInterfaceRemote;
    protected final RoofIdRepository roofIdRepository;
    protected final TranslationService translationService;
    protected final AppPropertiesLV appPropertiesLV;
    protected final UserService userService;
    protected final AuthStatusCheckThreadService authStatusCheckThreadService;

    public AuthMethodServiceLV(MaxAttemptsProperties maxAttemptsProperties,
                               AppSessionRepository appSessionRepository,
                               AppStateService appStateService,
                               AppProperties appProperties,
                               SimpleInterfaceRemote simpleInterfaceRemote,
                               RoofIdRepository roofIdRepository,
                               TranslationService translationService,
                               AppPropertiesLV appPropertiesLV,
                               UserService userService,
                               AuthStatusCheckThreadService authStatusCheckThreadService
    ) {
        super(maxAttemptsProperties, appSessionRepository, appStateService, appProperties);
        this.simpleInterfaceRemote = simpleInterfaceRemote;
        this.roofIdRepository = roofIdRepository;
        this.translationService = translationService;
        this.appPropertiesLV = appPropertiesLV;
        this.userService = userService;
        this.authStatusCheckThreadService = authStatusCheckThreadService;
    }

    @Override
    public String initSmartId(AppSession session) {
        final var result = simpleInterfaceRemote.smartIdAuthInit(session.getChosenUsername(), getMessageToSign(session));

        log.info("smartIdAuthInit respond with status = {} smartIdHash = {}, AcsTransactionId = {}", result.getStatus(), result.getHash(), session.getAcsTransactionId());

        if (ESmartIdStatus.ERROR.name().equalsIgnoreCase(result.getStatus())) {
            log.error("SmartIdAuthInit ERROR status = {} AppSession = {}", result.getStatus(), session);
            throw new BusinessException(AcsErrorCode.AUTHENTICATION_FAILED);
        }

        roofIdRepository.findById(session.getAcsTransactionId()).ifPresent(
                roofId -> {
                    roofId.setSmartIdHash(result.getHash());
                    roofIdRepository.save(roofId);
                }
        );

        // "status" it 4 digit code what should be shown to user
        log.info("SmartIdAuthInit success PIN to user is - {}, AcsTransactionId = {}", result.getStatus(), session.getAcsTransactionId());
        return result.getStatus();
    }

    @Override
    public String initMSignature(AppSession session) {
        //LV do not have M_SIGNATURE
        log.info("initMSignature, LV do not have {}, AcsTransactionId = {}", M_SIGNATURE, session.getAcsTransactionId());
        throw new BusinessException(AcsErrorCode.AUTHENTICATION_FAILED);
    }

    @Override
    public String initIDCard(AppSession session) {
        log.info("initIDCard, LV does not have {}, AcsTransactionId = {}", ID_CARD, session.getAcsTransactionId());
        throw new BusinessException(AcsErrorCode.AUTHENTICATION_FAILED);
    }

    @SneakyThrows
    @Override
    public Optional<Boolean> checkAuthMethodStatus(AppSession session) {
        var roofIdOptional = roofIdRepository.findById(session.getAcsTransactionId());

        if (roofIdOptional.isEmpty()){
            throw new BusinessException(AcsErrorCode.AUTHENTICATION_FAILED);
        }

        var roofId = roofIdOptional.get();
        Optional<RidSmartIdResponseDTO> result =
                authStatusCheckThreadService.checkSmartIdStatus(
                        session.getAcsTransactionId(),
                        session.getChosenUsername(),
                        roofId.getSmartIdHash(),
                        getMessageToUser(session)
                );

        if (result.isEmpty()) {
            return Optional.empty();
        }

        RidSmartIdResponseDTO responseDTO = result.get();

        log.info("Taking smartIdAuthCheck response: hash = {} and status = {}, AcsTransactionId = {}", responseDTO.getHash(), responseDTO.getStatus(), session.getAcsTransactionId());
        roofId.setSmartIdHash(responseDTO.getHash());
        roofIdRepository.save(roofId);

        switch(responseDTO.getStatus()) {
            case OK:
                if (responseDTO.isNeedCheck()) {
                    throw new BusinessException(AcsErrorCode.VALIDATE_SMART_ID);
                }
                return Optional.of(Boolean.TRUE);
            case CANCEL:
                throw new BusinessException(AcsErrorCode.SMART_ID_FAILED);
            case EXPIRED:
                throw new BusinessException(AcsErrorCode.SMART_ID_TIMEOUT);
            case ERROR_ACCOUNT:
                throw new BusinessException(AcsErrorCode.SMART_ID_BLOCKED);
            case NOT_FOUND:
                EnumSet<AuthMethod> allowedAuthMethods = userService.getAllowedUserAuthMethods(session, false);
                if (allowedAuthMethods.size() == 1) {
                    throw new BusinessException(AcsErrorCode.NO_SUPPORTED_AUTH_METHODS_AVAILABLE);
                }
                throw new BusinessException(AcsErrorCode.NO_SMART_ID);
            default:
                throw new BusinessException(AcsErrorCode.AUTHENTICATION_FAILED);
        }
    }

    /**
     * This text will be shown to user
     *
     * @param session AppSession
     * @return Text to show for user in format "MESSAGE_TO_USER_DEFAULT"
     */
    protected String getMessageToUser(AppSession session) {
        final var language = session.getUsedLanguage();
        String text = translationService.getMessageByJsonPath(
                String.format(appPropertiesLV.getSmartIdMessageToUserJsonPath(), language),
                MESSAGE_TO_USER_DEFAULT
        );
        final var resultText = String.format(text, session.getPurchaseAmount());
        log.info("Result text is = {}, AcsTransactionId = {}", resultText, session.getAcsTransactionId());
        return resultText;
    }

    /**
     * This text will be digitally signed, it will not be shown to user.
     *
     * @param session AppSession
     * @return Text to sign in format "acsTransactionId=%s;amount=%s;userId=%s"
     */
    private String getMessageToSign(AppSession session) {
        return String.format("acsTransactionId=%s;amount=%s;userId=%s",
                session.getAcsTransactionId(),
                session.getPurchaseAmount(),
                session.getChosenUsername());
    }

    @Override
    public AppState confirmCodeCalculator(AppSession session, String confirmationCode) {
        Assert.isTrue(session.getUsedAuthMethod() == CODE_CALCULATOR, "Session auth method is not Code Calculator");

        roofIdRepository.findById(session.getAcsTransactionId()).ifPresent(roofIdData -> {
            log.info("confirmCodeCalculator, AcsTransactionId = {}, CodetableName = {}, CodetableType = {}",
                    session.getAcsTransactionId(), roofIdData.getUsedLogin().getCodetableName(), roofIdData.getUsedLogin().getCodetableType());
            if (!simpleInterfaceRemote.codetableCheck(
                    roofIdData.getUsedLogin().getCodetableName(), roofIdData.getUsedLogin().getCodetableType(), "0", confirmationCode, true /* regular user */)
            ) {

                final var thisClient = simpleInterfaceRemote.getRidClientRemote(roofIdData.getUsedLogin().getClientnumber(), true /* regular user */);
                if (RoofIdRequestKeys.USER_SELF_BLOCKED_KEY.equalsIgnoreCase(thisClient.getStatus())) {
                    log.info("confirmCodeCalculator, session AcsTransactionId = {}, clientNumber = {} BLOCKED_LOGIN_CODE",
                            session.getAcsTransactionId(), roofIdData.getUsedLogin().getClientnumber());
                    throw new BusinessException(AcsErrorCode.BLOCKED_LOGIN_CODE);
                }
                log.info("confirmCodeCalculator, session AcsTransactionId = {}, clientNumber = {} WRONG_CODE_FOR_CODE_CALCULATOR",
                        session.getAcsTransactionId(), roofIdData.getUsedLogin().getClientnumber());
                throw new BusinessException(AcsErrorCode.WRONG_CODE_FOR_CODE_CALCULATOR);
            }
        });
        return RENDER_SUCCESSFUL_PAGE;
    }

    @Override
    public AppState confirmIDCard(AppSession session, String authorizationCode) {
        log.info("confirmIDCard, LV does not have {}, AcsTransactionId = {}", ID_CARD, session.getAcsTransactionId());
        throw new BusinessException(AcsErrorCode.AUTHENTICATION_FAILED);
    }


}
