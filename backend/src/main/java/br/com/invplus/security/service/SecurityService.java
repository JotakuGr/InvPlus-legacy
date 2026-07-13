package br.com.invplus.security.service;

import br.com.invplus.security.dto.LoginRequest;
import br.com.invplus.security.dto.LoginResponse;
import br.com.invplus.security.entity.Perfil;
import br.com.invplus.security.entity.Permissao;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SecurityService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioDetailsService usuarioDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final ContextoEmpresarialService contextoService;

    @Transactional
    public LoginResponse login(LoginRequest request, String ip, String userAgent) {
        String email = normalizarEmail(request.email());

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.senha())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = usuarioRepository.findByEmailWithPerfisAndPermissoes(email).orElseThrow();
        usuario.setUltimoLogin(OffsetDateTime.now());
        var empresas=contextoService.empresas(usuario.getId());
        if(empresas.size()>1) {
            return new LoginResponse(null,null,null,null,usuarioResponse(usuario),
                    "SELECAO_EMPRESA_NECESSARIA",jwtService.gerarSelectionToken(usuario.getId()),null,empresas);
        }
        if(empresas.size()==1) {
            var empresa=empresas.getFirst();
            UUID filialId=!empresa.acessoTodasFiliais() && empresa.filiais().size()==1
                    ? empresa.filiais().getFirst().filialId():null;
            var tokens=contextoService.emitir(usuario.getId(),empresa.empresaId(),filialId,ip,userAgent);
            return new LoginResponse("Bearer",tokens.accessToken(),tokens.refreshToken(),jwtService.calcularExpiracao(),
                    usuarioResponse(usuario),"AUTENTICADO",null,tokens.contexto(),empresas);
        }
        // Compatibilidade temporaria para usuarios pendentes de saneamento da V126.
        String token=jwtService.gerarToken(userDetails);
        return montarLoginResponse(token,refreshTokenService.criar(usuario,ip,userAgent),jwtService.calcularExpiracao(),usuario);
    }

    @Transactional
    public LoginResponse refresh(String refreshToken, String ip, String userAgent) {
        var refresh = refreshTokenService.validar(refreshToken);
        Usuario usuario = usuarioRepository.findByIdWithPerfisAndPermissoes(refresh.getUsuario().getId()).orElseThrow();
        UUID empresaId=refresh.getEmpresa()==null?null:refresh.getEmpresa().getId();
        UUID filialId=refresh.getFilial()==null?null:refresh.getFilial().getId();
        if(empresaId!=null) contextoService.validarAcesso(usuario.getId(),empresaId,filialId);
        UserDetails userDetails = new AuthenticatedUserDetails(usuario.getId(),usuario.getEmail(),usuario.getSenhaHash(),
                usuarioDetailsService.toAuthorities(usuario),true,empresaId,filialId);
        String accessToken = jwtService.gerarAccessToken(userDetails,empresaId,filialId);
        String novoRefreshToken = refreshTokenService.criar(usuario,refresh.getEmpresa(),refresh.getFilial(),ip,userAgent);
        refreshTokenService.revogar(refreshToken);

        return montarLoginResponse(accessToken, novoRefreshToken, jwtService.calcularExpiracao(), usuario);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revogar(refreshToken);
    }

    private LoginResponse montarLoginResponse(
            String accessToken,
            String refreshToken,
            OffsetDateTime expiresAt,
            Usuario usuario
    ) {
        return new LoginResponse(
                "Bearer",
                accessToken,
                refreshToken,
                expiresAt,
                usuarioResponse(usuario)
        );
    }

    private LoginResponse.UsuarioAutenticadoResponse usuarioResponse(Usuario usuario) {
        return new LoginResponse.UsuarioAutenticadoResponse(
                        usuario.getId().toString(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        perfis(usuario),
                        permissoes(usuario)
                );
    }

    private List<String> perfis(Usuario usuario) {
        return usuario.getPerfis()
                .stream()
                .filter(perfil -> Boolean.TRUE.equals(perfil.getAtivo()))
                .map(Perfil::getNome)
                .sorted()
                .toList();
    }

    private List<String> permissoes(Usuario usuario) {
        return usuario.getPerfis()
                .stream()
                .filter(perfil -> Boolean.TRUE.equals(perfil.getAtivo()))
                .flatMap(perfil -> perfil.getPermissoes().stream())
                .filter(permissao -> Boolean.TRUE.equals(permissao.getAtivo()))
                .map(this::permissao)
                .distinct()
                .sorted()
                .toList();
    }

    private String permissao(Permissao permissao) {
        return permissao.getRecurso() + ":" + permissao.getAcao();
    }

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
