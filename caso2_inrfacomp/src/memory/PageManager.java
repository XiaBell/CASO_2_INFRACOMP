package memory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class PageManager {
    private final List<Page> tablaPaginas;
    private final int numMarcos;
    private final ReentrantLock bloqueo;
    private int hits;
    private int fallos;

    public PageManager(int numPaginas, int numMarcos) {
        this.tablaPaginas = new ArrayList<>();
        for (int i = 0; i < numPaginas; i++) {
            tablaPaginas.add(new Page(i));
        }
        this.numMarcos = numMarcos;
        this.bloqueo = new ReentrantLock();
        this.hits = 0;
        this.fallos = 0;
    }

    public boolean accessPage(int numeroPagina, boolean esEscritura) {
        bloqueo.lock();
        try {
            Page pagina = tablaPaginas.get(numeroPagina);
            if (pagina.estaEnMemoria()) {
                pagina.marcarReferenciada(true);
                if (esEscritura) pagina.marcarModificada(true);
                hits++;
                return true;
            } else {
                fallos++;
                return false;
            }
        } finally {
            bloqueo.unlock();
        }
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

    public void printPageTableStatus() {
        System.out.println("\n=== Estado de la Tabla de Páginas ===");
        for (Page pagina : tablaPaginas) {
            System.out.println(pagina);
        }
    }
}
