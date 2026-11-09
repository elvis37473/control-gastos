package com.proyecto.gastos.com.proyecto.vistas;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;

public class Tarjeta extends Div {

    public Tarjeta(Component... contenido) {
        setWidthFull();
        getStyle()
                .set("background-color", Utilidades.SUPERFICIE)
                .set("border", "1px solid " + Utilidades.BORDE)
                .set("border-radius", "14px")
                .set("box-shadow", "0 1px 2px rgba(15, 23, 42, 0.04), 0 4px 12px rgba(15, 23, 42, 0.04)")
                .set("padding", "20px")
                .set("box-sizing", "border-box");
        add(contenido);
    }
}
