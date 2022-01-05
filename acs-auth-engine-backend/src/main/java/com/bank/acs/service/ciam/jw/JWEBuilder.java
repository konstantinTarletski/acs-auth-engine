package com.bank.acs.service.ciam.jw;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jwt.EncryptedJWT;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@Slf4j
@NoArgsConstructor
public class JWEBuilder {

    public static final JWEAlgorithm defaultJweAlgorithm = JWEAlgorithm.RSA_OAEP_256;
    public static final EncryptionMethod defaultEncryptionMethod = EncryptionMethod.A128GCM;

    private Long jwtLifetime = JwtUtils.defaultJwtLifetime;
    private JWEAlgorithm jweAlgorithm = defaultJweAlgorithm;
    private EncryptionMethod encryptionMethod = defaultEncryptionMethod;

    private PublicKey publicKey;

    private JWKSupport keySource;
    private String kid;

    private Map<String, Object> claims;

    public JWEBuilder setKeySource(JWKSupport keySource) {
        this.keySource = keySource;
        return this;
    }

    public JWEBuilder setClaims(Map<String, Object> claims) {
        this.claims = claims;
        return this;
    }

    public JWEBuilder setKid(String kid) {
        this.kid = kid;
        return this;
    }

    private static EncryptedJWT encrypt(long lifetime,
                                        Map<String, Object> claimsMap,
                                        RSAPublicKey publicKey,
                                        JWEAlgorithm jweAlgorithm,
                                        EncryptionMethod encryptionMethod) {
        return JwtUtils.getEncryptedJWT(lifetime, claimsMap, publicKey, jweAlgorithm, encryptionMethod);
    }

    private EncryptedJWT encrypt(RSAPublicKey publicKey) {
        EncryptedJWT jwe = encrypt(jwtLifetime, claims, publicKey, jweAlgorithm, encryptionMethod);
        return jwe;
    }

    public EncryptedJWT encrypt() {
        log.debug("encrypt: jwtLifetime={}, jweAlgorithm={}, encryptionMethod={}, claims={}, publicKey={}, kid={}",
                jwtLifetime, jweAlgorithm, encryptionMethod, claims, publicKey, kid);

        PublicKey key;

        if (publicKey != null) {
            key = publicKey;
        } else {
            key = keySource.getPublicKey(kid);
        }

        if (key instanceof RSAPublicKey) {
            RSAPublicKey publicKey = (RSAPublicKey) key;
            return encrypt(publicKey);
        } else {
            throw new RuntimeException("not implemented");
        }
    }

    public String build() {
        String jwe = encrypt().serialize();
        log.debug("build: jwe={}", jwe);
        return jwe;
    }

    public static JWEBuilder newInstance() {
        return new JWEBuilder();
    }

    public static JWEBuilder newInstance(JWKSupport keySource, String kid) {
        return newInstance().setKeySource(keySource).setKid(kid);
    }
}
