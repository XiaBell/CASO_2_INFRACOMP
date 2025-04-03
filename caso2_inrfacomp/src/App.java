import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import memory.GestorPaginas;
import threads.ReferenceProcessor;

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
            scanner.nextLine(); // Consumir el salto de línea

            switch (option) {
                case 1:
                    generarReferencias(scanner);
                    break;
                case 2:
                    calcularDatos(scanner);
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

    private static void generarReferencias(Scanner scanner) {
        try {
            System.out.print("Ingrese el tamaño de página (en bytes): ");
            int tamanoPagina = scanner.nextInt();
            scanner.nextLine(); // Consumir el salto de línea

            System.out.print("Ingrese el nombre del archivo de salida para las referencias: ");
            String archivoSalida = scanner.nextLine();

            // Generar referencias ficticias para prueba
            List<String> referencias = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                referencias.add(String.format("Página %d, Operación: %s", i, (i % 2 == 0 ? "Lectura" : "Escritura")));
            }

            // Guardar referencias en el archivo
            try (FileWriter writer = new FileWriter(archivoSalida)) {
                for (String referencia : referencias) {
                    writer.write(referencia + "\n");
                }
            }

            System.out.println("Referencias generadas exitosamente en " + archivoSalida);
        } catch (IOException e) {
            System.err.println("Error al generar referencias: " + e.getMessage());
        }
    }

    private static void calcularDatos(Scanner scanner) {
        try {
            System.out.print("Ingrese el número de marcos de página: ");
            int numMarcos = scanner.nextInt();
            scanner.nextLine(); // Consumir el salto de línea

            System.out.print("Ingrese el nombre del archivo de referencias: ");
            String archivoReferencias = scanner.nextLine();

            GestorPaginas gestorPaginas = new GestorPaginas(100, numMarcos); // 100 páginas como ejemplo
            ReferenceProcessor procesador = new ReferenceProcessor(gestorPaginas, archivoReferencias);

            // Iniciar procesamiento
            procesador.start();
            procesador.join();

            // Mostrar resultados
            System.out.println("\n=== Resultados ===");
            System.out.println("Hits: " + gestorPaginas.getHits());
            System.out.println("Fallos: " + gestorPaginas.getFallos());
        } catch (Exception e) {
            System.err.println("Error al calcular datos: " + e.getMessage());
        }
    }
}

