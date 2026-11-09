package com.proyecto.gastos.com.proyecto;

import com.proyecto.gastos.com.proyecto.archivos.GestorArchivos;
import com.proyecto.gastos.com.proyecto.modelo.Movimiento;
import com.proyecto.gastos.com.proyecto.modelo.Presupuesto;
import com.proyecto.gastos.com.proyecto.vistas.Tarjeta;
import com.proyecto.gastos.com.proyecto.vistas.Utilidades;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Route(value = "analisis", layout = MainLayout.class)
@PageTitle("Análisis | Control de Gastos")
public class AnalisisView extends VerticalLayout {

    private static final int ALTURA_GRAFICO = 200;
    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");

    private final LocalDate hoy = LocalDate.now();
    private final YearMonth mesActual = YearMonth.from(hoy);
    private final int diasTranscurridos = hoy.getDayOfMonth();
    private final int diasDelMes = mesActual.lengthOfMonth();
    private final List<Movimiento> movimientos;
    private final List<Presupuesto> presupuestos;
    private final double gastadoMesActual;
    private final double proyeccionTotal;

    public AnalisisView(GestorArchivos gestor) {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background-color", "#f4f5f7");

        List<Movimiento> movimientosCargados;
        List<Presupuesto> presupuestosCargados;
        try {
            movimientosCargados = gestor.cargarMovimientos();
            presupuestosCargados = gestor.cargarPresupuestos();
        } catch (IOException e) {
            Utilidades.mostrarError("No se pudieron cargar los datos para el análisis: "
                    + e.getMessage());
            movimientosCargados = List.of();
            presupuestosCargados = List.of();
        }
        movimientos = movimientosCargados;
        presupuestos = presupuestosCargados;
        gastadoMesActual = gastosDelMes(mesActual);
        proyeccionTotal = proyectar(gastadoMesActual);

        VerticalLayout contenido = new VerticalLayout(
                crearEncabezado(), crearTarjetaGrafico(), crearTarjetaProyeccion());
        contenido.setWidthFull();
        contenido.setMaxWidth("1200px");
        contenido.setPadding(false);
        contenido.setSpacing(true);
        add(contenido);
    }

    private double gastosDelMes(YearMonth mes) {
        return movimientos.stream()
                .filter(movimiento -> Movimiento.GASTO.equals(movimiento.getTipo()))
                .filter(movimiento -> YearMonth.from(movimiento.getFecha()).equals(mes))
                .filter(movimiento -> !movimiento.getFecha().isAfter(hoy))
                .mapToDouble(Movimiento::getValor)
                .sum();
    }

    private double gastosCategoriaMes(YearMonth mes, String categoria) {
        return movimientos.stream()
                .filter(movimiento -> Movimiento.GASTO.equals(movimiento.getTipo()))
                .filter(movimiento -> YearMonth.from(movimiento.getFecha()).equals(mes))
                .filter(movimiento -> !movimiento.getFecha().isAfter(hoy))
                .filter(movimiento -> categoria.equals(movimiento.getCategoria()))
                .mapToDouble(Movimiento::getValor)
                .sum();
    }

    private double proyectar(double gastado) {
        return gastado / diasTranscurridos * diasDelMes;
    }

    private Div crearEncabezado() {
        H2 titulo = new H2("Análisis");
        titulo.getStyle().set("margin", "0");

        Paragraph subtitulo = new Paragraph(
                "Compara tus gastos de los últimos 6 meses y mira cómo cerrarás el mes actual.");
        subtitulo.getStyle().set("margin", "0").set("color", "#6b7280");
        return new Div(titulo, subtitulo);
    }

    private H3 crearTitulo(String texto) {
        H3 titulo = new H3(texto);
        titulo.getStyle().set("margin", "0 0 12px");
        return titulo;
    }

    private Tarjeta crearTarjetaGrafico() {
        List<YearMonth> meses = new ArrayList<>();
        List<Double> gastos = new ArrayList<>();
        double maximo = 0;

        for (int i = 5; i >= 0; i--) {
            YearMonth mes = mesActual.minusMonths(i);
            double gasto = gastosDelMes(mes);
            meses.add(mes);
            gastos.add(gasto);
            double alto = mes.equals(mesActual) ? Math.max(gasto, proyeccionTotal) : gasto;
            maximo = Math.max(maximo, alto);
        }

        Div grafico = new Div();
        grafico.setWidthFull();
        grafico.getStyle()
                .set("display", "flex")
                .set("align-items", "flex-end")
                .set("gap", "12px")
                .set("border-bottom", "2px solid #e5e7eb")
                .set("padding-top", "8px")
                .set("overflow-x", "auto");
        for (int i = 0; i < meses.size(); i++) {
            grafico.add(crearColumna(meses.get(i), gastos.get(i), maximo));
        }
        return new Tarjeta(crearTitulo("Gasto de los últimos 6 meses"), grafico, crearLeyenda());
    }

