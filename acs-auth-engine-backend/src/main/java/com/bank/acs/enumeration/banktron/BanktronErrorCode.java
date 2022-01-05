package com.bank.acs.enumeration.banktron;

import com.bank.acs.enumeration.AcsErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum BanktronErrorCode {

    NO_SMART_ID(-40013,
            Optional.of(Map.of(AcsErrorCode.NO_SMART_ID, EnumSet.of(BanktronEndpoint.AUTHENTICATE, BanktronEndpoint.CHECKSTATUS)))),
    AUTHENTICATION_IN_PROGRESS(-290,
            Optional.of(Map.of(
                    AcsErrorCode.AUTHENTICATION_IN_PROGRESS, EnumSet.of(BanktronEndpoint.CHECKSTATUS)
            ))
    ),
    NO_ANSWER(-200,
            Optional.of(Map.of(
                    AcsErrorCode.SMART_ID_TIMEOUT, EnumSet.of(BanktronEndpoint.CHECKSTATUS, BanktronEndpoint.AUTHENTICATE)
            ))
    ),
    CONNECTION_MESSAGE_REJECTED(-199,
            Optional.of(Map.of(
                    AcsErrorCode.SMART_ID_FAILED, EnumSet.of(BanktronEndpoint.AUTHENTICATE, BanktronEndpoint.CHECKSTATUS)
            ))
    ),
    SESSION_HAS_EXPIRED(-167,
            Optional.of(Map.of(AcsErrorCode.SESSION_HAS_EXPIRED, EnumSet.of(BanktronEndpoint.AUTHENTICATE, BanktronEndpoint.CONFIRMCREDENTIALS, BanktronEndpoint.CHECKSTATUS)))),
    INCORRECT_CODE(-103,
            Optional.of(Map.of(AcsErrorCode.WRONG_CODE_FOR_CODE_CALCULATOR, EnumSet.of(BanktronEndpoint.CONFIRMCREDENTIALS)))),
    INVALID_WORKING_OBJECT(-20,
            Optional.of(Map.of(AcsErrorCode.FATAL_EXCEPTION, EnumSet.of(BanktronEndpoint.AUTHENTICATE, BanktronEndpoint.CONFIRMCREDENTIALS, BanktronEndpoint.CHECKSTATUS)))),
    BAD_STATUS_OF_THE_WORK_OBJECT(-21,
            Optional.of(Map.of(AcsErrorCode.OPERATION_FAILED, EnumSet.of(BanktronEndpoint.CONFIRMCREDENTIALS)))),
    INCORRECT_STATUS_OF_SESSION_AUTHENTICATION(-14,
            Optional.of(Map.of(
                    AcsErrorCode.OPERATION_FAILED, EnumSet.of(BanktronEndpoint.CHECKSTATUS),
                    AcsErrorCode.OPERATION_TERMINATED, EnumSet.of(BanktronEndpoint.AUTHENTICATE)
            ))
    ),
    SESSION_TERMINATED(-13,
            Optional.of(Map.of(AcsErrorCode.OPERATION_TERMINATED, EnumSet.of(BanktronEndpoint.AUTHENTICATE, BanktronEndpoint.CHECKSTATUS, BanktronEndpoint.CONFIRMCREDENTIALS)))),
    BLOCKED_LOGIN(-11,
            Optional.of(Map.of(AcsErrorCode.BLOCKED_LOGIN_CODE, EnumSet.of(BanktronEndpoint.AUTHENTICATE, BanktronEndpoint.CHECKSTATUS, BanktronEndpoint.CONFIRMCREDENTIALS)
            ))
    ),
    BLOCKED_LOGIN_CODE(-10,
            Optional.of(Map.of(
                    AcsErrorCode.BLOCKED_LOGIN_CODE, EnumSet.of(BanktronEndpoint.AUTHENTICATE, BanktronEndpoint.CHECKSTATUS)
            ))
    ),
    INTERNAL_SERVICE_IS_NOT_REACHABLE(2,
            Optional.of(Map.of(AcsErrorCode.INTERNAL_SERVICE_IS_NOT_REACHABLE, EnumSet.of(BanktronEndpoint.PERSONLOGINS)))),
    LOGIN_IS_ARCHIVED(40653, Optional.empty()),
    THIS_LOGIN_DOES_NOT_HAVE_ANY_AUTHENTICATION_TYPES_ASSOCIATED_WITH_THIS_SERVICE(40654, Optional.empty()),
    LOGIN_NAME_NOT_SUPPLIED(40656,
            Optional.of(Map.of(AcsErrorCode.AUTHENTICATION_FAILED, EnumSet.of(BanktronEndpoint.AUTHENTICATE)))),
    NO_AUTHENTICATION_TYPE_NAMES_FOUND_IN_ANY_LANGUAGES(40657, Optional.empty()),
    LOGIN_IS_BLOCKED(40665, Optional.empty()),
    SESSION_TOKEN_NOT_SUPPLIED(40705,
            Optional.of(Map.of(
                    AcsErrorCode.OPERATION_FAILED, EnumSet.of(BanktronEndpoint.CHECKSTATUS, BanktronEndpoint.CONFIRMCREDENTIALS, BanktronEndpoint.LOGOUT),
                    AcsErrorCode.AUTHENTICATION_FAILED, EnumSet.of(BanktronEndpoint.AUTHENTICATE)
            ))
    ),
    AUTHENTICATION_TYPE_ID_NOT_SUPPLIED(40706,
            Optional.of(Map.of(AcsErrorCode.AUTHENTICATION_FAILED, EnumSet.of(BanktronEndpoint.AUTHENTICATE)))),
    SESSION_DOES_NOT_EXIST(40707,
            Optional.of(Map.of(
                    AcsErrorCode.AUTHENTICATION_FAILED, EnumSet.of(BanktronEndpoint.AUTHENTICATE),
                    AcsErrorCode.SESSION_DOES_NOT_EXIST, EnumSet.of(BanktronEndpoint.CHECKSTATUS),
                    AcsErrorCode.OPERATION_FAILED, EnumSet.of(BanktronEndpoint.LOGOUT),
                    AcsErrorCode.OPERATION_TERMINATED, EnumSet.of(BanktronEndpoint.CONFIRMCREDENTIALS)
            ))
    ),
    SESSION_IS_CURRENTLY_BEING_AUTHENTICATED_TO_WITH_A_DIFFERENT_AUTHENTICATION_TYPE(40708,
            Optional.of(Map.of(AcsErrorCode.OPERATION_FAILED, EnumSet.of(BanktronEndpoint.CHECKSTATUS)))),
    THIS_AUTHENTICATION_TYPE_IS_NOT_SUPPORTED(40709,
            Optional.of(Map.of(AcsErrorCode.AUTHENTICATION_FAILED, EnumSet.of(BanktronEndpoint.AUTHENTICATE)))),
    THIS_AUTHENTICATION_TYPE_CANNOT_BE_USED_WITH_THIS_LOGIN(40710,
            Optional.of(Map.of(AcsErrorCode.THIS_AUTHENTICATION_TYPE_CANNOT_BE_USED_WITH_THIS_LOGIN, EnumSet.of(BanktronEndpoint.AUTHENTICATE)))),
    FAILED_TO_GENERATE_AUTHENTICATION_TOKEN(40711,
            Optional.of(Map.of(AcsErrorCode.AUTHENTICATION_FAILED, EnumSet.of(BanktronEndpoint.AUTHENTICATE)))),
    CUSTOMER_BACKEND_ID_NOT_SUPPLIED(40712,
            Optional.of(Map.of(AcsErrorCode.OPERATION_FAILED, EnumSet.of(BanktronEndpoint.CONFIRMCREDENTIALS)))),
    AUTHENTICATION_CODE_NOT_SUPPLIED(40713,
            Optional.of(Map.of(AcsErrorCode.WRONG_CODE_FOR_CODE_CALCULATOR, EnumSet.of(BanktronEndpoint.CONFIRMCREDENTIALS)))),
    CUSTOMER_WITH_THE_SUPPLIED_BACKEND_ID_DOES_NOT_EXIST(40714,
            Optional.of(Map.of(AcsErrorCode.OPERATION_FAILED, EnumSet.of(BanktronEndpoint.CONFIRMCREDENTIALS)))),
    LOGIN_DOES_NOT_MATCH_CUSTOMER(40715,
            Optional.of(Map.of(AcsErrorCode.FATAL_EXCEPTION, EnumSet.of(BanktronEndpoint.AUTHENTICATE, BanktronEndpoint.CONFIRMCREDENTIALS, BanktronEndpoint.CHECKSTATUS)))),
    FAILED_TO_ASSIGN_SESSION_A_CUSTOMER(40716,
            Optional.of(Map.of(AcsErrorCode.OPERATION_FAILED, EnumSet.of(BanktronEndpoint.CONFIRMCREDENTIALS)))),
    BLOCKED_LOGIN_40882(40882,
            Optional.of(Map.of(
                    AcsErrorCode.BLOCKED_LOGIN_CODE, EnumSet.of(BanktronEndpoint.AUTHENTICATE, BanktronEndpoint.CHECKSTATUS)
            ))
    ),
    BLOCKED_LOGIN_40920(40920,
            Optional.of(Map.of(
                    AcsErrorCode.BLOCKED_LOGIN_CODE, EnumSet.of(BanktronEndpoint.AUTHENTICATE, BanktronEndpoint.CHECKSTATUS)
            ))
    ),
    PERSON_CODE_NOT_SUPPLIED(50115,
            Optional.of(Map.of(AcsErrorCode.NO_LOGINS_AVAILABLE, EnumSet.of(BanktronEndpoint.PERSONLOGINS)))),
    NO_PERSONS_FOUND(50116,
            Optional.of(Map.of(AcsErrorCode.NO_LOGINS_AVAILABLE, EnumSet.of(BanktronEndpoint.PERSONLOGINS)))),
    SMART_ID_BLOCKED(50154,
            Optional.of(Map.of(AcsErrorCode.SMART_ID_BLOCKED, EnumSet.of(BanktronEndpoint.AUTHENTICATE, BanktronEndpoint.CHECKSTATUS)))),
    NO_PHONE_NUMBER(50262,
            Optional.of(Map.of(
                    AcsErrorCode.NO_PHONE_NUMBER, EnumSet.of(BanktronEndpoint.AUTHENTICATE, BanktronEndpoint.CHECKSTATUS)
            ))
    ),
    SK74_NO_MID_CERT(50264,
            Optional.of(Map.of(
                    AcsErrorCode.SK74_NO_MID_CERT, EnumSet.of(BanktronEndpoint.AUTHENTICATE, BanktronEndpoint.CHECKSTATUS)
            ))
    ),
    USER_CANCELLED_OPERATION(50265,
            Optional.of(Map.of(
                    AcsErrorCode.MOBILE_ID_FAILED, EnumSet.of(BanktronEndpoint.CHECKSTATUS)
            ))
    ),
    SK73_TIMEOUT(50266,
            Optional.of(Map.of(
                    AcsErrorCode.SK73_TIMEOUT, EnumSet.of(BanktronEndpoint.AUTHENTICATE, BanktronEndpoint.CHECKSTATUS)
            ))
    ),
    OTHER_SESSION_END_RESULT_CODE(50267,
            Optional.of(Map.of(
                    AcsErrorCode.OTHER_SESSION_END_RESULT_CODE, EnumSet.of(BanktronEndpoint.AUTHENTICATE, BanktronEndpoint.CHECKSTATUS)
            ))
    ),
    SK75_MID_CONFIG_ERROR(50268,
            Optional.of(Map.of(
                    AcsErrorCode.SK75_MID_CONFIG_ERROR, EnumSet.of(BanktronEndpoint.AUTHENTICATE, BanktronEndpoint.CHECKSTATUS)
            ))
    ),
    SK76_SIM_NOT_AVAILABLE(50269,
            Optional.of(Map.of(
                    AcsErrorCode.SK76_SIM_NOT_AVAILABLE, EnumSet.of(BanktronEndpoint.AUTHENTICATE, BanktronEndpoint.CHECKSTATUS)
            ))
    ),
    SK77_SMS_SENDING_ERROR(50270,
            Optional.of(Map.of(
                    AcsErrorCode.AUTHENTICATION_FAILED, EnumSet.of(BanktronEndpoint.AUTHENTICATE, BanktronEndpoint.CHECKSTATUS)
            ))
    ),
    SK78_SIM_ERROR(50271,
            Optional.of(Map.of(
                    AcsErrorCode.SK78_SIM_ERROR, EnumSet.of(BanktronEndpoint.AUTHENTICATE, BanktronEndpoint.CHECKSTATUS)
            ))
    ),
    AUTH_TYPE_TEMP_DISABLED(50310,
            Optional.of(Map.of(
                    AcsErrorCode.AUTH_TYPE_TEMP_DISABLED, EnumSet.of(BanktronEndpoint.AUTHENTICATE)
            ))
    ),


    UNDEFINED_EXCEPTION(Integer.MAX_VALUE, Optional.empty()),
    ;

    @JsonValue
    private final Integer code;

    @JsonIgnore
    private final Optional<Map<AcsErrorCode, EnumSet<BanktronEndpoint>>> acsErrorCodes;

    @SuppressWarnings("unused")
    @JsonCreator
    public static BanktronErrorCode fromCode(String code) {
        return Arrays.stream(values()).filter(v -> v.getCode().equals(Integer.valueOf(code))).findFirst().orElse(UNDEFINED_EXCEPTION);
    }

    public static AcsErrorCode findAcsErrorByBanktronErrorCode(String code, BanktronEndpoint endpoint) {
        final var errorCode = BanktronErrorCode.fromCode(code);
        return errorCode.getAcsErrorCodes().stream()
                .flatMap(item -> item.entrySet().stream())
                .filter(item -> item.getValue().contains(endpoint))
                .map(item -> item.getKey())
                .findFirst().orElse(AcsErrorCode.GENERAL_EXCEPTION);
    }
}
