package memoria.nucleo;

public class MemoriaPrimaria {
    private final boolean[] marcos;

    public MemoriaPrimaria(int capacidad) {
        this.marcos = new boolean[capacidad]; // false = libre, true = ocupado
    }

    public synchronized Integer reservarBloque() {
        for (int i = 0; i < marcos.length; i++) {
            if (!marcos[i]) {
                marcos[i] = true;
                return i;
            }
        }
        return null; // No hay marcos libres
    }

    public synchronized void liberarBloque(int marco) {
        if (marco >= 0 && marco < marcos.length) {
            marcos[marco] = false;
        }
    }
}