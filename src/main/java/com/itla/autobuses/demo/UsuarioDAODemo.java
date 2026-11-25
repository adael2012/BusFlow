package com.itla.autobuses.demo;

import com.itla.autobuses.dao.UsuarioDAO;
import com.itla.autobuses.model.Usuario;

import java.sql.SQLException;
import java.time.LocalDateTime;
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
                u.setEliminado(false);
                u.setFechaEliminacion(null);
                u.setEstadoCuenta(activo ? "Activo" : "Inactivo");
                u.setFechaModificacion(LocalDateTime.now());
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
                u.setFechaModificacion(LocalDateTime.now());
                return;
            }
        }
    }

    @Override
    public void desactivar(int idUsuario) {
        for (Usuario u : almacen.usuarios) {
            if (u.getId() == idUsuario) {
                u.setActivo(false);
                u.setEstadoCuenta("Inactivo");
                u.setFechaModificacion(LocalDateTime.now());
                return;
            }
        }
    }

    @Override
    public void eliminar(int idUsuario) {
        for (Usuario u : almacen.usuarios) {
            if (u.getId() == idUsuario) {
                u.setActivo(false);
                u.setEliminado(true);
                u.setEstadoCuenta("Eliminado");
                u.setFechaEliminacion(LocalDateTime.now());
                u.setFechaModificacion(LocalDateTime.now());
                return;
            }
        }
    }

    @Override
    public void reactivar(int idUsuario) {
        for (Usuario u : almacen.usuarios) {
            if (u.getId() == idUsuario) {
                u.setActivo(true);
                u.setEliminado(false);
                u.setEstadoCuenta("Activo");
                u.setFechaEliminacion(null);
                u.setFechaModificacion(LocalDateTime.now());
                return;
            }
        }
    }

    @Override
    public void registrarAcceso(int idUsuario) {
        for (Usuario u : almacen.usuarios) {
            if (u.getId() == idUsuario) {
                u.setUltimoAcceso(LocalDateTime.now());
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
        u.setEstadoCuenta(original.getEstadoCuenta());
        u.setFechaCreacion(original.getFechaCreacion());
        u.setFechaModificacion(original.getFechaModificacion());
        u.setUltimoAcceso(original.getUltimoAcceso());
        u.setEliminado(original.isEliminado());
        u.setFechaEliminacion(original.getFechaEliminacion());
        return u;
    }
}
