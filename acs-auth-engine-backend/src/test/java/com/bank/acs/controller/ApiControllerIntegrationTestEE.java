package com.bank.acs.controller;

import com.bank.acs.IntegrationTestBase;
import com.bank.acs.config.property.AppPropertiesEE;
import com.bank.acs.constant.ResponseParams;
import com.bank.acs.dto.ccc.CCCLanguageDO;
import com.bank.acs.dto.ccc.CCCUsernameAndLanguagesDO;
import com.bank.acs.dto.challenge.request.ChallengeRequestDto;
import com.bank.acs.dto.ciam.CIAMAccessTokenResponseDO;
import com.bank.acs.dto.ciam.CIAMAuthenticationCallbackDO;
import com.bank.acs.dto.ciam.CIAMAuthenticationInputOutputDO;
import com.bank.acs.dto.ciam.CIAMAuthenticationResponseDO;
import com.bank.acs.enumeration.AcsErrorCode;
import com.bank.acs.enumeration.AuthMethod;
import com.bank.acs.service.LinkAppMockConfig;
import com.bank.acs.service.ccc.CccConstants;
import com.bank.acs.service.ciam.CiamConstants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static com.bank.acs.Profile.COUNTRY_EE_PROFILE;
import static com.bank.acs.Profile.INT_TEST_PROFILE;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles({INT_TEST_PROFILE, COUNTRY_EE_PROFILE})
@SpringBootTest
@AutoConfigureMockMvc
@Import(LinkAppMockConfig.class)
@Slf4j
public class ApiControllerIntegrationTestEE extends IntegrationTestBase {

    @Autowired
    private AppPropertiesEE appPropertiesEE;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer restTemplateServer;

    public static final String ACCESS_TOKEN = "ANY_ACCESS_TOKEN";
    public static final String USERNAME = "USERNAME";
    public static final String AUTHENTICATE_TOKEN_ID = "AUTHENTICATE_TOKEN_ID";

    @BeforeEach
    public void setUp() {
        if (restTemplateServer == null) {
            restTemplateServer = MockRestServiceServer.createServer(restTemplate);
        }
        resetRestTemplateServer();
    }


    @SneakyThrows
    @Override
    public void mockInitUserInformation(String pan, String username, String language, String sessionToken, List<AuthMethod> authMethods) {
        final var ciamResponse = CIAMAccessTokenResponseDO.builder()
                .accessToken(ACCESS_TOKEN)
                .build();

        final var ciamResponseJson = mapper.writeValueAsString(ciamResponse);

        restTemplateServer.expect(ExpectedCount.once(),
                requestTo(matchesPattern(appPropertiesEE.getCiamAccessTokenUrl() + ".*")))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(withSuccess(ciamResponseJson, MediaType.APPLICATION_JSON));

        final var languageItem = CCCLanguageDO.builder()
                .settingCode(CccConstants.DESKTOP_LANGUAGE)
                .settingPayload(language)
                .build();

        final var cccResponse = CCCUsernameAndLanguagesDO.builder()
                .languages(List.of(languageItem))
                .username(USERNAME)
                .build();

        final var cccResponseJson = mapper.writeValueAsString(cccResponse);

        restTemplateServer.expect(ExpectedCount.once(),
                requestTo(appPropertiesEE.getCccUrl()))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(withSuccess(cccResponseJson, MediaType.APPLICATION_JSON));

    }

    @SneakyThrows
    @Override
    public void mockSmartIdAuthInit(String acsTransactionId, String username, String authToken, String amount) {

        //Can not be MOCKED thru "restTemplateServer"
        //And seams no need -- it just returns key for encryption without any input parameters
/*        restTemplateServer.expect(ExpectedCount.once(),
                requestTo(matchesPattern(appPropertiesEE.getCiamJwkUriUrl() + ".*")))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(withSuccess(getJwkResponse(), MediaType.APPLICATION_JSON));

        restTemplateServer.reset();*/

        final var authenticateResponseJson = mapper.writeValueAsString(getCIAInitSmartIdResponse());

        restTemplateServer.expect(ExpectedCount.once(),
                requestTo(matchesPattern(appPropertiesEE.getCiamAuthenticateUrl() + ".*")))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(withSuccess(authenticateResponseJson, MediaType.APPLICATION_JSON));
    }

    @SneakyThrows
    @Override
    public void mockCodeCalculatorAuthInit(String acsTransactionId, String username, String authToken, String amount, String confirmationCode) {
        //Nothing to do
    }

