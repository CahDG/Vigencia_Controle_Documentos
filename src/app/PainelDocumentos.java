package app;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import modelos.Documento;
import modelos.Empresa;
import modelos.TipoDocumento;
import repositorio.DocumentoRepositorio;
import repositorio.EmpresaRepositorio;
import repositorio.TipoDocumentoRepositorio;
import util.StatusUtil;

public class PainelDocumentos extends JPanel {

    // CORES DO SISTEMA
    private static final Color COR_FUNDO     = new Color(245, 247, 250);
    private static final Color COR_VERDE     = new Color(22, 163, 74);
    private static final Color COR_BORDA     = new Color(220, 225, 235);
    private static final Color COR_TEXTO_SEC = new Color(80, 90, 110);
    private static final Color COR_CINZA_BTN = new Color(230, 232, 236);

    // FONTE DO SISTEMA
    private static final String FONTE = "Segoe UI";

    // FORMATO DE DATA PADRAO
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // REFERENCIA PARA A JANELA PRINCIPAL
    private TelaPrincipal janela;

    // REPOSITORIOS
    private DocumentoRepositorio     docRepo     = new DocumentoRepositorio();
    private EmpresaRepositorio       empRepo     = new EmpresaRepositorio();
    private TipoDocumentoRepositorio tipoRepo    = new TipoDocumentoRepositorio();

    // COMPONENTES
    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField campoPesquisa;
    private JComboBox<String> comboEmpresa;
    private JComboBox<String> comboTipo;
    private JComboBox<String> comboStatus;
    private JLabel labelTotal;

    // LISTAS
    private List<Documento> todosDocumentos;
    private List<Empresa>   todasEmpresas;
    private List<TipoDocumento> todosTipos;

    public PainelDocumentos(TelaPrincipal janela) {
        this.janela      = janela;
        todosDocumentos  = docRepo.listarTodos();
        todasEmpresas    = empRepo.listarTodas();
        todosTipos       = tipoRepo.listarTodos();
        setLayout(new BorderLayout());
        setBackground(COR_FUNDO);
        initialize();
        carregarTabela();
    }

