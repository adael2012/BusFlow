package com.itla.autobuses.service;

import com.itla.autobuses.util.NegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CambiarPasswordPropioTest extends PruebaBase {

    @Test
    @DisplayName("UT-39 El administrador puede cambiar su propia contraseña")
    void administradorCambiaSuContrasena() throws Exception {
        iniciarComoAdmin();

        usuarios.cambiarPasswordPropio(UsuarioService.CLAVE_INICIAL, "Nueva1234");
        auth.logout();
        assertEquals("admin", auth.login("admin", "Nueva1234").getUsername());
        assertTrue(bitacora.acciones.contains("PASSWORD:Usuario"));
    }

    @Test
    @DisplayName("UT-40 Rechaza una contraseña actual incorrecta")
    void rechazaContrasenaActualIncorrecta() throws Exception {
        iniciarComoAdmin();

        NegocioException ex = assertThrows(NegocioException.class,
                () -> usuarios.cambiarPasswordPropio("Incorrecta1", "Nueva1234"));
        assertEquals("La contraseña actual es incorrecta.", ex.getMessage());
    }

    @Test
    @DisplayName("UT-41 El usuario genérico tambien puede cambiar la suya")
    void genericoCambiaSuContrasena() throws Exception {
        iniciarComoGenerico();

        usuarios.cambiarPasswordPropio("Clave1234", "Otra12345");
        auth.logout();
        assertEquals("operador1", auth.login("operador1", "Otra12345").getUsername());
    }
}
