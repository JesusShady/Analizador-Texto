# Copilot instructions for Analizador de Texto

## Panorama del proyecto
- App de escritorio Java Swing con UI moderna (FlatLaf) y un editor tipo “código”. La UI vive en [src/analizador/AnalizadorGUI.java](src/analizador/AnalizadorGUI.java).
- La lógica de análisis lingüístico y métricas está aislada en [src/analizador/AnalizadorLogica.java](src/analizador/AnalizadorLogica.java). La GUI solo consume getters tras llamar `analizar()`.
- La integración IA (Gemini) está encapsulada en [src/analizador/ServicioIA.java](src/analizador/ServicioIA.java). Se arma el prompt con texto del editor + instrucción del usuario y se hace POST HTTP.

## Flujo de datos principal
- `AnalizadorGUI` toma el texto del editor, llama `AnalizadorLogica.analizar(texto)` y construye el reporte en el panel “Informe Técnico”.
- La pestaña de frecuencia se construye con `getTopPalabrasMasUsadas()` y `getFrecuenciaLetras()`.
- El panel IA (en GUI) debe delegar toda llamada remota a `ServicioIA.consultarGemini()` y manejar solo presentación.

## Convenciones y patrones del código
- UI Swing con estilo oscuro fijo: colores/`Font` constantes al inicio de `AnalizadorGUI`.
- Reportes son strings formateados manualmente (no usar plantillas externas). Mantener el formato ASCII y encabezados.
- `AnalizadorLogica` reinicia estado en `reiniciar()` antes de cada análisis; no almacenar estado fuera de esa clase.
- Detección de idioma es heurística simple en `detectarIdioma()`; no introducir dependencias nuevas para NLP.
- La extracción de respuesta IA es parseo manual del JSON (`extraerRespuestaGemini()`); si se cambia, mantener compatibilidad con el formato actual.

## Integraciones y dependencias
- Gemini API vía `java.net.http.HttpClient`. La clave API está actualmente embebida como `API_KEY` en `ServicioIA`.
- Look & Feel: `FlatDarculaLaf` importado en la GUI (no hay framework adicional).

## Puntos sensibles al editar
- No romper el contrato de getters de `AnalizadorLogica`; la GUI los usa directamente.
- Evitar cambios que alteren el conteo base (caracteres, palabras, líneas) sin actualizar el formato del informe.
- Mantener el editor sin “line wrap” y el panel de números sincronizado con `actualizarNumerosLinea()`.

## Dónde mirar para ejemplos
- Construcción del informe: método `ejecutarAnalisis()` en [src/analizador/AnalizadorGUI.java](src/analizador/AnalizadorGUI.java).
- Cálculo de legibilidad y métricas: [src/analizador/AnalizadorLogica.java](src/analizador/AnalizadorLogica.java).
- Request HTTP a Gemini y parseo de respuesta: [src/analizador/ServicioIA.java](src/analizador/ServicioIA.java).
