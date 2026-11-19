package com.itla.autobuses.service;

import com.itla.autobuses.dao.RolDAO;
import com.itla.autobuses.dao.UsuarioDAO;
import com.itla.autobuses.model.Rol;
import com.itla.autobuses.model.Usuario;
import com.itla.autobuses.util.NegocioException;
import com.itla.autobuses.util.Seguridad;
import com.itla.autobuses.util.Sesion;

import java.sql.SQLException;
import java.util.List;

public class UsuarioService {

    public static final String ADMIN_INICIAL = "admin";
    public static final String CLAVE_INICIAL = "Admin123!";

    private final UsuarioDAO usuarioDAO;
    private final RolDAO rolDAO;

    public UsuarioService() {
        this(new UsuarioDAO(), new RolDAO());
    }

    public UsuarioService(UsuarioDAO usuarioDAO, RolDAO rolDAO) {
        this.usuarioDAO = usuarioDAO;
        this.rolDAO = rolDAO;
    }

    public void crearAdminInicialSiNoExiste() throws SQLException {
        if (usuarioDAO.contar() > 0) {
            return;
        }
        for (Rol r : rolDAO.listar()) {
            if (Rol.ADMINISTRADOR.equals(r.getNombre())) {
                Usuario u = construir(ADMIN_INICIAL, CLAVE_INICIAL, r.getId());
                usuarioDAO.insertar(u);
                return;
            }
        }
        throw new SQLException("No existe el rol Administrador. Ejecuta database/script.sql.");
    }

    public List<Rol> listarRoles() throws NegocioException {
        try {
            return rolDAO.listar();
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public List<Usuario> listar() throws NegocioException {
        exigirAdmin();
        try {
            return usuarioDAO.listar();
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public int crear(String username, String password, int idRol) throws NegocioException {
        exigirAdmin();
        validarUsername(username);
        validarPassword(password);
        validarRol(idRol);
        try {
            int id = usuarioDAO.insertar(construir(username.trim(), password, idRol));
            BitacoraHelper.registrar("CREAR", "Usuario", id, "Usuario " + username.trim());
            return id;
        } catch (SQLException e) {
            if (esDuplicado(e)) {
                throw new NegocioException("Ya existe un usuario con ese nombre.");
            }
            throw errorBD(e);
        }
    }

    public void actualizar(Usuario u, int idRol, boolean activo) throws NegocioException {
        exigirAdmin();
        if (u == null) {
            throw new NegocioException("Selecciona un usuario de la tabla.");
        }
        validarRol(idRol);
        if (u.getId() == Sesion.getInstancia().getUsuario().getId()) {
            if (!activo) {
                throw new NegocioException("No puedes desactivar tu propio usuario.");
            }
            if (!esAdministrador(idRol)) {
                throw new NegocioException("No puedes quitarte el rol de administrador a ti mismo.");
            }
        }
        try {
            usuarioDAO.actualizar(u.getId(), idRol, activo);
            BitacoraHelper.registrar("ACTUALIZAR", "Usuario", u.getId(), "Usuario " + u.getUsername());
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public void restablecerPassword(Usuario u, String nueva) throws NegocioException {
        exigirAdmin();
        validarPassword(nueva);
        String salt = Seguridad.generarSalt();
        try {
            usuarioDAO.cambiarPassword(u.getId(), Seguridad.hash(nueva, salt), salt);
            BitacoraHelper.registrar("PASSWORD", "Usuario", u.getId(), "Restablecida: " + u.getUsername());
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public void cambiarPasswordPropio(String actual, String nueva) throws NegocioException {
        Usuario yo = Sesion.getInstancia().getUsuario();
        if (yo == null) {
            throw new NegocioException("No hay una sesion activa.");
        }
        if (actual == null || actual.isBlank()
                || !Seguridad.verificar(actual, yo.getSalt(), yo.getPasswordHash())) {
            throw new NegocioException("La contrasena actual es incorrecta.");
        }
        validarPassword(nueva);
        if (Seguridad.verificar(nueva, yo.getSalt(), yo.getPasswordHash())) {
            throw new NegocioException("La nueva contrasena debe ser diferente a la actual.");
        }
        String salt = Seguridad.generarSalt();
        String hash = Seguridad.hash(nueva, salt);
        try {
            usuarioDAO.cambiarPassword(yo.getId(), hash, salt);
            yo.setSalt(salt);
            yo.setPasswordHash(hash);
            BitacoraHelper.registrar("PASSWORD", "Usuario", yo.getId(), "Cambio propio");
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public void desactivar(Usuario u) throws NegocioException {
        exigirAdmin();
        if (u.getId() == Sesion.getInstancia().getUsuario().getId()) {
            throw new NegocioException("No puedes desactivar tu propio usuario.");
        }
        try {
            usuarioDAO.desactivar(u.getId());
            BitacoraHelper.registrar("DESACTIVAR", "Usuario", u.getId(), "Usuario " + u.getUsername());
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    private Usuario construir(String username, String password, int idRol) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setSalt(Seguridad.generarSalt());
        u.setPasswordHash(Seguridad.hash(password, u.getSalt()));
        u.setIdRol(idRol);
        return u;
    }

    private void validarUsername(String username) throws NegocioException {
        if (username == null || !username.trim().matches("[A-Za-z0-9_]{3,30}")) {
            throw new NegocioException(
                    "El usuario debe tener entre 3 y 30 caracteres (letras, numeros o guion bajo).");
        }
    }

    private void validarRol(int idRol) throws NegocioException {
        if (idRol <= 0) {
            throw new NegocioException("Selecciona un rol valido.");
        }
        try {
            for (Rol r : rolDAO.listar()) {
                if (r.getId() == idRol) {
                    return;
                }
            }
        } catch (SQLException e) {
            throw errorBD(e);
        }
        throw new NegocioException("El rol seleccionado no existe.");
    }

    private boolean esAdministrador(int idRol) throws NegocioException {
        try {
            for (Rol r : rolDAO.listar()) {
                if (r.getId() == idRol) {
                    return Rol.ADMINISTRADOR.equals(r.getNombre());
                }
            }
        } catch (SQLException e) {
            throw errorBD(e);
        }
        return false;
    }

    private void validarPassword(String password) throws NegocioException {
        if (password == null || password.length() < 8 || password.length() > 64
                || !password.matches(".*[A-Z].*") || !password.matches(".*\\d.*")) {
            throw new NegocioException(
                    "La contrasena debe tener al menos 8 caracteres, una mayuscula y un numero.");
        }
    }

    private void exigirAdmin() throws NegocioException {
        if (!Sesion.getInstancia().esAdmin()) {
            throw new NegocioException("Solo el Administrador puede realizar esta accion.");
        }
    }

    static boolean esDuplicado(SQLException e) {
        return e.getErrorCode() == 2627 || e.getErrorCode() == 2601;
    }

    static NegocioException errorBD(SQLException e) {
        return new NegocioException("Error de base de datos: " + e.getMessage(), e);
    }
}
