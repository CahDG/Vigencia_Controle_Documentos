package repositorio;

import modelos.Empresa;
import util.JsonUtil;
import java.util.ArrayList;
import java.util.List;

public class EmpresaRepositorio {

    // CAMINHO DO ARQUIVO JSON
    private static final String CAMINHO = "json/empresas.json";

    // LISTA TODAS AS EMPRESAS DO ARQUIVO JSON
    public List<Empresa> listarTodas() {
        List<Empresa> empresas = new ArrayList<>();
        String json = JsonUtil.lerArquivo(CAMINHO);

        for (String obj : JsonUtil.separarObjetos(json)) {
            Empresa e = new Empresa();
            e.setId(Integer.parseInt(JsonUtil.extrairValor(obj, "id")));
            e.setNome(JsonUtil.extrairValor(obj, "nome"));
            e.setCnpj(JsonUtil.extrairValor(obj, "cnpj"));
            e.setSegmento(JsonUtil.extrairValor(obj, "segmento"));
            e.setCidade(JsonUtil.extrairValor(obj, "cidade"));
            e.setUf(JsonUtil.extrairValor(obj, "uf"));
            empresas.add(e);
        }
        return empresas;
    }

    // SALVA A LISTA COMPLETA NO ARQUIVO JSON
    public void salvarTodas(List<Empresa> empresas) {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < empresas.size(); i++) {
            Empresa e = empresas.get(i);
            json.append("  {\n");
            json.append("    \"id\": ").append(e.getId()).append(",\n");
            json.append("    \"nome\": \"").append(e.getNome()).append("\",\n");
            json.append("    \"cnpj\": \"").append(e.getCnpj()).append("\",\n");
            json.append("    \"segmento\": \"").append(e.getSegmento()).append("\",\n");
            json.append("    \"cidade\": \"").append(e.getCidade()).append("\",\n");
            json.append("    \"uf\": \"").append(e.getUf()).append("\"\n");
            json.append("  }");
            if (i < empresas.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("]");
        JsonUtil.salvarArquivo(CAMINHO, json.toString());
    }

    // ADICIONA UMA NOVA EMPRESA
    public void adicionar(Empresa empresa) {
        List<Empresa> empresas = listarTodas();
        empresa.setId(gerarNovoId(empresas));
        empresas.add(empresa);
        salvarTodas(empresas);
    }

    // EDITA UMA EMPRESA EXISTENTE
    public void editar(Empresa empresa) {
        List<Empresa> empresas = listarTodas();
        for (int i = 0; i < empresas.size(); i++) {
            if (empresas.get(i).getId() == empresa.getId()) {
                empresas.set(i, empresa);
                break;
            }
        }
        salvarTodas(empresas);
    }

    // DELETA UMA EMPRESA PELO ID
    public void deletar(int id) {
        List<Empresa> empresas = listarTodas();
        empresas.removeIf(e -> e.getId() == id);
        salvarTodas(empresas);
    }

    // GERA UM NOVO ID UNICO
    private int gerarNovoId(List<Empresa> empresas) {
        int maiorId = 0;
        for (Empresa e : empresas) {
            if (e.getId() > maiorId) maiorId = e.getId();
        }
        return maiorId + 1;
    }
}