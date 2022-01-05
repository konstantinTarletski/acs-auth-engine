package com.bank.acs.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersonalCodeValidatorUnitTest {


    @Test
    void testSuccessValidation() {
        assertThat(StringUtil.isPersonalCodeValid("0012345678900")).isTrue();
    }

    @Test
    void testFailValidation() {
        assertThat(StringUtil.isPersonalCodeValid("0000000000000")).isFalse();
    }
}
