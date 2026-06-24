package modelos;

import java.time.LocalDate;

public class Renovacao {

    // ATRIBUTOS
    private LocalDate dataRenovacao;
    private LocalDate novoVencimento;

    // CONSTRUTOR VAZIO
    public Renovacao() {}

    // CONSTRUTOR COMPLETO
    public Renovacao(LocalDate dataRenovacao, LocalDate novoVencimento) {
        this.dataRenovacao = dataRenovacao;
        this.novoVencimento = novoVencimento;
    }

    // GETTERS E SETTERS
    public LocalDate getDataRenovacao() { return dataRenovacao; }
    public void setDataRenovacao(LocalDate dataRenovacao) { this.dataRenovacao = dataRenovacao; }

    public LocalDate getNovoVencimento() { return novoVencimento; }
    public void setNovoVencimento(LocalDate novoVencimento) { this.novoVencimento = novoVencimento; }
}