package repositorio;

import java.util.ArrayList;
import java.util.List;
import modelos.TipoDocumento;
import util.JsonUtil;

public class TipoDocumentoRepositorio {

    // CAMINHO DO ARQUIVO JSON
    private static final String CAMINHO = "json/tipos.json";

    // LISTA TODOS OS TIPOS DO ARQUIVO JSON
    public List<TipoDocumento> listarTodos() {
        List<TipoDocumento> tipos = new ArrayList<>();
        String json = JsonUtil.lerArquivo(CAMINHO);

        for (String obj : JsonUtil.separarObjetos(json)) {
            TipoDocumento t = new TipoDocumento();
            t.setId(Integer.parseInt(JsonUtil.extrairValor(obj, "id")));
            t.setNome(JsonUtil.extrairValor(obj, "nome"));
            t.setDiasAntecedenciaPadrao(Integer.parseInt(JsonUtil.extrairValor(obj, "diasAntecedenciaPadrao")));
            tipos.add(t);
        }
        return tipos;
    }

    // SALVA A LISTA COMPLETA NO ARQUIVO JSON
    public void salvarTodos(List<TipoDocumento> tipos) {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < tipos.size(); i++) {
            TipoDocumento t = tipos.get(i);
            json.append("  {\n");
            json.append("    \"id\": ").append(t.getId()).append(",\n");
            json.append("    \"nome\": \"").append(t.getNome()).append("\",\n");
            json.append("    \"diasAntecedenciaPadrao\": ").append(t.getDiasAntecedenciaPadrao()).append("\n");
            json.append("  }");
            if (i < tipos.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("]");
        JsonUtil.salvarArquivo(CAMINHO, json.toString());
    }

    // ADICIONA UM NOVO TIPO
    public void adicionar(TipoDocumento tipo) {
        List<TipoDocumento> tipos = listarTodos();
        tipo.setId(gerarNovoId(tipos));
        tipos.add(tipo);
        salvarTodos(tipos);
    }

    // EDITA UM TIPO EXISTENTE
    public void editar(TipoDocumento tipo) {
        List<TipoDocumento> tipos = listarTodos();
        for (int i = 0; i < tipos.size(); i++) {
            if (tipos.get(i).getId() == tipo.getId()) {
                tipos.set(i, tipo);
                break;
            }
        }
        salvarTodos(tipos);
    }

    // DELETA UM TIPO PELO ID
    public void deletar(int id) {
        List<TipoDocumento> tipos = listarTodos();
        tipos.removeIf(t -> t.getId() == id);
        salvarTodos(tipos);
    }

    // GERA UM NOVO ID UNICO
    private int gerarNovoId(List<TipoDocumento> tipos) {
        int maiorId = 0;
        for (TipoDocumento t : tipos) {
            if (t.getId() > maiorId) maiorId = t.getId();
        }
        return maiorId + 1;
    }
}