package memoria.util;

import memoria.procesamiento.Imagen;
import java.io.*;
import java.util.ArrayList;

public class GeneradorReferencias {
    private final int tamPagina;
    private final Imagen imagen;
    private final ArrayList<String> referencias;
    private int numPaginas;
    private int numFilas;
    private int numColumnas;
    
    private int[] inicioImagen;
    private int[] inicioFiltroX;
    private int[] inicioFiltroY;
    private int[] inicioResultado;

    public GeneradorReferencias(int tamPagina, String rutaImagen) {
        this.tamPagina = tamPagina;
        this.imagen = new Imagen(rutaImagen);
        this.referencias = new ArrayList<>();
        this.numFilas = imagen.alto;
        this.numColumnas = imagen.ancho;
    }

    public void generarTraza() {
        calcularDistribucionMemoria();
        simularAccesosSobel();
    }

    private void calcularDistribucionMemoria() {
        int bytesImagen = numFilas * numColumnas * 3;
        int bytesFiltro = 36; // 3x3 enteros (4 bytes cada uno)
        int bytesTotal = bytesImagen * 2 + bytesFiltro * 2;

        numPaginas = (int) Math.ceil((double) bytesTotal / tamPagina);

        int offset = 0;
        inicioImagen = new int[]{0, 0};
        offset += bytesImagen;
        
        inicioFiltroX = new int[]{offset / tamPagina, offset % tamPagina};
        offset += bytesFiltro;
        
        inicioFiltroY = new int[]{offset / tamPagina, offset % tamPagina};
        offset += bytesFiltro;
        
        inicioResultado = new int[]{offset / tamPagina, offset % tamPagina};
    }

    private void simularAccesosSobel() {
        for (int i = 1; i < numFilas - 1; i++) {
            for (int j = 1; j < numColumnas - 1; j++) {
                procesarPixel(i, j);
            }
        }
    }

    private void procesarPixel(int i, int j) {
        for (int ki = -1; ki <= 1; ki++) {
            for (int kj = -1; kj <= 1; kj++) {
                generarAccesosPixel(i, j, ki, kj);
            }
        }
        generarAccesoResultado(i, j);
    }

    private void generarAccesosPixel(int i, int j, int ki, int kj) {
        // Accesos a imagen original (3 canales)
        generarAccesoImagen(i + ki, j + kj);
        
        // Accesos a filtros (3 accesos por canal)
        for (int c = 0; c < 3; c++) {
            generarAccesoFiltro(ki + 1, kj + 1, 'X');
            generarAccesoFiltro(ki + 1, kj + 1, 'Y');
        }
    }

    private void generarAccesoImagen(int fila, int col) {
        int offset = 3 * (fila * numColumnas + col);
        agregarReferencia("IMG", fila, col, offset, inicioImagen, 'R');
    }

    private void generarAccesoFiltro(int fila, int col, char tipoFiltro) {
        int offset = 4 * (fila * 3 + col);
        int[] inicio = (tipoFiltro == 'X') ? inicioFiltroX : inicioFiltroY;
        agregarReferencia("FIL_" + tipoFiltro, fila, col, offset, inicio, 'R');
    }

    private void generarAccesoResultado(int fila, int col) {
        int offset = 3 * (fila * numColumnas + col);
        agregarReferencia("RES", fila, col, offset, inicioResultado, 'W');
    }

    private void agregarReferencia(String tipo, int fila, int col, int offset, int[] inicio, char operacion) {
        int pagina = inicio[0] + (inicio[1] + offset) / tamPagina;
        int desplazamiento = (inicio[1] + offset) % tamPagina;
        referencias.add(String.format("%s[%d][%d],%d,%d,%c", 
            tipo, fila, col, pagina, desplazamiento, operacion));
    }

    public void guardarArchivo(String nombreArchivo) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(nombreArchivo))) {
            writer.println("TP=" + tamPagina);
            writer.println("NF=" + numFilas);
            writer.println("NC=" + numColumnas);
            writer.println("NR=" + referencias.size());
            writer.println("NP=" + numPaginas);
            
            for (String ref : referencias) {
                writer.println(ref);
            }
        }
    }

    public int getNumReferencias() {
        return referencias.size();
    }
}