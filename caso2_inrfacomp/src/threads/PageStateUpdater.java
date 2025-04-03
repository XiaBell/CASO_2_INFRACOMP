package threads;

import memory.GestorPaginas;

public class PageStateUpdater extends Thread {
    private final GestorPaginas gestorPaginas;
    private volatile boolean ejecutando;

    public PageStateUpdater(GestorPaginas gestorPaginas) {
        this.gestorPaginas = gestorPaginas;
        this.ejecutando = true;
    }

    @Override
    public void run() {
        while (ejecutando) {
            try {
                Thread.sleep(20); // Actualizar cada 20ms
                reiniciarBitsReferenciados();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void reiniciarBitsReferenciados() {
        gestorPaginas.getPages().forEach(pagina -> pagina.marcarReferenciada(false));
        System.out.println("Bits de referencia reiniciados.");
    }

    public void detenerActualizacion() {
        ejecutando = false;
    }
}
