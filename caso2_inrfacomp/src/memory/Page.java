package memory;
public class Page {
    private final int numeroPagina;
    private boolean referenciada;
    private boolean modificada;
    private long tiempoUltimoAcceso;
    private int numeroMarco; // -1 si no está en memoria
    private long tiempoCarga; // Tiempo en el que la página fue cargada en memoria

    public Page(int numeroPagina) {
        this.numeroPagina = numeroPagina;
        this.referenciada = false;
        this.modificada = false;
        this.tiempoUltimoAcceso = System.currentTimeMillis();
        this.numeroMarco = -1;
        this.tiempoCarga = 0;
    }

    public int getNumeroPagina() {
        return numeroPagina;
    }

    public boolean estaReferenciada() {
        return referenciada;
    }

    public void marcarReferenciada(boolean referenciada) {
        this.referenciada = referenciada;
        this.tiempoUltimoAcceso = System.currentTimeMillis();
    }

    public boolean estaModificada() {
        return modificada;
    }

    public void marcarModificada(boolean modificada) {
        this.modificada = modificada;
    }

    public long getTiempoUltimoAcceso() {
        return tiempoUltimoAcceso;
    }

    public int getNumeroMarco() {
        return numeroMarco;
    }

    public void asignarMarco(int numeroMarco) {
        this.numeroMarco = numeroMarco;
        if (numeroMarco != -1) {
            this.tiempoCarga = System.currentTimeMillis();
        }
    }

    public boolean estaEnMemoria() {
        return numeroMarco != -1;
    }

    public long getTiempoCarga() {
        return tiempoCarga;
    }

    @Override
    public String toString() {
        return "Página [" + numeroPagina +
               ", marco=" + (numeroMarco == -1 ? "SWAP" : numeroMarco) +
               ", R=" + (referenciada ? "1" : "0") +
               ", M=" + (modificada ? "1" : "0") + "]";
    }
}