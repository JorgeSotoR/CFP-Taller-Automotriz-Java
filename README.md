# Taller Automotriz - Generacion y Clasificacion de Datos

Proyecto del modulo Conceptos Fundamentales de Programacion (Politecnico Grancolombiano).
Sistema de rotacion de repuestos: cruza ordenes de trabajo con el catalogo de repuestos
y el padron de mecanicos, y genera reportes ordenados.

Java 8 - Eclipse IDE for Java Developers.

## Clases ejecutables (solo dos)

| Clase | Que hace |
|---|---|
| `co.edu.poli.taller.generator.GenerateInfoFiles` | Genera los archivos pseudoaleatorios en `datos_taller/` |
| `co.edu.poli.taller.main` | Menu de consola: lee, valida, cruza, busca y escribe los reportes en `reportes/` |

`GenerateInfoFiles` no pide datos al usuario. `main` abre un menu; con `--auto` corre sin pedir datos.

## Como ejecutar en Eclipse

1. File > Import > General > Existing Projects into Workspace > seleccionar esta carpeta.
2. Clic derecho en `GenerateInfoFiles.java` > Run As > Java Application (genera `datos_taller/`).
3. Clic derecho en `main.java` > Run As > Java Application (abre el menu).
4. Refrescar el proyecto (F5) para ver `datos_taller/` y `reportes/`.

### Menu de consola

```
[1] Generar Archivos      -> crea un lote nuevo de datos de prueba
[2] Importar/Ver Base     -> muestra repuestos, mecanicos, ordenes y errores
[3] Importar/Ver Salida   -> muestra los dos reportes ordenados
[4] Descargar Salida      -> escribe los reportes en reportes/
[5] Buscar y Filtrar      -> buscar mecanico, buscar repuesto, repuestos por reponer
[6] Salir
```

### Modo automatico (sin menu)

Ejecutar `main` con el argumento `--auto` (Run > Run Configurations > Arguments).
No pide datos al usuario: genera los reportes y muestra un mensaje de exito o de error,
como pide el enunciado original del proyecto.

## Archivos

Entrada (`datos_taller/`):

- `repuestos.csv` -> `IDRepuesto;NombreRepuesto;CostoUnitario;StockActual`
- `mecanicos.csv` -> `TipoDocumento;NumeroDocumento;Nombres;Apellidos`
- `orden_NNN_<documento>_<nombre>.txt` -> primera linea `TipoDocumento;NumeroDocumento`, luego `IDRepuesto;Cantidad;`

Salida (`reportes/`):

- `reporte_mecanicos.csv` -> `Nombres Apellidos;TotalRecaudado`, de mayor a menor
- `reporte_repuestos.csv` -> `NombreRepuesto;CostoUnitario;CantidadUsada`, de mayor a menor cantidad
- `reporte_errores.txt` -> lineas o archivos descartados por formato o datos incoherentes
