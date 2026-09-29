package co.edu.poli.taller.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Genera los archivos planos pseudoaleatorios que sirven como entrada al
 * sistema de clasificacion y rotacion de repuestos del Taller Automotriz.
 *
 * <p>Equivalencias con el enunciado oficial del proyecto:</p>
 * <ul>
 *   <li>Productos  = repuestos ({@code repuestos.csv}).</li>
 *   <li>Vendedores = mecanicos ({@code mecanicos.csv}).</li>
 *   <li>Archivos de ventas = ordenes de trabajo ({@code orden_*.txt}).</li>
 * </ul>
 *
 * <p>Todas las ordenes se asignan a mecanicos que existen en
 * {@code mecanicos.csv} y solo usan repuestos que existen en
 * {@code repuestos.csv}, de modo que los cruces de la Entrega 2 son
 * coherentes. El programa no solicita informacion al usuario.</p>
 *
 * @author Jorge Adrian Soto Reyes
 * @version 2.0
 */
public class GenerateInfoFiles {

    /** Carpeta relativa al proyecto donde se escriben los archivos. */
    private static final String DIRECTORY_PATH = "datos_taller";

    /** Separador de campos usado en todos los archivos. */
    private static final String SEPARATOR = ";";

    /** Tipo de documento asignado a los mecanicos. */
    private static final String DOCUMENT_TYPE = "CC";

    /** Cantidad de repuestos a generar en el catalogo. */
    private static final int TOTAL_PRODUCTS = 12;

    /** Cantidad de mecanicos a generar. */
    private static final int TOTAL_SALESMEN = 6;

    /** Cantidad de ordenes de trabajo a generar. */
    private static final int TOTAL_ORDERS = 10;

    /** Minimo de lineas (repuestos) por orden de trabajo. */
    private static final int MIN_LINES_PER_ORDER = 2;

    /** Maximo de lineas (repuestos) por orden de trabajo. */
    private static final int MAX_LINES_PER_ORDER = 5;

    /** Nombres reales usados para generar mecanicos coherentes. */
    private static final String[] FIRST_NAMES = {
        "Carlos", "Andres", "Mateo", "Felipe", "Alejandro",
        "David", "Sofia", "Mariana", "Juan", "Esteban"
    };

    /** Apellidos reales usados para generar mecanicos coherentes. */
    private static final String[] LAST_NAMES = {
        "Zapata", "Restrepo", "Gomez", "Mejia", "Henao",
        "Castano", "Perez", "Jaramillo", "Ramirez", "Ochoa"
    };

    /** Nombres de repuestos reales para el catalogo. */
    private static final String[] SPARE_PARTS = {
        "Filtro de Aceite Sintetico", "Pastillas de Freno Delanteras",
        "Bujia de Iridio", "Amortiguador Delantero Gas",
        "Kit Correa Distribucion", "Liquido de Frenos DOT 4",
        "Filtro de Aire Alto Flujo", "Bateria 12V 65Ah",
        "Bomba de Agua Termica", "Correa de Accesorios Alternador",
        "Disco de Freno Ventilado", "Rotula Direccion Externa"
    };

    /** Generador pseudoaleatorio unico compartido por toda la clase. */
    private static final Random RANDOM = new Random();

    /** Cantidad de repuestos escritos en el ultimo catalogo generado. */
    private static int generatedProductsCount = 0;

    /** Contador usado para numerar las ordenes de trabajo generadas. */
    private static int orderCounter = 0;

    /** Mecanicos escritos en el ultimo {@code mecanicos.csv} generado. */
    private static final List<Mechanic> generatedMechanics = new ArrayList<Mechanic>();

    /**
     * Datos minimos de un mecanico generado, usados para que las ordenes
     * referencien solo mecanicos existentes.
     */
    private static final class Mechanic {

        /** Nombre completo del mecanico. */
        private final String fullName;

        /** Numero de documento del mecanico. */
        private final long documentNumber;

        /**
         * Crea un mecanico.
         *
         * @param fullName       nombre y apellido del mecanico
         * @param documentNumber numero de documento unico
         */
        private Mechanic(String fullName, long documentNumber) {
            this.fullName = fullName;
            this.documentNumber = documentNumber;
        }
    }

    /** Clase utilitaria con metodos estaticos: no se instancia. */
    private GenerateInfoFiles() {
    }

