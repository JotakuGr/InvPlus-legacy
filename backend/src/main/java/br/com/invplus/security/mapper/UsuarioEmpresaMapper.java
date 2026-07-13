package br.com.invplus.security.mapper;
import br.com.invplus.security.dto.UsuarioEmpresaResponse;
import br.com.invplus.security.entity.UsuarioEmpresa;
import org.springframework.stereotype.Component;
@Component public class UsuarioEmpresaMapper {
 public UsuarioEmpresaResponse toResponse(UsuarioEmpresa e){return new UsuarioEmpresaResponse(e.getId(),e.getUsuario().getId(),
 e.getUsuario().getEmail(),e.getEmpresa().getId(),e.getEmpresa().getNomeFantasia(),e.getPrincipal(),
 e.getAcessoTodasFiliais(),e.getAtivo(),e.getCriadoEm(),e.getAtualizadoEm());}
}
