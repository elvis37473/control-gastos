package com.proyecto.gastos.com.proyecto;

import com.proyecto.gastos.com.proyecto.archivos.GestorArchivos;
import java.nio.file.Path;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	GestorArchivos gestorArchivos(@Value("${app.data-directory:datos}") String dataDirectory) {
		return new GestorArchivos(Path.of(dataDirectory));
	}

	@Bean
	CommandLineRunner inicializarDatos(GestorArchivos gestorArchivos) {
		return args -> {
			gestorArchivos.crearDatosDeEjemploSiNoExisten();
			System.out.println("Movimientos cargados: " + gestorArchivos.cargarMovimientos().size());
			System.out.println("Presupuestos cargados: " + gestorArchivos.cargarPresupuestos().size());
		};
	}

}
