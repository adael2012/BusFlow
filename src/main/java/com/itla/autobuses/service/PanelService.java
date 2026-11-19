package com.itla.autobuses.service;

import com.itla.autobuses.dao.AsignacionDAO;
import com.itla.autobuses.dao.AutobusDAO;
import com.itla.autobuses.dao.ConductorDAO;
import com.itla.autobuses.model.Conductor;
import com.itla.autobuses.model.Resumen;
import com.itla.autobuses.util.NegocioException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PanelService {

    private static final int DIAS_ALERTA = 30;

    private final ConductorDAO conductores;
    private final AutobusDAO autobuses;
    private final AsignacionDAO asignaciones;

    public PanelService() {
        this(new ConductorDAO(), new AutobusDAO(), new AsignacionDAO());
    }

    public PanelService(ConductorDAO conductores, AutobusDAO autobuses, AsignacionDAO asignaciones) {
        this.conductores = conductores;
        this.autobuses = autobuses;
        this.asignaciones = asignaciones;
    }

    public Resumen resumen() throws NegocioException {
        Resumen r = new Resumen();
        try {
            List<Conductor> todos = conductores.listar();
            int activos = 0;
            int porVencer = 0;
            for (Conductor k : todos) {
                if (k.isActivo()) {
                    activos++;
                }
                if (k.isActivo() && licenciaPorVencer(k)) {
                    porVencer++;
                }
            }
            int operativos = 0;
            for (var b : autobuses.listar()) {
                if (b.estaOperativo()) {
                    operativos++;
                }
            }
            r.setConductoresActivos(activos);
            r.setAutobusesOperativos(operativos);
            r.setAsignacionesHoy(asignaciones.contarDelDia(LocalDate.now()));
            r.setLicenciasPorVencer(porVencer);
            return r;
        } catch (SQLException e) {
            throw UsuarioService.errorBD(e);
        }
    }

    public List<Conductor> licenciasPorVencer() throws NegocioException {
        try {
            List<Conductor> resultado = new ArrayList<>();
            for (Conductor k : conductores.listar()) {
                if (k.isActivo() && licenciaPorVencer(k)) {
                    resultado.add(k);
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw UsuarioService.errorBD(e);
        }
    }

    private boolean licenciaPorVencer(Conductor k) {
        LocalDate vence = k.getVencimientoLicencia();
        if (vence == null) {
            return false;
        }
        LocalDate hoy = LocalDate.now();
        return !vence.isBefore(hoy) && !vence.isAfter(hoy.plusDays(DIAS_ALERTA));
    }
}
