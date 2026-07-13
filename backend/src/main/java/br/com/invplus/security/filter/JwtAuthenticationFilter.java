package br.com.invplus.security.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.invplus.security.service.JwtService;
import br.com.invplus.security.service.AuthenticatedUserDetails;
import br.com.invplus.security.service.UsuarioEmpresaAccessValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UsuarioEmpresaAccessValidator accessValidator;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());

        if (SecurityContextHolder.getContext().getAuthentication() == null
                && jwtService.tokenDoTipo(token, "ACCESS")) {
            try {
                String username = jwtService.extrairUsername(token);
                AuthenticatedUserDetails base = (AuthenticatedUserDetails) userDetailsService.loadUserByUsername(username);
                java.util.UUID empresaId = jwtService.extrairUuidClaimOpcional(token, "empresa_id");
                java.util.UUID filialId = jwtService.extrairUuidClaimOpcional(token, "filial_id");
                if (empresaId == null && exigeContextoEmpresarial(request)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                            "Contexto empresarial ausente ou invalido");
                    return;
                }
                if (empresaId != null) accessValidator.validarContexto(base.getId(), empresaId, filialId);
                UserDetails userDetails = new AuthenticatedUserDetails(base.getId(), base.getUsername(),
                        base.getPassword(), base.getAuthorities(), base.isEnabled(), empresaId, filialId);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (AuthenticationException ex) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean exigeContextoEmpresarial(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/cadastros/filiais")
                || path.startsWith("/api/v1/estoques")
                || path.startsWith("/api/v1/depositos")
                || path.startsWith("/api/v1/localizacoes-estoque")
                || path.startsWith("/api/v1/movimentacoes-estoque")
                || path.startsWith("/api/v1/reservas-estoque")
                || path.startsWith("/api/v1/pedidos-compra")
                || path.startsWith("/api/v1/pedidos-compra-itens")
                || path.startsWith("/api/v1/entradas-mercadoria")
                || path.startsWith("/api/v1/entradas-mercadoria-itens")
                || path.startsWith("/api/v1/pedidos-venda")
                || path.startsWith("/api/v1/pedidos-venda-itens")
                || path.startsWith("/api/v1/vendas-faturamentos")
                || path.startsWith("/api/v1/produtos-precos")
                || path.startsWith("/api/v1/contas-bancarias")
                || path.startsWith("/api/v1/extratos-bancarios")
                || path.startsWith("/api/v1/importacoes-extratos")
                || path.startsWith("/api/v1/conciliacoes-bancarias")
                || path.startsWith("/api/v1/movimentos-financeiros")
                || path.startsWith("/api/v1/contas-receber")
                || path.startsWith("/api/v1/contas-pagar")
                || path.startsWith("/api/v1/recebimentos")
                || path.startsWith("/api/v1/pagamentos")
                || path.startsWith("/api/v1/financeiro/relatorios");
    }
}
