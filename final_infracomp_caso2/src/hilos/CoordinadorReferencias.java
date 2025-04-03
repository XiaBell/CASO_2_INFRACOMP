package hilos;

import memoria.nucleo.SistemaMemoria;
import java.util.ArrayList;

public class CoordinadorReferencias extends Thread {
    private final SistemaMemoria sistema;
    private final ArrayList<String[]> referencias;
    public static volatile boolean activo = true;
    private int hits = 0;
    private int fallas = 0;

    public CoordinadorReferencias(SistemaMemoria sistema, ArrayList<String[]> referencias) {
        this.sistema = sistema;
        this.referencias = referencias;
    }

    @Override
    public void run() {
        for (String[] ref : referencias) {
            if (ref.length < 4) continue; // Saltar líneas mal formateadas

            int pagina = Integer.parseInt(ref[1]);
            boolean esEscritura = ref[3].equals("W");
            
            sistema.manejarAccesoMemoria(pagina, esEscritura);

            if (getHits() % 10000 == 0) {
                pausar(1);
            }
        }
        activo = false;
        mostrarEstadisticas();
    }

    private void pausar(int ms) {
        try { 
            Thread.sleep(ms); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void mostrarEstadisticas() {
        System.out.println("\n=== Estadísticas del Coordinador ===");
        System.out.println("Total referencias procesadas: " + (hits + fallas));
        System.out.println("Hits: " + hits);
        System.out.println("Fallas: " + fallas);
    }

    // Métodos sincronizados para acceso desde SistemaMemoria
    public synchronized void incrementarHits() { hits++; }
    public synchronized void incrementarFallas() { fallas++; }
    public synchronized int getHits() { return hits; }
    public synchronized int getFallas() { return fallas; }
}