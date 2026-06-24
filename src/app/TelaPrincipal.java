package app;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;

public class TelaPrincipal extends JFrame {

    // CORES DO SISTEMA
    public static final Color COR_SIDEBAR   = new Color(5, 32, 74);
    public static final Color COR_ATIVO_BG  = new Color(34, 197, 94);
    public static final Color COR_FUNDO     = new Color(245, 247, 250);
    public static final Color COR_VERDE     = new Color(22, 163, 74);
    public static final Color COR_BORDA     = new Color(220, 225, 235);
    public static final Color COR_TEXTO_SEC = new Color(80, 90, 110);

    // FONTE DO SISTEMA
    public static final String FONTE = "Segoe UI";

    // PAINEL CENTRAL QUE TROCA DE CONTEUDO
    private JPanel painelConteudo;

    // ITENS DO MENU PARA CONTROLAR QUAL ESTA ATIVO
    private JPanel itemDashboard;
    private JPanel itemDocumentos;
    private JPanel itemEmpresas;
    private JPanel itemTipos;

    public TelaPrincipal() {
        initialize();
        // ABRE A TELA DE DASHBOARD POR PADRAO
        navegarPara("dashboard");
    }

    // INICIALIZACAO DA JANELA PRINCIPAL
    private void initialize() {
        this.setTitle("Vigência - Sistema de Controle de Documentos Regulatórios");
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setMinimumSize(new Dimension(1024, 600));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());

        this.add(criarTopbar(), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(criarSidebar(), BorderLayout.WEST);

        // PAINEL CENTRAL COM CARDLAYOUT PARA TROCAR CONTEUDO SEM FECHAR A JANELA
        painelConteudo = new JPanel(new BorderLayout());
        painelConteudo.setBackground(COR_FUNDO);
        mainPanel.add(painelConteudo, BorderLayout.CENTER);

        this.add(mainPanel, BorderLayout.CENTER);
        this.setVisible(true);
    }

    // TROCA O CONTEUDO DO PAINEL CENTRAL SEM FECHAR A JANELA
    public void navegarPara(String tela) {
        painelConteudo.removeAll();
        painelConteudo.setBorder(null);

        switch (tela) {
            case "empresas":
                painelConteudo.add(new PainelEmpresas(this), BorderLayout.CENTER);
                setItemAtivo(itemEmpresas);
                break;
            case "tipos":
                painelConteudo.add(new PainelTipos(this), BorderLayout.CENTER);
                setItemAtivo(itemTipos);
                break;
            case "dashboard":
                painelConteudo.add(new PainelDashboard(this), BorderLayout.CENTER);
                setItemAtivo(itemDashboard);
                break;
            case "documentos":
                painelConteudo.add(new PainelDocumentos(this), BorderLayout.CENTER);
                setItemAtivo(itemDocumentos);
                break;
        }

        painelConteudo.revalidate();
        painelConteudo.repaint();
    }

    // TOPBAR COM LOGO, SUBTITULO E BOTAO SAIR
    private JPanel criarTopbar() {
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(COR_SIDEBAR);
        topbar.setPreferredSize(new Dimension(0, 95));
        topbar.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // LOGO CENTRALIZADO EXATAMENTE NA LARGURA DA SIDEBAR (220PX)
        JPanel logoBox = new JPanel(new GridBagLayout());
        logoBox.setOpaque(false);
        logoBox.setPreferredSize(new Dimension(220, 95));
        logoBox.setMaximumSize(new Dimension(220, 95));

        JLabel logoImg = new JLabel();
        // CARREGA O LOGO VIA CLASSPATH USANDO IMAGEIO PARA GARANTIR CARREGAMENTO COMPLETO
        try {
            java.net.URL urlLogo = getClass().getClassLoader().getResource("icons/LogoVigencia.png");
            if (urlLogo != null) {
                BufferedImage original = ImageIO.read(urlLogo);
                Image img = original.getScaledInstance(45, 45, Image.SCALE_SMOOTH);
                logoImg.setIcon(new ImageIcon(img));
            }
        } catch (Exception e) {
            // FALLBACK SILENCIOSO SE ARQUIVO NAO EXISTIR
        }

        JLabel nomeLabel = new JLabel("Vigência");
        nomeLabel.setForeground(Color.WHITE);
        nomeLabel.setFont(new Font(FONTE, Font.BOLD, 22));

        // AGRUPA LOGO + NOME CENTRALIZADOS DENTRO DOS 220PX DA SIDEBAR
        JPanel logoInterno = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        logoInterno.setOpaque(false);
        logoInterno.setPreferredSize(new Dimension(220, 60));
        logoInterno.add(logoImg);
        logoInterno.add(nomeLabel);

        logoBox.add(logoInterno);

        JLabel subtitulo = new JLabel("Sistema de Controle de Documentos Regulatórios");
        subtitulo.setForeground(new Color(150, 170, 200));
        subtitulo.setFont(new Font(FONTE, Font.PLAIN, 12));

        JButton btnSair = new JButton("Sair");
        btnSair.setBackground(new Color(180, 30, 30));
        btnSair.setForeground(Color.WHITE);
        btnSair.setFont(new Font(FONTE, Font.BOLD, 13));
        btnSair.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        btnSair.setFocusPainted(false);
        btnSair.setOpaque(true);
        btnSair.setBorderPainted(false);
        btnSair.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSair.addActionListener(e -> System.exit(0));

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        direita.setOpaque(false);
        direita.add(btnSair);

        JPanel linhaInfo = new JPanel(new BorderLayout());
        linhaInfo.setOpaque(false);
        linhaInfo.add(subtitulo, BorderLayout.WEST);
        linhaInfo.add(direita,   BorderLayout.EAST);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(linhaInfo, gbc);

        topbar.add(logoBox, BorderLayout.WEST);
        topbar.add(wrapper, BorderLayout.CENTER);

        return topbar;
    }

