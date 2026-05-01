import java.io.*;
import java.util.*;

/**
 * ExternalSort.java  –  Todo en uno
 * ──────────────────────────────────
 * Solo dale Run. El programa:
 *   1) Genera automáticamente un archivo de datos de prueba
 *   2) Lo ordena con el algoritmo de Ordenamiento Externo
 *   3) Guarda el resultado en resultado.txt
 *
 * No necesitas escribir nada en la terminal.
 */
public class ExternalSort {

    // ── Configuración general ─────────────────────────────────────────────────
    static final String ARCHIVO_ENTRADA  = "datos.txt";
    static final String ARCHIVO_SALIDA   = "resultado.txt";
    static final long   LINEAS_A_GENERAR = 500_000;       // cambia este número si quieres más datos

    // ── Configuración del algoritmo ───────────────────────────────────────────
    static final long CHUNK_SIZE_BYTES = 512L * 1024 * 1024; // 512 MB por bloque
    static final int  READ_BUFFER      = 64  * 1024;          // 64 KB por archivo temporal
    static final int  WRITE_BUFFER     = 4   * 1024 * 1024;   // 4 MB escritura

    static final String TEMP_PREFIX = "temp_";
    static final String TEMP_EXT    = ".dat";

    static final Random RNG = new Random();
    static final String[] PALABRAS = {
        "aguacate","banana","cereza","durazno","frambuesa","guayaba","higo",
        "kiwi","limon","mango","naranja","papaya","platano","sandia","uva",
        "manzana","pera","fresa","melon","coco","ciruela","granada","lichi"
    };

    // ═════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) throws IOException {

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║        ORDENAMIENTO EXTERNO  -  Java         ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        // PASO 0: Generar datos de prueba
        generarDatos(ARCHIVO_ENTRADA, LINEAS_A_GENERAR);

        // PASO 1: Fase de Particion
        long t0 = System.currentTimeMillis();
        List<File> temporales = particion(ARCHIVO_ENTRADA);
        long t1 = System.currentTimeMillis();
        System.out.printf("\n✔ Particion completada en %.2f s  (%d archivos temporales)\n",
                (t1 - t0) / 1000.0, temporales.size());

        // PASO 2: Fase de Mezcla
        mezcla(temporales, ARCHIVO_SALIDA);
        long t2 = System.currentTimeMillis();
        System.out.printf("✔ Mezcla completada en %.2f s\n", (t2 - t1) / 1000.0);

        // Resumen final
        System.out.println("\n══════════════════════════════════════════════");
        System.out.printf("Entrada : %s  (%s)\n",
                ARCHIVO_ENTRADA, formatBytes(new File(ARCHIVO_ENTRADA).length()));
        System.out.printf("Salida  : %s  (%s)\n",
                ARCHIVO_SALIDA,  formatBytes(new File(ARCHIVO_SALIDA).length()));
        System.out.printf("Tiempo total: %.2f s\n", (t2 - t0) / 1000.0);
        System.out.println("══════════════════════════════════════════════");
        System.out.println("\n¡Listo! Abre resultado.txt para ver los datos ordenados.");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // GENERADOR DE DATOS DE PRUEBA
    // ═════════════════════════════════════════════════════════════════════════
    static void generarDatos(String archivo, long lineas) throws IOException {
        System.out.printf("▶ Generando %,d lineas de prueba en '%s'...\n", lineas, archivo);
        long ini = System.currentTimeMillis();

        try (BufferedWriter w = new BufferedWriter(new FileWriter(archivo), WRITE_BUFFER)) {
            for (long i = 0; i < lineas; i++) {
                w.write(lineaAleatoria());
                w.newLine();
                if ((i + 1) % 100_000 == 0)
                    System.out.printf("  %,d / %,d lineas generadas...\n", i + 1, lineas);
            }
        }
        System.out.printf("  Generacion lista en %.1f s  (%s)\n\n",
                (System.currentTimeMillis() - ini) / 1000.0,
                formatBytes(new File(archivo).length()));
    }

