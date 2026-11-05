package com.proyecto.gastos.com.proyecto.modelo;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class Movimiento {

    public static final String INGRESO = "INGRESO";
    public static final String GASTO = "GASTO";

    public static final List<String> CATEGORIAS_GASTO = List.of(
            "Alimentación", "Transporte", "Vivienda", "Servicios",
            "Entretenimiento", "Salud", "Educación");

    public static final List<String> CATEGORIAS_INGRESO = List.of(
            "Salario", "Ingreso extra");

    private int id;
    private String tipo;
    private double valor;
    private LocalDate fecha;
    private String categoria;
    private String descripcion;

    public Movimiento(int id, String tipo, double valor, LocalDate fecha,
            String categoria, String descripcion) {
        this.id = id;
        this.tipo = tipo;
        this.valor = valor;
        this.fecha = fecha;
        this.categoria = categoria;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void validar() {
        if (id <= 0) {
            throw new IllegalArgumentException("El id debe ser mayor que cero");
        }
        if (!INGRESO.equals(tipo) && !GASTO.equals(tipo)) {
            throw new IllegalArgumentException("El tipo debe ser INGRESO o GASTO");
        }
        if (!Double.isFinite(valor) || valor <= 0) {
            throw new IllegalArgumentException("El valor debe ser un número mayor que cero");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("Debes indicar la fecha");
        }
        if (categoria == null || categoria.isBlank()) {
            throw new IllegalArgumentException("Debes elegir una categoría");
        }
        List<String> categoriasPermitidas = INGRESO.equals(tipo)
                ? CATEGORIAS_INGRESO
                : CATEGORIAS_GASTO;
        if (!categoriasPermitidas.contains(categoria)) {
            throw new IllegalArgumentException("La categoría no corresponde al tipo de movimiento");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("Debes escribir una descripción");
        }
        if (descripcion.length() > 60) {
            throw new IllegalArgumentException("La descripción no puede pasar de 60 caracteres");
        }
        if (descripcion.indexOf('\n') >= 0 || descripcion.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("La descripción no puede contener saltos de línea");
        }
    }

    public String aLinea() {
        validar();
        return id + "|" + tipo + "|" + String.format(Locale.US, "%.2f", valor) + "|"
                + fecha + "|" + categoria + "|" + descripcion.replace("|", "/");
    }

    public static Movimiento desdeLinea(String linea) {
        String[] partes = linea.split("\\|", -1);
        if (partes.length != 6) {
            throw new IllegalArgumentException("Se esperaban 6 campos y hay " + partes.length);
        }
        Movimiento movimiento = new Movimiento(
                Integer.parseInt(partes[0].trim()),
                partes[1].trim(),
                Double.parseDouble(partes[2].trim()),
                LocalDate.parse(partes[3].trim()),
                partes[4].trim(),
                partes[5].trim());
        movimiento.validar();
        return movimiento;
    }
}
