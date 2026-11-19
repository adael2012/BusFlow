package com.itla.autobuses.service;

import com.itla.autobuses.model.Asignacion;
import com.itla.autobuses.model.Autobus;
import com.itla.autobuses.model.Conductor;
import com.itla.autobuses.model.Resumen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PanelServiceTest extends PruebaBase {

    private AutobusDAOFalso autobusDAO;
    private AsignacionDAOFalso asignacionDAO;
    private PanelService servicio;

    @BeforeEach
    void prepararModulo() {
        autobusDAO = new AutobusDAOFalso();
        asignacionDAO = new AsignacionDAOFalso();
        servicio = new PanelService(conductorDAO, autobusDAO, asignacionDAO);
    }

    private Conductor conductor(String cedula, String licencia, LocalDate vence, boolean activo)
            throws Exception {
        Conductor k = conductorValido();
        k.setCedula(cedula);
        k.setNumeroLicencia(licencia);
        k.setVencimientoLicencia(vence);
        k.setActivo(activo);
        conductorDAO.insertar(k);
        return k;
    }

    @Test
    @DisplayName("UT-61 El resumen cuenta conductores, autobuses y asignaciones")
    void resumen() throws Exception {
        conductor("00111111111", "L-101", LocalDate.now().plusYears(1), true);
        conductor("00222222222", "L-102", LocalDate.now().plusDays(10), true);
        conductor("00333333333", "L-103", LocalDate.now().plusDays(5), false);

        Autobus operativo = autobusValido();
        autobusDAO.insertar(operativo);
        Autobus mantenimiento = autobusValido();
        mantenimiento.setPlaca("XYZ789");
        mantenimiento.setEstado(Autobus.MANTENIMIENTO);
        autobusDAO.insertar(mantenimiento);

        Asignacion a = new Asignacion();
        a.setFechaInicio(LocalDateTime.now().withHour(9).withMinute(0).withSecond(0).withNano(0));
        asignacionDAO.insertar(a);

        Resumen r = servicio.resumen();

        assertEquals(2, r.getConductoresActivos());
        assertEquals(1, r.getAutobusesOperativos());
        assertEquals(1, r.getAsignacionesHoy());
        assertEquals(1, r.getLicenciasPorVencer());
    }

    @Test
    @DisplayName("UT-62 La alerta solo incluye conductores activos con licencia por vencer")
    void alertaLicencias() throws Exception {
        conductor("00111111111", "L-101", LocalDate.now().plusYears(1), true);
        Conductor porVencer = conductor("00222222222", "L-102",
                LocalDate.now().plusDays(15), true);
        conductor("00333333333", "L-103", LocalDate.now().plusDays(5), false);

        var alerta = servicio.licenciasPorVencer();

        assertEquals(1, alerta.size());
        assertEquals(porVencer.getId(), alerta.get(0).getId());
    }
}
