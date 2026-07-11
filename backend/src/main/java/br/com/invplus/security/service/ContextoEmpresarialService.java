package br.com.invplus.security.service;

import br.com.invplus.modules.cadastros.repository.*;
import br.com.invplus.security.context.EmpresaContext;
import br.com.invplus.security.dto.*;
import br.com.invplus.security.entity.*;
import br.com.invplus.security.exception.AcessoEmpresaNegadoException;
import br.com.invplus.security.repository.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class ContextoEmpresarialService {
    private final UsuarioEmpresaRepository vinculoRepository;
    private final UsuarioEmpresaFilialRepository filialVinculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final FilialRepository filialRepository;
    private final UsuarioEmpresaAccessValidator accessValidator;
    private final UsuarioDetailsService detailsService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmpresaContext empresaContext;

    @Transactional(readOnly = true)
    public List<EmpresaDisponivelResponse> empresas(UUID usuarioId) {
        return vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByPrincipalDescEmpresaNomeFantasiaAsc(usuarioId)
                .stream().map(this::mapear).toList();
    }

    @Transactional
    public TokenContextoResponse emitir(UUID usuarioId, UUID empresaId, UUID filialId,
            String ip, String userAgent) {
        accessValidator.validarContexto(usuarioId, empresaId, filialId);
        Usuario usuario = usuarioRepository.findByIdWithPerfisAndPermissoes(usuarioId)
                .orElseThrow(AcessoEmpresaNegadoException::new);
        var empresa = empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresaId)
                .orElseThrow(AcessoEmpresaNegadoException::new);
        var filial = filialId == null ? null : filialRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(filialId)
                .orElseThrow(br.com.invplus.security.exception.AcessoFilialNegadoException::new);
        var principal = new AuthenticatedUserDetails(usuario.getId(), usuario.getEmail(), usuario.getSenhaHash(),
                detailsService.toAuthorities(usuario), true, empresaId, filialId);
        String access = jwtService.gerarAccessToken(principal, empresaId, filialId);
        String refresh = refreshTokenService.criar(usuario, empresa, filial, ip, userAgent);
        return new TokenContextoResponse(access, refresh, contexto(usuario, empresa, filial));
    }

    @Transactional
    public TokenContextoResponse selecionar(String selectionToken, SelecionarContextoRequest request,
            String ip, String userAgent) {
        if (!jwtService.tokenDoTipo(selectionToken, "COMPANY_SELECTION"))
            throw new org.springframework.security.authentication.BadCredentialsException("Token de selecao invalido");
        return emitir(jwtService.extrairUsuarioId(selectionToken), request.empresaId(), request.filialId(), ip, userAgent);
    }

    public TokenContextoResponse trocar(SelecionarContextoRequest request, String ip, String userAgent) {
        return emitir(empresaContext.getUsuarioId(), request.empresaId(), request.filialId(), ip, userAgent);
    }

    public void validarAcesso(UUID usuarioId, UUID empresaId, UUID filialId) {
        accessValidator.validarContexto(usuarioId, empresaId, filialId);
    }

    @Transactional(readOnly = true)
    public ContextoAtualResponse atual() {
        UUID usuarioId=empresaContext.getUsuarioId(), empresaId=empresaContext.getEmpresaId();
        Usuario usuario=usuarioRepository.findByIdWithPerfisAndPermissoes(usuarioId).orElseThrow();
        var empresa=empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresaId).orElseThrow();
        var filial=empresaContext.getFilialId().flatMap(filialRepository::findByIdAndAtivoTrueAndDeletedAtIsNull).orElse(null);
        return contexto(usuario,empresa,filial);
    }

    public List<EmpresaDisponivelResponse> empresasAtuais() { return empresas(empresaContext.getUsuarioId()); }

    private EmpresaDisponivelResponse mapear(UsuarioEmpresa v) {
        List<FilialDisponivelResponse> filiais=filialVinculoRepository
                .findByUsuarioEmpresaIdAndAtivoTrueOrderByPrincipalDescFilialNomeAsc(v.getId()).stream()
                .map(f->new FilialDisponivelResponse(f.getFilial().getId(),f.getFilial().getNome(),Boolean.TRUE.equals(f.getPrincipal()))).toList();
        return new EmpresaDisponivelResponse(v.getEmpresa().getId(),v.getEmpresa().getNomeFantasia(),
                v.getEmpresa().getRazaoSocial(),Boolean.TRUE.equals(v.getPrincipal()),
                Boolean.TRUE.equals(v.getAcessoTodasFiliais()),filiais);
    }

    private ContextoAtualResponse contexto(Usuario usuario, br.com.invplus.modules.cadastros.entity.Empresa empresa,
            br.com.invplus.modules.cadastros.entity.Filial filial) {
        List<String> permissoes=detailsService.toAuthorities(usuario).stream().map(a->a.getAuthority()).sorted().toList();
        return new ContextoAtualResponse(usuario.getId(),empresa.getId(),empresa.getNomeFantasia(),
                filial==null?null:filial.getId(),filial==null?null:filial.getNome(),permissoes);
    }
}
