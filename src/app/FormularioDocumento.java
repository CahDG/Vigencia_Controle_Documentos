package app;

import modelos.Documento;
import modelos.Empresa;
import modelos.Renovacao;
import modelos.TipoDocumento;
import repositorio.DocumentoRepositorio;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class FormularioDocumento extends JDialog {

    private static final Color COR_SIDEBAR  = new Color(5, 32, 74);
    private static final Color COR_VERDE    = new Color(22, 163, 74);
    private static final Color COR_VERMELHO = new Color(180, 30, 30);
    private static final Color COR_BORDA    = new Color(200, 210, 225);
    private static final Color COR_FUNDO_D  = new Color(250, 251, 252);
    private static final String FONTE       = "Segoe UI";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private DocumentoRepositorio repositorio;
    private Documento documento;
    private List<Empresa> empresas;
    private List<TipoDocumento> tipos;

    // CAMPOS DO FORMULARIO — LIMITES CONFORME DER LOGICO
    private JTextField campoNome          = criarCampo(255);  // DESCRICAO: VARCHAR(255)
    private JTextField campoNumDocumento  = criarCampo(100);  // NUM_DOCUMENTO: VARCHAR(100)
    private JTextField campoEmissao       = criarCampoData();
    private JTextField campoVencimento    = criarCampoData();
    private JSpinner   spinnerDias;
    private JTextArea  campoObservacoes   = criarAreaTexto(500); // OBSERVACAO: VARCHAR(500)
    private JCheckBox  checkSemVencimento = new JCheckBox("Sem vencimento");
    private JComboBox<String> comboEmpresa = new JComboBox<>();
    private JComboBox<String> comboTipo    = new JComboBox<>();

    public FormularioDocumento(JFrame pai, Documento documento, DocumentoRepositorio repositorio,
                               List<Empresa> empresas, List<TipoDocumento> tipos) {
        super(pai, documento == null ? "Novo Documento" : "Editar Documento", true);
        this.documento   = documento;
        this.repositorio = repositorio;
        this.empresas    = empresas;
        this.tipos       = tipos;
        initialize();
        if (documento != null) preencherCampos();
        this.setVisible(true);
    }

    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setResizable(false);
        this.getContentPane().setBackground(Color.WHITE);

        // CABECALHO
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(COR_SIDEBAR);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));
        JLabel titulo = new JLabel(documento == null ? "Novo Documento" : "Editar Documento");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font(FONTE, Font.BOLD, 16));
        cabecalho.add(titulo, BorderLayout.WEST);

        // SPINNER DE DIAS
        spinnerDias = new JSpinner(new SpinnerNumberModel(30, 1, 365, 1));
        spinnerDias.setFont(new Font(FONTE, Font.PLAIN, 13));
        JComponent ed = spinnerDias.getEditor();
        if (ed instanceof JSpinner.DefaultEditor)
            ((JSpinner.DefaultEditor) ed).getTextField().setFont(new Font(FONTE, Font.PLAIN, 13));

        // COMBOS
        comboEmpresa.setFont(new Font(FONTE, Font.PLAIN, 13));
        comboTipo.setFont(new Font(FONTE, Font.PLAIN, 13));
        comboEmpresa.addItem("Selecione a empresa...");
        for (Empresa e : empresas) comboEmpresa.addItem(e.getNome());
        comboTipo.addItem("Selecione o tipo...");
        for (TipoDocumento t : tipos) comboTipo.addItem(t.getNome());
        comboTipo.addActionListener(e -> {
            int idx = comboTipo.getSelectedIndex();
            if (idx > 0) spinnerDias.setValue(tipos.get(idx - 1).getDiasAntecedenciaPadrao());
        });

        // CHECKBOX SEM VENCIMENTO
        checkSemVencimento.setFont(new Font(FONTE, Font.PLAIN, 12));
        checkSemVencimento.setBackground(Color.WHITE);
        checkSemVencimento.addActionListener(e -> {
            campoVencimento.setEnabled(!checkSemVencimento.isSelected());
            if (checkSemVencimento.isSelected()) campoVencimento.setText("");
        });

        // ===== PAINEL ESQUERDO =====
        JPanel esquerdo = new JPanel();
        esquerdo.setLayout(new BoxLayout(esquerdo, BoxLayout.Y_AXIS));
        esquerdo.setBackground(Color.WHITE);
        esquerdo.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 18));
        esquerdo.setPreferredSize(new Dimension(460, 0));

        // NOME
        adicionarCampo(esquerdo, "Nome *", campoNome);
        esquerdo.add(Box.createVerticalStrut(12));

        // NUMERO DO DOCUMENTO
        adicionarCampo(esquerdo, "Número do Documento", campoNumDocumento);
        esquerdo.add(Box.createVerticalStrut(12));

        // EMPRESA
        adicionarCampo(esquerdo, "Empresa *", comboEmpresa);
        comboEmpresa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        esquerdo.add(Box.createVerticalStrut(12));

        // TIPO + DIAS NA MESMA LINHA
        JPanel linhaTipoDias = new JPanel(new BorderLayout(12, 0));
        linhaTipoDias.setBackground(Color.WHITE);
        linhaTipoDias.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaTipoDias.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        JPanel blocoTipo = new JPanel();
        blocoTipo.setLayout(new BoxLayout(blocoTipo, BoxLayout.Y_AXIS));
        blocoTipo.setBackground(Color.WHITE);
        blocoTipo.add(criarLabel("Tipo de Documento *"));
        blocoTipo.add(Box.createVerticalStrut(4));
        comboTipo.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboTipo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        blocoTipo.add(comboTipo);

        JPanel blocoDias = new JPanel();
        blocoDias.setLayout(new BoxLayout(blocoDias, BoxLayout.Y_AXIS));
        blocoDias.setBackground(Color.WHITE);
        blocoDias.setPreferredSize(new Dimension(150, 56));
        blocoDias.setMinimumSize(new Dimension(150, 56));
        blocoDias.setMaximumSize(new Dimension(150, 56));
        blocoDias.add(criarLabel("Dias de Antecedência *"));
        blocoDias.add(Box.createVerticalStrut(4));
        spinnerDias.setAlignmentX(Component.LEFT_ALIGNMENT);
        spinnerDias.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        blocoDias.add(spinnerDias);

        linhaTipoDias.add(blocoTipo, BorderLayout.CENTER);
        linhaTipoDias.add(blocoDias, BorderLayout.EAST);
        esquerdo.add(linhaTipoDias);
        esquerdo.add(Box.createVerticalStrut(12));

        // DATAS NA MESMA LINHA
        JPanel linhaDatas = new JPanel(new BorderLayout(12, 0));
        linhaDatas.setBackground(Color.WHITE);
        linhaDatas.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaDatas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        JPanel blocoEmissao = new JPanel();
        blocoEmissao.setLayout(new BoxLayout(blocoEmissao, BoxLayout.Y_AXIS));
        blocoEmissao.setBackground(Color.WHITE);
        blocoEmissao.setPreferredSize(new Dimension(195, 56));
        blocoEmissao.setMinimumSize(new Dimension(195, 56));
        blocoEmissao.setMaximumSize(new Dimension(195, 56));
        blocoEmissao.add(criarLabel("Data de Emissão *"));
        blocoEmissao.add(Box.createVerticalStrut(4));
        blocoEmissao.add(campoEmissao);

        JPanel blocoVencimento = new JPanel();
        blocoVencimento.setLayout(new BoxLayout(blocoVencimento, BoxLayout.Y_AXIS));
        blocoVencimento.setBackground(Color.WHITE);
        blocoVencimento.add(criarLabel("Data de Vencimento *"));
        blocoVencimento.add(Box.createVerticalStrut(4));
        blocoVencimento.add(campoVencimento);

        linhaDatas.add(blocoEmissao,   BorderLayout.WEST);
        linhaDatas.add(blocoVencimento, BorderLayout.CENTER);
        esquerdo.add(linhaDatas);
        esquerdo.add(Box.createVerticalStrut(4));

        // CHECKBOX ALINHADO COM DATA DE VENCIMENTO
        JPanel linhaCheck = new JPanel(new BorderLayout());
        linhaCheck.setBackground(Color.WHITE);
        linhaCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaCheck.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        JPanel espacoEsq = new JPanel();
        espacoEsq.setBackground(Color.WHITE);
        espacoEsq.setPreferredSize(new Dimension(207, 24));
        linhaCheck.add(espacoEsq, BorderLayout.WEST);
        checkSemVencimento.setBackground(Color.WHITE);
        linhaCheck.add(checkSemVencimento, BorderLayout.CENTER);
        esquerdo.add(linhaCheck);
        esquerdo.add(Box.createVerticalStrut(12));

        // OBSERVACOES
        adicionarCampo(esquerdo, "Observações", campoObservacoes);
        campoObservacoes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        // PAINEL DIREITO — SO HISTORICO
        JPanel direito = new JPanel();
        direito.setLayout(new BoxLayout(direito, BoxLayout.Y_AXIS));
        direito.setBackground(COR_FUNDO_D);
        direito.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, COR_BORDA),
            BorderFactory.createEmptyBorder(18, 16, 18, 24)
        ));

        if (documento != null) {
            JLabel lblHist = new JLabel("Histórico de Renovações");
            lblHist.setFont(new Font(FONTE, Font.BOLD, 14));
            lblHist.setForeground(COR_SIDEBAR);
            lblHist.setAlignmentX(Component.LEFT_ALIGNMENT);
            direito.add(lblHist);
            direito.add(Box.createVerticalStrut(10));

            String[] colunas = {"Data da Renovação", "Novo Vencimento"};
            DefaultTableModel modeloHist = new DefaultTableModel(colunas, 0) {
                public boolean isCellEditable(int row, int col) { return false; }
            };

            List<Renovacao> renovacoes = documento.getRenovacoes();
            if (renovacoes != null && !renovacoes.isEmpty()) {
                for (Renovacao r : renovacoes) {
                    modeloHist.addRow(new Object[]{
                        r.getDataRenovacao()  != null ? r.getDataRenovacao().format(FMT)  : "—",
                        r.getNovoVencimento() != null ? r.getNovoVencimento().format(FMT) : "—"
                    });
                }
            } else {
                modeloHist.addRow(new Object[]{"Nenhuma renovação registrada", ""});
            }

            JTable tabelaHist = new JTable(modeloHist);
            tabelaHist.setFont(new Font(FONTE, Font.PLAIN, 13));
            tabelaHist.setRowHeight(34);
            tabelaHist.setShowGrid(false);
            tabelaHist.setShowHorizontalLines(true);
            tabelaHist.setGridColor(new Color(240, 242, 245));
            tabelaHist.setBackground(Color.WHITE);
            tabelaHist.setIntercellSpacing(new Dimension(0, 0));
            tabelaHist.setFocusable(false);
            tabelaHist.setSelectionBackground(new Color(239, 246, 255));

            JTableHeader header = tabelaHist.getTableHeader();
            header.setFont(new Font(FONTE, Font.BOLD, 12));
            header.setBackground(new Color(249, 250, 251));
            header.setForeground(new Color(80, 90, 110));
            header.setReorderingAllowed(false);

            DefaultTableCellRenderer rendHist = new DefaultTableCellRenderer() {
                public Component getTableCellRendererComponent(JTable t, Object v,
                        boolean sel, boolean foc, int r, int c) {
                    super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                    setBackground(sel ? new Color(239, 246, 255) : Color.WHITE);
                    setForeground(new Color(15, 23, 42));
                    setFont(new Font(FONTE, Font.PLAIN, 13));
                    setHorizontalAlignment(CENTER);
                    return this;
                }
            };
            tabelaHist.getColumnModel().getColumn(0).setCellRenderer(rendHist);
            tabelaHist.getColumnModel().getColumn(1).setCellRenderer(rendHist);

            JScrollPane scrollHist = new JScrollPane(tabelaHist);
            scrollHist.setBorder(BorderFactory.createLineBorder(COR_BORDA));
            scrollHist.setAlignmentX(Component.LEFT_ALIGNMENT);
            scrollHist.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            direito.add(scrollHist);

        } else {
            direito.add(Box.createVerticalGlue());
            JLabel lblInfo = new JLabel("O histórico de renovações");
            lblInfo.setFont(new Font(FONTE, Font.PLAIN, 12));
            lblInfo.setForeground(new Color(150, 160, 175));
            lblInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel lblInfo2 = new JLabel("aparecerá após salvar.");
            lblInfo2.setFont(new Font(FONTE, Font.PLAIN, 12));
            lblInfo2.setForeground(new Color(150, 160, 175));
            lblInfo2.setAlignmentX(Component.LEFT_ALIGNMENT);
            direito.add(lblInfo);
            direito.add(lblInfo2);
            direito.add(Box.createVerticalGlue());
        }

        // PAINEL CENTRAL
        JPanel centro = new JPanel(new BorderLayout());
        centro.add(esquerdo, BorderLayout.WEST);
        centro.add(direito,  BorderLayout.CENTER);

        // BOTOES
        JPanel botoes = new JPanel(new BorderLayout());
        botoes.setBackground(Color.WHITE);
        botoes.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, COR_BORDA),
            BorderFactory.createEmptyBorder(14, 24, 14, 24)
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
        this.add(centro,    BorderLayout.CENTER);
        this.add(botoes,    BorderLayout.SOUTH);

        this.setSize(920, 620);
        this.setLocationRelativeTo(getParent());
    }

    private void adicionarCampo(JPanel painel, String labelTxt, JComponent comp) {
        JLabel lbl = criarLabel(labelTxt);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lbl);
        painel.add(Box.createVerticalStrut(4));
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(comp);
    }

    private void preencherCampos() {
        campoNome.setText(documento.getNome());

        if (documento.getNumDocumento() != null)
            campoNumDocumento.setText(documento.getNumDocumento());

        for (int i = 0; i < empresas.size(); i++) {
            if (empresas.get(i).getId() == documento.getEmpresaId()) {
                comboEmpresa.setSelectedIndex(i + 1); break;
            }
        }

        ActionListener[] listeners = comboTipo.getActionListeners();
        for (ActionListener l : listeners) comboTipo.removeActionListener(l);
        for (int i = 0; i < tipos.size(); i++) {
            if (tipos.get(i).getId() == documento.getTipoId()) {
                comboTipo.setSelectedIndex(i + 1); break;
            }
        }
        for (ActionListener l : listeners) comboTipo.addActionListener(l);

        if (documento.getDataEmissao() != null)
            campoEmissao.setText(documento.getDataEmissao().format(FMT));

        if (documento.getDataVencimento() == null) {
            checkSemVencimento.setSelected(true);
            campoVencimento.setEnabled(false);
        } else {
            campoVencimento.setText(documento.getVencimentoAtual().format(FMT));
        }

        spinnerDias.setValue(documento.getDiasAntecedencia());
        if (documento.getObservacoes() != null)
            campoObservacoes.setText(documento.getObservacoes());
    }

    private void salvar() {
        if (campoNome.getText().trim().isEmpty()) {
            mostrarErro("O nome é obrigatório."); return;
        }
        if (comboEmpresa.getSelectedIndex() == 0) {
            mostrarErro("Selecione a empresa."); return;
        }
        if (comboTipo.getSelectedIndex() == 0) {
            mostrarErro("Selecione o tipo de documento."); return;
        }

        // DATA DE EMISSAO OBRIGATORIA
        if (campoEmissao.getText().trim().isEmpty()) {
            mostrarErro("A data de emissão é obrigatória."); return;
        }
        LocalDate dataEmissao;
        try {
            dataEmissao = LocalDate.parse(campoEmissao.getText().trim(), FMT);
        } catch (DateTimeParseException ex) {
            mostrarErro("Data de emissão inválida. Use o formato dd/MM/yyyy."); return;
        }

        // DATA DE VENCIMENTO OBRIGATORIA A NAO SER QUE MARQUE SEM VENCIMENTO
        LocalDate dataVencimento = null;
        if (!checkSemVencimento.isSelected()) {
            if (campoVencimento.getText().trim().isEmpty()) {
                mostrarErro("A data de vencimento é obrigatória ou marque 'Sem vencimento'."); return;
            }
            try {
                dataVencimento = LocalDate.parse(campoVencimento.getText().trim(), FMT);
            } catch (DateTimeParseException ex) {
                mostrarErro("Data de vencimento inválida. Use o formato dd/MM/yyyy."); return;
            }
        }

        if (documento == null) documento = new Documento();
        documento.setNome(campoNome.getText().trim());
        documento.setNumDocumento(campoNumDocumento.getText().trim().isEmpty() ? null : campoNumDocumento.getText().trim());
        documento.setEmpresaId(empresas.get(comboEmpresa.getSelectedIndex() - 1).getId());
        documento.setTipoId(tipos.get(comboTipo.getSelectedIndex() - 1).getId());
        documento.setDataEmissao(dataEmissao);
        documento.setDataVencimento(dataVencimento);
        documento.setDiasAntecedencia((Integer) spinnerDias.getValue());
        documento.setObservacoes(campoObservacoes.getText().trim());

        if (documento.getId() == 0) {
            repositorio.adicionar(documento);
        } else {
            repositorio.editar(documento);
        }

        this.dispose();
    }

    private JLabel criarLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font(FONTE, Font.BOLD, 12));
        lbl.setForeground(new Color(74, 85, 104));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    // CRIA UM CAMPO DE TEXTO COM LIMITE DE CARACTERES CONFORME O DER LOGICO
    private static JTextField criarCampo(int limite) {
        JTextField campo = new JTextField();
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 225)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        // APLICA O FILTRO QUE IMPEDE DIGITAR ALEM DO LIMITE DEFINIDO NO DER
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string == null) return;
                if (fb.getDocument().getLength() + string.length() <= limite)
                    super.insertString(fb, offset, string, attr);
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null) return;
                if (fb.getDocument().getLength() - length + text.length() <= limite)
                    super.replace(fb, offset, length, text, attrs);
            }
        });
        return campo;
    }

    // CRIA UMA AREA DE TEXTO COM LIMITE DE CARACTERES CONFORME O DER LOGICO
    private static JTextArea criarAreaTexto(int limite) {
        JTextArea area = new JTextArea(6, 1);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 225)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        // APLICA O FILTRO QUE IMPEDE DIGITAR ALEM DO LIMITE DEFINIDO NO DER
        ((AbstractDocument) area.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string == null) return;
                if (fb.getDocument().getLength() + string.length() <= limite)
                    super.insertString(fb, offset, string, attr);
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null) return;
                if (fb.getDocument().getLength() - length + text.length() <= limite)
                    super.replace(fb, offset, length, text, attrs);
            }
        });
        return area;
    }

    private static JTextField criarCampoData() {
        JTextField campo = new JTextField();
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 225)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

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