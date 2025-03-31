import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReferenceProcessor extends Thread {
    private final PageManager pageManager;
    private final String referenceFile;
    private volatile boolean running;
    private static final int REFERENCES_PER_BATCH = 10000;

    public ReferenceProcessor(PageManager pageManager, String referenceFile) {
        this.pageManager = pageManager;
        this.referenceFile = referenceFile;
        this.running = true;
    }

    @Override
    public void run() {
        try {
            System.out.println("Iniciando procesamiento de referencias...");
            BufferedReader reader = new BufferedReader(new FileReader(referenceFile));
            
            // Leer información del encabezado
            String line;
            int pageSize = 0, numRows = 0, numCols = 0, numReferences = 0, numPages = 0;
            
            // Leer encabezado de manera más robusta
            for (int i = 0; i < 5; i++) {
                line = reader.readLine();
                if (line == null) break;
                
                String[] parts = line.split("=");
                if (parts.length != 2) continue;
                
                switch (parts[0]) {
                    case "TP": pageSize = Integer.parseInt(parts[1].trim()); break;
                    case "NF": numRows = Integer.parseInt(parts[1].trim()); break;
                    case "NC": numCols = Integer.parseInt(parts[1].trim()); break;
                    case "NR": numReferences = Integer.parseInt(parts[1].trim()); break;
                    case "NP": numPages = Integer.parseInt(parts[1].trim()); break;
                }
            }

            if (numReferences == 0) {
                throw new IllegalStateException("No se pudo leer el número de referencias esperado");
            }

            System.out.printf("Configuración: TP=%d, NF=%d, NC=%d, NR=%d, NP=%d%n", 
                            pageSize, numRows, numCols, numReferences, numPages);
            
            int processedCount = 0;
            int validReferences = 0;
            int batchCount = 0;
            
            while (running && (line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                try {
                    String[] parts = line.split(",");
                    if (parts.length >= 4) {
                        String matrixCell = parts[0].trim();
                        int pageNumber = Integer.parseInt(parts[1].trim());
                        int offset = Integer.parseInt(parts[2].trim());
                        String action = parts[3].trim();
                        
                        // Validar los valores
                        if (pageNumber >= 0 && pageNumber < numPages && 
                            offset >= 0 && offset < pageSize && 
                            (action.equals("R") || action.equals("W"))) {
                            
                            boolean isWrite = action.equals("W");
                            if (pageManager.accessPage(pageNumber, isWrite)) {
                                validReferences++;
                            }
                        }
                        
                        processedCount++;
                        batchCount++;
                        
                        // Esperar 1ms después de procesar 10000 referencias
                        if (batchCount >= REFERENCES_PER_BATCH) {
                            double progress = (double)processedCount * 100 / numReferences;
                            System.out.printf("Progreso: %.2f%% (%d/%d) - Referencias válidas: %d%n", 
                                       progress, processedCount, numReferences, validReferences);
                            Thread.sleep(1); // Pausa de 1ms después de cada lote
                            batchCount = 0;
                        }
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.err.println("Error en línea: " + line);
                    continue;
                }
            }
            
            reader.close();
            
            System.out.println("\n=== Resumen Final ===");
            System.out.println("Referencias procesadas: " + processedCount);
            System.out.println("Referencias válidas: " + validReferences);
            System.out.println("Referencias esperadas: " + numReferences);
            
            if (Math.abs(validReferences - numReferences) > numReferences * 0.01) {
                System.err.println("ADVERTENCIA: Diferencia significativa en el número de referencias");
                System.err.printf("Diferencia: %d (%.2f%%)%n", 
                    Math.abs(validReferences - numReferences),
                    Math.abs(validReferences - numReferences) * 100.0 / numReferences);
            }
            
        } catch (Exception e) {
            System.err.println("Error fatal en el procesamiento de referencias:");
            e.printStackTrace();
        }
    }

    public void stopProcessing() {
        running = false;
    }
}