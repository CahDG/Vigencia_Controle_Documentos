package modelos;

public class Empresa {

    // ATRIBUTOS
    private int id;
    private String nome;
    private String cnpj;
    private String segmento;
    private String cidade;
    private String uf;

    // CONSTRUTOR VAZIO
    public Empresa() {}

    // CONSTRUTOR COMPLETO
    public Empresa(int id, String nome, String cnpj, String segmento, String cidade, String uf) {
        this.id = id;
        this.nome = nome;
        this.cnpj = cnpj;
        this.segmento = segmento;
        this.cidade = cidade;
        this.uf = uf;
    }

    // GETTERS E SETTERS
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getSegmento() { return segmento; }
    public void setSegmento(String segmento) { this.segmento = segmento; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    // PARA EXIBIR O NOME NOS COMBOBOX DAS TELAS
    @Override
    public String toString() {
        return nome;
    }
}