package br.com.invplus.modules.financeiro.conciliacao.util;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Normaliza descricoes sem remover nomes e referencias relevantes. */
@Component
public class NormalizadorTextoFinanceiro {

    private static final Set<String> PALAVRAS_IRRELEVANTES = Set.of(
            "PIX", "PAGAMENTO", "RECEBIMENTO", "RECEBIDO", "TRANSFERENCIA", "CREDITO",
            "DEBITO", "BANCO", "TED", "DOC");

    public String normalizar(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return Arrays.stream(normalizado.split(" "))
                .filter(token -> !token.isBlank() && !PALAVRAS_IRRELEVANTES.contains(token))
                .collect(Collectors.joining(" "));
    }
}
