package com.itla.autobuses.dao;

import com.itla.autobuses.model.EntradaBitacora;
import com.itla.autobuses.util.ConexionDB;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BitacoraDAO {

    public void insertar(int idUsuario, String accion, String tabla,
                         int idRegistro, String detalle) throws SQLException {
        insertar(idUsuario, accion, tabla, idRegistro, detalle, "ÉXITO");
    }

    public void insertar(int idUsuario, String accion, String tabla,
                         int idRegistro, String detalle, String resultado) throws SQLException {
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c,
                     "{call sp_Bitacora_InsertarSeguro(?,?,?,?,?,?)}")) {
            cs.setInt(1, idUsuario);
            cs.setString(2, accion);
            cs.setString(3, tabla);
            cs.setInt(4, idRegistro);
            cs.setString(5, detalle);
            cs.setString(6, resultado);
            cs.execute();
        }
    }

    public List<EntradaBitacora> listar(String username, String accion,
                                        LocalDate desde, LocalDate hasta) throws SQLException {
        List<EntradaBitacora> lista = new ArrayList<>();
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Bitacora_Listar(?,?,?,?)}")) {
            cs.setString(1, nuloSiVacio(username));
            cs.setString(2, nuloSiVacio(accion));
            cs.setDate(3, desde == null ? null : Date.valueOf(desde));
            cs.setDate(4, hasta == null ? null : Date.valueOf(hasta));
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private String nuloSiVacio(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private EntradaBitacora mapear(ResultSet rs) throws SQLException {
        EntradaBitacora e = new EntradaBitacora();
        e.setId(rs.getInt("IdBitacora"));
        e.setUsername(rs.getString("Username"));
        e.setAccion(rs.getString("Accion"));
        e.setTabla(rs.getString("Tabla"));
        e.setIdRegistro(rs.getInt("IdRegistro"));
        Timestamp fecha = rs.getTimestamp("Fecha");
        e.setFecha(fecha == null ? null : fecha.toLocalDateTime());
        e.setDetalle(rs.getString("Detalle"));
        e.setResultado(rs.getString("Resultado"));
        return e;
    }
}
