package com.itla.autobuses.service;

import com.itla.autobuses.dao.UsuarioDAO;
import com.itla.autobuses.model.Rol;
import com.itla.autobuses.model.Usuario;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class UsuarioDAOFalso extends UsuarioDAO {

    private final Map<Integer, Usuario> datos = new LinkedHashMap<>();
    private int secuencia = 0;

    @Override
    public int contar() {
        return datos.size();
    }

    @Override
    public Usuario obtenerPorUsername(String username) {
        for (Usuario u : datos.values()) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public List<Usuario> listar() {
        return new ArrayList<>(datos.values());
    }

    @Override
    public int insertar(Usuario nuevo) throws SQLException {
        if (obtenerPorUsername(nuevo.getUsername()) != null) {
            throw new SQLException("duplicado", "23000", 2627);
        }
        nuevo.setId(++secuencia);
        nuevo.setRolNombre(nombreRol(nuevo.getIdRol()));
        nuevo.setActivo(true);
        nuevo.setEstadoCuenta("Activo");
        if (nuevo.getFechaCreacion() == null) {
            nuevo.setFechaCreacion(LocalDateTime.now());
        }
        datos.put(nuevo.getId(), nuevo);
        return nuevo.getId();
    }

    @Override
    public void actualizar(int idUsuario, int idRol, boolean activo) {
        Usuario u = datos.get(idUsuario);
        if (u == null) {
            return;
        }
        u.setIdRol(idRol);
        u.setRolNombre(nombreRol(idRol));
        u.setActivo(activo);
        u.setEstadoCuenta(activo ? "Activo" : "Inactivo");
        u.setFechaModificacion(LocalDateTime.now());
    }

    @Override
    public void cambiarPassword(int idUsuario, String hash, String salt) {
        Usuario u = datos.get(idUsuario);
        if (u != null) {
            u.setPasswordHash(hash);
            u.setSalt(salt);
            u.setFechaModificacion(LocalDateTime.now());
        }
    }

    @Override
    public void desactivar(int idUsuario) {
        Usuario u = datos.get(idUsuario);
        if (u != null) {
            u.setActivo(false);
            u.setEstadoCuenta("Inactivo");
            u.setFechaModificacion(LocalDateTime.now());
        }
    }

    @Override
    public void eliminar(int idUsuario) {
        Usuario u = datos.get(idUsuario);
        if (u != null) {
            u.setActivo(false);
            u.setEliminado(true);
            u.setEstadoCuenta("Eliminado");
            u.setFechaEliminacion(LocalDateTime.now());
            u.setFechaModificacion(LocalDateTime.now());
        }
    }

    @Override
    public void reactivar(int idUsuario) {
        Usuario u = datos.get(idUsuario);
        if (u != null) {
            u.setActivo(true);
            u.setEliminado(false);
            u.setEstadoCuenta("Activo");
            u.setFechaEliminacion(null);
            u.setFechaModificacion(LocalDateTime.now());
        }
    }

    @Override
    public void registrarAcceso(int idUsuario) {
        Usuario u = datos.get(idUsuario);
        if (u != null) {
            u.setUltimoAcceso(LocalDateTime.now());
        }
    }

    private String nombreRol(int idRol) {
        if (idRol == 1) {
            return Rol.ADMINISTRADOR;
        }
        if (idRol == 3) {
            return Rol.SUPERADMINISTRADOR;
        }
        return Rol.USUARIO_GENERICO;
    }
}