    @SneakyThrows
    @Override
    public void mockCodeCalculatorSuccess(String acsTransactionId, String username, String authToken, String amount, String confirmationCode) {

        //Can not be MOCKED thru "restTemplateServer"
        //And seams no need -- it just returns key for encryption without any input parameters
/*        restTemplateServer.expect(ExpectedCount.once(),
                requestTo(appPropertiesEE.getCiamJwkUriUrl() + "?realm=acs"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(withSuccess(getJwkResponse(), MediaType.APPLICATION_JSON));*/

        //restTemplateServer.reset();

        final var authenticateResponse = CIAMAuthenticationResponseDO.builder()
                .tokenId(AUTHENTICATE_TOKEN_ID)
                .build();

        final var authenticateResponseJson = mapper.writeValueAsString(authenticateResponse);

        restTemplateServer.expect(ExpectedCount.once(),
                requestTo(matchesPattern(appPropertiesEE.getCiamAuthenticateUrl() + ".*")))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(withSuccess(authenticateResponseJson, MediaType.APPLICATION_JSON));
    }

    @SneakyThrows
    @Override
    public void mockCodeCalculatorWrongCode(String acsTransactionId, String username, String authToken, String amount, String confirmationCode) {

        //Can not be MOCKED thru "restTemplateServer"
        //And seams no need -- it just returns key for encryption without any input parameters
/*        restTemplateServer.expect(ExpectedCount.once(),
                requestTo(appPropertiesEE.getCiamJwkUriUrl() + "?realm=acs"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(withSuccess(getJwkResponse(), MediaType.APPLICATION_JSON));*/

        //restTemplateServer.reset();

        final var authenticateResponseJson = mapper.writeValueAsString(getCIAMErrorResponse());

        restTemplateServer.expect(ExpectedCount.once(),
                requestTo(matchesPattern(appPropertiesEE.getCiamAuthenticateUrl() + ".*")))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(withSuccess(authenticateResponseJson, MediaType.APPLICATION_JSON));
    }

    @Override
    public AcsErrorCode getCodeCalculatorWrongCodeError() {
        return AcsErrorCode.TRANSLATED_ERROR;
    }

    @SneakyThrows
    @Override
    public void mockSmartIdCheckAuthMethodStatus(String acsTransactionId, String username, String amount) {
        final var authenticateResponse = CIAMAuthenticationResponseDO.builder()
                .tokenId(AUTHENTICATE_TOKEN_ID)
                .build();

        final var authenticateResponseJson = mapper.writeValueAsString(authenticateResponse);

        restTemplateServer.expect(ExpectedCount.once(),
                requestTo(matchesPattern(appPropertiesEE.getCiamAuthenticateUrl() + ".*")))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(withSuccess(authenticateResponseJson, MediaType.APPLICATION_JSON));
    }

    @Override
    public void resetRestTemplateServer() {
        restTemplateServer.reset();
    }

