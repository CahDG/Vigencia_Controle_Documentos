package app;

import modelos.TipoDocumento;
import repositorio.TipoDocumentoRepositorio;

import javax.swing.*;
import java.awt.*;

public class FormularioTipo extends JDialog {

    // CORES E FONTE DO FORMULARIO
    private static final Color COR_SIDEBAR  = new Color(5, 32, 74);
    private static final Color COR_VERDE    = new Color(22, 163, 74);
    private static final Color COR_VERMELHO = new Color(180, 30, 30);
    private static final String FONTE       = "Segoe UI";

    private TipoDocumentoRepositorio repositorio;
    private TipoDocumento tipo;

    // CAMPOS DO FORMULARIO
    private JTextField campoNome = criarCampo();
    private JSpinner   spinnerDias;

    public FormularioTipo(JFrame pai, TipoDocumento tipo, TipoDocumentoRepositorio repositorio) {
        super(pai, tipo == null ? "Novo Tipo de Documento" : "Editar Tipo de Documento", true);
        this.tipo        = tipo;
        this.repositorio = repositorio;
        initialize();
        if (tipo != null) preencherCampos();
        this.setVisible(true);
    }

    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setResizable(false);
        this.getContentPane().setBackground(Color.WHITE);

        // CABECALHO AZUL ESCURO
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(COR_SIDEBAR);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel titulo = new JLabel(tipo == null ? "Novo Tipo de Documento" : "Editar Tipo de Documento");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font(FONTE, Font.BOLD, 16));
        cabecalho.add(titulo, BorderLayout.WEST);

        // SPINNER DE DIAS — MINIMO 1, MAXIMO 365, PADRAO 30
        spinnerDias = new JSpinner(new SpinnerNumberModel(30, 1, 365, 1));
        spinnerDias.setFont(new Font(FONTE, Font.PLAIN, 13));
        JComponent editor = spinnerDias.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setFont(new Font(FONTE, Font.PLAIN, 13));
        }

        // PAINEL DOS CAMPOS
        JPanel campos = new JPanel(new GridLayout(4, 1, 0, 8));
        campos.setBackground(Color.WHITE);
        campos.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        campos.add(criarLabel("Nome do Tipo *"));
        campos.add(campoNome);
        campos.add(criarLabel("Dias de Antecedência para Alerta *"));
        campos.add(spinnerDias);

        // BOTOES — CANCELAR ESQUERDA VERMELHO, SALVAR DIREITA VERDE
        JPanel botoes = new JPanel(new BorderLayout());
        botoes.setBackground(Color.WHITE);
        botoes.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 235)),
            BorderFactory.createEmptyBorder(14, 20, 14, 20)
        ));

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font(FONTE, Font.BOLD, 13));
        btnCancelar.setBackground(COR_VERMELHO);
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setBorder(BorderFactory.createEmptyBorder(8, 22, 8, 22));
        btnCancelar.setFocusPainted(false);
        btnCancelar.setOpaque(true);
        btnCancelar.setBorderPainted(false);
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> this.dispose());

        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.setFont(new Font(FONTE, Font.BOLD, 13));
        btnSalvar.setBackground(COR_VERDE);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setBorder(BorderFactory.createEmptyBorder(8, 22, 8, 22));
        btnSalvar.setFocusPainted(false);
        btnSalvar.setOpaque(true);
        btnSalvar.setBorderPainted(false);
        btnSalvar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalvar.addActionListener(e -> salvar());

        botoes.add(btnCancelar, BorderLayout.WEST);
        botoes.add(btnSalvar,   BorderLayout.EAST);

        this.add(cabecalho, BorderLayout.NORTH);
        this.add(campos,    BorderLayout.CENTER);
        this.add(botoes,    BorderLayout.SOUTH);

        this.pack();
        this.setLocationRelativeTo(getParent());
    }

    // PREENCHE OS CAMPOS QUANDO FOR EDICAO
    private void preencherCampos() {
        campoNome.setText(tipo.getNome());
        spinnerDias.setValue(tipo.getDiasAntecedenciaPadrao());
    }

    // VALIDA E SALVA
    private void salvar() {
        if (campoNome.getText().trim().isEmpty()) {
            mostrarErro("O nome é obrigatório."); return;
        }

        // VERIFICA SE JA EXISTE OUTRO TIPO COM O MESMO NOME
        String nomeDigitado = campoNome.getText().trim();
        for (TipoDocumento t : repositorio.listarTodos()) {
            boolean mesmoNome = t.getNome().equalsIgnoreCase(nomeDigitado);
            boolean outroTipo = tipo == null || t.getId() != tipo.getId();
            if (mesmoNome && outroTipo) {
                mostrarErro("Já existe um tipo com esse nome."); return;
            }
        }

        if (tipo == null) tipo = new TipoDocumento();
        tipo.setNome(campoNome.getText().trim());
        tipo.setDiasAntecedenciaPadrao((Integer) spinnerDias.getValue());

        // SALVA OU EDITA DEPENDENDO SE E UM TIPO NOVO OU EXISTENTE
        if (tipo.getId() == 0) {
            repositorio.adicionar(tipo);
        } else {
            repositorio.editar(tipo);
        }

        this.dispose();
    }

    private JLabel criarLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font(FONTE, Font.BOLD, 12));
        lbl.setForeground(new Color(60, 70, 90));
        return lbl;
    }

    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    // CRIA UM CAMPO DE TEXTO COM ESTILO PADRAO DO SISTEMA
    private static JTextField criarCampo() {
        JTextField campo = new JTextField();
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campo.setPreferredSize(new Dimension(400, 34));
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 225)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return campo;
    }
}