package com.bank.acs.service.ciam.jw;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.KeyOperation;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class NimbusJWKSupport implements JWKSupport {

    private static final Logger logger = LoggerFactory.getLogger(NimbusJWKSupport.class);

    private static final int connectTimeout = 30000;
    private static final int readTimeout = 30000;

    private URL jwkSetUrl;
    private ResourceRetriever jwkSetRetriever;
    private RemoteJWKSet<SecurityContext> jwkSource;
    private JWSAlgorithm matcherAlgorithm;

    public NimbusJWKSupport(URI jwkUri) {
        this(jwkUri, defaultJwkSetRetriever(), "RS256");
    }

    private NimbusJWKSupport(URI jwkUri, ResourceRetriever jwkSetRetriever, String matcherAlgorithm) {
        logger.debug("jwkUri={}, matcherAlgorithm={}", jwkUri, matcherAlgorithm);
        this.jwkSetRetriever = jwkSetRetriever;
        this.setJwkSource(jwkUri);
        Optional.ofNullable(matcherAlgorithm).ifPresent(this::setMatcherAlgorithm);
    }

    private static ResourceRetriever defaultJwkSetRetriever() {
        return new DefaultResourceRetriever(connectTimeout, readTimeout);
    }

    public NimbusJWKSupport setJwkSetRetriever(ResourceRetriever jwkSetRetriever) {
        this.jwkSetRetriever = jwkSetRetriever;
        return this;
    }

    public NimbusJWKSupport setJwkSource(URI jwkUri) {
        Assert.notNull(jwkUri, "jwkSetUrl cannot be empty");
        try {
            this.jwkSetUrl = jwkUri.toURL();
        } catch (MalformedURLException var6) {
            throw new IllegalArgumentException("Invalid JWK Set URL " + jwkUri + " : " + var6.getMessage(), var6);
        }

        ResourceRetriever retriever = Objects.requireNonNull(jwkSetRetriever);
        this.jwkSource = new RemoteJWKSet<>(this.jwkSetUrl, retriever);

        return this;
    }

    public NimbusJWKSupport setMatcherAlgorithm(String matcherAlgorithm) {
        Assert.hasText(matcherAlgorithm, "matcherAlgorithm cannot be empty");
        this.matcherAlgorithm = JWSAlgorithm.parse(matcherAlgorithm);
        return this;
    }

    private List<JWK> getKeyCandidates(String kid) throws JOSEException {

        JWKMatcher matcher = new JWKMatcherBuilder()
                .setAlgs(matcherAlgorithm)
                .setIds(kid)
                .build();
        JWKSelector jwsKeySelector = new JWKSelector(matcher);

        return jwkSource.get(jwsKeySelector, null);
    }

    @Override
    public PublicKey getPublicKey(String kid) {
        try {
            List<JWK> keyCandidates = getKeyCandidates(kid);
            PublicKey publicKey = null;

            for (JWK jwk : keyCandidates) {
                if (jwk instanceof RSAKey) {
                    publicKey = ((RSAKey) jwk).toRSAPublicKey();
                    break;
                } else if (jwk instanceof ECKey) {
                    publicKey = ((ECKey) jwk).toECPublicKey();
                    break;
                } else {
                    throw new RuntimeException("not implemented");
                }
            }
            return publicKey;
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PrivateKey getPrivateKey(String kid) {
        try {
            List<JWK> keyCandidates = getKeyCandidates(kid);
            PrivateKey privateKey = null;

            for (JWK jwk : keyCandidates) {
                if (jwk instanceof RSAKey) {
                    privateKey = ((RSAKey) jwk).toRSAPrivateKey();
                    break;
                } else if (jwk instanceof ECKey) {
                    privateKey = ((ECKey) jwk).toECPrivateKey();
                    break;
                } else {
                    throw new RuntimeException("not implemented");
                }
            }
            return privateKey;
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private static class JWKMatcherBuilder {

        Set<KeyType> types = null;
        Set<KeyUse> uses = null;
        Set<KeyOperation> ops = null;
        Set<Algorithm> algs = null;
        Set<String> ids = null;
        boolean hasUse = false;
        boolean hasID = true;
        boolean privateOnly = false;
        boolean publicOnly = false;
        int minSizeBits = 0;
        int maxSizeBits = Integer.MAX_VALUE;
        Set<Integer> sizesBits = null;
        Set<Curve> curves = null;
        Set<Base64URL> x5tS256s = null;

        public JWKMatcherBuilder setIds(String kid) {
            this.ids = ids = Collections.singleton(kid);
            return this;
        }

        public JWKMatcherBuilder setAlgs(Algorithm jwsAlgorithm) {
            this.algs = Optional.ofNullable(jwsAlgorithm)
                    .map(o -> Collections.singleton(jwsAlgorithm))
                    .orElse(null);
            return this;
        }

        public JWKMatcher build() {
            return new JWKMatcher(
                    this.types,
                    this.uses,
                    this.ops,
                    this.algs,
                    this.ids,
                    this.hasUse,
                    this.hasID,
                    this.privateOnly,
                    this.publicOnly,
                    this.minSizeBits,
                    this.maxSizeBits,
                    this.sizesBits,
                    this.curves,
                    this.x5tS256s);
        }
    }
}
