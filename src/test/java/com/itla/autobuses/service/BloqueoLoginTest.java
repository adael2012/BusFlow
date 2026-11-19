package com.itla.autobuses.service;

import com.itla.autobuses.util.NegocioException;
import com.itla.autobuses.util.Sesion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BloqueoLoginTest extends PruebaBase {

    @Test
    @DisplayName("UT-37 El login se bloquea despues de tres intentos fallidos")
    void bloqueaDespuesDeTresIntentos() throws Exception {
        usuarios.crearAdminInicialSiNoExiste();

        for (int i = 0; i < 2; i++) {
            final String incorrecta = "ClaveMala" + i;
            NegocioException ex = assertThrows(NegocioException.class,
                    () -> auth.login("admin", incorrecta));
            assertEquals("Usuario o contrasena incorrectos.", ex.getMessage());
        }
        NegocioException tercero = assertThrows(NegocioException.class,
                () -> auth.login("admin", "ClaveMala3"));
        assertTrue(tercero.getMessage().contains("bloqueada"));

        NegocioException bloqueado = assertThrows(NegocioException.class,
                () -> auth.login("admin", UsuarioService.CLAVE_INICIAL));
        assertTrue(bloqueado.getMessage().contains("bloqueada"));
        assertFalse(Sesion.getInstancia().hayUsuario());
    }

    @Test
    @DisplayName("UT-38 Un login correcto reinicia el contador de intentos")
    void loginCorrectoReiniciaIntentos() throws Exception {
        usuarios.crearAdminInicialSiNoExiste();

        assertThrows(NegocioException.class, () -> auth.login("admin", "Mala1"));
        assertThrows(NegocioException.class, () -> auth.login("admin", "Mala2"));
        assertEquals("admin", auth.login("admin", UsuarioService.CLAVE_INICIAL).getUsername());
        auth.logout();

        assertThrows(NegocioException.class, () -> auth.login("admin", "Mala3"));
        assertThrows(NegocioException.class, () -> auth.login("admin", "Mala4"));
        assertEquals("admin", auth.login("admin", UsuarioService.CLAVE_INICIAL).getUsername());
        assertTrue(Sesion.getInstancia().esAdmin());
    }
}
