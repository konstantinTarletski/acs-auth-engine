package com.bank.acs.service.lv;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lv.ays.rid.RidClientParamDTO;

import java.io.Serializable;
import java.util.regex.Pattern;

import static com.bank.acs.util.CardUtil.maskCardNumber;

/**
 * Moved from "Pupas" project
 */
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class RidCardDTO implements Serializable {
    private String card;
    private String expiry;
    private String alias;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String value;
    @ToString.Include
    private boolean readonly = true;
    @ToString.Include
    private String clientId = null;
    private String pam = null;

    @ToString.Include
    private String card() {
        return maskCardNumber(card);
    }

    public RidCardDTO(RidClientParamDTO cp) {
        this.value = cp.getValue();
        String[] vars = Pattern.compile("%").split(this.value);

        for(int r = 0; r < vars.length; ++r) {
            if (vars[r].startsWith("card=")) {
                this.card = vars[r].substring(vars[r].indexOf(61) + 1);
            }

            if (vars[r].startsWith("expiry=")) {
                this.expiry = vars[r].substring(vars[r].indexOf(61) + 1);
            }

            if (vars[r].startsWith("readonly=")) {
                String ro = vars[r].substring(vars[r].indexOf(61) + 1);
                if (ro.equalsIgnoreCase("n")) {
                    this.readonly = false;
                }
            }
        }

        this.alias = cp.getName();
        this.clientId = cp.getRidClient();
    }

    public String getCompiledValue() {
        String[] vars = Pattern.compile("%").split(this.value);
        StringBuffer retData = new StringBuffer();

        for(int r = 0; r < vars.length; ++r) {
            if (vars[r].startsWith("card=")) {
                retData.append("card=" + this.card);
            } else if (vars[r].startsWith("expiry=")) {
                retData.append("expiry=" + this.expiry);
            } else if (vars[r].startsWith("readonly=")) {
                retData.append("readonly=" + (this.readonly ? "y" : "n"));
            } else {
                retData.append(vars[r]);
            }

            retData.append("%");
        }

        return retData.toString();
    }

}
