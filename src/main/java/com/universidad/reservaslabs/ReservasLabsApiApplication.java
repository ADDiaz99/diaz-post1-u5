package com.universidad.reservaslabs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Vive en el paquete raiz com.universidad.reservaslabs para que el escaneo de
// componentes encuentre model/, repository/, service/, controller/ y web/.
@SpringBootApplication
public class ReservasLabsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservasLabsApiApplication.class, args);
    }
}
