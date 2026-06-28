package repositorio;

import modelos.Documento;
import modelos.Renovacao;
import util.JsonUtil;
import java.util.ArrayList;
import java.util.List;

public class DocumentoRepositorio {

    private static final String CAMINHO = "json/documentos.json";

    public List<Documento> listarTodos() {
        List<Documento> documentos = new ArrayList<>();
        String json = JsonUtil.lerArquivo(CAMINHO);

        for (String obj : JsonUtil.separarObjetos(json)) {
            Documento d = new Documento();
            d.setId(Integer.parseInt(JsonUtil.extrairValor(obj, "id")));
            d.setNome(JsonUtil.extrairValor(obj, "nome"));

            // NUM_DOCUMENTO E OPCIONAL — PODE NAO EXISTIR EM REGISTROS ANTIGOS
            String numDoc = JsonUtil.extrairValor(obj, "numDocumento");
            d.setNumDocumento(numDoc.isEmpty() ? null : numDoc);

            d.setEmpresaId(Integer.parseInt(JsonUtil.extrairValor(obj, "empresaId")));
            d.setTipoId(Integer.parseInt(JsonUtil.extrairValor(obj, "tipoId")));
            d.setDataEmissao(JsonUtil.stringParaDate(JsonUtil.extrairValor(obj, "dataEmissao")));
            d.setDataVencimento(JsonUtil.stringParaDate(JsonUtil.extrairValor(obj, "dataVencimento")));
            d.setDiasAntecedencia(Integer.parseInt(JsonUtil.extrairValor(obj, "diasAntecedencia")));
            d.setObservacoes(JsonUtil.extrairValor(obj, "observacoes"));
            d.setRenovacoes(extrairRenovacoes(obj));
            documentos.add(d);
        }
        return documentos;
    }

    private List<Renovacao> extrairRenovacoes(String obj) {
        List<Renovacao> renovacoes = new ArrayList<>();
        int inicio = obj.indexOf("\"renovacoes\":");
        if (inicio == -1) return renovacoes;

        inicio = obj.indexOf("[", inicio);
        int fim = obj.indexOf("]", inicio);
        if (inicio == -1 || fim == -1) return renovacoes;

        String arrayRenovacoes = obj.substring(inicio, fim + 1);

        for (String r : JsonUtil.separarObjetos(arrayRenovacoes)) {
            Renovacao renovacao = new Renovacao();
            renovacao.setDataRenovacao(JsonUtil.stringParaDate(JsonUtil.extrairValor(r, "dataRenovacao")));
            renovacao.setNovoVencimento(JsonUtil.stringParaDate(JsonUtil.extrairValor(r, "novoVencimento")));
            renovacoes.add(renovacao);
        }
        return renovacoes;
    }

    public void salvarTodos(List<Documento> documentos) {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < documentos.size(); i++) {
            Documento d = documentos.get(i);
            json.append("  {\n");
            json.append("    \"id\": ").append(d.getId()).append(",\n");
            json.append("    \"nome\": \"").append(d.getNome()).append("\",\n");

            // NUM_DOCUMENTO E OPCIONAL — SALVA VAZIO SE NULO
            json.append("    \"numDocumento\": \"").append(d.getNumDocumento() != null ? d.getNumDocumento() : "").append("\",\n");

            json.append("    \"empresaId\": ").append(d.getEmpresaId()).append(",\n");
            json.append("    \"tipoId\": ").append(d.getTipoId()).append(",\n");
            json.append("    \"dataEmissao\": \"").append(JsonUtil.dateParaString(d.getDataEmissao())).append("\",\n");
            json.append("    \"dataVencimento\": \"").append(JsonUtil.dateParaString(d.getDataVencimento())).append("\",\n");
            json.append("    \"diasAntecedencia\": ").append(d.getDiasAntecedencia()).append(",\n");
            json.append("    \"observacoes\": \"").append(d.getObservacoes()).append("\",\n");
            json.append("    \"renovacoes\": ").append(construirJsonRenovacoes(d.getRenovacoes())).append("\n");
            json.append("  }");
            if (i < documentos.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("]");
        JsonUtil.salvarArquivo(CAMINHO, json.toString());
    }

    private String construirJsonRenovacoes(List<Renovacao> renovacoes) {
        if (renovacoes == null || renovacoes.isEmpty()) return "[]";
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < renovacoes.size(); i++) {
            Renovacao r = renovacoes.get(i);
            json.append("      {\n");
            json.append("        \"dataRenovacao\": \"").append(JsonUtil.dateParaString(r.getDataRenovacao())).append("\",\n");
            json.append("        \"novoVencimento\": \"").append(JsonUtil.dateParaString(r.getNovoVencimento())).append("\"\n");
            json.append("      }");
            if (i < renovacoes.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("    ]");
        return json.toString();
    }

    public void adicionar(Documento documento) {
        List<Documento> documentos = listarTodos();
        documento.setId(gerarNovoId(documentos));
        documentos.add(documento);
        salvarTodos(documentos);
    }

    public void editar(Documento documento) {
        List<Documento> documentos = listarTodos();
        for (int i = 0; i < documentos.size(); i++) {
            if (documentos.get(i).getId() == documento.getId()) {
                documentos.set(i, documento);
                break;
            }
        }
        salvarTodos(documentos);
    }

    public void deletar(int id) {
        List<Documento> documentos = listarTodos();
        documentos.removeIf(d -> d.getId() == id);
        salvarTodos(documentos);
    }

    public void adicionarRenovacao(int documentoId, Renovacao renovacao) {
        List<Documento> documentos = listarTodos();
        for (Documento d : documentos) {
            if (d.getId() == documentoId) {
                d.getRenovacoes().add(renovacao);
                break;
            }
        }
        salvarTodos(documentos);
    }

    private int gerarNovoId(List<Documento> documentos) {
        int maiorId = 0;
        for (Documento d : documentos) {
            if (d.getId() > maiorId) maiorId = d.getId();
        }
        return maiorId + 1;
    }
}