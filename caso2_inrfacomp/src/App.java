import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;

public class App {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n=== Sistema de Paginación NRU ===");
            System.out.println("1. Generación de referencias");
            System.out.println("2. Calcular datos (fallas, hits, tiempos)");
            System.out.println("3. Salir");
            System.out.print("Seleccione una opción: ");

            int option = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (option) {
                case 1:
                    generateReferences(scanner);
                    break;
                case 2:
                    calculateData(scanner);
                    break;
                case 3:
                    running = false;
                    break;
                default:
                    System.out.println("Opción no válida");
            }
        }
        scanner.close();
    }

    private static void generateReferences(Scanner scanner) {
        try {
            System.out.print("Ingrese el tamaño de página (en bytes): ");
            int pageSize = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            System.out.print("Ingrese el nombre del archivo de imagen BMP: ");
            String imageFile = scanner.nextLine();

            // Load and process the image
            BMPImage image = new BMPImage(imageFile);
            List<MemoryReference> references = image.generateReferences(pageSize);

            // Calculate total number of pages
            int totalPages = (image.getWidth() * image.getHeight() * 3 + pageSize - 1) / pageSize + // image
                           (9 * 4 + pageSize - 1) / pageSize + // filterX
                           (9 * 4 + pageSize - 1) / pageSize + // filterY
                           (image.getWidth() * image.getHeight() * 3 + pageSize - 1) / pageSize;   // response

            // Generate output file
            String outputFile = "referencias.txt";
            try (FileWriter writer = new FileWriter(outputFile)) {
                // Write header information
                writer.write("TP=" + pageSize + "\n");
                writer.write("NF=" + image.getHeight() + "\n");
                writer.write("NC=" + image.getWidth() + "\n");
                writer.write("NR=" + references.size() + "\n");
                writer.write("NP=" + totalPages + "\n");

                // Write references
                for (MemoryReference ref : references) {
                    writer.write(ref.toString() + "\n");
                }
            }

            System.out.println("Referencias generadas exitosamente en " + outputFile);
        } catch (IOException e) {
            System.out.println("Error al generar referencias: " + e.getMessage());
        }
    }

    private static void calculateData(Scanner scanner) {
        try {
            System.out.print("Ingrese el número de marcos de página: ");
            int numFrames = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            System.out.print("Ingrese el nombre del archivo de referencias: ");
            String referenceFile = scanner.nextLine();

            // Read header information
            BufferedReader reader = new BufferedReader(new FileReader(referenceFile));
            String line = reader.readLine();
            String[] parts = line.split("=");
            int pageSize = Integer.parseInt(parts[1]);
            
            line = reader.readLine();
            parts = line.split("=");
            int numRows = Integer.parseInt(parts[1]);
            
            line = reader.readLine();
            parts = line.split("=");
            int numCols = Integer.parseInt(parts[1]);
            
            line = reader.readLine();
            parts = line.split("=");
            int numReferences = Integer.parseInt(parts[1]);
            
            line = reader.readLine();
            parts = line.split("=");
            int numPages = Integer.parseInt(parts[1]);
            
            reader.close();

            // Create page manager and threads
            PageManager pageManager = new PageManager(numPages, numFrames);
            ReferenceProcessor processor = new ReferenceProcessor(pageManager, referenceFile);
            PageStateUpdater updater = new PageStateUpdater(pageManager);

            // Start threads
            processor.start();
            updater.start();

            // Wait for reference processing to complete
            processor.join();
            updater.stopUpdating();
            updater.join();

            // Calculate and display results
            int hits = pageManager.getHits();
            int misses = pageManager.getMisses();
            long totalTime = pageManager.getTotalTime();
            double hitPercentage = pageManager.getHitPercentage();

            // Calculate theoretical times
            long allRAMTime = numReferences * 50; // 50ns per reference
            long allSWAPTime = numReferences * 10_000_000; // 10ms per reference

            System.out.println("\nResultados:");
            System.out.println("Número de fallas de página: " + misses);
            System.out.println("Porcentaje de hits: " + String.format("%.2f", hitPercentage) + "%");
            System.out.println("Tiempo total: " + totalTime + " ns");
            System.out.println("Tiempo si todo estuviera en RAM: " + allRAMTime + " ns");
            System.out.println("Tiempo si todo estuviera en SWAP: " + allSWAPTime + " ns");

        } catch (IOException | InterruptedException e) {
            System.out.println("Error al calcular datos: " + e.getMessage());
        }
    }
}
