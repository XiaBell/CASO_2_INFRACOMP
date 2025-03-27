# Simulador de Paginación NRU para Procesamiento de Imágenes

Este proyecto implementa un simulador del sistema de paginación que utiliza el algoritmo de reemplazo "Páginas No Usadas Recientemente" (NRU) en el contexto del procesamiento de imágenes usando el operador de Sobel.

## Descripción

El simulador analiza el comportamiento de la memoria virtual durante la aplicación del filtro de Sobel sobre imágenes BMP. El filtro de Sobel es una técnica utilizada en procesamiento de imágenes y visión por computadora para detectar bordes en una imagen, resaltando cambios abruptos en la intensidad de los píxeles.

### Características Principales

- Simulación del algoritmo de reemplazo NRU (Not Recently Used)
- Procesamiento de imágenes BMP con profundidad de 24 bits
- Generación de referencias de memoria para el operador de Sobel
- Cálculo de fallas de página y porcentaje de hits
- Análisis de tiempos de acceso (RAM vs SWAP)
- Implementación concurrente con dos threads

## Requisitos

- Java JDK 8 o superior
- Imágenes BMP con profundidad de 24 bits
- Tamaño de imagen recomendado: 500x300 píxeles

## Estructura del Proyecto

```
caso2_inrfacomp/
├── src/
│   ├── App.java              # Clase principal con el menú y control de flujo
│   ├── BMPImage.java         # Manejo de imágenes BMP y generación de referencias
│   ├── MemoryReference.java  # Representación de referencias a memoria
│   ├── Page.java            # Representación de páginas virtuales
│   ├── PageManager.java     # Gestión de páginas y algoritmo NRU
│   ├── ReferenceProcessor.java # Thread para procesar referencias
│   └── PageStateUpdater.java   # Thread para actualizar estados de páginas
└── README.md
```

## Uso del Programa

El programa presenta un menú con las siguientes opciones:

### 1. Generación de Referencias
- Entrada:
  - Tamaño de página (en bytes)
  - Nombre del archivo de imagen BMP
- Salida:
  - Archivo `referencias.txt` con:
    - TP: Tamaño de página
    - NF: Número de filas de la imagen
    - NC: Número de columnas de la imagen
    - NR: Número total de referencias
    - NP: Número de páginas virtuales
    - Lista de referencias en formato: `matriz[celda],página,desplazamiento,acción`

### 2. Cálculo de Datos
- Entrada:
  - Número de marcos de página
  - Nombre del archivo de referencias
- Salida:
  - Número de fallas de página
  - Porcentaje de hits
  - Tiempo total de ejecución
  - Tiempos teóricos (todo en RAM vs todo en SWAP)

## Tiempos de Acceso

- Acceso a RAM: 50 ns
- Acceso a SWAP (incluyendo resolución de falla de página): 10 ms

## Implementación Técnica

### Algoritmo NRU
El algoritmo de reemplazo NRU clasifica las páginas en cuatro categorías:
1. No referenciada, no modificada
2. No referenciada, modificada
3. Referenciada, no modificada
4. Referenciada, modificada

### Concurrencia
El programa utiliza dos threads:
1. **ReferenceProcessor**: Procesa 10000 referencias y espera 1ms
2. **PageStateUpdater**: Actualiza el estado de las páginas cada 1ms

### Matrices en Memoria
Las matrices se almacenan en el siguiente orden:
1. Matriz de imagen original (RGB)
2. Matriz del filtro Sobel X (3x3)
3. Matriz del filtro Sobel Y (3x3)
4. Matriz de respuesta (RGB)

### Formato de Referencias
Cada referencia en el archivo de salida tiene el siguiente formato:
```
matriz[celda],página,desplazamiento,acción
```
Donde:
- `matriz[celda]`: Identificador de la celda (ej: "Imagen[0][0].r", "SOBEL_X[0][0]")
- `página`: Número de página virtual
- `desplazamiento`: Desplazamiento dentro de la página
- `acción`: "R" para lectura, "W" para escritura

### Procesamiento de Imágenes
- La imagen se procesa pixel por pixel, excluyendo los bordes
- Para cada pixel:
  1. Se leen los 9 píxeles del vecindario (3x3)
  2. Se aplican los filtros Sobel X e Y
  3. Se calcula la magnitud del gradiente
  4. Se escribe el resultado en la matriz de respuesta

## Ejemplo de Uso

1. Generar referencias:
```
=== Sistema de Paginación NRU ===
1. Generación de referencias
2. Calcular datos (fallas, hits, tiempos)
3. Salir
Seleccione una opción: 1
Ingrese el tamaño de página (en bytes): 512
Ingrese el nombre del archivo de imagen BMP: caso2-parrotspeq.bmp
```

2. Calcular datos:
```
Seleccione una opción: 2
Ingrese el número de marcos de página: 4
Ingrese el nombre del archivo de referencias: referencias.txt
```

## Resultados Esperados

Para una imagen de 79x119 píxeles con páginas de 512 bytes:
- Total de referencias: 756,756
- Con 4 marcos:
  - Hits: ~743,318
  - Fallas: ~13,438
- Con 6 marcos:
  - Hits: ~756,556
  - Fallas: ~200

## Notas Adicionales

- El programa está optimizado para imágenes de tamaño moderado (500x300 píxeles)
- Los resultados pueden variar ligeramente debido a la concurrencia
- Se recomienda usar diferentes tamaños de página y número de marcos para análisis comparativo
- El programa incluye archivos de ejemplo: caso2-parrotspeq.bmp y caso2-parrotspeq_sal.bmp
