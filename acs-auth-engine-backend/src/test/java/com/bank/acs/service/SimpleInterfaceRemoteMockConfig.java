package com.bank.acs.service;

import lv.ays.rid.SimpleInterfaceRemote;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import static com.bank.acs.Profile.INT_TEST_PROFILE;
import static org.mockito.Mockito.mock;

@ActiveProfiles(INT_TEST_PROFILE)
@Configuration
public class SimpleInterfaceRemoteMockConfig {

    @Bean
    @Primary
    public SimpleInterfaceRemote simpleInterfaceRemoteMock() {
        return mock(SimpleInterfaceRemote.class);
    }
}

