package hilos;

import memoria.nucleo.SistemaMemoria;

public class ActualizadorEstado extends Thread {
    private final SistemaMemoria sistema;
    private volatile boolean ejecutando = true;

    public ActualizadorEstado(SistemaMemoria sistema) {
        this.sistema = sistema;
    }

    @Override
    public void run() {
        while (ejecutando) {
            try {
                Thread.sleep(1); // Ciclo de 1ms
                sistema.getMapeador().resetearBitsReferencia();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void detener() {
        ejecutando = false;
    }
}