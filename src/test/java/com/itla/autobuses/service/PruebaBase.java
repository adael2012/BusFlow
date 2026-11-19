package com.itla.autobuses.service;

import com.itla.autobuses.model.Asignacion;
import com.itla.autobuses.model.Autobus;
import com.itla.autobuses.model.Conductor;
import com.itla.autobuses.model.Ruta;
import com.itla.autobuses.model.Usuario;
import com.itla.autobuses.util.Sesion;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

abstract class PruebaBase {

    UsuarioDAOFalso usuarioDAO;
    RolDAOFalso rolDAO;
    ConductorDAOFalso conductorDAO;
    BitacoraDAOFalso bitacora;
    UsuarioService usuarios;
    ConductorService conductores;
    AuthService auth;

    @BeforeEach
    void preparar() {
        Sesion.getInstancia().cerrar();
        AuthService.reiniciarIntentos();
        usuarioDAO = new UsuarioDAOFalso();
        rolDAO = new RolDAOFalso();
        conductorDAO = new ConductorDAOFalso();
        bitacora = new BitacoraDAOFalso();
        BitacoraHelper.usar(bitacora);
        usuarios = new UsuarioService(usuarioDAO, rolDAO);
        conductores = new ConductorService(conductorDAO);
        auth = new AuthService(usuarioDAO);
    }

    void iniciarComoAdmin() throws Exception {
        usuarios.crearAdminInicialSiNoExiste();
        auth.login(UsuarioService.ADMIN_INICIAL, UsuarioService.CLAVE_INICIAL);
    }

    void iniciarComoGenerico() throws Exception {
        iniciarComoAdmin();
        usuarios.crear("operador1", "Clave1234", 2);
        auth.logout();
        auth.login("operador1", "Clave1234");
    }

    static Conductor conductorValido() {
        Conductor k = new Conductor();
        k.setCedula("00112345678");
        k.setNombre("Juan");
        k.setApellido("Perez");
        k.setTelefono("809-555-1234");
        k.setNumeroLicencia("L-0001");
        k.setVencimientoLicencia(LocalDate.now().plusYears(1));
        k.setActivo(true);
        return k;
    }

    static Autobus autobusValido() {
        Autobus b = new Autobus();
        b.setPlaca("ABC123");
        b.setMarca("Toyota");
        b.setModelo("Coaster");
        b.setAnio(2020);
        b.setCapacidad(30);
        b.setEstado(Autobus.OPERATIVO);
        return b;
    }

    static Ruta rutaValida() {
        Ruta r = new Ruta();
        r.setCodigo("R-01");
        r.setOrigen("Santo Domingo");
        r.setDestino("Santiago");
        r.setDistanciaKm(new BigDecimal("130.50"));
        r.setDuracionMin(120);
        r.setActivo(true);
        return r;
    }

    static Asignacion asignacionValida(Conductor k, Autobus b, Ruta r) {
        Asignacion a = new Asignacion();
        a.setIdConductor(k.getId());
        a.setIdAutobus(b.getId());
        a.setIdRuta(r.getId());
        LocalDateTime inicio = LocalDateTime.now().plusHours(1).withSecond(0).withNano(0);
        a.setFechaInicio(inicio);
        a.setFechaFin(inicio.plusHours(2));
        a.setEstado(Asignacion.PROGRAMADA);
        a.setNombreConductor(k.getNombre() + " " + k.getApellido());
        a.setPlacaAutobus(b.getPlaca());
        a.setCodigoRuta(r.getCodigo());
        return a;
    }
}