    /**
     * Punto de entrada. Genera el catalogo de repuestos, el padron de
     * mecanicos y las ordenes de trabajo, e informa por consola si el
     * proceso termino con exito o con error.
     *
     * @param args no se usan
     */
    public static void main(String[] args) {
        try {
            generateAll();
            System.out.println("Finalizacion exitosa: archivos generados en ./" + DIRECTORY_PATH + "/");
        } catch (IOException | RuntimeException e) {
            System.err.println("Error: no fue posible generar los archivos. Detalle: " + e.getMessage());
        }
    }

    /**
     * Genera el lote completo de archivos de prueba: catalogo de repuestos,
     * padron de mecanicos y ordenes de trabajo. Antes de generar, elimina
     * las ordenes de una ejecucion anterior para que no queden ordenes de
     * mecanicos que ya no existen.
     *
     * @throws IOException si algun archivo no se puede escribir o borrar
     */
    public static void generateAll() throws IOException {
        prepareDirectory();
        deletePreviousOrders();
        orderCounter = 0;

        createProductsFile(TOTAL_PRODUCTS);
        createSalesManInfoFile(TOTAL_SALESMEN);

        for (int i = 0; i < TOTAL_ORDERS; i++) {
            Mechanic mechanic = generatedMechanics.get(RANDOM.nextInt(generatedMechanics.size()));
            int linesCount = MIN_LINES_PER_ORDER
                    + RANDOM.nextInt(MAX_LINES_PER_ORDER - MIN_LINES_PER_ORDER + 1);
            createSalesMenFile(linesCount, mechanic.fullName, mechanic.documentNumber);
        }
    }

    /**
     * Devuelve la carpeta donde se escriben y leen los archivos de datos.
     *
     * @return ruta relativa de la carpeta de datos
     */
    public static String getDirectoryPath() {
        return DIRECTORY_PATH;
    }

    /**
     * Elimina los archivos {@code orden_*.txt} de una generacion anterior.
     *
     * @throws IOException si un archivo existente no se puede borrar
     */
    private static void deletePreviousOrders() throws IOException {
        File[] oldOrders = new File(DIRECTORY_PATH).listFiles();
        if (oldOrders == null) {
            return;
        }
        for (File file : oldOrders) {
            if (file.isFile() && file.getName().startsWith("orden_") && file.getName().endsWith(".txt")
                    && !file.delete()) {
                throw new IOException("No fue posible borrar la orden anterior: " + file.getName());
            }
        }
    }

    /**
     * Crea el archivo {@code repuestos.csv} con informacion pseudoaleatoria
     * de repuestos. Formato por linea:
     * {@code IDRepuesto;NombreRepuesto;CostoUnitario;StockActual}.
     *
     * @param productsCount cantidad de repuestos a generar (mayor que cero)
     * @throws IOException              si el archivo no se puede escribir
     * @throws IllegalArgumentException si {@code productsCount} no es positivo
     */
    public static void createProductsFile(int productsCount) throws IOException {
        if (productsCount <= 0) {
            throw new IllegalArgumentException("La cantidad de repuestos debe ser mayor que cero.");
        }
        prepareDirectory();
        File targetFile = new File(DIRECTORY_PATH, "repuestos.csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {
            for (int i = 1; i <= productsCount; i++) {
                String id = buildProductId(i);
                String name = (i <= SPARE_PARTS.length) ? SPARE_PARTS[i - 1] : "Repuesto Estandar " + i;
                long unitPrice = 20000L + RANDOM.nextInt(40) * 5000L;
                int stock = 15 + RANDOM.nextInt(85);

                writer.write(id + SEPARATOR + name + SEPARATOR + unitPrice + SEPARATOR + stock);
                writer.newLine();
            }
        }
        generatedProductsCount = productsCount;
    }

