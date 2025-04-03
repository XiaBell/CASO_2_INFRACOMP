package threads;

import memory.GestorPaginas;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.atomic.AtomicInteger;

public class ReferenceProcessor extends Thread {
    private final GestorPaginas gestorPaginas;
    private final String archivoReferencias;
    private volatile boolean ejecutando;
    private static final int TAMANO_LOTE = 10000; // Procesar referencias en lotes
    private final AtomicInteger referenciasProcesadas = new AtomicInteger(0);

    public ReferenceProcessor(GestorPaginas gestorPaginas, String archivoReferencias) {
        this.gestorPaginas = gestorPaginas;
        this.archivoReferencias = archivoReferencias;
        this.ejecutando = true;
    }

    @Override
    public void run() {
        try (BufferedReader lector = new BufferedReader(new FileReader(archivoReferencias))) {
            System.out.println("Iniciando procesamiento de referencias...");
            String linea;

            while (ejecutando && (linea = lector.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                procesarReferencia(linea);

                if (referenciasProcesadas.incrementAndGet() % TAMANO_LOTE == 0) {
                    System.out.printf("Referencias procesadas: %d%n", referenciasProcesadas.get());
                    Thread.sleep(1); // Simular un retraso
                }
            }

            System.out.println("Procesamiento de referencias completado.");
        } catch (Exception e) {
            System.err.println("Error durante el procesamiento de referencias:");
            e.printStackTrace();
        }
    }

    private void procesarReferencia(String linea) {
        try {
            String[] partes = linea.split(",");
            if (partes.length >= 4) {
                int numeroPagina = Integer.parseInt(partes[1].trim());
                boolean esEscritura = partes[3].trim().equals("W");

                if (!gestorPaginas.accederPagina(numeroPagina, esEscritura)) {
                    System.out.printf("Fallo de página en la página %d.%n", numeroPagina);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al procesar la referencia: " + linea);
        }
    }

    public void detenerProcesamiento() {
        ejecutando = false;
    }
}
