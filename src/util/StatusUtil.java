package util;

import java.awt.Color;
import java.time.LocalDate;

public class StatusUtil {

    // CALCULA O STATUS DO DOCUMENTO COM BASE NA DATA DE VENCIMENTO ATUAL
    public static String calcularStatus(LocalDate dataVencimento, int diasAntecedencia) {
        LocalDate hoje = LocalDate.now();

        if (hoje.isAfter(dataVencimento)) {
            return "Vencido";
        }

        LocalDate inicioAlerta = dataVencimento.minusDays(diasAntecedencia);

        if (!hoje.isBefore(inicioAlerta)) {
            return "A Vencer";
        }

        return "Válido";
    }

    // RETORNA A COR ASSOCIADA AO STATUS PARA USO NAS TELAS
    public static Color corDoStatus(String status) {
        switch (status) {
            case "Vencido":  return new Color(220, 53, 69);  // VERMELHO
            case "A Vencer": return new Color(255, 200, 0);  // AMARELO
            case "Válido":   return new Color(40, 167, 69);  // VERDE
            default:         return Color.GRAY;
        }
    }
}