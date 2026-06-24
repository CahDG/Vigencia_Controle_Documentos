package app;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import modelos.Empresa;
import repositorio.EmpresaRepositorio;

public class PainelEmpresas extends JPanel {

    // CORES DO SISTEMA
    private static final Color COR_FUNDO     = new Color(245, 247, 250);
    private static final Color COR_VERDE     = new Color(22, 163, 74);
    private static final Color COR_BORDA     = new Color(220, 225, 235);
    private static final Color COR_TEXTO_SEC = new Color(80, 90, 110);
    private static final Color COR_CINZA_BTN = new Color(230, 232, 236);

    // FONTE DO SISTEMA
    private static final String FONTE = "Segoe UI";

    // SEGMENTOS FIXOS — ADICIONE AQUI SE PRECISAR DE MAIS
    private static final String[] SEGMENTOS = {"Todos", "Clínica", "Distribuidora", "Hospital", "Outros"};

    // REFERENCIA PARA A JANELA PRINCIPAL
    private TelaPrincipal janela;

    // REPOSITORIO
    private EmpresaRepositorio repositorio = new EmpresaRepositorio();

    // COMPONENTES
    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField campoPesquisa;
    private JComboBox<String> comboSegmento;
    private JComboBox<String> comboCidade;
    private JLabel labelTotal;

    // LISTA DE EMPRESAS
    private List<Empresa> todasEmpresas;

    public PainelEmpresas(TelaPrincipal janela) {
        this.janela    = janela;
        todasEmpresas  = repositorio.listarTodas();
        setLayout(new BorderLayout());
        setBackground(COR_FUNDO);
        initialize();
        carregarTabela();
    }

