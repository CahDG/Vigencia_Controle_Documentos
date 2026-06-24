package util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LocalidadeUtil {

    // CAMINHOS DOS ARQUIVOS DE REFERENCIA DENTRO DO PROJETO
    private static final String ARQUIVO_ESTADOS    = "/dados/estados.json";
    private static final String ARQUIVO_MUNICIPIOS = "/dados/municipios.json";

    // CACHE EM MEMORIA PARA NAO RELER O ARQUIVO A CADA CHAMADA
    private static Map<String, String> cacheEstados    = null;
    private static Map<String, String> cacheMunicipios = null;

    // LE O CONTEUDO DE UM ARQUIVO DE RECURSOS INTERNO DO PROJETO
    private static String lerResource(String caminho) {
        try (InputStream is = LocalidadeUtil.class.getResourceAsStream(caminho)) {
            if (is == null) {
                System.err.println("ARQUIVO NAO ENCONTRADO: " + caminho);
                return "{}";
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    // CONVERTE SEQUENCIAS UNICODE ESCAPADAS PARA CARACTERES REAIS
    // EXEMPLO: C\u00e1ceres VIRA Caceres COM ACENTO
    private static String converterUnicode(String texto) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < texto.length()) {
            if (i + 5 < texto.length()
                    && texto.charAt(i) == '\\'
                    && texto.charAt(i + 1) == 'u') {
                String hex = texto.substring(i + 2, i + 6);
                try {
                    sb.append((char) Integer.parseInt(hex, 16));
                    i += 6;
                } catch (NumberFormatException e) {
                    sb.append(texto.charAt(i));
                    i++;
                }
            } else {
                sb.append(texto.charAt(i));
                i++;
            }
        }
        return sb.toString();
    }

    // CARREGA OS ESTADOS DO JSON — RETORNA MAPA DE CODIGO -> SIGLA
    // EXEMPLO: "51" -> "MT"
    private static Map<String, String> carregarEstados() {
        if (cacheEstados != null) return cacheEstados;

        cacheEstados = new LinkedHashMap<>();
        String json = lerResource(ARQUIVO_ESTADOS);

        // FORMATO DO ARQUIVO: {"51":{"sigla":"MT","nome":"Mato Grosso"}, ...}
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}"))   json = json.substring(0, json.length() - 1);

        int i = 0;
        while (i < json.length()) {
            // LOCALIZA A CHAVE (CODIGO DO ESTADO)
            int inicioChave = json.indexOf('"', i);
            if (inicioChave == -1) break;
            int fimChave = json.indexOf('"', inicioChave + 1);
            String codigo = json.substring(inicioChave + 1, fimChave);

            // LOCALIZA O OBJETO DE VALOR {"sigla":...,"nome":...}
            int inicioObj = json.indexOf('{', fimChave);
            if (inicioObj == -1) break;
            int fimObj = json.indexOf('}', inicioObj);
            String obj = json.substring(inicioObj, fimObj + 1);

            // EXTRAI A SIGLA DO ESTADO
            String sigla = JsonUtil.extrairValor(obj, "sigla");
            if (!sigla.isEmpty()) {
                cacheEstados.put(codigo, sigla);
            }

            i = fimObj + 1;
        }

        return cacheEstados;
    }

    // CARREGA OS MUNICIPIOS DO JSON — RETORNA MAPA DE CODIGO -> NOME
    // EXEMPLO: "5100250" -> "Acorizal"
    private static Map<String, String> carregarMunicipios() {
        if (cacheMunicipios != null) return cacheMunicipios;

        cacheMunicipios = new LinkedHashMap<>();
        String json = lerResource(ARQUIVO_MUNICIPIOS);

        // FORMATO DO ARQUIVO: {"1100015":"Alta Floresta D'oeste", ...}
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}"))   json = json.substring(0, json.length() - 1);

        // DIVIDE NAS ENTRADAS — CADA ENTRADA TEM O FORMATO "codigo":"nome"
        String[] partes = json.split(",(?=\")");
        for (String parte : partes) {
            parte = parte.trim();
            int sep = parte.indexOf(':');
            if (sep == -1) continue;

            String codigo = parte.substring(0, sep).replace("\"", "").trim();
            String nome   = parte.substring(sep + 1).replace("\"", "").trim();

            if (!codigo.isEmpty() && !nome.isEmpty()) {
                // CONVERTE UNICODE ESCAPADO PARA CARACTERES COM ACENTO
                cacheMunicipios.put(codigo, converterUnicode(nome));
            }
        }

        return cacheMunicipios;
    }

    // RETORNA A LISTA DE SIGLAS DOS ESTADOS EM ORDEM ALFABETICA
    // EXEMPLO: ["AC", "AL", "AM", ...]
    public static List<String> listarSiglasEstados() {
        List<String> siglas = new ArrayList<>(carregarEstados().values());
        Collections.sort(siglas);
        return siglas;
    }

    // RETORNA O CODIGO DO ESTADO A PARTIR DA SIGLA
    // EXEMPLO: "MT" -> "51"
    public static String codigoPorSigla(String sigla) {
        for (Map.Entry<String, String> entry : carregarEstados().entrySet()) {
            if (entry.getValue().equals(sigla)) return entry.getKey();
        }
        return null;
    }

    // RETORNA O NOME DO MUNICIPIO A PARTIR DO CODIGO
    // EXEMPLO: "5100250" -> "Acorizal"
    public static String nomePorCodigoMunicipio(String codigo) {
        return carregarMunicipios().get(codigo);
    }

    // RETORNA A LISTA DE MUNICIPIOS DE UM ESTADO FILTRADO PELA SIGLA
    // USA OS 2 PRIMEIROS DIGITOS DO CODIGO DO MUNICIPIO PARA FILTRAR
    // EXEMPLO: listarMunicipios("MT") -> ["Acorizal", "Alta Floresta", ...]
    public static List<String> listarMunicipios(String sigla) {
        String codigoEstado = codigoPorSigla(sigla);
        if (codigoEstado == null) return new ArrayList<>();

        List<String> municipios = new ArrayList<>();
        for (Map.Entry<String, String> entry : carregarMunicipios().entrySet()) {
            if (entry.getKey().startsWith(codigoEstado)) {
                municipios.add(entry.getValue());
            }
        }

        Collections.sort(municipios);
        return municipios;
    }
}