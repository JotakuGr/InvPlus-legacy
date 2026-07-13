package br.com.invplus.modules.financeiro.importacao.service;

import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import br.com.invplus.modules.financeiro.entity.ExtratoBancario;
import br.com.invplus.modules.financeiro.importacao.dto.ImportacaoExtratoResponse;
import br.com.invplus.modules.financeiro.importacao.dto.ImportacaoExtratoResumoResponse;
import br.com.invplus.modules.financeiro.importacao.entity.ImportacaoExtratoBancario;
import br.com.invplus.modules.financeiro.importacao.enums.FormatoArquivoExtrato;
import br.com.invplus.modules.financeiro.importacao.enums.StatusImportacaoExtrato;
import br.com.invplus.modules.financeiro.importacao.mapper.ImportacaoExtratoMapper;
import br.com.invplus.modules.financeiro.importacao.parser.OfxParser;
import br.com.invplus.modules.financeiro.importacao.parser.dto.ExtratoImportado;
import br.com.invplus.modules.financeiro.importacao.parser.dto.LancamentoImportado;
import br.com.invplus.modules.financeiro.importacao.repository.ImportacaoExtratoRepository;
import br.com.invplus.modules.financeiro.importacao.specification.ImportacaoExtratoSpecification;
import br.com.invplus.modules.financeiro.importacao.validator.ImportacaoExtratoValidator;
import br.com.invplus.modules.financeiro.repository.ContaBancariaRepository;
import br.com.invplus.modules.financeiro.service.ExtratoBancarioLancamentoService;
import br.com.invplus.modules.financeiro.service.ExtratoBancarioService;
import br.com.invplus.security.context.EmpresaContext;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

