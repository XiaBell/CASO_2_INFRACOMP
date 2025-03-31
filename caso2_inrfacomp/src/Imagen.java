import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class Imagen {
    byte[] header = new byte[54];
    byte[][][] imagen;
    int alto, ancho; // en pixeles
    int padding;

    /**
    * Método para crear una matriz imagen a partir de un archivo.
    * @param input: nombre del archivo. El formato debe ser BMP de 24 bits de bit depth
    * @pos la matriz imagen tiene los valores correspondientes a la imagen
    * almacenada en el archivo.
    */
    public Imagen(String nombre) {
        try {
            FileInputStream fis = new FileInputStream(nombre);
            fis.read(header);
            // Extraer el ancho y alto de la imagen desde la cabecera
            // Almacenados en little endian
            ancho = ((header[21] & 0xFF) << 24) | ((header[20] & 0xFF) << 16) |
                    ((header[19] & 0xFF) << 8) | (header[18] & 0xFF);
            alto = ((header[25] & 0xFF) << 24) | ((header[24] & 0xFF) << 16) |
                    ((header[23] & 0xFF) << 8) | (header[22] & 0xFF);
            System.out.println("Ancho: " + ancho + " px, Alto: " + alto + " px");
            imagen = new byte[alto][ancho][3];
            int rowSizeSinPadding = ancho * 3;
            // El tamaño de la fila debe ser múltiplo de 4 bytes
            padding = (4 - (rowSizeSinPadding%4))%4;
            // Leer y modificar los datos de los píxeles
            // (en formato RGB, pero almacenados en orden BGR)
            byte[] pixel = new byte[3];
            for (int i = 0; i < alto; i++) {
                for (int j = 0; j < ancho; j++) {
                    // Leer los 3 bytes del píxel (B, G, R)
                    fis.read(pixel);
                    imagen[i][j][0] = pixel[0];
                    imagen[i][j][1] = pixel[1];
                    imagen[i][j][2] = pixel[2];
                }
                fis.skip(padding);
            }
            fis.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
    * Método para escribir una imagen a un archivo en formato BMP
    * @param output: nombre del archivo donde se almacenará la imagen.
    * Se espera que se invoque para almacenar la imagen modificada.
    * @pre la matriz imagen debe haber sido inicializada con una imagen
    * @pos se creó el archivo en formato bmp con la información de la matriz imagen
    */
    public void escribirImagen(String output) {
        byte pad = 0;
        try {
            FileOutputStream fos = new FileOutputStream(output);
            fos.write(header);
            byte[] pixel = new byte[3];
            for (int i = 0; i < alto; i++) {
                for (int j = 0; j < ancho; j++) {
                    // Leer los 3 bytes del píxel (B, G, R)
                    pixel[0] = imagen[i][j][0];
                    pixel[1] = imagen[i][j][1];
                    pixel[2] = imagen[i][j][2];
                    fos.write(pixel);
                }
                for (int k=0; k<padding; k++) fos.write(pad);
            }
            fos.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public List<MemoryReference> generateReferences(int pageSize) {
        List<MemoryReference> references = new ArrayList<>();
        
        // Calculate matrix sizes and pages
        int imageSize = alto * ancho * 3;
        int filterSize = 9 * 4; // 9 integers for each filter (3x3)
        
        // Calculate base page numbers for each matrix
        int imagePages = (imageSize + pageSize - 1) / pageSize;
        int filterXPages = (filterSize + pageSize - 1) / pageSize;
        int filterYPages = (filterSize + pageSize - 1) / pageSize;
        
        // Generate references for Sobel filter application
        for (int i = 0; i < alto; i++) {
            for (int j = 0; j < ancho; j++) {
                // Para cada píxel procesamos su vecindad 3x3
                for (int ki = -1; ki <= 1; ki++) {
                    for (int kj = -1; kj <= 1; kj++) {
                        // Calcular posición del píxel vecino con manejo de bordes
                        int ni = Math.max(0, Math.min(i + ki, alto - 1));
                        int nj = Math.max(0, Math.min(j + kj, ancho - 1));
                        
                        // Referencias para cada componente RGB del píxel vecino
                        for (int c = 0; c < 3; c++) {
                            int pixelOffset = (ni * ancho + nj) * 3 + c;
                            int pageNumber = pixelOffset / pageSize;
                            int offset = pixelOffset % pageSize;
                            String component = c == 0 ? "B" : (c == 1 ? "G" : "R");
                            references.add(new MemoryReference(
                                String.format("Imagen[%d][%d].%s", ni, nj, component),
                                pageNumber,
                                offset,
                                "R"
                            ));
                        }
                        
                        // Referencias a los kernels Sobel
                        int kernelIndex = (ki + 1) * 3 + (kj + 1);
                        
                        // Referencia a SOBEL_X
                        int sobelXOffset = kernelIndex * 4;
                        int sobelXPage = imagePages + (sobelXOffset / pageSize);
                        references.add(new MemoryReference(
                            String.format("SOBEL_X[%d][%d]", ki+1, kj+1),
                            sobelXPage,
                            sobelXOffset % pageSize,
                            "R"
                        ));
                        
                        // Referencia a SOBEL_Y
                        int sobelYOffset = kernelIndex * 4;
                        int sobelYPage = imagePages + filterXPages + (sobelYOffset / pageSize);
                        references.add(new MemoryReference(
                            String.format("SOBEL_Y[%d][%d]", ki+1, kj+1),
                            sobelYPage,
                            sobelYOffset % pageSize,
                            "R"
                        ));
                    }
                }
                
                // Referencias para escribir el resultado (una por cada componente BGR)
                for (int c = 0; c < 3; c++) {
                    int resultOffset = (i * ancho + j) * 3 + c;
                    int pageNumber = imagePages + filterXPages + filterYPages + (resultOffset / pageSize);
                    references.add(new MemoryReference(
                        String.format("ImagenOut[%d][%d].%s", i, j, (c == 0 ? "B" : (c == 1 ? "G" : "R"))),
                        pageNumber,
                        resultOffset % pageSize,
                        "W"
                    ));
                }
            }
        }
        
        return references;
    }
} 