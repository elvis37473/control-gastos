package com.proyecto.gastos.com.proyecto.vistas;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;

public class Tarjeta extends Div {

    public Tarjeta(Component... contenido) {
        setWidthFull();
        getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "12px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.15)")
                .set("border-top", "4px solid var(--color-acento, #2563eb)")
                .set("padding", "24px")
                .set("box-sizing", "border-box");
        add(contenido);
    }
}