    private void initialize() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(COR_FUNDO);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(20, 24, 8, 24));

        JLabel titulo = new JLabel("Documentos");
        titulo.setFont(new Font(FONTE, Font.BOLD, 22));
        titulo.setForeground(new Color(15, 23, 42));

        JButton btnNovo = new JButton("+ Novo Documento");
        btnNovo.setBackground(COR_VERDE);
        btnNovo.setForeground(Color.WHITE);
        btnNovo.setFont(new Font(FONTE, Font.BOLD, 13));
        btnNovo.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnNovo.setFocusPainted(false);
        btnNovo.setOpaque(true);
        btnNovo.setBorderPainted(false);
        btnNovo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNovo.addActionListener(e -> abrirFormulario(null));

        cabecalho.add(titulo,  BorderLayout.WEST);
        cabecalho.add(btnNovo, BorderLayout.EAST);

        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setBackground(COR_FUNDO);
        centro.setBorder(BorderFactory.createEmptyBorder(8, 24, 0, 24));

        centro.add(criarPainelTabela(),  BorderLayout.CENTER);
        centro.add(criarPainelFiltros(), BorderLayout.NORTH);
        centro.add(criarRodape(),        BorderLayout.SOUTH);

        this.add(cabecalho, BorderLayout.NORTH);
        this.add(centro,    BorderLayout.CENTER);
    }

    // FILTROS — PESQUISA, EMPRESA, TIPO E STATUS
    private JPanel criarPainelFiltros() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COR_BORDA),
            BorderFactory.createEmptyBorder(0, 4, 0, 4)
        ));

        // CAMPO DE PESQUISA
        campoPesquisa = new JTextField("Pesquise pelo nome do documento...");
        campoPesquisa.setForeground(Color.GRAY);
        campoPesquisa.setPreferredSize(new Dimension(260, 40));
        campoPesquisa.setFont(new Font(FONTE, Font.PLAIN, 13));
        campoPesquisa.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COR_BORDA),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));

        campoPesquisa.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (campoPesquisa.getText().equals("Pesquise pelo nome do documento...")) {
                    campoPesquisa.setText("");
                    campoPesquisa.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (campoPesquisa.getText().isEmpty()) {
                    campoPesquisa.setText("Pesquise pelo nome do documento...");
                    campoPesquisa.setForeground(Color.GRAY);
                }
            }
        });

        campoPesquisa.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltro(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltro(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
        });

        // COMBO EMPRESA
        JLabel lblEmpresa = new JLabel("  Empresa");
        lblEmpresa.setFont(new Font(FONTE, Font.BOLD, 12));
        lblEmpresa.setForeground(COR_TEXTO_SEC);

        comboEmpresa = new JComboBox<>(getItensEmpresa());
        comboEmpresa.setFont(new Font(FONTE, Font.PLAIN, 13));
        comboEmpresa.setPreferredSize(new Dimension(220, 40));
        comboEmpresa.setBackground(Color.WHITE);
        comboEmpresa.setRenderer(criarRenderer());
        comboEmpresa.addActionListener(e -> aplicarFiltro());

        // COMBO TIPO
        JLabel lblTipo = new JLabel("  Tipo");
        lblTipo.setFont(new Font(FONTE, Font.BOLD, 12));
        lblTipo.setForeground(COR_TEXTO_SEC);

        comboTipo = new JComboBox<>(getItensTipo());
        comboTipo.setFont(new Font(FONTE, Font.PLAIN, 13));
        comboTipo.setPreferredSize(new Dimension(300, 40));
        comboTipo.setBackground(Color.WHITE);
        comboTipo.setRenderer(criarRenderer());
        comboTipo.addActionListener(e -> aplicarFiltro());

        // COMBO STATUS
        JLabel lblStatus = new JLabel("  Status");
        lblStatus.setFont(new Font(FONTE, Font.BOLD, 12));
        lblStatus.setForeground(COR_TEXTO_SEC);

        comboStatus = new JComboBox<>(new String[]{"Todos", "Válido", "A Vencer", "Vencido"});
        comboStatus.setFont(new Font(FONTE, Font.PLAIN, 13));
        comboStatus.setPreferredSize(new Dimension(100, 40));
        comboStatus.setBackground(Color.WHITE);
        comboStatus.setRenderer(criarRenderer());
        comboStatus.addActionListener(e -> aplicarFiltro());

        // BOTAO LIMPAR
        JButton btnLimpar = new JButton("Limpar Filtros");
        btnLimpar.setBackground(COR_CINZA_BTN);
        btnLimpar.setForeground(Color.BLACK);
        btnLimpar.setFont(new Font(FONTE, Font.PLAIN, 13));
        btnLimpar.setPreferredSize(new Dimension(140, 40));
        btnLimpar.setFocusPainted(false);
        btnLimpar.setOpaque(true);
        btnLimpar.setBorderPainted(false);
        btnLimpar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimpar.addActionListener(e -> limparFiltros());

        painel.add(campoPesquisa);
        painel.add(lblEmpresa);
        painel.add(comboEmpresa);
        painel.add(lblTipo);
        painel.add(comboTipo);
        painel.add(lblStatus);
        painel.add(comboStatus);
        painel.add(btnLimpar);

        return painel;
    }

    // RENDERER COM PADDING PARA OS COMBOS
    private javax.swing.plaf.basic.BasicComboBoxRenderer criarRenderer() {
        return new javax.swing.plaf.basic.BasicComboBoxRenderer() {
            public Component getListCellRendererComponent(JList list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
                return this;
            }
        };
    }

    // TABELA DE DOCUMENTOS
    private JPanel criarPainelTabela() {
        String[] colunas = {"Documento", "Empresa", "Tipo", "Vencimento", "Status", "Ações"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            public boolean isCellEditable(int row, int col) { return col == 5; }
        };

        tabela = new JTable(modeloTabela);
        sorter  = new TableRowSorter<>(modeloTabela);
        tabela.setRowSorter(sorter);

        tabela.setRowHeight(52);
        tabela.setFont(new Font(FONTE, Font.PLAIN, 13));
        tabela.setShowGrid(false);
        tabela.setShowVerticalLines(false);
        tabela.setShowHorizontalLines(true);
        tabela.setGridColor(new Color(243, 244, 246));
        tabela.setIntercellSpacing(new Dimension(0, 0));
        tabela.setFillsViewportHeight(true);
        tabela.setBackground(Color.WHITE);
        tabela.setSelectionBackground(new Color(239, 246, 255));
        tabela.setSelectionForeground(new Color(15, 23, 42));

        JTableHeader header = tabela.getTableHeader();
        header.setFont(new Font(FONTE, Font.BOLD, 12));
        header.setBackground(new Color(249, 250, 251));
        header.setForeground(COR_TEXTO_SEC);
        header.setPreferredSize(new Dimension(0, 44));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COR_BORDA));

        // RENDERER PADRAO COM PADDING
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
                setBackground(sel ? new Color(239, 246, 255) : Color.WHITE);
                setForeground(new Color(15, 23, 42));
                if (c == 3) setHorizontalAlignment(CENTER);
                else        setHorizontalAlignment(LEFT);
                return this;
            }
        };

        for (int i = 0; i < 4; i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // RENDERER DO STATUS COM COR
        tabela.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String status = v != null ? v.toString() : "";
                setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
                setHorizontalAlignment(CENTER);
                setBackground(sel ? new Color(239, 246, 255) : Color.WHITE);
                switch (status) {
                    case "Vencido"  -> setForeground(new Color(180, 30, 30));
                    case "A Vencer" -> setForeground(new Color(200, 170, 0));
                    default         -> setForeground(new Color(22, 163, 74));
                }
                setFont(new Font(FONTE, Font.BOLD, 12));
                return this;
            }
        });

        tabela.getColumnModel().getColumn(0).setPreferredWidth(200);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(160);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(150);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(100);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(140);

        tabela.getColumnModel().getColumn(5).setCellRenderer(new AcoesCellRenderer());
        tabela.getColumnModel().getColumn(5).setCellEditor(new AcoesCellEditor());

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createLineBorder(COR_BORDA));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(COR_FUNDO);
        painel.add(scroll, BorderLayout.CENTER);
        return painel;
    }

    // RODAPE
    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBackground(Color.WHITE);
        rodape.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, COR_BORDA),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        labelTotal = new JLabel("Mostrando 0 documento(s)");
        labelTotal.setFont(new Font(FONTE, Font.PLAIN, 12));
        labelTotal.setForeground(COR_TEXTO_SEC);

        rodape.add(labelTotal, BorderLayout.WEST);
        return rodape;
    }

    // APLICA FILTRO COM TABLEROWSORTER
    private void aplicarFiltro() {
        if (sorter == null) return;

        String texto   = campoPesquisa.getText().trim();
        if (texto.equals("Pesquise pelo nome do documento...")) texto = "";

        String empresa = (String) comboEmpresa.getSelectedItem();
        String tipo    = (String) comboTipo.getSelectedItem();
        String status  = (String) comboStatus.getSelectedItem();

        final String tf = texto;
        final String ef = empresa;
        final String tipof = tipo;
        final String sf = status;

        List<RowFilter<DefaultTableModel, Object>> filtros = new ArrayList<>();

        if (!tf.isEmpty())             filtros.add(RowFilter.regexFilter("(?i)" + tf, 0));
        if (!"Todas".equals(ef))       filtros.add(RowFilter.regexFilter("(?i)^" + ef + "$", 1));
        if (!"Todos".equals(tipof))    filtros.add(RowFilter.regexFilter("(?i)^" + tipof + "$", 2));
        if (!"Todos".equals(sf))       filtros.add(RowFilter.regexFilter("(?i)^" + sf + "$", 4));

        sorter.setRowFilter(filtros.isEmpty() ? null : RowFilter.andFilter(filtros));
        labelTotal.setText("Mostrando " + tabela.getRowCount() + " documento(s)");
    }

    private void limparFiltros() {
        campoPesquisa.setText("Pesquise pelo nome do documento...");
        campoPesquisa.setForeground(Color.GRAY);
        comboEmpresa.setSelectedIndex(0);
        comboTipo.setSelectedIndex(0);
        comboStatus.setSelectedIndex(0);
        sorter.setRowFilter(null);
        labelTotal.setText("Mostrando " + tabela.getRowCount() + " documento(s)");
    }

    // RETORNA OS NOMES DAS EMPRESAS PARA O COMBO
    private String[] getItensEmpresa() {
        List<String> itens = new ArrayList<>();
        itens.add("Todas");
        for (Empresa e : todasEmpresas) itens.add(e.getNome());
        return itens.toArray(new String[0]);
    }

    // RETORNA OS NOMES DOS TIPOS PARA O COMBO
    private String[] getItensTipo() {
        List<String> itens = new ArrayList<>();
        itens.add("Todos");
        for (TipoDocumento t : todosTipos) itens.add(t.getNome());
        return itens.toArray(new String[0]);
    }

    // BUSCA O NOME DA EMPRESA PELO ID
    private String getNomeEmpresa(int id) {
        for (Empresa e : todasEmpresas) {
            if (e.getId() == id) return e.getNome();
        }
        return "—";
    }

    // BUSCA O NOME DO TIPO PELO ID
    private String getNomeTipo(int id) {
        for (TipoDocumento t : todosTipos) {
            if (t.getId() == id) return t.getNome();
        }
        return "—";
    }

    // CARREGA OS DOCUMENTOS NA TABELA
    private void carregarTabela() {
        modeloTabela.setRowCount(0);
        todosDocumentos = docRepo.listarTodos();
        for (Documento d : todosDocumentos) {
            String vencimento = d.getDataVencimento() == null ? "Sem vencimento"
                : d.getVencimentoAtual().format(FMT);
            String status = d.getDataVencimento() == null ? "Válido"
                : StatusUtil.calcularStatus(d.getVencimentoAtual(), d.getDiasAntecedencia());
            modeloTabela.addRow(new Object[]{
                d.getNome(),
                getNomeEmpresa(d.getEmpresaId()),
                getNomeTipo(d.getTipoId()),
                vencimento,
                status,
                "acoes"
            });
        }
        labelTotal.setText("Mostrando " + todosDocumentos.size() + " documento(s)");
    }

    private Documento getDocumentoDaLinha(int linha) {
        int idx = tabela.convertRowIndexToModel(linha);
        if (idx >= 0 && idx < todosDocumentos.size()) return todosDocumentos.get(idx);
        return null;
    }

    private void abrirFormulario(Documento doc) {
        new FormularioDocumento(janela, doc, docRepo, todasEmpresas, todosTipos);
        carregarTabela();
    }

    private void excluir(Documento doc) {
        if (doc == null) return;
        int r = JOptionPane.showConfirmDialog(janela,
            "Deseja excluir o documento \"" + doc.getNome() + "\"?",
            "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            docRepo.deletar(doc.getId());
            carregarTabela();
        }
    }

    // RENDERER DA COLUNA DE ACOES
    class AcoesCellRenderer implements TableCellRenderer {
        private JPanel panel;

        AcoesCellRenderer() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 10));
            panel.setBackground(Color.WHITE);
            panel.add(criarBtnEditar());
            panel.add(criarBtnExcluir());
        }

        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int r, int c) {
            panel.setBackground(sel ? new Color(239, 246, 255) : Color.WHITE);
            return panel;
        }
    }

    // EDITOR DA COLUNA DE ACOES
    class AcoesCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private JButton btnEditar  = criarBtnEditar();
        private JButton btnExcluir = criarBtnExcluir();
        private int linhaSelecionada;

        AcoesCellEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 10));
            panel.setBackground(Color.WHITE);

            btnEditar.addActionListener(e -> {
                fireEditingStopped();
                Documento doc = getDocumentoDaLinha(linhaSelecionada);
                if (doc != null) abrirFormulario(doc);
            });

            btnExcluir.addActionListener(e -> {
                fireEditingStopped();
                Documento doc = getDocumentoDaLinha(linhaSelecionada);
                excluir(doc);
            });

            panel.add(btnEditar);
            panel.add(btnExcluir);
        }

        public Component getTableCellEditorComponent(JTable t, Object v,
                boolean sel, int r, int c) {
            linhaSelecionada = r;
            return panel;
        }

        public Object getCellEditorValue() { return "acoes"; }
    }

    private JButton criarBtnEditar() {
        JButton btn = new JButton("Editar");
        btn.setFont(new Font(FONTE, Font.PLAIN, 12));
        btn.setBackground(new Color(37, 99, 235));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(75, 30));
        btn.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton criarBtnExcluir() {
        JButton btn = new JButton("Excluir");
        btn.setFont(new Font(FONTE, Font.PLAIN, 12));
        btn.setBackground(new Color(180, 30, 30));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(75, 30));
        btn.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}