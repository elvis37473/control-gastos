package com.proyecto.gastos.com.proyecto;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Movimientos | Control de Gastos")
public class MovimientosView extends VerticalLayout {

    public MovimientosView() {
        setSizeFull();
        setPadding(true);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background-color", "#f4f5f7");

        VerticalLayout content = createPageContent(
                "Movimientos",
                "Registra y consulta tus ingresos y gastos.",
                "Aquí aparecerán tus movimientos.",
                "Cuando agregues un ingreso o un gasto, podrás consultarlo en esta sección.");
        add(content);
    }

    private VerticalLayout createPageContent(String title, String subtitle, String emptyTitle,
            String emptyMessage) {
        H2 heading = new H2(title);
        heading.getStyle()
                .set("font-size", "1.75rem")
                .set("margin", "0");

        Paragraph description = new Paragraph(subtitle);
        description.getStyle()
                .set("color", "#6b7280")
                .set("margin", "4px 0 0");

        Span emptyStateTitle = new Span(emptyTitle);
        emptyStateTitle.getStyle()
                .set("font-weight", "600")
                .set("font-size", "1.125rem");

        Paragraph emptyStateMessage = new Paragraph(emptyMessage);
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

        VerticalLayout page = new VerticalLayout(heading, description, card);
        page.setWidthFull();
        page.setMaxWidth("1100px");
        page.setPadding(false);
        page.setSpacing(true);
        page.getStyle().set("padding", "24px 0");
        return page;
    }
}
