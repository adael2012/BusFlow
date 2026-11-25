package com.itla.autobuses.demo;

import com.itla.autobuses.dao.BitacoraDAO;
import com.itla.autobuses.model.EntradaBitacora;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BitacoraDAODemo extends BitacoraDAO {

    private final DemoAlmacen almacen;

    public BitacoraDAODemo(DemoAlmacen almacen) {
        this.almacen = almacen;
    }

    @Override
    public void insertar(int idUsuario, String accion, String tabla,
                         int idRegistro, String detalle) {
        EntradaBitacora e = new EntradaBitacora();
        e.setId(almacen.siguienteBitacora());
        e.setUsername(almacen.usuarioPorId(idUsuario) == null
                ? "" : almacen.usuarioPorId(idUsuario).getUsername());
        e.setAccion(accion);
        e.setTabla(tabla);
        e.setIdRegistro(idRegistro);
        e.setDetalle(detalle);
        e.setFecha(LocalDateTime.now());
        almacen.bitacora.add(e);
    }

    @Override
    public List<EntradaBitacora> listar(String username, String accion,
                                        LocalDate desde, LocalDate hasta) {
        String filtroUsuario = username == null ? "" : username.trim().toLowerCase();
        String filtroAccion = accion == null ? "" : accion.trim().toLowerCase();
        List<EntradaBitacora> resultado = new ArrayList<>();
        for (EntradaBitacora e : almacen.bitacora) {
            if (!filtroUsuario.isEmpty() && !contiene(e.getUsername(), filtroUsuario)) {
                continue;
            }
            if (!filtroAccion.isEmpty() && !contiene(e.getAccion(), filtroAccion)) {
                continue;
            }
            LocalDate fecha = e.getFecha() == null ? null : e.getFecha().toLocalDate();
            if (desde != null && (fecha == null || fecha.isBefore(desde))) {
                continue;
            }
            if (hasta != null && (fecha == null || fecha.isAfter(hasta))) {
                continue;
            }
            resultado.add(copia(e));
        }
        resultado.sort((a, b) -> {
            if (a.getFecha() == null && b.getFecha() == null) {
                return 0;
            }
            if (a.getFecha() == null) {
                return 1;
            }
            if (b.getFecha() == null) {
                return -1;
            }
            return b.getFecha().compareTo(a.getFecha());
        });
        return resultado;
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null && valor.toLowerCase().contains(filtro);
    }

    private EntradaBitacora copia(EntradaBitacora original) {
        EntradaBitacora e = new EntradaBitacora();
        e.setId(original.getId());
        e.setUsername(original.getUsername());
        e.setAccion(original.getAccion());
        e.setTabla(original.getTabla());
        e.setIdRegistro(original.getIdRegistro());
        e.setFecha(original.getFecha());
        e.setDetalle(original.getDetalle());
        return e;
    }
}
