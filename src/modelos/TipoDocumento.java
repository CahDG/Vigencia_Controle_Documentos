package modelos;

public class TipoDocumento {

    // ATRIBUTOS
    private int id;
    private String nome;
    private int diasAntecedenciaPadrao;

    // CONSTRUTOR VAZIO
    public TipoDocumento() {}

    // CONSTRUTOR COMPLETO
    public TipoDocumento(int id, String nome, int diasAntecedenciaPadrao) {
        this.id = id;
        this.nome = nome;
        this.diasAntecedenciaPadrao = diasAntecedenciaPadrao;
    }

    // GETTERS E SETTERS
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getDiasAntecedenciaPadrao() { return diasAntecedenciaPadrao; }
    public void setDiasAntecedenciaPadrao(int diasAntecedenciaPadrao) {
        this.diasAntecedenciaPadrao = diasAntecedenciaPadrao;
    }

    // PARA EXIBIR O NOME NOS COMBOBOX DAS TELAS
    @Override
    public String toString() {
        return nome;
    }
}