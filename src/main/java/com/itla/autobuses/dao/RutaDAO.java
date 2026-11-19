package com.itla.autobuses.dao;

import com.itla.autobuses.model.Ruta;
import com.itla.autobuses.util.ConexionDB;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RutaDAO {

    public List<Ruta> listar() throws SQLException {
        List<Ruta> lista = new ArrayList<>();
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Ruta_Listar}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Ruta> buscar(String texto) throws SQLException {
        List<Ruta> lista = new ArrayList<>();
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Ruta_Buscar(?)}")) {
            cs.setString(1, texto);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public List<Ruta> listarActivas() throws SQLException {
        List<Ruta> lista = new ArrayList<>();
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Ruta_ListarActivas}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public int insertar(Ruta r) throws SQLException {
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Ruta_Insertar(?,?,?,?,?)}")) {
            cs.setString(1, r.getCodigo());
            cs.setString(2, r.getOrigen());
            cs.setString(3, r.getDestino());
            cs.setBigDecimal(4, r.getDistanciaKm());
            cs.setInt(5, r.getDuracionMin());
            try (ResultSet rs = cs.executeQuery()) {
                return rs.next() ? rs.getInt("IdRuta") : 0;
            }
        }
    }

    public void actualizar(Ruta r) throws SQLException {
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Ruta_Actualizar(?,?,?,?,?,?,?)}")) {
            cs.setInt(1, r.getId());
            cs.setString(2, r.getCodigo());
            cs.setString(3, r.getOrigen());
            cs.setString(4, r.getDestino());
            cs.setBigDecimal(5, r.getDistanciaKm());
            cs.setInt(6, r.getDuracionMin());
            cs.setBoolean(7, r.isActivo());
            cs.execute();
        }
    }

    public void desactivar(int id) throws SQLException {
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Ruta_Desactivar(?)}")) {
            cs.setInt(1, id);
            cs.execute();
        }
    }

    private Ruta mapear(ResultSet rs) throws SQLException {
        Ruta r = new Ruta();
        r.setId(rs.getInt("IdRuta"));
        r.setCodigo(rs.getString("Codigo"));
        r.setOrigen(rs.getString("Origen"));
        r.setDestino(rs.getString("Destino"));
        r.setDistanciaKm(rs.getBigDecimal("DistanciaKm"));
        r.setDuracionMin(rs.getInt("DuracionMin"));
        r.setActivo(rs.getBoolean("Activo"));
        return r;
    }
}
