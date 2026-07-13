package br.com.invplus.security.validator;
import br.com.invplus.security.entity.UsuarioEmpresa;
import br.com.invplus.security.repository.UsuarioEmpresaFilialRepository;
import br.com.invplus.shared.domain.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.UUID;
@Component @RequiredArgsConstructor public class UsuarioEmpresaFilialValidator {
 private final UsuarioEmpresaFilialRepository repository;
 public void validarNovo(UsuarioEmpresa v,UUID filialId,UUID empresaFilialId,boolean principal){
  if(Boolean.TRUE.equals(v.getAcessoTodasFiliais()))throw new InvalidOperationException("Acesso total nao permite vinculos especificos");
  if(!v.getEmpresa().getId().equals(empresaFilialId))throw new InvalidOperationException("Filial deve pertencer a empresa do vinculo");
  if(repository.existsByUsuarioEmpresaIdAndFilialId(v.getId(),filialId))throw new ConflictException("Filial ja vinculada");
  if(principal&&repository.existsByUsuarioEmpresaIdAndPrincipalTrueAndAtivoTrue(v.getId()))throw new ConflictException("Vinculo ja possui filial principal ativa");
 }
}
