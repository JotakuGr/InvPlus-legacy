package br.com.invplus.modules.financeiro.importacao.parser;

import br.com.invplus.modules.financeiro.importacao.parser.dto.ExtratoImportado;
import br.com.invplus.modules.financeiro.importacao.parser.dto.LancamentoImportado;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Converte OFX 1.x/2.x em um modelo intermediario usando um parser XML seguro.
 * A normalizacao fecha apenas tags folha conhecidas do OFX SGML.
 */
@Component
public class OfxParser implements ExtratoParser {

    private static final Set<String> CONTENT_TYPES = Set.of(
            "application/x-ofx", "application/ofx", "application/octet-stream", "text/plain", "text/xml");
    private static final List<String> LEAF_TAGS = List.of(
            "BANKID", "BRANCHID", "ACCTID", "ACCTTYPE", "CURDEF", "DTSTART", "DTEND",
            "BALAMT", "DTASOF", "TRNTYPE", "DTPOSTED", "TRNAMT", "FITID", "CHECKNUM",
            "MEMO", "NAME", "REFNUM");

    @Override
    public boolean suporta(String nomeArquivo, String contentType) {
        boolean extensao = nomeArquivo != null && nomeArquivo.toLowerCase(Locale.ROOT).endsWith(".ofx");
        return extensao && (contentType == null || CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT)));
    }

    @Override
    public ExtratoImportado processar(byte[] conteudo) {
        if (conteudo == null || conteudo.length == 0) {
            throw new InvalidOperationException("Arquivo OFX nao pode ser vazio");
        }
        try {
            String ofx = normalizarOfx(conteudo);
            Document documento = criarFactorySegura().newDocumentBuilder().parse(
                    new ByteArrayInputStream(ofx.getBytes(StandardCharsets.UTF_8)));
            Element raiz = documento.getDocumentElement();
            if (raiz == null || !"OFX".equalsIgnoreCase(raiz.getTagName())) {
                throw new InvalidOperationException("Conteudo nao possui raiz OFX valida");
            }

            List<LancamentoImportado> lancamentos = lerLancamentos(documento);
            return new ExtratoImportado(
                    texto(documento, "BANKID"),
                    texto(documento, "BRANCHID"),
                    texto(documento, "ACCTID"),
                    texto(documento, "CURDEF"),
                    dataObrigatoria(texto(documento, "DTSTART"), "DTSTART"),
                    dataObrigatoria(texto(documento, "DTEND"), "DTEND"),
                    BigDecimal.ZERO,
                    decimalObrigatorio(textoNoPrimeiro(documento, "LEDGERBAL", "BALAMT"), "BALAMT"),
                    List.copyOf(lancamentos));
        } catch (InvalidOperationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidOperationException("Nao foi possivel interpretar o arquivo OFX");
        }
    }

    private List<LancamentoImportado> lerLancamentos(Document documento) {
        NodeList nodes = documento.getElementsByTagName("STMTTRN");
        List<LancamentoImportado> resultado = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element transacao = (Element) nodes.item(i);
            String tipo = texto(transacao, "TRNTYPE");
            String descricao = primeiroNaoVazio(texto(transacao, "MEMO"), texto(transacao, "NAME"), tipo);
            resultado.add(new LancamentoImportado(
                    dataObrigatoria(texto(transacao, "DTPOSTED"), "DTPOSTED"),
                    texto(transacao, "FITID"),
                    tipo,
                    descricao,
                    primeiroNaoVazio(texto(transacao, "CHECKNUM"), texto(transacao, "REFNUM")),
                    decimalObrigatorio(texto(transacao, "TRNAMT"), "TRNAMT"),
                    null));
        }
        return resultado;
    }

    private String normalizarOfx(byte[] conteudo) {
        String preliminar = new String(conteudo, StandardCharsets.ISO_8859_1);
        Charset charset = preliminar.toUpperCase(Locale.ROOT).contains("CHARSET:1252")
                ? Charset.forName("windows-1252") : StandardCharsets.UTF_8;
        String texto = new String(conteudo, charset).replace("\uFEFF", "");
        int inicio = texto.toUpperCase(Locale.ROOT).indexOf("<OFX");
        if (inicio < 0) {
            throw new InvalidOperationException("Conteudo nao parece ser um arquivo OFX");
        }
        String xml = texto.substring(inicio).trim();
        xml = xml.replaceAll("&(?!amp;|lt;|gt;|quot;|apos;|#[0-9]+;)", "&amp;");
        for (String tag : LEAF_TAGS) {
            Pattern pattern = Pattern.compile("(?is)<" + tag
                    + ">([^<\\r\\n]+?)(\\s*)(?=<(?!/" + tag + ">))");
            xml = pattern.matcher(xml).replaceAll("<" + tag + ">$1</" + tag + ">$2");
        }
        return xml;
    }

    private DocumentBuilderFactory criarFactorySegura() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    private String texto(Document document, String tag) {
        NodeList nodes = document.getElementsByTagName(tag);
        return nodes.getLength() == 0 ? null : limpar(nodes.item(0).getTextContent());
    }

    private String textoNoPrimeiro(Document document, String container, String tag) {
        NodeList containers = document.getElementsByTagName(container);
        return containers.getLength() == 0 ? texto(document, tag) : texto((Element) containers.item(0), tag);
    }

    private String texto(Element element, String tag) {
        NodeList nodes = element.getElementsByTagName(tag);
        return nodes.getLength() == 0 ? null : limpar(nodes.item(0).getTextContent());
    }

    private LocalDate dataObrigatoria(String valor, String campo) {
        if (valor == null) {
            throw new InvalidOperationException("Campo OFX obrigatorio ausente: " + campo);
        }
        Matcher matcher = Pattern.compile("^(\\d{4})(\\d{2})(\\d{2})").matcher(valor.trim());
        if (!matcher.find()) {
            throw new InvalidOperationException("Data OFX invalida em " + campo);
        }
        return LocalDate.of(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)));
    }

    private BigDecimal decimalObrigatorio(String valor, String campo) {
        if (valor == null) {
            throw new InvalidOperationException("Campo OFX obrigatorio ausente: " + campo);
        }
        try {
            return new BigDecimal(valor.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new InvalidOperationException("Valor OFX invalido em " + campo);
        }
    }

    private String primeiroNaoVazio(String... valores) {
        for (String valor : valores) {
            if (valor != null && !valor.isBlank()) {
                return valor;
            }
        }
        return "Lancamento bancario";
    }

    private String limpar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
