package com.proyecto.gastos.com.proyecto;

import com.proyecto.gastos.com.proyecto.archivos.GestorArchivos;
import com.proyecto.gastos.com.proyecto.modelo.Movimiento;
import com.proyecto.gastos.com.proyecto.modelo.Presupuesto;
import com.proyecto.gastos.com.proyecto.vistas.Tarjeta;
import com.proyecto.gastos.com.proyecto.vistas.Utilidades;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
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
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Movimientos | Control de Gastos")
public class MovimientosView extends VerticalLayout {

    private final GestorArchivos gestor;
    private List<Movimiento> movimientos = new ArrayList<>();
    private List<Presupuesto> presupuestos = new ArrayList<>();

    private final Grid<Movimiento> tabla = new Grid<>();
    private final TextField campoBusqueda = new TextField("Buscar");
    private final Select<String> filtroTipo = new Select<>();
    private final ComboBox<YearMonth> filtroMes = new ComboBox<>("Mes");
    private final ComboBox<String> filtroCategoria = new ComboBox<>("Categoría");
    private final Span resumen = new Span();

    private final Span valorIngresos = new Span();
    private final Span valorGastos = new Span();
    private final Span valorSaldo = new Span();
    private final Span valorCantidad = new Span();

    private final Div estadoVacio = new Div();
    private final Span tituloVacio = new Span();
    private final Span detalleVacio = new Span();

    private final Dialog formulario = new Dialog();
    private final Select<String> campoTipo = new Select<>();
    private final NumberField campoValor = new NumberField("Valor");
    private final DatePicker campoFecha = new DatePicker("Fecha");
    private final ComboBox<String> campoCategoria = new ComboBox<>("Categoría");
    private final TextField campoDescripcion = new TextField("Descripción");

    private Movimiento movimientoEnEdicion;

    public MovimientosView(GestorArchivos gestor) {
        this.gestor = gestor;
        setSizeFull();
        setPadding(true);
        getStyle().set("background-color", Utilidades.FONDO);

        try {
            movimientos = gestor.cargarMovimientos();
            presupuestos = gestor.cargarPresupuestos();
        } catch (IOException e) {
            Utilidades.mostrarError("No se pudieron cargar los movimientos: " + e.getMessage());
        }

        configurarTabla();
        configurarFormulario();
        configurarFiltros();
        configurarEstadoVacio();
        actualizarMeses();
        aplicarFiltros();

        Tarjeta tarjetaTabla = new Tarjeta(crearBarraFiltros(), resumen, tabla, estadoVacio);
        add(Utilidades.crearPagina(crearEncabezado(), crearTarjetasResumen(), tarjetaTabla));

        addAttachListener(evento -> {
            VaadinSession session = evento.getUI().getSession();
            if (Boolean.TRUE.equals(
                    session.getAttribute(ReportesView.ABRIR_FORMULARIO_NUEVO))) {
                session.setAttribute(ReportesView.ABRIR_FORMULARIO_NUEVO, null);
                abrirFormulario(null);
            }
        });
    }

    private HorizontalLayout crearEncabezado() {
        H2 titulo = new H2("Movimientos");
        titulo.getStyle()
                .set("margin", "0")
                .set("color", Utilidades.TEXTO);

        Paragraph subtitulo = new Paragraph("Registra y consulta tus ingresos y gastos.");
        subtitulo.getStyle()
                .set("margin", "4px 0 0")
                .set("color", Utilidades.TEXTO_SUAVE);

        Button botonNuevo = new Button("Nuevo movimiento", VaadinIcon.PLUS.create());
        Utilidades.estiloPrincipal(botonNuevo);
        Utilidades.aplicarTooltip(botonNuevo, "Registra un ingreso o un gasto");
        botonNuevo.addClickListener(evento -> abrirFormulario(null));

        HorizontalLayout encabezado =
                new HorizontalLayout(new Div(titulo, subtitulo), botonNuevo);
        encabezado.setWidthFull();
        encabezado.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        encabezado.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        encabezado.getStyle().set("flex-wrap", "wrap");
        return encabezado;
    }

