package com.itla.autobuses.service;

import com.itla.autobuses.model.Ruta;
import com.itla.autobuses.util.NegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RutaServiceTest extends PruebaBase {

    private RutaDAOFalso dao;
    private RutaService servicio;

    @BeforeEach
    void prepararModulo() {
        dao = new RutaDAOFalso();
        servicio = new RutaService(dao);
    }

    private String error(Ruta r) {
        return assertThrows(NegocioException.class, () -> servicio.guardar(r)).getMessage();
    }

    @Test
    @DisplayName("UT-48 Valida el codigo de la ruta")
    void validaCodigo() throws Exception {
        iniciarComoAdmin();
        Ruta r = rutaValida();
        r.setCodigo("X");
        assertTrue(error(r).contains("2 y 10 caracteres"));
    }

    @Test
    @DisplayName("UT-49 Valida distancia y duracion")
    void validaDistanciaYDuracion() throws Exception {
        iniciarComoAdmin();
        Ruta r = rutaValida();
        r.setDistanciaKm(null);
        assertTrue(error(r).contains("distancia"));

        r.setDistanciaKm(new java.math.BigDecimal("12.5"));
        r.setDuracionMin(0);
        assertTrue(error(r).contains("duracion"));
    }

    @Test
    @DisplayName("UT-50 Guarda una ruta y registra la bitacora")
    void guardaYRegistra() throws Exception {
        iniciarComoAdmin();
        Ruta r = rutaValida();

        servicio.guardar(r);

        assertEquals(1, r.getId());
        assertEquals(1, servicio.listarActivas().size());
        assertTrue(bitacora.acciones.contains("CREAR:Ruta"));
    }

    @Test
    @DisplayName("UT-51 Rechaza un codigo repetido")
    void rechazaCodigoRepetido() throws Exception {
        iniciarComoAdmin();
        servicio.guardar(rutaValida());
        Ruta otra = rutaValida();
        otra.setOrigen("La Vega");

        assertTrue(error(otra).contains("Ya existe"));
    }

    @Test
    @DisplayName("UT-52 Desactiva una ruta")
    void desactiva() throws Exception {
        iniciarComoAdmin();
        Ruta r = rutaValida();
        servicio.guardar(r);

        servicio.desactivar(r);

        assertFalse(r.isActivo());
        assertTrue(servicio.listarActivas().isEmpty());
        assertTrue(bitacora.acciones.contains("DESACTIVAR:Ruta"));
    }

    @Test
    @DisplayName("UT-53 Solo el administrador puede modificar rutas")
    void exigeAdmin() {
        NegocioException ex = assertThrows(NegocioException.class,
                () -> servicio.guardar(rutaValida()));
        assertTrue(ex.getMessage().contains("Administrador"));
    }
}
