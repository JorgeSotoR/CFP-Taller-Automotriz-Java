package co.edu.poli.taller;

import co.edu.poli.taller.cli.ConsoleMenu;
import co.edu.poli.taller.generator.GenerateInfoFiles;
import co.edu.poli.taller.processing.DataLoader;
import co.edu.poli.taller.processing.ReportService;
import co.edu.poli.taller.processing.ReportService.MechanicTotal;
import co.edu.poli.taller.processing.ReportService.SparePartUsage;
import co.edu.poli.taller.processing.TallerData;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Segunda clase ejecutable del proyecto (el nombre en minuscula lo exige el
 * enunciado). Lee los archivos generados por {@link GenerateInfoFiles},
 * cruza las ordenes de trabajo con los catalogos y crea los reportes:
 * <ul>
 *   <li>{@code reportes/reporte_mecanicos.csv}: mecanicos por dinero
 *       recaudado, de mayor a menor (punto 3 del enunciado).</li>
 *   <li>{@code reportes/reporte_repuestos.csv}: repuestos por cantidad
 *       usada, de mayor a menor (punto 4 del enunciado).</li>
 *   <li>{@code reportes/reporte_errores.txt}: datos descartados.</li>
 * </ul>
 *
 * <p>Modos de ejecucion:</p>
 * <ul>
 *   <li>Sin argumentos: abre el menu interactivo de consola
 *       ({@link ConsoleMenu}).</li>
 *   <li>Con el argumento {@code --auto}: modo automatico. No solicita
 *       informacion al usuario, genera los reportes y muestra un mensaje de
 *       exito o de error, como pide el enunciado original.</li>
 * </ul>
 *
 * @author Jorge Adrian Soto Reyes
 * @version 3.0
 */
public class main {

    /** Argumento que activa el modo automatico, sin menu. */
    private static final String AUTO_ARGUMENT = "--auto";

    /** Carpeta donde se escriben los reportes. */
    private static final String REPORTS_DIRECTORY = "reportes";

    /** Clase de arranque con metodos estaticos: no se instancia. */
    private main() {
    }

    /**
     * Punto de entrada del programa de reportes.
     *
     * @param args vacio para el menu, o {@code --auto} para el modo automatico
     */
    public static void main(String[] args) {
        File dataDirectory = new File(GenerateInfoFiles.getDirectoryPath());
        File reportsDirectory = new File(REPORTS_DIRECTORY);

        if (args.length == 0 || !AUTO_ARGUMENT.equals(args[0])) {
            new ConsoleMenu(dataDirectory, reportsDirectory, System.in, System.out).run();
            return;
        }

        try {
            runAutomatic(dataDirectory, reportsDirectory);
        } catch (IOException | RuntimeException e) {
            System.err.println("Error: no fue posible generar los reportes. Detalle: " + e.getMessage());
        }
    }

    /**
     * Ejecuta todo el proceso sin intervencion del usuario: carga, valida,
     * cruza, ordena y escribe los reportes.
     *
     * @param dataDirectory    carpeta con los archivos de entrada
     * @param reportsDirectory carpeta de salida de los reportes
     * @throws IOException si falta un archivo de entrada o no se puede escribir la salida
     */
    private static void runAutomatic(File dataDirectory, File reportsDirectory) throws IOException {
        TallerData data = DataLoader.load(dataDirectory);
        List<MechanicTotal> mechanics = ReportService.buildMechanicsReport(data);
        List<SparePartUsage> spareParts = ReportService.buildSparePartsReport(data);
        ReportService.writeReports(reportsDirectory, mechanics, spareParts, data.getErrors());

        System.out.println("Finalizacion exitosa: reportes generados en ./" + reportsDirectory.getPath() + "/");
        System.out.println("  Ordenes procesadas: " + data.getOrders().size()
                + " | Mecanicos: " + mechanics.size()
                + " | Repuestos usados: " + spareParts.size());
        if (!data.getErrors().isEmpty()) {
            System.out.println("  Advertencia: se descartaron " + data.getErrors().size()
                    + " datos con errores. Ver " + ReportService.ERRORS_REPORT);
        }
    }
}
