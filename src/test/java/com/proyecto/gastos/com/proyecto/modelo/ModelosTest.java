package com.proyecto.gastos.com.proyecto.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class ModelosTest {

    @Test
    void serializaMovimientoConSeparadorEnDescripcionSinRomperElFormato() {
        Movimiento original = new Movimiento(1, Movimiento.GASTO, 1234.5,
                LocalDate.of(2026, 11, 5), "Alimentación", "Compra | mercado");

        Movimiento restaurado = Movimiento.desdeLinea(original.aLinea());

        assertEquals("Compra / mercado", restaurado.getDescripcion());
        assertEquals(1234.5, restaurado.getValor());
        assertEquals(original.getFecha(), restaurado.getFecha());
    }

    @Test
    void rechazaImportesNoFinitosYDescripcionesMultilinea() {
        Movimiento importeNoFinito = new Movimiento(1, Movimiento.GASTO, Double.NaN,
                LocalDate.of(2026, 11, 5), "Alimentación", "Compra");
        Movimiento descripcionMultilinea = new Movimiento(2, Movimiento.GASTO, 100,
                LocalDate.of(2026, 11, 5), "Alimentación", "Primera línea\nSegunda línea");

        assertThrows(IllegalArgumentException.class, importeNoFinito::validar);
        assertThrows(IllegalArgumentException.class, descripcionMultilinea::validar);
    }

    @Test
    void presupuestoSeSerializaYValidaCategoria() {
        Presupuesto original = new Presupuesto(
                YearMonth.of(2026, 11), "Alimentación", 700000);

        Presupuesto restaurado = Presupuesto.desdeLinea(original.aLinea());

        assertEquals(original.getMes(), restaurado.getMes());
        assertEquals(original.getCategoria(), restaurado.getCategoria());
        assertEquals(original.getValorAsignado(), restaurado.getValorAsignado());
        assertThrows(IllegalArgumentException.class,
                () -> new Presupuesto(YearMonth.of(2026, 11), "Salario", 100).validar());
    }
}
