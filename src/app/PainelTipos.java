package app;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import modelos.TipoDocumento;
import repositorio.TipoDocumentoRepositorio;

public class PainelTipos extends JPanel {

    // CORES DO SISTEMA
    private static final Color COR_FUNDO     = new Color(245, 247, 250);
    private static final Color COR_VERDE     = new Color(22, 163, 74);
    private static final Color COR_BORDA     = new Color(220, 225, 235);
    private static final Color COR_TEXTO_SEC = new Color(80, 90, 110);
    private static final Color COR_CINZA_BTN = new Color(230, 232, 236);

    // FONTE DO SISTEMA
    private static final String FONTE = "Segoe UI";

    // REFERENCIA PARA A JANELA PRINCIPAL
    private TelaPrincipal janela;

    // REPOSITORIO
    private TipoDocumentoRepositorio repositorio = new TipoDocumentoRepositorio();

    // COMPONENTES
    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField campoPesquisa;
    private JLabel labelTotal;

    public PainelTipos(TelaPrincipal janela) {
        this.janela = janela;
        setLayout(new BorderLayout());
        setBackground(COR_FUNDO);
        initialize();
        carregarTabela();
    }

    private void initialize() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(COR_FUNDO);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(20, 24, 8, 24));

        JLabel titulo = new JLabel("Tipos de Documento");
        titulo.setFont(new Font(FONTE, Font.BOLD, 22));
        titulo.setForeground(new Color(15, 23, 42));

        JButton btnNovo = new JButton("+ Novo Tipo");
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

    private JPanel criarPainelFiltros() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COR_BORDA),
            BorderFactory.createEmptyBorder(0, 4, 0, 4)
        ));

        campoPesquisa = new JTextField("Pesquise pelo nome do tipo...");
        campoPesquisa.setForeground(Color.GRAY);
        campoPesquisa.setPreferredSize(new Dimension(320, 40));
        campoPesquisa.setFont(new Font(FONTE, Font.PLAIN, 13));
        campoPesquisa.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COR_BORDA),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));

        campoPesquisa.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (campoPesquisa.getText().equals("Pesquise pelo nome do tipo...")) {
                    campoPesquisa.setText("");
                    campoPesquisa.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (campoPesquisa.getText().isEmpty()) {
                    campoPesquisa.setText("Pesquise pelo nome do tipo...");
                    campoPesquisa.setForeground(Color.GRAY);
                }
            }
        });

        campoPesquisa.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltro(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltro(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
        });

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
        painel.add(btnLimpar);

        return painel;
    }

    private void aplicarFiltro() {
        if (sorter == null) return;
        String texto = campoPesquisa.getText().trim();
        if (texto.equals("Pesquise pelo nome do tipo...")) texto = "";
        sorter.setRowFilter(texto.isEmpty() ? null : RowFilter.regexFilter("(?i)" + texto, 0));
        labelTotal.setText("Mostrando " + tabela.getRowCount() + " tipo(s)");
    }

    private void limparFiltros() {
        campoPesquisa.setText("Pesquise pelo nome do tipo...");
        campoPesquisa.setForeground(Color.GRAY);
        sorter.setRowFilter(null);
        labelTotal.setText("Mostrando " + tabela.getRowCount() + " tipo(s)");
    }

    private JPanel criarPainelTabela() {
        String[] colunas = {"Nome", "Dias de Antecedência", "Ações"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            public boolean isCellEditable(int row, int col) { return col == 2; }
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
                setHorizontalAlignment(c == 1 ? CENTER : LEFT);
                return this;
            }
        };

        tabela.getColumnModel().getColumn(0).setCellRenderer(renderer);
        tabela.getColumnModel().getColumn(1).setCellRenderer(renderer);

        tabela.getColumnModel().getColumn(0).setPreferredWidth(500);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(200);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(140);

        tabela.getColumnModel().getColumn(2).setCellRenderer(new AcoesCellRenderer());
        tabela.getColumnModel().getColumn(2).setCellEditor(new AcoesCellEditor());

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

        labelTotal = new JLabel("Mostrando 0 tipo(s)");
        labelTotal.setFont(new Font(FONTE, Font.PLAIN, 12));
        labelTotal.setForeground(COR_TEXTO_SEC);

        rodape.add(labelTotal, BorderLayout.WEST);
        return rodape;
    }

    private void carregarTabela() {
        modeloTabela.setRowCount(0);
        List<TipoDocumento> tipos = repositorio.listarTodos();
        for (TipoDocumento t : tipos) {
            modeloTabela.addRow(new Object[]{
                t.getNome(),
                t.getDiasAntecedenciaPadrao() + " dias",
                "acoes"
            });
        }
        labelTotal.setText("Mostrando " + tipos.size() + " tipo(s)");
    }

    private TipoDocumento getTipoDaLinha(int linha) {
        int idx = tabela.convertRowIndexToModel(linha);
        String nome = (String) modeloTabela.getValueAt(idx, 0);
        for (TipoDocumento t : repositorio.listarTodos()) {
            if (t.getNome().equals(nome)) return t;
        }
        return null;
    }

    private void abrirFormulario(TipoDocumento tipo) {
        new FormularioTipo(janela, tipo, repositorio);
        carregarTabela();
    }

    private void excluir(TipoDocumento tipo) {
        if (tipo == null) return;
        int r = JOptionPane.showConfirmDialog(janela,
            "Deseja excluir o tipo \"" + tipo.getNome() + "\"?",
            "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            repositorio.deletar(tipo.getId());
            carregarTabela();
        }
    }

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
                TipoDocumento tipo = getTipoDaLinha(linhaSelecionada);
                if (tipo != null) abrirFormulario(tipo);
            });

            btnExcluir.addActionListener(e -> {
                fireEditingStopped();
                TipoDocumento tipo = getTipoDaLinha(linhaSelecionada);
                excluir(tipo);
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