package com.proyecto.gastos.com.proyecto.archivos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.proyecto.gastos.com.proyecto.modelo.Movimiento;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GestorArchivosTest {

    private static final Clock RELOJ_FIJO = Clock.fixed(
            Instant.parse("2026-11-05T12:00:00Z"), ZoneOffset.UTC);

    @TempDir
    Path carpetaTemporal;

    @Test
    void creaDatosDeSeisMesesYNoSobrescribeCambiosExistentes() throws IOException {
        GestorArchivos gestor = new GestorArchivos(carpetaTemporal, RELOJ_FIJO);

        gestor.crearDatosDeEjemploSiNoExisten();

        List<Movimiento> movimientos = gestor.cargarMovimientos();
        assertEquals(93, movimientos.size());
        assertEquals(42, gestor.cargarPresupuestos().size());
        assertTrue(Files.exists(carpetaTemporal.resolve("movimientos.txt")));
        assertTrue(Files.exists(carpetaTemporal.resolve("presupuestos.txt")));
        assertEquals(YearMonth.of(2026, 6), YearMonth.from(
                movimientos.stream().map(Movimiento::getFecha).min(LocalDate::compareTo).orElseThrow()));
        assertFalse(movimientos.stream()
                .anyMatch(movimiento -> movimiento.getFecha().isAfter(LocalDate.of(2026, 11, 5))));
        assertEquals(353000, movimientos.stream()
                .filter(movimiento -> Movimiento.GASTO.equals(movimiento.getTipo()))
                .filter(movimiento -> YearMonth.from(movimiento.getFecha())
                        .equals(YearMonth.of(2026, 11)))
                .mapToDouble(Movimiento::getValor)
                .sum());

        movimientos.get(0).setDescripcion("Descripción conservada");
        gestor.guardarMovimientos(movimientos);
        gestor.crearDatosDeEjemploSiNoExisten();

        assertEquals("Descripción conservada", gestor.cargarMovimientos().get(0).getDescripcion());
    }

    @Test
    void generaMovimientosDeImportePositivoCuandoElMesApenasEmpieza() throws IOException {
        Clock primerDiaDelMes = Clock.fixed(
                Instant.parse("2026-11-01T12:00:00Z"), ZoneOffset.UTC);
        GestorArchivos gestor = new GestorArchivos(carpetaTemporal, primerDiaDelMes);

        gestor.crearDatosDeEjemploSiNoExisten();

        List<Movimiento> delMes = gestor.cargarMovimientos().stream()
                .filter(movimiento -> YearMonth.from(movimiento.getFecha())
                        .equals(YearMonth.of(2026, 11)))
                .toList();
        assertTrue(delMes.stream().allMatch(movimiento -> movimiento.getValor() > 0));
        assertFalse(delMes.stream()
                .anyMatch(movimiento -> movimiento.getFecha().isAfter(LocalDate.of(2026, 11, 1))));
    }

    @Test
    void ignoraLineaCorruptaSinPerderLosRegistrosValidos() throws IOException {
        GestorArchivos gestor = new GestorArchivos(carpetaTemporal, RELOJ_FIJO);
        gestor.crearDatosDeEjemploSiNoExisten();
        Path archivo = carpetaTemporal.resolve("movimientos.txt");
        Files.writeString(archivo, Files.readString(archivo) + "esto no sirve\n");

        assertEquals(93, gestor.cargarMovimientos().size());
    }

    @Test
    void rechazaGuardarPresupuestosDuplicadosSinModificarElArchivo() throws IOException {
        GestorArchivos gestor = new GestorArchivos(carpetaTemporal, RELOJ_FIJO);
        gestor.crearDatosDeEjemploSiNoExisten();
        Path archivo = carpetaTemporal.resolve("presupuestos.txt");
        String contenidoOriginal = Files.readString(archivo);
        var presupuesto = gestor.cargarPresupuestos().get(0);

        assertThrows(IllegalArgumentException.class,
                () -> gestor.guardarPresupuestos(List.of(presupuesto, presupuesto)));
        assertEquals(contenidoOriginal, Files.readString(archivo));
    }

    @Test
    void guardaReportesComoUtf8EnLaCarpetaDeExportaciones() throws IOException {
        Path exportaciones = carpetaTemporal.resolve("exportaciones");
        GestorArchivos gestor = new GestorArchivos(carpetaTemporal, RELOJ_FIJO, exportaciones);
        String contenido = "Categoría: Alimentación\nDescripción: Café y pan";

        assertTrue(gestor.guardarReporte("reporte-2026-11.txt", contenido));
        assertEquals(contenido, Files.readString(
                exportaciones.resolve("reporte-2026-11.txt"), StandardCharsets.UTF_8));
    }

    @Test
    void rechazaRutasFueraDeLaCarpetaDeExportaciones() {
        GestorArchivos gestor = new GestorArchivos(
                carpetaTemporal, RELOJ_FIJO, carpetaTemporal.resolve("exportaciones"));

        assertThrows(IllegalArgumentException.class,
                () -> gestor.guardarReporte("..\\fuera.txt", "contenido"));
    }
}
