package interfaz;

import memoria.util.GeneradorReferencias;
import memoria.util.SimuladorPaginacion;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;

public class MainApp {
    private static final String CARPETA_IMAGENES = "assets/";
    private static final String ARCHIVO_REFERENCIAS = "referencias.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean ejecutando = true;

        while (ejecutando) {
            System.out.println("\n=== Simulador de Memoria Virtual ===");
            System.out.println("1. Generar traza de referencias");
            System.out.println("2. Ejecutar simulador de paginación");
            System.out.println("3. Salir");
            System.out.print("Seleccione opción: ");

            int opcion = scanner.nextInt();
            scanner.nextLine(); // Limpiar buffer

            switch (opcion) {
                case 1:
                    generarTrazaReferencias(scanner);
                    break;
                case 2:
                    ejecutarSimuladorPaginacion(scanner);
                    break;
                case 3:
                    ejecutando = false;
                    System.out.println("Saliendo del sistema...");
                    break;
                default:
                    System.out.println("Opción no válida");
            }
        }
        scanner.close();
    }

    private static void generarTrazaReferencias(Scanner scanner) {
        System.out.print("\nIngrese nombre de la imagen (sin extensión): ");
        String rutaImagen = scanner.nextLine();

        if (!new File(rutaImagen).exists()) {
            System.out.println("¡Error! La imagen no existe en la carpeta assets/");
            return;
        }

        System.out.print("Ingrese tamaño de página (bytes): ");
        int tamPagina = scanner.nextInt();

        GeneradorReferencias generador = new GeneradorReferencias(tamPagina, rutaImagen);
        generador.generarTraza();

        try {
            generador.guardarArchivo(ARCHIVO_REFERENCIAS);
            System.out.println("Traza generada exitosamente en " + ARCHIVO_REFERENCIAS);
            System.out.println("Total referencias: " + generador.getNumReferencias());
        } catch (IOException e) {
            System.err.println("Error al guardar archivo: " + e.getMessage());
        }
    }

    private static void ejecutarSimuladorPaginacion(Scanner scanner) {
        System.out.print("\nIngrese número de marcos de página: ");
        int numMarcos = scanner.nextInt();
        scanner.nextLine(); // Limpiar buffer

        if (!new File(ARCHIVO_REFERENCIAS).exists()) {
            System.out.println("¡Error! Primero genere el archivo de referencias");
            return;
        }

        SimuladorPaginacion simulador = new SimuladorPaginacion(numMarcos, ARCHIVO_REFERENCIAS);
        simulador.ejecutarSimulacion();
        simulador.mostrarResultados();
    }
}