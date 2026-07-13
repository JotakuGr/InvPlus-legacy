package br.com.invplus.security.mapper;
import br.com.invplus.security.dto.UsuarioEmpresaFilialResponse;
import br.com.invplus.security.entity.UsuarioEmpresaFilial;
import org.springframework.stereotype.Component;
@Component public class UsuarioEmpresaFilialMapper {
 public UsuarioEmpresaFilialResponse toResponse(UsuarioEmpresaFilial e){return new UsuarioEmpresaFilialResponse(e.getId(),
 e.getUsuarioEmpresa().getId(),e.getFilial().getId(),e.getFilial().getNome(),e.getPrincipal(),e.getAtivo(),
 e.getCriadoEm(),e.getAtualizadoEm());}
}
