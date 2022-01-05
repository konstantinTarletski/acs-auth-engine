package com.bank.acs.util;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UrlQueryUtilUnitTest {

    @Test
    void testParseEmptyQueryParams() {
        assertThat(UrlQueryUtil.parseQueryParams("")).isEmpty();
    }

    @Test
    void testConvertParamsToQueryString() {
        // given
        final var params = new LinkedHashMap<String, String>();
        params.put("param1", "value1");
        params.put("param2", "value2");
        params.put("param3", "test3_&_ampersand");
        params.put("param4", "test4_=_equal_sign");
        params.put("param5", "");
        params.put("param6", null);
        // when
        final String actual = UrlQueryUtil.convertToQueryString(params);
        // then
        assertThat(actual).isNotNull().isEqualTo("param1=value1&param2=value2&param3=test3_%26_ampersand&param4=test4_%3D_equal_sign&param5=&param6=null");
    }

    @Test
    void testParseQueryParams() {
        // given
        String queryParams = "code=******&verify=verify&threeDSSessionData=01c62048-8d26-41cc-8987-7cfafb39480d";
        // when
        final Map<String, String> actual = UrlQueryUtil.parseQueryParams(queryParams);
        // then
        assertThat(actual).hasSize(3);
        assertThat(actual.get("code")).isEqualTo("******");
        assertThat(actual.get("verify")).isEqualTo("verify");
        assertThat(actual.get("threeDSSessionData")).isEqualTo("01c62048-8d26-41cc-8987-7cfafb39480d");
    }
}
