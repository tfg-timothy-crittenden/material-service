package com.timcritt.tfg.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;

@Configuration
public class JwtDecoderConfig {

    @Bean
    JwtDecoder jwtDecoder(
            @Value("${security.jwt.hs256.secret:}") String hs256Secret,
            @Value("${security.jwt.hs256.secret-format:raw}") String secretFormat,
            @Value("${security.jwt.hs256.algorithm:HS256}") String algorithm) {
        if (hs256Secret == null || hs256Secret.isBlank()) {
            throw new IllegalStateException("security.jwt.hs256.secret must be configured for HS256 token validation");
        }

        byte[] rawSecret = decodeSecret(hs256Secret, secretFormat);
        byte[] compatibleSecret = rawSecret.length >= 32 ? rawSecret : padTo32Bytes(rawSecret);
        MacAlgorithm macAlgorithm = resolveAlgorithm(algorithm);
        SecretKeySpec key = new SecretKeySpec(compatibleSecret, toJcaHmacName(macAlgorithm));
        return NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(macAlgorithm)
                .build();
    }

    private byte[] decodeSecret(String secret, String format) {
        return switch (format.toLowerCase()) {
            case "raw" -> secret.getBytes(StandardCharsets.UTF_8);
            case "base64" -> Base64.getDecoder().decode(secret);
            case "hex" -> HexFormat.of().parseHex(secret);
            default -> throw new IllegalStateException(
                    "security.jwt.hs256.secret-format must be one of: raw, base64, hex");
        };
    }

    private MacAlgorithm resolveAlgorithm(String algorithm) {
        return switch (algorithm.toUpperCase()) {
            case "HS256" -> MacAlgorithm.HS256;
            case "HS384" -> MacAlgorithm.HS384;
            case "HS512" -> MacAlgorithm.HS512;
            default -> throw new IllegalStateException("security.jwt.hs256.algorithm must be one of: HS256, HS384, HS512");
        };
    }

    private String toJcaHmacName(MacAlgorithm algorithm) {
        return switch (algorithm.getName()) {
            case "HS256" -> "HmacSHA256";
            case "HS384" -> "HmacSHA384";
            case "HS512" -> "HmacSHA512";
            default -> throw new IllegalStateException("Unsupported MacAlgorithm: " + algorithm.getName());
        };
    }

    private byte[] padTo32Bytes(byte[] input) {
        byte[] padded = new byte[32];
        System.arraycopy(input, 0, padded, 0, Math.min(input.length, padded.length));
        return padded;
    }
}

