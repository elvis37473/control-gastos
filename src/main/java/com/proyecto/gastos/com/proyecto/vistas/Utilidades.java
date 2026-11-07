package com.proyecto.gastos.com.proyecto.vistas;

import com.proyecto.gastos.com.proyecto.modelo.Movimiento;
import com.proyecto.gastos.com.proyecto.modelo.Presupuesto;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.shared.Tooltip;
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

    public static Div crearIndicador(String titulo, Span valor, String color, String ayuda) {
        Span etiqueta = new Span(titulo);
        etiqueta.getStyle()
                .set("display", "block")
                .set("color", "#6b7280")
                .set("font-size", "0.9rem");
        valor.getStyle()
                .set("display", "block")
                .set("font-size", "1.6rem")
                .set("font-weight", "700");

        Div tarjeta = new Div(etiqueta, valor);
        tarjeta.getStyle()
                .set("flex", "1 1 200px")
                .set("padding", "16px")
                .set("border", "1px solid #e5e7eb")
                .set("border-left", "5px solid " + color)
                .set("border-radius", "10px")
                .set("box-sizing", "border-box");
        Tooltip.forComponent(tarjeta).withText(ayuda);
        return tarjeta;
    }

    public static Div crearAviso(
            VaadinIcon tipoIcono, String texto, String colorFondo, String colorTexto) {
        Div aviso = new Div(tipoIcono.create(), new Span(texto));
        aviso.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "10px")
                .set("padding", "10px 16px")
                .set("border-radius", "8px")
                .set("background-color", colorFondo)
                .set("color", colorTexto)
                .set("font-weight", "600");
        return aviso;
    }

    public static String colorDelEstado(String estado) {
        if (Presupuesto.SUPERADO.equals(estado)) {
            return "#dc2626";
        }
        if (Presupuesto.ALERTA.equals(estado)) {
            return "#f59e0b";
        }
        return "#16a34a";
    }
}
