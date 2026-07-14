package br.com.invplus.security.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
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
        return gerarAccessToken(userDetails, null, null);
    }

    public String gerarAccessToken(UserDetails userDetails, UUID empresaId, UUID filialId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationMinutes * 60);

        Map<String, Object> header = Map.of(
                "alg", "HS256",
                "typ", "JWT"
        );

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", userDetails.getUsername());
        if (userDetails instanceof AuthenticatedUserDetails principal) {
            payload.put("usuario_id", principal.getId().toString());
        }
        if (empresaId != null) payload.put("empresa_id", empresaId.toString());
        if (filialId != null) payload.put("filial_id", filialId.toString());
        payload.put("token_use", "ACCESS");
        payload.put("jti", UUID.randomUUID().toString());
        payload.put("authorities", authorities(userDetails));
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());

        String headerPayload = encode(header) + "." + encode(payload);
        return headerPayload + "." + assinar(headerPayload);
    }

    public String gerarSelectionToken(UUID usuarioId) {
        Instant now=Instant.now();
        Map<String,Object> payload=new LinkedHashMap<>();
        payload.put("sub", usuarioId.toString()); payload.put("usuario_id", usuarioId.toString());
        payload.put("token_use","COMPANY_SELECTION"); payload.put("jti",UUID.randomUUID().toString());
        payload.put("iat",now.getEpochSecond()); payload.put("exp",now.plusSeconds(300).getEpochSecond());
        Map<String,Object> header=Map.of("alg","HS256","typ","JWT");
        String hp=encode(header)+"."+encode(payload); return hp+"."+assinar(hp);
    }

    public String extrairUsername(String token) {
        return claims(token).get("sub").toString();
    }

    public List<String> extrairAuthorities(String token) {
        Object authorities = claims(token).get("authorities");
        return objectMapper.convertValue(authorities, new TypeReference<>() {
        });
    }

    public String extrairTokenUse(String token) { return String.valueOf(claims(token).get("token_use")); }
    public UUID extrairUsuarioId(String token) { return extrairUuidClaim(token,"usuario_id"); }
    public UUID extrairUuidClaim(String token,String nome) {
        Object valor=claims(token).get(nome);
        if(valor==null) throw new IllegalArgumentException("Claim obrigatorio ausente: "+nome);
        try { return UUID.fromString(valor.toString()); } catch(Exception ex) { throw new IllegalArgumentException("Claim UUID invalido: "+nome,ex); }
    }
    public UUID extrairUuidClaimOpcional(String token,String nome) {
        Object valor=claims(token).get(nome); return valor==null?null:extrairUuidClaim(token,nome);
    }
    public boolean tokenDoTipo(String token,String tipo) {
        return tokenValido(token) && tipo.equals(extrairTokenUse(token));
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
                .map(authority -> authority.getAuthority())
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
