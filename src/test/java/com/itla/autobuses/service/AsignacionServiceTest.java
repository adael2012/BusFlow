package com.itla.autobuses.service;

import com.itla.autobuses.model.Asignacion;
import com.itla.autobuses.model.Autobus;
import com.itla.autobuses.model.Conductor;
import com.itla.autobuses.model.Ruta;
import com.itla.autobuses.util.NegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AsignacionServiceTest extends PruebaBase {

    private AsignacionDAOFalso dao;
    private AutobusDAOFalso autobusDAO;
    private RutaDAOFalso rutaDAO;
    private AsignacionService servicio;

    private Conductor k1;
    private Conductor k2;
    private Autobus b1;
    private Autobus b2;
    private Ruta r1;
    private Ruta r2;

    @BeforeEach
    void prepararModulo() {
        dao = new AsignacionDAOFalso();
        autobusDAO = new AutobusDAOFalso();
        rutaDAO = new RutaDAOFalso();
        servicio = new AsignacionService(dao, conductorDAO, autobusDAO, rutaDAO);
    }

    private void prepararDatos() throws Exception {
        iniciarComoAdmin();

        k1 = conductorValido();
        conductores.guardar(k1);

        k2 = conductorValido();
        k2.setCedula("00212345678");
        k2.setNumeroLicencia("L-0002");
        conductorDAO.insertar(k2);

        b1 = autobusValido();
        new AutobusService(autobusDAO).guardar(b1);
        b2 = autobusValido();
        b2.setPlaca("DEF456");
        new AutobusService(autobusDAO).guardar(b2);

        r1 = rutaValida();
        new RutaService(rutaDAO).guardar(r1);
        r2 = rutaValida();
        r2.setCodigo("R-02");
        new RutaService(rutaDAO).guardar(r2);
    }

    private String error(Asignacion a) {
        return assertThrows(NegocioException.class, () -> servicio.guardar(a)).getMessage();
    }

    @Test
    @DisplayName("UT-54 Guarda una asignación y registra la bitácora")
    void guardaYRegistra() throws Exception {
        prepararDatos();
        Asignacion a = asignacionValida(k1, b1, r1);

        servicio.guardar(a);

        assertEquals(1, a.getId());
        assertEquals(1, servicio.listar().size());
        assertTrue(bitacora.acciones.contains("CREAR:Asignación"));
    }

    @Test
    @DisplayName("UT-55 Rechaza un solape de conductor")
    void rechazaSolapeDeConductor() throws Exception {
        prepararDatos();
        Asignacion primera = asignacionValida(k1, b1, r1);
        servicio.guardar(primera);

        Asignacion segunda = asignacionValida(k1, b2, r2);
        segunda.setFechaInicio(primera.getFechaInicio().plusMinutes(30));
        segunda.setFechaFin(primera.getFechaFin().plusMinutes(30));

        assertTrue(error(segunda).contains("conductor ya tiene"));
    }

    @Test
    @DisplayName("UT-56 Rechaza un solape de autobús")
    void rechazaSolapeDeAutobus() throws Exception {
        prepararDatos();
        Asignacion primera = asignacionValida(k1, b1, r1);
        servicio.guardar(primera);

        Asignacion segunda = asignacionValida(k2, b1, r2);

        assertTrue(error(segunda).contains("autobús ya esta"));
    }

    @Test
    @DisplayName("UT-57 Rechaza un conductor con licencia vencida")
    void rechazaLicenciaVencida() throws Exception {
        prepararDatos();
        Conductor vencido = conductorValido();
        vencido.setCedula("00312345678");
        vencido.setNumeroLicencia("L-0003");
        vencido.setVencimientoLicencia(LocalDate.now().minusDays(1));
        conductorDAO.insertar(vencido);
        Asignacion a = asignacionValida(vencido, b2, r2);

        assertTrue(error(a).contains("licencia"));
    }

    @Test
    @DisplayName("UT-58 Excluye los recursos ocupados de la disponibilidad")
    void calculaDisponibilidad() throws Exception {
        prepararDatos();
        Asignacion a = asignacionValida(k1, b1, r1);
        servicio.guardar(a);

        List<Conductor> conductoresLibres = servicio.conductoresDisponibles(
                a.getFechaInicio(), a.getFechaFin());
        List<Autobus> autobusesLibres = servicio.autobusesDisponibles(
                a.getFechaInicio(), a.getFechaFin());

        assertFalse(contieneConductor(conductoresLibres, k1));
        assertTrue(contieneConductor(conductoresLibres, k2));
        assertFalse(contieneAutobus(autobusesLibres, b1));
        assertTrue(contieneAutobus(autobusesLibres, b2));

        List<Conductor> otros = servicio.conductoresDisponibles(
                a.getFechaFin().plusHours(1), a.getFechaFin().plusHours(2));
        List<Autobus> otrosBuses = servicio.autobusesDisponibles(
                a.getFechaFin().plusHours(1), a.getFechaFin().plusHours(2));
        assertTrue(contieneConductor(otros, k1));
        assertTrue(contieneAutobus(otrosBuses, b1));
    }

    @Test
    @DisplayName("UT-59 Cancela una asignación")
    void cancela() throws Exception {
        prepararDatos();
        Asignacion a = asignacionValida(k1, b1, r1);
        servicio.guardar(a);

        servicio.cancelar(a);

        assertEquals(Asignacion.CANCELADA, a.getEstado());
        assertTrue(bitacora.acciones.contains("CANCELAR:Asignación"));
    }

    @Test
    @DisplayName("UT-60 Solo el administrador puede crear asignaciones")
    void exigeAdmin() {
        NegocioException ex = assertThrows(NegocioException.class,
                () -> servicio.guardar(new Asignacion()));
        assertTrue(ex.getMessage().contains("Administrador"));
    }

    private boolean contieneConductor(List<Conductor> lista, Conductor k) {
        return lista.stream().anyMatch(x -> x.getId() == k.getId());
    }

    private boolean contieneAutobus(List<Autobus> lista, Autobus b) {
        return lista.stream().anyMatch(x -> x.getId() == b.getId());
    }
}
