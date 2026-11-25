package com.itla.autobuses.demo;

import com.itla.autobuses.dao.RolDAO;
import com.itla.autobuses.model.Rol;

import java.util.ArrayList;
import java.util.List;

public class RolDAODemo extends RolDAO {

    private final DemoAlmacen almacen;

    public RolDAODemo(DemoAlmacen almacen) {
        this.almacen = almacen;
    }

    @Override
    public List<Rol> listar() {
        List<Rol> copia = new ArrayList<>();
        for (Rol r : almacen.roles) {
            copia.add(new Rol(r.getId(), r.getNombre()));
        }
        return copia;
    }
}
