package memoria.paginacion;

import java.util.List;

public class PoliticaNRU {
    private final MapeoMemoria mapeador;
    
    public PoliticaNRU(MapeoMemoria mapeador) {
        this.mapeador = mapeador;
    }

    /**
     * Selecciona una página víctima según el algoritmo NRU
     * @return El número de marco que fue liberado
     */
    public int seleccionarVictima() {
        // Buscar en las 4 clases en orden de prioridad
        for (int clase = 0; clase < 4; clase++) {
            List<Integer> paginasClase = mapeador.getPaginasPorClase(clase);
            
            if (!paginasClase.isEmpty()) {
                // Seleccionar la primera página de la clase encontrada
                int paginaVictima = paginasClase.get(0);
                liberarPagina(paginaVictima);
                return mapeador.getMarco(paginaVictima);
            }
        }
        
        throw new IllegalStateException("No se encontraron páginas candidatas para reemplazo");
    }

    /**
     * Libera una página de memoria, manejando bits M/R
     * @param pagina Número de página a liberar
     */
    private void liberarPagina(int pagina) {
        // Si la página fue modificada, debería escribirse a swap (simulado)
        if (mapeador.getModificada(pagina)) {
            // Simular escritura a swap - en implementación real se escribiría a almacenamiento secundario
            System.out.println("[NRU] Página " + pagina + " modificada, escribiendo a swap");
        }
        
        // Limpiar bits y desasignar marco
        mapeador.marcarReferenciada(pagina, false);
        mapeador.marcarModificada(pagina, false);
        mapeador.desocuparPagina(pagina);
    }

    /**
     * Versión alternativa que recibe la página solicitante para optimización
     * @param paginaSolicitante Página que está causando el fallo
     * @return Marco liberado
     */
    public int seleccionarVictima(int paginaSolicitante) {
        // Prioridad 1: Páginas no referenciadas ni modificadas (clase 0)
        List<Integer> clase0 = mapeador.getPaginasPorClase(0);
        if (!clase0.isEmpty()) {
            return liberarYRetornar(clase0.get(0));
        }

        // Prioridad 2: Páginas no referenciadas pero modificadas (clase 1)
        List<Integer> clase1 = mapeador.getPaginasPorClase(1);
        if (!clase1.isEmpty()) {
            return liberarYRetornar(clase1.get(0));
        }

        // Prioridad 3: Páginas referenciadas pero no modificadas (clase 2)
        List<Integer> clase2 = mapeador.getPaginasPorClase(2);
        if (!clase2.isEmpty()) {
            return liberarYRetornar(clase2.get(0));
        }

        // Prioridad 4: Páginas referenciadas y modificadas (clase 3)
        List<Integer> clase3 = mapeador.getPaginasPorClase(3);
        if (!clase3.isEmpty()) {
            return liberarYRetornar(clase3.get(0));
        }

        throw new IllegalStateException("No hay páginas disponibles para reemplazo");
    }

    private int liberarYRetornar(int pagina) {
        int marco = mapeador.getMarco(pagina);
        liberarPagina(pagina);
        return marco;
    }
}