package com.itla.autobuses.service;

import com.itla.autobuses.model.Conductor;
import com.itla.autobuses.model.Usuario;
import com.itla.autobuses.util.NegocioException;
import com.itla.autobuses.util.Sesion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlujosIntegracionTest extends PruebaBase {

    @Test
    @DisplayName("IT-01 Primer arranque: se crea el admin una sola vez y puede iniciar sesion")
    void primerArranqueYLogin() throws Exception {
        usuarios.crearAdminInicialSiNoExiste();
        usuarios.crearAdminInicialSiNoExiste();
        assertEquals(1, usuarioDAO.contar());

        Usuario u = auth.login("admin", "Admin123!");
        assertEquals("admin", u.getUsername());
        assertTrue(Sesion.getInstancia().esAdmin());
        assertTrue(bitacora.acciones.contains("LOGIN:Usuario"));
    }

    @Test
    @DisplayName("IT-02 Credenciales invalidas y usuario desactivado no inician sesion")
    void loginRechazado() throws Exception {
        iniciarComoAdmin();
        usuarios.crear("operador1", "Clave1234", 2);
        auth.logout();

        NegocioException e1 = assertThrows(NegocioException.class, () -> auth.login("admin", "mala"));
        NegocioException e2 = assertThrows(NegocioException.class, () -> auth.login("noexiste", "Clave1234"));
        assertEquals(e1.getMessage(), e2.getMessage());
        assertFalse(Sesion.getInstancia().hayUsuario());

        auth.login("admin", "Admin123!");
        usuarios.desactivar(usuarioDAO.obtenerPorUsername("operador1"));
        auth.logout();
        NegocioException e3 = assertThrows(NegocioException.class, () -> auth.login("operador1", "Clave1234"));
        assertTrue(e3.getMessage().contains("desactivado"));
    }

    @Test
    @DisplayName("IT-03 Admin crea un usuario generico y los permisos se aplican en todos los modulos")
    void permisosPorRol() throws Exception {
        iniciarComoGenerico();
        assertFalse(Sesion.getInstancia().esAdmin());

        assertThrows(NegocioException.class, () -> usuarios.listar());
        assertThrows(NegocioException.class, () -> conductores.guardar(conductorValido()));
        assertDoesNotThrow(() -> conductores.listar());
        assertDoesNotThrow(() -> conductores.buscar("perez"));
    }

    @Test
    @DisplayName("IT-04 Ciclo completo de un conductor: registrar, buscar, modificar y desactivar")
    void cicloCompletoConductor() throws Exception {
        iniciarComoAdmin();
        Conductor k = conductorValido();
        conductores.guardar(k);
        assertTrue(k.getId() > 0);

        List<Conductor> encontrados = conductores.buscar("perez");
        assertEquals(1, encontrados.size());

        k.setTelefono("829-555-9999");
        conductores.guardar(k);
        assertEquals("829-555-9999", conductores.listar().get(0).getTelefono());

        conductores.desactivar(k);
        assertFalse(conductores.listar().get(0).isActivo());

        assertTrue(bitacora.acciones.contains("CREAR:Conductor"));
        assertTrue(bitacora.acciones.contains("ACTUALIZAR:Conductor"));
        assertTrue(bitacora.acciones.contains("DESACTIVAR:Conductor"));
    }

    @Test
    @DisplayName("IT-05 Restablecer contrasena invalida la anterior y habilita la nueva")
    void restablecerPassword() throws Exception {
        iniciarComoAdmin();
        usuarios.crear("operador1", "Clave1234", 2);
        usuarios.restablecerPassword(usuarioDAO.obtenerPorUsername("operador1"), "NuevaClave99");
        auth.logout();

        assertThrows(NegocioException.class, () -> auth.login("operador1", "Clave1234"));
        assertDoesNotThrow(() -> auth.login("operador1", "NuevaClave99"));
    }

    @Test
    @DisplayName("IT-06 El admin no puede desactivarse a si mismo")
    void noPuedeDesactivarseASiMismo() throws Exception {
        iniciarComoAdmin();
        Usuario yo = usuarioDAO.obtenerPorUsername("admin");
        NegocioException ex = assertThrows(NegocioException.class, () -> usuarios.desactivar(yo));
        assertTrue(ex.getMessage().contains("propio usuario"));
        assertTrue(yo.isActivo());
    }

    @Test
    @DisplayName("IT-07 Cerrar sesion bloquea las operaciones protegidas")
    void cerrarSesionBloqueaOperaciones() throws Exception {
        iniciarComoAdmin();
        auth.logout();
        assertFalse(Sesion.getInstancia().hayUsuario());
        assertThrows(NegocioException.class, () -> conductores.guardar(conductorValido()));
        assertThrows(NegocioException.class, () -> usuarios.listar());
        assertTrue(bitacora.acciones.contains("LOGOUT:Usuario"));
    }
}
