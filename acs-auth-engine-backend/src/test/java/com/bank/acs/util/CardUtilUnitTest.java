package com.bank.acs.util;

import com.bank.acs.entity.AppSession;
import org.junit.jupiter.api.Test;

import static com.bank.acs.enumeration.AppState.CARD_CHECK_SUCCESSFUL;
import static org.assertj.core.api.Assertions.assertThat;

class CardUtilUnitTest {

    @Test
    void testMaskCardHolderPersonalCode() {
        // given
        final String xml = "<done><card-info-acs><person-code-card-holder>010101-12345</person-code-card-holder></card-info-acs></done>";
        // when
        final String actual = CardUtil.maskPersonalCodeInString(xml);
        // then
        assertThat(actual).isEqualTo("<done><card-info-acs><person-code-card-holder>01****-123**</person-code-card-holder></card-info-acs></done>");
    }

    @Test
    void testMaskPersonalCode() {
        // given
        final String personalCode = "120183-18551";
        // when
        final String actual = CardUtil.maskPersonalCode(personalCode);
        // then
        assertThat(actual).isEqualTo("12****-185**");
    }

    @Test
    void testMaskPersonalCodeToString() {
        // given
        final AppSession appSession = AppSession.builder()
                .acsTransactionId("1")
                .state(CARD_CHECK_SUCCESSFUL)
                .cardHolderPersonalCode("120181-18551")
                .acctNumber("4005850110000610")
                .build();
        // when
        final String appSessionToString = appSession.toString();
        // then
        assertThat(appSessionToString).doesNotContain("120181-18551");
        assertThat(appSessionToString).doesNotContain("4005850110000610");
    }

    @Test
    void testMaskCardNumber() {
        final var pan = "4691390000397746";
        final String actual = CardUtil.maskCardNumber(pan);
        assertThat(actual).isEqualTo("************7746");
    }

}
