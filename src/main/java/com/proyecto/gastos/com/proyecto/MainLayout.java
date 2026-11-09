package com.proyecto.gastos.com.proyecto;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.dom.Element;

public class MainLayout extends AppLayout {

    private static final String ESTILOS = """
            :root {
                --color-acento: #2563eb;
                --fondo: #f8fafc;
                --superficie: #ffffff;
                --texto: #0f172a;
                --texto-suave: #64748b;
                --borde: #e2e8f0;
                --ingreso: #16a34a;
                --gasto: #dc2626;
                --alerta: #f59e0b;
            }

            .marca {
                display: flex;
                align-items: center;
                gap: 10px;
                min-width: 0;
            }
            .marca-logo {
                display: flex;
                align-items: center;
                justify-content: center;
                width: 34px;
                height: 34px;
                flex-shrink: 0;
                border-radius: 10px;
                background: var(--color-acento);
                color: #ffffff;
                box-shadow: 0 2px 6px color-mix(in srgb, var(--color-acento) 40%, transparent);
            }
            .marca-logo vaadin-icon {
                width: 18px;
                height: 18px;
            }
            .marca-nombre {
                margin: 0;
                font-size: 1.05rem;
                font-weight: 700;
                letter-spacing: -0.01em;
                white-space: nowrap;
            }
            .punto-color {
                width: 22px;
                height: 22px;
                min-width: 22px;
                padding: 0;
                border: 2px solid #ffffff;
                border-radius: 50%;
                box-shadow: 0 0 0 1px var(--borde);
                cursor: pointer;
                transition: transform 0.15s ease;
            }
            .punto-color:hover {
                transform: scale(1.18);
            }

            .menu-etiqueta {
                display: block;
                padding: 16px 16px 4px 16px;
                font-size: 0.7rem;
                font-weight: 600;
                letter-spacing: 0.08em;
                color: var(--texto-suave);
            }
            vaadin-side-nav-item {
                border-radius: 8px;
                margin-bottom: 2px;
                transition: background-color 0.15s ease;
            }
            vaadin-side-nav-item:hover {
                background-color: color-mix(in srgb, var(--color-acento) 8%, transparent);
            }
            vaadin-side-nav-item[current] {
                background-color: color-mix(in srgb, var(--color-acento) 14%, transparent);
                box-shadow: inset 3px 0 0 var(--color-acento);
                font-weight: 600;
            }
            vaadin-side-nav-item:hover vaadin-icon,
            vaadin-side-nav-item[current] vaadin-icon {
                color: var(--color-acento);
            }

            .tarjeta-estadistica {
                transition: transform 0.15s ease, box-shadow 0.15s ease;
            }
            .tarjeta-estadistica:hover {
                transform: translateY(-2px);
                box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
            }

            .budget-progress-bar { width: 100%; }
            .budget-progress-bar::part(bar) { border-radius: 999px; }
            .budget-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
                gap: 16px;
                width: 100%;
            }

            @media (max-width: 420px) {
                .marca-nombre { display: none; }
            }

            @media (prefers-reduced-motion: reduce) {
                * {
                    transition: none !important;
                    animation: none !important;
                }
            }
            """;

    public MainLayout() {
        createHeader();
        createNavigation();
        agregarEstilos();
        recordarColor();
    }

    private void createHeader() {
        DrawerToggle drawerToggle = new DrawerToggle();
        drawerToggle.getElement().setAttribute("aria-label", "Mostrar u ocultar el menú");
        Tooltip.forComponent(drawerToggle).withText("Mostrar u ocultar el menú");

        Div logo = new Div(VaadinIcon.WALLET.create());
        logo.addClassName("marca-logo");

        H1 title = new H1("Control de Gastos");
        title.addClassName("marca-nombre");

        Div marca = new Div(logo, title);
        marca.addClassName("marca");

        HorizontalLayout header = new HorizontalLayout(drawerToggle, marca, crearSelectorColor());
        header.setWidthFull();
        header.expand(marca);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.getStyle().set("padding-right", "16px");

        addToNavbar(header);
    }

    private HorizontalLayout crearSelectorColor() {
        HorizontalLayout selector = new HorizontalLayout(
                crearPuntoColor("Azul", "#2563eb"),
                crearPuntoColor("Verde", "#16a34a"),
                crearPuntoColor("Violeta", "#7c3aed"),
                crearPuntoColor("Naranja", "#ea580c"));
        selector.setSpacing(false);
        selector.getStyle().set("gap", "8px");
        selector.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        return selector;
    }

    private Button crearPuntoColor(String nombre, String color) {
        Button punto = new Button();
        punto.addClassName("punto-color");
        punto.setAriaLabel("Color de la aplicación: " + nombre);
        punto.getStyle().set("background-color", color);
        Tooltip.forComponent(punto).withText("Color " + nombre.toLowerCase());
        punto.addClickListener(evento -> cambiarColor(color));
        return punto;
    }

    private void cambiarColor(String color) {
        UI.getCurrent().getPage().executeJs(
                "document.documentElement.style.setProperty('--color-acento', $0);"
                        + "document.documentElement.style.setProperty('--aura-accent-color', $0);"
                        + "try { localStorage.setItem('color-acento', $0); } catch (e) {}",
                color);
    }

    private void recordarColor() {
        addAttachListener(evento -> evento.getUI().getPage().executeJs(
                "let c = null;"
                        + "try { c = localStorage.getItem('color-acento'); } catch (e) {}"
                        + "if (c) {"
                        + "  document.documentElement.style.setProperty('--color-acento', c);"
                        + "  document.documentElement.style.setProperty('--aura-accent-color', c);"
                        + "}"));
    }

    private void createNavigation() {
        Span label = new Span("MENÚ PRINCIPAL");
        label.addClassName("menu-etiqueta");

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
        style.setText(ESTILOS);
        getElement().appendChild(style);
    }
}
