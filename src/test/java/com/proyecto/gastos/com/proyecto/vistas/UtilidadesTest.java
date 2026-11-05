package com.proyecto.gastos.com.proyecto.vistas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.proyecto.gastos.com.proyecto.modelo.Movimiento;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class UtilidadesTest {

    @Test
    void etiquetaFiltroSinSeleccionComoTodos() {
        assertEquals("Todos", Utilidades.nombreTipo(null));
        assertEquals("Ingreso", Utilidades.nombreTipo(Movimiento.INGRESO));
        assertEquals("Gasto", Utilidades.nombreTipo(Movimiento.GASTO));
    }

    @Test
    void formateaDineroYMesEnEspanol() {
        assertEquals("$ 735.000", Utilidades.formatoDinero(735000));
        assertEquals("Noviembre 2026",
                Utilidades.nombreMes(YearMonth.of(2026, 11)));
    }
}
