package com.itla.autobuses.dao;

import com.itla.autobuses.model.Asignacion;
import com.itla.autobuses.util.ConexionDB;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AsignacionDAO {

    public List<Asignacion> listar() throws SQLException {
        List<Asignacion> lista = new ArrayList<>();
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Asignacion_Listar}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Asignacion> buscar(String texto) throws SQLException {
        List<Asignacion> lista = new ArrayList<>();
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Asignacion_Buscar(?)}")) {
            cs.setString(1, texto);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public int insertar(Asignacion a) throws SQLException {
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Asignacion_Insertar(?,?,?,?,?,?,?)}")) {
            cs.setInt(1, a.getIdConductor());
            cs.setInt(2, a.getIdAutobus());
            cs.setInt(3, a.getIdRuta());
            cs.setTimestamp(4, Timestamp.valueOf(a.getFechaInicio()));
            cs.setTimestamp(5, Timestamp.valueOf(a.getFechaFin()));
            cs.setString(6, a.getEstado());
            cs.setInt(7, a.getIdUsuarioRegistro());
            try (ResultSet rs = cs.executeQuery()) {
                return rs.next() ? rs.getInt("IdAsignacion") : 0;
            }
        }
    }

    public void actualizar(Asignacion a) throws SQLException {
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Asignacion_Actualizar(?,?,?,?,?,?,?)}")) {
            cs.setInt(1, a.getId());
            cs.setInt(2, a.getIdConductor());
            cs.setInt(3, a.getIdAutobus());
            cs.setInt(4, a.getIdRuta());
            cs.setTimestamp(5, Timestamp.valueOf(a.getFechaInicio()));
            cs.setTimestamp(6, Timestamp.valueOf(a.getFechaFin()));
            cs.setString(7, a.getEstado());
            cs.execute();
        }
    }

    public void cancelar(int id) throws SQLException {
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Asignacion_Cancelar(?)}")) {
            cs.setInt(1, id);
            cs.execute();
        }
    }

    public boolean conductorOcupado(int idConductor, LocalDateTime inicio,
                                    LocalDateTime fin, int idExcluido) throws SQLException {
        return haySolape("sp_Asignacion_ConductorOcupado", idConductor, inicio, fin, idExcluido);
    }

    public boolean autobusOcupado(int idAutobus, LocalDateTime inicio,
                                  LocalDateTime fin, int idExcluido) throws SQLException {
        return haySolape("sp_Asignacion_AutobusOcupado", idAutobus, inicio, fin, idExcluido);
    }

    public int contarDelDia(LocalDate dia) throws SQLException {
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Asignacion_ContarDelDia(?)}")) {
            cs.setDate(1, java.sql.Date.valueOf(dia));
            try (ResultSet rs = cs.executeQuery()) {
                return rs.next() ? rs.getInt("Total") : 0;
            }
        }
    }

    private boolean haySolape(String procedimiento, int id, LocalDateTime inicio,
                              LocalDateTime fin, int idExcluido) throws SQLException {
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call " + procedimiento + "(?,?,?,?)}")) {
            cs.setInt(1, id);
            cs.setTimestamp(2, Timestamp.valueOf(inicio));
            cs.setTimestamp(3, Timestamp.valueOf(fin));
            cs.setInt(4, idExcluido);
            try (ResultSet rs = cs.executeQuery()) {
                return rs.next() && rs.getInt("Total") > 0;
            }
        }
    }

    private Asignacion mapear(ResultSet rs) throws SQLException {
        Asignacion a = new Asignacion();
        a.setId(rs.getInt("IdAsignacion"));
        a.setIdConductor(rs.getInt("IdConductor"));
        a.setIdAutobus(rs.getInt("IdAutobus"));
        a.setIdRuta(rs.getInt("IdRuta"));
        Timestamp inicio = rs.getTimestamp("FechaInicio");
        Timestamp fin = rs.getTimestamp("FechaFin");
        a.setFechaInicio(inicio == null ? null : inicio.toLocalDateTime());
        a.setFechaFin(fin == null ? null : fin.toLocalDateTime());
        a.setEstado(rs.getString("Estado"));
        a.setNombreConductor(rs.getString("NombreConductor"));
        a.setPlacaAutobus(rs.getString("PlacaAutobus"));
        a.setCodigoRuta(rs.getString("CodigoRuta"));
        return a;
    }
}
