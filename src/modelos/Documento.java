package modelos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Documento {

    // ATRIBUTOS
    private int id;
    private String nome;
    private String numDocumento;
    private int empresaId;
    private int tipoId;
    private LocalDate dataEmissao;
    private LocalDate dataVencimento;
    private int diasAntecedencia;
    private String observacoes;
    private List<Renovacao> renovacoes;

    // CONSTRUTOR VAZIO
    public Documento() {
        this.renovacoes = new ArrayList<>();
    }

    // CONSTRUTOR COMPLETO
    public Documento(int id, String nome, String numDocumento, int empresaId, int tipoId,
                     LocalDate dataEmissao, LocalDate dataVencimento,
                     int diasAntecedencia, String observacoes) {
        this.id = id;
        this.nome = nome;
        this.numDocumento = numDocumento;
        this.empresaId = empresaId;
        this.tipoId = tipoId;
        this.dataEmissao = dataEmissao;
        this.dataVencimento = dataVencimento;
        this.diasAntecedencia = diasAntecedencia;
        this.observacoes = observacoes;
        this.renovacoes = new ArrayList<>();
    }

    // GETTERS E SETTERS
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getNumDocumento() { return numDocumento; }
    public void setNumDocumento(String numDocumento) { this.numDocumento = numDocumento; }

    public int getEmpresaId() { return empresaId; }
    public void setEmpresaId(int empresaId) { this.empresaId = empresaId; }

    public int getTipoId() { return tipoId; }
    public void setTipoId(int tipoId) { this.tipoId = tipoId; }

    public LocalDate getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDate dataEmissao) { this.dataEmissao = dataEmissao; }

    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }

    public int getDiasAntecedencia() { return diasAntecedencia; }
    public void setDiasAntecedencia(int diasAntecedencia) { this.diasAntecedencia = diasAntecedencia; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public List<Renovacao> getRenovacoes() { return renovacoes; }
    public void setRenovacoes(List<Renovacao> renovacoes) { this.renovacoes = renovacoes; }

    // RETORNA O VENCIMENTO ATUAL (ULTIMA RENOVACAO OU VENCIMENTO ORIGINAL)
    public LocalDate getVencimentoAtual() {
        if (renovacoes != null && !renovacoes.isEmpty()) {
            return renovacoes.get(renovacoes.size() - 1).getNovoVencimento();
        }
        return dataVencimento;
    }

    // PARA EXIBIR O NOME NOS COMBOBOX DAS TELAS
    @Override
    public String toString() {
        return nome;
    }
}