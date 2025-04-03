package memoria.nucleo;

import java.util.HashSet;

public class AlmacenamientoSecundario {
    private final HashSet<Integer> enSwap;
    private final HashSet<Integer> enDisco;

    public AlmacenamientoSecundario(int totalPaginas) {
        this.enSwap = new HashSet<>();
        this.enDisco = new HashSet<>();
        for (int i = 0; i < totalPaginas; i++) {
            enDisco.add(i); // Todas las páginas comienzan en disco
        }
    }

    public boolean estaEnSwap(int pagina) {
        return enSwap.contains(pagina);
    }

    public boolean estaEnDisco(int pagina) {
        return enDisco.contains(pagina);
    }

    public void moverASwap(int pagina) {
        enDisco.remove(pagina);
        enSwap.add(pagina);
    }
}