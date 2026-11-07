package com.proyecto.gastos.com.proyecto.vistas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.proyecto.gastos.com.proyecto.modelo.Movimiento;
import com.proyecto.gastos.com.proyecto.modelo.Presupuesto;
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

    @Test
    void asignaColoresSegunElEstadoDelPresupuesto() {
        assertEquals("#dc2626", Utilidades.colorDelEstado(Presupuesto.SUPERADO));
        assertEquals("#f59e0b", Utilidades.colorDelEstado(Presupuesto.ALERTA));
        assertEquals("#16a34a", Utilidades.colorDelEstado(Presupuesto.NORMAL));
    }
}
