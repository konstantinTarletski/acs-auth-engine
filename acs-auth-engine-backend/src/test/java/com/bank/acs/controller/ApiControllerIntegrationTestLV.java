package com.bank.acs.controller;

import com.bank.acs.IntegrationTestBase;
import com.bank.acs.constant.ResponseParams;
import com.bank.acs.dto.challenge.request.ChallengeRequestDto;
import com.bank.acs.enumeration.AcsErrorCode;
import com.bank.acs.enumeration.AuthMethod;
import com.bank.acs.enumeration.roofid.RoofIdAuthMethod;
import com.bank.acs.service.LinkAppMockConfig;
import com.bank.acs.service.lv.AuthMethodServiceLV;
import lombok.SneakyThrows;
import lv.ays.rid.ESmartIdStatus;
import lv.ays.rid.RidClientDTO;
import lv.ays.rid.RidClientParamDTO;
import lv.ays.rid.RidSmartIdInitDTO;
import lv.ays.rid.RidSmartIdResponseDTO;
import lv.ays.rid.SimpleInterfaceRemote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.bank.acs.Profile.COUNTRY_LV_PROFILE;
import static com.bank.acs.Profile.INT_TEST_PROFILE;
import static com.bank.acs.service.lv.RoofIdRequestKeys.CARD;
import static com.bank.acs.service.lv.RoofIdRequestKeys.JF;
import static com.bank.acs.service.lv.RoofIdRequestKeys.OPTIONS;
import static com.bank.acs.service.lv.RoofIdRequestKeys.P;
import static com.bank.acs.service.lv.RoofIdRequestKeys.PERCENT;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles({INT_TEST_PROFILE, COUNTRY_LV_PROFILE})
@SpringBootTest
@AutoConfigureMockMvc
@Import(LinkAppMockConfig.class)
public class ApiControllerIntegrationTestLV extends IntegrationTestBase {

    public static final RoofIdAuthMethod ROOF_ID_CODE_CALCULATOR_TYPE = RoofIdAuthMethod.N; //Any type of code calculator
    public static final String ROOF_ID_CODE_TABLE_NAME = "Any";
    public static final String ROOF_ID_CODE_CALCULATOR_HASH = "something";

    @Autowired
    private SimpleInterfaceRemote simpleInterfaceRemoteMock;

    private String getMessageToSign(String acsTransactionId, String amount, String ridClientId) {
        return String.format("acsTransactionId=%s;amount=%s;userId=%s",
                acsTransactionId,
                amount,
                ridClientId);
    }

    @Override
    public void mockSmartIdAuthInit(String acsTransactionId, String username, String authToken, String amount) {
        var ridSmartIdInitDTO = new RidSmartIdInitDTO();
        ridSmartIdInitDTO.setStatus(authToken);
        ridSmartIdInitDTO.setHash(ROOF_ID_CODE_CALCULATOR_HASH);

        when(simpleInterfaceRemoteMock.smartIdAuthInit(username, getMessageToSign(acsTransactionId, amount, username)))
                .thenReturn(ridSmartIdInitDTO);
    }

    @Override
    public void mockCodeCalculatorAuthInit(String acsTransactionId, String username, String authToken, String amount, String confirmationCode) {
        //Nothing to do
    }

    @Override
    public void mockCodeCalculatorSuccess(String acsTransactionId, String username, String authToken, String amount, String confirmationCode) {
        when(simpleInterfaceRemoteMock.codetableCheck(ROOF_ID_CODE_TABLE_NAME, ROOF_ID_CODE_CALCULATOR_TYPE.name(), "0", confirmationCode, true))
                .thenReturn(true);
    }

    @Override
    public void mockCodeCalculatorWrongCode(String acsTransactionId, String username, String authToken, String amount, String confirmationCode) {
        when(simpleInterfaceRemoteMock.codetableCheck(ROOF_ID_CODE_TABLE_NAME, ROOF_ID_CODE_CALCULATOR_TYPE.name(), "0", confirmationCode, true))
                .thenReturn(false);
    }

    @Override
    public AcsErrorCode getCodeCalculatorWrongCodeError() {
        return AcsErrorCode.WRONG_CODE_FOR_CODE_CALCULATOR;
    }

    @Override
    public void mockSmartIdCheckAuthMethodStatus(String acsTransactionId, String username, String amount) {
        final var response = new RidSmartIdResponseDTO();
        response.setStatus(ESmartIdStatus.OK);
        when(simpleInterfaceRemoteMock.smartIdAuthCheck(username,
                ROOF_ID_CODE_CALCULATOR_HASH,
                String.format(AuthMethodServiceLV.MESSAGE_TO_USER_DEFAULT, amount)
                )
        ).thenReturn(response);
    }

    @Override
    public void mockInitUserInformation(String pan, String username, String language, String sessionToken, List<AuthMethod> authMethods) {
        var ridDto = new RidClientParamDTO();
        ridDto.setId(1L);
        ridDto.setName("VISA(EUR)...1234");
        ridDto.setRidClient(username);
        ridDto.setType("card");
        ridDto.setValue("card=" + pan + "%expiry=2304%readonly=n");

        when(simpleInterfaceRemoteMock.findRidClientParams(null, null, CARD, PERCENT + pan + PERCENT))
                .thenReturn(List.of(ridDto));

        var ridClientDTO = new RidClientDTO();
        ridClientDTO.setStatus("a");
        ridClientDTO.setClientId(username);
        ridClientDTO.setSmartId(authMethods.contains(AuthMethod.SMART_ID));
        if (authMethods.contains(AuthMethod.CODE_CALCULATOR)) {
            ridClientDTO.setDefaultCodetableId(1L);
            ridClientDTO.setDefaultCodetableName(ROOF_ID_CODE_TABLE_NAME);
            ridClientDTO.setDefaultCodetableType(ROOF_ID_CODE_CALCULATOR_TYPE.name());
        }
        ridClientDTO.setLanguage(language);
        ridClientDTO.setLastAuthMethod(RoofIdAuthMethod.toRoofIdAuthMethod(AuthMethod.SMART_ID).name());

        when(simpleInterfaceRemoteMock.getRidClientRemote(username, true))
                .thenReturn(ridClientDTO);

        when(simpleInterfaceRemoteMock.findRidClientParams(username, JF, OPTIONS, P))
                .thenReturn(List.of());
    }

    @Override
    public void resetRestTemplateServer() {
        //Nothing to do
    }

    @Override
    public void mockLogout(String sessionToken) {
        //Nothing to do
    }

    @SneakyThrows
    @Override
    public void testSmartIDResponse(ChallengeRequestDto dto) {
        mvc.perform(post(challengeApi).contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.content",
                        containsString(ResponseParams.AUTH_METHOD_PARAM + "=" + AuthMethod.SMART_ID.name())));

        mvc.perform(post(challengeApi).contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.content",
                        containsString( ResponseParams.AUTH_METHOD_PARAM + "=" + AuthMethod.SMART_ID.name() + "&" + ResponseParams.CONFIRMATION_SUCCESSFUL_PARAM + "=" + Boolean.TRUE.toString())));
    }

}
