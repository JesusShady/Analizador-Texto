package analizador;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalizadorLogica {

   
    private int totalCaracteres, totalPalabras, totalParrafos, totalOraciones, totalLineas;
    private int mayusculas, minusculas, digitos;
    private int especiales; 
    private int espaciosBarra;   
    private int saltosLinea;     
    private int tabulaciones;     
    private long pesoBytes;         
    private String tiempoEscritura; 
    private String palabraMasLarga = "";
    private char caracterMasUsado = ' ';
    private int cantidadCaracterMasUsado = 0; 
    private Map<Character, Integer> frecuenciaLetras;
    private String idiomaDetectado;

    
    public void analizar(String texto) {
        if (texto == null || texto.isEmpty()) {
            reiniciar();
            return;
        }

        reiniciar();
        
   
        totalCaracteres = texto.length();
        
        String[] lineas = texto.split("\\r?\\n", -1);
        totalLineas = lineas.length;

        String[] palabras = texto.split("\\s+");
        totalPalabras = (texto.trim().isEmpty()) ? 0 : palabras.length;
        
        String[] parrafos = texto.split("\\n+");
        totalParrafos = (texto.trim().isEmpty()) ? 0 : parrafos.length;
        
        String[] oraciones = texto.split("[.!?]+");
        totalOraciones = (texto.trim().isEmpty()) ? 0 : oraciones.length;

       
        
        // Calcular Peso (Bytes)
        pesoBytes = texto.getBytes().length;
        
        //  Calcular Tiempo de Escritura 
        if (totalPalabras > 0) {
            double minutos = totalPalabras / 40.0;
            int mins = (int) minutos;
            int segs = (int) ((minutos - mins) * 60);
            tiempoEscritura = mins + " min " + segs + " seg";
        } else {
            tiempoEscritura = "0 min 0 seg";
        }

      
        Map<Character, Integer> mapaTodos = new HashMap<>();

        for (char c : texto.toCharArray()) {
            
            if (!Character.isWhitespace(c)) {
                mapaTodos.put(c, mapaTodos.getOrDefault(c, 0) + 1);
            }

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
                char lowerC = Character.toLowerCase(c);
                frecuenciaLetras.put(lowerC, frecuenciaLetras.getOrDefault(lowerC, 0) + 1);
            }
        }

        //  Estadísticas Finales
        for (Map.Entry<Character, Integer> entry : mapaTodos.entrySet()) {
            if (entry.getValue() > cantidadCaracterMasUsado) {
                caracterMasUsado = entry.getKey();
                cantidadCaracterMasUsado = entry.getValue();
            }
        }
        
        for (String p : palabras) {
            String limpia = p.replaceAll("[^a-zA-ZáéíóúÁÉÍÓÚñÑ]", "");
            if (limpia.length() > palabraMasLarga.length()) {
                palabraMasLarga = limpia;
            }
        }
        
        detectarIdioma(texto.toLowerCase());
    }
    
    private void reiniciar() {
        totalLineas = 0; totalCaracteres = 0; totalPalabras = 0;
        mayusculas = 0; minusculas = 0; digitos = 0; especiales = 0;
        espaciosBarra = 0; saltosLinea = 0; tabulaciones = 0;
        palabraMasLarga = "";
        caracterMasUsado = '-';
        cantidadCaracterMasUsado = 0;
        frecuenciaLetras = new HashMap<>();
        idiomaDetectado = "-";
        
        
        pesoBytes = 0;
        tiempoEscritura = "0 min";
    }

   
    public int[] analizarBusqueda(String textoCompleto, String palabraBusqueda) {
        if (textoCompleto.isEmpty() || palabraBusqueda.isEmpty()) return new int[]{0,0};
        String busquedaLower = palabraBusqueda.toLowerCase();
        int contadorTotal = 0;
        Pattern p = Pattern.compile(Pattern.quote(busquedaLower), Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(textoCompleto);
        while (m.find()) contadorTotal++;

        int parrafosConPalabra = 0;
        String[] parrafos = textoCompleto.split("\\n+"); 
        for (String parrafo : parrafos) {
            if (parrafo.toLowerCase().contains(busquedaLower)) parrafosConPalabra++;
        }
        return new int[]{contadorTotal, parrafosConPalabra};
    }

    private void detectarIdioma(String txt) {
        if (txt.contains(" el ") || txt.contains(" la ") || txt.contains(" que ")) idiomaDetectado = "Español";
        else if (txt.contains(" the ") || txt.contains(" and ") || txt.contains(" is ")) idiomaDetectado = "Ingles";
        else idiomaDetectado = "Desconocido";
    }

  
    public int getTotalLineas() { return totalLineas; }
    public int getTotalCaracteres() { return totalCaracteres; }
    public int getTotalPalabras() { return totalPalabras; }
    public int getTotalParrafos() { return totalParrafos; }
    public int getTotalOraciones() { return totalOraciones; }
    public int getMayusculas() { return mayusculas; }
    public int getMinusculas() { return minusculas; }
    public int getDigitos() { return digitos; }
    public int getEspeciales() { return especiales; }
    public int getEspaciosBarra() { return espaciosBarra; }
    public int getSaltosLinea() { return saltosLinea; }
    public int getTabulaciones() { return tabulaciones; }
    public String getPalabraMasLarga() { return palabraMasLarga; }
    public char getCaracterMasUsado() { return caracterMasUsado; }
    public int getCantidadCaracterMasUsado() { return cantidadCaracterMasUsado; }
    public String getIdiomaDetectado() { return idiomaDetectado; }
    public Map<Character, Integer> getFrecuenciaLetras() { return frecuenciaLetras; }
    public long getPesoBytes() { return pesoBytes; }
    public String getTiempoEscritura() { return tiempoEscritura; }
}