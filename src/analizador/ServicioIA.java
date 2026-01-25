package analizador;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ServicioIA {

    private static final String API_KEY = "AIzaSyBxsjlfCts7ev6VVKMFKA_5GY4yRHWspr4";

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" + API_KEY;

    public String consultarGemini(String instruccionUsuario, String textoEditor){
        try {
            // Limpieza del string
            String promptCombinado = "Contexto del texto: \n" + textoEditor + 
                                     "\n\nInstrucción: " + instruccionUsuario;
            
            String promptSeguro = promptCombinado
                    .replace("\\", "\\\\")   // Escapar barras invertidas
                    .replace("\"", "\\\"")   // Escapar comillas dobles
                    .replace("\n", "\\n")   // Escapar saltos de línea
                    .replace("\r", "");

            // Estructura JSON específica de Google Gemini
            String jsonBody = String.format("""
                {
                  "contents": [{
                    "parts":[{
                      "text": "%s"
                    }]
                  }]
                }
                """, promptSeguro);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return extraerRespuestaGemini(response.body());
            } else {
                return "Error Google API (" + response.statusCode() + "): " + response.body();
            }

        } catch (Exception e) {
            return "Error de conexión: " + e.getMessage();
        }
    }
    
    private String extraerRespuestaGemini(String jsonResponse) {
        try {
            // La respuesta de Gemini viene anidada: candidates -> content -> parts -> text
            // Buscamos el campo "text"
            String marcador = "\"text\": \"";
            int indiceInicio = jsonResponse.indexOf(marcador);
            
            if (indiceInicio == -1) return "La IA respondió, pero no pude leer el texto (Formato inesperado).";
            
            indiceInicio += marcador.length();
            
            
            int indiceFin = indiceInicio;
            boolean escapado = false;
            
            while (indiceFin < jsonResponse.length()) {
                char c = jsonResponse.charAt(indiceFin);
                if (c == '\\') {
                    escapado = !escapado;
                } else if (c == '"' && !escapado) {
                    break; // Fin del string
                } else {
                    escapado = false;
                }
                indiceFin++;
            }

            String contenidoRaw = jsonResponse.substring(indiceInicio, indiceFin);
            
            // Decodificar caracteres especiales de JSON a Texto normal
            return contenidoRaw
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\t", "\t");
                    
        } catch (Exception e) {
            return "Error procesando respuesta de Gemini.";
        }
    }

}
