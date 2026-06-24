package app;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;
import modelos.Documento;
import modelos.Empresa;
import modelos.TipoDocumento;
import repositorio.DocumentoRepositorio;
import repositorio.EmpresaRepositorio;
import repositorio.TipoDocumentoRepositorio;
import util.StatusUtil;

public class PainelDashboard extends JPanel {

    // CORES DO SISTEMA
    private static final Color COR_FUNDO     = new Color(245, 247, 250);
    private static final Color COR_BORDA     = new Color(220, 225, 235);
    private static final Color COR_TEXTO_SEC = new Color(80, 90, 110);
    private static final Color COR_CINZA_BTN = new Color(230, 232, 236);
    private static final Color COR_VENCIDO   = new Color(220, 53, 69);
    private static final Color COR_AVENCER   = new Color(200, 170, 0);
    private static final Color COR_VALIDO    = new Color(40, 167, 69);
    private static final Color COR_AZUL      = new Color(37, 99, 235);

    // FONTE DO SISTEMA
    private static final String FONTE = "Segoe UI";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // REFERENCIA PARA A JANELA PRINCIPAL
    private TelaPrincipal janela;

    // REPOSITORIOS
    private DocumentoRepositorio     docRepo  = new DocumentoRepositorio();
    private EmpresaRepositorio       empRepo  = new EmpresaRepositorio();
    private TipoDocumentoRepositorio tipoRepo = new TipoDocumentoRepositorio();

    // COMPONENTES
    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField campoPesquisa;
    private JComboBox<String> comboEmpresa;
    private JComboBox<String> comboTipo;
    private JComboBox<String> comboStatus;
    private JLabel labelTotal;
    private JLabel labelTituloTabela;

    // CONTROLE DO MODO DE EXIBICAO: TRUE = APENAS ALERTAS, FALSE = TODOS OS DOCUMENTOS
    private boolean modoAlerta = true;

    // LISTAS
    private List<Documento>     todosDocumentos;
    private List<Documento>     documentosExibidos;
    private List<Empresa>       todasEmpresas;
    private List<TipoDocumento> todosTipos;

    public PainelDashboard(TelaPrincipal janela) {
        this.janela     = janela;
        todasEmpresas   = empRepo.listarTodas();
        todosTipos      = tipoRepo.listarTodos();
        todosDocumentos = docRepo.listarTodos();
        documentosExibidos = getDocumentosAlerta();
        setLayout(new BorderLayout());
        setBackground(COR_FUNDO);
        initialize();
        carregarTabela();
    }

    // RETORNA DOCUMENTOS VENCIDOS OU A VENCER ORDENADOS POR URGENCIA
    private List<Documento> getDocumentosAlerta() {
        List<Documento> alerta = new ArrayList<>();
        for (Documento d : todosDocumentos) {
            if (d.getDataVencimento() == null) continue;
            String status = StatusUtil.calcularStatus(d.getVencimentoAtual(), d.getDiasAntecedencia());
            if (status.equals("Vencido") || status.equals("A Vencer")) {
                alerta.add(d);
            }
        }
        // ORDENA POR DATA DE VENCIMENTO — MAIS URGENTE PRIMEIRO
        alerta.sort(Comparator.comparing(d -> d.getVencimentoAtual()));
        return alerta;
    }

    // RETORNA TODOS OS DOCUMENTOS COM VENCIMENTO, ORDENADOS POR DATA
    private List<Documento> getTodosComVencimento() {
        List<Documento> lista = new ArrayList<>();
        for (Documento d : todosDocumentos) {
            lista.add(d);
        }
        lista.sort((a, b) -> {
            if (a.getVencimentoAtual() == null && b.getVencimentoAtual() == null) return 0;
            if (a.getVencimentoAtual() == null) return 1;
            if (b.getVencimentoAtual() == null) return -1;
            return a.getVencimentoAtual().compareTo(b.getVencimentoAtual());
        });
        return lista;
    }

