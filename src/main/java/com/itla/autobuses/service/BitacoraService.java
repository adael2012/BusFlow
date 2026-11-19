package com.itla.autobuses.service;

import com.itla.autobuses.dao.BitacoraDAO;
import com.itla.autobuses.model.EntradaBitacora;
import com.itla.autobuses.util.NegocioException;
import com.itla.autobuses.util.Sesion;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class BitacoraService {

    private final BitacoraDAO dao;

    public BitacoraService() {
        this(new BitacoraDAO());
    }

    public BitacoraService(BitacoraDAO dao) {
        this.dao = dao;
    }

    public List<EntradaBitacora> listar(String username, String accion,
                                        LocalDate desde, LocalDate hasta) throws NegocioException {
        exigirAdmin();
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new NegocioException("La fecha hasta debe ser posterior a la fecha desde.");
        }
        try {
            return dao.listar(username, accion, desde, hasta);
        } catch (SQLException e) {
            throw UsuarioService.errorBD(e);
        }
    }

    private void exigirAdmin() throws NegocioException {
        if (!Sesion.getInstancia().esAdmin()) {
            throw new NegocioException("Solo el Administrador puede consultar la bitacora.");
        }
    }
}
