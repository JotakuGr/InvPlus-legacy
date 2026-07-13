package br.com.invplus.security.validator;
import br.com.invplus.security.repository.UsuarioEmpresaRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.UUID;
@Component @RequiredArgsConstructor public class UsuarioEmpresaValidator {
 private final UsuarioEmpresaRepository repository;
 public void validarNovo(UUID usuarioId,UUID empresaId,boolean principal){
  if(repository.existsByUsuarioIdAndEmpresaId(usuarioId,empresaId))throw new ConflictException("Vinculo usuario-empresa duplicado");
  if(principal&&repository.existsByUsuarioIdAndPrincipalTrueAndAtivoTrue(usuarioId))throw new ConflictException("Usuario ja possui empresa principal ativa");
 }
}
