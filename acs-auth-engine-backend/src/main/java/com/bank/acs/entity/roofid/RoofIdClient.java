package com.bank.acs.entity.roofid;

import com.bank.acs.entity.banktron.BanktronData;
import com.bank.acs.enumeration.AuthMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.SEQUENCE;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "roof_id_client")
public class RoofIdClient {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "roof_id_client_seq")
    @SequenceGenerator(name = "roof_id_client_seq", sequenceName = "roof_id_client_seq", allocationSize = 1)
    private Long id;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "roof_id_acs_trans_id", nullable = false)
    private RoofIdData roofIdData;

    @Column(name = "client_number", nullable = false)
    private String clientnumber;

    @Column(name = "codetable_type")
    private String codetableType;

    @Column(name = "codetable_name")
    private String codetableName;

    @Column(name = "codetable_id")
    private Long codetableId;

    @Column(name = "smart_id")
    protected Boolean smartId;

    @Column(name = "language")
    private String language;

    @Enumerated(STRING)
    @Column(name = "last_used_auth_method")
    private AuthMethod lastUsedAuthMethod;


}
