package com.bank.acs.entity.banktron;

import com.bank.acs.enumeration.banktron.BanktronAuthMethod;
import com.bank.acs.enumeration.banktron.BanktronStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;
import java.util.Set;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.SEQUENCE;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "banktron_login")
public class BanktronLogin {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "banktron_login_seq")
    @SequenceGenerator(name = "banktron_login_seq", sequenceName = "banktron_login_seq", allocationSize = 1)
    private Long id;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "banktron_person_id", nullable = false)
    private BanktronPerson banktronPerson;

    @NotNull
    @Enumerated(STRING)
    @Column(name = "status", nullable = false)
    private BanktronStatus status;

    @Column(name = "username")
    private String username;

    @Column(name = "last_auth_method")
    private BanktronAuthMethod lastAuthMethod;

    @ElementCollection(targetClass = BanktronAuthMethod.class)
    @Enumerated(STRING)
    @CollectionTable(name = "backtron_login_to_auth_method")
    @Column(name = "auth_method")
    private Set<BanktronAuthMethod> authMethods;
}
