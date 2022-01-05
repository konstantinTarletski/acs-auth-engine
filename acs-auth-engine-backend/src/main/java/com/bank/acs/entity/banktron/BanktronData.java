package com.bank.acs.entity.banktron;

import com.bank.acs.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "banktron_data")
public class BanktronData extends BaseEntity {

    @Id
    @Column(name = "acs_transaction_id", nullable = false)
    private String acsTransactionId;

    @Column(name = "session_token", nullable = true)
    private String sessionToken;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "used_login_id")
    private BanktronLogin usedLogin;

    @Valid
    @OneToMany(mappedBy = "banktronData", cascade = ALL, orphanRemoval = true)
    private List<BanktronPerson> persons;

}
