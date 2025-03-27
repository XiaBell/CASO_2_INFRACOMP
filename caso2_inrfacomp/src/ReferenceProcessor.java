import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReferenceProcessor extends Thread {
    private final PageManager pageManager;
    private final String referenceFile;
    private volatile boolean running;
    private static final int REFERENCES_PER_MS = 10000;

    public ReferenceProcessor(PageManager pageManager, String referenceFile) {
        this.pageManager = pageManager;
        this.referenceFile = referenceFile;
        this.running = true;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new FileReader(referenceFile))) {
            // Skip header information
            String line = reader.readLine();
            while (line != null && line.startsWith("TP=")) {
                line = reader.readLine();
            }

            // Process references
            int count = 0;
            while (running && (line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(",");
                int pageNumber = Integer.parseInt(parts[1].trim());
                pageManager.accessPage(pageNumber);
                
                count++;
                if (count >= REFERENCES_PER_MS) {
                    Thread.sleep(1); // Wait 1ms after processing 10000 references
                    count = 0;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopProcessing() {
        running = false;
    }
} 