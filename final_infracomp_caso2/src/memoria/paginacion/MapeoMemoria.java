package memoria.paginacion;

import java.util.ArrayList;
import java.util.List;

public class MapeoMemoria {
    private static class EntradaPagina {
        Integer marcoAsignado;  // Marco físico asignado (null si no está en memoria)
        boolean referenciada;   // Bit R (Referenciada)
        boolean modificada;     // Bit M (Modificada)
    }

    private final EntradaPagina[] entradas;
    private final int totalPaginas;
    private final int totalMarcos;

    public MapeoMemoria(int totalPaginas, int totalMarcos) {
        this.totalPaginas = totalPaginas;
        this.totalMarcos = totalMarcos;
        this.entradas = new EntradaPagina[totalPaginas];
        
        for (int i = 0; i < totalPaginas; i++) {
            entradas[i] = new EntradaPagina();
        }
    }

    // Métodos sincronizados para acceso thread-safe
    
    public synchronized boolean estaEnMemoria(int pagina) {
        validarPagina(pagina);
        return entradas[pagina].marcoAsignado != null;
    }

    public synchronized Integer getMarco(int pagina) {
        validarPagina(pagina);
        return entradas[pagina].marcoAsignado;
    }

    public synchronized void setMarco(int pagina, int marco) {
        validarPagina(pagina);
        validarMarco(marco);
        entradas[pagina].marcoAsignado = marco;
    }

    public synchronized boolean getReferenciada(int pagina) {
        validarPagina(pagina);
        return entradas[pagina].referenciada;
    }

    public synchronized void marcarReferenciada(int pagina, boolean valor) {
        validarPagina(pagina);
        entradas[pagina].referenciada = valor;
    }

    public synchronized boolean getModificada(int pagina) {
        validarPagina(pagina);
        return entradas[pagina].modificada;
    }

    public synchronized void marcarModificada(int pagina, boolean valor) {
        validarPagina(pagina);
        entradas[pagina].modificada = valor;
    }

    public synchronized void resetearBitsReferencia() {
        for (EntradaPagina entrada : entradas) {
            entrada.referenciada = false;
        }
    }

    public synchronized void desocuparPagina(int pagina) {
        validarPagina(pagina);
        entradas[pagina].marcoAsignado = null;
    }

    public synchronized List<Integer> getPaginasEnMemoria() {
        List<Integer> paginas = new ArrayList<>();
        for (int i = 0; i < entradas.length; i++) {
            if (entradas[i].marcoAsignado != null) {
                paginas.add(i);
            }
        }
        return paginas;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public int getTotalMarcos() {
        return totalMarcos;
    }

    // Métodos de validación
    
    private void validarPagina(int pagina) {
        if (pagina < 0 || pagina >= totalPaginas) {
            throw new IllegalArgumentException("Página inválida: " + pagina);
        }
    }

    private void validarMarco(int marco) {
        if (marco < 0 || marco >= totalMarcos) {
            throw new IllegalArgumentException("Marco inválido: " + marco);
        }
    }

    // Método para NRU (Not Recently Used)
    public synchronized List<Integer> getPaginasPorClase(int clase) {
        List<Integer> resultado = new ArrayList<>();
        for (int i = 0; i < entradas.length; i++) {
            if (entradas[i].marcoAsignado != null) {
                boolean r = entradas[i].referenciada;
                boolean m = entradas[i].modificada;
                
                int clasePagina = (r ? 2 : 0) + (m ? 1 : 0);
                if (clasePagina == clase) {
                    resultado.add(i);
                }
            }
        }
        return resultado;
    }

        // Método asignarMarco() faltante
        public synchronized void asignarMarco(int pagina, int marco) {
            validarPagina(pagina);
            validarMarco(marco);
            entradas[pagina].marcoAsignado = marco;
            entradas[pagina].referenciada = true;
            entradas[pagina].modificada = false;
        }
    
}