    private Div crearColumna(YearMonth mes, double gasto, double maximo) {
        boolean enCurso = mes.equals(mesActual);
        Span monto = new Span(enCurso ? formatoCorto(proyeccionTotal) : formatoCorto(gasto));
        monto.getStyle()
                .set("font-size", "0.8rem")
                .set("font-weight", "600")
                .set("margin-bottom", "4px")
                .set("white-space", "nowrap");

        Div columna = new Div(monto);
        columna.getStyle()
                .set("flex", "1 1 0")
                .set("min-width", "48px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("justify-content", "flex-end")
                .set("height", (ALTURA_GRAFICO + 60) + "px");

        Div barraGastada = crearBarra(gasto, maximo, false);
        if (enCurso) {
            double faltante = Math.max(0, proyeccionTotal - gasto);
            columna.add(crearBarra(faltante, maximo, true), barraGastada);
        } else {
            columna.add(barraGastada);
        }

        Span etiqueta = new Span(etiquetaMes(mes));
        etiqueta.getStyle().set("margin-top", "8px").set("font-size", "0.85rem");
        if (enCurso) {
            etiqueta.getStyle()
                    .set("font-weight", "700")
                    .set("color", "var(--color-acento, #2563eb)");
        } else {
            etiqueta.getStyle().set("color", "#6b7280");
        }
        columna.add(etiqueta);

        String ayuda = enCurso
                ? Utilidades.nombreMes(mes) + " (en curso): gastado "
                        + Utilidades.formatoDinero(gasto) + " hasta hoy. Proyección al cierre: "
                        + Utilidades.formatoDinero(proyeccionTotal)
                : Utilidades.nombreMes(mes) + ": "
                        + Utilidades.formatoDinero(gasto) + " en gastos";
        Tooltip.forComponent(columna).withText(ayuda).withPosition(Tooltip.TooltipPosition.TOP);
        return columna;
    }

    private Div crearBarra(double valor, double maximo, boolean proyectada) {
        Div barra = new Div();
        barra.setWidthFull();
        barra.getStyle()
                .set("max-width", "70px")
                .set("height", altura(valor, maximo) + "px")
                .set("background-color", proyectada
                        ? "transparent" : "var(--color-acento, #2563eb)")
                .set("border-radius", "6px 6px 0 0")
                .set("box-sizing", "border-box");
        if (proyectada) {
            barra.getStyle()
                    .set("opacity", "0.7")
                    .set("border", "2px dashed var(--color-acento, #2563eb)");
        }
        return barra;
    }

    private int altura(double valor, double maximo) {
        if (maximo <= 0 || valor <= 0) {
            return 0;
        }
        return Math.max(2, (int) Math.round(valor / maximo * ALTURA_GRAFICO));
    }

    private String etiquetaMes(YearMonth mes) {
        String corto = mes.getMonth().getDisplayName(TextStyle.SHORT, COLOMBIA).replace(".", "");
        corto = corto.substring(0, Math.min(3, corto.length()));
        corto = corto.substring(0, 1).toUpperCase() + corto.substring(1);
        return corto + " " + (mes.getYear() % 100);
    }

    private String formatoCorto(double valor) {
        if (valor >= 1_000_000) {
            return String.format(COLOMBIA, "$ %.1f M", valor / 1_000_000);
        }
        if (valor >= 1_000) {
            return String.format(COLOMBIA, "$ %.0f mil", valor / 1_000);
        }
        return "$ " + Math.round(valor);
    }

    private Div crearLeyenda() {
        double suma = 0;
        for (int i = 1; i <= 5; i++) {
            suma += gastosDelMes(mesActual.minusMonths(i));
        }

        Div muestras = new Div(
                crearItemLeyenda("Gastado", false),
                crearItemLeyenda("Proyección al cierre del mes en curso", true));
        muestras.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "20px");

        Span promedio = new Span("Promedio de los 5 meses anteriores: "
                + Utilidades.formatoDinero(suma / 5));
        promedio.getStyle()
                .set("display", "block")
                .set("margin-top", "8px")
                .set("color", "#6b7280");

        Div leyenda = new Div(muestras, promedio);
        leyenda.getStyle().set("margin-top", "16px");
        return leyenda;
    }