    private Div crearTarjetasResumen() {
        Div tarjetas = new Div(
                Utilidades.crearTarjetaEstadistica(VaadinIcon.ARROW_UP, "Total de ingresos",
                        valorIngresos, "#16a34a",
                        "Suma de los ingresos que se ven con los filtros actuales"),
                Utilidades.crearTarjetaEstadistica(VaadinIcon.ARROW_DOWN, "Total de gastos",
                        valorGastos, "#dc2626",
                        "Suma de los gastos que se ven con los filtros actuales"),
                Utilidades.crearTarjetaEstadistica(VaadinIcon.WALLET, "Balance disponible",
                        valorSaldo, "#2563eb", "Ingresos menos gastos"),
                Utilidades.crearTarjetaEstadistica(VaadinIcon.LIST, "Movimientos",
                        valorCantidad, "#7c3aed", "Cantidad de movimientos que se ven en la tabla"));
        tarjetas.setWidthFull();
        tarjetas.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(210px, 1fr))")
                .set("gap", "16px");
        return tarjetas;
    }

    private HorizontalLayout crearBarraFiltros() {
        Button botonLimpiar = new Button("Limpiar filtros", VaadinIcon.CLOSE_SMALL.create());
        botonLimpiar.getStyle().set("background-color", "transparent");
        Utilidades.aplicarTooltip(botonLimpiar, "Quita la búsqueda y todos los filtros");
        botonLimpiar.addClickListener(evento -> limpiarFiltros());

        HorizontalLayout barra = new HorizontalLayout(
                campoBusqueda, filtroTipo, filtroMes, filtroCategoria, botonLimpiar);
        barra.setWidthFull();
        barra.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        barra.getStyle()
                .set("flex-wrap", "wrap")
                .set("margin-bottom", "8px");

        campoBusqueda.getStyle().set("flex", "2 1 260px");
        filtroTipo.getStyle().set("flex", "1 1 150px");
        filtroMes.getStyle().set("flex", "1 1 170px");
        filtroCategoria.getStyle().set("flex", "1 1 170px");

        resumen.getStyle()
                .set("display", "block")
                .set("margin", "4px 0 12px")
                .set("font-size", "0.9rem")
                .set("color", Utilidades.TEXTO_SUAVE);
        return barra;
    }

    private void configurarEstadoVacio() {
        Icon icono = VaadinIcon.SEARCH.create();
        icono.setSize("32px");
        icono.getStyle().set("color", Utilidades.TEXTO_SUAVE);

        tituloVacio.getStyle()
                .set("font-weight", "600")
                .set("color", Utilidades.TEXTO);
        detalleVacio.getStyle().set("color", Utilidades.TEXTO_SUAVE);

        Button botonLimpiar = new Button("Limpiar filtros", evento -> limpiarFiltros());

        estadoVacio.add(icono, tituloVacio, detalleVacio, botonLimpiar);
        estadoVacio.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("gap", "8px")
                .set("padding", "40px 16px")
                .set("text-align", "center");
        estadoVacio.setVisible(false);
    }

    private void configurarTabla() {
        tabla.addComponentColumn(movimiento -> Utilidades.crearEtiquetaTipo(movimiento.getTipo()))
                .setHeader("Tipo")
                .setAutoWidth(true)
                .setFlexGrow(0);
        tabla.addColumn(movimiento -> movimiento.getFecha().format(Utilidades.FORMATO_FECHA))
                .setHeader("Fecha")
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
        tabla.addComponentColumn(this::crearBotonesAcciones)
                .setHeader("Acciones")
                .setAutoWidth(true)
                .setFlexGrow(0);
        tabla.setWidthFull();
        tabla.setHeight("55vh");
        tabla.setMinHeight("320px");
    }

    private HorizontalLayout crearBotonesAcciones(Movimiento movimiento) {
        Button botonEditar =
                Utilidades.crearBotonIcono(VaadinIcon.EDIT.create(), "Editar movimiento");
        botonEditar.addClickListener(evento -> abrirFormulario(movimiento));

        Icon iconoBorrar = VaadinIcon.TRASH.create();
        iconoBorrar.getStyle().set("color", "#dc2626");
        Button botonEliminar =
                Utilidades.crearBotonIcono(iconoBorrar, "Eliminar movimiento");
        botonEliminar.addClickListener(evento -> confirmarEliminacion(movimiento));

        HorizontalLayout acciones = new HorizontalLayout(botonEditar, botonEliminar);
        acciones.setSpacing(false);
        return acciones;
    }

    private void configurarFiltros() {
        campoBusqueda.setPlaceholder("Descripción, categoría, valor o fecha...");
        campoBusqueda.setPrefixComponent(VaadinIcon.SEARCH.create());
        campoBusqueda.setClearButtonVisible(true);
        campoBusqueda.setValueChangeMode(ValueChangeMode.EAGER);
        campoBusqueda.addValueChangeListener(evento -> aplicarFiltros());

        filtroTipo.setLabel("Tipo");
        filtroTipo.setItems(Movimiento.INGRESO, Movimiento.GASTO);
        filtroTipo.setItemLabelGenerator(Utilidades::nombreTipo);
        filtroTipo.setEmptySelectionAllowed(true);
        filtroTipo.setEmptySelectionCaption("Todos");
        filtroTipo.addValueChangeListener(evento -> aplicarFiltros());

        filtroMes.setItemLabelGenerator(Utilidades::nombreMes);
        filtroMes.setPlaceholder("Todos");
        filtroMes.setClearButtonVisible(true);
        filtroMes.addValueChangeListener(evento -> aplicarFiltros());

        List<String> categorias = new ArrayList<>(Movimiento.CATEGORIAS_GASTO);
        categorias.addAll(Movimiento.CATEGORIAS_INGRESO);
        filtroCategoria.setItems(categorias);
        filtroCategoria.setPlaceholder("Todas");
        filtroCategoria.setClearButtonVisible(true);
        filtroCategoria.addValueChangeListener(evento -> aplicarFiltros());
    }

    private void limpiarFiltros() {
        campoBusqueda.clear();
        filtroTipo.clear();
        filtroMes.clear();
        filtroCategoria.clear();
        aplicarFiltros();
    }

    private void actualizarMeses() {
        List<YearMonth> meses = movimientos.stream()
                .map(movimiento -> YearMonth.from(movimiento.getFecha()))
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();

        YearMonth seleccionado = filtroMes.getValue();
        filtroMes.setItems(meses);
        if (seleccionado != null && meses.contains(seleccionado)) {
            filtroMes.setValue(seleccionado);
        } else if (seleccionado != null) {
            filtroMes.clear();
        }
    }

    private void aplicarFiltros() {
        String texto = campoBusqueda.getValue() == null
                ? ""
                : campoBusqueda.getValue().trim().toLowerCase(Locale.ROOT);
        List<Movimiento> filtrados = new ArrayList<>();
        double totalIngresos = 0;
        double totalGastos = 0;

        for (Movimiento movimiento : movimientos) {
            if (!cumpleFiltros(movimiento, texto)) {
                continue;
            }
            filtrados.add(movimiento);
            if (Movimiento.INGRESO.equals(movimiento.getTipo())) {
                totalIngresos += movimiento.getValor();
            } else {
                totalGastos += movimiento.getValor();
            }
        }

        filtrados.sort(Comparator.comparing(Movimiento::getFecha)
                .thenComparingInt(Movimiento::getId)
                .reversed());
        tabla.setItems(filtrados);

        double saldo = totalIngresos - totalGastos;
        valorIngresos.setText(Utilidades.formatoDinero(totalIngresos));
        valorGastos.setText(Utilidades.formatoDinero(totalGastos));
        valorSaldo.setText(Utilidades.formatoDinero(saldo));
        valorSaldo.getStyle().set("color", saldo >= 0 ? "#166534" : "#991b1b");
        valorCantidad.setText(String.valueOf(filtrados.size()));
        resumen.setText(filtrados.size() == 1
                ? "1 movimiento"
                : filtrados.size() + " movimientos");

        boolean vacio = filtrados.isEmpty();
        tabla.setVisible(!vacio);
        estadoVacio.setVisible(vacio);
        if (vacio) {
            if (movimientos.isEmpty()) {
                tituloVacio.setText("Todavía no tienes movimientos");
                detalleVacio.setText("Pulsa “Nuevo movimiento” para registrar el primero.");
            } else {
                tituloVacio.setText("No encontramos movimientos");
                detalleVacio.setText("Prueba con otra búsqueda o quita algún filtro.");
            }
        }
    }

    private boolean cumpleFiltros(Movimiento movimiento, String texto) {
        String tipo = filtroTipo.getValue();
        if (tipo != null && !tipo.equals(movimiento.getTipo())) {
            return false;
        }
        YearMonth mes = filtroMes.getValue();
        if (mes != null && !mes.equals(YearMonth.from(movimiento.getFecha()))) {
            return false;
        }
        String categoria = filtroCategoria.getValue();
        if (categoria != null && !categoria.equals(movimiento.getCategoria())) {
            return false;
        }
        if (texto.isEmpty()) {
            return true;
        }

        String contenido = (movimiento.getDescripcion() + " "
                + movimiento.getCategoria() + " "
                + Utilidades.formatoDinero(movimiento.getValor()) + " "
                + movimiento.getFecha() + " "
                + movimiento.getFecha().format(Utilidades.FORMATO_FECHA))
                .toLowerCase(Locale.ROOT);
        return contenido.contains(texto);
    }

    private void configurarFormulario() {
        campoTipo.setLabel("Tipo");
        campoTipo.setItems(Movimiento.INGRESO, Movimiento.GASTO);
        campoTipo.setItemLabelGenerator(Utilidades::nombreTipo);
        campoTipo.addValueChangeListener(evento -> actualizarCategoriasDelFormulario());

        campoValor.setPrefixComponent(new Span("$"));
        campoValor.setMin(0);
        campoValor.setPlaceholder("0");
        campoFecha.setLocale(Locale.forLanguageTag("es-CO"));
        campoCategoria.setPlaceholder("Elige una categoría");
        campoDescripcion.setPlaceholder("Ej.: Mercado del mes");
        campoDescripcion.setMaxLength(60);
        campoDescripcion.setHelperText("Máximo 60 caracteres");

        FormLayout disposicion = new FormLayout(
                campoTipo, campoValor, campoFecha, campoCategoria, campoDescripcion);
        disposicion.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("460px", 2));
        disposicion.setColspan(campoDescripcion, 2);

        formulario.add(disposicion);
        formulario.setWidth("520px");
        formulario.setMaxWidth("95vw");

        Button botonCancelar = new Button("Cancelar", evento -> formulario.close());
        Button botonGuardar = new Button("Guardar", evento -> guardarMovimiento());
        Utilidades.estiloPrincipal(botonGuardar);
        formulario.getFooter().add(botonCancelar, botonGuardar);
    }

    private void actualizarCategoriasDelFormulario() {
        if (Movimiento.INGRESO.equals(campoTipo.getValue())) {
            campoCategoria.setItems(Movimiento.CATEGORIAS_INGRESO);
        } else {
            campoCategoria.setItems(Movimiento.CATEGORIAS_GASTO);
        }
        campoCategoria.clear();
    }

    private void abrirFormulario(Movimiento movimiento) {
        movimientoEnEdicion = movimiento;
        limpiarErrores();

        if (movimiento == null) {
            formulario.setHeaderTitle("Nuevo movimiento");
            campoTipo.setValue(Movimiento.GASTO);
            campoValor.clear();
            campoFecha.setValue(LocalDate.now());
            campoCategoria.clear();
            campoDescripcion.clear();
        } else {
            formulario.setHeaderTitle("Editar movimiento");
            campoTipo.setValue(movimiento.getTipo());
            campoValor.setValue(movimiento.getValor());
            campoFecha.setValue(movimiento.getFecha());
            campoCategoria.setValue(movimiento.getCategoria());
            campoDescripcion.setValue(movimiento.getDescripcion());
        }
        formulario.open();
    }

    private void limpiarErrores() {
        campoTipo.setInvalid(false);
        campoValor.setInvalid(false);
        campoFecha.setInvalid(false);
        campoCategoria.setInvalid(false);
        campoDescripcion.setInvalid(false);
    }

    private boolean formularioEsValido() {
        limpiarErrores();
        boolean valido = true;

        if (campoTipo.getValue() == null) {
            campoTipo.setInvalid(true);
            campoTipo.setErrorMessage("Elige el tipo de movimiento");
            valido = false;
        }
        if (campoValor.getValue() == null || !Double.isFinite(campoValor.getValue())
                || campoValor.getValue() <= 0) {
            campoValor.setInvalid(true);
            campoValor.setErrorMessage("Escribe un valor mayor que cero");
            valido = false;
        }
        if (campoFecha.getValue() == null) {
            campoFecha.setInvalid(true);
            campoFecha.setErrorMessage("Elige la fecha");
            valido = false;
        }
        if (campoCategoria.getValue() == null || campoCategoria.getValue().isBlank()) {
            campoCategoria.setInvalid(true);
            campoCategoria.setErrorMessage("Elige una categoría");
            valido = false;
        }
        if (campoDescripcion.getValue() == null
                || campoDescripcion.getValue().isBlank()
                || campoDescripcion.getValue().length() > 60) {
            campoDescripcion.setInvalid(true);
            campoDescripcion.setErrorMessage("Escribe una descripción de hasta 60 caracteres");
            valido = false;
        }
        return valido;
    }

    private void guardarMovimiento() {
        if (!formularioEsValido()) {
            Utilidades.mostrarError("Revisa los campos marcados en rojo");
            return;
        }

        boolean esNuevo = movimientoEnEdicion == null;
        int id = esNuevo
                ? gestor.siguienteIdMovimiento(movimientos)
                : movimientoEnEdicion.getId();
        Movimiento movimientoNuevo = new Movimiento(id, campoTipo.getValue(),
                campoValor.getValue(), campoFecha.getValue(),
                campoCategoria.getValue(), campoDescripcion.getValue().trim());

        try {
            movimientoNuevo.validar();
        } catch (IllegalArgumentException e) {
            Utilidades.mostrarError(e.getMessage());
            return;
        }

        List<Movimiento> originales = new ArrayList<>(movimientos);
        if (esNuevo) {
            movimientos.add(movimientoNuevo);
        } else {
            int posicion = movimientos.indexOf(movimientoEnEdicion);
            if (posicion < 0) {
                Utilidades.mostrarError("El movimiento ya no está disponible. Recarga la página.");
                return;
            }
            movimientos.set(posicion, movimientoNuevo);
        }

        try {
            gestor.guardarMovimientos(movimientos);
            formulario.close();
            actualizarMeses();
            aplicarFiltros();
            Utilidades.mostrarExito(
                    esNuevo ? "Movimiento registrado" : "Movimiento actualizado");
            comprobarAlertaPresupuesto(movimientoNuevo);
        } catch (IOException | IllegalArgumentException e) {
            movimientos = originales;
            aplicarFiltros();
            Utilidades.mostrarError("No se pudo guardar el movimiento: " + e.getMessage());
        }
    }

    private void comprobarAlertaPresupuesto(Movimiento movimiento) {
        if (!Movimiento.GASTO.equals(movimiento.getTipo())) {
            return;
        }
        YearMonth mes = YearMonth.from(movimiento.getFecha());
        Presupuesto presupuesto = presupuestos.stream()
                .filter(p -> p.getMes().equals(mes))
                .filter(p -> p.getCategoria().equals(movimiento.getCategoria()))
                .findFirst()
                .orElse(null);
        if (presupuesto == null) {
            return;
        }
        double gastado = presupuesto.calcularGastado(movimientos);
        double porcentaje = presupuesto.calcularPorcentaje(gastado);
        String estado = Presupuesto.obtenerEstado(porcentaje);
        if (Presupuesto.ALERTA.equals(estado)) {
            Utilidades.mostrarAdvertencia("¡Alerta! Este gasto lleva a "
                    + Utilidades.formatoPorcentaje(porcentaje)
                    + " del presupuesto de " + presupuesto.getCategoria() + ".");
        } else if (Presupuesto.SUPERADO.equals(estado)) {
            Utilidades.mostrarError("¡Presupuesto superado! Ya llevas "
                    + Utilidades.formatoPorcentaje(porcentaje)
                    + " del límite de " + presupuesto.getCategoria() + ".");
        }
    }

    private void confirmarEliminacion(Movimiento movimiento) {
        ConfirmDialog dialogo = new ConfirmDialog();
        dialogo.setHeader("Eliminar movimiento");
        dialogo.setText("¿Seguro que quieres eliminar “" + movimiento.getDescripcion()
                + "” por " + Utilidades.formatoDinero(movimiento.getValor())
                + "? Esta acción no se puede deshacer.");
        dialogo.setCancelable(true);
        dialogo.setCancelText("Cancelar");
        dialogo.setConfirmText("Eliminar");
        dialogo.setConfirmButtonTheme("error primary");
        dialogo.addConfirmListener(evento -> eliminarMovimiento(movimiento));
        dialogo.open();
    }

    private void eliminarMovimiento(Movimiento movimiento) {
        List<Movimiento> originales = new ArrayList<>(movimientos);
        if (!movimientos.remove(movimiento)) {
            Utilidades.mostrarError("El movimiento ya no está disponible. Recarga la página.");
            return;
        }

        try {
            gestor.guardarMovimientos(movimientos);
            actualizarMeses();
            aplicarFiltros();
            Utilidades.mostrarExito("Movimiento eliminado");
        } catch (IOException | IllegalArgumentException e) {
            movimientos = originales;
            aplicarFiltros();
            Utilidades.mostrarError("No se pudo eliminar el movimiento: " + e.getMessage());
        }
    }
}
