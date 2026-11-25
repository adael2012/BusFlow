package com.itla.autobuses.service;

import com.itla.autobuses.model.Usuario;
import com.itla.autobuses.util.Sesion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CrearAdministradoresTest extends PruebaBase {

    @Test
    @DisplayName("UT-76 El administrador puede crear otros administradores")
    void crearOtroAdministrador() throws Exception {
        iniciarComoAdmin();

        int id = usuarios.crear("admin2", "Clave1234", 1);

        assertTrue(id > 0);
        assertEquals("Administrador", usuarioDAO.obtenerPorUsername("admin2").getRolNombre());
        auth.logout();
        Sesion.getInstancia().cerrar();
        Usuario otro = auth.login("admin2", "Clave1234");
        assertTrue(otro.esAdmin());
    }
}