    private void initialize() {
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(COR_FUNDO);
        topo.add(criarCards(), BorderLayout.NORTH);
        topo.add(criarCabecalhoTabela(), BorderLayout.SOUTH);

        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setBackground(COR_FUNDO);
        centro.setBorder(BorderFactory.createEmptyBorder(8, 24, 0, 24));
        centro.add(criarPainelTabela(),  BorderLayout.CENTER);
        centro.add(criarPainelFiltros(), BorderLayout.NORTH);
        centro.add(criarRodape(),        BorderLayout.SOUTH);

        this.add(topo,   BorderLayout.NORTH);
        this.add(centro, BorderLayout.CENTER);
    }

    // CABECALHO COM TITULO E BOTAO TOGGLE
    private JPanel criarCabecalhoTabela() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(COR_FUNDO);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));

        labelTituloTabela = new JLabel("Alertas de Documentos");
        labelTituloTabela.setFont(new Font(FONTE, Font.BOLD, 20));
        labelTituloTabela.setForeground(new Color(15, 23, 42));

        // BOTAO TOGGLE: ALTERNA ENTRE VER ALERTAS E VER TODOS
        JButton btnToggle = new JButton("Ver Todos os Documentos");
        btnToggle.setFont(new Font(FONTE, Font.PLAIN, 13));
        btnToggle.setBackground(COR_AZUL);
        btnToggle.setForeground(Color.WHITE);
        btnToggle.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnToggle.setFocusPainted(false);
        btnToggle.setOpaque(true);
        btnToggle.setBorderPainted(false);
        btnToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggle.addActionListener(e -> {
            modoAlerta = !modoAlerta;
            if (modoAlerta) {
                btnToggle.setText("Ver Todos os Documentos");
                labelTituloTabela.setText("Alertas de Documentos");
                // AJUSTA O COMBO STATUS PARA ALERTAS
                comboStatus.setModel(new DefaultComboBoxModel<>(new String[]{"Todos", "Vencido", "A Vencer"}));
            } else {
                btnToggle.setText("Ver Somente Alertas");
                labelTituloTabela.setText("Todos os Documentos");
                // ADICIONA VALIDO NO COMBO STATUS
                comboStatus.setModel(new DefaultComboBoxModel<>(new String[]{"Todos", "Vencido", "A Vencer", "Válido"}));
            }
            todosDocumentos = docRepo.listarTodos();
            carregarTabela();
        });

        cabecalho.add(labelTituloTabela, BorderLayout.WEST);
        cabecalho.add(btnToggle,         BorderLayout.EAST);
        return cabecalho;
    }

    // CARREGA ICONE DO CLASSPATH E RETORNA IMAGEM 48x48 SEM ALTERACAO DE COR
    private ImageIcon carregarIconeCard(String caminho, Color cor) {
        try {
            java.net.URL url = getClass().getClassLoader().getResource(caminho);
            if (url == null) return null;
            BufferedImage original = ImageIO.read(url);
            BufferedImage bi = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bi.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(original, 0, 0, 48, 48, null);
            g2d.dispose();
            return new ImageIcon(bi);
        } catch (Exception e) {
            return null;
        }
    }

    // CARDS DE RESUMO COM ICONES DE IMAGEM
    private JPanel criarCards() {
        JPanel painel = new JPanel(new GridLayout(1, 4, 16, 0));
        painel.setBackground(COR_FUNDO);
        painel.setBorder(BorderFactory.createEmptyBorder(20, 24, 8, 24));

        long vencidos = todosDocumentos.stream()
            .filter(d -> d.getDataVencimento() != null)
            .filter(d -> StatusUtil.calcularStatus(d.getVencimentoAtual(), d.getDiasAntecedencia()).equals("Vencido"))
            .count();

        long aVencer = todosDocumentos.stream()
            .filter(d -> d.getDataVencimento() != null)
            .filter(d -> StatusUtil.calcularStatus(d.getVencimentoAtual(), d.getDiasAntecedencia()).equals("A Vencer"))
            .count();

        painel.add(criarCard("Vencidos",   String.valueOf(vencidos),               "Documentos vencidos",    COR_VENCIDO, "icons/CardVencidos.png",   "!"));
        painel.add(criarCard("A Vencer",   String.valueOf(aVencer),                "Próximos do vencimento", COR_AVENCER, "icons/CardAVencer.png",    "⏱"));
        painel.add(criarCard("Empresas",   String.valueOf(todasEmpresas.size()),   "Empresas cadastradas",   COR_AZUL,    "icons/CardEmpresas.png",   "E"));
        painel.add(criarCard("Documentos", String.valueOf(todosDocumentos.size()), "Total de documentos",    COR_VALIDO,  "icons/CardDocumentos.png", "D"));

        return painel;
    }

    // CARD INDIVIDUAL — USA ICONE DE IMAGEM SE DISPONIVEL, SENAO DESENHA CIRCULO COM SIMBOLO
    private JPanel criarCard(String titulo, String numero, String descricao, Color cor, String caminhoIcone, String simboloFallback) {
        JPanel card = new JPanel(new BorderLayout(16, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COR_BORDA),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        ImageIcon iconeImagem = carregarIconeCard(caminhoIcone, cor);

        JPanel iconePanel;
        if (iconeImagem != null) {
            iconePanel = new JPanel(new GridBagLayout());
            iconePanel.setOpaque(false);
            iconePanel.setPreferredSize(new Dimension(56, 56));
            iconePanel.add(new JLabel(iconeImagem));
        } else {
            final String simbolo = simboloFallback;
            iconePanel = new JPanel(new GridBagLayout()) {
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(cor);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font(FONTE, Font.BOLD, 22));
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth()  - fm.stringWidth(simbolo)) / 2;
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(simbolo, x, y);
                    g2.dispose();
                }
            };
            iconePanel.setOpaque(false);
            iconePanel.setPreferredSize(new Dimension(56, 56));
        }

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font(FONTE, Font.PLAIN, 13));
        lblTitulo.setForeground(COR_TEXTO_SEC);

        JLabel lblNumero = new JLabel(numero);
        lblNumero.setFont(new Font(FONTE, Font.BOLD, 34));
        lblNumero.setForeground(cor);

        JLabel lblDesc = new JLabel(descricao);
        lblDesc.setFont(new Font(FONTE, Font.PLAIN, 12));
        lblDesc.setForeground(COR_TEXTO_SEC);

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);
        textos.add(lblTitulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(lblNumero);
        textos.add(Box.createVerticalStrut(2));
        textos.add(lblDesc);

        card.add(iconePanel, BorderLayout.WEST);
        card.add(textos,     BorderLayout.CENTER);

        return card;
    }

    // FILTROS
    private JPanel criarPainelFiltros() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COR_BORDA),
            BorderFactory.createEmptyBorder(0, 4, 0, 4)
        ));

        campoPesquisa = new JTextField("Pesquise pelo documento...");
        campoPesquisa.setForeground(Color.GRAY);
        campoPesquisa.setPreferredSize(new Dimension(240, 40));
        campoPesquisa.setFont(new Font(FONTE, Font.PLAIN, 13));
        campoPesquisa.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COR_BORDA),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));

        campoPesquisa.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (campoPesquisa.getText().equals("Pesquise pelo documento...")) {
                    campoPesquisa.setText("");
                    campoPesquisa.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (campoPesquisa.getText().isEmpty()) {
                    campoPesquisa.setText("Pesquise pelo documento...");
                    campoPesquisa.setForeground(Color.GRAY);
                }
            }
        });

        campoPesquisa.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltro(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltro(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
        });

        JLabel lblEmpresa = new JLabel("  Empresa");
        lblEmpresa.setFont(new Font(FONTE, Font.BOLD, 12));
        lblEmpresa.setForeground(COR_TEXTO_SEC);

        comboEmpresa = new JComboBox<>(getItensEmpresa());
        comboEmpresa.setFont(new Font(FONTE, Font.PLAIN, 13));
        comboEmpresa.setPreferredSize(new Dimension(220, 40));
        comboEmpresa.setBackground(Color.WHITE);
        comboEmpresa.setRenderer(criarRenderer());
        comboEmpresa.addActionListener(e -> aplicarFiltro());

        JLabel lblTipo = new JLabel("  Tipo");
        lblTipo.setFont(new Font(FONTE, Font.BOLD, 12));
        lblTipo.setForeground(COR_TEXTO_SEC);

        comboTipo = new JComboBox<>(getItensTipo());
        comboTipo.setFont(new Font(FONTE, Font.PLAIN, 13));
        comboTipo.setPreferredSize(new Dimension(300, 40));
        comboTipo.setBackground(Color.WHITE);
        comboTipo.setRenderer(criarRenderer());
        comboTipo.addActionListener(e -> aplicarFiltro());

        JLabel lblStatus = new JLabel("  Status");
        lblStatus.setFont(new Font(FONTE, Font.BOLD, 12));
        lblStatus.setForeground(COR_TEXTO_SEC);

        // COMBO STATUS INICIA COM OPCOES DE ALERTA APENAS
        comboStatus = new JComboBox<>(new String[]{"Todos", "Vencido", "A Vencer"});
        comboStatus.setFont(new Font(FONTE, Font.PLAIN, 13));
        comboStatus.setPreferredSize(new Dimension(100, 40));
        comboStatus.setBackground(Color.WHITE);
        comboStatus.setRenderer(criarRenderer());
        comboStatus.addActionListener(e -> aplicarFiltro());

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
        String[] colunas = {"Documento", "Empresa", "Tipo", "Vencimento", "Dias", "Status", "Ação"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            public boolean isCellEditable(int row, int col) { return col == 6; }
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

        // RENDERER PADRAO
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
                setBackground(sel ? new Color(239, 246, 255) : Color.WHITE);
                setForeground(new Color(15, 23, 42));
                if (c == 3 || c == 4) setHorizontalAlignment(CENTER);
                else                  setHorizontalAlignment(LEFT);
                return this;
            }
        };

        for (int i = 0; i < 4; i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // COLUNA DIAS — COLORIDA POR URGENCIA (VERDE PARA VALIDO, VERMELHO PARA VENCIDO, AMARELO PARA A VENCER)
        tabela.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
                setHorizontalAlignment(CENTER);
                setBackground(sel ? new Color(239, 246, 255) : Color.WHITE);
                setFont(new Font(FONTE, Font.BOLD, 13));
                if (v == null || v.toString().equals("—")) {
                    setForeground(COR_TEXTO_SEC);
                } else {
                    int dias = Integer.parseInt(v.toString());
                    if (dias < 0)  setForeground(COR_VENCIDO);
                    else if (dias > 30) setForeground(COR_VALIDO);
                    else           setForeground(COR_AVENCER);
                }
                return this;
            }
        });

        // COLUNA STATUS — COLORIDA
        tabela.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
                setHorizontalAlignment(CENTER);
                setBackground(sel ? new Color(239, 246, 255) : Color.WHITE);
                String status = v != null ? v.toString() : "";
                if (status.equals("Vencido"))       setForeground(COR_VENCIDO);
                else if (status.equals("A Vencer")) setForeground(COR_AVENCER);
                else                                setForeground(COR_VALIDO);
                setFont(new Font(FONTE, Font.BOLD, 12));
                return this;
            }
        });

        tabela.getColumnModel().getColumn(0).setPreferredWidth(180);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(140);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(110);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(60);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(100);

        tabela.getColumnModel().getColumn(6).setCellRenderer(new AcoesCellRenderer());
        tabela.getColumnModel().getColumn(6).setCellEditor(new AcoesCellEditor());

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

    private void aplicarFiltro() {
        if (sorter == null) return;

        String texto   = campoPesquisa.getText().trim();
        if (texto.equals("Pesquise pelo documento...")) texto = "";

        String empresa = (String) comboEmpresa.getSelectedItem();
        String tipo    = (String) comboTipo.getSelectedItem();
        String status  = (String) comboStatus.getSelectedItem();

        final String tf    = texto;
        final String ef    = empresa;
        final String tipof = tipo;
        final String sf    = status;

        List<RowFilter<DefaultTableModel, Object>> filtros = new ArrayList<>();

        if (!tf.isEmpty())          filtros.add(RowFilter.regexFilter("(?i)" + tf, 0));
        if (!"Todas".equals(ef))    filtros.add(RowFilter.regexFilter("(?i)^" + ef + "$", 1));
        if (!"Todos".equals(tipof)) filtros.add(RowFilter.regexFilter("(?i)^" + tipof + "$", 2));
        if (!"Todos".equals(sf))    filtros.add(RowFilter.regexFilter("(?i)^" + sf + "$", 5));

        sorter.setRowFilter(filtros.isEmpty() ? null : RowFilter.andFilter(filtros));
        labelTotal.setText("Mostrando " + tabela.getRowCount() + " documento(s)");
    }

    private void limparFiltros() {
        campoPesquisa.setText("Pesquise pelo documento...");
        campoPesquisa.setForeground(Color.GRAY);
        comboEmpresa.setSelectedIndex(0);
        comboTipo.setSelectedIndex(0);
        comboStatus.setSelectedIndex(0);
        sorter.setRowFilter(null);
        labelTotal.setText("Mostrando " + tabela.getRowCount() + " documento(s)");
    }

    private String[] getItensEmpresa() {
        List<String> itens = new ArrayList<>();
        itens.add("Todas");
        for (Empresa e : todasEmpresas) itens.add(e.getNome());
        return itens.toArray(new String[0]);
    }

    private String[] getItensTipo() {
        List<String> itens = new ArrayList<>();
        itens.add("Todos");
        for (TipoDocumento t : todosTipos) itens.add(t.getNome());
        return itens.toArray(new String[0]);
    }

    private String getNomeEmpresa(int id) {
        for (Empresa e : todasEmpresas) {
            if (e.getId() == id) return e.getNome();
        }
        return "—";
    }

    private String getNomeTipo(int id) {
        for (TipoDocumento t : todosTipos) {
            if (t.getId() == id) return t.getNome();
        }
        return "—";
    }

    // CARREGA OS DOCUMENTOS NA TABELA CONFORME O MODO ATUAL
    private void carregarTabela() {
        modeloTabela.setRowCount(0);

        // ESCOLHE A LISTA CONFORME O MODO: SO ALERTAS OU TODOS
        documentosExibidos = modoAlerta ? getDocumentosAlerta() : getTodosComVencimento();

        for (Documento d : documentosExibidos) {
            LocalDate venc = d.getVencimentoAtual();
            String vencStr = venc != null ? venc.format(FMT) : "Sem vencimento";
            Object diasVal;
            String status;

            if (venc == null) {
                diasVal = "—";
                status  = "Válido";
            } else {
                long dias = ChronoUnit.DAYS.between(LocalDate.now(), venc);
                diasVal = dias;
                status  = StatusUtil.calcularStatus(venc, d.getDiasAntecedencia());
            }

            modeloTabela.addRow(new Object[]{
                d.getNome(),
                getNomeEmpresa(d.getEmpresaId()),
                getNomeTipo(d.getTipoId()),
                vencStr,
                diasVal,
                status,
                "acoes"
            });
        }
        labelTotal.setText("Mostrando " + documentosExibidos.size() + " documento(s)");
    }

    private Documento getDocumentoDaLinha(int linha) {
        int idx = tabela.convertRowIndexToModel(linha);
        if (idx >= 0 && idx < documentosExibidos.size()) return documentosExibidos.get(idx);
        return null;
    }

    private void renovar(Documento doc) {
        if (doc == null) return;
        new FormularioRenovacao(janela, doc, docRepo);
        // RECARREGA DADOS E ATUALIZA TABELA AUTOMATICAMENTE APOS FECHAR O FORMULARIO
        todosDocumentos = docRepo.listarTodos();
        carregarTabela();
    }

    // RENDERER DA COLUNA DE ACOES
    class AcoesCellRenderer implements TableCellRenderer {
        private JPanel panel;

        AcoesCellRenderer() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 10));
            panel.setBackground(Color.WHITE);
            panel.add(criarBtnRenovar());
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
        private JButton btnRenovar = criarBtnRenovar();
        private int linhaSelecionada;

        AcoesCellEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 10));
            panel.setBackground(Color.WHITE);

            btnRenovar.addActionListener(e -> {
                fireEditingStopped();
                Documento doc = getDocumentoDaLinha(linhaSelecionada);
                renovar(doc);
            });

            panel.add(btnRenovar);
        }

        public Component getTableCellEditorComponent(JTable t, Object v,
                boolean sel, int r, int c) {
            linhaSelecionada = r;
            return panel;
        }

        public Object getCellEditorValue() { return "acoes"; }
    }

    // BOTAO RENOVAR — VERDE COM TEXTO BRANCO
    private JButton criarBtnRenovar() {
        JButton btn = new JButton("Renovar");
        btn.setFont(new Font(FONTE, Font.PLAIN, 12));
        btn.setBackground(COR_VALIDO);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(85, 30));
        btn.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}