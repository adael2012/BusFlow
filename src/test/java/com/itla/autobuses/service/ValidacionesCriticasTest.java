package com.itla.autobuses.service;

import com.itla.autobuses.model.Asignacion;
import com.itla.autobuses.model.Ruta;
import com.itla.autobuses.model.Usuario;
import com.itla.autobuses.util.NegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ValidacionesCriticasTest extends PruebaBase {

    private AutobusDAOFalso autobusDAO;
    private RutaDAOFalso rutaDAO;
    private AsignacionDAOFalso asignacionDAO;
    private AsignacionService asignaciones;

    @BeforeEach
    void prepararModulos() {
        autobusDAO = new AutobusDAOFalso();
        rutaDAO = new RutaDAOFalso();
        asignacionDAO = new AsignacionDAOFalso();
        asignaciones = new AsignacionService(asignacionDAO, conductorDAO, autobusDAO, rutaDAO);
    }

    @Test
    @DisplayName("UT-63 Rechaza contraseña sin mayúscula o número")
    void validaComplejidadDeContrasena() throws Exception {
        iniciarComoAdmin();
        NegocioException ex = assertThrows(NegocioException.class,
                () -> usuarios.crear("operador9", "clave1234", 2));
        assertTrue(ex.getMessage().contains("mayúscula"));
    }

    @Test
    @DisplayName("UT-64 Contraseña actual nula devuelve aviso controlado")
    void contrasenaActualNula() throws Exception {
        iniciarComoAdmin();
        NegocioException ex = assertThrows(NegocioException.class,
                () -> usuarios.cambiarPasswordPropio(null, "Nueva1234"));
        assertEquals("La contraseña actual es incorrecta.", ex.getMessage());
    }

    @Test
    @DisplayName("UT-65 No permite reutilizar la contraseña actual")
    void noReutilizaContrasena() throws Exception {
        iniciarComoAdmin();
        NegocioException ex = assertThrows(NegocioException.class,
                () -> usuarios.cambiarPasswordPropio(UsuarioService.CLAVE_INICIAL,
                        UsuarioService.CLAVE_INICIAL));
        assertTrue(ex.getMessage().contains("diferente"));
    }

    @Test
    @DisplayName("UT-66 Rechaza un rol inexistente")
    void rechazaRolInexistente() throws Exception {
        iniciarComoAdmin();
        NegocioException ex = assertThrows(NegocioException.class,
                () -> usuarios.crear("operador9", "Clave1234", 99));
        assertTrue(ex.getMessage().contains("rol"));
    }

    @Test
    @DisplayName("UT-67 El administrador no puede quitarse su propio rol")
    void noPuedeDegradarse() throws Exception {
        iniciarComoAdmin();
        Usuario yo = usuarioDAO.obtenerPorUsername("admin");
        NegocioException ex = assertThrows(NegocioException.class,
                () -> usuarios.actualizar(yo, 2, true));
        assertTrue(ex.getMessage().contains("quitarte"));
    }

    @Test
    @DisplayName("UT-68 La ruta debe tener origen y destino diferentes")
    void rutaRequiereDestinoDiferente() throws Exception {
        iniciarComoAdmin();
        Ruta r = rutaValida();
        r.setDestino("SANTO DOMINGO");
        NegocioException ex = assertThrows(NegocioException.class,
                () -> new RutaService(rutaDAO).guardar(r));
        assertTrue(ex.getMessage().contains("diferentes"));
    }

    @Test
    @DisplayName("UT-69 Rechaza una distancia fuera del rango permitido")
    void rechazaDistanciaExcesiva() throws Exception {
        iniciarComoAdmin();
        Ruta r = rutaValida();
        r.setDistanciaKm(new BigDecimal("1000000"));
        NegocioException ex = assertThrows(NegocioException.class,
                () -> new RutaService(rutaDAO).guardar(r));
        assertTrue(ex.getMessage().contains("distancia"));
    }

    @Test
    @DisplayName("UT-70 Rechaza una capacidad mayor al limite del autobús")
    void rechazaCapacidadExcesiva() throws Exception {
        iniciarComoAdmin();
        var b = autobusValido();
        b.setCapacidad(201);
        NegocioException ex = assertThrows(NegocioException.class,
                () -> new AutobusService(autobusDAO).guardar(b));
        assertTrue(ex.getMessage().contains("capacidad"));
    }

    @Test
    @DisplayName("UT-71 Valida el formato del número de licencia")
    void validaFormatoDeLicencia() throws Exception {
        iniciarComoAdmin();
        var k = conductorValido();
        k.setNumeroLicencia("L!");
        NegocioException ex = assertThrows(NegocioException.class,
                () -> conductores.guardar(k));
        assertTrue(ex.getMessage().contains("licencia"));
    }

    @Test
    @DisplayName("UT-72 No se puede cancelar una asignación completada")
    void noCancelaAsignacionCompletada() throws Exception {
        iniciarComoAdmin();
        var k = conductorValido();
        conductores.guardar(k);
        var b = autobusValido();
        new AutobusService(autobusDAO).guardar(b);
        var r = rutaValida();
        new RutaService(rutaDAO).guardar(r);

        Asignacion a = asignacionValida(k, b, r);
        asignaciones.guardar(a);
        a.setEstado(Asignacion.COMPLETADA);

        NegocioException ex = assertThrows(NegocioException.class,
                () -> asignaciones.cancelar(a));
        assertTrue(ex.getMessage().contains("programada"));
    }
}