    static String lineaAleatoria() {
        String p = PALABRAS[RNG.nextInt(PALABRAS.length)];
        int    n = RNG.nextInt(100_000_000);
        switch (RNG.nextInt(3)) {
            case 0:  return String.format("%08d", n);
            case 1:  return String.format("%s_%08d", p, n);
            default: return String.format("%08d_%s", n, p);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // FASE 1 - PARTICION
    // ═════════════════════════════════════════════════════════════════════════
    static List<File> particion(String ruta) throws IOException {
        System.out.println("▶ FASE 1 - PARTICION");
        List<File> temporales = new ArrayList<>();
        int numero = 0;

        try (BufferedReader r = new BufferedReader(new FileReader(ruta), READ_BUFFER)) {
            List<String> bloque = new ArrayList<>();
            long bytesBloque = 0;
            String linea;

            while ((linea = r.readLine()) != null) {
                bloque.add(linea);
                bytesBloque += linea.length() * 2L + 2;

                if (bytesBloque >= CHUNK_SIZE_BYTES) {
                    File t = guardarBloque(bloque, ++numero);
                    temporales.add(t);
                    System.out.printf("  [%s] -> %s  (%,d lineas)\n",
                            hora(), t.getName(), bloque.size());
                    bloque.clear();
                    bytesBloque = 0;
                }
            }

            if (!bloque.isEmpty()) {
                File t = guardarBloque(bloque, ++numero);
                temporales.add(t);
                System.out.printf("  [%s] -> %s  (%,d lineas)\n",
                        hora(), t.getName(), bloque.size());
            }
        }
        return temporales;
    }

    static File guardarBloque(List<String> bloque, int num) throws IOException {
        Collections.sort(bloque);   // Timsort en RAM

        File f = new File(TEMP_PREFIX + String.format("%02d", num) + TEMP_EXT);
        f.deleteOnExit();

        try (BufferedWriter w = new BufferedWriter(new FileWriter(f), WRITE_BUFFER)) {
            for (String s : bloque) {
                w.write(s);
                w.newLine();
            }
        }
        return f;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // FASE 2 - MEZCLA K-WAY
    // ═════════════════════════════════════════════════════════════════════════
    static void mezcla(List<File> temporales, String salida) throws IOException {
        System.out.println("\n▶ FASE 2 - MEZCLA (K-Way Merge)");
        System.out.printf("  Archivos a mezclar: %d\n", temporales.size());

        List<BufferedReader> readers = new ArrayList<>();
        try {
            for (File f : temporales)
                readers.add(new BufferedReader(new FileReader(f), READ_BUFFER));

            // Min-heap: siempre extrae la linea mas pequeña
            PriorityQueue<Entrada> heap = new PriorityQueue<>(
                    Math.max(1, temporales.size()),
                    Comparator.comparing(e -> e.linea)
            );

            for (int i = 0; i < readers.size(); i++) {
                String primera = readers.get(i).readLine();
                if (primera != null)
                    heap.offer(new Entrada(primera, i));
            }

            long escritas = 0;
            try (BufferedWriter w = new BufferedWriter(new FileWriter(salida), WRITE_BUFFER)) {
                while (!heap.isEmpty()) {
                    Entrada menor = heap.poll();
                    w.write(menor.linea);
                    w.newLine();
                    escritas++;

                    String siguiente = readers.get(menor.indice).readLine();
                    if (siguiente != null)
                        heap.offer(new Entrada(siguiente, menor.indice));

                    if (escritas % 100_000 == 0)
                        System.out.printf("  [%s] %,d lineas escritas...\n", hora(), escritas);
                }
            }
            System.out.printf("  [%s] Total: %,d lineas en el archivo de salida.\n",
                    hora(), escritas);

        } finally {
            for (BufferedReader r : readers) try { r.close(); } catch (IOException ignored) {}
            for (File f : temporales) f.delete();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // AUXILIARES
    // ═════════════════════════════════════════════════════════════════════════
    static class Entrada {
        String linea;
        int    indice;
        Entrada(String linea, int indice) { this.linea = linea; this.indice = indice; }
    }

    static String formatBytes(long b) {
        if (b >= 1024L*1024*1024) return String.format("%.2f GB", b/(1024.0*1024*1024));
        if (b >= 1024L*1024)      return String.format("%.2f MB", b/(1024.0*1024));
        if (b >= 1024L)           return String.format("%.2f KB", b/1024.0);
        return b + " B";
    }

    static String hora() {
        Calendar c = Calendar.getInstance();
        return String.format("%02d:%02d:%02d",
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
    }
}
