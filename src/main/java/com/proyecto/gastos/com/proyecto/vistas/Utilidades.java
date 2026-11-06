package com.proyecto.gastos.com.proyecto.vistas;

import com.proyecto.gastos.com.proyecto.modelo.Movimiento;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Utilidades {

    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter FORMATO_MES =
            DateTimeFormatter.ofPattern("MMMM yyyy", COLOMBIA);

    public static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Utilidades() {
    }

    public static void mostrarExito(String mensaje) {
        Notification notificacion =
                Notification.show(mensaje, 3000, Notification.Position.TOP_CENTER);
        notificacion.addThemeName("success");
    }

    public static void mostrarAdvertencia(String mensaje) {
        Notification notificacion =
                Notification.show(mensaje, 6000, Notification.Position.TOP_CENTER);
        notificacion.addThemeName("warning");
    }

    public static void mostrarError(String mensaje) {
        Notification notificacion =
                Notification.show(mensaje, 5000, Notification.Position.TOP_CENTER);
        notificacion.addThemeName("error");
    }

    public static String formatoDinero(double valor) {
        NumberFormat formato = NumberFormat.getNumberInstance(COLOMBIA);
        formato.setMaximumFractionDigits(2);
        return "$ " + formato.format(valor);
    }

    public static String formatoPorcentaje(double porcentaje) {
        return String.format(COLOMBIA, "%.1f %%", porcentaje);
    }

    public static String nombreMes(YearMonth mes) {
        String texto = mes.format(FORMATO_MES);
        return texto.substring(0, 1).toUpperCase(COLOMBIA) + texto.substring(1);
    }

    public static String nombreTipo(String tipo) {
        if (tipo == null) {
            return "Todos";
        }
        if (Movimiento.INGRESO.equals(tipo)) {
            return "Ingreso";
        }
        if (Movimiento.GASTO.equals(tipo)) {
            return "Gasto";
        }
        throw new IllegalArgumentException("Tipo de movimiento desconocido: " + tipo);
    }

    public static void estiloPrincipal(Button boton) {
        boton.getStyle()
                .set("background-color", "var(--color-acento, #2563eb)")
                .set("color", "#ffffff");
    }

    public static Button crearBotonIcono(Icon icono, String etiqueta) {
        Button boton = new Button(icono);
        boton.setAriaLabel(etiqueta);
        boton.getElement().setProperty("title", etiqueta);
        boton.getStyle().set("background-color", "transparent");
        return boton;
    }

    public static void aplicarTooltip(Component componente, String texto) {
        componente.getElement().setProperty("title", texto);
    }
}
