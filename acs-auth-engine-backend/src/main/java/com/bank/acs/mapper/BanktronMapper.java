package com.bank.acs.mapper;

import com.bank.acs.dto.banktron.BanktronLoginAuthenticationTypeDto;
import com.bank.acs.dto.banktron.BanktronLoginDto;
import com.bank.acs.dto.banktron.BanktronPersonDto;
import com.bank.acs.entity.banktron.BanktronLogin;
import com.bank.acs.entity.banktron.BanktronPerson;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Component
public class BanktronMapper {

    public BanktronPerson map(BanktronPersonDto dto) {
        final BanktronPerson person = BanktronPerson.builder()
                .status(dto.getStatus())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .personCode(dto.getPersonCode())
                .language(dto.getLanguage())
                .logins(getLogins(dto).stream().map(this::map).collect(toList()))
                .build();
        person.getLogins().forEach(login -> login.setBanktronPerson(person));
        return person;
    }

    public List<BanktronLoginDto> getLogins(BanktronPersonDto dto) {
        return dto.getLoginList() != null && dto.getLoginList().getLogin() != null ? dto.getLoginList().getLogin() : emptyList();
    }

    public static <T> List<T> emptyIfNull(List<T> collection) {
        return collection == null ? emptyList() : collection;
    }

    public BanktronLogin map(BanktronLoginDto dto) {
        return BanktronLogin.builder()
                .status(dto.getStatus())
                .username(dto.getUsername())
                .lastAuthMethod(dto.getLastAuthMethod())
                .authMethods(emptyIfNull(dto.getAuthenticationTypesList().getAuthenticationType()).stream().map(BanktronLoginAuthenticationTypeDto::getAuthMethod).collect(toSet()))
                .build();
    }
}
