package analizador;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalizadorGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    
 
    private JTextArea areaTexto;
    private JTextArea areaNumeros; 
    private JTextArea areaResultadosGeneral;
    private JTextArea areaFrecuencia;       
    private JTextField txtBuscador;
    private JTabbedPane pestañas;    
    private AnalizadorLogica logica;         

 
    private final Font FUENTE_EDITOR = new Font("Consolas", Font.PLAIN, 16);
    private final Insets MARGENES_EDITOR = new Insets(5, 10, 5, 10); 
    private final Insets MARGENES_NUMEROS = new Insets(5, 10, 5, 10); 
    private final Color COLOR_FONDO_APP = new Color(40, 40, 40);
    private final Color COLOR_EDITOR_BG = new Color(30, 30, 30);
    private final Color COLOR_NUMEROS_BG = new Color(50, 50, 50);
    private final Color COLOR_NUMEROS_FG = new Color(160, 160, 160);
    private final Color COLOR_TEXTO = new Color(230, 230, 230);
    private final Color BTN_FONDO = new Color(225, 225, 225); 
    private final Color BTN_TEXTO = Color.BLACK;
    private final Color BTN_ACCION = new Color(144, 238, 144); 
    private final Color BTN_GUIA = new Color(173, 216, 230); 

    public AnalizadorGUI() {
        logica = new AnalizadorLogica(); 

        setTitle("ANALIZADOR DE TEXTO - UNEG");
        setSize(1380, 850); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- PANEL SUPERIOR ---
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        panelTop.setBackground(COLOR_FONDO_APP);
        JButton btnCargar = crearBoton("ABRIR", BTN_FONDO, BTN_TEXTO);
        JButton btnGuardar = crearBoton("GUARDAR REPORTE", BTN_ACCION, Color.BLACK); 
        JButton btnAnalizar = crearBoton("ANALIZAR", BTN_FONDO, BTN_TEXTO);
        JButton btnLimpiar = crearBoton("LIMPIAR", BTN_FONDO, BTN_TEXTO);
        JButton btnCopiar = crearBoton("COPIAR", BTN_FONDO, BTN_TEXTO);
        JButton btnPegar = crearBoton("PEGAR", BTN_FONDO, BTN_TEXTO);
        JButton btnOrtografia = crearBoton("CORREGIR (WEB)", BTN_FONDO, BTN_TEXTO);
        JButton btnGuia = crearBoton("GUÍA", BTN_GUIA, Color.BLACK);
        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setForeground(Color.WHITE);
        lblBuscar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        txtBuscador = new JTextField(10);
        txtBuscador.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtBuscador.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JButton btnBuscar = crearBoton("IR", BTN_FONDO, BTN_TEXTO);

       
        panelTop.add(btnCargar);
        panelTop.add(btnAnalizar);
        panelTop.add(btnGuardar); 
        panelTop.add(btnLimpiar);
        panelTop.add(crearSeparador()); 
        panelTop.add(btnCopiar);
        panelTop.add(btnPegar);
        panelTop.add(crearSeparador()); 
        panelTop.add(btnOrtografia);
        panelTop.add(btnGuia); 
        panelTop.add(crearSeparador()); 
        panelTop.add(lblBuscar);
        panelTop.add(txtBuscador);
        panelTop.add(btnBuscar);

        add(panelTop, BorderLayout.NORTH);

       
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.55); 
        splitPane.setDividerSize(5);
        splitPane.setBackground(COLOR_FONDO_APP);

       
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
        scrollTexto.setBorder(null);
        
        JPanel panelEditor = new JPanel(new BorderLayout());
        panelEditor.setBackground(COLOR_FONDO_APP);
        panelEditor.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), 
                " Editor de Código ", 0, 0, new Font("Segoe UI", Font.BOLD, 12), Color.WHITE));
        panelEditor.add(scrollTexto, BorderLayout.CENTER);
        
        splitPane.setLeftComponent(panelEditor);

        
        pestañas = new JTabbedPane(); 
        pestañas.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pestañas.setBackground(Color.LIGHT_GRAY);
        
        areaResultadosGeneral = new JTextArea();
        configurarTextAreaResultados(areaResultadosGeneral);
        pestañas.addTab(" Informe Técnico ", new JScrollPane(areaResultadosGeneral));

        areaFrecuencia = new JTextArea();
        configurarTextAreaResultados(areaFrecuencia);
        pestañas.addTab(" Frecuencia de Letras ", new JScrollPane(areaFrecuencia));

        splitPane.setRightComponent(pestañas);
        add(splitPane, BorderLayout.CENTER);

       
        btnAnalizar.addActionListener(e -> ejecutarAnalisis());
        btnGuardar.addActionListener(e -> guardarReporte()); 
        btnLimpiar.addActionListener(e -> limpiarTodo());
        btnCargar.addActionListener(e -> cargarArchivo());
        btnBuscar.addActionListener(e -> ejecutarBusquedaSegura()); 
        btnOrtografia.addActionListener(e -> abrirCorrectorWeb());
        btnGuia.addActionListener(e -> mostrarGuiaUso());
        
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

    // --- NUEVO MÉTODO PARA GUARDAR REPORTE ---
    private void guardarReporte() {
        try {
            if (areaResultadosGeneral.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Primero debes ANALIZAR el texto para generar un reporte.");
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar Informe Técnico");
            fileChooser.setSelectedFile(new File("Informe_Analisis.txt"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File archivo = fileChooser.getSelectedFile();
                if (!archivo.getName().endsWith(".txt")) {
                    archivo = new File(archivo.getAbsolutePath() + ".txt");
                }
                
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
                    writer.write(areaResultadosGeneral.getText());
                    
                    writer.write("\n\n=== FRECUENCIA DE LETRAS ===\n");
                    writer.write(areaFrecuencia.getText());
                }
                
                JOptionPane.showMessageDialog(this, "Informe guardado exitosamente en:\n" + archivo.getAbsolutePath());
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
                h.addHighlight(m.start(), m.end(), new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
                contador++;
            }

            if (areaResultadosGeneral != null) {
                areaResultadosGeneral.append("\n-----------------------------------------\n");
                areaResultadosGeneral.append(" BUSQUEDA: \"" + palabra + "\"\n");
                areaResultadosGeneral.append(" Coincidencias encontradas: " + contador + "\n");
                areaResultadosGeneral.append("-----------------------------------------\n");
                areaResultadosGeneral.setCaretPosition(areaResultadosGeneral.getDocument().getLength());
            }
            if (pestañas != null) pestañas.setSelectedIndex(0);

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
            StringBuilder sb = new StringBuilder();
            
            sb.append(" MANUAL DE USUARIO \n");
            sb.append("==================================================\n\n");
            
            sb.append("1. BARRA DE HERRAMIENTAS PRINCIPAL\n");
            sb.append("--------------------------------------------------\n");
            sb.append(" • ABRIR: \n");
            sb.append("   Abre un explorador de archivos para cargar documentos de texto\n");
            sb.append("   (.txt, .java, .md, etc.) directamente en el editor.\n\n");
            
            sb.append(" • ANALIZAR (Boton Central): \n");
            sb.append("   Es el corazon del sistema. Procesa el texto actual y calcula:\n");
            sb.append("   Conteo de palabras, estadisticas, métricas y frecuencias.\n");
            sb.append("   IMPORTANTE: Debes pulsar esto antes de Guardar.\n\n");
            
            sb.append(" • GUARDAR REPORTE (Verde): \n");
            sb.append("   Genera un archivo externo (.txt) con todos los resultados\n");
            sb.append("   del informe técnico y la tabla de frecuencias.\n\n");
            
            sb.append(" • LIMPIAR: \n");
            sb.append("   Borra el editor, reinicia los contadores y elimina los\n");
            sb.append("   resaltados de busqueda. Deja la app lista para empezar de cero.\n\n");
            
            sb.append("2. HERRAMIENTAS DE EDICION\n");
            sb.append("--------------------------------------------------\n");
            sb.append(" • COPIAR / PEGAR: \n");
            sb.append("   Botones rápidos para gestionar el portapapeles sin usar\n");
            sb.append("   atajos de teclado (Ctrl+C / Ctrl+V).\n\n");
            
            sb.append(" • CORREGIR (WEB): \n");
            sb.append("   Abre el navegador con 'LanguageTool', una herramienta\n");
            sb.append("   externa para revisar ortografía y gramatica.\n\n");

            sb.append("3. BUSQUEDA INTELIGENTE\n");
            sb.append("--------------------------------------------------\n");
            sb.append("   Escribe una palabra en el campo superior derecho y pulsa 'IR'.\n");
            sb.append("   El sistema resaltará en AMARILLO todas las coincidencias\n");
            sb.append("   en el editor y te dirá cuántas veces aparece.\n\n");
            
            sb.append("4. PESTAÑAS DE RESULTADOS\n");
            sb.append("--------------------------------------------------\n");
            sb.append(" • Informe Técnico: Resumen detallado y estetico.\n");
            sb.append(" • Frecuencia: Tabla exacta de cuántas veces aparece cada letra.\n");

            JTextArea areaGuia = new JTextArea(sb.toString());
            areaGuia.setEditable(false);
            areaGuia.setFont(new Font("Consolas", Font.PLAIN, 14));
            areaGuia.setBackground(new Color(245, 245, 245));
            areaGuia.setMargin(new Insets(10, 10, 10, 10));
            areaGuia.setCaretPosition(0); 

          
            JScrollPane scrollGuia = new JScrollPane(areaGuia);
            scrollGuia.setPreferredSize(new Dimension(600, 400)); 

            JOptionPane.showMessageDialog(this, scrollGuia, 
                    "Guía de Uso del Sistema", JOptionPane.INFORMATION_MESSAGE);
        }
    
    private void abrirCorrectorWeb() {
        try { Desktop.getDesktop().browse(new URI("https://languagetool.org/es")); } catch (Exception e) {}
    }

  
    private void ejecutarAnalisis() {
        try {
            if(areaTexto == null) return;
            String texto = areaTexto.getText();
            if(texto.trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Texto vacio"); return; }
            
            if(logica == null) logica = new AnalizadorLogica();
            logica.analizar(texto);
            
            
            String palabraLarga = logica.getPalabraMasLarga();
            int longitudLarga = (palabraLarga != null) ? palabraLarga.length() : 0;
            
            
            StringBuilder sb = new StringBuilder();
            sb.append("╔════════════════════════════════════════════════════╗\n");
            sb.append("║            INFORME TECNICO DE ANÁLISIS             ║\n");
            sb.append("╚════════════════════════════════════════════════════╝\n\n");
            
            sb.append(" 1. ESTADISTICAS ESTRUCTURALES\n");
            sb.append(" ────────────────────────────────────────────────────\n");
            sb.append(String.format("  • Total de Palabras    :  %d\n", logica.getTotalPalabras()));
            sb.append(String.format("  • Total de Caracteres  :  %d\n", logica.getTotalCaracteres()));
            sb.append(String.format("  • Líneas de Texto      :  %d\n", logica.getTotalLineas()));
            sb.append(String.format("  • Parrafos Detectados  :  %d\n", logica.getTotalParrafos()));
            sb.append(String.format("  • Oraciones            :  %d\n", logica.getTotalOraciones()));
            sb.append("\n");

            sb.append(" 2. ANALISIS DE ESPACIADO Y FORMATO\n");
            sb.append(" ────────────────────────────────────────────────────\n");
            sb.append(String.format("  • Espacios en Blanco   :  %d\n", logica.getEspaciosBarra()));
            sb.append(String.format("  • Saltos de Línea      :  %d\n", logica.getSaltosLinea()));
            sb.append(String.format("  • Tabulaciones         :  %d\n", logica.getTabulaciones()));
            sb.append("\n");

            sb.append(" 3. COMPOSICION DEL CONTENIDO\n");
            sb.append(" ────────────────────────────────────────────────────\n");
            sb.append(String.format("  • Letras Mayusculas    :  %d\n", logica.getMayusculas()));
            sb.append(String.format("  • Letras Minusculas    :  %d\n", logica.getMinusculas()));
            sb.append(String.format("  • Dígitos Numericos    :  %d\n", logica.getDigitos()));
            sb.append(String.format("  • Simbolos Especiales  :  %d\n", logica.getEspeciales()));
            sb.append("\n");

            sb.append(" 4. CURIOSIDADES Y DATOS EXTREMOS\n");
            sb.append(" ────────────────────────────────────────────────────\n");
            sb.append(String.format("  • Idioma Probable      :  %s\n", logica.getIdiomaDetectado()));
            sb.append(String.format("  • Caracter mas usado   :  '%s' (x%d veces)\n", logica.getCaracterMasUsado(), logica.getCantidadCaracterMasUsado()));
            sb.append(String.format("  • Palabra mas larga    :  \"%s\"\n", palabraLarga));
            sb.append(String.format("  • Longitud P. Larga    :  %d letras\n", longitudLarga)); 
            sb.append("\n");
            
            sb.append(" 5. METRICAS TECNICAS\n");
            sb.append(" ────────────────────────────────────────────────────\n");
            sb.append(String.format("  • Peso en Disco        :  %d Bytes\n", logica.getPesoBytes()));
            sb.append(String.format("  • Tiempo de Lectura    :  %s (estimado)\n", logica.getTiempoEscritura()));
            sb.append("\n");
            sb.append("══════════════════════════════════════════════════════\n");
            sb.append(" Fin del Reporte Generado \n");

            if(areaResultadosGeneral != null) {
                areaResultadosGeneral.setText(sb.toString());
                areaResultadosGeneral.setCaretPosition(0); 
            }

           
            StringBuilder sbFreq = new StringBuilder();
            sbFreq.append("\n   LETRA   │  FRECUENCIA  \n");
            sbFreq.append(" ──────────┼──────────────\n");
            logica.getFrecuenciaLetras().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    sbFreq.append(String.format("     %c     │      %d \n", e.getKey().toString().toUpperCase().charAt(0), e.getValue()));
                    sbFreq.append(" ──────────┼──────────────\n");
                });
            
            if(areaFrecuencia != null) areaFrecuencia.setText(sbFreq.toString());
            
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

    private JButton crearBoton(String txt, Color bg, Color fg) {
        JButton btn = new JButton(txt);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12)); 
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(Color.GRAY, 1),
                new EmptyBorder(8, 15, 8, 15)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private JSeparator crearSeparador() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 30));
        sep.setForeground(Color.GRAY);
        return sep;
    }

    private void configurarTextAreaResultados(JTextArea ta) {
        ta.setBackground(COLOR_FONDO_APP);
        ta.setForeground(Color.WHITE);
        ta.setEditable(false);
        ta.setFont(new Font("Consolas", Font.PLAIN, 15)); 
        ta.setMargin(new Insets(15, 15, 15, 15));
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new AnalizadorGUI().setVisible(true));
    }
}