package com.itla.autobuses.util;

import com.itla.autobuses.model.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SesionTest {

    @AfterEach
    void limpiar() {
        Sesion.getInstancia().cerrar();
    }

    @Test
    @DisplayName("UT-06 Sesion es un Singleton")
    void sesionEsSingleton() {
        assertSame(Sesion.getInstancia(), Sesion.getInstancia());
    }

    @Test
    @DisplayName("UT-07 Sesion sin usuario no es administrador")
    void sinUsuarioNoEsAdmin() {
        Sesion.getInstancia().cerrar();
        assertFalse(Sesion.getInstancia().hayUsuario());
        assertFalse(Sesion.getInstancia().esAdmin());
    }

    @Test
    @DisplayName("UT-08 Iniciar y cerrar sesion actualiza el estado")
    void iniciarYCerrar() {
        Usuario u = new Usuario();
        u.setUsername("admin");
        u.setRolNombre("Administrador");
        Sesion.getInstancia().iniciar(u);
        assertTrue(Sesion.getInstancia().hayUsuario());
        assertTrue(Sesion.getInstancia().esAdmin());
        Sesion.getInstancia().cerrar();
        assertNull(Sesion.getInstancia().getUsuario());
    }
}
