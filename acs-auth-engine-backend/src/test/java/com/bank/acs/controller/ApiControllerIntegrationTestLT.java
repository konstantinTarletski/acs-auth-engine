package com.bank.acs.controller;

import com.bank.acs.IntegrationTestBase;
import com.bank.acs.config.property.AppPropertiesLT;
import com.bank.acs.config.property.MaxAttemptsProperties;
import com.bank.acs.constant.RequestParams;
import com.bank.acs.constant.ResponseParams;
import com.bank.acs.dto.banktron.BanktronAuthenticateResponseDto;
import com.bank.acs.dto.banktron.BanktronCheckStatusResponseDto;
import com.bank.acs.dto.banktron.BanktronGetPersonsResponseDto;
import com.bank.acs.dto.banktron.BanktronGetPersonsResponseDto.BanktronPersonList;
import com.bank.acs.dto.banktron.BanktronLoginAuthenticationTypeDto;
import com.bank.acs.dto.banktron.BanktronLoginDto;
import com.bank.acs.dto.banktron.BanktronLoginDto.BanktronLoginAuthenticationTypeList;
import com.bank.acs.dto.banktron.BanktronPersonDto;
import com.bank.acs.dto.banktron.BanktronPersonDto.BanktronLoginList;
import com.bank.acs.dto.banktron.BanktronResponseDto;
import com.bank.acs.dto.challenge.ChallengeAuthDto;
import com.bank.acs.dto.challenge.ChallengeHeadersDto;
import com.bank.acs.dto.challenge.request.ChallengeRequestAcsRenderingTypeDto;
import com.bank.acs.dto.challenge.request.ChallengeRequestDto;
import com.bank.acs.dto.challenge.request.ChallengeRequestPurchaseDto;
import com.bank.acs.enumeration.AcsErrorCode;
import com.bank.acs.enumeration.AppState;
import com.bank.acs.enumeration.AuthMethod;
import com.bank.acs.enumeration.UiAction;
import com.bank.acs.enumeration.banktron.BanktronAuthMethod;
import com.bank.acs.enumeration.banktron.BanktronEndpoint;
import com.bank.acs.enumeration.banktron.BanktronErrorCode;
import com.bank.acs.enumeration.banktron.BanktronStatus;
import com.bank.acs.service.LinkAppMockConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.bank.acs.Profile.COUNTRY_LT_PROFILE;
import static com.bank.acs.Profile.INT_TEST_PROFILE;
import static com.bank.acs.enumeration.AcsAuthenticationMethod.SMS_OTP;
import static com.bank.acs.enumeration.AuthMethod.CODE_CALCULATOR;
import static com.bank.acs.enumeration.ChallengeMethod.POST;
import static com.bank.acs.enumeration.DeviceChannel.BROWSER;
import static com.bank.acs.enumeration.banktron.BanktronStatus.ACTIVE;
import static com.bank.acs.util.UrlQueryUtil.convertToQueryString;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles({INT_TEST_PROFILE, COUNTRY_LT_PROFILE})
@SpringBootTest
@AutoConfigureMockMvc
@Import(LinkAppMockConfig.class)
@Slf4j
class ApiControllerIntegrationTestLT extends IntegrationTestBase {

    private final boolean PRINT_BANKTRON_RESPONSES = false;

    @Autowired
    private MaxAttemptsProperties maxAttemptsProperties;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AppPropertiesLT appPropertiesLT;

    private MockRestServiceServer restTemplateServer;

    @BeforeEach
    public void setUp() {
        if (restTemplateServer == null) {
            restTemplateServer = MockRestServiceServer.createServer(restTemplate);
        }
        resetRestTemplateServer();
    }

