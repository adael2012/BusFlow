package com.itla.autobuses.service;

import com.itla.autobuses.dao.BitacoraDAO;

import java.util.ArrayList;
import java.util.List;

class BitacoraDAOFalso extends BitacoraDAO {

    final List<String> acciones = new ArrayList<>();

    @Override
    public void insertar(int idUsuario, String accion, String tabla, int idRegistro, String detalle) {
        acciones.add(accion + ":" + tabla);
    }
}
