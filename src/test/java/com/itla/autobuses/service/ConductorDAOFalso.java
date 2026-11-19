package com.itla.autobuses.service;

import com.itla.autobuses.dao.ConductorDAO;
import com.itla.autobuses.model.Conductor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

class ConductorDAOFalso extends ConductorDAO {

    private final List<Conductor> datos = new ArrayList<>();
    private int secuencia = 0;

    @Override
    public List<Conductor> listar() {
        return new ArrayList<>(datos);
    }

    @Override
    public List<Conductor> buscar(String texto) {
        List<Conductor> r = new ArrayList<>();
        String t = texto.toLowerCase();
        for (Conductor k : datos) {
            if (k.getCedula().contains(t) || k.getNombre().toLowerCase().contains(t)
                    || k.getApellido().toLowerCase().contains(t)
                    || k.getNumeroLicencia().toLowerCase().contains(t)) {
                r.add(k);
            }
        }
        return r;
    }

    @Override
    public int insertar(Conductor nuevo) throws SQLException {
        for (Conductor k : datos) {
            if (k.getCedula().equals(nuevo.getCedula())
                    || k.getNumeroLicencia().equals(nuevo.getNumeroLicencia())) {
                throw new SQLException("duplicado", "23000", 2627);
            }
        }
        nuevo.setId(++secuencia);
        datos.add(nuevo);
        return nuevo.getId();
    }

    @Override
    public void actualizar(Conductor k) {
        for (int i = 0; i < datos.size(); i++) {
            if (datos.get(i).getId() == k.getId()) {
                datos.set(i, k);
            }
        }
    }

    @Override
    public void desactivar(int id) {
        for (Conductor k : datos) {
            if (k.getId() == id) {
                k.setActivo(false);
            }
        }
    }
}
