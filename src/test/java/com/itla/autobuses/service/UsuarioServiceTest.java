package com.itla.autobuses.service;

import com.itla.autobuses.util.NegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioServiceTest extends PruebaBase {

    private String errorCrear(String usuario, String clave) {
        NegocioException ex = assertThrows(NegocioException.class, () -> usuarios.crear(usuario, clave, 2));
        return ex.getMessage();
    }

    @Test
    @DisplayName("UT-24 Rechaza nombre de usuario demasiado corto")
    void rechazaUsuarioCorto() throws Exception {
        iniciarComoAdmin();
        assertTrue(errorCrear("ab", "Clave1234").contains("entre 3 y 30"));
    }

    @Test
    @DisplayName("UT-25 Rechaza nombre de usuario con espacios o simbolos")
    void rechazaUsuarioConEspacios() throws Exception {
        iniciarComoAdmin();
        assertTrue(errorCrear("juan perez", "Clave1234").contains("entre 3 y 30"));
        assertTrue(errorCrear("juan@perez", "Clave1234").contains("entre 3 y 30"));
    }

    @Test
    @DisplayName("UT-26 Rechaza contrasena de menos de 8 caracteres")
    void rechazaClaveCorta() throws Exception {
        iniciarComoAdmin();
        assertTrue(errorCrear("operador1", "1234567").contains("8 caracteres"));
    }

    @Test
    @DisplayName("UT-27 Usuario generico no puede crear usuarios")
    void genericoNoPuedeCrear() throws Exception {
        iniciarComoGenerico();
        assertTrue(errorCrear("otro", "Clave1234").contains("Administrador"));
    }

    @Test
    @DisplayName("UT-28 Nombre de usuario repetido devuelve mensaje amigable")
    void usuarioDuplicado() throws Exception {
        iniciarComoAdmin();
        usuarios.crear("operador1", "Clave1234", 2);
        assertTrue(errorCrear("operador1", "Otra12345").contains("Ya existe"));
    }

    @Test
    @DisplayName("UT-29 La contrasena se guarda con hash y no en texto plano")
    void claveSeGuardaConHash() throws Exception {
        iniciarComoAdmin();
        usuarios.crear("operador1", "Clave1234", 2);
        String guardado = usuarioDAO.obtenerPorUsername("operador1").getPasswordHash();
        assertNotEquals("Clave1234", guardado);
        assertFalse(guardado.contains("Clave1234"));
    }
}
