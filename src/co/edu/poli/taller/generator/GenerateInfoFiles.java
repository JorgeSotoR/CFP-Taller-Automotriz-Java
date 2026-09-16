package co.edu.poli.taller.generator;  
  
import java.io.BufferedWriter;  
import java.io.File;  
import java.io.FileWriter;  
import java.io.IOException;  
import java.util.Random;  
  
/**  
 * Clase responsable de la generacion pseudoaleatoria de archivos planos  
 * para el sistema de clasificacion y rotacion del Taller Automotriz.  
 * Cumple con los requerimientos de la Entrega 1 (Semana 3).  
 *  
 * @author Jorge Adrian Soto Reyes  
 * @version 1.0  
 */  
public class GenerateInfoFiles {  
  
    private static final String DIRECTORY_PATH = "datos_taller";  
  
    private static final String[] FIRST_NAMES = {  
        "Carlos", "Andres", "Mateo", "Felipe", "Alejandro",   
        "David", "Sofia", "Mariana", "Juan", "Esteban"  
    };  
  
    private static final String[] LAST_NAMES = {  
        "Zapata", "Restrepo", "Gomez", "Mejia", "Henao",   
        "Castano", "Perez", "Jaramillo", "Ramirez", "Ochoa"  
    };  
  
    private static final String[] SPARE_PARTS = {  
        "Filtro de Aceite Sintetico", "Pastillas de Freno Delanteras",   
        "Bujia de Iridio", "Amortiguador Delantero Gas",   
        "Kit Correa Distribucion", "Liquido de Frenos DOT 4",   
        "Filtro de Aire Alto Flujo", "Bateria 12V 65Ah",   
        "Bomba de Agua Termica", "Correa de Accesorios Alternador",  
        "Disco de Freno Ventilado", "Rotula Direccion Externa"  
    };  
  
    public static void main(String[] args) {  
        try {  
            File baseDir = new File(DIRECTORY_PATH);  
            if (!baseDir.exists()) {  
                boolean created = baseDir.mkdirs();  
                if (!created) {  
                    throw new IOException("No fue posible crear la carpeta de destino: " + DIRECTORY_PATH);  
                }  
            }  
  
            int totalProducts = 12;  
            int totalSalesmen = 6;  
            int totalOrders = 10;  
  
            createProductsFile(totalProducts);  
            createSalesManInfoFile(totalSalesmen);  
  
            Random random = new Random();  
            for (int i = 1; i <= totalOrders; i++) {  
                String orderId = String.format("%03d", i);  
                long randomMechanicDoc = 10000000L + random.nextInt(90000000);  
                String mechanicName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];  
                int salesCount = 2 + random.nextInt(4);  
  
                createSalesMenFile(salesCount, mechanicName, randomMechanicDoc, orderId, totalProducts);  
            }  
  
            System.out.println("SUCESO: Archivos generados correctamente en el directorio: ./" + DIRECTORY_PATH + "/");  
        } catch (Exception e) {  
            System.err.println("ERROR: Ocurrio un error critico durante la generacion: " + e.getMessage());  
        }  
    }  
  
    public static void createProductsFile(int productsCount) throws IOException {  
        File targetFile = new File(DIRECTORY_PATH + File.separator + "repuestos.csv");  
        Random random = new Random();  
  
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {  
            for (int i = 1; i <= productsCount; i++) {  
                String id = "R" + String.format("%03d", i);  
                String name = (i <= SPARE_PARTS.length) ? SPARE_PARTS[i - 1] : "Repuesto Estandar " + i;  
                long unitPrice = 20000L + (random.nextInt(40) * 5000L);  
                int stock = 15 + random.nextInt(85);  
  
                writer.write(id + ";" + name + ";" + unitPrice + ";" + stock);  
                writer.newLine();  
            }  
        }  
    }  
  
    public static void createSalesManInfoFile(int salesmanCount) throws IOException {  
        File targetFile = new File(DIRECTORY_PATH + File.separator + "mecanicos.csv");  
        Random random = new Random();  
  
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {  
            for (int i = 0; i < salesmanCount; i++) {  
                String docType = "CC";  
                long docNumber = 10000000L + random.nextInt(90000000);  
                String name = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];  
                String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];  
  
                writer.write(docType + ";" + docNumber + ";" + name + ";" + lastName);  
                writer.newLine();  
            }  
        }  
    }  
  
    public static void createSalesMenFile(int randomSalesCount, String name, long id, String orderId, int totalProducts) throws IOException {  
        File targetFile = new File(DIRECTORY_PATH + File.separator + "orden_" + orderId + ".txt");  
        Random random = new Random();  
  
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {  
            writer.write("CC;" + id);  
            writer.newLine();  
  
            for (int i = 0; i < randomSalesCount; i++) {  
                int productIndex = 1 + random.nextInt(totalProducts);  
                String sparePartId = "R" + String.format("%03d", productIndex);  
                int quantity = 1 + random.nextInt(4);  
  
                writer.write(sparePartId + ";" + quantity + ";");  
                writer.newLine();  
            }  
        }  
    }  
}
