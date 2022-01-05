package com.bank.acs.mapper;

import com.bank.acs.entity.roofid.RoofIdClient;
import com.bank.acs.enumeration.roofid.RoofIdAuthMethod;
import lv.ays.rid.RidClientDTO;
import org.springframework.stereotype.Component;

@Component
public class RoofIdMapper {

    public RoofIdClient map(RidClientDTO dto) {
        return RoofIdClient.builder()
                .clientnumber(dto.getClientId())
                .codetableName(dto.getDefaultCodetableName())
                .codetableType(dto.getDefaultCodetableType())
                .codetableId(dto.getDefaultCodetableId())
                .smartId(dto.isSmartId())
                .lastUsedAuthMethod(RoofIdAuthMethod.toAuthMethod(RoofIdAuthMethod.fromString(dto.getLastAuthMethod())))
                .language(dto.getLanguage())
                .build();
    }

}
