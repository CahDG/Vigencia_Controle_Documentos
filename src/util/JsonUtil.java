package util;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class JsonUtil {

    // LE O CONTEUDO DE UM ARQUIVO JSON E RETORNA COMO STRING
    public static String lerArquivo(String caminho) {
        try {
            File arquivo = new File(caminho);
            if (!arquivo.exists()) return "[]";
            return new String(Files.readAllBytes(arquivo.toPath()));
        } catch (Exception e) {
            return "[]";
        }
    }

    // SALVA UMA STRING NO ARQUIVO JSON
    public static void salvarArquivo(String caminho, String conteudo) {
        try {
            Files.write(Paths.get(caminho), conteudo.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // EXTRAI O VALOR DE UM CAMPO SIMPLES DO JSON
    public static String extrairValor(String json, String chave) {
        String busca = "\"" + chave + "\":";
        int inicio = json.indexOf(busca);
        if (inicio == -1) return "";
        inicio += busca.length();

        // PULA ESPACOS APOS O DOIS PONTOS
        while (inicio < json.length() && json.charAt(inicio) == ' ') inicio++;

        if (json.charAt(inicio) == '"') {
            // PULA AS ASPAS DE ABERTURA
            inicio++;
            int fim = json.indexOf('"', inicio);
            if (fim == -1) return "";
            return json.substring(inicio, fim).trim();
        } else {
            // VALOR NUMERICO OU BOOLEANO
            int fim = json.indexOf(',', inicio);
            if (fim == -1) fim = json.indexOf('}', inicio);
            return json.substring(inicio, fim).trim();
        }
    }

    // SEPARA OS OBJETOS DE UM ARRAY JSON EM UMA LISTA DE STRINGS
    public static List<String> separarObjetos(String json) {
        List<String> objetos = new ArrayList<>();
        json = json.trim();
        if (json.equals("[]") || json.isEmpty()) return objetos;

        int profundidade = 0;
        int inicio = -1;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (profundidade == 0) inicio = i;
                profundidade++;
            } else if (c == '}') {
                profundidade--;
                if (profundidade == 0 && inicio != -1) {
                    objetos.add(json.substring(inicio, i + 1));
                }
            }
        }
        return objetos;
    }

    // CONVERTE LocalDate PARA STRING PARA SALVAR NO JSON
    public static String dateParaString(LocalDate data) {
        if (data == null) return "";
        return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    // CONVERTE STRING DO JSON PARA LocalDate
    public static LocalDate stringParaDate(String data) {
        if (data == null || data.isEmpty()) return null;
        return LocalDate.parse(data, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}