    @SneakyThrows
    @Test
    void testConvertRequestJsonToDto() {
        // given
        final String requestJson = loadFile("classpath:request_example.json");

        // when
        final ChallengeRequestDto actual = toDto(requestJson, ChallengeRequestDto.class);

        // then
        assertThat(actual.getAcsAuthenticationMethod()).isEqualTo(SMS_OTP);
        assertThat(actual.getAcsTransID()).isEqualTo("01c62048-8d26-41cc-8987-7cfafb39480d");
        assertThat(actual.getChallengeWindowSize()).isEqualTo("05");
        assertThat(actual.getMethod()).isEqualTo(POST);
        assertThat(actual.getDeviceChannel()).isEqualTo(BROWSER);
        assertThat(actual.getUrl()).isEqualTo("https://ri1:8446");
        assertThat(actual.getContent())
                .isEqualTo("creq=eyJhY3NUcmFuc0lEIjoiMDFjNjIwNDgtOGQyNi00MWNjLTg5ODctN2NmYWZiMzk0ODBkIiwiY2hhbGxlbmdlV2luZG93U2l6ZSI6IjA1IiwibWVzc2FnZVR5cGUiOiJDUmVxIiwibWVzc2FnZVZlcnNpb24iOiIyLjEuMCIsInRocmVlRFNTZXJ2ZXJUcmFuc0lEIjoiNGI5ZTc5MDktMmJhMi00YTdkLTk5YWUtMGRjNDk3NGFmZjRiIn0&threeDSSessionData=0aPTs6OZ5V3AfISEV8h8szvWL2U=");
        assertThat(actual.getAcctNumber()).isEqualTo("411790******8063");
        assertThat(actual.getDsProtocol()).isEqualTo("visa");
        assertThat(actual.getDsTransID()).isEqualTo("2e7b83af-9a82-45f4-a09d-d1275401a0ad");
        assertThat(actual.getMessageVersion()).isEqualTo("2.1.0");
        assertThat(actual.getThreeDSServerTransID()).isEqualTo("4b9e7909-2ba2-4a7d-99ae-0dc4974aff4b");

        final ChallengeAuthDto authData = actual.getAuthenticationData();
        assertThat(authData.getPhone()).isEqualTo("+371 27789277");
        assertThat(authData.getMobile()).isEqualTo(singletonList("+371 27789277"));
        assertThat(authData.getLang()).isEqualTo("en");
        assertThat(authData.getAcsPanRange()).isEqualTo("reference");

        final ChallengeHeadersDto headers = actual.getHeaders();
        assertThat(headers.getSecFetchMode()).isEqualTo("navigate");
        assertThat(headers.getSecFetchSite()).isEqualTo("same-site");
        assertThat(headers.getSecFetchDest()).isEqualTo("document");
        assertThat(headers.getAcceptLanguage()).isEqualTo("en-GB,en;q=0.9,en-US;q=0.8,lv;q=0.7,pl;q=0.6");
        assertThat(headers.getAcceptEncoding()).isEqualTo("gzip, deflate, br");
        assertThat(headers.getUpgradeInsecureRequests()).isEqualTo("1");
        assertThat(headers.getContentLength()).isEqualTo("306");
        assertThat(headers.getContentType()).isEqualTo("application/x-www-form-urlencoded");
        assertThat(headers.getCacheControl()).isEqualTo("max-age=0");
        assertThat(headers.getUserAgent())
                .isEqualTo("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36");
        assertThat(headers.getReferer()).isEqualTo("https://ri1:9443/ecomm2/ThreeDs2Handler/Authentication");
        assertThat(headers.getCookie())
                .isEqualTo("SERVER3DS_15445_SESSION_ID=BF2E216B9E6A96076A98826B37372310; DS3DS_SESSION_ID=3DC2E56546EADECA7D442457E36B6C98; SERVER3DS_SESSION_ID=F2E3050FEDCBA541EC7B7F41904EF45B; DS3DS_24443_SESSION_ID=44BD5DD9CAF29522CC0A25890EDCD956; ACS3DS_SESSION_ID=A8B35206ED44B83DFA52085C5CA0C8A0");
        assertThat(headers.getOrigin()).isEqualTo("https://ri1:9443");
        assertThat(headers.getAccept())
                .isEqualTo("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        assertThat(headers.getHost()).isEqualTo("ri1:8446");
        assertThat(headers.getConnection()).isEqualTo("keep-alive");

        final ChallengeRequestPurchaseDto purchase = actual.getPurchaseData();
        assertThat(purchase.getPurchaseDate()).isEqualTo("202006****1821");
        assertThat(purchase.getPurchaseCurrency()).isEqualTo("EUR");
        assertThat(purchase.getPurchaseAmount()).isEqualTo("104.96");
        assertThat(purchase.getMerchantName()).isEqualTo("Ivo Upe");

        final ChallengeRequestAcsRenderingTypeDto acsRenderingType = actual.getAcsRenderingType();
        assertThat(acsRenderingType.getAcsUiTemplate()).isEqualTo("05");
        assertThat(acsRenderingType.getAcsInterface()).isEqualTo("02");
    }

    @SneakyThrows
    @Test
    void testGetEnterLoginPage() {
        // given
        final var pan = "1234567890123456";
        final var acsTransactionId = UUID.randomUUID().toString();
        final var dto = buildRequestDto(acsTransactionId, pan);
        final var cardHolderPersonalCode = "010101-12345";
        final var cardCountry = "LT";
        final var userLanguage = "RU";
        final var username1 = "someLogin1";
        final var username2 = "someLogin2";
        final var username3 = "someLogin3";
        final var sessionToken = "SESSION_TOKEN";

        mockSuccessfulCardCheckResponses(pan, cardHolderPersonalCode, cardCountry);

        final var login1 = getBanktronLoginDto(username1, List.of(BanktronAuthMethod.SMART_ID), ACTIVE);
        final var login2 = getBanktronLoginDto(username2, List.of(BanktronAuthMethod.TAN), ACTIVE);
        final var login3 = getBanktronLoginDto(username3, List.of(BanktronAuthMethod.SMART_ID), ACTIVE);
        final var person1 = getBanktronPersonDto(List.of(login1, login2), ACTIVE, userLanguage);
        final var person2 = getBanktronPersonDto(List.of(login3), ACTIVE, userLanguage);
        final var getPersonsResponseJson = getPersonsResponseJson(List.of(person1, person2), sessionToken);

        mockInitUserInformation(getPersonsResponseJson);

        Map<String, String> data = new HashMap<>();
        data.put(RequestParams.CREQ_PARAM, creq);
        dto.setContent(convertToQueryString(data));

        // when, then (1): Check card details + get React App on initial request
        sendChallengeRequestAndWaitForString(dto, acsTransactionId,
                "<div id=\"root\">",
                RequestParams.CREQ_PARAM.toUpperCase() + ": \"" + creq + "\""
        );
        resetRestTemplateServer();

        // when, then (2): Get user state request
        data = new HashMap<>();
        data.put(RequestParams.UI_ACTION_PARAM, UiAction.GET_INITIAL_INFORMATION.name());
        dto.setContent(convertToQueryString(data));

        sendChallengeRequestAndWaitForString(dto, acsTransactionId, "state=" + AppState.RENDER_ENTER_LOGIN_PAGE);
    }


    @SneakyThrows
    @Test
    //TODO use this test for LV
    void testReachMaxLoginAttempts() {
        // given
        final var pan = "1234567890123456";
        final var acsTransactionId = UUID.randomUUID().toString();
        final var dto = buildRequestDto(acsTransactionId, pan);
        final var sessionToken = "SESSION_TOKEN";
        final var cardHolderPersonalCode = "010101-12345";
        final var cardCountry = "LT";
        final var userLanguage = "RU";
        final var username1 = "someLogin1";
        final var username2 = "someLogin2";
        final var usernameWrong = "someLogin3";

        mockSuccessfulCardCheckResponses(pan, cardHolderPersonalCode, cardCountry);

        final var login1 = getBanktronLoginDto(username1, List.of(BanktronAuthMethod.SMART_ID), ACTIVE);
        final var login2 = getBanktronLoginDto(username2, List.of(BanktronAuthMethod.SMART_ID), ACTIVE);
        final var person = getBanktronPersonDto(List.of(login1, login2), ACTIVE, userLanguage);
        final var getPersonsResponseJson = getPersonsResponseJson(List.of(person), sessionToken);

        mockInitUserInformation(getPersonsResponseJson);
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

        // when, then (2): Get user state request
        data = new HashMap<>();
        data.put(RequestParams.UI_ACTION_PARAM, UiAction.GET_INITIAL_INFORMATION.name());
        dto.setContent(convertToQueryString(data));

        sendChallengeRequestAndWaitForString(dto, acsTransactionId, ResponseParams.STATE_PARAM + "=" + AppState.RENDER_ENTER_LOGIN_PAGE);

        data = new HashMap<>();
        data.put(RequestParams.UI_ACTION_PARAM, UiAction.CONFIRM_USER_LOGIN.name());
        data.put(RequestParams.ENTERED_LOGIN_PARAM, usernameWrong);
        dto.setContent(convertToQueryString(data));

        // when, then
        for (int i = 0; i < maxAttemptsProperties.getForEnterLogin() - 1; i++) {
            sendChallengeRequestAndWaitForError(dto, acsTransactionId, AcsErrorCode.NO_MATCHING_LOGIN_FOUND);
        }
        sendChallengeRequestAndWaitForError(dto, acsTransactionId, AcsErrorCode.EXCEEDED_MAX_ATTEMPTS_FOR_ENTERING_LOGIN);

    }

    @SneakyThrows
    @Test
    void testReturnBanktronError() {
        // given
        final var pan = "1234567890123456";
        final var acsTransactionId = UUID.randomUUID().toString();
        final var dto = buildRequestDto(acsTransactionId, pan);
        final var cardHolderPersonalCode = "010101-12345";
        final var cardCountry = "LT";

        mockSuccessfulCardCheckResponses(pan, cardHolderPersonalCode, cardCountry);

        final var body = convertMapToJson(Map.of(
                "error_code", String.valueOf(BanktronErrorCode.PERSON_CODE_NOT_SUPPLIED.getCode()),
                "error_message", BanktronErrorCode.PERSON_CODE_NOT_SUPPLIED.name()));

        restTemplateServer.expect(ExpectedCount.once(),
                requestTo(appPropertiesLT.getSonicUrl() + BanktronEndpoint.PERSONLOGINS.getLink()))
                .andRespond(withServerError().body(body).contentType(MediaType.APPLICATION_JSON));

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
                AcsErrorCode.NO_LOGINS_AVAILABLE.name()
        );
        resetRestTemplateServer();
    }

