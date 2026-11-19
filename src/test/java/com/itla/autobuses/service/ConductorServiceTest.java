package com.itla.autobuses.service;

import com.itla.autobuses.model.Conductor;
import com.itla.autobuses.util.NegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ConductorServiceTest extends PruebaBase {

    private String error(Conductor k) {
        NegocioException ex = assertThrows(NegocioException.class, () -> conductores.guardar(k));
        return ex.getMessage();
    }

    @Test
    @DisplayName("UT-14 Rechaza cédula que no tiene 11 dígitos")
    void rechazaCedulaCorta() throws Exception {
        iniciarComoAdmin();
        Conductor k = conductorValido();
        k.setCedula("12345");
        assertTrue(error(k).contains("11 dígitos"));
    }

    @Test
    @DisplayName("UT-15 Rechaza cédula con letras o guiones")
    void rechazaCedulaConGuiones() throws Exception {
        iniciarComoAdmin();
        Conductor k = conductorValido();
        k.setCedula("001-1234567-8");
        assertTrue(error(k).contains("11 dígitos"));
    }

    @Test
    @DisplayName("UT-16 Rechaza nombre o apellido vacios")
    void rechazaNombreVacio() throws Exception {
        iniciarComoAdmin();
        Conductor k = conductorValido();
        k.setNombre("  ");
        assertTrue(error(k).contains("obligatorios"));
    }

    @Test
    @DisplayName("UT-17 Rechaza teléfono con formato inválido")
    void rechazaTelefonoInvalido() throws Exception {
        iniciarComoAdmin();
        Conductor k = conductorValido();
        k.setTelefono("abc");
        assertTrue(error(k).contains("teléfono"));
    }

    @Test
    @DisplayName("UT-18 Rechaza licencia vacia")
    void rechazaLicenciaVacia() throws Exception {
        iniciarComoAdmin();
        Conductor k = conductorValido();
        k.setNumeroLicencia("");
        assertTrue(error(k).contains("licencia"));
    }

    @Test
    @DisplayName("UT-19 Rechaza falta de fecha de vencimiento")
    void rechazaFechaNula() throws Exception {
        iniciarComoAdmin();
        Conductor k = conductorValido();
        k.setVencimientoLicencia(null);
        assertTrue(error(k).contains("vencimiento"));
    }

    @Test
    @DisplayName("UT-20 No permite conductor activo con licencia vencida")
    void rechazaActivoConLicenciaVencida() throws Exception {
        iniciarComoAdmin();
        Conductor k = conductorValido();
        k.setVencimientoLicencia(LocalDate.now().minusDays(1));
        assertTrue(error(k).contains("vencida"));
    }

    @Test
    @DisplayName("UT-21 Permite guardar conductor inactivo con licencia vencida")
    void permiteInactivoConLicenciaVencida() throws Exception {
        iniciarComoAdmin();
        Conductor k = conductorValido();
        k.setVencimientoLicencia(LocalDate.now().minusDays(1));
        k.setActivo(false);
        conductores.guardar(k);
        assertTrue(k.getId() > 0);
    }

    @Test
    @DisplayName("UT-22 Usuario genérico no puede guardar conductores")
    void genericoNoPuedeGuardar() throws Exception {
        iniciarComoGenerico();
        assertTrue(error(conductorValido()).contains("Administrador"));
    }

    @Test
    @DisplayName("UT-23 Cédula repetida devuelve mensaje amigable")
    void cedulaDuplicada() throws Exception {
        iniciarComoAdmin();
        conductores.guardar(conductorValido());
        Conductor otro = conductorValido();
        otro.setNumeroLicencia("L-0002");
        assertTrue(error(otro).contains("Ya existe"));
    }
}
