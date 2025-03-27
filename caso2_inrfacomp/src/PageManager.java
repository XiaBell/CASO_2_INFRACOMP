import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class PageManager {
    private final List<Page> pageTable;
    private final int numFrames;
    private final ReentrantLock lock;
    private int hits;
    private int misses;
    private long totalTime;
    private static final long RAM_ACCESS_TIME = 50; // nanoseconds
    private static final long SWAP_ACCESS_TIME = 10_000_000; // nanoseconds (10ms)

    public PageManager(int numPages, int numFrames) {
        this.pageTable = new ArrayList<>();
        for (int i = 0; i < numPages; i++) {
            pageTable.add(new Page(i));
        }
        this.numFrames = numFrames;
        this.lock = new ReentrantLock();
        this.hits = 0;
        this.misses = 0;
        this.totalTime = 0;
    }

    public synchronized void updatePageStates() {
        lock.lock();
        try {
            for (Page page : pageTable) {
                if (page.isReferenced()) {
                    page.setReferenced(false);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public synchronized boolean accessPage(int pageNumber) {
        lock.lock();
        try {
            Page page = pageTable.get(pageNumber);
            page.setReferenced(true);
            
            if (page.isInMemory()) {
                hits++;
                totalTime += RAM_ACCESS_TIME;
                return true;
            }

            misses++;
            totalTime += SWAP_ACCESS_TIME;

            if (getNumPagesInMemory() < numFrames) {
                // There's a free frame
                page.setFrameNumber(getNumPagesInMemory());
                return true;
            }

            // Need to replace a page
            Page victim = selectVictimPage();
            victim.setFrameNumber(-1);
            page.setFrameNumber(victim.getFrameNumber());
            return true;
        } finally {
            lock.unlock();
        }
    }

    private Page selectVictimPage() {
        // NRU algorithm implementation
        // Class 0: not referenced, not modified
        // Class 1: not referenced, modified
        // Class 2: referenced, not modified
        // Class 3: referenced, modified
        List<Page> class0 = new ArrayList<>();
        List<Page> class1 = new ArrayList<>();
        List<Page> class2 = new ArrayList<>();
        List<Page> class3 = new ArrayList<>();

        for (Page page : pageTable) {
            if (!page.isInMemory()) continue;
            
            if (!page.isReferenced() && !page.isModified()) {
                class0.add(page);
            } else if (!page.isReferenced() && page.isModified()) {
                class1.add(page);
            } else if (page.isReferenced() && !page.isModified()) {
                class2.add(page);
            } else {
                class3.add(page);
            }
        }

        // Select the first page from the lowest non-empty class
        if (!class0.isEmpty()) return class0.get(0);
        if (!class1.isEmpty()) return class1.get(0);
        if (!class2.isEmpty()) return class2.get(0);
        return class3.get(0);
    }

    private int getNumPagesInMemory() {
        return (int) pageTable.stream().filter(Page::isInMemory).count();
    }

    public int getHits() {
        return hits;
    }

    public int getMisses() {
        return misses;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public double getHitPercentage() {
        int total = hits + misses;
        return total == 0 ? 0 : (double) hits / total * 100;
    }
} 