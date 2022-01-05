package com.bank.acs;

import com.bank.acs.config.property.MaxAttemptsProperties;
import com.bank.acs.constant.RequestParams;
import com.bank.acs.constant.ResponseParams;
import com.bank.acs.dto.challenge.ChallengeHeadersDto;
import com.bank.acs.dto.challenge.request.ChallengeRequestAcsRenderingTypeDto;
import com.bank.acs.dto.challenge.request.ChallengeRequestDto;
import com.bank.acs.dto.challenge.request.ChallengeRequestPurchaseDto;
import com.bank.acs.dto.challenge.response.ChallengeResponseDto;
import com.bank.acs.enumeration.AcsErrorCode;
import com.bank.acs.enumeration.AppState;
import com.bank.acs.enumeration.AuthMethod;
import com.bank.acs.enumeration.UiAction;
import com.bank.acs.handler.ErrorHandlingControllerAdvice;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.SneakyThrows;
import lv.bank.cards.rtcu.util.BankCardsWSWrapperDelegate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.bank.acs.enumeration.AuthMethod.CODE_CALCULATOR;
import static com.bank.acs.enumeration.AuthMethod.SMART_ID;
import static com.bank.acs.enumeration.ChallengeMethod.POST;
import static com.bank.acs.enumeration.DeviceChannel.BROWSER;
import static com.bank.acs.enumeration.TransactionStatus.CHALLENGE_REQUIRED;
import static com.bank.acs.enumeration.TransactionStatus.VERIFICATION_SUCCESSFUL;
import static com.bank.acs.util.UrlQueryUtil.convertToQueryString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.ResourceUtils.getFile;

public abstract class IntegrationTestBase {

    public static final String PAN = "1234567890123456";
    public static final String SESSION_TOKEN = "SESSION_TOKEN";
    public static final String creq = "ewogICJhY3NUcmFuc0lEIjogImI0N2MwYjQ0LThkMWQtNDU1Mi05YmEwLTRhZjhhZmFlNjU1NyIsCiAgImNoYWxsZW5nZVdpbmRvd1NpemUiOiAiMDEiLAogICJtZXNzYWdlVHlwZSI6ICJDUmVxIiwKICAibWVzc2FnZVZlcnNpb24iOiAiMi4xLjAiLAogICJ0aHJlZURTU2VydmVyVHJhbnNJRCI6ICJjZDBkMGRmNC02OGNkLTQ4NmUtOTNkYy1lNmJkOWExOWYwZDkiCn0";


    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    protected BankCardsWSWrapperDelegate linkAppWsMock;

    @Autowired
    protected MaxAttemptsProperties maxAttemptsProperties;

    @Value("${app.api.challenge.url}")
    protected String challengeApi;

    public abstract void mockInitUserInformation(String pan, String username, String language, String sessionToken, List<AuthMethod> authMethods);

    public abstract void mockSmartIdAuthInit(String acsTransactionId, String username, String authToken, String amount);

    public abstract void mockCodeCalculatorAuthInit(String acsTransactionId, String username, String authToken, String amount, String confirmationCode);

    public abstract void mockCodeCalculatorSuccess(String acsTransactionId, String username, String authToken, String amount, String confirmationCode);

    public abstract void mockCodeCalculatorWrongCode(String acsTransactionId, String username, String authToken, String amount, String confirmationCode);

    public abstract AcsErrorCode getCodeCalculatorWrongCodeError();

    public abstract void mockSmartIdCheckAuthMethodStatus(String acsTransactionId, String username, String amount);

    public abstract void resetRestTemplateServer();

    public abstract void mockLogout(String acsTransactionId);

    public abstract void testSmartIDResponse(ChallengeRequestDto dto);

