package com.itla.autobuses.service;

import com.itla.autobuses.dao.UsuarioDAO;
import com.itla.autobuses.model.Usuario;

import java.sql.SQLException;
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
        nuevo.setRolNombre(nuevo.getIdRol() == 1 ? "Administrador" : "Usuario Generico");
        nuevo.setActivo(true);
        datos.put(nuevo.getId(), nuevo);
        return nuevo.getId();
    }

    @Override
    public void actualizar(int idUsuario, int idRol, boolean activo) {
        Usuario u = datos.get(idUsuario);
        u.setIdRol(idRol);
        u.setRolNombre(idRol == 1 ? "Administrador" : "Usuario Generico");
        u.setActivo(activo);
    }

    @Override
    public void cambiarPassword(int idUsuario, String hash, String salt) {
        Usuario u = datos.get(idUsuario);
        u.setPasswordHash(hash);
        u.setSalt(salt);
    }

    @Override
    public void desactivar(int idUsuario) {
        datos.get(idUsuario).setActivo(false);
    }
}
