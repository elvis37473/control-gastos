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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Utilidades {

    public static final String FONDO = "var(--fondo, #f8fafc)";
    public static final String SUPERFICIE = "var(--superficie, #ffffff)";
    public static final String TEXTO = "var(--texto, #0f172a)";
    public static final String TEXTO_SUAVE = "var(--texto-suave, #64748b)";
    public static final String BORDE = "var(--borde, #e2e8f0)";

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
                .set("color", "#ffffff")
                .set("font-weight", "600")
                .set("border-radius", "8px")
                .set("box-shadow", "0 1px 2px rgba(15, 23, 42, 0.2)")
                .set("cursor", "pointer");
    }

    public static Button crearBotonIcono(Icon icono, String etiqueta) {
        Button boton = new Button(icono);
        boton.setAriaLabel(etiqueta);
        boton.getStyle()
                .set("background-color", "transparent")
                .set("border-radius", "8px");
        Tooltip.forComponent(boton).withText(etiqueta);
        return boton;
    }

    public static void aplicarTooltip(Component componente, String texto) {
        Tooltip.forComponent(componente).withText(texto);
    }

    public static VerticalLayout crearPagina(Component... contenido) {
        VerticalLayout pagina = new VerticalLayout(contenido);
        pagina.setWidthFull();
        pagina.setMaxWidth("1240px");
        pagina.setPadding(false);
        pagina.setSpacing(false);
        pagina.getStyle()
                .set("margin", "0 auto")
                .set("gap", "20px");
        return pagina;
    }

    public static Div crearTarjetaEstadistica(
            VaadinIcon tipoIcono, String titulo, Span valor, String color, String ayuda) {
        Icon icono = tipoIcono.create();
        icono.setSize("20px");
        icono.getStyle().set("color", color);

        Div insignia = new Div(icono);
        insignia.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("width", "44px")
                .set("height", "44px")
                .set("flex-shrink", "0")
                .set("border-radius", "12px")
                .set("background-color", "color-mix(in srgb, " + color + " 14%, transparent)");

        Span etiqueta = new Span(titulo);
        etiqueta.getStyle()
                .set("display", "block")
                .set("color", TEXTO_SUAVE)
                .set("font-size", "0.85rem")
                .set("font-weight", "500");
        valor.getStyle()
                .set("display", "block")
                .set("color", TEXTO)
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("line-height", "1.2");

        Div tarjeta = new Div(insignia, new Div(etiqueta, valor));
        tarjeta.addClassName("tarjeta-estadistica");
        tarjeta.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "14px")
                .set("padding", "18px")
                .set("min-width", "0")
                .set("background-color", SUPERFICIE)
                .set("border", "1px solid " + BORDE)
                .set("border-radius", "14px")
                .set("box-sizing", "border-box");
        Tooltip.forComponent(tarjeta).withText(ayuda);
        return tarjeta;
    }

    public static Span crearEtiquetaTipo(String tipo) {
        Span etiqueta = new Span(nombreTipo(tipo));
        etiqueta.getStyle()
                .set("padding", "2px 10px")
                .set("border-radius", "999px")
                .set("font-size", "0.8rem")
                .set("font-weight", "600");
        if (Movimiento.INGRESO.equals(tipo)) {
            etiqueta.getStyle()
                    .set("background-color", "#dcfce7")
                    .set("color", "#166534");
        } else {
            etiqueta.getStyle()
                    .set("background-color", "#fee2e2")
                    .set("color", "#991b1b");
        }
        return etiqueta;
    }

    public static Div crearIndicador(String titulo, Span valor, String color, String ayuda) {
        Span etiqueta = new Span(titulo);
        etiqueta.getStyle()
                .set("display", "block")
                .set("color", TEXTO_SUAVE)
                .set("font-size", "0.9rem");
        valor.getStyle()
                .set("display", "block")
                .set("font-size", "1.6rem")
                .set("font-weight", "700");

        Div tarjeta = new Div(etiqueta, valor);
        tarjeta.getStyle()
                .set("flex", "1 1 200px")
                .set("padding", "16px")
                .set("border", "1px solid " + BORDE)
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
