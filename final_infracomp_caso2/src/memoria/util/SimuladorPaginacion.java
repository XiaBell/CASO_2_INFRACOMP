package memoria.util;

import memoria.nucleo.SistemaMemoria;
import memoria.paginacion.PoliticaNRU;
import java.io.*;
import java.util.ArrayList;

public class SimuladorPaginacion {
    private final int numMarcos;
    private final String archivoReferencias;
    private int hits;
    private int fallas;

    public SimuladorPaginacion(int numMarcos, String archivoReferencias) {
        this.numMarcos = numMarcos;
        this.archivoReferencias = archivoReferencias;
    }

    public void ejecutarSimulacion() {
        ArrayList<String[]> referencias = cargarReferencias();
        if (referencias == null) return;

        int tamPagina = Integer.parseInt(referencias.get(0)[1]);
        int numPaginas = Integer.parseInt(referencias.get(4)[1]);

        SistemaMemoria sistema = new SistemaMemoria(numMarcos, numPaginas, tamPagina, referencias);
        sistema.iniciar();

        // Obtener resultados
        this.hits = sistema.getHits();
        this.fallas = sistema.getFallas();
    }

    private ArrayList<String[]> cargarReferencias() {
        ArrayList<String[]> referencias = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(archivoReferencias))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.startsWith("TP=") || linea.startsWith("NF=") || 
                    linea.startsWith("NC=") || linea.startsWith("NR=") || 
                    linea.startsWith("NP=")) {
                    referencias.add(linea.split("="));
                } else if (!linea.trim().isEmpty()) {
                    referencias.add(linea.split(","));
                }
            }
            return referencias;
        } catch (IOException e) {
            System.err.println("Error al leer archivo: " + e.getMessage());
            return null;
        }
    }

    public void mostrarResultados() {
        System.out.println("\n=== Resultados de Simulación ===");
        System.out.println("Marcos de página: " + numMarcos);
        System.out.println("Total referencias: " + (hits + fallas));
        System.out.println("Hits: " + hits + " (" + String.format("%.2f", getPorcentajeHits()) + "%)");
        System.out.println("Fallas: " + fallas + " (" + String.format("%.2f", getPorcentajeFallas()) + "%)");
    }


    private double getPorcentajeFallas() {
        return (fallas * 100.0) / (hits + fallas);
    }

    public int getHits() { return hits; }
    public int getFallas() { return fallas; }

    private double getPorcentajeHits() {
        return (hits * 100.0) / (hits + fallas);
    }
    
}