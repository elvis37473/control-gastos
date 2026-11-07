package com.proyecto.gastos.com.proyecto;

import com.proyecto.gastos.com.proyecto.archivos.GestorArchivos;
import com.proyecto.gastos.com.proyecto.modelo.Movimiento;
import com.proyecto.gastos.com.proyecto.vistas.Tarjeta;
import com.proyecto.gastos.com.proyecto.vistas.Utilidades;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Route(value = "reportes", layout = MainLayout.class)
@PageTitle("Reportes | Control de Gastos")
public class ReportesView extends VerticalLayout {

    static final String ABRIR_FORMULARIO_NUEVO = "abrirFormularioNuevo";
    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final GestorArchivos gestor;
    private final List<Movimiento> movimientos;
    private final List<YearMonth> mesesDisponibles = new ArrayList<>();
    private final ComboBox<YearMonth> filtroMes = new ComboBox<>("Mes");
    private final ComboBox<String> filtroCategoria = new ComboBox<>("Categoría");
    private final Span valorIngresos = new Span();
    private final Span valorGastos = new Span();
    private final Span valorSaldo = new Span();
    private final Div listaCategorias = new Div();
    private final H3 tituloMovimientos = new H3();
    private final Grid<Movimiento> tabla = new Grid<>();

    private String textoDescarga = "";
    private String nombreDescarga = "reporte.txt";
    private Anchor enlaceDescarga;

    public ReportesView(GestorArchivos gestor) {
        this.gestor = gestor;
        setSizeFull();
        setPadding(true);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background-color", "#f4f5f7");

        List<Movimiento> cargados;
        try {
            cargados = gestor.cargarMovimientos();
        } catch (IOException e) {
            Utilidades.mostrarError("No se pudieron cargar los movimientos: " + e.getMessage());
            cargados = List.of();
        }
        movimientos = cargados;

        calcularMesesDisponibles();
        configurarFiltros();
        configurarTabla();
        crearEnlaceDescarga();
        filtroMes.setValue(elegirMesInicial());

        VerticalLayout contenido = new VerticalLayout(
                crearEncabezado(),
                new Tarjeta(crearBarraFiltros(), crearTarjetasResumen(),
                        crearTitulo("Gastos por categoría"), listaCategorias,
                        tituloMovimientos, tabla),
                enlaceDescarga);
        contenido.setWidthFull();
        contenido.setMaxWidth("1200px");
        contenido.setPadding(false);
        contenido.setSpacing(true);
        add(contenido);
        actualizarPantalla();
    }

    private HorizontalLayout crearEncabezado() {
        H2 titulo = new H2("Reportes");
        titulo.getStyle().set("margin", "0");

        Paragraph subtitulo = new Paragraph("Consulta cómo se ha usado tu dinero cada mes.");
        subtitulo.getStyle().set("margin", "0").set("color", "#6b7280");

        HorizontalLayout encabezado = new HorizontalLayout(
                new Div(titulo, subtitulo), crearMenu());
        encabezado.setWidthFull();
        encabezado.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        encabezado.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        encabezado.getStyle().set("flex-wrap", "wrap");
        return encabezado;
    }

    private H3 crearTitulo(String texto) {
        H3 titulo = new H3(texto);
        titulo.getStyle().set("margin", "24px 0 12px");
        return titulo;
    }

    private MenuBar crearMenu() {
        MenuBar menu = new MenuBar();

        MenuItem registrar = crearItemConIcono(menu, VaadinIcon.PLUS_CIRCLE, "Registrar movimiento");
        registrar.addClickListener(evento -> irARegistrarMovimiento());

        MenuItem exportar = crearItemConIcono(menu, VaadinIcon.DOWNLOAD, "Exportar reporte");
        SubMenu opcionesExportar = exportar.getSubMenu();
        opcionesExportar.addItem("Con el filtro de categoría actual",
                evento -> exportarReporte(true));
        opcionesExportar.addItem("Del mes completo", evento -> exportarReporte(false));

        MenuItem cambiarMes = crearItemConIcono(menu, VaadinIcon.CALENDAR, "Cambiar de mes");
        SubMenu opcionesMes = cambiarMes.getSubMenu();
        opcionesMes.addItem("Mes anterior", evento -> moverMes(-1));
        opcionesMes.addItem("Mes siguiente", evento -> moverMes(1));
        opcionesMes.addItem("Mes actual", evento -> filtroMes.setValue(YearMonth.now()));
        MenuItem separador = opcionesMes.addItem("────────────────");
        separador.setEnabled(false);
        for (YearMonth mes : mesesDisponibles) {
            opcionesMes.addItem(Utilidades.nombreMes(mes), evento -> filtroMes.setValue(mes));
        }
        return menu;
    }

