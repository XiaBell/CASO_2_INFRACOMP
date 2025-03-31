public class Page {
    private int pageNumber;
    private boolean referenced;
    private boolean modified;
    private long lastAccessTime;
    private int frameNumber; // -1 if not in memory
    private long loadTime; // Cuando la página se cargó en memoria

    public Page(int pageNumber) {
        this.pageNumber = pageNumber;
        this.referenced = false;
        this.modified = false;
        this.lastAccessTime = System.currentTimeMillis();
        this.frameNumber = -1;
        this.loadTime = 0;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public boolean isReferenced() {
        return referenced;
    }

    public void setReferenced(boolean referenced) {
        this.referenced = referenced;
        this.lastAccessTime = System.currentTimeMillis();
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public void setFrameNumber(int frameNumber) {
        this.frameNumber = frameNumber;
        if (frameNumber != -1) {
            // Si se carga en memoria, registrar el tiempo
            this.loadTime = System.currentTimeMillis();
        }
    }

    public boolean isInMemory() {
        return frameNumber != -1;
    }
    
    public long getLoadTime() {
        return loadTime;
    }
    
    @Override
    public String toString() {
        return "Page [" + pageNumber + 
               ", frame=" + (frameNumber == -1 ? "SWAP" : frameNumber) + 
               ", R=" + (referenced ? "1" : "0") + 
               ", M=" + (modified ? "1" : "0") + "]";
    }
} 