    @SneakyThrows
    @Test
    void testReachMaxChangeBiAttempts() {

        final var acsTransactionId = UUID.randomUUID().toString();
        final var userLanguage = "RU";
        final var amount = "999";
        final var username = "363066";
        final var authMethodList = List.of(SMART_ID, CODE_CALCULATOR);
        final var dto = successGetHtml(acsTransactionId, username, userLanguage, authMethodList);

        // when, then (2): Get user state request
        Map<String, String> data = new HashMap<>();
        data.put(RequestParams.UI_ACTION_PARAM, UiAction.GET_INITIAL_INFORMATION.name());
        dto.setContent(convertToQueryString(data));

        mvc.perform(post(challengeApi).contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.content",
                        containsString(ResponseParams.STATE_PARAM + "=" + AppState.RENDER_SELECT_AUTH_METHOD_PAGE)))
                .andExpect(jsonPath("$.content", containsString(SMART_ID.name())))
                .andExpect(jsonPath("$.content", containsString(CODE_CALCULATOR.name())));

        // when, then (3): Get user auth methods request
        final var confirmationCode = "123456";
        final var authToken = "1234";
        data = new HashMap<>();
        data.put(RequestParams.UI_ACTION_PARAM, UiAction.INIT_AUTH.name());
        data.put(RequestParams.SELECTED_AUTH_METHOD_PARAM, SMART_ID.name());
        data.put(RequestParams.CONFIRMATION_CODE_PARAM, confirmationCode);
        dto.setContent(convertToQueryString(data));

        // when, there
        for (int i = 0; i < maxAttemptsProperties.getForChangeAuthMethod(); i++) {
            mockSmartIdAuthInit(acsTransactionId, username, authToken, amount);
            sendChallengeRequestAndWaitForString(dto, acsTransactionId, ResponseParams.AUTHORIZATION_CODE_PARAM + "=" + authToken);
            resetRestTemplateServer();
            mockLogout(acsTransactionId);
            mockInitUserInformation(PAN, username, userLanguage, SESSION_TOKEN, authMethodList);

        }

