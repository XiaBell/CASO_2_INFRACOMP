public class PageStateUpdater extends Thread {
    private final PageManager pageManager;
    private volatile boolean running;
    private static final long UPDATE_INTERVAL = 1; // 1ms según requerimiento del enunciado

    public PageStateUpdater(PageManager pageManager) {
        this.pageManager = pageManager;
        this.running = true;
        // Mantener prioridad alta para asegurarnos que este thread se ejecute
        this.setPriority(Thread.MAX_PRIORITY);
    }

    @Override
    public void run() {
        try {
            // Esperar un poco antes de comenzar
            Thread.sleep(100);
            
            while (running) {
                pageManager.updatePageStates();
                Thread.sleep(UPDATE_INTERVAL);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stopUpdating() {
        running = false;
    }
} 