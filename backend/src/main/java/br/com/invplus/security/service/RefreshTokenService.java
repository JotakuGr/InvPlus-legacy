package br.com.invplus.security.service;

import br.com.invplus.security.entity.RefreshToken;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.RefreshTokenRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder TOKEN_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${invplus.security.jwt.refresh-expiration-days:7}")
    private long refreshExpirationDays;

    @Transactional
    public String criar(Usuario usuario, String ip, String userAgent) {
        return criar(usuario, null, null, ip, userAgent);
    }

    @Transactional
    public String criar(Usuario usuario, Empresa empresa, Filial filial, String ip, String userAgent) {
        byte[] randomBytes = new byte[64];
        SECURE_RANDOM.nextBytes(randomBytes);

        String token = TOKEN_ENCODER.encodeToString(randomBytes);
        RefreshToken refreshToken = new RefreshToken(
                usuario, empresa, filial,
                hash(token),
                OffsetDateTime.now().plusDays(refreshExpirationDays),
                ip,
                limitarUserAgent(userAgent)
        );

        refreshTokenRepository.save(refreshToken);
        return token;
    }

    @Transactional(readOnly = true)
    public Usuario validarEObterUsuario(String token) {
        return validar(token).getUsuario();
    }

    @Transactional(readOnly = true)
    public RefreshToken validar(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashAndRevogadoFalse(hash(token))
                .orElseThrow(() -> new BusinessException("Refresh token invalido"));

        if (refreshToken.estaExpirado()) {
            throw new BusinessException("Refresh token expirado");
        }

        Usuario usuario = refreshToken.getUsuario();
        if (!Boolean.TRUE.equals(usuario.getAtivo()) || usuario.getDeletedAt() != null) {
            throw new BusinessException("Usuario inativo");
        }

        return refreshToken;
    }

    @Transactional
    public void revogar(String token) {
        refreshTokenRepository.findByTokenHashAndRevogadoFalse(hash(token))
                .ifPresent(refreshToken -> {
                    refreshToken.revogar();
                    refreshTokenRepository.save(refreshToken);
                });
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Falha ao gerar hash do refresh token", ex);
        }
    }

    private String limitarUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return null;
        }
        return userAgent.length() <= 255 ? userAgent : userAgent.substring(0, 255);
    }
}
