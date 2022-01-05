package com.bank.acs.entity;

import com.bank.acs.enumeration.AcsErrorCode;
import com.bank.acs.enumeration.AppState;
import com.bank.acs.enumeration.AuthMethod;
import com.bank.acs.enumeration.ChallengeFlowType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

import static com.bank.acs.util.CardUtil.maskCardNumber;
import static com.bank.acs.util.CardUtil.maskPersonalCode;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity(name = "app_session")
public class AppSession extends BaseEntity {

    @Id
    @Column(name = "acs_transaction_id", nullable = false)
    private String acsTransactionId;

    @Column(name = "creq", length = 500)
    private String creq;

    @NotNull
    @Enumerated(STRING)
    @Column(name = "state", nullable = false)
    private AppState state;

    @Enumerated(STRING)
    @Column(name = "error_code")
    private AcsErrorCode errorCode;

    @Column(name = "card_country")
    private String cardCountry;

    @ToString.Exclude
    @Column(name = "card_holder_personal_code")
    private String cardHolderPersonalCode;

    @Enumerated(STRING)
    @Column(name = "used_auth_method")
    private AuthMethod usedAuthMethod;

    @Column(name = "used_language")
    private String usedLanguage;

    @Column(name = "chosen_username")
    private String chosenUsername;

    @Enumerated(STRING)
    @Column(name = "chosen_auth_method")
    private AuthMethod chosenAuthMethod;

    @ToString.Exclude
    @Valid
    @OneToMany(mappedBy = "appSession", cascade = ALL, orphanRemoval = true)
    private List<UserAuthMethod> userAuthMethods;

    @ToString.Exclude
    @Column(name = "card_expiry_date")
    private String cardExpiryDate;

    @ToString.Exclude
    @Column(name = "acct_number")
    private String acctNumber;

    @Column(name = "purchase_date")
    private String purchaseDate;

    @Column(name = "purchase_currency")
    private String purchaseCurrency;

    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "purchase_amount")
    private String purchaseAmount;

    @NotNull
    @Builder.Default
    @Column(name = "current_login_attempt", nullable = false)
    private Integer currentLoginAttempt = 1;

    @NotNull
    @Builder.Default
    @Column(name = "current_auth_method_attempt", nullable = false)
    private Integer currentAuthMethodAttempt = 1;

    @Column(name = "frontend_loaded")
    private Boolean frontendLoaded;

    @Column(name = "logout_done")
    private Boolean logoutDone;

    @Column(name = "authorization_code")
    private String authorizationCode;

    @Column(name = "redirect_url", length = 10000)
    private String redirectUrl;

    @Enumerated(STRING)
    @Column(name = "challenge_flow_type")
    private ChallengeFlowType challengeFlowType;

    @ToString.Include
    private String cardHolderPersonalCode() {
        return maskPersonalCode(cardHolderPersonalCode);
    }

    @ToString.Include
    private String acctNumber() {
        return maskCardNumber(acctNumber);
    }
}