    // SIDEBAR COM MENU LATERAL
    private JPanel criarSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(COR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));

        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(COR_SIDEBAR);
        menu.setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));

        itemDashboard  = criarItemMenu("Dashboard",          "icons/MenuDashboard.png",      "dashboard");
        itemDocumentos = criarItemMenu("Documentos",         "icons/MenuDocumento.png",       "documentos");
        itemEmpresas   = criarItemMenu("Empresas",           "icons/MenuEmpresa.png",         "empresas");
        itemTipos      = criarItemMenu("Tipos de Documento", "icons/MenuTiposDocumentos.png", "tipos");

        menu.add(itemDashboard);
        menu.add(Box.createVerticalStrut(4));
        menu.add(itemDocumentos);
        menu.add(Box.createVerticalStrut(4));
        menu.add(itemEmpresas);
        menu.add(Box.createVerticalStrut(4));
        menu.add(itemTipos);

        // RODAPE COM DATA DE HOJE
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        rodape.setBackground(COR_SIDEBAR);
        String dataHoje = java.time.LocalDate.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        JLabel dataLabel = new JLabel(dataHoje);
        dataLabel.setForeground(new Color(120, 140, 170));
        dataLabel.setFont(new Font(FONTE, Font.PLAIN, 11));
        rodape.add(dataLabel);

        sidebar.add(menu,   BorderLayout.NORTH);
        sidebar.add(rodape, BorderLayout.SOUTH);

        return sidebar;
    }

    // CRIA UM ITEM DO MENU COM NAVEGACAO AO CLICAR
    private JPanel criarItemMenu(String texto, String caminhoIcone, String destino) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        item.setMaximumSize(new Dimension(196, 46));
        item.setPreferredSize(new Dimension(196, 46));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        item.setBackground(COR_SIDEBAR);
        item.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

        // CARREGA O ICONE VIA IMAGEIO PARA GARANTIR CARREGAMENTO COMPLETO E SINCRONO
        JLabel iconeLabel = new JLabel();
        if (caminhoIcone != null) {
            try {
                java.net.URL urlIcone = getClass().getClassLoader().getResource(caminhoIcone);
                if (urlIcone == null) throw new Exception("Nao encontrado: " + caminhoIcone);

                // IMAGEIO.READ CARREGA A IMAGEM COMPLETAMENTE ANTES DE CONTINUAR
                BufferedImage original = ImageIO.read(urlIcone);

                // CRIA IMAGEM DESTINO 20x20 COM CANAL ALPHA
                BufferedImage bi = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = bi.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(original, 0, 0, 20, 20, null);
                g2d.dispose();

                // APLICA FILTRO BRANCO MANTENDO TRANSPARENCIA ORIGINAL
                for (int x = 0; x < 20; x++) {
                    for (int y = 0; y < 20; y++) {
                        int pixel = bi.getRGB(x, y);
                        int alpha = (pixel >> 24) & 0xFF;
                        if (alpha > 0) bi.setRGB(x, y, (alpha << 24) | 0x00FFFFFF);
                    }
                }
                iconeLabel.setIcon(new ImageIcon(bi));
            } catch (Exception e) {
                System.out.println("ERRO AO CARREGAR ICONE: " + e.getMessage());
            }
        }

        JLabel textoLabel = new JLabel(texto);
        textoLabel.setForeground(new Color(160, 175, 200));
        textoLabel.setFont(new Font(FONTE, Font.PLAIN, 13));

        item.add(iconeLabel);
        item.add(textoLabel);

        // NAVEGA PARA A TELA AO CLICAR
        item.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                navegarPara(destino);
            }
        });

        return item;
    }

    // MARCA O ITEM ATIVO NO MENU E DESMARCA OS OUTROS
    private void setItemAtivo(JPanel itemAtivo) {
        JPanel[] itens = {itemDashboard, itemDocumentos, itemEmpresas, itemTipos};
        for (JPanel item : itens) {
            if (item == null) continue;
            boolean ativo = item == itemAtivo;
            item.setBackground(ativo ? COR_ATIVO_BG : COR_SIDEBAR);
            // ATUALIZA COR E PESO DA FONTE DO TEXTO
            for (Component c : item.getComponents()) {
                if (c instanceof JLabel && ((JLabel) c).getIcon() == null) {
                    ((JLabel) c).setForeground(ativo ? Color.WHITE : new Color(160, 175, 200));
                    ((JLabel) c).setFont(new Font(FONTE, ativo ? Font.BOLD : Font.PLAIN, 13));
                }
            }
        }
    }
}