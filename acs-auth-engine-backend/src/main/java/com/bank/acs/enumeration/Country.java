package com.bank.acs.enumeration;

import java.util.Arrays;

public enum Country {
    EE,
    LT,
    LV;

    public static Country getCountryFromProfiles(String[] profiles){
        var ret = Arrays.stream(Country.values())
                .filter(country -> profilesContainsCountry(profiles, country))
                .findFirst().orElse(null);
        return ret;
    }

    private static boolean profilesContainsCountry(String[] profiles, Country country){
        return Arrays.stream(profiles).anyMatch(item -> item.equalsIgnoreCase(country.name()));
    }

}
