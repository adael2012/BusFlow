package com.itla.autobuses.service;

import com.itla.autobuses.dao.BitacoraDAO;
import com.itla.autobuses.model.EntradaBitacora;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class BitacoraDAOFalso extends BitacoraDAO {

    final List<String> acciones = new ArrayList<>();
    final List<EntradaBitacora> entradas = new ArrayList<>();

    @Override
    public void insertar(int idUsuario, String accion, String tabla,
                         int idRegistro, String detalle, String resultado) {
        acciones.add(accion + ":" + tabla);
        EntradaBitacora entrada = new EntradaBitacora();
        entrada.setId(entradas.size() + 1);
        entrada.setAccion(accion);
        entrada.setTabla(tabla);
        entrada.setIdRegistro(idRegistro);
        entrada.setDetalle(detalle);
        entrada.setResultado(resultado);
        entrada.setFecha(LocalDateTime.now());
        entradas.add(entrada);
    }
}