        sendChallengeRequestAndWaitForError(dto, acsTransactionId, AcsErrorCode.EXCEEDED_MAX_ATTEMPTS_FOR_AUTH_METHOD_CHANGE);
        resetRestTemplateServer();
    }

    @SneakyThrows
    @Test
    void testSmartIdSuccess() {
        final var acsTransactionId = UUID.randomUUID().toString();
        final var userLanguage = "en";
        final var amount = "999";
        final var username = "363066";
        final var authMethodList = List.of(SMART_ID);
        final var dto = successGetHtml(acsTransactionId, username, userLanguage, authMethodList);

        // when, then (2): Get user state request
        Map<String, String> data = new HashMap<>();
        data.put(RequestParams.UI_ACTION_PARAM, UiAction.GET_INITIAL_INFORMATION.name());
        dto.setContent(convertToQueryString(data));

        mvc.perform(post(challengeApi).contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.content",
                        containsString(ResponseParams.STATE_PARAM + "=" + AppState.RENDER_SELECT_AUTH_METHOD_PAGE)))
                .andExpect(jsonPath("$.content",
                        containsString(ResponseParams.AVAILABLE_AUTH_METHODS_PARAM + "=")))
                .andExpect(jsonPath("$.content",
                        containsString(AuthMethod.SMART_ID.name())))
                .andExpect(jsonPath("$.content",
                        containsString(ResponseParams.USER_LANGUAGE + "=" + userLanguage)))
                .andExpect(jsonPath("$.content", containsString(ResponseParams.DEFAULT_AUTH_METHOD_PARAM)));

        final var authToken = "1234";
        data = new HashMap<>();
        data.put(RequestParams.UI_ACTION_PARAM, UiAction.INIT_AUTH.name());
        data.put(RequestParams.SELECTED_AUTH_METHOD_PARAM, SMART_ID.name());
        dto.setContent(convertToQueryString(data));

        mockSmartIdAuthInit(acsTransactionId, username, authToken, amount);
        sendChallengeRequestAndWaitForString(dto, acsTransactionId, ResponseParams.AUTHORIZATION_CODE_PARAM + "=" + authToken);
        resetRestTemplateServer();

        mockSmartIdCheckAuthMethodStatus(acsTransactionId, username, amount);

        data = new HashMap<>();
        data.put(RequestParams.UI_ACTION_PARAM, UiAction.AUTH_STATUS.name());
        dto.setContent(convertToQueryString(data));

        testSmartIDResponse(dto);

        resetRestTemplateServer();

        handleSendCurrentStatusToTietoAcs(acsTransactionId, dto);
    }

    @SneakyThrows
    @Test
    void testCodeCalculatorSuccess() {

        final var acsTransactionId = UUID.randomUUID().toString();
        final var userLanguage = "ru";
        final var amount = "999";
        final var username = "someLogin";
        final var authMethodList = List.of(CODE_CALCULATOR);
        final var dto = successGetHtml(acsTransactionId, username, userLanguage, authMethodList);

        // when, then (2): Get user state request
        Map<String, String> data = new HashMap<>();
        data.put(RequestParams.UI_ACTION_PARAM, UiAction.GET_INITIAL_INFORMATION.name());
        dto.setContent(convertToQueryString(data));

        mvc.perform(post(challengeApi).contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.content",
                        containsString(ResponseParams.STATE_PARAM + "=" + AppState.RENDER_SELECT_AUTH_METHOD_PAGE.name())))
                .andExpect(jsonPath("$.content",
                        containsString(ResponseParams.AVAILABLE_AUTH_METHODS_PARAM + "=" + AuthMethod.CODE_CALCULATOR.name())))
                .andExpect(jsonPath("$.content",
                        containsString(ResponseParams.USER_LANGUAGE + "=" + userLanguage)))
                .andExpect(jsonPath("$.content", containsString(ResponseParams.DEFAULT_AUTH_METHOD_PARAM)));

        // when, then (3): Get user auth methods request
        final var confirmationCode = "123456";
        final var authToken = "SOME_STRING";
        data = new HashMap<>();
        data.put(RequestParams.UI_ACTION_PARAM, UiAction.INIT_AUTH.name());
        data.put(RequestParams.SELECTED_AUTH_METHOD_PARAM, AuthMethod.CODE_CALCULATOR.name());
        data.put(RequestParams.CONFIRMATION_CODE_PARAM, confirmationCode);
        dto.setContent(convertToQueryString(data));

        mockCodeCalculatorAuthInit(acsTransactionId, username, authToken, amount, confirmationCode);
        mockCodeCalculatorWrongCode(acsTransactionId, username, authToken, amount, confirmationCode);

        sendChallengeRequestAndWaitForError(dto, acsTransactionId, getCodeCalculatorWrongCodeError());
        resetRestTemplateServer();

        mockLogout(acsTransactionId);
        mockInitUserInformation(PAN, username, userLanguage, SESSION_TOKEN, authMethodList);
        mockCodeCalculatorAuthInit(acsTransactionId, username, authToken, amount, confirmationCode);
        mockCodeCalculatorSuccess(acsTransactionId, username, authToken, amount, confirmationCode);

        // when, then
        mvc.perform(post(challengeApi).contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.acsTransID", is(acsTransactionId)));
        resetRestTemplateServer();

        handleSendCurrentStatusToTietoAcs(acsTransactionId, dto);
    }

    @Test
    void testLinkAppCartBlockedRms() {
        // given
        final var acsTransactionId = UUID.randomUUID().toString();
        final var userLanguage = "RU";
        final var username = "363066";
        final var authMethodList = List.of(SMART_ID, CODE_CALCULATOR);
        final var dto = buildRequestDto(acsTransactionId, PAN);
        final var cardHolderPersonalCode = "010101-12345";
        final var cardCountry = "LT";

        mockLickUpBlockRms(PAN, cardHolderPersonalCode, cardCountry);
        mockInitUserInformation(PAN, username, userLanguage, SESSION_TOKEN, authMethodList);
        mockLogout(acsTransactionId);

        Map<String, String> data = new HashMap<>();
        data.put(RequestParams.CREQ_PARAM, creq);
        dto.setContent(convertToQueryString(data));

        // when, then (1): Check card details + get React App on initial request
        sendChallengeRequestAndWaitForString(
                HttpStatus.BAD_REQUEST,
                dto,
                acsTransactionId,
                "<div id=\"root\">",
                RequestParams.CREQ_PARAM.toUpperCase() + ": \"" + creq + "\"",
                AcsErrorCode.CARD_BLOCKED_RMS.name()
        );
        resetRestTemplateServer();
    }

    @Test
    void testApplicationInvalidStateError() {
        // given
        final var acsTransactionId = UUID.randomUUID().toString();
        final var userLanguage = "RU";
        final var username = "363066";
        final var authMethodList = List.of(SMART_ID, CODE_CALCULATOR);
        final var dto = buildRequestDto(acsTransactionId, PAN);
        final var cardHolderPersonalCode = "010101-12345";
        final var cardCountry = "LT";

        mockSuccessfulCardCheckResponses(PAN, cardHolderPersonalCode, cardCountry);
        mockInitUserInformation(PAN, username, userLanguage, SESSION_TOKEN, authMethodList);
        mockLogout(acsTransactionId);

        Map<String, String> data = new HashMap<>();
        data.put(RequestParams.CREQ_PARAM, creq);
        dto.setContent(convertToQueryString(data));

        // when, then (1): Check card details + get React App on initial request
        sendChallengeRequestAndWaitForString(dto, acsTransactionId,
                "<div id=\"root\">",
                RequestParams.CREQ_PARAM.toUpperCase() + ": \"" + creq + "\""
        );
        resetRestTemplateServer();

        data = new HashMap<>();
        data.put(RequestParams.UI_ACTION_PARAM, UiAction.BACK_TO_MERCHANT_SUCCESS.name());
        dto.setContent(convertToQueryString(data));

        sendChallengeRequestAndWaitForError(dto, acsTransactionId, AcsErrorCode.WRONG_APPLICATION_STATE);
        resetRestTemplateServer();
    }

    @SneakyThrows
    @Test
    void testValidationForRequestDto() {
        // given
        final var acsTransactionId = UUID.randomUUID().toString();
        final ChallengeRequestDto dto = ChallengeRequestDto.builder()
                .acsTransID(acsTransactionId)
                .acsRenderingType(new ChallengeRequestAcsRenderingTypeDto(null,"02"))
                .deviceChannel(BROWSER)
                .build();

        Map<String, String> data = new HashMap<>();
        data.put(RequestParams.CREQ_PARAM, creq);
        dto.setContent(convertToQueryString(data));

        // when, then
        sendChallengeRequestAndWaitForString(
                HttpStatus.BAD_REQUEST,
                dto,
                acsTransactionId,
                "<div id=\"root\">",
                RequestParams.CREQ_PARAM.toUpperCase() + ": \"" + creq + "\"",
                AcsErrorCode.JSON_BODY_VALIDATION_EXCEPTION.name()
        );
    }

    @SneakyThrows
    @Test
    void testConvertResponseDtoToJson() {
        // given
        final var acsTransactionId = UUID.randomUUID().toString();

        final ChallengeResponseDto dto = ChallengeResponseDto.builder()
                .acsTransID(acsTransactionId)
                .templateName("html_brw05_otp.html")
                .templateLanguage("en")
                .content("<!DOCTYPE HTML><html><head><title>3DS - One-Time Passcode</title></head><body> some <form name=\"one_time_password_form\">....</form></body></html>")
                .transStatus(CHALLENGE_REQUIRED)
                .statusCode("200")
                .build();
        // when
        final String actualJson = toJson(dto);
        // then
        final DocumentContext ctx = JsonPath.parse(actualJson);
        assertThat(ctx.read("$.acsTransID").toString()).isEqualTo(acsTransactionId);
        assertThat(ctx.read("$.templateName").toString()).isEqualTo("html_brw05_otp.html");
        assertThat(ctx.read("$.templateLanguage").toString()).isEqualTo("en");
        assertThat(ctx.read("$.content").toString())
                .isEqualTo("<!DOCTYPE HTML><html><head><title>3DS - One-Time Passcode</title></head><body> some <form name=\"one_time_password_form\">....</form></body></html>");
        assertThat(ctx.read("$.transStatus").toString()).isEqualTo("C");
        assertThat(ctx.read("$.statusCode").toString()).isEqualTo("200");
    }

    protected ChallengeRequestDto successGetHtml(String acsTransactionId, String username, String userLanguage, List<AuthMethod> authMethodList) {
        // given
        final var dto = buildRequestDto(acsTransactionId, PAN);
        final var cardHolderPersonalCode = "010101-12345";
        final var cardCountry = "LT";

        mockSuccessfulCardCheckResponses(PAN, cardHolderPersonalCode, cardCountry);
        mockInitUserInformation(PAN, username, userLanguage, SESSION_TOKEN, authMethodList);
        mockLogout(acsTransactionId);

        Map<String, String> data = new HashMap<>();
        data.put(RequestParams.CREQ_PARAM, creq);
        dto.setContent(convertToQueryString(data));

        // when, then (1): Check card details + get React App on initial request
        sendChallengeRequestAndWaitForString(dto, acsTransactionId,
                "<div id=\"root\">",
                RequestParams.CREQ_PARAM.toUpperCase() + ": \"" + creq + "\""
        );

        resetRestTemplateServer();
        return dto;
    }

    @SneakyThrows
    protected void handleSendCurrentStatusToTietoAcs(String acsTransactionId, ChallengeRequestDto dto) {

        mockLogout(acsTransactionId);

        Map<String, String> data = new HashMap<>();
        data.put(RequestParams.UI_ACTION_PARAM, UiAction.BACK_TO_MERCHANT_SUCCESS.name());
        dto.setContent(convertToQueryString(data));

        mvc.perform(post(challengeApi).contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.acsTransID", is(acsTransactionId)))
                .andExpect(jsonPath("$.statusCode", is("200")))
                .andExpect(jsonPath("$.transStatus", is(VERIFICATION_SUCCESSFUL.getStatus())));
    }

    @SneakyThrows
    public void sendChallengeRequestAndWaitForString(ChallengeRequestDto dto, String acsTransactionId, String expectString) {
        mvc.perform(post(challengeApi).contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.acsTransID", is(acsTransactionId)))
                .andExpect(jsonPath("$.content", containsString(expectString)));
    }

    @SneakyThrows
    public void sendChallengeRequestAndWaitForString(ChallengeRequestDto dto, String acsTransactionId, String expectString1, String expectString2) {
        mvc.perform(post(challengeApi).contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.acsTransID", is(acsTransactionId)))
                .andExpect(jsonPath("$.content", containsString(expectString1)))
                .andExpect(jsonPath("$.content", containsString(expectString2)));
    }

    @SneakyThrows
    public void sendChallengeRequestAndWaitForString(HttpStatus status, ChallengeRequestDto dto, String acsTransactionId, String expectString1, String expectString2, String expectString3) {
        mvc.perform(post(challengeApi).contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(toJson(dto)))
                .andDo(print())
                .andExpect(status().is(status.value()))
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.acsTransID", is(acsTransactionId)))
                .andExpect(jsonPath("$.content", containsString(expectString1)))
                .andExpect(jsonPath("$.content", containsString(expectString2)))
                .andExpect(jsonPath("$.content", containsString(expectString3)));
    }

    @SneakyThrows
    public void sendChallengeRequestAndWaitForError(ChallengeRequestDto dto, String acsTransactionId, AcsErrorCode error) {
        mvc.perform(post(challengeApi).contentType(APPLICATION_JSON).content(toJson(dto)))
                .andDo(print())
                .andExpect(status().is(ErrorHandlingControllerAdvice.ERROR_HTTP_STATUS_CODE.value()))
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.acsTransID", is(acsTransactionId)))
                .andExpect(jsonPath("$.content",
                        containsString(ResponseParams.ERROR_CODE + "=" + error.name())))
                .andExpect(jsonPath("$.content",
                        containsString(ResponseParams.ERROR_TYPE + "=" + error.getErrorType().name())));
    }

    protected String toJson(final Object object) throws JsonProcessingException {
        return mapper.writeValueAsString(object);
    }

    protected <DTO> DTO toDto(final String json, final Class<DTO> dtoClass) throws JsonProcessingException {
        return mapper.readValue(json, dtoClass);
    }

    protected String loadFile(String resourceUrl) throws IOException {
        return Files.readString(getFile(resourceUrl).toPath());
    }

    @SneakyThrows
    protected String convertMapToJson(Map<String, String> data) {
        return mapper.writeValueAsString(data);
    }

    protected ChallengeRequestDto buildRequestDto(String acsTransactionId, String pan) {

        final var challengeRequestPurchaseDto = ChallengeRequestPurchaseDto.builder()
                .purchaseAmount("999")
                .purchaseCurrency("EUR")
                .build();

        return ChallengeRequestDto.builder()
                .acsTransID(acsTransactionId)
                .threeDSServerTransID("1")
                .acsRenderingType(new ChallengeRequestAcsRenderingTypeDto(null,"02"))
                .dsTransID("2")
                .acctNumber(pan)
                .url("https://ri1:8446")
                .method(POST)
                .deviceChannel(BROWSER)
                .purchaseData(challengeRequestPurchaseDto)
                .headers(ChallengeHeadersDto.builder().accept("text/html,application/xhtml+xml").build())
                .build();
    }

    protected void mockBaseLinkAppResponses(String pan, String cardHolderPersonalCode, String cardCountry) {
        final String cardInfoXml = "" +
                "<done>" +
                "    <card-info-acs>" +
                "        <person-code-card-holder>" + cardHolderPersonalCode + "</person-code-card-holder>" +
                "        <country>" + cardCountry + "</country>" +
                "    </card-info-acs>" +
                "</done>";
        when(linkAppWsMock.rtcungCall("<do what=\"card-info-acs\"><card>" + pan + "</card></do>")).thenReturn(cardInfoXml);

        final String cardStatusInCms = "" +
                "<done>" +
                "    <card-status-bo>" +
                "        <card>1234567890123456</card>" +
                "        <card-status-1>0</card-status-1>" +
                "        <card-status-2>0</card-status-2>" +
                "        <stoplist/>" +
                "    </card-status-bo>" +
                "</done>";
        when(linkAppWsMock.rtcungCall("<do what=\"card-status-bo\"><card>" + pan + "</card></do>")).thenReturn(cardStatusInCms);
    }

    protected void mockSuccessfulCardCheckResponses(String pan, String cardHolderPersonalCode, String cardCountry) {
        mockBaseLinkAppResponses(pan, cardHolderPersonalCode, cardCountry);
        final String cardStatusInRmsStopListFromRtps = "" +
                "<done>" +
                "    <card-status-rms>" +
                "        <card>1234567890123456</card>" +
                "    </card-status-rms>" +
                "</done>";
        when(linkAppWsMock.rtcungCall("<do what=\"card-status-rms\"><card>" + pan + "</card></do>")).thenReturn(cardStatusInRmsStopListFromRtps);
    }

    protected void mockLickUpBlockRms(String pan, String cardHolderPersonalCode, String cardCountry) {
        mockBaseLinkAppResponses(pan, cardHolderPersonalCode, cardCountry);
        final String cardStatusInRmsStopListFromRtps = "" +
                "<done>" +
                "    <card-status-rms>" +
                "        <card>1234567890123456</card>" +
                "        <entry>" +
                "            <centre-id>42800202350</centre-id>" +
                "            <card>4652281998644768</card>" +
                "            <effective-date>2006.04.26 16:50:35</effective-date>" +
                "            <update-date>2006.04.26 16:50:35</update-date>" +
                "            <purge-date/>" +
                "            <action-code>100</action-code>" +
                "            <description>RTPS.IIA.CALL(AddCardToRMSStop)</description>" +
                "            <rule-expression>FLD_041!%'PBANK1'</rule-expression>" +
                "            <priority>-1</priority>" +
                "        </entry>" +
                "    </card-status-rms>" +
                "</done>";
        when(linkAppWsMock.rtcungCall("<do what=\"card-status-rms\"><card>" + pan + "</card></do>")).thenReturn(cardStatusInRmsStopListFromRtps);
    }

}
