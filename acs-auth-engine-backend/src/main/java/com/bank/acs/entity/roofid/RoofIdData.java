package com.bank.acs.entity.roofid;

import com.bank.acs.entity.BaseEntity;
import com.bank.acs.entity.banktron.BanktronLogin;
import com.bank.acs.entity.banktron.BanktronPerson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

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
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity(name = "roof_id_data")
public class RoofIdData extends BaseEntity {

    @Id
    @Column(name = "acs_transaction_id", nullable = false)
    private String acsTransactionId;

    @Column(name = "smart_id_hash")
    private String smartIdHash;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "used_roof_id_client_id")
    private RoofIdClient usedLogin;

    @Valid
    @OneToMany(mappedBy = "roofIdData", cascade = ALL, orphanRemoval = true)
    private List<RoofIdClient> clients;

}
