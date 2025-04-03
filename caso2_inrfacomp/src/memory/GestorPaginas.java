package memory;

import java.util.ArrayList;
import java.util.List;
import threads.PageStateUpdater;

public class GestorPaginas {
    private final List<Page> tablaPaginas;
    private final int numMarcos;
    private final PageStateUpdater pageStateUpdater;
    private final NRUAlgorithm nruAlgorithm;
    private int hits;
    private int fallos;

    public GestorPaginas(int numPaginas, int numMarcos) {
        this.tablaPaginas = new ArrayList<>();
        for (int i = 0; i < numPaginas; i++) {
            tablaPaginas.add(new Page(i));
        }
        this.numMarcos = numMarcos;
        this.pageStateUpdater = new PageStateUpdater(this); // Pasar `this` correctamente
        this.nruAlgorithm = new NRUAlgorithm(this);
        this.hits = 0;
        this.fallos = 0;
    }

    public boolean accederPagina(int numeroPagina, boolean esEscritura) {
        if (numeroPagina < 0 || numeroPagina >= tablaPaginas.size()) {
            System.err.println("Error: Número de página fuera de rango.");
            return false;
        }

        Page pagina = tablaPaginas.get(numeroPagina);
        if (pagina.estaEnMemoria()) {
            pagina.marcarReferenciada(true);
            if (esEscritura) pagina.marcarModificada(true);
            hits++;
            return true;
        } else {
            fallos++;
            Page victima = nruAlgorithm.seleccionarVictima();
            if (victima != null) {
                liberarMarco(victima);
                asignarMarco(pagina, esEscritura);
                return true;
            }
            return false;
        }
    }

    private void liberarMarco(Page victima) {
        victima.asignarMarco(-1); // Liberar el marco de la víctima
        victima.marcarReferenciada(false);
        victima.marcarModificada(false);
    }

    private void asignarMarco(Page pagina, boolean esEscritura) {
        int marcoLibre = buscarMarcoLibre();
        if (marcoLibre != -1) {
            pagina.asignarMarco(marcoLibre);
            pagina.marcarReferenciada(true);
            if (esEscritura) pagina.marcarModificada(true);
        } else {
            System.err.println("Error: No se encontró un marco libre.");
        }
    }

    private int buscarMarcoLibre() {
        boolean[] marcosOcupados = new boolean[numMarcos];
        for (Page pagina : tablaPaginas) {
            if (pagina.estaEnMemoria()) {
                marcosOcupados[pagina.getNumeroMarco()] = true;
            }
        }
        for (int i = 0; i < marcosOcupados.length; i++) {
            if (!marcosOcupados[i]) return i;
        }
        return -1; // No hay marcos libres
    }

    public List<Page> getPages() {
        return tablaPaginas;
    }

    public int getNumMarcos() {
        return numMarcos;
    }

    public int getHits() {
        return hits;
    }

    public int getFallos() {
        return fallos;
    }

    public void iniciarActualizacionEstados() {
        pageStateUpdater.start();
    }

    public void detenerActualizacionEstados() {
        pageStateUpdater.detenerActualizacion();
    }

    public void imprimirEstadoTablaPaginas() {
        System.out.println("\n=== Estado de la Tabla de Páginas ===");
        for (Page pagina : tablaPaginas) {
            System.out.println(pagina);
        }
    }
}