    private MenuItem crearItemConIcono(MenuBar menu, VaadinIcon tipoIcono, String texto) {
        Icon icono = tipoIcono.create();
        icono.setSize("16px");
        Span contenido = new Span(icono, new Span(texto));
        contenido.getStyle()
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("gap", "8px");
        return menu.addItem(contenido);
    }

    private HorizontalLayout crearBarraFiltros() {
        HorizontalLayout barra = new HorizontalLayout(filtroMes, filtroCategoria);
        barra.setWidthFull();
        barra.getStyle().set("flex-wrap", "wrap");
        return barra;
    }

    private Div crearTarjetasResumen() {
        Div tarjetas = new Div(
                Utilidades.crearIndicador("Total ingresos", valorIngresos, "#16a34a",
                        "Todo el dinero que entró en el mes"),
                Utilidades.crearIndicador("Total gastos", valorGastos, "#dc2626",
                        "Todo el dinero que salió en el mes"),
                Utilidades.crearIndicador("Saldo disponible", valorSaldo, "#2563eb",
                        "Ingresos menos gastos del mes"));
        tarjetas.setWidthFull();
        tarjetas.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "16px")
                .set("margin-top", "16px");
        return tarjetas;
    }

    private void configurarFiltros() {
        filtroMes.setItems(mesesDisponibles);
        filtroMes.setItemLabelGenerator(Utilidades::nombreMes);
        filtroMes.setWidth("260px");
        filtroMes.setMaxWidth("100%");
        filtroMes.addValueChangeListener(evento -> actualizarPantalla());

        List<String> categorias = new ArrayList<>(Movimiento.CATEGORIAS_GASTO);
        categorias.addAll(Movimiento.CATEGORIAS_INGRESO);
        filtroCategoria.setItems(categorias);
        filtroCategoria.setPlaceholder("Todas");
        filtroCategoria.setClearButtonVisible(true);
        filtroCategoria.setWidth("260px");
        filtroCategoria.setMaxWidth("100%");
        filtroCategoria.addValueChangeListener(evento -> actualizarPantalla());
    }

    private void configurarTabla() {
        tabla.addColumn(movimiento -> movimiento.getFecha().format(Utilidades.FORMATO_FECHA))
                .setHeader("Fecha")
                .setAutoWidth(true);
        tabla.addColumn(movimiento -> Utilidades.nombreTipo(movimiento.getTipo()))
                .setHeader("Tipo")
                .setAutoWidth(true);
        tabla.addColumn(Movimiento::getCategoria)
                .setHeader("Categoría")
                .setAutoWidth(true);
        tabla.addColumn(Movimiento::getDescripcion)
                .setHeader("Descripción")
                .setAutoWidth(true)
                .setFlexGrow(1);
        tabla.addColumn(movimiento -> Utilidades.formatoDinero(movimiento.getValor()))
                .setHeader("Valor")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.END);
        tabla.setWidthFull();
        tabla.setHeight("480px");
        tabla.setEmptyStateText("No hay movimientos para mostrar en este mes.");
    }

    private void calcularMesesDisponibles() {
        mesesDisponibles.addAll(movimientos.stream()
                .map(movimiento -> YearMonth.from(movimiento.getFecha()))
                .distinct()
                .toList());
        YearMonth actual = YearMonth.now();
        if (!mesesDisponibles.contains(actual)) {
            mesesDisponibles.add(actual);
        }
        mesesDisponibles.sort(Comparator.reverseOrder());
    }

    private YearMonth elegirMesInicial() {
        YearMonth actual = YearMonth.now();
        if (movimientos.stream()
                .anyMatch(movimiento -> YearMonth.from(movimiento.getFecha()).equals(actual))) {
            return actual;
        }
        return movimientos.stream()
                .map(movimiento -> YearMonth.from(movimiento.getFecha()))
                .max(Comparator.naturalOrder())
                .orElse(actual);
    }

    private void moverMes(int cantidad) {
        YearMonth mesActual = filtroMes.getValue();
        int indice = mesesDisponibles.indexOf(mesActual);
        int destino = indice - cantidad;
        if (indice >= 0 && destino >= 0 && destino < mesesDisponibles.size()) {
            filtroMes.setValue(mesesDisponibles.get(destino));
        } else {
            Notification.show("No hay más meses en esa dirección", 3000,
                    Notification.Position.TOP_CENTER);
        }
    }

    private List<Movimiento> movimientosDelMes(YearMonth mes, String categoria) {
        return movimientos.stream()
                .filter(movimiento -> YearMonth.from(movimiento.getFecha()).equals(mes))
                .filter(movimiento -> categoria == null
                        || categoria.equals(movimiento.getCategoria()))
                .sorted(Comparator.comparing(Movimiento::getFecha)
                        .thenComparingInt(Movimiento::getId)
                        .reversed())
                .toList();
    }

    private double sumar(List<Movimiento> lista, String tipo) {
        return lista.stream()
                .filter(movimiento -> tipo.equals(movimiento.getTipo()))
                .mapToDouble(Movimiento::getValor)
                .sum();
    }

    private double sumarCategoria(List<Movimiento> lista, String categoria) {
        return lista.stream()
                .filter(movimiento -> Movimiento.GASTO.equals(movimiento.getTipo()))
                .filter(movimiento -> categoria.equals(movimiento.getCategoria()))
                .mapToDouble(Movimiento::getValor)
                .sum();
    }

    private void actualizarPantalla() {
        YearMonth mes = filtroMes.getValue();
        if (mes == null) {
            return;
        }

        String categoria = filtroCategoria.getValue();
        List<Movimiento> delMes = movimientosDelMes(mes, null);
        double ingresos = sumar(delMes, Movimiento.INGRESO);
        double gastos = sumar(delMes, Movimiento.GASTO);
        double saldo = ingresos - gastos;

        valorIngresos.setText(Utilidades.formatoDinero(ingresos));
        valorGastos.setText(Utilidades.formatoDinero(gastos));
        valorSaldo.setText(Utilidades.formatoDinero(saldo));
        valorSaldo.getStyle().set("color", saldo >= 0 ? "#166534" : "#991b1b");

        mostrarGastosPorCategoria(delMes, gastos, categoria);

        List<Movimiento> detalle = movimientosDelMes(mes, categoria);
        tabla.setItems(detalle);
        String titulo = "Movimientos de " + Utilidades.nombreMes(mes)
                + " (" + detalle.size() + ")";
        if (categoria != null) {
            titulo += " · " + categoria;
        }
        tituloMovimientos.setText(titulo);
        tituloMovimientos.getStyle().set("margin", "24px 0 12px");
    }

    private void mostrarGastosPorCategoria(
            List<Movimiento> delMes, double gastos, String categoriaFiltro) {
        listaCategorias.removeAll();
        listaCategorias.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "14px");

        boolean hayFilas = false;
        for (String nombre : Movimiento.CATEGORIAS_GASTO) {
            double total = sumarCategoria(delMes, nombre);
            boolean mostrar = categoriaFiltro == null || categoriaFiltro.equals(nombre);
            if (mostrar && total > 0) {
                listaCategorias.add(crearFilaCategoria(nombre, total, gastos));
                hayFilas = true;
            }
        }

        if (!hayFilas) {
            Span vacio = new Span("No hay gastos para mostrar con estos filtros.");
            vacio.getStyle().set("color", "#6b7280");
            listaCategorias.add(vacio);
        }
    }

    private Div crearFilaCategoria(String nombre, double total, double gastosDelMes) {
        double parte = total / gastosDelMes;
        Span nombreCategoria = new Span(nombre);
        nombreCategoria.getStyle().set("font-weight", "600");

        Span montos = new Span(Utilidades.formatoDinero(total) + "  ·  "
                + Utilidades.formatoPorcentaje(parte * 100));
        montos.getStyle().set("color", "#6b7280");

        HorizontalLayout textos = new HorizontalLayout(nombreCategoria, montos);
        textos.setWidthFull();
        textos.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        ProgressBar barra = new ProgressBar();
        barra.setWidthFull();
        barra.setValue(parte);
        barra.getStyle()
                .set("--vaadin-progress-bar-background", "#e5e7eb")
                .set("--vaadin-progress-bar-value-background", "var(--color-acento, #2563eb)");
        Tooltip.forComponent(barra)
                .withText(nombre + " es el " + Utilidades.formatoPorcentaje(parte * 100)
                        + " de los gastos del mes")
                .withPosition(Tooltip.TooltipPosition.TOP);
        return new Div(textos, barra);
    }

    private void irARegistrarMovimiento() {
        VaadinSession.getCurrent().setAttribute(ABRIR_FORMULARIO_NUEVO, Boolean.TRUE);
        UI.getCurrent().navigate(MovimientosView.class);
    }

    private void crearEnlaceDescarga() {
        enlaceDescarga = new Anchor(DownloadHandler.fromInputStream(evento -> {
            byte[] contenido = textoDescarga.getBytes(StandardCharsets.UTF_8);
            return new DownloadResponse(new ByteArrayInputStream(contenido), nombreDescarga,
                    "text/plain; charset=utf-8", contenido.length);
        }), "Descargar reporte");
        enlaceDescarga.getStyle().set("display", "none");
    }

    private void exportarReporte(boolean aplicarFiltroCategoria) {
        YearMonth mes = filtroMes.getValue();
        String categoria = aplicarFiltroCategoria ? filtroCategoria.getValue() : null;
        String texto = crearTextoReporte(mes, categoria);
        String nombre = "reporte-" + mes + (categoria == null ? "" : "-filtrado") + ".txt";
        boolean guardado = gestor.guardarReporte(nombre, texto);

        textoDescarga = texto;
        nombreDescarga = nombre;
        enlaceDescarga.getElement().executeJs("this.click()");

        if (guardado) {
            Utilidades.mostrarExito("Reporte exportado: " + nombre);
        } else {
            Utilidades.mostrarError(
                    "Se descargó el reporte, pero no se pudo guardar la copia en exportaciones");
        }
    }

    private String crearTextoReporte(YearMonth mes, String categoria) {
        List<Movimiento> delMes = movimientosDelMes(mes, null);
        double ingresos = sumar(delMes, Movimiento.INGRESO);
        double gastos = sumar(delMes, Movimiento.GASTO);
        String linea = "=".repeat(70);
        String separador = "-".repeat(70);
        StringBuilder texto = new StringBuilder();

        texto.append(linea).append('\n')
                .append("REPORTE MENSUAL - CONTROL DE GASTOS PERSONALES\n")
                .append(linea).append('\n')
                .append("Mes:       ").append(Utilidades.nombreMes(mes)).append('\n')
                .append("Categoría: ").append(categoria == null ? "Todas" : categoria).append('\n')
                .append("Generado:  ").append(LocalDateTime.now().format(FORMATO_HORA))
                .append("\n\n")
                .append("RESUMEN DEL MES\n").append(separador).append('\n')
                .append(String.format("%-22s %18s\n", "Total ingresos:",
                        Utilidades.formatoDinero(ingresos)))
                .append(String.format("%-22s %18s\n", "Total gastos:",
                        Utilidades.formatoDinero(gastos)))
                .append(String.format("%-22s %18s\n\n", "Saldo disponible:",
                        Utilidades.formatoDinero(ingresos - gastos)))
                .append("GASTOS POR CATEGORÍA\n").append(separador).append('\n');

        double totalListado = 0;
        for (String nombre : Movimiento.CATEGORIAS_GASTO) {
            double total = sumarCategoria(delMes, nombre);
            if ((categoria == null || categoria.equals(nombre)) && total > 0) {
                double porcentaje = gastos == 0 ? 0 : total / gastos * 100;
                texto.append(String.format("%-20s %18s %10s\n", nombre,
                        Utilidades.formatoDinero(total),
                        Utilidades.formatoPorcentaje(porcentaje)));
                totalListado += total;
            }
        }
        if (totalListado == 0) {
            texto.append("(No hay gastos para mostrar)\n");
        } else {
            texto.append(String.format("%-20s %18s\n", "TOTAL",
                    Utilidades.formatoDinero(totalListado)));
        }

        List<Movimiento> detalle = movimientosDelMes(mes, categoria);
        texto.append("\nMOVIMIENTOS (").append(detalle.size()).append(")\n")
                .append(separador).append('\n');
        if (detalle.isEmpty()) {
            texto.append("(No hay movimientos para mostrar)\n");
        }
        for (Movimiento movimiento : detalle) {
            texto.append(String.format("%-11s %-8s %-16s %16s  %s\n",
                    movimiento.getFecha().format(Utilidades.FORMATO_FECHA),
                    Utilidades.nombreTipo(movimiento.getTipo()),
                    movimiento.getCategoria(),
                    Utilidades.formatoDinero(movimiento.getValor()),
                    movimiento.getDescripcion()));
        }
        texto.append(linea).append('\n');
        return texto.toString().replace("\n", System.lineSeparator());
    }

}
