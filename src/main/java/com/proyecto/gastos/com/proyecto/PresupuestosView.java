package com.proyecto.gastos.com.proyecto;

import com.proyecto.gastos.com.proyecto.archivos.GestorArchivos;
import com.proyecto.gastos.com.proyecto.modelo.Movimiento;
import com.proyecto.gastos.com.proyecto.modelo.Presupuesto;
import com.proyecto.gastos.com.proyecto.vistas.Tarjeta;
import com.proyecto.gastos.com.proyecto.vistas.Utilidades;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.io.IOException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Route(value = "presupuestos", layout = MainLayout.class)
@PageTitle("Presupuestos | Control de Gastos")
public class PresupuestosView extends VerticalLayout {

    private static final String ORDEN_CATEGORIA = "Por categoría";
    private static final String ORDEN_MAS_USADO = "Más usado primero";
    private static final String ORDEN_MENOS_USADO = "Menos usado primero";

    private final GestorArchivos gestor;
    private final Div listaTarjetas = new Div();
    private final Span resumen = new Span();
    private final Div avisos = new Div();
    private final HorizontalLayout filtrosEstado = new HorizontalLayout();
    private final Div mensajeVacio = new Div();
    private final ComboBox<YearMonth> selectorMes = new ComboBox<>("Mes");
    private final Select<String> selectorOrden = new Select<>();
    private final Dialog formulario = new Dialog();
    private final ComboBox<YearMonth> campoMes = new ComboBox<>("Mes");
    private final ComboBox<String> campoCategoria = new ComboBox<>("Categoría");
    private final NumberField campoValor = new NumberField("Valor asignado");
    private List<Movimiento> movimientos = new ArrayList<>();
    private List<Presupuesto> presupuestos = new ArrayList<>();
    private Presupuesto presupuestoEnEdicion;
    private String estadoFiltrado;

    public PresupuestosView(GestorArchivos gestor) {
        this.gestor = gestor;
        setSizeFull();
        setPadding(true);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background-color", "#f4f5f7");

        try {
            movimientos = gestor.cargarMovimientos();
            presupuestos = gestor.cargarPresupuestos();
        } catch (IOException e) {
            Utilidades.mostrarError("No se pudieron cargar los presupuestos: " + e.getMessage());
        }

        listaTarjetas.addClassName("budget-grid");
        prepararContenedores();
        configurarFormulario();
        configurarSelectorMes();
        configurarOrden();
        agregarEstilos();

        H2 heading = new H2("Presupuestos");
        heading.getStyle().set("font-size", "1.75rem").set("margin", "0");

        Paragraph description = new Paragraph("Define cuánto quieres gastar al mes en cada categoría.");
        description.getStyle().set("color", "#6b7280").set("margin", "4px 0 0");

        Button botonNuevo = new Button("Nuevo presupuesto", VaadinIcon.PLUS.create());
        Utilidades.estiloPrincipal(botonNuevo);
        botonNuevo.addClickListener(evento -> abrirFormulario(null));

        HorizontalLayout encabezado = new HorizontalLayout(heading, botonNuevo);
        encabezado.setWidthFull();
        encabezado.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        encabezado.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        encabezado.getStyle().set("flex-wrap", "wrap");

        HorizontalLayout controles = new HorizontalLayout(selectorMes, selectorOrden);
        controles.getStyle().set("flex-wrap", "wrap");

        VerticalLayout content = new VerticalLayout(encabezado, description, controles, resumen,
                avisos, filtrosEstado, new Tarjeta(listaTarjetas, mensajeVacio));
        content.setWidthFull();
        content.setMaxWidth("1400px");
        content.setPadding(false);
        content.setSpacing(true);
        content.getStyle().set("padding", "24px 0");
        add(content);

        renderizarPresupuestos();
    }

    private void prepararContenedores() {
        avisos.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "flex-start")
                .set("gap", "8px");
        filtrosEstado.getStyle().set("flex-wrap", "wrap");

