package co.edu.poli.taller.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Orden de trabajo leida de un archivo {@code orden_*.txt}. Equivale al
 * archivo de ventas de un vendedor en el enunciado oficial. Solo contiene
 * lineas que ya pasaron la validacion.
 *
 * @author Jorge Adrian Soto Reyes
 * @version 1.0
 */
public class WorkOrder {

    /** Nombre del archivo de donde se leyo la orden. */
    private final String fileName;

    /** Documento del mecanico responsable de la orden. */
    private final long mechanicDocument;

    /** Lineas validas de la orden. */
    private final List<OrderLine> lines = new ArrayList<OrderLine>();

    /**
     * Crea una orden sin lineas.
     *
     * @param fileName         nombre del archivo de origen
     * @param mechanicDocument documento del mecanico responsable
     */
    public WorkOrder(String fileName, long mechanicDocument) {
        this.fileName = fileName;
        this.mechanicDocument = mechanicDocument;
    }

    /**
     * Agrega una linea valida a la orden.
     *
     * @param line linea a agregar
     */
    public void addLine(OrderLine line) {
        lines.add(line);
    }

    /**
     * Devuelve el nombre del archivo de origen.
     *
     * @return el nombre del archivo de origen
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Devuelve el documento del mecanico responsable.
     *
     * @return el documento del mecanico responsable
     */
    public long getMechanicDocument() {
        return mechanicDocument;
    }

    /**
     * Devuelve las lineas validas de la orden (solo lectura).
     *
     * @return las lineas validas de la orden (solo lectura)
     */
    public List<OrderLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    /**
     * Una linea de la orden: un repuesto y la cantidad usada.
     */
    public static class OrderLine {

        /** Identificador del repuesto usado. */
        private final String sparePartId;

        /** Cantidad de unidades usadas (mayor que cero). */
        private final int quantity;

        /**
         * Crea una linea de orden.
         *
         * @param sparePartId identificador del repuesto
         * @param quantity    cantidad usada
         */
        public OrderLine(String sparePartId, int quantity) {
            this.sparePartId = sparePartId;
            this.quantity = quantity;
        }

        /**
         * Devuelve el identificador del repuesto.
         *
         * @return el identificador del repuesto
         */
        public String getSparePartId() {
            return sparePartId;
        }

        /**
         * Devuelve la cantidad usada.
         *
         * @return la cantidad usada
         */
        public int getQuantity() {
            return quantity;
        }
    }
}
