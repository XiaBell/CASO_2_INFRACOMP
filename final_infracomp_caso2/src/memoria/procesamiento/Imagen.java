package memoria.procesamiento;

import java.io.*;

public class Imagen {

    byte[] header = new byte[54];
    byte[][][] imagen;
    public int alto, ancho; // en pixeles
    int padding;

    public Imagen(String ruta) {
               try {
            FileInputStream fis = new FileInputStream(ruta);
            fis.read(header);

            // Extraer el ancho y alto de la imagen desde la cabecera
            // Almacenados en little endian
            ancho = ((header[21] & 0xFF) << 24) | ((header[20] & 0xFF) << 16) |
                    ((header[19] & 0xFF) << 8) | (header[18] & 0xFF);
            alto = ((header[25] & 0xFF) << 24) | ((header[24] & 0xFF) << 16) |
                    ((header[23] & 0xFF) << 8) | (header[22] & 0xFF);

            // System.out.println("Ancho: " + ancho + " px, Alto: " + alto + " px");
            imagen = new byte[alto][ancho][3];

            int rowSizeSinPadding = ancho * 3;
            // El tamaño de la fila debe ser múltiplo de 4 bytes
            padding = (4 - (rowSizeSinPadding % 4)) % 4;

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void guardar(String ruta) {
        byte pad = 0;
        try {
            FileOutputStream fos = new FileOutputStream(ruta);
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
                for (int k = 0; k < padding; k++)
                    fos.write(pad);
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}