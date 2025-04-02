import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class PageManager {
    private final List<Page> pageTable;
    private final int numFrames;
    private final ReentrantLock lock;
    private final AtomicInteger hits;
    private final AtomicInteger misses;
    private final AtomicLong totalTime;
    private static final long RAM_ACCESS_TIME = 50; // nanoseconds
    private static final long SWAP_ACCESS_TIME = 10_000_000; // nanoseconds (10ms)
    private final Random random = new Random();
    private static final long CLOCK_INTERVAL = 20; // 20 milliseconds for clock interrupt

    public PageManager(int numPages, int numFrames) {
        this.pageTable = new ArrayList<>();
        for (int i = 0; i < numPages; i++) {
            pageTable.add(new Page(i));
        }
        this.numFrames = numFrames;
        this.lock = new ReentrantLock();
        this.hits = new AtomicInteger(0);
        this.misses = new AtomicInteger(0);
        this.totalTime = new AtomicLong(0);
    }

    public boolean accessPage(int pageNumber, boolean isWrite) {
    
        if (pageNumber < 0 || pageNumber >= pageTable.size()) {
            return false;
        }

        lock.lock();
        try {
            Page page = pageTable.get(pageNumber);
            
            if (page.isInMemory()) {
                // Al acceder a una página:
                // - Siempre se marca como referenciada (R=1)
                // - Si es escritura, se marca como modificada (M=1)
                page.setReferenced(true);  // Bit R se establece en cada referencia
                if (isWrite) {
                    page.setModified(true);  // Bit M se establece solo en escrituras
                }
                hits.incrementAndGet();
                totalTime.addAndGet(RAM_ACCESS_TIME);
                return true;
            }

            misses.incrementAndGet();
            totalTime.addAndGet(SWAP_ACCESS_TIME);
            
            // Log detallado del fallo de página
            if (misses.get() % 100 == 0) {
                System.out.println("\nFallo de página #" + misses + ":");
                System.out.println("- Página solicitada: " + pageNumber);
                System.out.println("- Operación: " + (isWrite ? "Escritura" : "Lectura"));
                System.out.println("- Marcos ocupados: " + getNumPagesInMemory() + "/" + numFrames);
                
                // Mostrar estado de las páginas en memoria
                System.out.println("- Páginas en memoria:");
                for (Page p : pageTable) {
                    if (p.isInMemory()) {
                        System.out.println("  * Marco " + p.getFrameNumber() + 
                                         ": Página " + p.getPageNumber() + 
                                         " (R=" + (p.isReferenced() ? "1" : "0") + 
                                         ", M=" + (p.isModified() ? "1" : "0") + ")");
                    }
                }
            }

            if (getNumPagesInMemory() < numFrames) {
                int frameNumber = getNextFreeFrame();
                page.setFrameNumber(frameNumber);
                page.setReferenced(true);  // Nueva página siempre tiene R=1
                if (isWrite) {
                    page.setModified(true);  // M=1 solo si es escritura
                }
                System.out.println("Caso extraordinario? (1)");
                hits.incrementAndGet();
                return true;
            }

            Page victim = selectVictimNRU();
            if (victim != null) {
                if (misses.get() % 100 == 0) {
                    System.out.println("- Víctima seleccionada: Página " + victim.getPageNumber() + 
                                     " del marco " + victim.getFrameNumber() +
                                     " (Clase " + (victim.isReferenced() ? "2" : "0") + 
                                     (victim.isModified() ? "+1" : "") + ")");
                }
                
                int victimFrame = victim.getFrameNumber();
                victim.setFrameNumber(-1);  // La víctima va a swap
                page.setFrameNumber(victimFrame);
                page.setReferenced(true);   // Nueva página siempre tiene R=1
                if (isWrite) {
                    page.setModified(true); // M=1 solo si es escritura
                }
                System.out.println("Caso extraordinario? (2)");
                hits.incrementAndGet();
                return true;
            }

            return false;
        } finally {
            lock.unlock();
        }
    }

    private Page selectVictimNRU() {
        List<Page>[] classes = new List<Page>[4];
        for (int i = 0; i < 4; i++) {
            classes[i] = new ArrayList<>();
        }

        // Clasificar páginas en las 4 clases NRU según Tanenbaum:
        // Clase 0: no referenciada (R=0), no modificada (M=0)
        // Clase 1: no referenciada (R=0), modificada (M=1)
        // Clase 2: referenciada (R=1), no modificada (M=0)
        // Clase 3: referenciada (R=1), modificada (M=1)
        for (Page page : pageTable) {
            if (!page.isInMemory()) continue;
            
            int classNum = 0;
            if (page.isModified()) classNum |= 1;  // Bit M
            if (page.isReferenced()) classNum |= 2; // Bit R
            classes[classNum].add(page);
        }

        // Buscar la primera clase no vacía y seleccionar una página al azar de esa clase
        for (int i = 0; i < 4; i++) {
            if (!classes[i].isEmpty()) {
                // Seleccionar una página al azar de esta clase, como especifica Tanenbaum
                int randomIndex = random.nextInt(classes[i].size());
                return classes[i].get(randomIndex);
            }
        }

        // Si no se encontró ninguna página (no debería ocurrir)
        return pageTable.stream()
            .filter(Page::isInMemory)
            .findFirst()
            .orElse(null);
    }

    private int getNextFreeFrame() {
        boolean[] usedFrames = new boolean[numFrames];
        for (Page page : pageTable) {
            if (page.isInMemory()) {
                usedFrames[page.getFrameNumber()] = true;
            }
        }
        for (int i = 0; i < numFrames; i++) {
            if (!usedFrames[i]) return i;
        }
        return -1;
    }

    private int getNumPagesInMemory() {
        int count = 0;
        for (Page page : pageTable) {
            if (page.isInMemory()) count++;
        }
        return count;
    }

    public void printPageTableStatus() {
        lock.lock();
        try {
            System.out.println("\n=== ESTADO DE LA TABLA DE PÁGINAS ===");
            System.out.println("Hits: " + hits + ", Misses: " + misses);
            System.out.println("Marcos utilizados: " + getNumPagesInMemory() + "/" + numFrames);
            
            System.out.println("\nPáginas en memoria:");
            for (Page page : pageTable) {
                if (page.isInMemory()) {
                    System.out.println("Marco " + page.getFrameNumber() + ": " + page);
                }
            }
            
            int[] classCounts = new int[4];
            for (Page page : pageTable) {
                if (page.isInMemory()) {
                    int classNum = 0;
                    if (page.isModified()) classNum |= 1;
                    if (page.isReferenced()) classNum |= 2;
                    classCounts[classNum]++;
                }
            }
            
            System.out.println("\nEstadísticas por clase NRU:");
            System.out.println("Clase 0 (R=0, M=0): " + classCounts[0]);
            System.out.println("Clase 1 (R=0, M=1): " + classCounts[1]);
            System.out.println("Clase 2 (R=1, M=0): " + classCounts[2]);
            System.out.println("Clase 3 (R=1, M=1): " + classCounts[3]);
        } finally {
            lock.unlock();
        }
    }

    public String getMemoryStatus() {
        return "Marcos de memoria utilizados: " + getNumPagesInMemory() + "/" + numFrames;
    }

    public int getHits() { return hits.get(); }
    public int getMisses() { return misses.get(); }
    public long getTotalTime() { return totalTime.get(); }
    public double getHitPercentage() {
        return hits.get() * 100.0 / (hits.get() + misses.get());
    }
} 