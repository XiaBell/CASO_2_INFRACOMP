package memoria.nucleo;

import memoria.paginacion.*;
import hilos.*;
import java.util.ArrayList;

public class SistemaMemoria {
    private final MapeoMemoria mapeador;
    private final MemoriaPrimaria memoria;
    private final AlmacenamientoSecundario almacenamiento;
    private final PoliticaNRU politica;
    private final CoordinadorReferencias coordinador;
    private final ActualizadorEstado actualizador;

    public SistemaMemoria(int numMarcos, int numPaginas, int tamPagina, ArrayList<String[]> referencias) {
        this.memoria = new MemoriaPrimaria(numMarcos);
        this.almacenamiento = new AlmacenamientoSecundario(numPaginas);
        this.mapeador = new MapeoMemoria(numPaginas, numMarcos);
        this.politica = new PoliticaNRU(mapeador);
        this.coordinador = new CoordinadorReferencias(this, referencias);
        this.actualizador = new ActualizadorEstado(this);
    }

    public void iniciar() {
        actualizador.start();
        coordinador.start();
        
        try {
            coordinador.join();
            actualizador.detener();
            actualizador.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void manejarAccesoMemoria(int pagina, boolean esEscritura) {
        if (mapeador.estaEnMemoria(pagina)) {
            coordinador.incrementarHits();
            actualizarBits(pagina, esEscritura);
        } else {
            coordinador.incrementarFallas();
            cargarPagina(pagina, esEscritura);
        }
    }

    private void cargarPagina(int pagina, boolean esEscritura) {
        Integer marco = memoria.reservarBloque();
        
        if (marco == null) {
            marco = politica.seleccionarVictima(pagina);
        }
        
        if (almacenamiento.estaEnSwap(pagina)) {
            System.out.println("[Memoria] Cargando página " + pagina + " desde SWAP");
        } else {
            System.out.println("[Memoria] Cargando página " + pagina + " desde disco");
        }
        
        mapeador.asignarMarco(pagina, marco);
        actualizarBits(pagina, esEscritura);
    }

    private void actualizarBits(int pagina, boolean esEscritura) {
        mapeador.marcarReferenciada(pagina, true);
        if (esEscritura) {
            mapeador.marcarModificada(pagina, true);
        }
    }

    // Métodos de acceso para pruebas
    public int getHits() { return coordinador.getHits(); }
    public int getFallas() { return coordinador.getFallas(); }
    public double getPorcentajeHits() {
        return (double) getHits() / (getHits() + getFallas()) * 100;
    }

    public MapeoMemoria getMapeador() {
        return this.mapeador;
    }


}