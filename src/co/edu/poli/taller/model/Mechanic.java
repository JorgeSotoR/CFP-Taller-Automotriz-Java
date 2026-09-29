package co.edu.poli.taller.model;

/**
 * Mecanico del padron ({@code mecanicos.csv}). Equivale al "vendedor" del
 * enunciado oficial.
 *
 * @author Jorge Adrian Soto Reyes
 * @version 1.0
 */
public class Mechanic {

    /** Tipo de documento, por ejemplo {@code CC}. */
    private final String documentType;

    /** Numero de documento, unico por mecanico. */
    private final long documentNumber;

    /** Nombres del mecanico. */
    private final String firstNames;

    /** Apellidos del mecanico. */
    private final String lastNames;

    /**
     * Crea un mecanico.
     *
     * @param documentType   tipo de documento
     * @param documentNumber numero de documento
     * @param firstNames     nombres
     * @param lastNames      apellidos
     */
    public Mechanic(String documentType, long documentNumber, String firstNames, String lastNames) {
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstNames = firstNames;
        this.lastNames = lastNames;
    }

    /**
     * Devuelve el tipo de documento.
     *
     * @return el tipo de documento
     */
    public String getDocumentType() {
        return documentType;
    }

    /**
     * Devuelve el numero de documento.
     *
     * @return el numero de documento
     */
    public long getDocumentNumber() {
        return documentNumber;
    }

    /**
     * Devuelve los nombres del mecanico.
     *
     * @return los nombres del mecanico
     */
    public String getFirstNames() {
        return firstNames;
    }

    /**
     * Devuelve los apellidos del mecanico.
     *
     * @return los apellidos del mecanico
     */
    public String getLastNames() {
        return lastNames;
    }

    /**
     * Devuelve nombres y apellidos separados por un espacio.
     *
     * @return nombres y apellidos separados por un espacio
     */
    public String getFullName() {
        return firstNames + " " + lastNames;
    }
}
