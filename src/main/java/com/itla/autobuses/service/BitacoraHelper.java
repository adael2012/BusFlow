package com.itla.autobuses.service;

import com.itla.autobuses.dao.BitacoraDAO;
import com.itla.autobuses.util.Sesion;

final class BitacoraHelper {

    private static BitacoraDAO dao = new BitacoraDAO();

    private BitacoraHelper() { }

    static void usar(BitacoraDAO nuevo) {
        dao = nuevo;
    }

    static void registrar(String accion, String tabla, int idRegistro, String detalle) {
        registrar(accion, tabla, idRegistro, detalle, "ÉXITO");
    }

    static void registrar(String accion, String tabla, int idRegistro,
                          String detalle, String resultado) {
        Sesion sesion = Sesion.getInstancia();
        if (!sesion.hayUsuario()) {
            return;
        }
        registrar(sesion.getUsuario().getId(), accion, tabla, idRegistro, detalle, resultado);
    }

    static void registrar(int idActor, String accion, String tabla, int idRegistro,
                          String detalle, String resultado) {
        if (idActor <= 0) {
            return;
        }
        try {
            dao.insertar(idActor, accion, tabla, idRegistro, detalle, resultado);
        } catch (Exception ignored) {

        }
    }
}
