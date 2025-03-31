import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.math.*;


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

        // Load image
        Imagen imagen = new Imagen(imageFile);
        List<MemoryReference> references = new ArrayList<>();
        
        // Calculate matrix sizes and pages
        int imageSize = imagen.alto * imagen.ancho * 3;
        int filterSize = 9 * 4; // 9 integers for each filter (3x3)
        
        // Calculate base page numbers for each matrix
        int imageBaseAddress = 0;
        int filterXBaseAddress = imageSize;
        int filterYBaseAddress = filterXBaseAddress + filterSize;
        int resultBaseAddress = filterYBaseAddress + filterSize;
        
        int imagePages =   imageSize;
        int filterXPages = filterSize;
        int filterYPages = filterSize;
        int resultPages =  imageSize;
        
        // Generate references for Sobel filter application
        for (int i = 1; i < imagen.alto - 1; i++) {
            for (int j = 1; j < imagen.ancho - 1; j++) {
                // Para cada pixel central, procesamos su vecindad 3x3
                
                // Leer valores de gradiente para kernel Sobel X e Y
                for (int ki = -1; ki <= 1; ki++) {
                    for (int kj = -1; kj <= 1; kj++) {
                        // Referencias a los píxeles vecinos (RGB)
                        int ni = i + ki;
                        int nj = j + kj;
                        
                        // Por cada pixel vecino, leer RGB
                        for (int c = 0; c < 3; c++) {
                            int pixelOffset = (ni * imagen.ancho + nj) * 3 + c;
                            int pageNumber = pixelOffset / pageSize;
                            String colorComponent = (c == 0) ? "r" : ((c == 1) ? "g" : "b");
                            references.add(new MemoryReference(
                                String.format("Imagen[%d][%d].%s", ni, nj, colorComponent),
                                pageNumber,
                                pixelOffset % pageSize,
                                "R"
                            ));
                        }
                        
                        // Referencias a los kernels Sobel (3 veces para simular operaciones)
                        int kernelIndex = (ki + 1) * 3 + (kj + 1);
                        
                        // SOBEL_X (leer 3 veces para simular operación por cada canal)
                        int sobelXOffset = kernelIndex * 4; // 4 bytes per int
                        int sobelXPage = imagePages + (sobelXOffset / pageSize);
                        for (int rep = 0; rep < 3; rep++) { // Una lectura por cada canal de color
                            references.add(new MemoryReference(
                                String.format("SOBEL_X[%d][%d]", ki+1, kj+1),
                                sobelXPage,
                                sobelXOffset % pageSize,
                                "R"
                            ));
                        }
                        
                        // SOBEL_Y (leer 3 veces para simular operación por cada canal de color)
                        int sobelYOffset = kernelIndex * 4; // 4 bytes per int
                        int sobelYPage = imagePages + filterXPages + (sobelYOffset / pageSize);
                        for (int rep = 0; rep < 3; rep++) { // Una lectura por cada canal de color
                            references.add(new MemoryReference(
                                String.format("SOBEL_Y[%d][%d]", ki+1, kj+1),
                                sobelYPage,
                                sobelYOffset % pageSize,
                                "R"
                            ));
                        }
                    }
                }
                
                // Escribir resultado (RGB para el pixel actual)
                for (int c = 0; c < 3; c++) {
                    int resultOffset = (i * imagen.ancho + j) * 3 + c;
                    int resultPage = imagePages + filterXPages + filterYPages + (resultOffset / pageSize);
                    String colorComponent = (c == 0) ? "r" : ((c == 1) ? "g" : "b");
                    references.add(new MemoryReference(
                        String.format("Rta[%d][%d].%s", i, j, colorComponent),
                        resultPage,
                        resultOffset % pageSize,
                        "W"
                    ));
                }
            }
        }

        // Calculate total number of pages
        int totalPages = (int) Math.ceil((double) (imagePages + filterXPages + filterYPages + resultPages) / pageSize);

        // Generate output file
        String outputFile = "referencias.txt";
        try (FileWriter writer = new FileWriter(outputFile)) {
            // Write header information
            writer.write("TP=" + pageSize + "\n");
            writer.write("NF=" + imagen.alto + "\n");
            writer.write("NC=" + imagen.ancho + "\n");
            writer.write("NR=" + references.size() + "\n");
            writer.write("NP=" + totalPages + "\n");

            // Write references
            for (MemoryReference ref : references) {
                writer.write(ref.toString() + "\n");
            }
        }

        System.out.println("Referencias generadas exitosamente en " + outputFile);
        System.out.println("Número total de referencias generadas: " + references.size());
        System.out.println("Número total de páginas: " + totalPages);
    } catch (IOException e) {
        System.out.println("Error al generar referencias: " + e.getMessage());
        e.printStackTrace();
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

            System.out.println("\nConfiguración:");
            System.out.println("- Tamaño de página: " + pageSize + " bytes");
            System.out.println("- Número de filas: " + numRows);
            System.out.println("- Número de columnas: " + numCols);
            System.out.println("- Número de referencias: " + numReferences);
            System.out.println("- Número total de páginas: " + numPages);
            System.out.println("- Marcos de memoria: " + numFrames);
            System.out.println("\nIniciando simulación...");
            
            // Create page manager and threads
            PageManager pageManager = new PageManager(numPages, numFrames);
            ReferenceProcessor processor = new ReferenceProcessor(pageManager, referenceFile);
            PageStateUpdater updater = new PageStateUpdater(pageManager);

            long startTime = System.currentTimeMillis();
            
            // Verificar estado inicial
            System.out.println("\nEstado inicial de la tabla de páginas:");
            pageManager.printPageTableStatus();
            
            // Start threads
            processor.start();
            updater.start();

            // Wait for reference processing to complete
            processor.join();
            updater.stopUpdating();
            updater.join();
            
            long endTime = System.currentTimeMillis();
            
            // Verificar estado final
            System.out.println("\nEstado final de la tabla de páginas:");
            pageManager.printPageTableStatus();

            // Calculate and display results
            int hits = pageManager.getHits();
            int misses = pageManager.getMisses();
            long totalTime = pageManager.getTotalTime();
            double hitPercentage = pageManager.getHitPercentage();
            int totalRefs = hits + misses;

            // Calculate theoretical times
            long allRAMTime = numReferences * 50; // 50ns per reference
            long allSWAPTime = numReferences * 10_000_000; // 10ms per reference

            System.out.println("\n==== RESULTADOS DE LA SIMULACIÓN ====");
            System.out.println(pageManager.getMemoryStatus());
            System.out.println("Tiempo real de ejecución: " + (endTime - startTime) + " ms");
            System.out.println("\nEstadísticas de acceso a memoria:");
            System.out.println("- Número total de referencias procesadas: " + totalRefs);
            System.out.println("- Número de hits: " + hits);
            System.out.println("- Número de fallos de página: " + misses);
            System.out.println("- Porcentaje de hits: " + String.format("%.2f", hitPercentage) + "%");
            
            if (totalRefs != numReferences) {
                System.out.println("\n⚠️ ADVERTENCIA: El número de referencias procesadas (" + totalRefs + 
                                  ") no coincide con el número esperado (" + numReferences + ")");
                System.out.println("  Diferencia: " + Math.abs(totalRefs - numReferences) + " referencias");
                double errorPct = Math.abs((double)(totalRefs - numReferences) / numReferences * 100);
                System.out.println("  Error: " + String.format("%.2f", errorPct) + "%");
            }
            
            System.out.println("\nTiempos de acceso simulados:");
            System.out.println("- Tiempo total simulado: " + totalTime + " ns");
            System.out.println("- Tiempo si todo estuviera en RAM: " + allRAMTime + " ns");
            System.out.println("- Tiempo si todo estuviera en SWAP: " + allSWAPTime + " ns");
            System.out.println("- Factor de mejora vs. todo en SWAP: " + String.format("%.2f", (double)allSWAPTime / totalTime) + "x");

        } catch (IOException | InterruptedException e) {
            System.out.println("Error al calcular datos: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

