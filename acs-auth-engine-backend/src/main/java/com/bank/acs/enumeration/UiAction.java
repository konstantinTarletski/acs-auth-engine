package com.bank.acs.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumSet;

@Getter
@RequiredArgsConstructor
public enum UiAction {

    /**
     * Request: empty
     * <p>
     * Response (content):
     * {
     * state=RENDER_ENTER_LOGIN_PAGE,
     * acctNumber=...
     * purchaseDate=...
     * purchaseCurrency=...
     * merchantName=...
     * purchaseAmount=...
     * country=...
     * userLanguage=...
     * defaultAuthMethod=SMART_ID
     * availableAuthMethods=SMART_ID,CODE_CALCULATOR
     * }
     *
     * @see com.bank.acs.enumeration.AppState (possible state values)
     * @see com.bank.acs.enumeration.banktron.BanktronAuthMethod (possible auth methods)
     */
    GET_INITIAL_INFORMATION(
            EnumSet.of(
                    AppState.CHECK_CARD_STATUS,
                    AppState.CARD_CHECK_SUCCESSFUL,
                    AppState.RENDER_ENTER_LOGIN_PAGE,
                    AppState.RENDER_SELECT_AUTH_METHOD_PAGE,
                    AppState.RENDER_INIT_AUTH_PAGE,
                    AppState.CHECKING_AUTH_STATUS,
                    AppState.AUTHENTICATION_IN_PROGRESS,
                    AppState.RENDER_SUCCESSFUL_PAGE,
                    AppState.RENDER_FATAL_ERROR_PAGE,
                    AppState.RENDER_NON_FATAL_ERROR_PAGE,
                    AppState.REDIRECTED_TO_EXTERNAL_SYSTEM
            )
    ),

    /**
     * Request: {"enteredLogin": "user_1234_test"}
     * <p>
     * Response (content):
     * {
     * defaultAuthMethod=SMART_ID
     * availableAuthMethods=SMART_ID,CODE_CALCULATOR
     * }
     */
    CONFIRM_USER_LOGIN(EnumSet.of(AppState.RENDER_ENTER_LOGIN_PAGE, AppState.RENDER_NON_FATAL_ERROR_PAGE)),


    /**
     * Request: {"authMethod": "SMART_ID"}
     * <p>
     * Response (content):
     * {
     * authorizationCode=123456
     * }
     */
    CHOOSE_AUTH_METHOD(
                EnumSet.of(
                        AppState.RENDER_SELECT_AUTH_METHOD_PAGE,
                        AppState.CHECKING_AUTH_STATUS,
                        AppState.AUTHENTICATION_IN_PROGRESS,
                        AppState.RENDER_INIT_AUTH_PAGE,
                        AppState.RENDER_NON_FATAL_ERROR_PAGE,
                        AppState.REDIRECTED_TO_EXTERNAL_SYSTEM
                )
    ),

    /**
     * Request: {"authMethod": "SMART_ID", "confirmationCode": "12345"}
     * <p>
     * Response (content):
     * {
     * authorizationCode=123456
     * }
     */
    INIT_AUTH(
            EnumSet.of(
                    AppState.RENDER_SELECT_AUTH_METHOD_PAGE,
                    AppState.CHECKING_AUTH_STATUS,
                    AppState.AUTHENTICATION_IN_PROGRESS,
                    AppState.RENDER_INIT_AUTH_PAGE,
                    AppState.RENDER_NON_FATAL_ERROR_PAGE,
                    AppState.REDIRECTED_TO_EXTERNAL_SYSTEM
            )
    ),

    /**
     * Request: empty
     * <p>
     * Response (content):
     * {
     * confirmationSuccessful=true
     * }
     */
    AUTH_STATUS(EnumSet.of(AppState.CHECKING_AUTH_STATUS, AppState.AUTHENTICATION_IN_PROGRESS)),

    /**
     * Request: empty
     * <p>
     * Response (content): empty
     */
    BACK_TO_MERCHANT_SUCCESS(EnumSet.of(AppState.RENDER_SUCCESSFUL_PAGE)),

    /**
     * Request: empty
     * <p>
     * Response (content): empty
     */
    BACK_TO_MERCHANT_CANCEL(EnumSet.of(
            AppState.CHECK_CARD_STATUS,
            AppState.CARD_CHECK_SUCCESSFUL,
            AppState.RENDER_ENTER_LOGIN_PAGE,
            AppState.RENDER_SELECT_AUTH_METHOD_PAGE,
            AppState.RENDER_INIT_AUTH_PAGE,
            AppState.CHECKING_AUTH_STATUS,
            AppState.AUTHENTICATION_IN_PROGRESS,
            AppState.RENDER_SUCCESSFUL_PAGE,
            AppState.RENDER_FATAL_ERROR_PAGE,
            AppState.RENDER_NON_FATAL_ERROR_PAGE,
            AppState.REDIRECTED_TO_EXTERNAL_SYSTEM
    )),

    /**
     * Request: { "language": "en" }
     * <p>
     * Response (content): empty
     */
    CHANGE_CURRENT_LANGUAGE(EnumSet.of(
            AppState.CHECK_CARD_STATUS,
            AppState.CARD_CHECK_SUCCESSFUL,
            AppState.RENDER_ENTER_LOGIN_PAGE,
            AppState.RENDER_SELECT_AUTH_METHOD_PAGE,
            AppState.RENDER_INIT_AUTH_PAGE,
            AppState.CHECKING_AUTH_STATUS,
            AppState.AUTHENTICATION_IN_PROGRESS,
            AppState.RENDER_SUCCESSFUL_PAGE,
            AppState.RENDER_FATAL_ERROR_PAGE,
            AppState.RENDER_NON_FATAL_ERROR_PAGE,
            AppState.REDIRECTED_TO_EXTERNAL_SYSTEM
    ));

    private final EnumSet<AppState> availableAppStates;

}