    private void initialize() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(COR_FUNDO);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(20, 24, 8, 24));

        JLabel titulo = new JLabel("Cadastro de Empresas");
        titulo.setFont(new Font(FONTE, Font.BOLD, 22));
        titulo.setForeground(new Color(15, 23, 42));

        JButton btnNova = new JButton("+ Nova Empresa");
        btnNova.setBackground(COR_VERDE);
        btnNova.setForeground(Color.WHITE);
        btnNova.setFont(new Font(FONTE, Font.BOLD, 13));
        btnNova.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnNova.setFocusPainted(false);
        btnNova.setOpaque(true);
        btnNova.setBorderPainted(false);
        btnNova.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNova.addActionListener(e -> abrirFormulario(null));

        cabecalho.add(titulo,  BorderLayout.WEST);
        cabecalho.add(btnNova, BorderLayout.EAST);

        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setBackground(COR_FUNDO);
        centro.setBorder(BorderFactory.createEmptyBorder(8, 24, 0, 24));

        centro.add(criarPainelTabela(),  BorderLayout.CENTER);
        centro.add(criarPainelFiltros(), BorderLayout.NORTH);
        centro.add(criarRodape(),        BorderLayout.SOUTH);

        this.add(cabecalho, BorderLayout.NORTH);
        this.add(centro,    BorderLayout.CENTER);
    }

    private JPanel criarPainelFiltros() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COR_BORDA),
            BorderFactory.createEmptyBorder(0, 4, 0, 4)
        ));

        campoPesquisa = new JTextField("Pesquise pela empresa...");
        campoPesquisa.setForeground(Color.GRAY);
        campoPesquisa.setPreferredSize(new Dimension(280, 40));
        campoPesquisa.setFont(new Font(FONTE, Font.PLAIN, 13));
        campoPesquisa.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COR_BORDA),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));

        campoPesquisa.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (campoPesquisa.getText().equals("Pesquise pela empresa...")) {
                    campoPesquisa.setText("");
                    campoPesquisa.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (campoPesquisa.getText().isEmpty()) {
                    campoPesquisa.setText("Pesquise pela empresa...");
                    campoPesquisa.setForeground(Color.GRAY);
                }
            }
        });

        campoPesquisa.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltro(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltro(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
        });

        JLabel lblSeg = new JLabel("  Segmento");
        lblSeg.setFont(new Font(FONTE, Font.BOLD, 12));
        lblSeg.setForeground(COR_TEXTO_SEC);

        comboSegmento = new JComboBox<>(SEGMENTOS);
        comboSegmento.setFont(new Font(FONTE, Font.PLAIN, 13));
        comboSegmento.setPreferredSize(new Dimension(160, 40));
        comboSegmento.setBackground(Color.WHITE);
        comboSegmento.setRenderer(new javax.swing.plaf.basic.BasicComboBoxRenderer() {
            public Component getListCellRendererComponent(JList list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
                return this;
            }
        });
        comboSegmento.addActionListener(e -> aplicarFiltro());

        JLabel lblCidade = new JLabel("  Cidade");
        lblCidade.setFont(new Font(FONTE, Font.BOLD, 12));
        lblCidade.setForeground(COR_TEXTO_SEC);

        comboCidade = new JComboBox<>(getCidadesCadastradas());
        comboCidade.setFont(new Font(FONTE, Font.PLAIN, 13));
        comboCidade.setPreferredSize(new Dimension(180, 40));
        comboCidade.setBackground(Color.WHITE);
        comboCidade.setEditable(false);
        comboCidade.setRenderer(new javax.swing.plaf.basic.BasicComboBoxRenderer() {
            public Component getListCellRendererComponent(JList list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
                return this;
            }
        });
        comboCidade.addActionListener(e -> aplicarFiltro());

        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.setBackground(COR_CINZA_BTN);
        btnFiltrar.setForeground(Color.BLACK);
        btnFiltrar.setFont(new Font(FONTE, Font.PLAIN, 13));
        btnFiltrar.setPreferredSize(new Dimension(110, 40));
        btnFiltrar.setFocusPainted(false);
        btnFiltrar.setOpaque(true);
        btnFiltrar.setBorderPainted(false);
        btnFiltrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFiltrar.addActionListener(e -> aplicarFiltro());

        JButton btnLimpar = new JButton("Limpar Filtros");
        btnLimpar.setBackground(COR_CINZA_BTN);
        btnLimpar.setForeground(Color.BLACK);
        btnLimpar.setFont(new Font(FONTE, Font.PLAIN, 13));
        btnLimpar.setPreferredSize(new Dimension(150, 40));
        btnLimpar.setFocusPainted(false);
        btnLimpar.setOpaque(true);
        btnLimpar.setBorderPainted(false);
        btnLimpar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimpar.addActionListener(e -> limparFiltros());

        painel.add(campoPesquisa);
        painel.add(lblSeg);
        painel.add(comboSegmento);
        painel.add(lblCidade);
        painel.add(comboCidade);
        painel.add(btnFiltrar);
        painel.add(btnLimpar);

        return painel;
    }

    private JPanel criarPainelTabela() {
        String[] colunas = {"Empresa", "CNPJ", "Segmento", "Cidade", "UF", "Ações"};
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

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
                setBackground(sel ? new Color(239, 246, 255) : Color.WHITE);
                setForeground(new Color(15, 23, 42));
                if (c == 1 || c == 2 || c == 4) {
                    setHorizontalAlignment(CENTER);
                } else {
                    setHorizontalAlignment(LEFT);
                }
                return this;
            }
        };

        for (int i = 0; i < 5; i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        tabela.getColumnModel().getColumn(0).setPreferredWidth(220);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(160);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(130);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(50);
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

    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBackground(Color.WHITE);
        rodape.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, COR_BORDA),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        labelTotal = new JLabel("Mostrando 0 empresa(s)");
        labelTotal.setFont(new Font(FONTE, Font.PLAIN, 12));
        labelTotal.setForeground(COR_TEXTO_SEC);

        rodape.add(labelTotal, BorderLayout.WEST);
        return rodape;
    }

    private void aplicarFiltro() {
        if (sorter == null) return;

        String texto  = campoPesquisa.getText().trim();
        if (texto.equals("Pesquise pela empresa...")) texto = "";

        String seg    = (String) comboSegmento.getSelectedItem();
        String cidade = (String) comboCidade.getSelectedItem();
        if (cidade == null) cidade = "Todas";

        final String tf = texto;
        final String sf = seg;
        final String cf = cidade;

        List<RowFilter<DefaultTableModel, Object>> filtros = new ArrayList<>();

        if (!tf.isEmpty()) filtros.add(RowFilter.regexFilter("(?i)" + tf, 0));
        if (!"Todos".equals(sf)) filtros.add(RowFilter.regexFilter("(?i)^" + sf + "$", 2));
        if (!"Todas".equals(cf) && !cf.isEmpty()) filtros.add(RowFilter.regexFilter("(?i)" + cf, 3));

        sorter.setRowFilter(filtros.isEmpty() ? null : RowFilter.andFilter(filtros));
        labelTotal.setText("Mostrando " + tabela.getRowCount() + " empresa(s)");
    }

    private void limparFiltros() {
        campoPesquisa.setText("Pesquise pela empresa...");
        campoPesquisa.setForeground(Color.GRAY);
        comboSegmento.setSelectedIndex(0);
        comboCidade.setSelectedIndex(0);
        sorter.setRowFilter(null);
        labelTotal.setText("Mostrando " + tabela.getRowCount() + " empresa(s)");
    }

    private String[] getCidadesCadastradas() {
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
        set.add("Todas");
        for (Empresa e : todasEmpresas) {
            if (e.getCidade() != null && !e.getCidade().isBlank()) set.add(e.getCidade());
        }
        return set.toArray(new String[0]);
    }

    private void atualizarComboCidade() {
        String atual = (String) comboCidade.getSelectedItem();
        comboCidade.setModel(new DefaultComboBoxModel<>(getCidadesCadastradas()));
        comboCidade.setSelectedItem(atual);
    }

    private void carregarTabela() {
        modeloTabela.setRowCount(0);
        for (Empresa e : todasEmpresas) {
            modeloTabela.addRow(new Object[]{
                e.getNome(), e.getCnpj(), e.getSegmento(),
                e.getCidade(), e.getUf(), "acoes"
            });
        }
        labelTotal.setText("Mostrando " + todasEmpresas.size() + " empresa(s)");
    }

    private Empresa getEmpresaDaLinha(int linha) {
        int idx = tabela.convertRowIndexToModel(linha);
        String nome = (String) modeloTabela.getValueAt(idx, 0);
        for (Empresa e : todasEmpresas) {
            if (e.getNome().equals(nome)) return e;
        }
        return null;
    }

    private void abrirFormulario(Empresa empresa) {
        new FormularioEmpresa(janela, empresa, repositorio);
        todasEmpresas = repositorio.listarTodas();
        carregarTabela();
        atualizarComboCidade();
        aplicarFiltro();
    }

    private void excluir(Empresa empresa) {
        if (empresa == null) return;
        int r = JOptionPane.showConfirmDialog(janela,
            "Deseja excluir a empresa \"" + empresa.getNome() + "\"?",
            "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            repositorio.deletar(empresa.getId());
            todasEmpresas = repositorio.listarTodas();
            carregarTabela();
            atualizarComboCidade();
            aplicarFiltro();
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
                Empresa emp = getEmpresaDaLinha(linhaSelecionada);
                if (emp != null) abrirFormulario(emp);
            });

            btnExcluir.addActionListener(e -> {
                fireEditingStopped();
                Empresa emp = getEmpresaDaLinha(linhaSelecionada);
                excluir(emp);
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