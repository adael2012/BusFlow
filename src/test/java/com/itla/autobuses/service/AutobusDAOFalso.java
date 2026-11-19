package com.itla.autobuses.service;

import com.itla.autobuses.dao.AutobusDAO;
import com.itla.autobuses.model.Autobus;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class AutobusDAOFalso extends AutobusDAO {

    private final List<Autobus> datos = new ArrayList<>();
    private int secuencia;

    @Override
    public List<Autobus> listar() {
        return new ArrayList<>(datos);
    }

    @Override
    public List<Autobus> buscar(String texto) {
        String t = texto.toLowerCase();
        List<Autobus> r = new ArrayList<>();
        for (Autobus b : datos) {
            if (b.getPlaca().toLowerCase().contains(t)
                    || b.getMarca().toLowerCase().contains(t)
                    || b.getModelo().toLowerCase().contains(t)) {
                r.add(b);
            }
        }
        return r;
    }

    @Override
    public List<Autobus> listarOperativos() {
        List<Autobus> r = new ArrayList<>();
        for (Autobus b : datos) {
            if (b.estaOperativo()) {
                r.add(b);
            }
        }
        return r;
    }

    @Override
    public List<Autobus> listarDisponibles(LocalDateTime inicio, LocalDateTime fin) {
        return listarOperativos();
    }

    @Override
    public int insertar(Autobus nuevo) throws SQLException {
        for (Autobus b : datos) {
            if (b.getPlaca().equalsIgnoreCase(nuevo.getPlaca())) {
                throw new SQLException("duplicado", "23000", 2627);
            }
        }
        nuevo.setId(++secuencia);
        datos.add(nuevo);
        return nuevo.getId();
    }

    @Override
    public void actualizar(Autobus b) {
        for (int i = 0; i < datos.size(); i++) {
            if (datos.get(i).getId() == b.getId()) {
                datos.set(i, b);
                return;
            }
        }
    }

    @Override
    public void cambiarEstado(int id, String estado) {
        for (Autobus b : datos) {
            if (b.getId() == id) {
                b.setEstado(estado);
                return;
            }
        }
    }
}
