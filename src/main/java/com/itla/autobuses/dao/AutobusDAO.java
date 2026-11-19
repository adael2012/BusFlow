package com.itla.autobuses.dao;

import com.itla.autobuses.model.Autobus;
import com.itla.autobuses.util.ConexionDB;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AutobusDAO {

    public List<Autobus> listar() throws SQLException {
        List<Autobus> lista = new ArrayList<>();
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Autobus_Listar}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Autobus> buscar(String texto) throws SQLException {
        List<Autobus> lista = new ArrayList<>();
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Autobus_Buscar(?)}")) {
            cs.setString(1, texto);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public List<Autobus> listarOperativos() throws SQLException {
        List<Autobus> lista = new ArrayList<>();
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Autobus_ListarOperativos}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Autobus> listarDisponibles(LocalDateTime inicio, LocalDateTime fin) throws SQLException {
        List<Autobus> lista = new ArrayList<>();
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Autobus_Disponibles(?,?)}")) {
            cs.setTimestamp(1, Timestamp.valueOf(inicio));
            cs.setTimestamp(2, Timestamp.valueOf(fin));
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public int insertar(Autobus a) throws SQLException {
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Autobus_Insertar(?,?,?,?,?,?)}")) {
            cs.setString(1, a.getPlaca());
            cs.setString(2, a.getMarca());
            cs.setString(3, a.getModelo());
            cs.setInt(4, a.getAnio());
            cs.setInt(5, a.getCapacidad());
            cs.setString(6, a.getEstado());
            try (ResultSet rs = cs.executeQuery()) {
                return rs.next() ? rs.getInt("IdAutobus") : 0;
            }
        }
    }

    public void actualizar(Autobus a) throws SQLException {
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Autobus_Actualizar(?,?,?,?,?,?,?)}")) {
            cs.setInt(1, a.getId());
            cs.setString(2, a.getPlaca());
            cs.setString(3, a.getMarca());
            cs.setString(4, a.getModelo());
            cs.setInt(5, a.getAnio());
            cs.setInt(6, a.getCapacidad());
            cs.setString(7, a.getEstado());
            cs.execute();
        }
    }

    public void cambiarEstado(int id, String estado) throws SQLException {
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Autobus_CambiarEstado(?,?)}")) {
            cs.setInt(1, id);
            cs.setString(2, estado);
            cs.execute();
        }
    }

    private Autobus mapear(ResultSet rs) throws SQLException {
        Autobus a = new Autobus();
        a.setId(rs.getInt("IdAutobus"));
        a.setPlaca(rs.getString("Placa"));
        a.setMarca(rs.getString("Marca"));
        a.setModelo(rs.getString("Modelo"));
        a.setAnio(rs.getInt("Anio"));
        a.setCapacidad(rs.getInt("Capacidad"));
        a.setEstado(rs.getString("Estado"));
        return a;
    }
}
