package com.proyecto.gastos.com.proyecto.modelo;

import java.time.YearMonth;
import java.util.List;
import java.util.Locale;

public class Presupuesto {

    public static final String NORMAL = "NORMAL";
    public static final String ALERTA = "ALERTA";
    public static final String SUPERADO = "SUPERADO";

    private YearMonth mes;
    private String categoria;
    private double valorAsignado;

    public Presupuesto(YearMonth mes, String categoria, double valorAsignado) {
        this.mes = mes;
        this.categoria = categoria;
        this.valorAsignado = valorAsignado;
    }

    public YearMonth getMes() {
        return mes;
    }

    public void setMes(YearMonth mes) {
        this.mes = mes;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getValorAsignado() {
        return valorAsignado;
    }

    public void setValorAsignado(double valorAsignado) {
        this.valorAsignado = valorAsignado;
    }

    public void validar() {
        if (mes == null) {
            throw new IllegalArgumentException("Debes indicar el mes");
        }
        if (categoria == null || !Movimiento.CATEGORIAS_GASTO.contains(categoria)) {
            throw new IllegalArgumentException("Debes elegir una categoría de gasto válida");
        }
        if (!Double.isFinite(valorAsignado) || valorAsignado <= 0) {
            throw new IllegalArgumentException("El presupuesto debe ser un número mayor que cero");
        }
    }

    public double calcularGastado(List<Movimiento> movimientos) {
        double total = 0;
        for (Movimiento movimiento : movimientos) {
            if (Movimiento.GASTO.equals(movimiento.getTipo())
                    && movimiento.getCategoria().equals(categoria)
                    && YearMonth.from(movimiento.getFecha()).equals(mes)) {
                total += movimiento.getValor();
            }
        }
        return total;
    }

    public double calcularPorcentaje(double gastado) {
        if (valorAsignado <= 0) {
            return 0;
        }
        return gastado / valorAsignado * 100;
    }

    public static String obtenerEstado(double porcentaje) {
        if (porcentaje > 100) {
            return SUPERADO;
        }
        if (porcentaje > 80) {
            return ALERTA;
        }
        return NORMAL;
    }

    public String aLinea() {
        validar();
        return mes + "|" + categoria + "|" + String.format(Locale.US, "%.2f", valorAsignado);
    }

    public static Presupuesto desdeLinea(String linea) {
        String[] partes = linea.split("\\|", -1);
        if (partes.length != 3) {
            throw new IllegalArgumentException("Se esperaban 3 campos y hay " + partes.length);
        }
        Presupuesto presupuesto = new Presupuesto(
                YearMonth.parse(partes[0].trim()),
                partes[1].trim(),
                Double.parseDouble(partes[2].trim()));
        presupuesto.validar();
        return presupuesto;
    }
}
