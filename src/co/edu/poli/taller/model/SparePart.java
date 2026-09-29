package co.edu.poli.taller.model;

/**
 * Repuesto del catalogo maestro ({@code repuestos.csv}). Equivale al
 * "producto" del enunciado oficial.
 *
 * @author Jorge Adrian Soto Reyes
 * @version 1.0
 */
public class SparePart {

    /** Identificador unico del repuesto, por ejemplo {@code R001}. */
    private final String id;

    /** Nombre comercial del repuesto. */
    private final String name;

    /** Costo por unidad en pesos colombianos. */
    private final long unitPrice;

    /** Unidades disponibles en bodega. */
    private final int stock;

    /**
     * Crea un repuesto.
     *
     * @param id        identificador unico
     * @param name      nombre del repuesto
     * @param unitPrice costo por unidad (no negativo)
     * @param stock     unidades disponibles (no negativo)
     */
    public SparePart(String id, String name, long unitPrice, int stock) {
        this.id = id;
        this.name = name;
        this.unitPrice = unitPrice;
        this.stock = stock;
    }

    /**
     * Devuelve el identificador del repuesto.
     *
     * @return el identificador del repuesto
     */
    public String getId() {
        return id;
    }

    /**
     * Devuelve el nombre del repuesto.
     *
     * @return el nombre del repuesto
     */
    public String getName() {
        return name;
    }

    /**
     * Devuelve el costo por unidad.
     *
     * @return el costo por unidad
     */
    public long getUnitPrice() {
        return unitPrice;
    }

    /**
     * Devuelve las unidades disponibles en bodega.
     *
     * @return las unidades disponibles en bodega
     */
    public int getStock() {
        return stock;
    }
}
