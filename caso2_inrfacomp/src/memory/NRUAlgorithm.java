package memory;

import java.util.ArrayList;
import java.util.List;

public class NRUAlgorithm {
    private final GestorPaginas gestorPaginas;

    public NRUAlgorithm(GestorPaginas gestorPaginas) {
        this.gestorPaginas = gestorPaginas;
    }

    public Page seleccionarVictima() {
        List<Page>[] clases = new List[4];
        for (int i = 0; i < 4; i++) {
            clases[i] = new ArrayList<>();
        }

        for (Page pagina : gestorPaginas.getPages()) {
            if (!pagina.estaEnMemoria()) continue;

            int indiceClase = 0;
            if (pagina.estaReferenciada()) indiceClase |= 2;
            if (pagina.estaModificada()) indiceClase |= 1;
            clases[indiceClase].add(pagina);
        }

        for (int i = 0; i < 4; i++) {
            if (!clases[i].isEmpty()) {
                return clases[i].get(0);
            }
        }

        return null;
    }
}