    private Div crearItemLeyenda(String texto, boolean proyectada) {
        Div cuadro = new Div();
        cuadro.getStyle()
                .set("width", "14px")
                .set("height", "14px")
                .set("box-sizing", "border-box")
                .set("background-color", proyectada
                        ? "transparent" : "var(--color-acento, #2563eb)");
        if (proyectada) {
            cuadro.getStyle()
                    .set("opacity", "0.7")
                    .set("border", "2px dashed var(--color-acento, #2563eb)");
        }
        Div item = new Div(cuadro, new Span(texto));
        item.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "8px")
                .set("font-size", "0.9rem");
        return item;
    }

    private Tarjeta crearTarjetaProyeccion() {
        Tarjeta tarjeta = new Tarjeta(
                crearTitulo("Proyección de cierre de " + Utilidades.nombreMes(mesActual)));
        Paragraph explicacion = new Paragraph("Hoy es el día " + diasTranscurridos
                + " de " + diasDelMes + ". Si sigues gastando al mismo ritmo diario que llevas, "
                + "así cerrará el mes.");
        explicacion.getStyle().set("color", "#6b7280").set("margin", "0 0 16px");

        double promedioDiario = gastadoMesActual / diasTranscurridos;
        Div indicadores = new Div(
                Utilidades.crearIndicador("Gastado hasta hoy",
                        new Span(Utilidades.formatoDinero(gastadoMesActual)), "#2563eb",
                        "Total de gastos del mes hasta hoy"),
                Utilidades.crearIndicador("Promedio diario",
                        new Span(Utilidades.formatoDinero(promedioDiario)), "#6b7280",
                        "Lo gastado hasta hoy dividido entre los días transcurridos"),
                Utilidades.crearIndicador("Proyección al cierre",
                        new Span(Utilidades.formatoDinero(proyeccionTotal)), "#f59e0b",
                        "Promedio diario multiplicado por los días del mes"));
        indicadores.setWidthFull();
        indicadores.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "16px")
                .set("margin-bottom", "16px");
        tarjeta.add(explicacion, indicadores);

        List<Presupuesto> delMes = presupuestosDelMes();
        if (delMes.isEmpty()) {
            tarjeta.add(crearMensajeSinPresupuestos());
            return tarjeta;
        }

        Div filas = new Div();
        filas.setWidthFull();
        filas.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(min(100%, 300px), 1fr))")
                .set("gap", "16px");
        List<String> superaran = new ArrayList<>();
        List<String> cercanas = new ArrayList<>();

        for (Presupuesto presupuesto : delMes) {
            double gastado = gastosCategoriaMes(mesActual, presupuesto.getCategoria());
            double proyeccion = proyectar(gastado);
            double porcentaje = proyeccion / presupuesto.getValorAsignado() * 100;
            String estado = Presupuesto.obtenerEstado(porcentaje);
            if (Presupuesto.SUPERADO.equals(estado)) {
                superaran.add(presupuesto.getCategoria());
            } else if (Presupuesto.ALERTA.equals(estado)) {
                cercanas.add(presupuesto.getCategoria());
            }
            filas.add(crearFilaProyeccion(presupuesto, gastado, proyeccion, porcentaje, estado));
        }
        tarjeta.add(crearAvisosProyeccion(superaran, cercanas), filas);
        return tarjeta;
    }

    private List<Presupuesto> presupuestosDelMes() {
        return presupuestos.stream()
                .filter(presupuesto -> presupuesto.getMes().equals(mesActual))
                .sorted(Comparator.comparingInt(presupuesto ->
                        Movimiento.CATEGORIAS_GASTO.indexOf(presupuesto.getCategoria())))
                .toList();
    }

    private Div crearMensajeSinPresupuestos() {
        Span texto = new Span("No hay presupuestos para este mes, así que no se puede comparar "
                + "la proyección. Crea los presupuestos para ver qué categorías están en riesgo.");
        texto.getStyle().set("color", "#6b7280");
        Button botonIr = new Button("Ir a Presupuestos", VaadinIcon.WALLET.create(),
                evento -> UI.getCurrent().navigate(PresupuestosView.class));
        Utilidades.estiloPrincipal(botonIr);

        Div mensaje = new Div(texto, botonIr);
        mensaje.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "flex-start")
                .set("gap", "12px");
        return mensaje;
    }

    private Div crearAvisosProyeccion(List<String> superaran, List<String> cercanas) {
        Div avisos = new Div();
        avisos.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "flex-start")
                .set("gap", "8px")
                .set("margin-bottom", "16px");
        if (!superaran.isEmpty()) {
            avisos.add(Utilidades.crearAviso(VaadinIcon.EXCLAMATION_CIRCLE,
                    "A este ritmo superarás el presupuesto en: " + String.join(", ", superaran),
                    "#fee2e2", "#991b1b"));
        }
        if (!cercanas.isEmpty()) {
            avisos.add(Utilidades.crearAviso(VaadinIcon.WARNING,
                    "A este ritmo quedarás por encima del 80 % en: " + String.join(", ", cercanas),
                    "#fef3c7", "#92400e"));
        }
        if (superaran.isEmpty() && cercanas.isEmpty()) {
            avisos.add(Utilidades.crearAviso(VaadinIcon.CHECK_CIRCLE,
                    "Vas bien: a este ritmo ninguna categoría superará su presupuesto",
                    "#dcfce7", "#166534"));
        }
        return avisos;
    }

    private Div crearFilaProyeccion(
            Presupuesto presupuesto, double gastado, double proyeccion,
            double porcentaje, String estado) {
        String color = Utilidades.colorDelEstado(estado);
        Span nombre = new Span(presupuesto.getCategoria());
        nombre.getStyle().set("font-weight", "600").set("font-size", "1.05rem");

        Span veredicto = new Span();
        veredicto.getStyle()
                .set("font-size", "0.85rem")
                .set("font-weight", "600")
                .set("color", color);
        if (Presupuesto.SUPERADO.equals(estado)) {
            veredicto.setText("Superará el presupuesto");
        } else if (Presupuesto.ALERTA.equals(estado)) {
            veredicto.setText("Quedará cerca del límite");
        } else {
            veredicto.setText("En orden");
        }

        HorizontalLayout cabecera = new HorizontalLayout(nombre, veredicto);
        cabecera.setWidthFull();
        cabecera.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        cabecera.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        cabecera.getStyle().set("flex-wrap", "wrap");

        Span detalle = new Span("Gastado: " + Utilidades.formatoDinero(gastado)
                + " · Proyección: " + Utilidades.formatoDinero(proyeccion)
                + " de " + Utilidades.formatoDinero(presupuesto.getValorAsignado())
                + " (" + Utilidades.formatoPorcentaje(porcentaje) + ")");
        detalle.getStyle()
                .set("display", "block")
                .set("color", "#6b7280")
                .set("font-size", "0.9rem");

        ProgressBar barra = new ProgressBar();
        barra.setWidthFull();
        barra.setValue(Math.min(Math.max(porcentaje / 100, 0), 1.0));
        barra.getStyle()
                .set("margin", "10px 0")
                .set("--vaadin-progress-bar-background", "#e5e7eb")
                .set("--vaadin-progress-bar-value-background", color);
        Tooltip.forComponent(barra)
                .withText("A este ritmo usarás el "
                        + Utilidades.formatoPorcentaje(porcentaje) + " del presupuesto")
                .withPosition(Tooltip.TooltipPosition.TOP);

        Div fila = new Div(cabecera, detalle, barra);
        fila.getStyle()
                .set("border", "1px solid #e5e7eb")
                .set("border-left", "5px solid " + color)
                .set("border-radius", "10px")
                .set("padding", "16px")
                .set("box-sizing", "border-box");

        if (!Presupuesto.NORMAL.equals(estado)) {
            double disponible = presupuesto.getValorAsignado() - gastado;
            int diasRestantes = diasDelMes - diasTranscurridos;
            Span consejo = new Span();
            consejo.getStyle()
                    .set("display", "block")
                    .set("font-weight", "600")
                    .set("font-size", "0.9rem");
            if (disponible <= 0) {
                consejo.setText("Ya superaste el presupuesto de este mes.");
                consejo.getStyle().set("color", "#991b1b");
                fila.add(consejo);
            } else if (diasRestantes > 0) {
                consejo.setText("Para no pasarte, gasta máximo "
                        + Utilidades.formatoDinero(disponible / diasRestantes)
                        + " al día en los " + diasRestantes + " días que quedan.");
                consejo.getStyle().set("color", "#92400e");
                fila.add(consejo);
            }
        }
        return fila;
    }
}
