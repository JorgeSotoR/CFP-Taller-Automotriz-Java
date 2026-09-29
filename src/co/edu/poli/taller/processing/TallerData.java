package co.edu.poli.taller.processing;

import co.edu.poli.taller.model.Mechanic;
import co.edu.poli.taller.model.SparePart;
import co.edu.poli.taller.model.WorkOrder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Datos del taller ya cargados en memoria y validados: catalogo de
 * repuestos, padron de mecanicos, ordenes de trabajo y la lista de errores
 * encontrados durante la lectura.
 *
 * @author Jorge Adrian Soto Reyes
 * @version 1.0
 */
public class TallerData {

    /** Repuestos indexados por ID, en el orden en que se leyeron. */
    private final Map<String, SparePart> spareParts = new LinkedHashMap<String, SparePart>();

    /** Mecanicos indexados por numero de documento. */
    private final Map<Long, Mechanic> mechanics = new LinkedHashMap<Long, Mechanic>();

    /** Ordenes de trabajo validas. */
    private final List<WorkOrder> orders = new ArrayList<WorkOrder>();

    /** Descripcion de cada linea o archivo descartado. */
    private final List<String> errors = new ArrayList<String>();

    /**
     * Crea un contenedor vacio. Solo {@link DataLoader} lo llena.
     */
    TallerData() {
    }

    /**
     * Devuelve el mapa de repuestos por ID (solo lectura).
     *
     * @return el mapa de repuestos por ID (solo lectura)
     */
    public Map<String, SparePart> getSpareParts() {
        return Collections.unmodifiableMap(spareParts);
    }

    /**
     * Devuelve el mapa de mecanicos por documento.
     *
     * @return el mapa de mecanicos por documento
     */
    public Map<Long, Mechanic> getMechanics() {
        return Collections.unmodifiableMap(mechanics);
    }

    /**
     * Devuelve las ordenes de trabajo validas.
     *
     * @return las ordenes de trabajo validas
     */
    public List<WorkOrder> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    /**
     * Devuelve los errores encontrados durante la carga.
     *
     * @return los errores encontrados durante la carga
     */
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Registra un repuesto.
     *
     * @param sparePart repuesto valido
     */
    void addSparePart(SparePart sparePart) {
        spareParts.put(sparePart.getId(), sparePart);
    }

    /**
     * Registra un mecanico.
     *
     * @param mechanic mecanico valido
     */
    void addMechanic(Mechanic mechanic) {
        mechanics.put(mechanic.getDocumentNumber(), mechanic);
    }

    /**
     * Registra una orden de trabajo.
     *
     * @param order orden valida
     */
    void addOrder(WorkOrder order) {
        orders.add(order);
    }

    /**
     * Registra un error de datos.
     *
     * @param error descripcion del error con archivo y numero de linea
     */
    void addError(String error) {
        errors.add(error);
    }
}
