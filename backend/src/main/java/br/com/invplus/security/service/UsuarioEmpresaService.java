package br.com.invplus.security.service;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.security.dto.*;import br.com.invplus.security.entity.*;import br.com.invplus.security.mapper.UsuarioEmpresaMapper;
import br.com.invplus.security.repository.*;import br.com.invplus.security.validator.UsuarioEmpresaValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;import java.util.*;
import lombok.RequiredArgsConstructor;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor public class UsuarioEmpresaService {
 private final UsuarioEmpresaRepository repository;private final UsuarioRepository usuarioRepository;private final EmpresaRepository empresaRepository;
 private final UsuarioEmpresaMapper mapper;private final UsuarioEmpresaValidator validator;
 @Transactional public UsuarioEmpresaResponse criar(UsuarioEmpresaRequest r){var u=usuarioRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(r.usuarioId()).orElseThrow(()->new ResourceNotFoundException("Usuario nao encontrado"));
 var e=empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(r.empresaId()).orElseThrow(()->new ResourceNotFoundException("Empresa nao encontrada"));
 validator.validarNovo(u.getId(),e.getId(),Boolean.TRUE.equals(r.principal()));var v=new UsuarioEmpresa();v.configurar(u,e,r.principal(),r.acessoTodasFiliais());return mapper.toResponse(repository.save(v));}
 @Transactional(readOnly=true) public List<UsuarioEmpresaResponse> listar(UUID usuarioId){return repository.findByUsuarioIdAndAtivoTrueOrderByPrincipalDescEmpresaNomeFantasiaAsc(usuarioId).stream().map(mapper::toResponse).toList();}
 @Transactional public UsuarioEmpresaResponse desativar(UUID id){var v=buscar(id);v.desativar();return mapper.toResponse(v);}
 public UsuarioEmpresa buscar(UUID id){return repository.findById(id).filter(v->Boolean.TRUE.equals(v.getAtivo())).orElseThrow(()->new ResourceNotFoundException("Vinculo empresarial nao encontrado"));}
}
