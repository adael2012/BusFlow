package com.itla.autobuses.demo;

import com.itla.autobuses.dao.ConductorDAO;
import com.itla.autobuses.model.Conductor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConductorDAODemo extends ConductorDAO {

    private final DemoAlmacen almacen;

    public ConductorDAODemo(DemoAlmacen almacen) {
        this.almacen = almacen;
    }

    @Override
    public List<Conductor> listar() {
        List<Conductor> copia = new ArrayList<>();
        for (Conductor c : almacen.conductores) {
            copia.add(copia(c));
        }
        return copia;
    }

    @Override
    public List<Conductor> buscar(String texto) {
        String filtro = texto == null ? "" : texto.trim().toLowerCase();
        List<Conductor> resultado = new ArrayList<>();
        for (Conductor c : almacen.conductores) {
            if (contiene(c.getCedula(), filtro)
                    || contiene(c.getNombre(), filtro)
                    || contiene(c.getApellido(), filtro)
                    || contiene(c.getNumeroLicencia(), filtro)) {
                resultado.add(copia(c));
            }
        }
        return resultado;
    }

    @Override
    public int insertar(Conductor k) throws SQLException {
        for (Conductor c : almacen.conductores) {
            if (c.getCedula().equalsIgnoreCase(k.getCedula())
                    || c.getNumeroLicencia().equalsIgnoreCase(k.getNumeroLicencia())) {
                throw DemoAlmacen.duplicado("Conductor duplicado");
            }
        }
        k.setId(almacen.siguienteConductor());
        almacen.conductores.add(copia(k));
        return k.getId();
    }

    @Override
    public void actualizar(Conductor k) {
        for (int i = 0; i < almacen.conductores.size(); i++) {
            if (almacen.conductores.get(i).getId() == k.getId()) {
                almacen.conductores.set(i, copia(k));
                return;
            }
        }
    }

    @Override
    public void desactivar(int idConductor) {
        for (Conductor c : almacen.conductores) {
            if (c.getId() == idConductor) {
                c.setActivo(false);
                return;
            }
        }
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null && valor.toLowerCase().contains(filtro);
    }

    private Conductor copia(Conductor original) {
        Conductor c = new Conductor();
        c.setId(original.getId());
        c.setCedula(original.getCedula());
        c.setNombre(original.getNombre());
        c.setApellido(original.getApellido());
        c.setTelefono(original.getTelefono());
        c.setNumeroLicencia(original.getNumeroLicencia());
        c.setVencimientoLicencia(original.getVencimientoLicencia());
        c.setActivo(original.isActivo());
        return c;
    }
}
