import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BMPImage {
    private int width;
    private int height;
    private byte[] imageData;
    private static final int[] SOBEL_X = {-1, 0, 1, -2, 0, 2, -1, 0, 1};
    private static final int[] SOBEL_Y = {-1, -2, -1, 0, 0, 0, 1, 2, 1};

    public BMPImage(String filename) throws IOException {
        try (FileInputStream fis = new FileInputStream(filename)) {
            // Skip BMP header (54 bytes)
            byte[] header = new byte[54];
            fis.read(header);

            // Read image dimensions
            width = (header[21] & 0xFF) << 24 | (header[20] & 0xFF) << 16 |
                   (header[19] & 0xFF) << 8 | (header[18] & 0xFF);
                   
            height = (header[25] & 0xFF) << 24 | (header[24] & 0xFF) << 16 |
                    (header[23] & 0xFF) << 8 | (header[22] & 0xFF);

            // Read image data
            imageData = new byte[width * height * 3];
            fis.read(imageData);
        }
    }

    public List<MemoryReference> generateReferences(int pageSize) {
        List<MemoryReference> references = new ArrayList<>();
        
        // Calculate matrix sizes
        int imageSize = width * height * 3;
        int filterSize = 9 * 4; // 9 integers for each filter
        int responseSize = width * height * 3;
        
        // Calculate number of pages for each matrix
        int imagePages = (imageSize + pageSize - 1) / pageSize;
        int filterXPages = (filterSize + pageSize - 1) / pageSize;
        int filterYPages = (filterSize + pageSize - 1) / pageSize;
        int responsePages = (responseSize + pageSize - 1) / pageSize;
        
        // Generate references for Sobel filter application
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                // References to input image
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        for (int c = 0; c < 3; c++) {
                            int pixelIndex = ((y + i) * width + (x + j)) * 3 + c;
                            int pageNumber = pixelIndex / pageSize;
                            int offset = pixelIndex % pageSize;
                            String component = c == 0 ? "r" : (c == 1 ? "g" : "b");
                            references.add(new MemoryReference(
                                String.format("Imagen[%d][%d].%s", y + i, x + j, component),
                                pageNumber,
                                offset,
                                "R"
                            ));
                        }
                    }
                }
                
                // References to filterX
                for (int i = 0; i < 9; i++) {
                    int pageNumber = imagePages + (i * 4) / pageSize;
                    int offset = (i * 4) % pageSize;
                    references.add(new MemoryReference(
                        String.format("SOBEL_X[%d][%d]", i/3, i%3),
                        pageNumber,
                        offset,
                        "R"
                    ));
                }
                
                // References to filterY
                for (int i = 0; i < 9; i++) {
                    int pageNumber = imagePages + filterXPages + (i * 4) / pageSize;
                    int offset = (i * 4) % pageSize;
                    references.add(new MemoryReference(
                        String.format("SOBEL_Y[%d][%d]", i/3, i%3),
                        pageNumber,
                        offset,
                        "R"
                    ));
                }
                
                // Reference to response matrix
                int responseIndex = (y * width + x) * 3;
                int pageNumber = imagePages + filterXPages + filterYPages + responseIndex / pageSize;
                int offset = responseIndex % pageSize;
                references.add(new MemoryReference(
                    String.format("Rta[%d][%d]", y, x),
                    pageNumber,
                    offset,
                    "W"
                ));
            }
        }
        
        return references;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
} 