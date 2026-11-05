package com.proyecto.gastos.com.proyecto.archivos;

import com.proyecto.gastos.com.proyecto.modelo.Movimiento;
import com.proyecto.gastos.com.proyecto.modelo.Presupuesto;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GestorArchivos {

    private final Path carpeta;
    private final Clock reloj;
    private final Path archivoMovimientos;
    private final Path archivoPresupuestos;

    public GestorArchivos(Path carpeta) {
        this(carpeta, Clock.systemDefaultZone());
    }

    public GestorArchivos(Path carpeta, Clock reloj) {
        this.carpeta = carpeta;
        this.reloj = reloj;
        this.archivoMovimientos = carpeta.resolve("movimientos.txt");
        this.archivoPresupuestos = carpeta.resolve("presupuestos.txt");
    }

    public List<Movimiento> cargarMovimientos() throws IOException {
        List<Movimiento> movimientos = new ArrayList<>();
        if (!Files.exists(archivoMovimientos)) {
            return movimientos;
        }
        List<String> lineas = Files.readAllLines(archivoMovimientos, StandardCharsets.UTF_8);
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            if (linea.isBlank()) {
                continue;
            }
            try {
                movimientos.add(Movimiento.desdeLinea(linea));
            } catch (RuntimeException e) {
                System.err.println("Línea de movimiento " + (i + 1) + " ignorada ("
                        + e.getMessage() + "): " + linea);
            }
        }
        return movimientos;
    }

    public void guardarMovimientos(List<Movimiento> movimientos) throws IOException {
        List<String> lineas = new ArrayList<>();
        for (Movimiento movimiento : movimientos) {
            lineas.add(movimiento.aLinea());
        }
        guardarLineas(archivoMovimientos, lineas);
    }

    public int siguienteIdMovimiento(List<Movimiento> movimientos) {
        return movimientos.stream()
                .mapToInt(Movimiento::getId)
                .max()
                .orElse(0) + 1;
    }

    public List<Presupuesto> cargarPresupuestos() throws IOException {
        List<Presupuesto> presupuestos = new ArrayList<>();
        if (!Files.exists(archivoPresupuestos)) {
            return presupuestos;
        }
        List<String> lineas = Files.readAllLines(archivoPresupuestos, StandardCharsets.UTF_8);
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            if (linea.isBlank()) {
                continue;
            }
            try {
                presupuestos.add(Presupuesto.desdeLinea(linea));
            } catch (RuntimeException e) {
                System.err.println("Línea de presupuesto " + (i + 1) + " ignorada ("
                        + e.getMessage() + "): " + linea);
            }
        }
        return presupuestos;
    }

    public void guardarPresupuestos(List<Presupuesto> presupuestos) throws IOException {
        List<String> lineas = new ArrayList<>();
        Set<String> clavesUsadas = new HashSet<>();
        for (Presupuesto presupuesto : presupuestos) {
            presupuesto.validar();
            String clave = presupuesto.getMes() + "|" + presupuesto.getCategoria();
            if (!clavesUsadas.add(clave)) {
                throw new IllegalArgumentException("Ya existe un presupuesto para " + clave);
            }
            lineas.add(presupuesto.aLinea());
        }
        guardarLineas(archivoPresupuestos, lineas);
    }

    public void crearDatosDeEjemploSiNoExisten() throws IOException {
        if (!Files.exists(archivoMovimientos)) {
            guardarMovimientos(generarMovimientosDeEjemplo());
        }
        if (!Files.exists(archivoPresupuestos)) {
            guardarPresupuestos(generarPresupuestosDeEjemplo());
        }
    }

    private void guardarLineas(Path archivo, List<String> lineas) throws IOException {
        Files.createDirectories(carpeta);
        Files.write(archivo, lineas, StandardCharsets.UTF_8);
    }

    private List<Movimiento> generarMovimientosDeEjemplo() {
        List<Movimiento> lista = new ArrayList<>();
        LocalDate hoy = LocalDate.now(reloj);
        YearMonth mesActual = YearMonth.from(hoy);
        List<String> categorias = Movimiento.CATEGORIAS_GASTO;
        double[] gastosMesActual = {735000, 170000, 810000, 150000, 100000, 30000, 120000};
        String[][] descripciones = {
                {"Mercado del mes", "Domicilios y restaurantes"},
                {"Tarjeta de transporte", "Taxi"},
                {"Arriendo", "Administración"},
                {"Energía y agua", "Internet y celular"},
                {"Cine con amigos", "Suscripción de streaming"},
                {"Consulta médica", "Medicamentos"},
                {"Libros y copias", "Curso en línea"}
        };
        double[] factorPorMes = {0.80, 0.90, 0.70, 1.00, 0.85, 1.00};

        int id = 1;
        for (int i = 0; i < 6; i++) {
            YearMonth mes = mesActual.minusMonths(5 - i);
            int ultimoDia = mes.equals(mesActual) ? hoy.getDayOfMonth() : mes.lengthOfMonth();
            double factor = factorPorMes[i];

            lista.add(new Movimiento(id++, Movimiento.INGRESO, 3000000,
                    mes.atDay(1), "Salario", "Pago mensual"));

            if (i % 2 == 0) {
                lista.add(new Movimiento(id++, Movimiento.INGRESO, 350000,
                        mes.atDay(Math.min(15, ultimoDia)), "Ingreso extra", "Trabajo freelance"));
            }

            for (int c = 0; c < categorias.size(); c++) {
                double total = Math.round(gastosMesActual[c] * factor / 1000) * 1000;
                double primero = Math.round(total * 0.6 / 1000) * 1000;
                double segundo = total - primero;
                int dia1 = Math.min(3 + c * 2, ultimoDia);
                int dia2 = Math.min(16 + c * 2, ultimoDia);

                lista.add(new Movimiento(id++, Movimiento.GASTO, primero,
                        mes.atDay(dia1), categorias.get(c), descripciones[c][0]));
                lista.add(new Movimiento(id++, Movimiento.GASTO, segundo,
                        mes.atDay(dia2), categorias.get(c), descripciones[c][1]));
            }
        }
        return lista;
    }

    private List<Presupuesto> generarPresupuestosDeEjemplo() {
        List<Presupuesto> lista = new ArrayList<>();
        YearMonth mesActual = YearMonth.now(reloj);
        List<String> categorias = Movimiento.CATEGORIAS_GASTO;
        double[] valores = {700000, 200000, 900000, 300000, 250000, 150000, 200000};

        for (int i = 0; i < 6; i++) {
            YearMonth mes = mesActual.minusMonths(5 - i);
            for (int c = 0; c < categorias.size(); c++) {
                lista.add(new Presupuesto(mes, categorias.get(c), valores[c]));
            }
        }
        return lista;
    }
}
