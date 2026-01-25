package analizador;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ServicioIA {

    private static final String API_KEY = "";

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
    
    private String extraerRespuestaGemini(String jsonResponse){
         try {
        String marcador = "\"text\":";
        int indiceInicio = jsonResponse.indexOf(marcador);
        if (indiceInicio == -1) return "La IA respondió, pero no pude leer el texto (Formato inesperado).";

        indiceInicio += marcador.length();
        while (indiceInicio < jsonResponse.length() && Character.isWhitespace(jsonResponse.charAt(indiceInicio))) {
            indiceInicio++;
        }
        if (indiceInicio >= jsonResponse.length() || jsonResponse.charAt(indiceInicio) != '\"') {
            return "La IA respondió, pero no pude leer el texto (Formato inesperado).";
        }

        indiceInicio++; // saltar comilla inicial

        StringBuilder contenidoRaw = new StringBuilder();
        int i = indiceInicio;
        boolean escapado = false;

        while (i < jsonResponse.length()) {
            char c = jsonResponse.charAt(i);
            if (escapado) {
                contenidoRaw.append('\\').append(c);
                escapado = false;
            } else if (c == '\\') {
                escapado = true;
            } else if (c == '"') {
                break;
            } else {
                contenidoRaw.append(c);
            }
            i++;
        }

        return unescapeJson(contenidoRaw.toString());

    } catch (Exception e) {
        return "Error procesando respuesta de Gemini.";
    }
    }
   
    private String unescapeJson(String input) {
    StringBuilder sb = new StringBuilder(input.length());
    for (int i = 0; i < input.length(); i++) {
        char c = input.charAt(i);
        if (c != '\\') {
            sb.append(c);
            continue;
        }

        if (i + 1 >= input.length()) {
            sb.append('\\');
            break;
        }

        char next = input.charAt(++i);
        switch (next) {
            case '"': sb.append('"'); break;
            case '\\': sb.append('\\'); break;
            case '/': sb.append('/'); break;
            case 'b': sb.append('\b'); break;
            case 'f': sb.append('\f'); break;
            case 'n': sb.append('\n'); break;
            case 'r': sb.append('\r'); break;
            case 't': sb.append('\t'); break;
            case 'u':
                if (i + 4 <= input.length() - 1) {
                    String hex = input.substring(i + 1, i + 5);
                    try {
                        int code = Integer.parseInt(hex, 16);
                        i += 4;

                        if (Character.isHighSurrogate((char) code)
                                && i + 6 < input.length()
                                && input.charAt(i + 1) == '\\'
                                && input.charAt(i + 2) == 'u') {
                            String lowHex = input.substring(i + 3, i + 7);
                            int low = Integer.parseInt(lowHex, 16);
                            if (Character.isLowSurrogate((char) low)) {
                                sb.append(Character.toChars(Character.toCodePoint((char) code, (char) low)));
                                i += 6;
                                break;
                            }
                        }

                        sb.append((char) code);
                    } catch (NumberFormatException ex) {
                        sb.append("\\u").append(hex);
                    }
                } else {
                    sb.append("\\u");
                }
                break;
            default:
                sb.append(next);
        }
    }
    return sb.toString();
}
}