    @SneakyThrows
    @Override
    public void mockLogout(String acsTransactionId) {

        final var ciamResponse = CIAMAccessTokenResponseDO.builder()
                .accessToken(ACCESS_TOKEN)
                .build();

        final var ciamResponseJson = mapper.writeValueAsString(ciamResponse);

        restTemplateServer.expect(ExpectedCount.once(),
                requestTo(matchesPattern(appPropertiesEE.getCiamAccessTokenUrl() + ".*")))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(withSuccess(ciamResponseJson, MediaType.APPLICATION_JSON));


        restTemplateServer.expect(ExpectedCount.once(),
                requestTo(matchesPattern(appPropertiesEE.getCiamLogoutUrl() + ".*")))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));
    }

    protected CIAMAuthenticationResponseDO getCIAMResponse(String name, String type, String value) {
        final var output = CIAMAuthenticationInputOutputDO.builder()
                .name(name)
                .value(value)
                .build();

        final var callback = CIAMAuthenticationCallbackDO.builder()
                .type(type)
                .output(List.of(output))
                .build();

        return CIAMAuthenticationResponseDO.builder()
                .callbacks(List.of(callback))
                .build();
    }

    protected CIAMAuthenticationResponseDO getCIAInitSmartIdResponse() {
        return getCIAMResponse(CiamConstants.OUTPUT_PIN, CiamConstants.CALLBACK_PIN, "1234");
    }

    protected CIAMAuthenticationResponseDO getCIAMErrorResponse() {
        return getCIAMResponse(CiamConstants.OUTPUT_MESSAGE, CiamConstants.CALLBACK_MESSAGE, "Error message");
    }

    //protected String getJwkResponse(){
    //    return "{\"keys\":[{\"kty\":\"RSA\",\"kid\":\"c71b6e3e86903af36dccf1a3879862d07a146257fa33affbc47946773b43afa8\",\"use\":\"sig\",\"x5t\":\"dXjaKmSVCaQg1tdjr5_gF1vNseE\",\"x5c\":[\"MIIDpDCCAoygAwIBAgIEXilgZTANBgkqhkiG9w0BAQsFADCBhTELMAkGA1UEBhMCRUUxDjAMBgNVBAgMBUhhcmp1MRAwDgYDVQQHDAdUYWxsaW5uMRkwFwYDVQQKDBBMdW1pbm9yIEdyb3VwIEFCMRgwFgYDVQQLDA9SZW1vdGUgSWRlbnRpdHkxHzAdBgNVBAMMFkx1bWlub3IgRGV2ZWxvcG1lbnQgQ0EwHhcNMjAwMTIzMDg1OTE3WhcNMzAwMTIzMDg1OTE3WjB/MQswCQYDVQQGEwJFRTEOMAwGA1UECBMFSGFyanUxEDAOBgNVBAcTB1RhbGxpbm4xGTAXBgNVBAoTEEx1bWlub3IgR3JvdXAgQUIxGDAWBgNVBAsTD1JlbW90ZSBJZGVudGl0eTEZMBcGA1UEAxMQcnNhand0c2lnbmluZ2tleTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAPdIcjwkfyHGcpr3LRceQvd2Auy0YVqIvM0lmqpjA8fE8vorYQ4we+cRqT5bE8skZvy4lLtRUpl/Xe4R6CWmvGrYvO/WMeNUOvr/lNacsf2wLpPyuYmxZAZGODyXh7aUwjRJY093i/DeiHDR5kiylYKF7tCSutLpTtw4MiCizNFZFi2uF5Enhenzb9z13jP/DdpAbhLcEHnVWhopmurGmifmSuW0LwWzZHgQdB9mBOKIW6/1EQ1XRvD0MuHioC/Qcno5dmDBmG3jAhTCEEh82IkYvaqMfojBgeNt8zi6DVQLucEzY+SFY7DknVrJ/gmjuDUouSnsv03UQQYp1SJYnS0CAwEAAaMhMB8wHQYDVR0OBBYEFOmC4UyqeVM8INQpNFOAIWQ0BRTlMA0GCSqGSIb3DQEBCwUAA4IBAQAlCK0pyc8uBYeQVNmZ3gBeO2Y2m/t8Q9bkQvbJXdZxisFKd9nUKsU7JOFNMi4imfRimm4UQxSHHkBu4KpPBnKNLPdnt1OJ/LixX4pCCZ0JL7nc45wyWrx4YMm4oSBFL1D6DDCPHKCWZOXBuA39j9viBqNzquhQPzNDwBDr3R1izpKnYvjeZPTid7lE45U7Sqz+LLtG/IvNOtMbMRd8MhXMbWtfS3LNiFtkxCsCY35Hf7UEptd5wd4oNNM44iiLlKNrqcMeShaQzSDKZIdNvDrGNl7FJmBuXYgjKRVY73loT7KoF9v7tvqJnPZHiqy6lTvH0gvH0z0OANO0XrfAfBCn\"],\"n\":\"90hyPCR_IcZymvctFx5C93YC7LRhWoi8zSWaqmMDx8Ty-ithDjB75xGpPlsTyyRm_LiUu1FSmX9d7hHoJaa8ati879Yx41Q6-v-U1pyx_bAuk_K5ibFkBkY4PJeHtpTCNEljT3eL8N6IcNHmSLKVgoXu0JK60ulO3DgyIKLM0VkWLa4XkSeF6fNv3PXeM_8N2kBuEtwQedVaGima6saaJ-ZK5bQvBbNkeBB0H2YE4ohbr_URDVdG8PQy4eKgL9Byejl2YMGYbeMCFMIQSHzYiRi9qox-iMGB423zOLoNVAu5wTNj5IVjsOSdWsn-CaO4NSi5Key_TdRBBinVIlidLQ\",\"e\":\"AQAB\",\"alg\":\"RS256\"}]}";
    //}

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
