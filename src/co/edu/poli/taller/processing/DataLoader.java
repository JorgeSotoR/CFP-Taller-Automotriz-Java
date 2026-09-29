package co.edu.poli.taller.processing;

import co.edu.poli.taller.model.Mechanic;
import co.edu.poli.taller.model.SparePart;
import co.edu.poli.taller.model.WorkOrder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

/**
 * Lee los archivos planos del taller y los carga en memoria validando su
 * formato y coherencia. Las lineas con errores no detienen el programa: se
 * descartan y se registran en {@link TallerData#getErrors()}.
 *
 * <p>Reglas de validacion:</p>
 * <ul>
 *   <li>Cantidad de campos correcta en cada archivo.</li>
 *   <li>Precios, stock y documentos numericos y no negativos.</li>
 *   <li>IDs de repuesto y documentos de mecanico sin duplicados.</li>
 *   <li>Cada orden pertenece a un mecanico existente; si no, se descarta completa.</li>
 *   <li>Cada linea de orden usa un repuesto existente y una cantidad mayor que cero.</li>
 * </ul>
 *
 * @author Jorge Adrian Soto Reyes
 * @version 1.0
 */
public final class DataLoader {

    /** Nombre del archivo de catalogo de repuestos. */
    public static final String SPARE_PARTS_FILE = "repuestos.csv";

    /** Nombre del archivo de padron de mecanicos. */
    public static final String MECHANICS_FILE = "mecanicos.csv";

    /** Prefijo de los archivos de ordenes de trabajo. */
    public static final String ORDER_PREFIX = "orden_";

    /** Extension de los archivos de ordenes de trabajo. */
    public static final String ORDER_EXTENSION = ".txt";

    /** Separador de campos. */
    private static final String SEPARATOR = ";";

    /** Clase utilitaria: no se instancia. */
    private DataLoader() {
    }

    /**
     * Carga y valida todos los archivos de la carpeta indicada.
     *
     * @param dataDirectory carpeta que contiene repuestos, mecanicos y ordenes
     * @return los datos validos y la lista de errores encontrados
     * @throws IOException si la carpeta o un archivo maestro no existe o no se puede leer
     */
    public static TallerData load(File dataDirectory) throws IOException {
        if (!dataDirectory.isDirectory()) {
            throw new IOException("No existe la carpeta de datos: " + dataDirectory.getPath()
                    + ". Ejecute primero GenerateInfoFiles.");
        }
        TallerData data = new TallerData();
        loadSpareParts(new File(dataDirectory, SPARE_PARTS_FILE), data);
        loadMechanics(new File(dataDirectory, MECHANICS_FILE), data);

        File[] orderFiles = dataDirectory.listFiles();
        if (orderFiles != null) {
            Arrays.sort(orderFiles);
            for (File file : orderFiles) {
                if (isOrderFile(file)) {
                    loadOrder(file, data);
                }
            }
        }
        if (data.getOrders().isEmpty()) {
            data.addError("No se encontraron ordenes de trabajo validas en " + dataDirectory.getPath());
        }
        return data;
    }

    /**
     * Indica si un archivo corresponde a una orden de trabajo.
     *
     * @param file archivo a revisar
     * @return {@code true} si su nombre es {@code orden_*.txt}
     */
    private static boolean isOrderFile(File file) {
        String name = file.getName();
        return file.isFile() && name.startsWith(ORDER_PREFIX) && name.endsWith(ORDER_EXTENSION);
    }

