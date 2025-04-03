import memory.PageManager;

public class Testpad {
    public static void main(String[] args) {
        PageManager gestorPaginas = new PageManager(10, 3);

        gestorPaginas.accessPage(0, false);
        gestorPaginas.accessPage(1, false);
        gestorPaginas.accessPage(0, true);
        gestorPaginas.accessPage(2, false);
        gestorPaginas.accessPage(1, true);

        System.out.println("Hits: " + gestorPaginas.getHits());
        System.out.println("Fallos: " + gestorPaginas.getFallos());
    }
}