package com.bank.acs.mapper;

import com.bank.acs.dto.banktron.BanktronLoginAuthenticationTypeDto;
import com.bank.acs.dto.banktron.BanktronLoginDto;
import com.bank.acs.dto.banktron.BanktronLoginDto.BanktronLoginAuthenticationTypeList;
import com.bank.acs.dto.banktron.BanktronPersonDto;
import com.bank.acs.dto.banktron.BanktronPersonDto.BanktronLoginList;
import com.bank.acs.entity.banktron.BanktronLogin;
import com.bank.acs.entity.banktron.BanktronPerson;
import com.bank.acs.enumeration.banktron.BanktronAuthMethod;
import com.bank.acs.enumeration.banktron.BanktronStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.bank.acs.enumeration.banktron.BanktronAuthMethod.CODE_CALCULATOR;
import static com.bank.acs.enumeration.banktron.BanktronAuthMethod.SMART_ID;
import static com.bank.acs.enumeration.banktron.BanktronStatus.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;

class BanktronMapperUnitTest {

    private static final BanktronStatus STATUS = ACTIVE;
    private static final String FIRST_NAME = "FIRST_NAME";
    private static final String LAST_NAME = "LAST_NAME";
    private static final String PERSON_CODE = "PERSON_CODE";
    private static final String LANGUAGE = "EN";
    private static final BanktronAuthMethod LAST_AUTH_METHOD = SMART_ID;

    private final BanktronMapper mapper = new BanktronMapper();

    @Test
    void testMapping() {
        // given
        final BanktronLoginAuthenticationTypeDto authMethod1 = BanktronLoginAuthenticationTypeDto.builder().authMethod(CODE_CALCULATOR).build();
        final BanktronLoginAuthenticationTypeDto authMethod2 = BanktronLoginAuthenticationTypeDto.builder().authMethod(SMART_ID).build();
        final BanktronLoginDto loginDto = BanktronLoginDto.builder()
                .status(STATUS)
                .lastAuthMethod(LAST_AUTH_METHOD)
                .authenticationTypesList(BanktronLoginAuthenticationTypeList.builder().authenticationType(List.of(authMethod1, authMethod2)).build())
                .build();
        final BanktronPersonDto dto = BanktronPersonDto.builder()
                .status(STATUS)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .personCode(PERSON_CODE)
                .language(LANGUAGE)
                .loginList(BanktronLoginList.builder().login((List.of(loginDto))).build())
                .build();

        // when
        final BanktronPerson actual = mapper.map(dto);

        // then
        assertThat(actual.getStatus()).isEqualTo(STATUS);
        assertThat(actual.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(actual.getLastName()).isEqualTo(LAST_NAME);
        assertThat(actual.getPersonCode()).isEqualTo(PERSON_CODE);
        assertThat(actual.getLanguage()).isEqualTo(LANGUAGE);

        final List<BanktronLogin> logins = actual.getLogins();
        assertThat(logins).hasSize(1);

        final BanktronLogin login = logins.get(0);
        assertThat(login.getStatus()).isEqualTo(STATUS);
        assertThat(login.getLastAuthMethod()).isEqualTo(LAST_AUTH_METHOD);
        assertThat(login.getAuthMethods()).isEqualTo(Set.of(CODE_CALCULATOR, SMART_ID));
    }
}
