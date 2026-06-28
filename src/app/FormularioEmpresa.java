package app;

import modelos.Empresa;
import repositorio.EmpresaRepositorio;
import util.LocalidadeUtil;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class FormularioEmpresa extends JDialog {

    // CORES E FONTE DO FORMULARIO
    private static final Color COR_SIDEBAR  = new Color(5, 32, 74);
    private static final Color COR_VERDE    = new Color(22, 163, 74);
    private static final Color COR_VERMELHO = new Color(180, 30, 30);
    private static final String FONTE       = "Segoe UI";

    // SEGMENTOS FIXOS — ADICIONE AQUI SE PRECISAR DE MAIS
    private static final String[] SEGMENTOS = {"Selecione...", "Clínica", "Distribuidora", "Hospital", "Outros"};

    private EmpresaRepositorio repositorio;
    private Empresa empresa;

    // CAMPOS DO FORMULARIO — LIMITES CONFORME DER LOGICO
    private JTextField campoNome = criarCampo(150); // RAZAO_SOCIAL: VARCHAR(150)
    private JTextField campoCnpj = criarCampoCnpj(); // CNPJ: CHAR(18) COM MASCARA AUTOMATICA
    private JComboBox<String> comboSegmento = new JComboBox<>(SEGMENTOS);
    private JComboBox<String> comboUF       = new JComboBox<>();
    private JComboBox<String> comboCidade   = new JComboBox<>();

    public FormularioEmpresa(JFrame pai, Empresa empresa, EmpresaRepositorio repositorio) {
        super(pai, empresa == null ? "Nova Empresa" : "Editar Empresa", true);
        this.empresa     = empresa;
        this.repositorio = repositorio;
        initialize();
        if (empresa != null) preencherCampos();
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
        JLabel titulo = new JLabel(empresa == null ? "Nova Empresa" : "Editar Empresa");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font(FONTE, Font.BOLD, 16));
        cabecalho.add(titulo, BorderLayout.WEST);

        // PAINEL DOS CAMPOS
        JPanel campos = new JPanel(new GridLayout(10, 1, 0, 6));
        campos.setBackground(Color.WHITE);
        campos.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // CONFIGURA OS COMBOS
        configurarCombo(comboSegmento);
        configurarCombo(comboUF);
        configurarCombo(comboCidade);
        comboCidade.setEnabled(false);

        // CARREGA OS ESTADOS DO ARQUIVO IBGE
        comboUF.addItem("Selecione a UF...");
        for (String sigla : LocalidadeUtil.listarSiglasEstados()) {
            comboUF.addItem(sigla);
        }
        comboCidade.addItem("Selecione a cidade...");

        // ATUALIZA AS CIDADES QUANDO A UF MUDAR
        comboUF.addActionListener(e -> atualizarCidades());

        // ADICIONA OS CAMPOS NA ORDEM
        campos.add(criarLabel("Razão Social *"));
        campos.add(campoNome);
        campos.add(criarLabel("CNPJ *"));
        campos.add(campoCnpj);
        campos.add(criarLabel("Segmento *"));
        campos.add(comboSegmento);
        campos.add(criarLabel("UF *"));
        campos.add(comboUF);
        campos.add(criarLabel("Cidade *"));
        campos.add(comboCidade);

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

    // APLICA FONTE E PADDING INTERNO NO COMBO
    private void configurarCombo(JComboBox<String> combo) {
        combo.setFont(new Font(FONTE, Font.PLAIN, 13));
        combo.setRenderer(new BasicComboBoxRenderer() {
            public Component getListCellRendererComponent(JList list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                return this;
            }
        });
    }

    // ATUALIZA O COMBO DE CIDADES COM BASE NA UF SELECIONADA
    private void atualizarCidades() {
        String ufSelecionada = (String) comboUF.getSelectedItem();
        comboCidade.removeAllItems();

        if (ufSelecionada == null || ufSelecionada.equals("Selecione a UF...")) {
            comboCidade.addItem("Selecione a cidade...");
            comboCidade.setEnabled(false);
            return;
        }

        // CARREGA AS CIDADES DO ESTADO SELECIONADO
        List<String> cidades = LocalidadeUtil.listarMunicipios(ufSelecionada);
        comboCidade.addItem("Selecione a cidade...");
        for (String cidade : cidades) {
            comboCidade.addItem(cidade);
        }
        comboCidade.setEnabled(true);
    }

    private JLabel criarLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font(FONTE, Font.BOLD, 12));
        lbl.setForeground(new Color(60, 70, 90));
        return lbl;
    }

    // PREENCHE OS CAMPOS QUANDO FOR EDICAO — USA BUSCA POR INDICE PARA GARANTIR O PREENCHIMENTO
    private void preencherCampos() {
        campoNome.setText(empresa.getNome());
        campoCnpj.setText(empresa.getCnpj());

        // SELECIONA O SEGMENTO PERCORRENDO OS ITENS
        for (int i = 0; i < comboSegmento.getItemCount(); i++) {
            if (comboSegmento.getItemAt(i).equalsIgnoreCase(empresa.getSegmento())) {
                comboSegmento.setSelectedIndex(i);
                break;
            }
        }

        // REMOVE O LISTENER TEMPORARIAMENTE PARA NAO DISPARAR ANTES DA HORA
        ActionListener[] listeners = comboUF.getActionListeners();
        for (ActionListener l : listeners) comboUF.removeActionListener(l);

        // SELECIONA A UF PERCORRENDO OS ITENS
        for (int i = 0; i < comboUF.getItemCount(); i++) {
            if (comboUF.getItemAt(i).equalsIgnoreCase(empresa.getUf())) {
                comboUF.setSelectedIndex(i);
                break;
            }
        }

        // CARREGA AS CIDADES DA UF MANUALMENTE
        atualizarCidades();

        // RECOLOCA O LISTENER
        for (ActionListener l : listeners) comboUF.addActionListener(l);

        // SELECIONA A CIDADE PERCORRENDO OS ITENS
        for (int i = 0; i < comboCidade.getItemCount(); i++) {
            if (comboCidade.getItemAt(i).equalsIgnoreCase(empresa.getCidade())) {
                comboCidade.setSelectedIndex(i);
                break;
            }
        }
    }

    // VALIDA OS CAMPOS E SALVA NO REPOSITORIO
    private void salvar() {
        if (campoNome.getText().trim().isEmpty()) {
            mostrarErro("O nome é obrigatório."); return;
        }
        if (campoCnpj.getText().trim().isEmpty()) {
            mostrarErro("O CNPJ é obrigatório."); return;
        }
        if (!validarCnpj(campoCnpj.getText().trim())) {
            mostrarErro("CNPJ inválido. Verifique o número digitado."); return;
        }

        // VERIFICA SE JA EXISTE OUTRA EMPRESA COM O MESMO CNPJ
        String cnpjDigitado = campoCnpj.getText().trim();
        for (Empresa e : repositorio.listarTodas()) {
            boolean mesmoCnpj    = e.getCnpj().equals(cnpjDigitado);
            boolean outraEmpresa = empresa == null || e.getId() != empresa.getId();
            if (mesmoCnpj && outraEmpresa) {
                mostrarErro("Já existe uma empresa cadastrada com esse CNPJ."); return;
            }
        }

        if (comboSegmento.getSelectedIndex() == 0) {
            mostrarErro("Selecione o segmento."); return;
        }
        if (comboUF.getSelectedIndex() == 0) {
            mostrarErro("Selecione a UF."); return;
        }
        if (comboCidade.getSelectedIndex() == 0) {
            mostrarErro("Selecione a cidade."); return;
        }

        if (empresa == null) empresa = new Empresa();
        empresa.setNome(campoNome.getText().trim());
        empresa.setCnpj(campoCnpj.getText().trim());
        empresa.setSegmento((String) comboSegmento.getSelectedItem());
        empresa.setUf((String) comboUF.getSelectedItem());
        empresa.setCidade((String) comboCidade.getSelectedItem());

        // SALVA OU EDITA DEPENDENDO SE E UMA EMPRESA NOVA OU EXISTENTE
        if (empresa.getId() == 0) {
            repositorio.adicionar(empresa);
        } else {
            repositorio.editar(empresa);
        }

        this.dispose();
    }

    // VALIDA O CNPJ: FORMATO E DIGITOS VERIFICADORES MATEMATICOS
    private boolean validarCnpj(String cnpj) {
        // VERIFICA O FORMATO XX.XXX.XXX/XXXX-XX
        if (!cnpj.matches("\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}")) return false;

        // REMOVE OS CARACTERES NAO NUMERICOS PARA CALCULAR OS DIGITOS
        String nums = cnpj.replaceAll("[^0-9]", "");

        // CNPJ NAO PODE TER TODOS OS DIGITOS IGUAIS (EX: 00.000.000/0000-00)
        if (nums.matches("(\\d)\\1{13}")) return false;

        // CALCULA O PRIMEIRO DIGITO VERIFICADOR
        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < 12; i++) soma += Character.getNumericValue(nums.charAt(i)) * pesos1[i];
        int digito1 = soma % 11 < 2 ? 0 : 11 - (soma % 11);
        if (Character.getNumericValue(nums.charAt(12)) != digito1) return false;

        // CALCULA O SEGUNDO DIGITO VERIFICADOR
        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        soma = 0;
        for (int i = 0; i < 13; i++) soma += Character.getNumericValue(nums.charAt(i)) * pesos2[i];
        int digito2 = soma % 11 < 2 ? 0 : 11 - (soma % 11);
        return Character.getNumericValue(nums.charAt(13)) == digito2;
    }

    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    // CRIA CAMPO CNPJ COM MASCARA AUTOMATICA XX.XXX.XXX/XXXX-XX
    private static JTextField criarCampoCnpj() {
        JTextField campo = new JTextField();
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campo.setPreferredSize(new Dimension(400, 34));
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 225)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // MASCARA QUE FORMATA O CNPJ AUTOMATICAMENTE CONFORME O USUARIO DIGITA
        campo.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            boolean atualizando = false;
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { formatar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  {}
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
            private void formatar() {
                if (atualizando) return;
                SwingUtilities.invokeLater(() -> {
                    atualizando = true;
                    // REMOVE TUDO QUE NAO FOR NUMERO
                    String texto = campo.getText().replaceAll("[^0-9]", "");
                    // LIMITA A 14 DIGITOS
                    if (texto.length() > 14) texto = texto.substring(0, 14);
                    // APLICA A MASCARA XX.XXX.XXX/XXXX-XX
                    StringBuilder sb = new StringBuilder(texto);
                    if (sb.length() > 2)  sb.insert(2, ".");
                    if (sb.length() > 6)  sb.insert(6, ".");
                    if (sb.length() > 10) sb.insert(10, "/");
                    if (sb.length() > 15) sb.insert(15, "-");
                    campo.setText(sb.toString());
                    atualizando = false;
                });
            }
        });

        return campo;
    }

    // CRIA UM CAMPO DE TEXTO COM LIMITE DE CARACTERES CONFORME O DER LOGICO
    private static JTextField criarCampo(int limite) {
        JTextField campo = new JTextField();
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campo.setPreferredSize(new Dimension(400, 34));
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
}