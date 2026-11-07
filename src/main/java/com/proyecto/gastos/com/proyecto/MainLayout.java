package com.proyecto.gastos.com.proyecto;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.dom.Element;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createNavigation();
        agregarEstilos();
    }

    private void createHeader() {
        DrawerToggle drawerToggle = new DrawerToggle();
        drawerToggle.getElement().setAttribute("aria-label", "Abrir menú");

        Icon logo = VaadinIcon.WALLET.create();
        logo.getStyle().set("color", "#2563eb");

        H1 title = new H1("Control de Gastos");
        title.getStyle()
                .set("font-size", "1.25rem")
                .set("font-weight", "600")
                .set("margin", "0");

        HorizontalLayout header = new HorizontalLayout(drawerToggle, logo, title);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        addToNavbar(header);
    }

    private void createNavigation() {
        Span label = new Span("MENÚ PRINCIPAL");
        label.getStyle()
                .set("font-size", "0.75rem")
                .set("font-weight", "600")
                .set("color", "#6b7280")
                .set("padding", "16px 16px 0 16px");

        SideNav navigation = new SideNav();
        navigation.getStyle().set("padding", "8px");
        navigation.addItem(new SideNavItem("Movimientos", MovimientosView.class,
                VaadinIcon.EXCHANGE.create()));
        navigation.addItem(new SideNavItem("Presupuestos", PresupuestosView.class,
                VaadinIcon.WALLET.create()));
        navigation.addItem(new SideNavItem("Reportes", ReportesView.class,
                VaadinIcon.CHART.create()));
        navigation.addItem(new SideNavItem("Análisis", AnalisisView.class,
                VaadinIcon.BAR_CHART.create()));

        addToDrawer(label, navigation);
    }

    private void agregarEstilos() {
        Element style = new Element("style");
        style.setText("""
                .budget-progress-bar { width: 100%; }
                .budget-progress-bar::part(bar) { border-radius: 999px; }
                .budget-grid {
                    display: grid;
                    grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
                    gap: 16px;
                    width: 100%;
                }
                """);
        getElement().appendChild(style);
    }
}
