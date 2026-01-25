package analizador;

import com.formdev.flatlaf.FlatDarculaLaf;
//import com.formdev.flatlaf.FlatLaf;
//import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
//import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalizadorGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private ServicioIA servicioIA = new ServicioIA();

    private JTextArea areaTexto;
    private JTextArea areaNumeros;
    private JTextArea areaResultadosGeneral;
    private JTextArea areaFrecuencia;
    private JTextField txtBuscador;
    private JTabbedPane pestañas;
    private AnalizadorLogica logica;

    // Fuentes y Colores 

    private final Font FUENTE_EDITOR = new Font("Consolas", Font.PLAIN, 16);
    private final Insets MARGENES_EDITOR = new Insets(10, 10, 10, 10);
    private final Insets MARGENES_NUMEROS = new Insets(10, 10, 10, 10);
    
    // Colores del editor
    private final Color COLOR_EDITOR_BG = new Color(30, 30, 30);
    private final Color COLOR_NUMEROS_BG = new Color(45, 48, 50);
    private final Color COLOR_NUMEROS_FG = new Color(120, 120, 120);
    private final Color COLOR_TEXTO = new Color(230, 230, 230);
    
    // Colores de los botones
    private final Color BTN_ACCION_BG = new Color(40, 167, 69); // Verde moderno
    private final Color BTN_ACCION_FG = Color.WHITE;
    private final Color BTN_INFO_BG = new Color(23, 162, 184);   // Azul moderno

    public AnalizadorGUI() {
        logica = new AnalizadorLogica();

        setTitle("Analizador de Texto - UNEG");
        setSize(1380, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- PANEL SUPERIOR ---
        
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        
        JButton btnCargar = crearBoton("Abrir Archivo", null, null);
        JButton btnAnalizar = crearBoton("ANALIZAR", new Color(0, 120, 215), Color.WHITE); // Azulito Claro
        JButton btnGuardar = crearBoton("Guardar Reporte", BTN_ACCION_BG, BTN_ACCION_FG);
        JButton btnLimpiar = crearBoton("Limpiar", null, null);
        
        JButton btnCopiar = crearBoton("Copiar", null, null);
        JButton btnPegar = crearBoton("Pegar", null, null);
        JButton btnOrtografia = crearBoton("Corregir (Web)", null, null);
        JButton btnGuia = crearBoton("Guía", BTN_INFO_BG, Color.WHITE);

        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setFont(UIManager.getFont("h4.font")); // Fuente del sistema
        
        txtBuscador = new JTextField(15);
        txtBuscador.putClientProperty("JTextField.placeholderText", "Escribe para buscar...");
        txtBuscador.putClientProperty("JTextField.showClearButton", true); // Botón X nativo de FlatLaf
        
        JButton btnBuscar = crearBoton("Ir", null, null);

        JButton btnIA = crearBoton("Asistente IA ", new Color(138, 43, 226), Color.WHITE);

        panelTop.add(btnCargar);
        panelTop.add(btnAnalizar);
        panelTop.add(btnGuardar);
        panelTop.add(btnLimpiar);
        panelTop.add(new JToolBar.Separator()); //separador de elementos
        panelTop.add(btnCopiar);
        panelTop.add(btnPegar);
        panelTop.add(new JToolBar.Separator());
        panelTop.add(btnIA);
       // panelTop.add(btnOrtografia);
        panelTop.add(btnGuia);
        panelTop.add(new JToolBar.Separator());
        panelTop.add(lblBuscar);
        panelTop.add(txtBuscador);
        panelTop.add(btnBuscar);

        add(panelTop, BorderLayout.NORTH);

        // --- DIVISOR CENTRAL ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerSize(8); 
        splitPane.setContinuousLayout(true); 

        // --- EDITOR ---
        areaTexto = new JTextArea();
        areaTexto.setBackground(COLOR_EDITOR_BG);
        areaTexto.setForeground(COLOR_TEXTO);
        areaTexto.setCaretColor(Color.WHITE);
        areaTexto.setFont(FUENTE_EDITOR);
        areaTexto.setMargin(MARGENES_EDITOR);
        areaTexto.setLineWrap(false);

        areaNumeros = new JTextArea("1");
        areaNumeros.setBackground(COLOR_NUMEROS_BG);
        areaNumeros.setForeground(COLOR_NUMEROS_FG);
        areaNumeros.setEditable(false);
        areaNumeros.setFont(FUENTE_EDITOR);
        areaNumeros.setMargin(MARGENES_NUMEROS);
        areaNumeros.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JScrollPane scrollTexto = new JScrollPane(areaTexto);
        scrollTexto.setRowHeaderView(areaNumeros);

        
        scrollTexto.setBorder(BorderFactory.createEmptyBorder()); 

        JPanel panelEditor = new JPanel(new BorderLayout());
        
        panelEditor.setBorder(BorderFactory.createTitledBorder(" Editor de Código "));
        panelEditor.add(scrollTexto, BorderLayout.CENTER);

        splitPane.setLeftComponent(panelEditor);

        // --- PESTAÑAS ---
        pestañas = new JTabbedPane();

        pestañas.putClientProperty("JTabbedPane.tabType", "card"); 
        
        areaResultadosGeneral = new JTextArea();
        configurarTextAreaResultados(areaResultadosGeneral);
        pestañas.addTab("Informe Técnico", new JScrollPane(areaResultadosGeneral));

        areaFrecuencia = new JTextArea();
        configurarTextAreaResultados(areaFrecuencia);
        pestañas.addTab("Frecuencia de Letras", new JScrollPane(areaFrecuencia));

        splitPane.setRightComponent(pestañas);
        add(splitPane, BorderLayout.CENTER);

        // --- EVENTOS ---
        btnAnalizar.addActionListener(e -> ejecutarAnalisis());
        btnGuardar.addActionListener(e -> guardarReporte());
        btnLimpiar.addActionListener(e -> limpiarTodo());
        btnCargar.addActionListener(e -> cargarArchivo());
        btnBuscar.addActionListener(e -> ejecutarBusquedaSegura());
        btnOrtografia.addActionListener(e -> abrirCorrectorWeb());
        btnGuia.addActionListener(e -> mostrarGuiaUso());
        btnIA.addActionListener(e -> abrirPanelIA());

        // Atajo: Enter en el buscador activa buscar
        txtBuscador.addActionListener(e -> ejecutarBusquedaSegura());

        btnCopiar.addActionListener(e -> {
            if(areaTexto != null) { areaTexto.selectAll(); areaTexto.copy(); areaTexto.select(0, 0); }
        });
        btnPegar.addActionListener(e -> {
            if(areaTexto != null) { areaTexto.paste(); actualizarNumerosLinea(); }
        });

        areaTexto.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { actualizarNumerosLinea(); }
            public void removeUpdate(DocumentEvent e) { actualizarNumerosLinea(); }
            public void changedUpdate(DocumentEvent e) { actualizarNumerosLinea(); }
        });
    }

    // --- MÉTODOS AUXILIARES ---

  
    private JButton crearBoton(String txt, Color bg, Color fg) {
        JButton btn = new JButton(txt);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        
        if (bg != null) {
            btn.setBackground(bg);
            btn.setForeground(fg != null ? fg : Color.WHITE);
            
            btn.setBorderPainted(false); 
        }
        
        
        btn.putClientProperty("JButton.buttonType", "roundRect"); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        
        btn.setMargin(new Insets(6, 15, 6, 15));
        
        return btn;
    }

    private void configurarTextAreaResultados(JTextArea ta) {
        
        ta.setBackground(new Color(40, 40, 40)); 
        ta.setForeground(new Color(220, 220, 220));
        ta.setEditable(false);
        ta.setFont(new Font("Consolas", Font.PLAIN, 14));
        ta.setMargin(new Insets(15, 15, 15, 15));
    }

    // --- LOGICA ---

    private void guardarReporte() {
        try {
            if (areaResultadosGeneral.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Primero debes ANALIZAR el texto.");
                return;
            }
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar Informe Técnico");
            fileChooser.setSelectedFile(new File("Informe_Analisis.txt"));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File archivo = fileChooser.getSelectedFile();
                if (!archivo.getName().endsWith(".txt")) archivo = new File(archivo.getAbsolutePath() + ".txt");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
                    writer.write(areaResultadosGeneral.getText());
                    writer.write("\n\n=== FRECUENCIA DE LETRAS ===\n");
                    writer.write(areaFrecuencia.getText());
                }
                JOptionPane.showMessageDialog(this, "Informe guardado en:\n" + archivo.getAbsolutePath());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage());
        }
    }

    private void ejecutarBusquedaSegura() {
        try {
            if (txtBuscador == null || areaTexto == null) return;
            String palabra = txtBuscador.getText().trim();
            if (palabra.isEmpty()) return;

            Highlighter h = areaTexto.getHighlighter();
            h.removeAllHighlights();
            Pattern p = Pattern.compile(Pattern.quote(palabra), Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(areaTexto.getText());
            int contador = 0;
            while (m.find()) {
                h.addHighlight(m.start(), m.end(), new DefaultHighlighter.DefaultHighlightPainter(new Color(100, 100, 0))); // Amarillo oscuro para no quemar la vista
                contador++;
            }
            JOptionPane.showMessageDialog(this, "Se encontraron " + contador + " coincidencias.");
        } catch (Exception e) {}
    }

    private void actualizarNumerosLinea() {
        SwingUtilities.invokeLater(() -> {
            if(areaTexto == null) return;
            int lineas = areaTexto.getLineCount();
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= lineas; i++) sb.append(i).append("\n");
            areaNumeros.setText(sb.toString());
        });
    }

    private void mostrarGuiaUso() {
        String guia = """
            MANUAL DE USUARIO
            =================
            
            1. BARRA DE HERRAMIENTAS
            • ABRIR: Carga archivos .txt, .java, etc.
            • ANALIZAR: Procesa el texto (Requerido para guardar).
            • GUARDAR: Exporta los resultados a .txt.
            
            2. BÚSQUEDA
            Escribe en la caja superior y presiona Enter o 'IR'.
            
            3. EDITOR
            Usa Ctrl+C / Ctrl+V libremente.
            """;
        
        JTextArea areaGuia = new JTextArea(guia);
        areaGuia.setEditable(false);
        areaGuia.setFont(new Font("Consolas", Font.PLAIN, 14));
        areaGuia.setMargin(new Insets(10,10,10,10));
        
        JOptionPane.showMessageDialog(this, new JScrollPane(areaGuia), "Ayuda", JOptionPane.INFORMATION_MESSAGE);
    }

    private void abrirCorrectorWeb() {
        try { Desktop.getDesktop().browse(new URI("https://languagetool.org/es")); } catch (Exception e) {}
    }

    private void ejecutarAnalisis() {

        try {
            if(areaTexto == null) return;
            String texto = areaTexto.getText();
            if(texto.trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Texto vacío"); return; }

            if(logica == null) logica = new AnalizadorLogica();
            logica.analizar(texto);

            StringBuilder sb = new StringBuilder();
            sb.append("INFORME TÉCNICO\n");
            sb.append("===============\n\n");
            sb.append("Total Palabras: ").append(logica.getTotalPalabras()).append("\n");
            sb.append("Total Caracteres: ").append(logica.getTotalCaracteres()).append("\n");
            sb.append("Total Líneas: ").append(logica.getTotalLineas()).append("\n");
          
            sb.append("\n(Análisis completo generado correctamente)");

            areaResultadosGeneral.setText(sb.toString());
            areaResultadosGeneral.setCaretPosition(0);

        
            StringBuilder sbFreq = new StringBuilder();
            sbFreq.append("LETRA\tFRECUENCIA\n");
            logica.getFrecuenciaLetras().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> sbFreq.append(e.getKey()).append("\t").append(e.getValue()).append("\n"));
            areaFrecuencia.setText(sbFreq.toString());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al analizar: " + e.getMessage());
        }
    }

    private void limpiarTodo() {
        if(areaTexto != null) areaTexto.setText("");
        if(areaNumeros != null) areaNumeros.setText("1");
        if(areaResultadosGeneral != null) areaResultadosGeneral.setText("");
        if(areaFrecuencia != null) areaFrecuencia.setText("");
        if(txtBuscador != null) txtBuscador.setText("");
        if(areaTexto != null) areaTexto.getHighlighter().removeAllHighlights();
    }

    private void cargarArchivo() {
        JFileChooser fc = new JFileChooser(new File("."));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader br = new BufferedReader(new FileReader(fc.getSelectedFile()))) {
                if(areaTexto != null) {
                    areaTexto.read(br, null);
                    actualizarNumerosLinea();
                    ejecutarAnalisis();
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    private void abrirPanelIA() {
    JDialog dialogoIA = new JDialog(this, "Copiloto de Redacción", false);
    dialogoIA.setSize(500, 600);
    dialogoIA.setLayout(new BorderLayout());

    // --- 1. CONFIGURACIÓN DEL CHAT (Igual que antes) ---
    JEditorPane areaChat = new JEditorPane();
    areaChat.setContentType("text/html");
    areaChat.setEditable(false);
    areaChat.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    areaChat.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
    areaChat.setBackground(new Color(45, 48, 55));
    
    String estiloCSS = "<style>"
            + "body { font-family: 'Segoe UI Emoji', sans-serif; font-size: 14px; color: #E0E0E0; margin: 10px; }"
            + ".user { color: #FFFFFF; font-weight: bold; margin-top: 10px; }"
            + ".gemini { color: #FFD700; font-weight: bold; margin-top: 10px; }"
            + ".msg { margin-bottom: 5px; }"
            + "</style>";

    StringBuilder historialHtml = new StringBuilder();
    historialHtml.append("<html><head>").append(estiloCSS).append("</head><body>");
    historialHtml.append("<div class='gemini'>🤖 ASISTENTE:</div>")
                 .append("<div class='msg'>Hola. Pídeme que <b>reescriba</b> o <b>corrija</b> el texto y usa el botón 'Aplicar' para llevarlo al editor.</div>");
    
    areaChat.setText(historialHtml.toString() + "</body></html>");

    // --- 2. CONTROLES INFERIORES ---
    JTextField txtInput = new JTextField();
    txtInput.putClientProperty("JTextField.placeholderText", "Ej: Corrígelo y dame el código completo...");

    JButton btnEnviar = new JButton("Enviar");
    btnEnviar.setBackground(new Color(138, 43, 226)); // Morado
    btnEnviar.setForeground(Color.WHITE);

    // NUEVO BOTÓN: APLICAR
    JButton btnAplicar = new JButton("Aplicar al Editor");
    btnAplicar.setBackground(new Color(40, 167, 69)); // Verde
    btnAplicar.setForeground(Color.WHITE);
    btnAplicar.setEnabled(false); // Desactivado hasta que la IA responda algo útil

    JPanel panelInput = new JPanel(new BorderLayout(5, 5));
    panelInput.setBorder(new EmptyBorder(10, 10, 10, 10));
    panelInput.add(txtInput, BorderLayout.CENTER);
    
    // Panel para los dos botones
    JPanel panelBotones = new JPanel(new GridLayout(1, 2, 5, 0));
    panelBotones.add(btnEnviar);
    panelBotones.add(btnAplicar);
    panelInput.add(panelBotones, BorderLayout.EAST);

    // Variable para guardar la respuesta "cruda" de la IA (para poder pegarla limpia)
    // Usamos un array de 1 posición para poder modificarla desde dentro del Thread (truco de Java)
    final String[] ultimaRespuestaIA = { "" };

    // --- 3. LÓGICA DE ENVÍO ---
    Runnable accionEnviar = () -> {
        String pregunta = txtInput.getText().trim();
        if (pregunta.isEmpty()) return;

        historialHtml.append("<div class='user'>👤 TÚ:</div>")
                     .append("<div class='msg'>").append(convertirMarkdownAHtml(pregunta)).append("</div>");
        areaChat.setText(historialHtml.toString() + "</body></html>");

        txtInput.setText("");
        txtInput.setEnabled(false);
        btnEnviar.setEnabled(false);
        btnAplicar.setEnabled(false); // Bloquear mientras piensa
        
        SwingUtilities.invokeLater(() -> areaChat.setCaretPosition(areaChat.getDocument().getLength()));

        new Thread(() -> {
    String textoActual = areaTexto.getText();
    
    //prompt para evitar que responda en multiples opciones
    String promptDeSistema = """
            ACTÚA COMO UN MOTOR DE REFACTORIZACIÓN DE CÓDIGO Y TEXTO.
            Tu única función es recibir un texto y una instrucción, y devolver el texto transformado.
            
            REGLAS OBLIGATORIAS:
            1. NO saludes, NO expliques, NO converses.
            2. Tu respuesta debe contener ÚNICAMENTE el resultado final.
            3. Envuelve SIEMPRE el resultado completo en un bloque de código Markdown (```).
            4. Si la instrucción pide corregir, devuelve TODO el texto corregido, no solo las partes cambiadas.
            
            Instrucción del usuario: %s
            """;
    
    // Inyectamos la instrucción del usuario dentro de nuestras reglas rígidas
    String promptFinal = String.format(promptDeSistema, pregunta);
    
    // Enviamos el prompt "trucado" a la IA, pero el usuario solo vio su pregunta original
    String respuesta = servicioIA.consultarGemini(promptFinal, textoActual);
    
    // Guardamos la respuesta cruda para el botón Aplicar
    ultimaRespuestaIA[0] = respuesta;

    SwingUtilities.invokeLater(() -> {
        // Convertimos a HTML para que se vea bonito en el chat
        String respuestaHtml = convertirMarkdownAHtml(respuesta);

        // Si la IA obedeció y mandó solo código, agregamos un mensajito visual para el usuario
        // para que no se vea tan "seco" en el chat, aunque el botón Aplicar usará el código limpio.
        if (!respuestaHtml.contains("Hola") && !respuestaHtml.contains("Aquí")) {
             respuestaHtml = "<i>(Código generado listo para aplicar)</i><br>" + respuestaHtml;
        }

        historialHtml.append("<div class='gemini'>✨ GEMINI:</div>")
                     .append("<div class='msg'>").append(respuestaHtml).append("</div>");

        areaChat.setText(historialHtml.toString() + "</body></html>");
        
        txtInput.setEnabled(true);
        btnEnviar.setEnabled(true);
        btnAplicar.setEnabled(true); 
        txtInput.requestFocus();
        areaChat.setCaretPosition(areaChat.getDocument().getLength());
    });
    }).start();
    };

    // --- 4. LÓGICA DEL BOTÓN APLICAR ---
    btnAplicar.addActionListener(e -> {
        String textoIA = ultimaRespuestaIA[0];
        if (textoIA == null || textoIA.isEmpty()) return;

        int confirm = JOptionPane.showConfirmDialog(dialogoIA, 
            "Esto reemplazará TODO el contenido del editor principal con la respuesta de la IA.\n¿Estás seguro?",
            "Confirmar reemplazo", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Intentar extraer solo el código si viene en formato Markdown (``` ... ```)
            String textoLimpio = extraerContenidoCodigo(textoIA);
            
            areaTexto.setText(textoLimpio);
            actualizarNumerosLinea(); // Actualizar interfaz
            ejecutarAnalisis();       // Re-analizar automáticamente
            dialogoIA.dispose();      // Cerrar ventana de IA (opcional)
        }
    });

    btnEnviar.addActionListener(e -> accionEnviar.run());
    txtInput.addActionListener(e -> accionEnviar.run());

    dialogoIA.add(new JScrollPane(areaChat), BorderLayout.CENTER);
    dialogoIA.add(panelInput, BorderLayout.SOUTH);
    dialogoIA.setLocationRelativeTo(this);
    dialogoIA.setVisible(true);
}

    private String escapeHtml(String s){
        if(s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String convertirMarkdownAHtml(String texto) {
    if (texto == null) return "";
    
    // 1. Escapar HTML propio del texto para evitar inyecciones, 
    // pero manteniendo los saltos de línea para procesarlos después
    String html = texto.replace("&", "&amp;")
                       .replace("<", "&lt;")
                       .replace(">", "&gt;");

    // 2. Convertir Negritas: **texto** -> <b>texto</b>
    html = html.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");

    // 3. Convertir Títulos: ### Texto -> <h3>Texto</h3>
    html = html.replaceAll("### (.*?)(?:\\n|$)", "<h3>$1</h3>");

    // 4. Convertir Listas con viñetas: * Texto -> • Texto
    html = html.replaceAll("(?m)^\\* (.*)$", "&bull; $1");

    // 5. Convertir Listas numéricas (simple): 1. Texto -> <b>1.</b> Texto
    // Esto ayuda a que los números resalten
    html = html.replaceAll("(?m)^(\\d+\\.) (.*)$", "<b>$1</b> $2");

    // 6. Finalmente, convertir saltos de línea a <br>
    html = html.replace("\n", "<br>");

    return html;
 }

    private String extraerContenidoCodigo(String textoBruto) {
    // Patrón para buscar contenido entre tres comillas invertidas ```
    // Pattern.DOTALL permite que el punto (.) incluya saltos de línea
    Pattern pattern = Pattern.compile("```(?:\\w*\\n)?(.*?)```", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(textoBruto);

    if (matcher.find()) {
        // Si encuentra un bloque de código, devuelve SOLO lo de adentro
        return matcher.group(1).trim(); 
    }
    
    // Si no hay bloques de código, devuelve el texto completo (asumiendo que es puro texto)
    return textoBruto;
}

    // --- MAIN ---
    public static void main(String[] args) {
        // CONFIGURACIÓN DE FLATLAF
        try {
          
            FlatDarculaLaf.setup();
            
            
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 10);
            
        } catch (Exception ex) {
            System.err.println("No se pudo iniciar FlatLaf");
        }
        
        
        SwingUtilities.invokeLater(() -> {
            AnalizadorGUI frame = new AnalizadorGUI();
            
            frame.setDefaultLookAndFeelDecorated(true);
            frame.setVisible(true);
        });
    }
}