    public BanktronLoginDto getBanktronLoginDto(String username, List<BanktronAuthMethod> authMethods, BanktronStatus status) {
        final var authMethodsList = authMethods.stream()
                .map(authMethod -> BanktronLoginAuthenticationTypeDto.builder().authMethod(authMethod).build())
                .collect(toList());
        return BanktronLoginDto.builder()
                .username(username)
                .status(status)
                .authenticationTypesList(BanktronLoginAuthenticationTypeList.builder().authenticationType(authMethodsList).build())
                .build();
    }

    public BanktronPersonDto getBanktronPersonDto(List<BanktronLoginDto> logins, BanktronStatus status, String language) {
        return BanktronPersonDto.builder()
                .language(language)
                .loginList(BanktronLoginList.builder().login(logins).build())
                .status(status).build();
    }

    @SneakyThrows
    public String getPersonsResponseJson(List<BanktronPersonDto> persons, String sessionToken) {
        final var getPersonsResponse = BanktronGetPersonsResponseDto.builder()
                .sessionToken(sessionToken)
                .personList(BanktronPersonList.builder().person(persons).build())
                .build();
        return mapper.writeValueAsString(getPersonsResponse);
    }

    @SneakyThrows
    @Override
    public void mockLogout(String banktronSessionToken) {
        final var banktronAuthResponseJson = mapper.writeValueAsString(new BanktronResponseDto());
        if (PRINT_BANKTRON_RESPONSES) {
            log.info("url = {}", appPropertiesLT.getSonicUrl() + BanktronEndpoint.LOGOUT.getLink());
            log.info("response = {}", banktronAuthResponseJson);
        }
        restTemplateServer.expect(ExpectedCount.once(),
                requestTo(appPropertiesLT.getSonicUrl() + BanktronEndpoint.LOGOUT.getLink()))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(withSuccess(banktronAuthResponseJson, MediaType.APPLICATION_JSON));
    }

