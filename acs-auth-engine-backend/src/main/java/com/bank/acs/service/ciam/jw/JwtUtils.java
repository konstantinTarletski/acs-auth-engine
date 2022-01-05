package com.bank.acs.service.ciam.jw;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtUtils {

    public static final long defaultJwtLifetime = 1000 * 300;//5min
    public static final long defaultReasonableJwtLifetime = 1000 * 1800;//30min


    private static EncryptedJWT encrypt(long lifetime,
                                        Map<String, Object> claimsMap,
                                        RSAPublicKey publicKey,
                                        JWEAlgorithm jweAlgorithm,
                                        EncryptionMethod encryptionMethod) {
        return JwtUtils.getEncryptedJWT(lifetime, claimsMap, publicKey, jweAlgorithm, encryptionMethod);
    }

    @SneakyThrows
    public static EncryptedJWT getEncryptedJWT(long lifetime,
                                               Map<String, Object> claimsMap,
                                               RSAPublicKey publicKey,
                                               JWEAlgorithm jweAlgorithm,
                                               EncryptionMethod encryptionMethod) {

        JWEHeader header = new JWEHeader(jweAlgorithm, encryptionMethod);
        JWTClaimsSet claimsSet = getJwtBuilder(lifetime, claimsMap).build();
        EncryptedJWT jwe = new EncryptedJWT(header, claimsSet);
        RSAEncrypter encrypter = new RSAEncrypter(publicKey);
        jwe.encrypt(encrypter);

        return jwe;
    }

    public static JWTClaimsSet.Builder getJwtBuilder(long lifetime, Map<String, Object> claimsMap) {

        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + lifetime;

        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
        builder.issueTime(new Date(nowMillis));
        builder.expirationTime(new Date(expMillis));
        builder.notBeforeTime(new Date());
        builder.jwtID(UUID.randomUUID().toString());

        claimsMap.forEach(builder::claim);

        return builder;
    }

    @SneakyThrows
    public static SignedJWT signedJWT(long lifetime, Map<String, Object> claimsMap, JWSAlgorithm jwsAlgorithm, PrivateKey privateKey) {
        JWTClaimsSet claimsSet = getJwtBuilder(lifetime, claimsMap).build();
        JWSHeader header = new JWSHeader.Builder(jwsAlgorithm)
                .type(JOSEObjectType.JWT)
                .build();
        JWSSigner signer = jwsSigner(jwsAlgorithm, privateKey);
        SignedJWT jws = new SignedJWT(header, claimsSet);
        jws.sign(signer);
        return jws;
    }

    private static JWSSigner jwsSigner(JWSAlgorithm jwsAlgorithm, PrivateKey privateKey) throws JOSEException {
        switch (jwsAlgorithm.getName()) {
            case "RS256":
            case "RS384":
            case "RS512": {
                return new RSASSASigner(privateKey);
            }
            case "ES256":
            case "ES384":
            case "ES512": {
                return new ECDSASigner((ECPrivateKey) privateKey);
            }
            default:
                throw new RuntimeException("not implemented");
        }
    }

}
