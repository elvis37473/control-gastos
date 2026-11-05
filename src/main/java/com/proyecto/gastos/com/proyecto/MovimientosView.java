package com.proyecto.gastos.com.proyecto;

import com.proyecto.gastos.com.proyecto.archivos.GestorArchivos;
import com.proyecto.gastos.com.proyecto.modelo.Movimiento;
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

    private final Grid<Movimiento> tabla = new Grid<>();
    private final TextField campoBusqueda = new TextField("Buscar");
    private final Select<String> filtroTipo = new Select<>();
    private final ComboBox<YearMonth> filtroMes = new ComboBox<>("Mes");
    private final ComboBox<String> filtroCategoria = new ComboBox<>("Categoría");
    private final Span resumen = new Span();

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
        getStyle().set("background-color", "#f4f5f7");

        try {
            movimientos = gestor.cargarMovimientos();
        } catch (IOException e) {
            Utilidades.mostrarError("No se pudieron cargar los movimientos: " + e.getMessage());
        }

        configurarTabla();
        configurarFormulario();
        configurarFiltros();
        actualizarMeses();
        aplicarFiltros();

        VerticalLayout contenido = new VerticalLayout(crearEncabezado(),
                new Tarjeta(crearBarraFiltros(), resumen, tabla));
        contenido.setWidthFull();
        contenido.setMaxWidth("1200px");
        contenido.setPadding(false);
        contenido.setSpacing(true);
        add(contenido);
    }

    private HorizontalLayout crearEncabezado() {
        H2 titulo = new H2("Movimientos");
        titulo.getStyle().set("margin", "0");

        Paragraph subtitulo = new Paragraph("Registra y consulta tus ingresos y gastos.");
        subtitulo.getStyle()
                .set("margin", "0")
                .set("color", "#6b7280");

        Button botonNuevo = new Button("Nuevo movimiento", VaadinIcon.PLUS.create());
        Utilidades.estiloPrincipal(botonNuevo);
        botonNuevo.addClickListener(evento -> abrirFormulario(null));

        HorizontalLayout encabezado =
                new HorizontalLayout(new Div(titulo, subtitulo), botonNuevo);
        encabezado.setWidthFull();
        encabezado.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        encabezado.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        encabezado.getStyle().set("flex-wrap", "wrap");
        return encabezado;
    }

    private HorizontalLayout crearBarraFiltros() {
        HorizontalLayout barra =
                new HorizontalLayout(campoBusqueda, filtroTipo, filtroMes, filtroCategoria);
        barra.setWidthFull();
        barra.getStyle()
                .set("flex-wrap", "wrap")
                .set("margin-bottom", "8px");

        campoBusqueda.getStyle().set("flex", "2 1 260px");
        filtroTipo.getStyle().set("flex", "1 1 150px");
        filtroMes.getStyle().set("flex", "1 1 170px");
        filtroCategoria.getStyle().set("flex", "1 1 170px");

        resumen.getStyle()
                .set("display", "block")
                .set("margin-bottom", "12px")
                .set("color", "#6b7280");
        return barra;
    }

    private void configurarTabla() {
        tabla.addComponentColumn(this::crearEtiquetaTipo)
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
        tabla.setHeight("480px");
    }

    private Span crearEtiquetaTipo(Movimiento movimiento) {
        Span etiqueta = new Span(Utilidades.nombreTipo(movimiento.getTipo()));
        etiqueta.getStyle()
                .set("padding", "2px 10px")
                .set("border-radius", "999px")
                .set("font-size", "0.8rem")
                .set("font-weight", "600");
        if (Movimiento.INGRESO.equals(movimiento.getTipo())) {
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
        resumen.setText(filtrados.size() + " movimientos  |  Ingresos: "
                + Utilidades.formatoDinero(totalIngresos)
                + "  |  Gastos: " + Utilidades.formatoDinero(totalGastos)
                + "  |  Saldo: " + Utilidades.formatoDinero(totalIngresos - totalGastos));
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
        campoFecha.setLocale(Locale.forLanguageTag("es-CO"));
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
        } catch (IOException | IllegalArgumentException e) {
            movimientos = originales;
            aplicarFiltros();
            Utilidades.mostrarError("No se pudo guardar el movimiento: " + e.getMessage());
        }
    }

    private void confirmarEliminacion(Movimiento movimiento) {
        ConfirmDialog dialogo = new ConfirmDialog();
        dialogo.setHeader("Eliminar movimiento");
        dialogo.setText("¿Seguro que quieres eliminar \"" + movimiento.getDescripcion()
                + "\" por " + Utilidades.formatoDinero(movimiento.getValor())
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