/** Coordena parser, rastreamento e criacao atomica do dominio bancario. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportacaoExtratoService {

    private final ImportacaoExtratoRepository repository;
    private final ContaBancariaRepository contaBancariaRepository;
    private final ExtratoBancarioService extratoService;
    private final ExtratoBancarioLancamentoService lancamentoService;
    private final OfxParser ofxParser;
    private final ImportacaoExtratoValidator validator;
    private final ImportacaoExtratoMapper mapper;
    private final PlatformTransactionManager transactionManager;
    private final EmpresaContext empresaContext;

    public ImportacaoExtratoResponse importarOfx(MultipartFile arquivo, UUID contaBancariaId, String observacao) {
        OffsetDateTime inicio = OffsetDateTime.now();
        validator.validarArquivo(arquivo);
        byte[] conteudo = lerUmaVez(arquivo);
        validator.validarConteudo(conteudo);
        String hash = sha256(conteudo);
        validator.validarHashUnico(hash);

        ImportacaoExtratoBancario processo = new ImportacaoExtratoBancario();
        processo.receber(sanitizarNome(arquivo.getOriginalFilename()), hash);
        processo = repository.saveAndFlush(processo);
        processo.iniciar();
        repository.saveAndFlush(processo);

        try {
            ExtratoImportado dados = ofxParser.processar(conteudo);
            validator.validarDados(dados);
            ContaBancaria conta = identificarConta(contaBancariaId, dados);
            validator.validarConta(conta, dados);
            List<String> alertas = new ArrayList<>();
            UUID importacaoId = processo.getId();

            ImportacaoExtratoBancario concluida = new TransactionTemplate(transactionManager).execute(status ->
                    persistirDominio(importacaoId, conta, dados, observacao, alertas));
            if (concluida == null) {
                throw new BusinessException("Importacao OFX nao produziu resultado");
            }
            log.info("Importacao OFX concluida importacaoId={} contaBancariaId={} hash={} lancamentos={} status={} tempoMs={}",
                    concluida.getId(), conta.getId(), hash.substring(0, 12),
                    concluida.getQuantidadeLancamentosImportados(), concluida.getStatus(),
                    Duration.between(inicio, OffsetDateTime.now()).toMillis());
            return mapper.toResponse(concluida, alertas);
        } catch (RuntimeException ex) {
            marcarFalha(processo.getId(), ex.getMessage());
            log.warn("Importacao OFX falhou importacaoId={} hash={} status=FALHOU tempoMs={}",
                    processo.getId(), hash.substring(0, 12), Duration.between(inicio, OffsetDateTime.now()).toMillis());
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public ImportacaoExtratoResumoResponse buscar(UUID id) {
        return mapper.toResumo(repository.findByIdAndContaBancariaEmpresaId(id, empresaContext.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Importacao de extrato nao encontrada")));
    }

    @Transactional(readOnly = true)
    public Page<ImportacaoExtratoResumoResponse> listar(UUID contaBancariaId, FormatoArquivoExtrato formato,
            StatusImportacaoExtrato status, String arquivoHash, OffsetDateTime iniciadaEmDe,
            OffsetDateTime iniciadaEmAte, Pageable pageable) {
        Specification<ImportacaoExtratoBancario> spec =
                ImportacaoExtratoSpecification.pertenceEmpresa(empresaContext.getEmpresaId());
        spec = and(spec, ImportacaoExtratoSpecification.contaIgual(contaBancariaId));
        spec = and(spec, ImportacaoExtratoSpecification.formatoIgual(formato));
        spec = and(spec, ImportacaoExtratoSpecification.statusIgual(status));
        spec = and(spec, ImportacaoExtratoSpecification.hashIgual(arquivoHash));
        spec = and(spec, ImportacaoExtratoSpecification.iniciadaDe(iniciadaEmDe));
        spec = and(spec, ImportacaoExtratoSpecification.iniciadaAte(iniciadaEmAte));
        return repository.findAll(spec, pageable).map(mapper::toResumo);
    }

    private ImportacaoExtratoBancario persistirDominio(UUID importacaoId, ContaBancaria conta,
            ExtratoImportado dados, String observacao, List<String> alertas) {
        ImportacaoExtratoBancario processo = repository.findById(importacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Importacao de extrato nao encontrada"));
        ExtratoBancario extrato = extratoService.criarPorImportacao(conta, dados, processo.getArquivoNome(),
                processo.getArquivoHash(), observacao);

        Set<String> fitids = new HashSet<>();
        int importados = 0;
        int ignorados = 0;
        for (LancamentoImportado lancamento : dados.lancamentos()) {
            String fitid = normalizarFitid(lancamento.identificadorBancario());
            if (fitid != null && !fitids.add(fitid)) {
                ignorados++;
                alertas.add("Lancamento FITID " + fitid + " ignorado por duplicidade no arquivo");
                continue;
            }
            lancamentoService.criarPorImportacao(extrato, lancamento);
            importados++;
        }
        processo.concluir(conta, extrato, dados.lancamentos().size(), importados, ignorados);
        return repository.saveAndFlush(processo);
    }

    private ContaBancaria identificarConta(UUID contaBancariaId, ExtratoImportado dados) {
        UUID empresaId = empresaContext.getEmpresaId();
        if (contaBancariaId != null) {
            return contaBancariaRepository
                    .findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(contaBancariaId, empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Conta bancaria nao encontrada"));
        }
        String banco = validator.normalizar(dados.bancoCodigo());
        String agencia = validator.normalizar(dados.agencia());
        String contaOfx = validator.normalizar(dados.numeroConta());
        List<ContaBancaria> candidatas = contaBancariaRepository
                .localizarAtivasPorBancoEAgenciaNormalizados(empresaId, banco, agencia).stream()
                .filter(conta -> contaCompativel(conta, contaOfx))
                .toList();
        if (candidatas.isEmpty()) {
            throw new ResourceNotFoundException("Conta bancaria do arquivo OFX nao encontrada");
        }
        if (candidatas.size() > 1) {
            throw new InvalidOperationException("Mais de uma conta bancaria corresponde ao arquivo OFX");
        }
        return candidatas.get(0);
    }

    private boolean contaCompativel(ContaBancaria conta, String contaOfx) {
        String numero = validator.normalizar(conta.getNumeroConta());
        return contaOfx.equals(numero) || contaOfx.equals(numero + validator.normalizar(conta.getContaDigito()));
    }

    private void marcarFalha(UUID id, String mensagem) {
        ImportacaoExtratoBancario processo = repository.findById(id).orElse(null);
        if (processo != null) {
            processo.falhar(mensagem);
            repository.saveAndFlush(processo);
        }
    }

    private byte[] lerUmaVez(MultipartFile arquivo) {
        try {
            return arquivo.getBytes();
        } catch (IOException ex) {
            throw new BusinessException("Nao foi possivel ler o arquivo OFX", ex);
        }
    }

    private String sha256(byte[] conteudo) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(conteudo));
        } catch (NoSuchAlgorithmException ex) {
            throw new BusinessException("Nao foi possivel calcular o hash do arquivo", ex);
        }
    }

    private String sanitizarNome(String nome) {
        String seguro = nome == null ? "extrato.ofx" : nome.replace('\\', '/');
        seguro = seguro.substring(seguro.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "").trim();
        if (seguro.isBlank()) {
            seguro = "extrato.ofx";
        }
        return seguro.substring(0, Math.min(255, seguro.length()));
    }

    private String normalizarFitid(String fitid) {
        return fitid == null || fitid.isBlank() ? null : fitid.trim().toUpperCase();
    }

    private Specification<ImportacaoExtratoBancario> and(Specification<ImportacaoExtratoBancario> base,
            Specification<ImportacaoExtratoBancario> filtro) {
        return filtro == null ? base : base.and(filtro);
    }
}
