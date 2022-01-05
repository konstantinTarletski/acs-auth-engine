package com.bank.acs.entity.banktron;

import com.bank.acs.enumeration.banktron.BanktronStatus;
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
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.SEQUENCE;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "banktron_person")
public class BanktronPerson {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "banktron_person_seq")
    @SequenceGenerator(name = "banktron_person_seq", sequenceName = "banktron_person_seq", allocationSize = 1)
    private Long id;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "banktron_data_acs_trans_id", nullable = false)
    private BanktronData banktronData;

    @NotNull
    @Enumerated(STRING)
    @Column(name = "status", nullable = false)
    private BanktronStatus status;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "person_code")
    private String personCode;

    @Column(name = "language")
    private String language;

    @Valid
    @OneToMany(mappedBy = "banktronPerson", cascade = ALL)
    private List<BanktronLogin> logins;
}
