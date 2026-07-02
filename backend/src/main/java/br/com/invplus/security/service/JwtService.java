package br.com.invplus.security.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;

    @Value("${invplus.security.jwt.secret}")
    private String secret;

    @Value("${invplus.security.jwt.expiration-minutes}")
    private long expirationMinutes;

    public String gerarToken(UserDetails userDetails) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationMinutes * 60);

        Map<String, Object> header = Map.of(
                "alg", "HS256",
                "typ", "JWT"
        );

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", userDetails.getUsername());
        payload.put("authorities", authorities(userDetails));
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());

        String headerPayload = encode(header) + "." + encode(payload);
        return headerPayload + "." + assinar(headerPayload);
    }

    public String extrairUsername(String token) {
        return claims(token).get("sub").toString();
    }

    public List<String> extrairAuthorities(String token) {
        Object authorities = claims(token).get("authorities");
        return objectMapper.convertValue(authorities, new TypeReference<>() {
        });
    }

    public boolean tokenValido(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }

            String headerPayload = parts[0] + "." + parts[1];
            if (!constantTimeEquals(assinar(headerPayload), parts[2])) {
                return false;
            }

            Object exp = claims(token).get("exp");
            return exp instanceof Number expiration && Instant.now().getEpochSecond() < expiration.longValue();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return false;
        }
    }

    public OffsetDateTime calcularExpiracao() {
        return OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(expirationMinutes);
    }

    private List<String> authorities(UserDetails userDetails) {
        return userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();
    }

    @SuppressWarnings("UseSpecificCatch")
    private String encode(Map<String, Object> value) {
        try {
            return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao gerar JWT", ex);
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    private Map<String, Object> claims(String token) {
        try {
            String[] parts = token.split("\\.");
            byte[] payload = BASE64_URL_DECODER.decode(parts[1]);
            return objectMapper.readValue(payload, new TypeReference<>() {
            });
        } catch (Exception ex) {
            throw new IllegalArgumentException("JWT invalido", ex);
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    private String assinar(String content) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(key);
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao assinar JWT", ex);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = actual.getBytes(StandardCharsets.UTF_8);
        return java.security.MessageDigest.isEqual(expectedBytes, actualBytes);
    }
}