    public void mockInitUserInformation(String personsResponseJson) {
        if (PRINT_BANKTRON_RESPONSES) {
            log.info("url = {}", appPropertiesLT.getSonicUrl() + BanktronEndpoint.PERSONLOGINS.getLink());
            log.info("response = {}", personsResponseJson);
        }
        restTemplateServer.expect(ExpectedCount.once(),
                requestTo(appPropertiesLT.getSonicUrl() + BanktronEndpoint.PERSONLOGINS.getLink()))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(withSuccess(personsResponseJson, MediaType.APPLICATION_JSON));
    }


    @SneakyThrows
    @Override
    public void mockInitUserInformation(String pan, String username, String language, String sessionToken, List<AuthMethod> authMethods) {
        final var login = getBanktronLoginDto(username, authMethods.stream().map(BanktronAuthMethod::toBanktronAuthMethod).collect(toList()), ACTIVE);
        final var person = getBanktronPersonDto(List.of(login), ACTIVE, language);
        final var getPersonsResponseJson = getPersonsResponseJson(List.of(person), sessionToken);

        mockInitUserInformation(getPersonsResponseJson);
    }

    @SneakyThrows
    @Override
    public void mockSmartIdAuthInit(String acsTransactionId, String username, String authToken, String amount) {
        mockAuthInit(acsTransactionId, username, authToken, amount);
    }

    @SneakyThrows
    @Override
    public void mockCodeCalculatorAuthInit(String acsTransactionId, String username, String authToken, String amount, String confirmationCode) {
        mockAuthInit(acsTransactionId, username, authToken, amount);
    }

