package br.com.invplus.security.service;
import br.com.invplus.modules.cadastros.repository.FilialRepository;import br.com.invplus.security.dto.*;import br.com.invplus.security.entity.*;
import br.com.invplus.security.mapper.UsuarioEmpresaFilialMapper;import br.com.invplus.security.repository.UsuarioEmpresaFilialRepository;
import br.com.invplus.security.validator.UsuarioEmpresaFilialValidator;import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.util.*;import lombok.RequiredArgsConstructor;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor public class UsuarioEmpresaFilialService {
 private final UsuarioEmpresaFilialRepository repository;private final UsuarioEmpresaService vinculoService;private final FilialRepository filialRepository;
 private final UsuarioEmpresaFilialMapper mapper;private final UsuarioEmpresaFilialValidator validator;
 @Transactional public UsuarioEmpresaFilialResponse criar(UsuarioEmpresaFilialRequest r){var v=vinculoService.buscar(r.usuarioEmpresaId());var f=filialRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(r.filialId()).orElseThrow(()->new ResourceNotFoundException("Filial nao encontrada"));
 validator.validarNovo(v,f.getId(),f.getEmpresa().getId(),Boolean.TRUE.equals(r.principal()));var e=new UsuarioEmpresaFilial();e.configurar(v,f,r.principal());return mapper.toResponse(repository.save(e));}
 @Transactional(readOnly=true) public List<UsuarioEmpresaFilialResponse> listar(UUID vinculoId){return repository.findByUsuarioEmpresaIdAndAtivoTrueOrderByPrincipalDescFilialNomeAsc(vinculoId).stream().map(mapper::toResponse).toList();}
 @Transactional public UsuarioEmpresaFilialResponse desativar(UUID id){var e=repository.findById(id).orElseThrow(()->new ResourceNotFoundException("Vinculo de filial nao encontrado"));e.desativar();return mapper.toResponse(e);}
}
