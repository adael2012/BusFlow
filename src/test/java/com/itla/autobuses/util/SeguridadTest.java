package com.itla.autobuses.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeguridadTest {

    @Test
    @DisplayName("UT-01 Misma clave y mismo salt producen el mismo hash")
    void hashEsDeterministaConElMismoSalt() {
        String salt = Seguridad.generarSalt();
        assertEquals(Seguridad.hash("Clave1234", salt), Seguridad.hash("Clave1234", salt));
    }

    @Test
    @DisplayName("UT-02 La misma clave con salts distintos produce hashes distintos")
    void saltsDistintosCambianElHash() {
        String h1 = Seguridad.hash("Clave1234", Seguridad.generarSalt());
        String h2 = Seguridad.hash("Clave1234", Seguridad.generarSalt());
        assertNotEquals(h1, h2);
    }

    @Test
    @DisplayName("UT-03 verificar acepta la clave correcta")
    void verificarAceptaClaveCorrecta() {
        String salt = Seguridad.generarSalt();
        String hash = Seguridad.hash("Clave1234", salt);
        assertTrue(Seguridad.verificar("Clave1234", salt, hash));
    }

    @Test
    @DisplayName("UT-04 verificar rechaza una clave incorrecta")
    void verificarRechazaClaveIncorrecta() {
        String salt = Seguridad.generarSalt();
        String hash = Seguridad.hash("Clave1234", salt);
        assertFalse(Seguridad.verificar("clave1234", salt, hash));
    }

    @Test
    @DisplayName("UT-05 El hash no contiene la clave en texto plano y cabe en la columna")
    void hashNoExponeLaClave() {
        String salt = Seguridad.generarSalt();
        String hash = Seguridad.hash("Clave1234", salt);
        assertFalse(hash.contains("Clave1234"));
        assertTrue(hash.length() <= 100);
        assertTrue(salt.length() <= 50);
    }
}
