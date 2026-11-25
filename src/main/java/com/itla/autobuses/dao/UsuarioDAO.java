package com.itla.autobuses.dao;

import com.itla.autobuses.model.Usuario;
import com.itla.autobuses.util.ConexionDB;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public int contar() throws SQLException {
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Usuario_Contar}");
             ResultSet rs = cs.executeQuery()) {
            return rs.next() ? rs.getInt("Total") : 0;
        }
    }

    public Usuario obtenerPorUsername(String username) throws SQLException {
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Usuario_ObtenerPorUsername(?)}")) {
            cs.setString(1, username);
            try (ResultSet rs = cs.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<Usuario> listar() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Usuario_Listar}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public int insertar(Usuario u) throws SQLException {
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Usuario_Insertar(?,?,?,?)}")) {
            cs.setString(1, u.getUsername());
            cs.setString(2, u.getPasswordHash());
            cs.setString(3, u.getSalt());
            cs.setInt(4, u.getIdRol());
            try (ResultSet rs = cs.executeQuery()) {
                return rs.next() ? rs.getInt("IdUsuario") : 0;
            }
        }
    }

    public void actualizar(int idUsuario, int idRol, boolean activo) throws SQLException {
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Usuario_Actualizar(?,?,?)}")) {
            cs.setInt(1, idUsuario);
            cs.setInt(2, idRol);
            cs.setBoolean(3, activo);
            cs.execute();
        }
    }

    public void cambiarPassword(int idUsuario, String hash, String salt) throws SQLException {
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Usuario_CambiarPassword(?,?,?)}")) {
            cs.setInt(1, idUsuario);
            cs.setString(2, hash);
            cs.setString(3, salt);
            cs.execute();
        }
    }

    public void desactivar(int idUsuario) throws SQLException {
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Usuario_Desactivar(?)}")) {
            cs.setInt(1, idUsuario);
            cs.execute();
        }
    }

    public void eliminar(int idUsuario) throws SQLException {
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Usuario_Eliminar(?)}")) {
            cs.setInt(1, idUsuario);
            cs.execute();
        }
    }

    public void reactivar(int idUsuario) throws SQLException {
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Usuario_Reactivar(?)}")) {
            cs.setInt(1, idUsuario);
            cs.execute();
        }
    }

    public void registrarAcceso(int idUsuario) throws SQLException {
        try (Connection c = ConexionDB.getInstancia().getConexion();
             CallableStatement cs = ConexionDB.getInstancia().preparar(c, "{call sp_Usuario_RegistrarAcceso(?)}")) {
            cs.setInt(1, idUsuario);
            cs.execute();
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("IdUsuario"));
        u.setUsername(rs.getString("Username"));
        u.setPasswordHash(rs.getString("PasswordHash"));
        u.setSalt(rs.getString("Salt"));
        u.setIdRol(rs.getInt("IdRol"));
        u.setRolNombre(rs.getString("RolNombre"));
        u.setActivo(rs.getBoolean("Activo"));
        u.setEstadoCuenta(rs.getString("EstadoCuenta"));
        u.setFechaCreacion(fecha(rs, "FechaCreacion"));
        u.setFechaModificacion(fecha(rs, "FechaModificacion"));
        u.setUltimoAcceso(fecha(rs, "UltimoAcceso"));
        u.setEliminado(rs.getBoolean("Eliminado"));
        u.setFechaEliminacion(fecha(rs, "FechaEliminacion"));
        return u;
    }

    private LocalDateTime fecha(ResultSet rs, String columna) throws SQLException {
        Timestamp valor = rs.getTimestamp(columna);
        return valor == null ? null : valor.toLocalDateTime();
    }
}
