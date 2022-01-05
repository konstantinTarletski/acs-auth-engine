package com.bank.acs.config;

import com.bank.acs.config.property.AppPropertiesLV;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lv.ays.rid.RidClientDTO;
import lv.ays.rid.RidClientParamDTO;
import lv.ays.rid.RidLogDTO;
import lv.ays.rid.RidSmartIdInitDTO;
import lv.ays.rid.RidSmartIdResponseDTO;
import lv.ays.rid.RidUserAuthDataDTO;
import lv.ays.rid.SimpleInterfaceRemote;
import lv.bank.cards.rtcu.util.SimpleInterfaceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.net.URL;
import java.util.List;

import static com.bank.acs.Profile.COUNTRY_LV_PROFILE;

@Slf4j
@Profile(COUNTRY_LV_PROFILE)
@Configuration
public class RoofIdConfig {

    @SneakyThrows
    @Bean
    public SimpleInterfaceRemote simpleInterfaceRemote(AppPropertiesLV appProperties) {
        final URL url = new URL(appProperties.getRoofIdUrl() + appProperties.getRoofIdWsdlPath());
        try {
            var wsdl = new SimpleInterfaceService(url).getSimpleInterfaceWSWrapperPort();
            log.info("Starting WSDL at url = {}", url);
            return wsdl;
        } catch (javax.xml.ws.WebServiceException e) {
            //if WSDL not accessible, it allows application to start;
            log.error("Failed to start WSDL at url = {}, starting empty implementation", url, e);
            return getEmptySimpleInterfaceRemote();
        }
    }

    private SimpleInterfaceRemote getEmptySimpleInterfaceRemote(){
        return new SimpleInterfaceRemote() {

            @Override
            public RidSmartIdResponseDTO smartIdAuthCheck(String s, String s1, String s2) {
                return null;
            }

            @Override
            public String checkCode(String s, String s1, String s2, String s3) {
                return null;
            }

            @Override
            public boolean checkPasswd2Remote(String s, String s1) {
                return false;
            }

            @Override
            public RidClientDTO getRidClientRemote(String s, boolean b) {
                return null;
            }

            @Override
            public boolean checkPasswd1Remote(String s, String s1) {
                return false;
            }

            @Override
            public void journalWrite(RidLogDTO ridLogDTO) {

            }

            @Override
            public RidSmartIdInitDTO smartIdSignInit(String s, String s1) {
                return null;
            }

            @Override
            public String sendCode(String s, String s1, String s2, String s3, String s4, String s5, Integer integer) {
                return null;
            }

            @Override
            public RidSmartIdInitDTO smartIdAuthInit(String s, String s1) {
                return null;
            }

            @Override
            public boolean codetableCheck(String s, String s1, String s2, String s3, boolean b) {
                return false;
            }

            @Override
            public List<RidClientParamDTO> findRidClientParams(String s, String s1, String s2, String s3) {
                return null;
            }

            @Override
            public RidSmartIdResponseDTO smartIdSignCheck(String s, String s1, String s2) {
                return null;
            }

            @Override
            public RidUserAuthDataDTO codetableLoad(String s, String s1) {
                return null;
            }
        };
    }

}
