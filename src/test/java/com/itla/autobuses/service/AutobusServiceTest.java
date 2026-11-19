package com.itla.autobuses.service;

import com.itla.autobuses.model.Autobus;
import com.itla.autobuses.util.NegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AutobusServiceTest extends PruebaBase {

    private AutobusDAOFalso dao;
    private AutobusService servicio;

    @BeforeEach
    void prepararModulo() {
        dao = new AutobusDAOFalso();
        servicio = new AutobusService(dao);
    }

    private String error(Autobus b) {
        return assertThrows(NegocioException.class, () -> servicio.guardar(b)).getMessage();
    }

    @Test
    @DisplayName("UT-42 Valida la placa del autobus")
    void validaPlaca() throws Exception {
        iniciarComoAdmin();
        Autobus b = autobusValido();
        b.setPlaca("AB");
        assertTrue(error(b).contains("3 y 10 caracteres"));
    }

    @Test
    @DisplayName("UT-43 Valida anio y capacidad")
    void validaAnioYCapacidad() throws Exception {
        iniciarComoAdmin();
        Autobus b = autobusValido();
        b.setAnio(1800);
        assertTrue(error(b).contains("anio"));

        b.setAnio(2020);
        b.setCapacidad(0);
        assertTrue(error(b).contains("capacidad"));
    }

    @Test
    @DisplayName("UT-44 Guarda un autobus y registra la bitacora")
    void guardaYRegistra() throws Exception {
        iniciarComoAdmin();
        Autobus b = autobusValido();

        servicio.guardar(b);

        assertEquals(1, b.getId());
        assertEquals(1, servicio.listar().size());
        assertTrue(bitacora.acciones.contains("CREAR:Autobus"));
    }

    @Test
    @DisplayName("UT-45 Rechaza una placa repetida")
    void rechazaPlacaRepetida() throws Exception {
        iniciarComoAdmin();
        servicio.guardar(autobusValido());
        Autobus otro = autobusValido();
        otro.setMarca("Hyundai");

        assertTrue(error(otro).contains("Ya existe"));
    }

    @Test
    @DisplayName("UT-46 Solo el administrador puede modificar autobuses")
    void exigeAdmin() {
        NegocioException ex = assertThrows(NegocioException.class,
                () -> servicio.guardar(autobusValido()));
        assertTrue(ex.getMessage().contains("Administrador"));
    }

    @Test
    @DisplayName("UT-47 Cambia el estado a mantenimiento")
    void cambiaEstado() throws Exception {
        iniciarComoAdmin();
        Autobus b = autobusValido();
        servicio.guardar(b);

        servicio.cambiarEstado(b, Autobus.MANTENIMIENTO);

        assertEquals(Autobus.MANTENIMIENTO, b.getEstado());
        assertTrue(bitacora.acciones.contains("ESTADO:Autobus"));
    }
}
