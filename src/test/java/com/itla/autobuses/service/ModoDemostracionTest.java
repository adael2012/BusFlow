package com.itla.autobuses.service;

import com.itla.autobuses.model.Asignacion;
import com.itla.autobuses.model.Resumen;
import com.itla.autobuses.model.Usuario;
import com.itla.autobuses.util.NegocioException;
import com.itla.autobuses.util.Sesion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ModoDemostracionTest extends PruebaBase {

    @Test
    @DisplayName("UT-77 El modo demostración opera sin SQL Server")
    void modoDemostracion() throws Exception {
        assertTrue(FabricaServicios.inicializar(new String[]{"--demo"}));
        assertTrue(FabricaServicios.esDemo());
        Sesion.getInstancia().cerrar();
        AuthService.reiniciarIntentos();

        Usuario admin = FabricaServicios.auth().login(
                UsuarioService.ADMIN_INICIAL, UsuarioService.CLAVE_INICIAL);
        assertTrue(admin.esAdmin());
        assertEquals(4, FabricaServicios.conductores().listar().size());
        assertEquals(2, FabricaServicios.autobuses().listarOperativos().size());
        assertEquals(4, FabricaServicios.asignaciones().listar().size());
        assertFalse(FabricaServicios.bitacora().listar(null, null, null, null).isEmpty());

        Resumen resumen = FabricaServicios.panel().resumen();
        assertEquals(3, resumen.getConductoresActivos());
        assertEquals(2, resumen.getAutobusesOperativos());

        int id = FabricaServicios.usuarios().crear("adminDemo", "Clave1234", 1);
        assertTrue(id > 0);
        FabricaServicios.auth().logout();
        Sesion.getInstancia().cerrar();
        assertTrue(FabricaServicios.auth().login("adminDemo", "Clave1234").esAdmin());
    }

    @Test
    @DisplayName("UT-78 El modo demostración mantiene las validaciones de negocio")
    void validacionesEnDemo() throws Exception {
        FabricaServicios.inicializar(new String[]{"--demo"});
        Sesion.getInstancia().cerrar();
        AuthService.reiniciarIntentos();
        FabricaServicios.auth().login(
                UsuarioService.ADMIN_INICIAL, UsuarioService.CLAVE_INICIAL);

        Asignacion original = FabricaServicios.asignaciones().listar().stream()
                .filter(a -> Asignacion.PROGRAMADA.equals(a.getEstado()))
                .findFirst().orElseThrow();
        Asignacion solapada = new Asignacion();
        solapada.setIdConductor(original.getIdConductor());
        solapada.setIdAutobus(original.getIdAutobus());
        solapada.setIdRuta(original.getIdRuta());
        LocalDateTime inicio = original.getFechaInicio().plusHours(1);
        solapada.setFechaInicio(inicio);
        solapada.setFechaFin(inicio.plusHours(1));
        solapada.setEstado(Asignacion.PROGRAMADA);

        NegocioException error = assertThrows(NegocioException.class,
                () -> FabricaServicios.asignaciones().guardar(solapada));
        assertEquals("El conductor ya tiene una asignación en ese horario.", error.getMessage());
    }
}
