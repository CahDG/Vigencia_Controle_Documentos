package app;

import modelos.Documento;
import modelos.Renovacao;
import repositorio.DocumentoRepositorio;
import util.StatusUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class FormularioRenovacao extends JDialog {

    // CORES E FONTE DO FORMULARIO
    private static final Color COR_SIDEBAR  = new Color(5, 32, 74);
    private static final Color COR_VERDE    = new Color(22, 163, 74);
    private static final Color COR_VERMELHO = new Color(180, 30, 30);
    private static final String FONTE       = "Segoe UI";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private DocumentoRepositorio repositorio;
    private Documento documento;

    // CAMPOS COM MASCARA AUTOMATICA DE DATA
    private JTextField campoDataRenovacao  = criarCampoData();
    private JTextField campoNovoVencimento = criarCampoData();

    public FormularioRenovacao(JFrame pai, Documento documento, DocumentoRepositorio repositorio) {
        super(pai, "Renovar Documento", true);
        this.documento   = documento;
        this.repositorio = repositorio;
        initialize();
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
        JLabel titulo = new JLabel("Renovar: " + documento.getNome());
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font(FONTE, Font.BOLD, 15));
        cabecalho.add(titulo, BorderLayout.WEST);

        // VENCIMENTO ATUAL
        String vencAtual = documento.getVencimentoAtual() != null
            ? documento.getVencimentoAtual().format(FMT) : "—";

        // PREENCHE DATA DE RENOVACAO COM HOJE AUTOMATICAMENTE
        campoDataRenovacao.setText(LocalDate.now().format(FMT));

        // PAINEL DOS CAMPOS
        JPanel campos = new JPanel(new GridLayout(6, 1, 0, 8));
        campos.setBackground(Color.WHITE);
        campos.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        JLabel lblVencAtual = new JLabel("Vencimento atual: " + vencAtual);
        lblVencAtual.setFont(new Font(FONTE, Font.PLAIN, 13));
        lblVencAtual.setForeground(new Color(100, 110, 130));

        campos.add(lblVencAtual);
        campos.add(criarLabel("Data da Renovação (dd/MM/yyyy) *"));
        campos.add(campoDataRenovacao);
        campos.add(criarLabel("Novo Vencimento (dd/MM/yyyy) *"));
        campos.add(campoNovoVencimento);
        campos.add(new JLabel());

        // BOTOES
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

        JButton btnSalvar = new JButton("Confirmar Renovação");
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

    private void salvar() {
        // BLOQUEIA RENOVACAO SE O DOCUMENTO AINDA ESTIVER VALIDO
        if (documento.getVencimentoAtual() != null) {
            String status = StatusUtil.calcularStatus(documento.getVencimentoAtual(), documento.getDiasAntecedencia());
            if ("Válido".equals(status)) {
                mostrarErro("Este documento ainda está válido e não pode ser renovado.\nA renovação só é permitida quando o status for 'A Vencer' ou 'Vencido'.");
                return;
            }
        }

        // VALIDA DATA DA RENOVACAO
        LocalDate dataRenovacao;
        try {
            dataRenovacao = LocalDate.parse(campoDataRenovacao.getText().trim(), FMT);
        } catch (DateTimeParseException ex) {
            mostrarErro("Data da renovação inválida. Use o formato dd/MM/yyyy."); return;
        }

        // VALIDA NOVO VENCIMENTO
        LocalDate novoVencimento;
        try {
            novoVencimento = LocalDate.parse(campoNovoVencimento.getText().trim(), FMT);
        } catch (DateTimeParseException ex) {
            mostrarErro("Novo vencimento inválido. Use o formato dd/MM/yyyy."); return;
        }

        // VERIFICA SE O NOVO VENCIMENTO E POSTERIOR AO ATUAL
        if (documento.getVencimentoAtual() != null &&
                !novoVencimento.isAfter(documento.getVencimentoAtual())) {
            mostrarErro("O novo vencimento deve ser posterior ao vencimento atual."); return;
        }

        // REGISTRA A RENOVACAO NO HISTORICO DO DOCUMENTO
        Renovacao renovacao = new Renovacao();
        renovacao.setDataRenovacao(dataRenovacao);
        renovacao.setNovoVencimento(novoVencimento);
        documento.getRenovacoes().add(renovacao);

        repositorio.editar(documento);
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

    // CAMPO DE DATA COM MASCARA AUTOMATICA DD/MM/YYYY
    private static JTextField criarCampoData() {
        JTextField campo = new JTextField();
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campo.setPreferredSize(new Dimension(400, 34));
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 225)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // MASCARA QUE INSERE AS BARRAS AUTOMATICAMENTE CONFORME O USUARIO DIGITA
        campo.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            boolean atualizando = false;
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { formatar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  {}
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
            private void formatar() {
                if (atualizando) return;
                SwingUtilities.invokeLater(() -> {
                    atualizando = true;
                    String texto = campo.getText().replaceAll("[^0-9]", "");
                    if (texto.length() > 8) texto = texto.substring(0, 8);
                    StringBuilder sb = new StringBuilder(texto);
                    if (sb.length() > 2) sb.insert(2, "/");
                    if (sb.length() > 5) sb.insert(5, "/");
                    campo.setText(sb.toString());
                    atualizando = false;
                });
            }
        });

        return campo;
    }
}