        mensajeVacio.add(new Span("No hay presupuestos para este mes. Crea uno o copia los del mes anterior."));
        Button copiar = new Button("Copiar los del mes anterior", VaadinIcon.COPY.create());
        copiar.setTooltipText("Copia los presupuestos del mes anterior a este mes");
        copiar.addClickListener(evento -> copiarMesAnterior());
        mensajeVacio.add(copiar);
        mensajeVacio.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "flex-start")
                .set("gap", "12px");
    }

    private void configurarOrden() {
        selectorOrden.setLabel("Ordenar por");
        selectorOrden.setItems(ORDEN_CATEGORIA, ORDEN_MAS_USADO, ORDEN_MENOS_USADO);
        selectorOrden.setValue(ORDEN_CATEGORIA);
        selectorOrden.addValueChangeListener(evento -> renderizarPresupuestos());
    }

    private void agregarEstilos() {
        Element estilos = new Element("style");
        estilos.setText("""
                .budget-category-card {
                    transition: transform 0.2s ease, box-shadow 0.2s ease;
                }
                .budget-category-card:hover {
                    transform: translateY(-4px);
                    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12) !important;
                }
                @keyframes budget-fill {
                    from { transform: scaleX(0); }
                }
                .budget-category-card vaadin-progress-bar::part(value) {
                    transform-origin: left center;
                    animation: budget-fill 0.9s ease-out;
                }
                .budget-progress-bar { cursor: pointer; }
                """);
        getElement().appendChild(estilos);
    }

    private void configurarSelectorMes() {
        selectorMes.setItemLabelGenerator(Utilidades::nombreMes);
        actualizarMeses(elegirMesInicial());
        selectorMes.setPlaceholder("Selecciona el mes");
        selectorMes.addValueChangeListener(evento -> {
            estadoFiltrado = null;
            renderizarPresupuestos();
        });
    }

    private YearMonth elegirMesInicial() {
        YearMonth mesActual = YearMonth.now();
        return presupuestos.stream()
                .map(Presupuesto::getMes)
                .filter(mes -> mes.equals(mesActual))
                .findFirst()
                .orElseGet(() -> presupuestos.stream()
                        .map(Presupuesto::getMes)
                        .max(Comparator.naturalOrder())
                        .orElse(mesActual));
    }

    private void actualizarMeses(YearMonth mesSeleccionado) {
        List<YearMonth> meses = presupuestos.stream()
                .map(Presupuesto::getMes)
                .distinct()
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        YearMonth actual = YearMonth.now();
        if (!meses.contains(actual)) {
            meses.add(actual);
        }
        if (mesSeleccionado != null && !meses.contains(mesSeleccionado)) {
            meses.add(mesSeleccionado);
        }
        meses.sort(Comparator.reverseOrder());
        selectorMes.setItems(meses);
        if (mesSeleccionado != null) {
            selectorMes.setValue(mesSeleccionado);
        }
    }

    private void configurarFormulario() {
        List<YearMonth> meses = new ArrayList<>();
        YearMonth actual = YearMonth.now();
        for (int i = 3; i >= -12; i--) {
            meses.add(actual.plusMonths(i));
        }
        for (Presupuesto presupuesto : presupuestos) {
            if (!meses.contains(presupuesto.getMes())) {
                meses.add(presupuesto.getMes());
            }
        }
        campoMes.setItems(meses);
        campoMes.setItemLabelGenerator(Utilidades::nombreMes);
        campoMes.setRequiredIndicatorVisible(true);

        campoCategoria.setItems(Movimiento.CATEGORIAS_GASTO);
        campoCategoria.setRequiredIndicatorVisible(true);
        campoMes.addValueChangeListener(evento -> campoMes.setInvalid(false));
        campoCategoria.addValueChangeListener(evento -> campoCategoria.setInvalid(false));
        campoValor.setPrefixComponent(new Span("$"));
        campoValor.setMin(0);
        campoValor.setRequiredIndicatorVisible(true);
        campoValor.addValueChangeListener(evento -> campoValor.setInvalid(false));

        FormLayout formularioLayout = new FormLayout(campoMes, campoCategoria, campoValor);
        formularioLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        formulario.add(formularioLayout);
        formulario.setWidth("420px");
        formulario.setMaxWidth("95vw");

        Button cancelar = new Button("Cancelar", evento -> formulario.close());
        Button guardar = new Button("Guardar", evento -> guardarPresupuesto());
        Utilidades.estiloPrincipal(guardar);
        formulario.getFooter().add(cancelar, guardar);
    }

    private void renderizarResumen() {
        YearMonth mesSeleccionado = selectorMes.getValue();
        if (mesSeleccionado == null) {
            resumen.setText("Selecciona un mes para ver el resumen");
            return;
        }

        double totalAsignado = presupuestos.stream()
                .filter(p -> p.getMes().equals(mesSeleccionado))
                .mapToDouble(Presupuesto::getValorAsignado)
                .sum();
        double totalGastado = movimientos.stream()
                .filter(m -> Movimiento.GASTO.equals(m.getTipo()))
                .filter(m -> YearMonth.from(m.getFecha()).equals(mesSeleccionado))
                .mapToDouble(Movimiento::getValor)
                .sum();
        double disponible = totalAsignado - totalGastado;
        resumen.setText("Presupuestado: " + Utilidades.formatoDinero(totalAsignado)
                + " | Gastado: " + Utilidades.formatoDinero(totalGastado)
                + " | Disponible: " + Utilidades.formatoDinero(disponible));
    }

    private void renderizarPresupuestos() {
        renderizarResumen();
        listaTarjetas.removeAll();
        avisos.removeAll();
        filtrosEstado.removeAll();

        YearMonth mesSeleccionado = selectorMes.getValue();
        if (mesSeleccionado == null) {
            listaTarjetas.add(new Span("Selecciona un mes para ver tus presupuestos."));
            return;
        }

        List<Presupuesto> delMes = new ArrayList<>(presupuestos.stream()
                .filter(p -> p.getMes().equals(mesSeleccionado))
                .toList());
        ordenarPresupuestos(delMes);

        if (delMes.isEmpty()) {
            mensajeVacio.setVisible(true);
            listaTarjetas.setVisible(false);
            resumen.setVisible(false);
            avisos.setVisible(false);
            filtrosEstado.setVisible(false);
            selectorOrden.setVisible(false);
            return;
        }

        mensajeVacio.setVisible(false);
        listaTarjetas.setVisible(true);
        resumen.setVisible(true);
        avisos.setVisible(true);
        filtrosEstado.setVisible(true);
        selectorOrden.setVisible(true);

        List<String> categoriasSuperadas = new ArrayList<>();
        List<String> categoriasEnAlerta = new ArrayList<>();
        int cantidadEnOrden = 0;
        int cantidadMostrada = 0;
        for (Presupuesto presupuesto : delMes) {
            double gastado = presupuesto.calcularGastado(movimientos);
            double porcentaje = presupuesto.calcularPorcentaje(gastado);
            String estado = Presupuesto.obtenerEstado(porcentaje);
            if (Presupuesto.SUPERADO.equals(estado)) {
                categoriasSuperadas.add(presupuesto.getCategoria());
            } else if (Presupuesto.ALERTA.equals(estado)) {
                categoriasEnAlerta.add(presupuesto.getCategoria());
            } else {
                cantidadEnOrden++;
            }

            if (estadoFiltrado == null || estadoFiltrado.equals(estado)) {
                listaTarjetas.add(crearTarjetaPresupuesto(presupuesto, gastado, porcentaje, estado));
                cantidadMostrada++;
            }
        }

        if (cantidadMostrada == 0) {
            Span sinResultados = new Span("No hay presupuestos con ese estado en este mes.");
            sinResultados.getStyle().set("grid-column", "1 / -1").set("color", "#6b7280");
            listaTarjetas.add(sinResultados);
        }

        mostrarAvisos(categoriasSuperadas, categoriasEnAlerta, delMes.size());
        crearFiltrosEstado(delMes.size(), categoriasSuperadas.size(), categoriasEnAlerta.size(), cantidadEnOrden);
    }

    private void ordenarPresupuestos(List<Presupuesto> lista) {
        String orden = selectorOrden.getValue();
        Comparator<Presupuesto> porCategoria = Comparator.comparingInt(p -> {
            int indice = Movimiento.CATEGORIAS_GASTO.indexOf(p.getCategoria());
            return indice < 0 ? Integer.MAX_VALUE : indice;
        });
        if (ORDEN_MAS_USADO.equals(orden)) {
            lista.sort(Comparator.comparingDouble(this::porcentajeDe).reversed().thenComparing(porCategoria));
        } else if (ORDEN_MENOS_USADO.equals(orden)) {
            lista.sort(Comparator.comparingDouble(this::porcentajeDe).thenComparing(porCategoria));
        } else {
            lista.sort(porCategoria);
        }
    }

    private double porcentajeDe(Presupuesto presupuesto) {
        return presupuesto.calcularPorcentaje(presupuesto.calcularGastado(movimientos));
    }

    private void crearFiltrosEstado(int total, int superados, int enAlerta, int enOrden) {
        filtrosEstado.add(
                crearBotonFiltro("Todos (" + total + ")", null, "Mostrar todos los presupuestos"),
                crearBotonFiltro("Superados (" + superados + ")", Presupuesto.SUPERADO,
                        "Mostrar los presupuestos por encima del 100 %"),
                crearBotonFiltro("En alerta (" + enAlerta + ")", Presupuesto.ALERTA,
                        "Mostrar los presupuestos entre 80 % y 100 %"),
                crearBotonFiltro("En orden (" + enOrden + ")", Presupuesto.NORMAL,
                        "Mostrar los presupuestos hasta el 80 %"));
    }

    private Button crearBotonFiltro(String texto, String estado, String ayuda) {
        Button boton = new Button(texto);
        boton.setTooltipText(ayuda);
        boolean activo = estado == null ? estadoFiltrado == null : estado.equals(estadoFiltrado);
        if (activo) {
            Utilidades.estiloPrincipal(boton);
        } else {
            boton.getStyle().set("--vaadin-button-background", "#e5e7eb");
        }
        boton.addClickListener(evento -> {
            estadoFiltrado = estado;
            renderizarPresupuestos();
        });
        return boton;
    }

    private void mostrarAvisos(List<String> superados, List<String> enAlerta, int total) {
        if (!superados.isEmpty()) {
            avisos.add(crearAviso(VaadinIcon.EXCLAMATION_CIRCLE,
                    "Superadas: " + String.join(", ", superados), "#fee2e2", "#991b1b",
                    Presupuesto.SUPERADO));
        }
        if (!enAlerta.isEmpty()) {
            avisos.add(crearAviso(VaadinIcon.WARNING,
                    "Sobre el 80 %: " + String.join(", ", enAlerta), "#fef3c7", "#92400e",
                    Presupuesto.ALERTA));
        }
        if (superados.isEmpty() && enAlerta.isEmpty() && total > 0) {
            avisos.add(crearAviso(VaadinIcon.CHECK_CIRCLE,
                    "Todo en orden: ninguna categoría pasa del 80 %", "#dcfce7", "#166534", null));
        }
    }

    private Div crearAviso(VaadinIcon icono, String texto, String fondo, String color, String estado) {
        Div aviso = new Div(icono.create(), new Span(texto));
        aviso.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "10px")
                .set("padding", "10px 16px")
                .set("border-radius", "8px")
                .set("background-color", fondo)
                .set("color", color)
                .set("font-weight", "600");
        if (estado != null) {
            aviso.getStyle().set("cursor", "pointer");
            aviso.getElement().setAttribute("title", "Haz clic para filtrar estas categorías");
            aviso.addClickListener(evento -> {
                estadoFiltrado = estado;
                renderizarPresupuestos();
            });
        }
        return aviso;
    }

    private Div crearTarjetaPresupuesto(Presupuesto presupuesto, double gastado, double porcentaje,
            String estado) {
        String colorEstado = colorDelEstado(estado);
        Div tarjeta = new Div();
        tarjeta.addClassName("budget-category-card");
        tarjeta.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "12px")
                .set("border", "1px solid #e5e7eb")
                .set("border-left", "6px solid " + colorEstado)
                .set("padding", "16px")
                .set("box-sizing", "border-box")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.08)")
                .set("height", "100%");

        Span titulo = new Span(presupuesto.getCategoria());
        titulo.getStyle().set("font-weight", "700").set("font-size", "1.05rem");
        HorizontalLayout cabecera = new HorizontalLayout(titulo, crearBotonesAcciones(presupuesto));
        cabecera.setWidthFull();
        cabecera.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        cabecera.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Span detalleGastado = new Span("Gastado: " + Utilidades.formatoDinero(gastado) + " de "
                + Utilidades.formatoDinero(presupuesto.getValorAsignado())
                + " · " + Utilidades.formatoPorcentaje(porcentaje));
        detalleGastado.getStyle().set("color", "#6b7280").set("line-height", "1.3");
        double disponible = presupuesto.getValorAsignado() - gastado;
        Span detalleDisponible = new Span(disponible >= 0
                ? "Disponible: " + Utilidades.formatoDinero(disponible)
                : "Te pasaste por: " + Utilidades.formatoDinero(Math.abs(disponible)));
        detalleDisponible.getStyle()
                .set("color", disponible >= 0 ? "#166534" : "#b91c1c")
                .set("font-weight", "600");
        Div detalle = new Div(detalleGastado, detalleDisponible);
        detalle.getStyle().set("display", "grid").set("gap", "4px");

        ProgressBar barra = new ProgressBar(0, 1, Math.min(Math.max(porcentaje / 100, 0), 1));
        barra.setWidthFull();
        barra.addClassName("budget-progress-bar");
        barra.getStyle()
                .set("--vaadin-progress-bar-value-background", colorEstado)
                .set("--vaadin-progress-bar-background", "#e5e7eb")
                .set("margin", "8px 0 10px");
        barra.getElement().setAttribute("title", "Usado " + Utilidades.formatoPorcentaje(porcentaje)
                + "; haz clic para ver los gastos");
        barra.getElement().addEventListener("click", evento -> abrirDetalleGastos(presupuesto));

        String estadoTexto = Presupuesto.SUPERADO.equals(estado) ? "Superado"
                : Presupuesto.ALERTA.equals(estado) ? "Alerta" : "En orden";
        Span etiqueta = new Span(estadoTexto);
        etiqueta.getStyle()
                .set("display", "inline-flex")
                .set("padding", "4px 10px")
                .set("border-radius", "999px")
                .set("font-size", "0.8rem")
                .set("font-weight", "700")
                .set("background-color", Presupuesto.SUPERADO.equals(estado) ? "#fee2e2"
                        : Presupuesto.ALERTA.equals(estado) ? "#fef3c7" : "#dcfce7")
                .set("color", Presupuesto.SUPERADO.equals(estado) ? "#b91c1c"
                        : Presupuesto.ALERTA.equals(estado) ? "#b45309" : "#166534");
        etiqueta.getElement().setAttribute("title", "Estado del presupuesto: " + estadoTexto);

        tarjeta.add(cabecera, detalle, barra, etiqueta);
        return tarjeta;
    }

    private String colorDelEstado(String estado) {
        if (Presupuesto.SUPERADO.equals(estado)) {
            return "#dc2626";
        }
        if (Presupuesto.ALERTA.equals(estado)) {
            return "#f59e0b";
        }
        return "#16a34a";
    }

    private HorizontalLayout crearBotonesAcciones(Presupuesto presupuesto) {
        Button ver = Utilidades.crearBotonIcono(VaadinIcon.LIST.create(), "Ver los gastos de esta categoría");
        ver.addClickListener(evento -> abrirDetalleGastos(presupuesto));
        ver.setTooltipText("Ver los gastos de esta categoría");

        Button editar = Utilidades.crearBotonIcono(VaadinIcon.EDIT.create(), "Editar presupuesto");
        editar.addClickListener(evento -> abrirFormulario(presupuesto));
        editar.setTooltipText("Editar presupuesto");

        Icon iconoEliminar = VaadinIcon.TRASH.create();
        iconoEliminar.getStyle().set("color", "#dc2626");
        Button eliminar = Utilidades.crearBotonIcono(iconoEliminar, "Eliminar presupuesto");
        eliminar.addClickListener(evento -> confirmarEliminacion(presupuesto));
        eliminar.setTooltipText("Eliminar presupuesto");

        HorizontalLayout acciones = new HorizontalLayout(ver, editar, eliminar);
        acciones.setSpacing(false);
        acciones.setPadding(false);
        return acciones;
    }

    private void abrirDetalleGastos(Presupuesto presupuesto) {
        List<Movimiento> gastos = movimientos.stream()
                .filter(m -> Movimiento.GASTO.equals(m.getTipo()))
                .filter(m -> m.getCategoria().equals(presupuesto.getCategoria()))
                .filter(m -> YearMonth.from(m.getFecha()).equals(presupuesto.getMes()))
                .sorted(Comparator.comparing(Movimiento::getFecha).reversed())
                .toList();
        double gastado = presupuesto.calcularGastado(movimientos);
        double porcentaje = presupuesto.calcularPorcentaje(gastado);

        Dialog detalle = new Dialog();
        detalle.setHeaderTitle(presupuesto.getCategoria() + " · " + Utilidades.nombreMes(presupuesto.getMes()));
        VerticalLayout contenido = new VerticalLayout();
        contenido.setPadding(false);
        contenido.add(new Span("Gastado: " + Utilidades.formatoDinero(gastado)
                + " de " + Utilidades.formatoDinero(presupuesto.getValorAsignado())
                + " (" + Utilidades.formatoPorcentaje(porcentaje) + ")"));

        if (gastos.isEmpty()) {
            contenido.add(new Span("Todavía no hay gastos en esta categoría y mes."));
        } else {
            Grid<Movimiento> tabla = new Grid<>();
            tabla.addColumn(m -> m.getFecha().format(Utilidades.FORMATO_FECHA))
                    .setHeader("Fecha").setAutoWidth(true);
            tabla.addColumn(Movimiento::getDescripcion)
                    .setHeader("Descripción").setAutoWidth(true).setFlexGrow(1);
            tabla.addColumn(m -> Utilidades.formatoDinero(m.getValor()))
                    .setHeader("Valor").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);
            tabla.setItems(gastos);
            if (gastos.size() <= 6) {
                tabla.setAllRowsVisible(true);
            } else {
                tabla.setHeight("300px");
            }
            contenido.add(tabla);
        }
        detalle.add(contenido);
        Button cerrar = new Button("Cerrar", evento -> detalle.close());
        Button irAMovimientos = new Button("Ir a Movimientos", VaadinIcon.EXCHANGE.create(), evento -> {
            detalle.close();
            UI.getCurrent().navigate(MovimientosView.class);
        });
        Utilidades.estiloPrincipal(irAMovimientos);
        detalle.getFooter().add(cerrar, irAMovimientos);
        detalle.open();
    }

    private void copiarMesAnterior() {
        YearMonth mes = selectorMes.getValue();
        if (mes == null) {
            Utilidades.mostrarError("Selecciona un mes antes de copiar presupuestos");
            return;
        }
        YearMonth anterior = mes.minusMonths(1);
        List<Presupuesto> delMesAnterior = presupuestos.stream()
                .filter(p -> p.getMes().equals(anterior))
                .toList();
        if (delMesAnterior.isEmpty()) {
            Utilidades.mostrarError("No hay presupuestos en " + Utilidades.nombreMes(anterior) + " para copiar");
            return;
        }

        List<Presupuesto> originales = new ArrayList<>(presupuestos);
        for (Presupuesto presupuesto : delMesAnterior) {
            presupuestos.add(new Presupuesto(mes, presupuesto.getCategoria(), presupuesto.getValorAsignado()));
        }
        try {
            gestor.guardarPresupuestos(presupuestos);
            estadoFiltrado = null;
            renderizarPresupuestos();
            Utilidades.mostrarExito(delMesAnterior.size() + " presupuestos copiados de "
                    + Utilidades.nombreMes(anterior));
        } catch (IOException | IllegalArgumentException e) {
            presupuestos = originales;
            Utilidades.mostrarError("No se pudieron copiar los presupuestos: " + e.getMessage());
        }
    }

    private void abrirFormulario(Presupuesto presupuesto) {
        presupuestoEnEdicion = presupuesto;
        campoMes.setReadOnly(presupuesto != null);
        campoCategoria.setReadOnly(presupuesto != null);
        campoMes.setInvalid(false);
        if (presupuesto == null) {
            formulario.setHeaderTitle("Nuevo presupuesto");
            campoMes.setValue(selectorMes.getValue());
            campoCategoria.clear();
            campoValor.clear();
        } else {
            formulario.setHeaderTitle("Editar presupuesto");
            campoMes.setValue(presupuesto.getMes());
            campoCategoria.setValue(presupuesto.getCategoria());
            campoValor.setValue(presupuesto.getValorAsignado());
        }
        campoCategoria.setInvalid(false);
        campoValor.setInvalid(false);
        formulario.open();
    }

    private void guardarPresupuesto() {
        boolean mesInvalido = campoMes.getValue() == null;
        boolean categoriaInvalida = campoCategoria.getValue() == null || campoCategoria.getValue().isBlank();
        boolean valorInvalido = campoValor.getValue() == null || !Double.isFinite(campoValor.getValue())
                || campoValor.getValue() <= 0;
        campoMes.setErrorMessage("Debes elegir un mes");
        campoMes.setInvalid(mesInvalido);
        campoCategoria.setErrorMessage("Debes elegir una categoría");
        campoCategoria.setInvalid(categoriaInvalida);
        campoValor.setErrorMessage("El presupuesto debe ser mayor que cero");
        campoValor.setInvalid(valorInvalido);
        if (mesInvalido || categoriaInvalida || valorInvalido) {
            return;
        }

        YearMonth mes = campoMes.getValue();

        boolean categoriaDuplicada = presupuestos.stream()
                .anyMatch(p -> p != presupuestoEnEdicion
                        && p.getMes().equals(mes)
                        && p.getCategoria().equals(campoCategoria.getValue()));
        if (categoriaDuplicada) {
            campoCategoria.setErrorMessage("Ya existe un presupuesto para esta categoría y este mes");
            campoCategoria.setInvalid(true);
            return;
        }

        Presupuesto nuevo = new Presupuesto(mes, campoCategoria.getValue(), campoValor.getValue());
        try {
            nuevo.validar();
        } catch (IllegalArgumentException e) {
            Utilidades.mostrarError(e.getMessage());
            return;
        }

        List<Presupuesto> originales = new ArrayList<>(presupuestos);
        if (presupuestoEnEdicion == null) {
            presupuestos.add(nuevo);
        } else {
            int indice = presupuestos.indexOf(presupuestoEnEdicion);
            if (indice < 0) {
                Utilidades.mostrarError("El presupuesto ya no está disponible. Recarga la página.");
                return;
            }
            presupuestos.set(indice, nuevo);
        }

        try {
            gestor.guardarPresupuestos(presupuestos);
            formulario.close();
            estadoFiltrado = null;
            actualizarMeses(nuevo.getMes());
            renderizarPresupuestos();
            Utilidades.mostrarExito(presupuestoEnEdicion == null ? "Presupuesto creado" : "Presupuesto actualizado");
        } catch (IOException | IllegalArgumentException e) {
            presupuestos = originales;
            Utilidades.mostrarError("No se pudo guardar el presupuesto: " + e.getMessage());
        }
    }

    private void confirmarEliminacion(Presupuesto presupuesto) {
        ConfirmDialog dialogo = new ConfirmDialog();
        dialogo.setHeader("Eliminar presupuesto");
        dialogo.setText("¿Seguro que quieres eliminar el presupuesto de " + presupuesto.getCategoria()
                + " para " + Utilidades.nombreMes(presupuesto.getMes()) + "?");
        dialogo.setCancelable(true);
        dialogo.setCancelText("Cancelar");
        dialogo.setConfirmText("Eliminar");
        dialogo.setConfirmButtonTheme("error primary");
        dialogo.addConfirmListener(evento -> eliminarPresupuesto(presupuesto));
        dialogo.open();
    }

    private void eliminarPresupuesto(Presupuesto presupuesto) {
        List<Presupuesto> originales = new ArrayList<>(presupuestos);
        if (!presupuestos.remove(presupuesto)) {
            Utilidades.mostrarError("El presupuesto ya no está disponible. Recarga la página.");
            return;
        }

        try {
            gestor.guardarPresupuestos(presupuestos);
            actualizarMeses(selectorMes.getValue());
            renderizarPresupuestos();
            Utilidades.mostrarExito("Presupuesto eliminado");
        } catch (IOException | IllegalArgumentException e) {
            presupuestos = originales;
            Utilidades.mostrarError("No se pudo eliminar el presupuesto: " + e.getMessage());
        }
    }
}
