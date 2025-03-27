public class PageStateUpdater extends Thread {
    private final PageManager pageManager;
    private volatile boolean running;

    public PageStateUpdater(PageManager pageManager) {
        this.pageManager = pageManager;
        this.running = true;
    }

    @Override
    public void run() {
        while (running) {
            try {
                pageManager.updatePageStates();
                Thread.sleep(1); // Update every 1ms
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void stopUpdating() {
        running = false;
    }
} 