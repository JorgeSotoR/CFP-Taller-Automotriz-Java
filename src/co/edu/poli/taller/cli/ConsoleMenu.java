package co.edu.poli.taller.cli;

import co.edu.poli.taller.generator.GenerateInfoFiles;
import co.edu.poli.taller.model.Mechanic;
import co.edu.poli.taller.model.SparePart;
import co.edu.poli.taller.model.WorkOrder;
import co.edu.poli.taller.processing.DataLoader;
import co.edu.poli.taller.processing.ReportService;
import co.edu.poli.taller.processing.ReportService.MechanicTotal;
import co.edu.poli.taller.processing.ReportService.SparePartUsage;
import co.edu.poli.taller.processing.TallerData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

/**
 * Menu interactivo de consola (CLI) del taller. Es la interfaz por defecto de
 * la clase {@code main}, siguiendo la propuesta de la idea 10 (Taller
 * Automotriz) del documento de ideas del curso.
 *
 * <pre>
 * [1] Generar Archivos
 * [2] Importar/Ver Base
 * [3] Importar/Ver Salida
 * [4] Descargar Salida
 * [5] Buscar y Filtrar
 * [6] Salir
 * </pre>
 *
 * @author Jorge Adrian Soto Reyes
 * @version 2.0
 */
public class ConsoleMenu {

    /** Opcion para generar archivos de prueba. */
    private static final String OPTION_GENERATE = "1";

    /** Opcion para cargar y ver los archivos base. */
    private static final String OPTION_VIEW_BASE = "2";

    /** Opcion para procesar y ver los reportes. */
    private static final String OPTION_VIEW_OUTPUT = "3";

    /** Opcion para escribir los reportes en disco. */
    private static final String OPTION_DOWNLOAD = "4";

    /** Opcion para buscar y filtrar informacion. */
    private static final String OPTION_SEARCH = "5";

    /** Opcion para salir. */
    private static final String OPTION_EXIT = "6";

    /** Opcion del submenu para buscar un mecanico. */
    private static final String SEARCH_MECHANIC = "1";

    /** Opcion del submenu para buscar un repuesto. */
    private static final String SEARCH_SPARE_PART = "2";

    /** Opcion del submenu para ver repuestos con riesgo de desabastecimiento. */
    private static final String SEARCH_LOW_STOCK = "3";

    /** Opcion del submenu para volver al menu principal. */
    private static final String SEARCH_BACK = "0";

    /** Unidades restantes por debajo de las cuales un repuesto se debe reponer. */
    private static final int LOW_STOCK_THRESHOLD = 20;

    /** Linea separadora de la interfaz. */
    private static final String RULE = "==========================================================";

    /** Formato de dinero con separador de miles colombiano. */
    private static final DecimalFormat MONEY_FORMAT = createMoneyFormat();

    /** Carpeta de datos de entrada. */
    private final File dataDirectory;

    /** Carpeta de reportes de salida. */
    private final File reportsDirectory;

    /** Lector de la entrada del usuario. */
    private final Scanner input;

    /** Salida de la interfaz. */
    private final PrintStream out;

    /** Datos cargados; {@code null} si aun no se han importado. */
    private TallerData data;

    /** Reporte de mecanicos calculado; {@code null} si aun no se ha procesado. */
    private List<MechanicTotal> mechanicsReport;

    /** Reporte de repuestos calculado; {@code null} si aun no se ha procesado. */
    private List<SparePartUsage> sparePartsReport;

    /**
     * Crea el menu.
     *
     * @param dataDirectory    carpeta con los archivos de entrada
     * @param reportsDirectory carpeta donde se escriben los reportes
     * @param in               flujo de entrada del usuario
     * @param out              flujo de salida de la interfaz
     */
    public ConsoleMenu(File dataDirectory, File reportsDirectory, InputStream in, PrintStream out) {
        this.dataDirectory = dataDirectory;
        this.reportsDirectory = reportsDirectory;
        this.input = new Scanner(in);
        this.out = out;
    }

