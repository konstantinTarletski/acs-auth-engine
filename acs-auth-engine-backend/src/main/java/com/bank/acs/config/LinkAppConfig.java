package com.bank.acs.config;

import com.bank.acs.config.property.AppProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lv.bank.cards.rtcu.util.BankCardsWSWrapperDelegate;
import lv.bank.cards.rtcu.util.BankCardsWSWrapperService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;

@Slf4j
@Configuration
public class LinkAppConfig {

    @SneakyThrows
    @Bean
    public BankCardsWSWrapperDelegate bankCardsWs(AppProperties appProperties) {
        final URL url = new URL(appProperties.getLinkAppUrl() + appProperties.getLinkAppWsdlPath());
        try {
            var wsdl = new BankCardsWSWrapperService(url).getBankCardsWSWrapperPort();
            log.info("Starting WSDL at url = {}", url);
            return wsdl;
        } catch (javax.xml.ws.WebServiceException e) {
            //if WSDL not accessible, it allows application to start;
            log.error("Failed to start WSDL at url = {}, starting empty implementation", url, e);
            return getEmptyBankCardsWSWrapperDelegate();
        }
    }

    private BankCardsWSWrapperDelegate getEmptyBankCardsWSWrapperDelegate(){
        return new BankCardsWSWrapperDelegate() {
            @Override
            public String rtcungCall(String s) {
                return null;
            }

            @Override
            public String queryCall(String s) {
                return null;
            }
        };
    }
}
