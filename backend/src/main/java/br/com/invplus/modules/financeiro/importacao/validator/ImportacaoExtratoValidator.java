package br.com.invplus.modules.financeiro.importacao.validator;

import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import br.com.invplus.modules.financeiro.importacao.enums.StatusImportacaoExtrato;
import br.com.invplus.modules.financeiro.importacao.parser.dto.ExtratoImportado;
import br.com.invplus.modules.financeiro.importacao.repository.ImportacaoExtratoRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/** Valida arquivo, modelo intermediario e correspondencia da conta bancaria. */
@Component
@RequiredArgsConstructor
public class ImportacaoExtratoValidator {

    public static final long TAMANHO_MAXIMO = 10L * 1024L * 1024L;
    private final ImportacaoExtratoRepository repository;

    public void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new InvalidOperationException("Arquivo OFX e obrigatorio e nao pode estar vazio");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new InvalidOperationException("Arquivo OFX excede o limite de 10 MB");
        }
        String nome = arquivo.getOriginalFilename();
        if (nome == null || !nome.toLowerCase(Locale.ROOT).endsWith(".ofx")) {
            throw new InvalidOperationException("Arquivo deve possuir extensao .ofx");
        }
    }

    public void validarConteudo(byte[] conteudo) {
        String amostra = new String(conteudo, 0, Math.min(conteudo.length, 4096),
                java.nio.charset.StandardCharsets.ISO_8859_1).toUpperCase(Locale.ROOT);
        if (!amostra.contains("<OFX")) {
            throw new InvalidOperationException("Conteudo enviado nao parece ser OFX");
        }
    }

    public void validarHashUnico(String hash) {
        if (repository.existsByArquivoHashIgnoreCaseAndStatusNot(hash, StatusImportacaoExtrato.FALHOU)) {
            throw new ConflictException("Este arquivo OFX ja foi importado");
        }
    }

    public void validarDados(ExtratoImportado dados) {
        if (dados.bancoCodigo() == null || dados.agencia() == null || dados.numeroConta() == null) {
            throw new InvalidOperationException("OFX deve informar banco, agencia e conta");
        }
        if (dados.dataInicio() == null || dados.dataFim() == null || dados.dataInicio().isAfter(dados.dataFim())) {
            throw new InvalidOperationException("Periodo do arquivo OFX e invalido");
        }
        if (dados.lancamentos() == null || dados.lancamentos().isEmpty()) {
            throw new InvalidOperationException("Arquivo OFX nao possui transacoes");
        }
    }

    public void validarConta(ContaBancaria conta, ExtratoImportado dados) {
        if (!Boolean.TRUE.equals(conta.getAtivo()) || conta.getDeletedAt() != null) {
            throw new InvalidOperationException("Conta bancaria deve estar ativa");
        }
        if (!normalizar(conta.getBancoCodigo()).equals(normalizar(dados.bancoCodigo()))
                || !normalizar(conta.getAgencia()).equals(normalizar(dados.agencia()))) {
            throw new InvalidOperationException("Banco ou agencia do OFX nao correspondem a conta informada");
        }
        String contaOfx = normalizar(dados.numeroConta());
        String numero = normalizar(conta.getNumeroConta());
        String numeroComDigito = numero + normalizar(conta.getContaDigito());
        if (!contaOfx.equals(numero) && !contaOfx.equals(numeroComDigito)) {
            throw new InvalidOperationException("Numero da conta do OFX nao corresponde a conta informada");
        }
    }

    public String normalizar(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }
}
