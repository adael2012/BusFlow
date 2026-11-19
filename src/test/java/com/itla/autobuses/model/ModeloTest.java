package com.itla.autobuses.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ModeloTest {

    @Test
    @DisplayName("UT-09 Usuario con rol Administrador es admin")
    void usuarioAdministradorEsAdmin() {
        Usuario u = new Usuario();
        u.setRolNombre("Administrador");
        assertTrue(u.esAdmin());
    }

    @Test
    @DisplayName("UT-10 Usuario genérico o sin rol no es admin")
    void usuarioGenericoNoEsAdmin() {
        Usuario u = new Usuario();
        assertFalse(u.esAdmin());
        u.setRolNombre("Usuario Genérico");
        assertFalse(u.esAdmin());
    }

    @Test
    @DisplayName("UT-11 Licencia que vence hoy sigue vigente")
    void licenciaQueVenceHoyEsVigente() {
        Conductor k = new Conductor();
        k.setVencimientoLicencia(LocalDate.now());
        assertTrue(k.licenciaVigente());
    }

    @Test
    @DisplayName("UT-12 Licencia vencida ayer no es vigente")
    void licenciaVencidaNoEsVigente() {
        Conductor k = new Conductor();
        k.setVencimientoLicencia(LocalDate.now().minusDays(1));
        assertFalse(k.licenciaVigente());
    }

    @Test
    @DisplayName("UT-13 Licencia sin fecha no es vigente")
    void licenciaSinFechaNoEsVigente() {
        assertFalse(new Conductor().licenciaVigente());
    }
}
