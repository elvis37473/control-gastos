package com.proyecto.gastos.com.proyecto;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "presupuestos", layout = MainLayout.class)
@PageTitle("Presupuestos | Control de Gastos")
public class PresupuestosView extends VerticalLayout {

    public PresupuestosView() {
        setSizeFull();
        setPadding(true);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background-color", "#f4f5f7");

        H2 heading = new H2("Presupuestos");
        heading.getStyle()
                .set("font-size", "1.75rem")
                .set("margin", "0");

        Paragraph description = new Paragraph("Define cuánto quieres gastar al mes en cada categoría.");
        description.getStyle()
                .set("color", "#6b7280")
                .set("margin", "4px 0 0");

        Span emptyStateTitle = new Span("Tus presupuestos estarán aquí.");
        emptyStateTitle.getStyle()
                .set("font-weight", "600")
                .set("font-size", "1.125rem");

        Paragraph emptyStateMessage = new Paragraph(
                "Crea presupuestos por categoría y sigue cuánto llevas gastado.");
        emptyStateMessage.getStyle()
                .set("color", "#6b7280")
                .set("margin", "4px 0 0");

        Div card = new Div(emptyStateTitle, emptyStateMessage);
        card.setWidthFull();
        card.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "12px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.15)")
                .set("padding", "24px")
                .set("box-sizing", "border-box");

        VerticalLayout content = new VerticalLayout(heading, description, card);
        content.setWidthFull();
        content.setMaxWidth("1100px");
        content.setPadding(false);
        content.setSpacing(true);
        content.getStyle().set("padding", "24px 0");
        add(content);
    }
}