    /**
     * Muestra el menu en ciclo hasta que el usuario elija salir o se
     * termine la entrada.
     */
    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            if (!input.hasNextLine()) {
                break;
            }
            String option = input.nextLine().trim();
            try {
                running = handleOption(option);
            } catch (IOException e) {
                out.println("Error: " + e.getMessage());
            }
        }
        out.println("Hasta pronto.");
    }

    /**
     * Ejecuta la opcion elegida.
     *
     * @param option texto ingresado por el usuario
     * @return {@code false} si el usuario eligio salir
     * @throws IOException si falla la lectura o escritura de archivos
     */
    private boolean handleOption(String option) throws IOException {
        if (OPTION_GENERATE.equals(option)) {
            generateFiles();
        } else if (OPTION_VIEW_BASE.equals(option)) {
            importAndShowBase();
        } else if (OPTION_VIEW_OUTPUT.equals(option)) {
            processAndShowOutput();
        } else if (OPTION_DOWNLOAD.equals(option)) {
            downloadOutput();
        } else if (OPTION_SEARCH.equals(option)) {
            searchMenu();
        } else if (OPTION_EXIT.equals(option)) {
            return false;
        } else {
            out.println("Opcion no valida. Escriba un numero del 1 al 6.");
        }
        return true;
    }

    /** Imprime el encabezado y las opciones del menu. */
    private void printMenu() {
        out.println();
        out.println(RULE);
        out.println("  TALLER AUTOMOTRIZ - Rotacion de Repuestos");
        out.println(RULE);
        out.println("[1] Generar Archivos");
        out.println("[2] Importar/Ver Base");
        out.println("[3] Importar/Ver Salida");
        out.println("[4] Descargar Salida");
        out.println("[5] Buscar y Filtrar");
        out.println("[6] Salir");
        out.print("Seleccione una opcion: ");
        out.flush();
    }

    /**
     * Opcion 1: genera un lote nuevo de archivos de prueba y descarta los
     * datos cargados anteriormente.
     *
     * @throws IOException si no se pueden generar los archivos
     */
    private void generateFiles() throws IOException {
        GenerateInfoFiles.generateAll();
        data = null;
        mechanicsReport = null;
        sparePartsReport = null;
        out.println("Archivos generados en ./" + dataDirectory.getPath() + "/");
    }

    /**
     * Opcion 2: carga los archivos base y muestra repuestos, mecanicos,
     * ordenes leidas y errores de datos.
     *
     * @throws IOException si faltan archivos maestros
     */
    private void importAndShowBase() throws IOException {
        loadData();
        out.println();
        out.println("REPUESTOS (" + data.getSpareParts().size() + ")");
        out.println(String.format("%-6s %-34s %14s %6s", "ID", "Nombre", "Costo", "Stock"));
        for (SparePart part : data.getSpareParts().values()) {
            out.println(String.format("%-6s %-34s %14s %6d", part.getId(), part.getName(),
                    formatMoney(part.getUnitPrice()), part.getStock()));
        }
        out.println();
        out.println("MECANICOS (" + data.getMechanics().size() + ")");
        out.println(String.format("%-4s %-12s %-28s", "Tipo", "Documento", "Nombre"));
        for (Mechanic mechanic : data.getMechanics().values()) {
            out.println(String.format("%-4s %-12d %-28s", mechanic.getDocumentType(),
                    mechanic.getDocumentNumber(), mechanic.getFullName()));
        }
        out.println();
        out.println("Ordenes validas leidas: " + data.getOrders().size());
        printErrors();
    }

    /**
     * Opcion 3: procesa los cruces y muestra los dos reportes ordenados.
     *
     * @throws IOException si faltan archivos maestros
     */
    private void processAndShowOutput() throws IOException {
        processReports();
        out.println();
        out.println("MECANICOS POR DINERO RECAUDADO (mayor a menor)");
        out.println(String.format("%-3s %-10s %-24s %8s %16s", "#", "Documento", "Mecanico", "Ordenes",
                "Recaudado"));
        int position = 1;
        for (MechanicTotal total : mechanicsReport) {
            Mechanic mechanic = total.getMechanic();
            out.println(String.format("%-3d %-10d %-24s %8d %16s", position++, mechanic.getDocumentNumber(),
                    mechanic.getFullName(), total.getOrders(), formatMoney(total.getAmount())));
        }
        out.println();
        out.println("REPUESTOS POR CANTIDAD USADA (mayor a menor)");
        out.println(String.format("%-3s %-34s %14s %9s", "#", "Repuesto", "Costo", "Cantidad"));
        position = 1;
        for (SparePartUsage usage : sparePartsReport) {
            out.println(String.format("%-3d %-34s %14s %9d", position++, usage.getSparePart().getName(),
                    formatMoney(usage.getSparePart().getUnitPrice()), usage.getQuantity()));
        }
        printErrors();
    }

    /**
     * Opcion 4: escribe los reportes CSV en la carpeta de salida.
     *
     * @throws IOException si no se pueden escribir los reportes
     */
    private void downloadOutput() throws IOException {
        processReports();
        ReportService.writeReports(reportsDirectory, mechanicsReport, sparePartsReport, data.getErrors());
        out.println("Reportes guardados en ./" + reportsDirectory.getPath() + "/:");
        out.println("  - " + ReportService.MECHANICS_REPORT);
        out.println("  - " + ReportService.SPARE_PARTS_REPORT);
        out.println("  - " + ReportService.ERRORS_REPORT);
    }

    /**
     * Opcion 5: submenu de busqueda y filtrado. Se repite hasta que el
     * usuario elige volver al menu principal.
     *
     * @throws IOException si faltan archivos maestros
     */
    private void searchMenu() throws IOException {
        processReports();
        while (true) {
            out.println();
            out.println("BUSCAR Y FILTRAR");
            out.println("[1] Buscar mecanico (nombre o documento)");
            out.println("[2] Buscar repuesto (ID o nombre)");
            out.println("[3] Repuestos con riesgo de desabastecimiento");
            out.println("[0] Volver al menu principal");
            String option = readLine("Seleccione una opcion: ");
            if (option == null || SEARCH_BACK.equals(option)) {
                return;
            } else if (SEARCH_MECHANIC.equals(option)) {
                searchMechanic();
            } else if (SEARCH_SPARE_PART.equals(option)) {
                searchSparePart();
            } else if (SEARCH_LOW_STOCK.equals(option)) {
                showLowStock();
            } else {
                out.println("Opcion no valida. Escriba 0, 1, 2 o 3.");
            }
        }
    }

    /**
     * Busca mecanicos cuyo nombre contenga el texto o cuyo documento lo
     * contenga, y muestra sus ordenes de trabajo con el detalle de repuestos.
     */
    private void searchMechanic() {
        String term = readSearchTerm("Nombre o documento a buscar: ");
        if (term == null) {
            return;
        }
        int found = 0;
        for (MechanicTotal total : mechanicsReport) {
            Mechanic mechanic = total.getMechanic();
            String document = String.valueOf(mechanic.getDocumentNumber());
            if (!normalize(mechanic.getFullName()).contains(term) && !document.contains(term)) {
                continue;
            }
            found++;
            out.println();
            out.println(mechanic.getDocumentType() + " " + document + " - " + mechanic.getFullName()
                    + " | Ordenes: " + total.getOrders() + " | Recaudado: " + formatMoney(total.getAmount()));
            for (WorkOrder order : data.getOrders()) {
                if (order.getMechanicDocument() != mechanic.getDocumentNumber()) {
                    continue;
                }
                out.println("  " + order.getFileName());
                for (WorkOrder.OrderLine line : order.getLines()) {
                    SparePart part = data.getSpareParts().get(line.getSparePartId());
                    out.println(String.format("    %-5s %-34s x%-3d %14s", part.getId(), part.getName(),
                            line.getQuantity(), formatMoney(part.getUnitPrice() * line.getQuantity())));
                }
            }
        }
        out.println();
        out.println(found == 0 ? "No se encontraron mecanicos con \"" + term + "\"."
                : "Mecanicos encontrados: " + found);
    }

    /**
     * Busca repuestos por ID exacto o por parte del nombre, y muestra su
     * costo, stock, unidades usadas y estado de inventario.
     */
    private void searchSparePart() {
        String term = readSearchTerm("ID o nombre del repuesto: ");
        if (term == null) {
            return;
        }
        Map<String, Long> used = usedQuantities();
        List<SparePart> matches = new ArrayList<SparePart>();
        for (SparePart part : data.getSpareParts().values()) {
            if (normalize(part.getId()).equals(term) || normalize(part.getName()).contains(term)) {
                matches.add(part);
            }
        }
        if (matches.isEmpty()) {
            out.println("No se encontraron repuestos con \"" + term + "\".");
            return;
        }
        printStockTable(matches, used);
    }

    /**
     * Filtra los repuestos cuyo stock restante (stock menos unidades usadas)
     * es menor que {@link #LOW_STOCK_THRESHOLD}, ordenados del mas critico al
     * menos critico. Sirve como alerta para el area de compras.
     */
    private void showLowStock() {
        final Map<String, Long> used = usedQuantities();
        List<SparePart> critical = new ArrayList<SparePart>();
        for (SparePart part : data.getSpareParts().values()) {
            if (remaining(part, used) < LOW_STOCK_THRESHOLD) {
                critical.add(part);
            }
        }
        if (critical.isEmpty()) {
            out.println("Ningun repuesto tiene menos de " + LOW_STOCK_THRESHOLD + " unidades restantes.");
            return;
        }
        Collections.sort(critical, new Comparator<SparePart>() {
            @Override
            public int compare(SparePart first, SparePart second) {
                return Long.compare(remaining(first, used), remaining(second, used));
            }
        });
        out.println();
        out.println("REPUESTOS CON MENOS DE " + LOW_STOCK_THRESHOLD + " UNIDADES RESTANTES");
        printStockTable(critical, used);
    }

    /**
     * Imprime una tabla de inventario para los repuestos indicados.
     *
     * @param parts repuestos a mostrar
     * @param used  unidades usadas por ID de repuesto
     */
    private void printStockTable(List<SparePart> parts, Map<String, Long> used) {
        out.println(String.format("%-5s %-32s %12s %6s %6s %9s  %s", "ID", "Repuesto", "Costo", "Stock",
                "Usado", "Restante", "Estado"));
        for (SparePart part : parts) {
            long remaining = remaining(part, used);
            String status = remaining < 0 ? "SIN STOCK" : remaining < LOW_STOCK_THRESHOLD ? "REPONER" : "OK";
            out.println(String.format("%-5s %-32s %12s %6d %6d %9d  %s", part.getId(), part.getName(),
                    formatMoney(part.getUnitPrice()), part.getStock(), usedOf(part, used), remaining, status));
        }
    }

    /**
     * Construye un mapa con las unidades usadas de cada repuesto.
     *
     * @return unidades usadas por ID de repuesto (solo repuestos usados)
     */
    private Map<String, Long> usedQuantities() {
        Map<String, Long> used = new HashMap<String, Long>();
        for (SparePartUsage usage : sparePartsReport) {
            used.put(usage.getSparePart().getId(), usage.getQuantity());
        }
        return used;
    }

    /**
     * Devuelve las unidades usadas de un repuesto.
     *
     * @param part repuesto
     * @param used unidades usadas por ID
     * @return unidades usadas, o cero si no se ha usado
     */
    private static long usedOf(SparePart part, Map<String, Long> used) {
        Long value = used.get(part.getId());
        return value == null ? 0 : value;
    }

    /**
     * Calcula las unidades que quedan en bodega despues de las ordenes.
     *
     * @param part repuesto
     * @param used unidades usadas por ID
     * @return stock menos unidades usadas (puede ser negativo)
     */
    private static long remaining(SparePart part, Map<String, Long> used) {
        return part.getStock() - usedOf(part, used);
    }

    /**
     * Pide un texto de busqueda y lo normaliza.
     *
     * @param prompt mensaje a mostrar
     * @return el texto en minusculas y sin espacios sobrantes, o {@code null}
     *         si esta vacio o se termino la entrada
     */
    private String readSearchTerm(String prompt) {
        String term = readLine(prompt);
        if (term == null || term.isEmpty()) {
            out.println("Debe escribir un texto para buscar.");
            return null;
        }
        return normalize(term);
    }

    /**
     * Muestra un mensaje y lee una linea del usuario.
     *
     * @param prompt mensaje a mostrar
     * @return la linea sin espacios al inicio y al final, o {@code null} si
     *         se termino la entrada
     */
    private String readLine(String prompt) {
        out.print(prompt);
        out.flush();
        if (!input.hasNextLine()) {
            return null;
        }
        return input.nextLine().trim();
    }

    /**
     * Pasa un texto a minusculas para comparar sin importar mayusculas.
     *
     * @param text texto a normalizar
     * @return el texto en minusculas y sin espacios al inicio y al final
     */
    private static String normalize(String text) {
        return text.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Carga los datos si todavia no estan en memoria.
     *
     * @throws IOException si faltan archivos maestros
     */
    private void loadData() throws IOException {
        if (data == null) {
            data = DataLoader.load(dataDirectory);
            mechanicsReport = null;
            sparePartsReport = null;
        }
    }

    /**
     * Calcula los reportes si todavia no se han calculado.
     *
     * @throws IOException si faltan archivos maestros
     */
    private void processReports() throws IOException {
        loadData();
        if (mechanicsReport == null || sparePartsReport == null) {
            mechanicsReport = ReportService.buildMechanicsReport(data);
            sparePartsReport = ReportService.buildSparePartsReport(data);
        }
    }

    /** Imprime los errores de datos encontrados en la carga, si hay. */
    private void printErrors() {
        if (data.getErrors().isEmpty()) {
            out.println("Validacion: sin errores de datos.");
            return;
        }
        out.println();
        out.println("LINEAS DESCARTADAS (" + data.getErrors().size() + ")");
        for (String error : data.getErrors()) {
            out.println("  - " + error);
        }
    }

    /**
     * Da formato de pesos a un valor, por ejemplo {@code $ 1.250.000}.
     *
     * @param value valor en pesos
     * @return el valor con separador de miles
     */
    public static String formatMoney(long value) {
        return "$ " + MONEY_FORMAT.format(value);
    }

    /**
     * Crea el formato de dinero con punto como separador de miles.
     *
     * @return el formato configurado
     */
    private static DecimalFormat createMoneyFormat() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        return new DecimalFormat("#,##0", symbols);
    }
}
