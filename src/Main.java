import app.TelaPrincipal;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
 
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // USA O VISUAL NATIVO DO SISTEMA OPERACIONAL
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new TelaPrincipal();
        });
    }
}
 