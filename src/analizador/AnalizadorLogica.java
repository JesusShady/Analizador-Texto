package analizador;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AnalizadorLogica {

    // --- VARIABLES DE ESTADO ---
    // Volumetria basica
    private int totalCaracteres, totalPalabras, totalParrafos, totalOraciones, totalLineas;
    private int totalSilabas;
    
    // Conteo de tipos de caracteres
    private int mayusculas, minusculas, digitos, especiales; 
    private int espaciosBarra, saltosLinea, tabulaciones;     
    
    // Metricas Lexicas
    private int totalPalabrasUnicas;
    private double densidadLexica; 
    private double promedioPalabrasPorFrase;
    private double promedioCaracteresPorPalabra;
    private double promedioSilabasPorPalabra;
    
    // Indices de Legibilidad
    private double indiceLegibilidad;     // Valor numerico
    private String interpretacionLegibilidad; // "Facil", "Dificil", etc.
    
    // Teoria de la Informacion
    private double entropiaShannon;
    
    // Mineria de Datos
    private int cantidadEmails, cantidadURLs, cantidadTelefonos, cantidadFechas, cantidadHashtags, cantidadIPs;
    
    // Extremos y Frecuencias
    private long pesoBytes;         
    private String palabraMasLarga = "";
    private String oracionMasLarga = "";
    private String oracionMasCorta = "";
    private char caracterMasUsado = ' ';
    private int cantidadCaracterMasUsado = 0; 
    
    private Map<Character, Integer> frecuenciaLetras;
    private Map<String, Integer> frecuenciaPalabras; //Top Palabras
    private List<Map.Entry<String, Integer>> topPalabrasMasUsadas;
    
    private String idiomaDetectado;

    // --- METODO PRINCIPAL ---
    public void analizar(String texto) {
        if (texto == null || texto.isEmpty()) {
            reiniciar();
            return;
        }
        reiniciar();
        
        // 1. CARGA INICIAL
        pesoBytes = texto.getBytes().length;
        totalCaracteres = texto.length();
        totalLineas = texto.split("\\r?\\n", -1).length;
        
        // 2. DETECCIÓN DE IDIOMA PRELIMINAR aqui se ajustan las silabas
        detectarIdioma(texto.toLowerCase());
        
        // 3. ANALISIS POR CARÁCTER Y ENTROPÍA
        Map<Character, Integer> conteoChars = new HashMap<>();
        for (char c : texto.toCharArray()) {
            conteoChars.put(c, conteoChars.getOrDefault(c, 0) + 1);
            
            if (Character.isUpperCase(c)) mayusculas++;
            else if (Character.isLowerCase(c)) minusculas++;
            else if (Character.isDigit(c)) digitos++;
            else if (Character.isWhitespace(c)) {
                if (c == ' ') espaciosBarra++;
                else if (c == '\n') saltosLinea++;
                else if (c == '\t') tabulaciones++;
            } else {
                especiales++;
            }
            
            if (Character.isLetter(c)) {
                char lower = Character.toLowerCase(c);
                frecuenciaLetras.put(lower, frecuenciaLetras.getOrDefault(lower, 0) + 1);
            }
        }
        calcularEntropia(conteoChars, totalCaracteres);
        
        // CarActer más usado
        for (Map.Entry<Character, Integer> entry : conteoChars.entrySet()) {
            if (entry.getValue() > cantidadCaracterMasUsado) {
                caracterMasUsado = entry.getKey();
                cantidadCaracterMasUsado = entry.getValue();
            }
        }

        // 4. ANALISIS DE PALABRAS Y SiLABAS
        // Regex para separar palabras limpias (incluye acentos del español)
        String[] palabrasRaw = texto.split("[^a-zA-ZáéíóúÁÉÍÓÚñÑüÜ0-9]+");
        long sumaLongitud = 0;
        
        for (String p : palabrasRaw) {
            if (p.trim().isEmpty()) continue;
            
            totalPalabras++;
            sumaLongitud += p.length();
            totalSilabas += contarSilabas(p); // HeurIstica inteligente
            
            String pLower = p.toLowerCase();
            frecuenciaPalabras.put(pLower, frecuenciaPalabras.getOrDefault(pLower, 0) + 1);
            
            if (p.length() > palabraMasLarga.length()) palabraMasLarga = p;
        }
        
        // Calcular metricas de palabras
        totalPalabrasUnicas = frecuenciaPalabras.size();
        if (totalPalabras > 0) {
            densidadLexica = ((double) totalPalabrasUnicas / totalPalabras) * 100;
            promedioCaracteresPorPalabra = (double) sumaLongitud / totalPalabras;
            promedioSilabasPorPalabra = (double) totalSilabas / totalPalabras;
        }

        // Generar Top 10 Palabras
        topPalabrasMasUsadas = frecuenciaPalabras.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())) // Orden descendente
                .limit(10)
                .collect(Collectors.toList());

        // 5. ANALISIS DE ESTRUCTURA (Parrafos y Oraciones)
        String[] parrafos = texto.split("\\n\\s*\\n+");
        totalParrafos = (texto.trim().isEmpty()) ? 0 : parrafos.length;
        
        // Splitting de oraciones un poco mas robusto
        String[] oraciones = texto.split("(?<=[.!?])\\s+");
        totalOraciones = 0;
        int maxLen = 0;
        int minLen = Integer.MAX_VALUE;
        
        for (String oracion : oraciones) {
            if (oracion.trim().isEmpty()) continue;
            totalOraciones++;
            
            int largo = oracion.length();
            if (largo > maxLen) { maxLen = largo; oracionMasLarga = oracion; }
            if (largo < minLen) { minLen = largo; oracionMasCorta = oracion; }
        }
        if (totalOraciones == 0) oracionMasCorta = "";

        if (totalOraciones > 0) {
            promedioPalabrasPorFrase = (double) totalPalabras / totalOraciones;
        }

        // 6. CALCULO DE LEGIBILIDAD
        calcularIndicesLegibilidad();

        // 7. MINERIA DE DATOS (REGEX AVANZADO)
        cantidadEmails = contarRegex(texto, "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");
        cantidadURLs = contarRegex(texto, "https?://[\\w\\-\\.]+(?:/[\\w\\-./?%&=]*)?");
        // Teléfonos (Formatos comunes internacionales y locales)
        cantidadTelefonos = contarRegex(texto, "(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}");
        // Fechas (dd/mm/yyyy o yyyy-mm-dd)
        cantidadFechas = contarRegex(texto, "\\b(?:\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}|\\d{4}[/-]\\d{1,2}[/-]\\d{1,2})\\b");
        // Hashtags (#Algo)
        cantidadHashtags = contarRegex(texto, "#[a-zA-Z0-9_]+");
        // Direcciones IP v4
        cantidadIPs = contarRegex(texto, "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    }

    // --- ALGORITMOS AUXILIARES ---

    private void calcularEntropia(Map<Character, Integer> mapa, int total) {
        if (total == 0) return;
        double entropia = 0.0;
        for (int count : mapa.values()) {
            double prob = (double) count / total;
            entropia -= prob * (Math.log(prob) / Math.log(2)); // Log base 2
        }
        entropiaShannon = entropia;
    }
    
    private void calcularIndicesLegibilidad() {
        if (totalPalabras == 0 || totalOraciones == 0) return;
        
        if ("Español".equals(idiomaDetectado)) {
            // Índice de Fernández-Huerta (Adaptación del Flesch para Español)
            // 206.84 - (0.60 * P) - (1.02 * F)
            // P = Sílabas por cada 100 palabras
            // F = Frases por cada 100 palabras (Aquí usaremos la inversa: Palabras/Frase)
            
            //La formula estandar suele expresarse como: 
            // 206.84 - 60 * (sílabas/palabras) - 1.02 * (palabras/frases)
            indiceLegibilidad = 206.84 - (60.0 * promedioSilabasPorPalabra) - (1.02 * promedioPalabrasPorFrase);
            
            if (indiceLegibilidad >= 90) interpretacionLegibilidad = "Muy Fácil (Infantil)";
            else if (indiceLegibilidad >= 80) interpretacionLegibilidad = "Fácil (Primaria)";
            else if (indiceLegibilidad >= 70) interpretacionLegibilidad = "Bastante Fácil";
            else if (indiceLegibilidad >= 60) interpretacionLegibilidad = "Estándar";
            else if (indiceLegibilidad >= 50) interpretacionLegibilidad = "Algo Difícil";
            else if (indiceLegibilidad >= 30) interpretacionLegibilidad = "Difícil (Universitario)";
            else interpretacionLegibilidad = "Muy Difícil (Científico)";
            
        } else {
            // Flesch Reading Ease (Inglés/Genérico)
            // 206.835 - 1.015(words/sentences) - 84.6(syllables/words)
            indiceLegibilidad = 206.835 - (1.015 * promedioPalabrasPorFrase) - (84.6 * promedioSilabasPorPalabra);
            
            if (indiceLegibilidad >= 90) interpretacionLegibilidad = "Very Easy (5th grade)";
            else if (indiceLegibilidad >= 80) interpretacionLegibilidad = "Easy (6th grade)";
            else if (indiceLegibilidad >= 60) interpretacionLegibilidad = "Standard (8th-9th grade)";
            else if (indiceLegibilidad >= 30) interpretacionLegibilidad = "Difficult (College)";
            else interpretacionLegibilidad = "Very Difficult (Graduate)";
        }
    }

    private int contarSilabas(String palabra) {
        // Heuristica simple: Contar grupos de vocales
        // Funciona decentemente para estimaciones estadisticas en Esp/Ing
        String clean = palabra.toLowerCase().replaceAll("[^a-zxcvbnmasdfghjklqwertyuiopáéíóúü]", "");
        if (clean.length() <= 3) return 1; // Palabras muy cortas suelen ser 1 sílaba
        
        // Quitar 'e' muda al final si es ingles (aproximación) 
        if (!idiomaDetectado.equals("Español") && clean.endsWith("e")) {
            clean = clean.substring(0, clean.length() - 1);
        }

        // Regex para encontrar diptongos o vocales solas
        Matcher m = Pattern.compile("[aeiouáéíóúüy]+").matcher(clean);
        int count = 0;
        while (m.find()) count++;
        
        return Math.max(1, count);
    }
    
    private int contarRegex(String texto, String regex) {
        Matcher m = Pattern.compile(regex).matcher(texto);
        int count = 0;
        while (m.find()) count++;
        return count;
    }
    
    private void detectarIdioma(String txt) {
        // Método simple pero efectivo por palabras clave
        if (txt.contains(" el ") || txt.contains(" la ") || txt.contains(" que ") || txt.contains(" y ")) idiomaDetectado = "Español";
        else if (txt.contains(" the ") || txt.contains(" and ") || txt.contains(" is ") || txt.contains(" of ")) idiomaDetectado = "Ingles";
        else idiomaDetectado = "Desconocido/Otro";
    }

    private void reiniciar() {
        totalLineas = 0; totalCaracteres = 0; totalPalabras = 0; totalParrafos = 0; totalOraciones = 0;
        mayusculas = 0; minusculas = 0; digitos = 0; especiales = 0;
        espaciosBarra = 0; saltosLinea = 0; tabulaciones = 0;
        totalPalabrasUnicas = 0; densidadLexica = 0;
        promedioPalabrasPorFrase = 0; promedioCaracteresPorPalabra = 0; promedioSilabasPorPalabra = 0;
        cantidadEmails = 0; cantidadURLs = 0; cantidadTelefonos = 0; cantidadFechas = 0; cantidadHashtags = 0; cantidadIPs = 0;
        palabraMasLarga = "N/A"; oracionMasLarga = ""; oracionMasCorta = "";
        caracterMasUsado = '-'; cantidadCaracterMasUsado = 0;
        frecuenciaLetras = new HashMap<>();
        frecuenciaPalabras = new HashMap<>();
        topPalabrasMasUsadas = new ArrayList<>();
        idiomaDetectado = "-";
        pesoBytes = 0;
        entropiaShannon = 0;
        indiceLegibilidad = 0; interpretacionLegibilidad = "N/A";
    }

    // --- GETTERS ---
    public int getTotalLineas() { return totalLineas; }
    public int getTotalCaracteres() { return totalCaracteres; }
    public int getTotalPalabras() { return totalPalabras; }
    public int getTotalParrafos() { return totalParrafos; }
    public int getTotalOraciones() { return totalOraciones; }
    public int getTotalSilabas() { return totalSilabas; }
    public int getMayusculas() { return mayusculas; }
    public int getMinusculas() { return minusculas; }
    public int getDigitos() { return digitos; }
    public int getEspeciales() { return especiales; }
    public String getPalabraMasLarga() { return palabraMasLarga; }
    public String getOracionMasLarga() { return oracionMasLarga; }
    public String getOracionMasCorta() { return oracionMasCorta; }
    public char getCaracterMasUsado() { return caracterMasUsado; }
    public Map<Character, Integer> getFrecuenciaLetras() { return frecuenciaLetras; }
    public List<Map.Entry<String, Integer>> getTopPalabrasMasUsadas() { return topPalabrasMasUsadas; }
    public long getPesoBytes() { return pesoBytes; }
    public int getTotalPalabrasUnicas() { return totalPalabrasUnicas; }
    public double getDensidadLexica() { return densidadLexica; }
    public double getPromedioPalabrasPorFrase() { return promedioPalabrasPorFrase; }
    public double getPromedioCaracteresPorPalabra() { return promedioCaracteresPorPalabra; }
    public double getPromedioSilabasPorPalabra() { return promedioSilabasPorPalabra; }
    public double getIndiceLegibilidad() { return indiceLegibilidad; }
    public String getInterpretacionLegibilidad() { return interpretacionLegibilidad; }
    public double getEntropiaShannon() { return entropiaShannon; }
    public int getCantidadEmails() { return cantidadEmails; }
    public int getCantidadURLs() { return cantidadURLs; }
    public int getCantidadTelefonos() { return cantidadTelefonos; }
    public int getCantidadFechas() { return cantidadFechas; }
    public int getCantidadHashtags() { return cantidadHashtags; }
    public int getCantidadIPs() { return cantidadIPs; }
    public String getIdiomaDetectado() { return idiomaDetectado; }
}