    @SneakyThrows
    @Override
    public void mockSmartIdCheckAuthMethodStatus(String acsTransactionId, String username, String amount) {
        final var banktronAuthResponse = new BanktronCheckStatusResponseDto();
        banktronAuthResponse.setSignedString("ANY");
        banktronAuthResponse.setSigned(true);
        final var banktronAuthResponseJson = mapper.writeValueAsString(banktronAuthResponse);

        if (PRINT_BANKTRON_RESPONSES) {
            log.info("url = {}", appPropertiesLT.getSonicUrl() + BanktronEndpoint.CHECKSTATUS.getLink());
            log.info("response = {}", banktronAuthResponseJson);
        }

        restTemplateServer.expect(ExpectedCount.once(),
                requestTo(appPropertiesLT.getSonicUrl() + BanktronEndpoint.CHECKSTATUS.getLink()))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(withSuccess(banktronAuthResponseJson, MediaType.APPLICATION_JSON));

        mockConfirmCredentials(acsTransactionId, username, "any", amount, "any");
    }

    @SneakyThrows
    @Override
    public void mockCodeCalculatorSuccess(String acsTransactionId, String username, String authToken, String amount, String confirmationCode) {
        mockConfirmCredentials(acsTransactionId, username, "any", amount, "any");
    }


    @SneakyThrows
    @Override
    public void mockCodeCalculatorWrongCode(String acsTransactionId, String username, String authToken, String amount, String confirmationCode) {
        final var banktronError = BanktronResponseDto.builder()
                .errorCode(BanktronErrorCode.INCORRECT_CODE.getCode().toString())
                .errorMessage("Incorrect login credentials. Please try again.")
                .build();

        final var confirmCredentialsResponseJson = mapper.writeValueAsString(banktronError);

        if (PRINT_BANKTRON_RESPONSES) {
            log.info("url = {}", appPropertiesLT.getSonicUrl() + BanktronEndpoint.CONFIRMCREDENTIALS.getLink());
            log.info("response = {}", confirmCredentialsResponseJson);
        }

        restTemplateServer.expect(ExpectedCount.once(),
                requestTo(appPropertiesLT.getSonicUrl() + BanktronEndpoint.CONFIRMCREDENTIALS.getLink()))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(withSuccess(confirmCredentialsResponseJson, MediaType.APPLICATION_JSON));

        mockLogout("any");
        //TODO put not hardcoded parameters
        mockInitUserInformation(PAN, username, "ru", SESSION_TOKEN, List.of(CODE_CALCULATOR));
    }

    @Override
    public AcsErrorCode getCodeCalculatorWrongCodeError() {
        return AcsErrorCode.WRONG_CODE_FOR_CODE_CALCULATOR;
    }

    @SneakyThrows
    public void mockConfirmCredentials(String acsTransactionId, String username, String authToken, String amount, String confirmationCode) {
        final var confirmCredentialsResponseJson = mapper.writeValueAsString(new BanktronResponseDto());

        if (PRINT_BANKTRON_RESPONSES) {
            log.info("url = {}", appPropertiesLT.getSonicUrl() + BanktronEndpoint.CONFIRMCREDENTIALS.getLink());
            log.info("response = {}", confirmCredentialsResponseJson);
        }

        restTemplateServer.expect(ExpectedCount.once(),
                requestTo(appPropertiesLT.getSonicUrl() + BanktronEndpoint.CONFIRMCREDENTIALS.getLink()))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(withSuccess(confirmCredentialsResponseJson, MediaType.APPLICATION_JSON));
    }

    @SneakyThrows
    public void mockAuthInit(String acsTransactionId, String username, String authToken, String amount) {
        final var banktronAuthResponse = new BanktronAuthenticateResponseDto();
        banktronAuthResponse.setAuthenticationToken(authToken);
        final var banktronAuthResponseJson = mapper.writeValueAsString(banktronAuthResponse);

        if (PRINT_BANKTRON_RESPONSES) {
            log.info("url = {}", appPropertiesLT.getSonicUrl() + BanktronEndpoint.AUTHENTICATE.getLink());
            log.info("response = {}", banktronAuthResponseJson);
        }

        restTemplateServer.expect(ExpectedCount.once(),
                requestTo(appPropertiesLT.getSonicUrl() + BanktronEndpoint.AUTHENTICATE.getLink()))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(withSuccess(banktronAuthResponseJson, MediaType.APPLICATION_JSON));
    }


    @Override
    public void resetRestTemplateServer() {
        restTemplateServer.reset();
    }

    @SneakyThrows
    @Override
    public void testSmartIDResponse(ChallengeRequestDto dto) {
        mvc.perform(post(challengeApi).contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.content",
                        containsString(ResponseParams.CONFIRMATION_SUCCESSFUL_PARAM + "=" + Boolean.TRUE.toString())));
    }

}
