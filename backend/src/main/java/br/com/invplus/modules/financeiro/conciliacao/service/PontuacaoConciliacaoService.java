package br.com.invplus.modules.financeiro.conciliacao.service;

import br.com.invplus.modules.financeiro.conciliacao.dto.CriteriosSugestaoResponse;
import br.com.invplus.modules.financeiro.conciliacao.dto.SugestaoConciliacaoResponse;
import br.com.invplus.modules.financeiro.conciliacao.util.NormalizadorTextoFinanceiro;
import br.com.invplus.modules.financeiro.entity.ExtratoBancarioLancamento;
import br.com.invplus.modules.financeiro.entity.MovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.NivelConfiancaConciliacao;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Calcula uma pontuacao explicavel sem substituir validacoes de dominio. */
@Service
@RequiredArgsConstructor
public class PontuacaoConciliacaoService {

    private final NormalizadorTextoFinanceiro normalizador;

    public SugestaoConciliacaoResponse calcular(
            ExtratoBancarioLancamento lancamento, MovimentoFinanceiro movimento) {
        long diferencaDias = Math.abs(ChronoUnit.DAYS.between(
                lancamento.getDataLancamento(), movimento.getDataMovimento()));
        boolean documentoIgual = documentosIguais(lancamento.getDocumento(), movimento.getOrigemId());
        BigDecimal similaridade = calcularSimilaridade(
                lancamento.getDescricao(), movimento.getDescricao());
        int pontuacao = 75 + pontuarData(diferencaDias)
                + (documentoIgual ? 10 : 0) + pontuarDescricao(similaridade);
        var conta = movimento.getContaBancaria();
        return new SugestaoConciliacaoResponse(
                movimento.getId(), movimento.getDescricao(), movimento.getDataMovimento(),
                movimento.getTipoMovimento(), movimento.getValor(), conta.getId(),
                conta.getBancoNome() + " - " + conta.getAgencia() + "/" + conta.getNumeroConta(),
                pontuacao, classificar(pontuacao),
                new CriteriosSugestaoResponse(true, true, true, diferencaDias,
                        documentoIgual, similaridade));
    }

    private int pontuarData(long diferenca) {
        return switch ((int) diferenca) {
            case 0 -> 10;
            case 1 -> 8;
            case 2 -> 5;
            case 3 -> 2;
            default -> 0;
        };
    }

    private boolean documentosIguais(String documento, java.util.UUID origemId) {
        if (documento == null || documento.isBlank() || origemId == null) return false;
        return normalizarDocumento(documento).equals(normalizarDocumento(origemId.toString()));
    }

    private String normalizarDocumento(String valor) {
        return valor.replaceAll("[^A-Za-z0-9]", "").toUpperCase(java.util.Locale.ROOT);
    }

    private BigDecimal calcularSimilaridade(String a, String b) {
        Set<String> tokensA = tokens(normalizador.normalizar(a));
        Set<String> tokensB = tokens(normalizador.normalizar(b));
        if (tokensA.isEmpty() || tokensB.isEmpty()) return BigDecimal.ZERO.setScale(4);
        Set<String> intersecao = new HashSet<>(tokensA);
        intersecao.retainAll(tokensB);
        Set<String> uniao = new HashSet<>(tokensA);
        uniao.addAll(tokensB);
        return BigDecimal.valueOf((double) intersecao.size() / uniao.size())
                .setScale(4, RoundingMode.HALF_UP);
    }

    private Set<String> tokens(String texto) {
        return Arrays.stream(texto.split(" ")).filter(token -> !token.isBlank()).collect(Collectors.toSet());
    }

    private int pontuarDescricao(BigDecimal valor) {
        if (valor.compareTo(new BigDecimal("0.90")) >= 0) return 5;
        if (valor.compareTo(new BigDecimal("0.70")) >= 0) return 4;
        if (valor.compareTo(new BigDecimal("0.50")) >= 0) return 3;
        if (valor.compareTo(new BigDecimal("0.30")) >= 0) return 1;
        return 0;
    }

    private NivelConfiancaConciliacao classificar(int pontuacao) {
        if (pontuacao >= 90) return NivelConfiancaConciliacao.ALTA;
        if (pontuacao >= 80) return NivelConfiancaConciliacao.MEDIA;
        return NivelConfiancaConciliacao.BAIXA;
    }
}