    /**
     * Lee el catalogo de repuestos. Formato:
     * {@code IDRepuesto;NombreRepuesto;CostoUnitario[;StockActual]}.
     *
     * @param file archivo de repuestos
     * @param data destino de los repuestos validos y de los errores
     * @throws IOException si el archivo no existe o no se puede leer
     */
    private static void loadSpareParts(File file, TallerData data) throws IOException {
        requireFile(file);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                String location = file.getName() + ":" + lineNumber;
                String[] fields = splitFields(line);
                if (fields.length < 3 || fields.length > 4) {
                    data.addError(location + " se esperaban 3 o 4 campos -> " + line);
                    continue;
                }
                String id = fields[0];
                String name = fields[1];
                Long price = parseNonNegativeLong(fields[2]);
                Long stock = fields.length == 4 ? parseNonNegativeLong(fields[3]) : Long.valueOf(0);

                if (id.isEmpty() || name.isEmpty()) {
                    data.addError(location + " ID o nombre vacio -> " + line);
                } else if (price == null) {
                    data.addError(location + " precio invalido o negativo -> " + line);
                } else if (stock == null || stock > Integer.MAX_VALUE) {
                    data.addError(location + " stock invalido o negativo -> " + line);
                } else if (data.getSpareParts().containsKey(id)) {
                    data.addError(location + " ID de repuesto duplicado -> " + line);
                } else {
                    data.addSparePart(new SparePart(id, name, price, stock.intValue()));
                }
            }
        }
    }

    /**
     * Lee el padron de mecanicos. Formato:
     * {@code TipoDocumento;NumeroDocumento;Nombres;Apellidos}.
     *
     * @param file archivo de mecanicos
     * @param data destino de los mecanicos validos y de los errores
     * @throws IOException si el archivo no existe o no se puede leer
     */
    private static void loadMechanics(File file, TallerData data) throws IOException {
        requireFile(file);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                String location = file.getName() + ":" + lineNumber;
                String[] fields = splitFields(line);
                if (fields.length != 4) {
                    data.addError(location + " se esperaban 4 campos -> " + line);
                    continue;
                }
                Long document = parseNonNegativeLong(fields[1]);
                if (fields[0].isEmpty() || fields[2].isEmpty() || fields[3].isEmpty()) {
                    data.addError(location + " tipo de documento, nombres o apellidos vacios -> " + line);
                } else if (document == null || document == 0) {
                    data.addError(location + " numero de documento invalido -> " + line);
                } else if (data.getMechanics().containsKey(document)) {
                    data.addError(location + " documento de mecanico duplicado -> " + line);
                } else {
                    data.addMechanic(new Mechanic(fields[0], document, fields[2], fields[3]));
                }
            }
        }
    }

    /**
     * Lee una orden de trabajo. La primera linea identifica al mecanico
     * ({@code TipoDocumento;NumeroDocumento}); las siguientes tienen el formato
     * {@code IDRepuesto;Cantidad;}. Si el mecanico no existe, la orden se
     * descarta completa; si una linea es invalida, solo se descarta esa linea.
     *
     * @param file archivo de la orden
     * @param data destino de la orden valida y de los errores
     * @throws IOException si el archivo no se puede leer
     */
    private static void loadOrder(File file, TallerData data) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();
            while (header != null && header.trim().isEmpty()) {
                header = reader.readLine();
            }
            if (header == null) {
                data.addError(file.getName() + " archivo vacio, orden descartada");
                return;
            }
            String[] headerFields = splitFields(header);
            Long document = headerFields.length == 2 ? parseNonNegativeLong(headerFields[1]) : null;
            if (document == null) {
                data.addError(file.getName() + ":1 cabecera invalida, orden descartada -> " + header);
                return;
            }
            Mechanic mechanic = data.getMechanics().get(document);
            if (mechanic == null) {
                data.addError(file.getName() + ":1 mecanico " + document
                        + " no existe en " + MECHANICS_FILE + ", orden descartada");
                return;
            }
            if (!mechanic.getDocumentType().equals(headerFields[0])) {
                data.addError(file.getName() + ":1 tipo de documento no coincide con " + MECHANICS_FILE
                        + ", orden descartada -> " + header);
                return;
            }

            WorkOrder order = new WorkOrder(file.getName(), document);
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                String location = file.getName() + ":" + lineNumber;
                String[] fields = splitFields(line);
                if (fields.length != 2) {
                    data.addError(location + " se esperaban IDRepuesto;Cantidad; -> " + line);
                    continue;
                }
                Long quantity = parseNonNegativeLong(fields[1]);
                if (!data.getSpareParts().containsKey(fields[0])) {
                    data.addError(location + " repuesto " + fields[0] + " no existe en el catalogo -> " + line);
                } else if (quantity == null || quantity == 0 || quantity > Integer.MAX_VALUE) {
                    data.addError(location + " cantidad invalida, negativa o cero -> " + line);
                } else {
                    order.addLine(new WorkOrder.OrderLine(fields[0], quantity.intValue()));
                }
            }
            data.addOrder(order);
        }
    }

    /**
     * Separa una linea por punto y coma, recorta espacios y descarta el
     * campo vacio final que deja un {@code ;} al terminar la linea.
     *
     * @param line linea a separar
     * @return los campos de la linea
     */
    private static String[] splitFields(String line) {
        String[] fields = line.split(SEPARATOR);
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }
        return fields;
    }

    /**
     * Convierte un texto a numero entero no negativo.
     *
     * @param text texto a convertir
     * @return el numero, o {@code null} si no es un entero valido o es negativo
     */
    private static Long parseNonNegativeLong(String text) {
        try {
            long value = Long.parseLong(text);
            return value >= 0 ? Long.valueOf(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Verifica que un archivo maestro exista.
     *
     * @param file archivo requerido
     * @throws IOException si el archivo no existe
     */
    private static void requireFile(File file) throws IOException {
        if (!file.isFile()) {
            throw new IOException("No existe el archivo requerido: " + file.getPath()
                    + ". Ejecute primero GenerateInfoFiles.");
        }
    }
}
