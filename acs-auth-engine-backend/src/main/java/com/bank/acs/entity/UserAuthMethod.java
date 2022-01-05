package com.bank.acs.entity;

import com.bank.acs.enumeration.AuthMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.SEQUENCE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "user_auth_method")
public class UserAuthMethod {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "user_auth_method_seq")
    @SequenceGenerator(name = "user_auth_method_seq", sequenceName = "user_auth_method_seq", allocationSize = 1)
    private Long id;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "app_session_id", nullable = false)
    private AppSession appSession;

    @Enumerated(STRING)
    @Column(name = "auth_method")
    private AuthMethod authMethod;

}
