package com.bank.acs.controller;

import com.bank.acs.enumeration.AuthMethod;
import com.bank.acs.enumeration.ciam.CiamAuthMethod;
import com.bank.acs.service.RenderHTMLService;
import com.bank.acs.service.ciam.CiamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.Set;

import static com.bank.acs.Profile.COUNTRY_EE_PROFILE;
import static com.bank.acs.Profile.COUNTRY_LT_PROFILE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/test/ciam", produces = APPLICATION_JSON_VALUE)
//@Profile(COUNTRY_LV_PROFILE)
@Profile(COUNTRY_EE_PROFILE)
//@Profile(COUNTRY_LT_PROFILE)
public class TestController {

    //protected final CccService cccService;
    protected final CiamService ciamService;
    protected final ObjectMapper objectMapper;
    //protected final SimpleInterfaceRemote simpleInterfaceRemote;
    protected final RenderHTMLService renderHTMLService;


    @SneakyThrows
    @GetMapping
    public String testSomething123(@RequestParam(required = false) String regCode) {
        log.info("testSomething, regCode = ", regCode);
        //final var params = simpleInterfaceRemote.findRidClientParams(null, null, CARD, PERCENT + regCode + PERCENT);
        //final var clientDtos = getRoofIdValidClients(params, null);

        var ret =ciamService.authenticate(
                CiamAuthMethod.PINCALC,
                null,
                "en",
                "4327706",
                "EE",
                regCode,
                true
        );
        return ret.toString();
        //return renderHTMLService.getChooseAuthMethodPage("en", Set.of(AuthMethod.CODE_CALCULATOR), Optional.empty());
    }

/*    protected List<RidClientDTO> getRoofIdValidClients(List<RidClientParamDTO> ridClientList, AppSession session) {

        final var validClients = new ArrayList<RidClientDTO>();

        for (RidClientParamDTO param : ridClientList) {

            final var thisCard = new RidCardDTO(param);


            final var thisClient = simpleInterfaceRemote.getRidClientRemote(param.getRidClient(), true );


                    //User is blocked
                    //TODO need to check, in "old" version "a" - means blocked (BDBCDVIEU-626)
            if (!thisClient.getStatus().equalsIgnoreCase(USER_ACTIVE_KEY)) {

                continue;
            }

            //"Can't use this card, because it is readonly"
            List<RidClientParamDTO> jf = simpleInterfaceRemote.findRidClientParams(param.getRidClient(), JF, OPTIONS, P);


            if ((thisCard.isReadonly()) && (jf != null) && (!jf.isEmpty())) {

                continue;
            }
            validClients.add(thisClient);
        }

        return validClients;
    }*/


/*
    @SneakyThrows
    @GetMapping

    public String testSomething11(@RequestParam String regCode) {
        log.info("testSomething");

        //regCode = "48611183590";
        //regCode = "37405110459";
        //regCode = "39111114830";

        final var user = ccc(regCode);
        final var username = user.getUsername();
        //  "4334020";

        log.info("username = " + username + "; ");

        final var auth1 = ciamService.authenticate(CiamAuthMethod.SMARTID, null, "et", username, "ee", null, true);
        log.info("1 getErrorMessage = {}", getErrorMessage(auth1));
        log.info("1 getVerificationCode = {}", getVerificationCode(auth1));
        log.info("1 getWaitTimeStr = {}", getWaitTimeStr(auth1));
        log.info("1 getTokenId = {}", auth1.getTokenId());


        final var authenticateResponse = objectMapper.writeValueAsString(auth1);
        CIAMAuthenticationResponseDO body = objectMapper.readValue(authenticateResponse, CIAMAuthenticationResponseDO.class);

        Thread.sleep(5000);

        log.info("map = {}", body);

        final var auth2 = ciamService.authenticate(CiamAuthMethod.SMARTID, body, "et", username, "ee", null, false);
        log.info("2 getErrorMessage = {}", getErrorMessage(auth2));
        log.info("2 getVerificationCode = {}", getVerificationCode(auth2));
        log.info("2 getWaitTimeStr = {}", getWaitTimeStr(auth2));
        log.info("2 getTokenId = {}", auth1.getTokenId()); // when signed

        ciamLogout(username);

        return username;
    }


    public CCCUsernameAndLanguagesDO ccc(String regCode) {
        final var token = ciamService.obtainSTSToken();

        return cccService.getUserNameAndLanguage(
                token.getAccessToken(),
                //"48611183590",
                regCode,//"37405110459",
                "EE");
    }

    public void ciamLogout(String username) {
        ciamService.logoutFromCiam("ee", username);
    }

    protected String getVerificationCode(CIAMAuthenticationResponseDO response) {
        return getCallbackOutput(response.getCallbacks(), CALLBACK_PIN, OUTPUT_PIN);
    }

    protected String getWaitTimeStr(CIAMAuthenticationResponseDO response) {
        return getCallbackOutput(response.getCallbacks(), CALLBACK_POLLING_WAIT_TIME, OUTPUT_POLLING_WAIT_TIME);
    }

    protected String getErrorMessage(CIAMAuthenticationResponseDO response) {
        return getCallbackOutput(response.getCallbacks(), CALLBACK_MESSAGE, OUTPUT_MESSAGE);
    }

    protected String getCallbackOutput(List<CIAMAuthenticationCallbackDO> callbacks, String type, String outputName) {
        if (callbacks == null) {
            return null;
        }
        Optional<CIAMAuthenticationCallbackDO> callback = callbacks.stream().filter(cb -> type.equals(cb.getType())).findFirst();
        if (!callback.isPresent()) {
            return null;
        }
        Optional<CIAMAuthenticationInputOutputDO> output = callback.get().getOutput().stream()
                .filter(o -> outputName.equals(o.getName())).findFirst();
        return output.isPresent() ? output.get().getValue().toString() : null;
    }
*/


}