    /**
     * Crea el archivo {@code mecanicos.csv} con informacion pseudoaleatoria y
     * coherente de mecanicos, sin repetir numeros de documento ni nombres
     * completos (asi los reportes no muestran dos mecanicos con el mismo
     * nombre). Formato por linea:
     * {@code TipoDocumento;NumeroDocumento;Nombres;Apellidos}.
     * Los mecanicos quedan registrados para asignarles ordenes de trabajo.
     *
     * @param salesmanCount cantidad de mecanicos a generar, entre 1 y el
     *                      numero de combinaciones posibles de nombre y apellido
     * @throws IOException              si el archivo no se puede escribir
     * @throws IllegalArgumentException si {@code salesmanCount} esta fuera de rango
     */
    public static void createSalesManInfoFile(int salesmanCount) throws IOException {
        int maxNames = FIRST_NAMES.length * LAST_NAMES.length;
        if (salesmanCount <= 0 || salesmanCount > maxNames) {
            throw new IllegalArgumentException("La cantidad de mecanicos debe estar entre 1 y " + maxNames + ".");
        }
        prepareDirectory();
        File targetFile = new File(DIRECTORY_PATH, "mecanicos.csv");
        Set<Long> usedDocuments = new HashSet<Long>();
        Set<String> usedNames = new HashSet<String>();
        generatedMechanics.clear();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {
            for (int i = 0; i < salesmanCount; i++) {
                long documentNumber;
                do {
                    documentNumber = 10000000L + RANDOM.nextInt(90000000);
                } while (!usedDocuments.add(documentNumber));

                String firstName;
                String lastName;
                do {
                    firstName = FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)];
                    lastName = LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)];
                } while (!usedNames.add(firstName + " " + lastName));

                writer.write(DOCUMENT_TYPE + SEPARATOR + documentNumber
                        + SEPARATOR + firstName + SEPARATOR + lastName);
                writer.newLine();
                generatedMechanics.add(new Mechanic(firstName + " " + lastName, documentNumber));
            }
        }
    }

    /**
     * Crea un archivo pseudoaleatorio de orden de trabajo (equivalente al
     * archivo de ventas de un vendedor) para el mecanico con el nombre y el
     * documento dados. Un mismo mecanico puede tener varias ordenes. El
     * archivo se llama {@code orden_NNN_<documento>_<nombre>.txt} y su
     * formato es:
     * <pre>
     * TipoDocumento;NumeroDocumento
     * IDRepuesto;Cantidad;
     * IDRepuesto;Cantidad;
     * </pre>
     * Debe llamarse despues de {@link #createProductsFile(int)} para que los
     * IDs de repuesto existan en el catalogo.
     *
     * @param randomSalesCount cantidad de lineas de repuestos (mayor que cero)
     * @param name             nombre del mecanico, usado en el nombre del archivo
     * @param id               numero de documento del mecanico
     * @throws IOException              si el archivo no se puede escribir
     * @throws IllegalArgumentException si {@code randomSalesCount} no es positivo
     * @throws IllegalStateException    si aun no se ha generado el catalogo
     */
    public static void createSalesMenFile(int randomSalesCount, String name, long id) throws IOException {
        if (randomSalesCount <= 0) {
            throw new IllegalArgumentException("La cantidad de lineas de la orden debe ser mayor que cero.");
        }
        if (generatedProductsCount <= 0) {
            throw new IllegalStateException("Primero se debe generar el catalogo con createProductsFile.");
        }
        prepareDirectory();
        orderCounter++;
        String safeName = name.trim().replaceAll("\\s+", "_");
        String fileName = "orden_" + String.format("%03d", orderCounter) + "_" + id + "_" + safeName + ".txt";
        File targetFile = new File(DIRECTORY_PATH, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {
            writer.write(DOCUMENT_TYPE + SEPARATOR + id);
            writer.newLine();

            for (int i = 0; i < randomSalesCount; i++) {
                String productId = buildProductId(1 + RANDOM.nextInt(generatedProductsCount));
                int quantity = 1 + RANDOM.nextInt(4);

                writer.write(productId + SEPARATOR + quantity + SEPARATOR);
                writer.newLine();
            }
        }
    }

    /**
     * Construye el ID de un repuesto con formato {@code R001}.
     *
     * @param index numero consecutivo del repuesto (desde 1)
     * @return el ID formateado
     */
    private static String buildProductId(int index) {
        return "R" + String.format("%03d", index);
    }

    /**
     * Crea la carpeta de salida si todavia no existe.
     *
     * @throws IOException si la carpeta no existe y no se puede crear
     */
    private static void prepareDirectory() throws IOException {
        File baseDir = new File(DIRECTORY_PATH);
        if (!baseDir.isDirectory() && !baseDir.mkdirs()) {
            throw new IOException("No fue posible crear la carpeta de destino: " + DIRECTORY_PATH);
        }
    }
}
