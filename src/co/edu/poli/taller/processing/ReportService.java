package co.edu.poli.taller.processing;

import co.edu.poli.taller.model.Mechanic;
import co.edu.poli.taller.model.SparePart;
import co.edu.poli.taller.model.WorkOrder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cruza las ordenes de trabajo con los catalogos maestros, calcula los
 * totales y escribe los reportes pedidos en los puntos 3 y 4 del enunciado:
 * <ul>
 *   <li>{@code reporte_mecanicos.csv}: mecanicos ordenados por dinero
 *       recaudado, de mayor a menor.</li>
 *   <li>{@code reporte_repuestos.csv}: repuestos usados ordenados por
 *       cantidad, de mayor a menor.</li>
 *   <li>{@code reporte_errores.txt}: lineas o archivos descartados.</li>
 * </ul>
 *
 * @author Jorge Adrian Soto Reyes
 * @version 1.0
 */
public final class ReportService {

    /** Nombre del reporte de mecanicos. */
    public static final String MECHANICS_REPORT = "reporte_mecanicos.csv";

    /** Nombre del reporte de repuestos. */
    public static final String SPARE_PARTS_REPORT = "reporte_repuestos.csv";

    /** Nombre del reporte de errores de datos. */
    public static final String ERRORS_REPORT = "reporte_errores.txt";

    /** Separador de campos de los reportes. */
    private static final String SEPARATOR = ";";

    /** Clase utilitaria: no se instancia. */
    private ReportService() {
    }

    /**
     * Calcula el dinero recaudado por cada mecanico (suma de cantidad por
     * costo unitario de los repuestos de sus ordenes). Incluye a los
     * mecanicos sin ordenes, con total cero.
     *
     * @param data datos validados del taller
     * @return mecanicos ordenados por total recaudado, de mayor a menor
     */
    public static List<MechanicTotal> buildMechanicsReport(TallerData data) {
        Map<Long, MechanicTotal> totals = new LinkedHashMap<Long, MechanicTotal>();
        for (Mechanic mechanic : data.getMechanics().values()) {
            totals.put(mechanic.getDocumentNumber(), new MechanicTotal(mechanic));
        }
        for (WorkOrder order : data.getOrders()) {
            MechanicTotal total = totals.get(order.getMechanicDocument());
            total.orders++;
            for (WorkOrder.OrderLine line : order.getLines()) {
                SparePart part = data.getSpareParts().get(line.getSparePartId());
                total.amount += part.getUnitPrice() * line.getQuantity();
            }
        }
        List<MechanicTotal> report = new ArrayList<MechanicTotal>(totals.values());
        Collections.sort(report, new Comparator<MechanicTotal>() {
            @Override
            public int compare(MechanicTotal first, MechanicTotal second) {
                return Long.compare(second.amount, first.amount);
            }
        });
        return report;
    }

    /**
     * Calcula la cantidad usada de cada repuesto en todas las ordenes.
     * Solo incluye los repuestos que se usaron al menos una vez.
     *
     * @param data datos validados del taller
     * @return repuestos ordenados por cantidad usada, de mayor a menor
     */
    public static List<SparePartUsage> buildSparePartsReport(TallerData data) {
        Map<String, SparePartUsage> usages = new LinkedHashMap<String, SparePartUsage>();
        for (WorkOrder order : data.getOrders()) {
            for (WorkOrder.OrderLine line : order.getLines()) {
                SparePartUsage usage = usages.get(line.getSparePartId());
                if (usage == null) {
                    usage = new SparePartUsage(data.getSpareParts().get(line.getSparePartId()));
                    usages.put(line.getSparePartId(), usage);
                }
                usage.quantity += line.getQuantity();
            }
        }
        List<SparePartUsage> report = new ArrayList<SparePartUsage>(usages.values());
        Collections.sort(report, new Comparator<SparePartUsage>() {
            @Override
            public int compare(SparePartUsage first, SparePartUsage second) {
                return Long.compare(second.quantity, first.quantity);
            }
        });
        return report;
    }

    /**
     * Escribe los tres reportes en la carpeta indicada, creandola si no existe.
     * Formatos:
     * <ul>
     *   <li>Mecanicos: {@code Nombres Apellidos;TotalRecaudado}</li>
     *   <li>Repuestos: {@code NombreRepuesto;CostoUnitario;CantidadUsada}</li>
     * </ul>
     *
     * @param outputDirectory carpeta de salida
     * @param mechanics       reporte de mecanicos ya ordenado
     * @param spareParts      reporte de repuestos ya ordenado
     * @param errors          errores de datos encontrados en la carga
     * @throws IOException si la carpeta o algun archivo no se puede escribir
     */
    public static void writeReports(File outputDirectory, List<MechanicTotal> mechanics,
            List<SparePartUsage> spareParts, List<String> errors) throws IOException {
        if (!outputDirectory.isDirectory() && !outputDirectory.mkdirs()) {
            throw new IOException("No fue posible crear la carpeta de reportes: " + outputDirectory.getPath());
        }
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(new File(outputDirectory, MECHANICS_REPORT)))) {
            for (MechanicTotal total : mechanics) {
                writer.write(total.mechanic.getFullName() + SEPARATOR + total.amount);
                writer.newLine();
            }
        }
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(new File(outputDirectory, SPARE_PARTS_REPORT)))) {
            for (SparePartUsage usage : spareParts) {
                writer.write(usage.sparePart.getName() + SEPARATOR + usage.sparePart.getUnitPrice()
                        + SEPARATOR + usage.quantity);
                writer.newLine();
            }
        }
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(new File(outputDirectory, ERRORS_REPORT)))) {
            if (errors.isEmpty()) {
                writer.write("Sin errores: todos los datos fueron validos.");
                writer.newLine();
            }
            for (String error : errors) {
                writer.write(error);
                writer.newLine();
            }
        }
    }

    /**
     * Total recaudado por un mecanico.
     */
    public static final class MechanicTotal {

        /** Mecanico al que corresponde el total. */
        private final Mechanic mechanic;

        /** Dinero recaudado en pesos. */
        private long amount;

        /** Cantidad de ordenes validas del mecanico. */
        private int orders;

        /**
         * Crea un total en cero para el mecanico dado.
         *
         * @param mechanic mecanico
         */
        private MechanicTotal(Mechanic mechanic) {
            this.mechanic = mechanic;
        }

        /**
         * Devuelve el mecanico.
         *
         * @return el mecanico
         */
        public Mechanic getMechanic() {
            return mechanic;
        }

        /**
         * Devuelve el dinero recaudado.
         *
         * @return el dinero recaudado
         */
        public long getAmount() {
            return amount;
        }

        /**
         * Devuelve la cantidad de ordenes validas.
         *
         * @return la cantidad de ordenes validas
         */
        public int getOrders() {
            return orders;
        }
    }

    /**
     * Cantidad usada de un repuesto.
     */
    public static final class SparePartUsage {

        /** Repuesto al que corresponde la cantidad. */
        private final SparePart sparePart;

        /** Unidades usadas en todas las ordenes. */
        private long quantity;

        /**
         * Crea un uso en cero para el repuesto dado.
         *
         * @param sparePart repuesto
         */
        private SparePartUsage(SparePart sparePart) {
            this.sparePart = sparePart;
        }

        /**
         * Devuelve el repuesto.
         *
         * @return el repuesto
         */
        public SparePart getSparePart() {
            return sparePart;
        }

        /**
         * Devuelve las unidades usadas.
         *
         * @return las unidades usadas
         */
        public long getQuantity() {
            return quantity;
        }
    }
}
