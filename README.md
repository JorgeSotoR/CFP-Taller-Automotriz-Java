# Taller Automotriz - Generacion y Clasificacion de Datos

Proyecto del modulo Conceptos Fundamentales de Programacion (Politecnico Grancolombiano).
Sistema de rotacion de repuestos: cruza ordenes de trabajo con el catalogo de repuestos
y el padron de mecanicos, y genera reportes ordenados.

Java 8 - Eclipse IDE for Java Developers.

## Clases ejecutables (solo dos)

| Clase | Que hace |
|---|---|
| `co.edu.poli.taller.generator.GenerateInfoFiles` | Genera los archivos pseudoaleatorios en `datos_taller/` |
| `co.edu.poli.taller.main` | Lee los archivos, valida, cruza y escribe los reportes en `reportes/` |

Ninguna de las dos pide datos al usuario.

## Como ejecutar en Eclipse

1. File > Import > General > Existing Projects into Workspace > seleccionar esta carpeta.
2. Clic derecho en `GenerateInfoFiles.java` > Run As > Java Application.
3. Clic derecho en `main.java` > Run As > Java Application.
4. Refrescar el proyecto (F5) para ver `datos_taller/` y `reportes/`.

## Archivos

Entrada (`datos_taller/`):

- `repuestos.csv` -> `IDRepuesto;NombreRepuesto;CostoUnitario;StockActual`
- `mecanicos.csv` -> `TipoDocumento;NumeroDocumento;Nombres;Apellidos`
- `orden_NNN_<documento>_<nombre>.txt` -> primera linea `TipoDocumento;NumeroDocumento`, luego `IDRepuesto;Cantidad;`

Salida (`reportes/`):

- `reporte_mecanicos.csv` -> `Nombres Apellidos;TotalRecaudado`, de mayor a menor
- `reporte_repuestos.csv` -> `NombreRepuesto;CostoUnitario;CantidadUsada`, de mayor a menor cantidad
- `reporte_errores.txt` -> lineas o archivos descartados por formato o datos incoherentes
