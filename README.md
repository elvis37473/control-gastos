# Control de Gastos Personales

Aplicación web para administrar ingresos y gastos, controlar presupuestos mensuales y consultar reportes y proyecciones.

- **Autor:** Joelvis Segovia
- **Materia:** Lógica de Programación / Aplicaciones Web
- **Fecha:** Noviembre de 2026
- **Repositorio:** https://github.com/elvis37473/control-gastos

## Funcionalidades

- **Movimientos:** registrar, consultar, modificar y eliminar ingresos y gastos; buscar y filtrar por tipo, mes y categoría.
![alt text](image.png)
- **Presupuestos:** asignar un presupuesto a cada categoría y mes, consultar el gasto y porcentaje usado, y ver alertas al superar el 80 % y el 100 %.
![alt text](image-1.png)
- **Reportes:** consultar ingresos, gastos, saldo y detalle por categoría; filtrar por mes y categoría; exportar el reporte como archivo `.txt`.
![alt text](image-2.png)
- **Análisis:** visualizar los gastos de los últimos seis meses y la proyección de gasto al cierre del mes actual.
- Validación de datos, notificaciones, confirmación antes de eliminar y ayudas emergentes (tooltips).
- Datos persistidos en archivos de texto, sin base de datos.
![alt text](image-3.png)

## Tecnologías

- Java 21 o superior
- Spring Boot 4.1.2
- Vaadin 25.3.5 (Flow)
- Maven
- Archivos `.txt`

## Requisitos

- JDK 21 o superior
- Maven 3.8 o superior, o usar el wrapper incluido (`mvnw.cmd` en Windows)
- Conexión a internet para descargar dependencias la primera vez

## Cómo ejecutar

Desde esta carpeta, donde está `pom.xml`:

```powershell
.\mvnw.cmd spring-boot:run
```

También se puede usar `mvn spring-boot:run` si Maven está instalado. Luego abre <http://localhost:8080>. Para detener la aplicación, pulsa `Ctrl+C` en la terminal.

## Datos de ejemplo

En el primer arranque se crean `datos/movimientos.txt` y `datos/presupuestos.txt` si todavía no existen. Los datos se generan para los últimos seis meses, incluido el mes actual. La aplicación conserva los archivos existentes al reiniciar.

Para regenerar los datos de ejemplo, detén la aplicación, elimina la carpeta `datos/` y vuelve a iniciarla. `datos-ejemplo/` contiene una copia de referencia; la aplicación no lee ni modifica esa carpeta.

Los archivos usan campos separados por `|`:

```text
movimientos.txt:   id|TIPO|valor|fecha|categoría|descripción
                   1|INGRESO|3000000.00|2026-11-01|Salario|Pago mensual

presupuestos.txt:  mes|categoría|valor
                   2026-11|Alimentación|700000.00
```

## Manual de uso

El menú lateral permite abrir las cuatro pantallas. Los selectores de color de la barra superior cambian el tema visual.

### Movimientos

Usa la búsqueda para localizar movimientos y los filtros para acotar la tabla por tipo, mes o categoría. El resumen refleja los resultados mostrados. Desde el botón de nuevo movimiento puedes crear registros; las acciones de editar y eliminar están disponibles en la tabla. La eliminación solicita confirmación.

### Presupuestos

Selecciona el mes para consultar los presupuestos y su porcentaje de uso. Las barras y avisos indican el estado: normal, alerta (más del 80 %) o superado (más del 100 %). La pantalla permite filtrar y ordenar, consultar los gastos de una categoría, crear, editar o eliminar presupuestos y copiar los del mes anterior.

### Reportes

Selecciona el mes y, si quieres, una categoría. Las tarjetas resumen los ingresos, gastos y saldo; el detalle muestra los gastos agrupados por categoría. El menú ofrece acciones para registrar un movimiento, cambiar de mes y exportar el reporte. La exportación descarga un `.txt` y guarda una copia en `exportaciones/`.

### Análisis

El gráfico presenta seis meses de gastos. En el mes actual, la barra distingue lo gastado hasta hoy de la proyección al cierre. La proyección se calcula con el promedio diario de gasto multiplicado por los días del mes; también se muestran alertas de categorías cuyo presupuesto podría superarse o acercarse al límite.

## Componentes de Vaadin

| Componente | Uso |
|---|---|
| `ProgressBar` | Representa el porcentaje de uso del presupuesto. |
| `MenuBar` | Agrupa acciones de la pantalla de Reportes. |
| `Tooltip` | Muestra información adicional en botones, barras y tarjetas. |

## Pruebas

Ejecuta la suite automatizada con:

```powershell
.\mvnw.cmd test
```

Las pruebas cubren modelos, utilidades, persistencia y generación de datos. Antes de tomar capturas o presentar la aplicación, recorre manualmente los flujos principales en el navegador.

## Capturas

Las capturas todavía están pendientes. Guárdalas en `docs/capturas/` con estos nombres para añadirlas a la documentación:

| Archivo | Contenido |
|---|--- |
| `01-movimientos.png` | Tabla de movimientos, resumen y filtros |
| `02-formulario.png` | Formulario con validaciones |
| `03-confirmar-eliminar.png` | Confirmación de eliminación |
| `04-presupuestos.png` | Presupuestos y alertas |
| `05-tooltip.png` | Tooltip visible |
| `06-reportes.png` | Reportes y menú |
| `07-exportado.png` | Reporte exportado |
| `08-analisis.png` | Gráfico de seis meses |
| `09-proyeccion.png` | Proyección y avisos |
| `10-celular.png` | Aplicación en vista móvil |

## Estructura

```text
control-gastos/
├── pom.xml
├── datos/                  (se crea al ejecutar; no se sube)
├── datos-ejemplo/          (copia de referencia)
├── exportaciones/          (reportes exportados)
└── src/
    ├── main/java/com/proyecto/gastos/com/proyecto/
    │   ├── Application.java
    │   ├── MainLayout.java
    │   ├── MovimientosView.java
    │   ├── PresupuestosView.java
    │   ├── ReportesView.java
    │   ├── AnalisisView.java
    │   ├── archivos/GestorArchivos.java
    │   ├── modelo/Movimiento.java
    │   ├── modelo/Presupuesto.java
    │   └── vistas/
    │       ├── Tarjeta.java
    │       └── Utilidades.java
    └── test/
```

## Limitaciones

- No hay usuarios ni inicio de sesión; está pensada para uso individual.
- Los datos se almacenan en archivos de texto, adecuados para un proyecto pequeño.
- La aplicación se ejecuta en modo desarrollo.
