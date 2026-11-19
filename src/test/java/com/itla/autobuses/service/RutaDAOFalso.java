package com.itla.autobuses.service;

import com.itla.autobuses.dao.RutaDAO;
import com.itla.autobuses.model.Ruta;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

class RutaDAOFalso extends RutaDAO {

    private final List<Ruta> datos = new ArrayList<>();
    private int secuencia;

    @Override
    public List<Ruta> listar() {
        return new ArrayList<>(datos);
    }

    @Override
    public List<Ruta> buscar(String texto) {
        String t = texto.toLowerCase();
        List<Ruta> r = new ArrayList<>();
        for (Ruta x : datos) {
            if (x.getCodigo().toLowerCase().contains(t)
                    || x.getOrigen().toLowerCase().contains(t)
                    || x.getDestino().toLowerCase().contains(t)) {
                r.add(x);
            }
        }
        return r;
    }

    @Override
    public List<Ruta> listarActivas() {
        List<Ruta> r = new ArrayList<>();
        for (Ruta x : datos) {
            if (x.isActivo()) {
                r.add(x);
            }
        }
        return r;
    }

    @Override
    public int insertar(Ruta nuevo) throws SQLException {
        for (Ruta x : datos) {
            if (x.getCodigo().equalsIgnoreCase(nuevo.getCodigo())) {
                throw new SQLException("duplicado", "23000", 2627);
            }
        }
        nuevo.setId(++secuencia);
        datos.add(nuevo);
        return nuevo.getId();
    }

    @Override
    public void actualizar(Ruta r) {
        for (int i = 0; i < datos.size(); i++) {
            if (datos.get(i).getId() == r.getId()) {
                datos.set(i, r);
                return;
            }
        }
    }

    @Override
    public void desactivar(int id) {
        for (Ruta r : datos) {
            if (r.getId() == id) {
                r.setActivo(false);
                return;
            }
        }
    }
}
