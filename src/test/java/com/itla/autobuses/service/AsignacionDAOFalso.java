package com.itla.autobuses.service;

import com.itla.autobuses.dao.AsignacionDAO;
import com.itla.autobuses.model.Asignacion;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class AsignacionDAOFalso extends AsignacionDAO {

    private final List<Asignacion> datos = new ArrayList<>();
    private int secuencia;

    @Override
    public List<Asignacion> listar() {
        return new ArrayList<>(datos);
    }

    @Override
    public List<Asignacion> buscar(String texto) {
        String t = texto.toLowerCase();
        List<Asignacion> r = new ArrayList<>();
        for (Asignacion a : datos) {
            if (contiene(a.getNombreConductor(), t)
                    || contiene(a.getPlacaAutobus(), t)
                    || contiene(a.getCodigoRuta(), t)) {
                r.add(a);
            }
        }
        return r;
    }

    @Override
    public int insertar(Asignacion nuevo) throws SQLException {
        nuevo.setId(++secuencia);
        datos.add(nuevo);
        return nuevo.getId();
    }

    @Override
    public void actualizar(Asignacion a) {
        for (int i = 0; i < datos.size(); i++) {
            if (datos.get(i).getId() == a.getId()) {
                datos.set(i, a);
                return;
            }
        }
    }

    @Override
    public void cancelar(int id) {
        for (Asignacion a : datos) {
            if (a.getId() == id) {
                a.setEstado(Asignacion.CANCELADA);
                return;
            }
        }
    }

    @Override
    public boolean conductorOcupado(int idConductor, LocalDateTime inicio,
                                    LocalDateTime fin, int idExcluido) {
        return ocupado(idConductor, inicio, fin, idExcluido, true);
    }

    @Override
    public boolean autobusOcupado(int idAutobus, LocalDateTime inicio,
                                  LocalDateTime fin, int idExcluido) {
        return ocupado(idAutobus, inicio, fin, idExcluido, false);
    }

    @Override
    public int contarDelDia(LocalDate dia) {
        int total = 0;
        for (Asignacion a : datos) {
            if (a.getFechaInicio() != null
                    && a.getFechaInicio().toLocalDate().equals(dia)
                    && !Asignacion.CANCELADA.equals(a.getEstado())) {
                total++;
            }
        }
        return total;
    }

    private boolean ocupado(int id, LocalDateTime inicio, LocalDateTime fin,
                            int idExcluido, boolean porConductor) {
        for (Asignacion a : datos) {
            if (a.getId() == idExcluido || !Asignacion.PROGRAMADA.equals(a.getEstado())) {
                continue;
            }
            int valor = porConductor ? a.getIdConductor() : a.getIdAutobus();
            if (valor == id && inicio.isBefore(a.getFechaFin()) && fin.isAfter(a.getFechaInicio())) {
                return true;
            }
        }
        return false;
    }

    private boolean contiene(String valor, String texto) {
        return valor != null && valor.toLowerCase().contains(texto);
    }
}
