package com.itla.autobuses.demo;

import com.itla.autobuses.dao.UsuarioDAO;
import com.itla.autobuses.model.Usuario;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAODemo extends UsuarioDAO {

    private final DemoAlmacen almacen;

    public UsuarioDAODemo(DemoAlmacen almacen) {
        this.almacen = almacen;
    }

    @Override
    public int contar() {
        return almacen.usuarios.size();
    }

    @Override
    public Usuario obtenerPorUsername(String username) {
        Usuario u = almacen.usuarioPorUsername(username);
        return u == null ? null : copia(u);
    }

    @Override
    public List<Usuario> listar() {
        List<Usuario> copia = new ArrayList<>();
        for (Usuario u : almacen.usuarios) {
            copia.add(copia(u));
        }
        return copia;
    }

    @Override
    public int insertar(Usuario u) throws SQLException {
        if (almacen.usuarioPorUsername(u.getUsername()) != null) {
            throw DemoAlmacen.duplicado("Ya existe el usuario " + u.getUsername());
        }
        u.setId(almacen.siguienteUsuario());
        almacen.aplicarRol(u);
        almacen.usuarios.add(copia(u));
        return u.getId();
    }

    @Override
    public void actualizar(int idUsuario, int idRol, boolean activo) {
        for (Usuario u : almacen.usuarios) {
            if (u.getId() == idUsuario) {
                u.setIdRol(idRol);
                u.setActivo(activo);
                almacen.aplicarRol(u);
                return;
            }
        }
    }

    @Override
    public void cambiarPassword(int idUsuario, String hash, String salt) {
        for (Usuario u : almacen.usuarios) {
            if (u.getId() == idUsuario) {
                u.setPasswordHash(hash);
                u.setSalt(salt);
                return;
            }
        }
    }

    @Override
    public void desactivar(int idUsuario) {
        for (Usuario u : almacen.usuarios) {
            if (u.getId() == idUsuario) {
                u.setActivo(false);
                return;
            }
        }
    }

    private Usuario copia(Usuario original) {
        Usuario u = new Usuario();
        u.setId(original.getId());
        u.setUsername(original.getUsername());
        u.setPasswordHash(original.getPasswordHash());
        u.setSalt(original.getSalt());
        u.setIdRol(original.getIdRol());
        u.setRolNombre(original.getRolNombre());
        u.setActivo(original.isActivo());
        return u;
    }
}
