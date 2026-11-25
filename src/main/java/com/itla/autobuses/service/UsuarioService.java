package com.itla.autobuses.service;

import com.itla.autobuses.dao.RolDAO;
import com.itla.autobuses.dao.UsuarioDAO;
import com.itla.autobuses.model.Rol;
import com.itla.autobuses.model.Usuario;
import com.itla.autobuses.util.NegocioException;
import com.itla.autobuses.util.Roles;
import com.itla.autobuses.util.Seguridad;
import com.itla.autobuses.util.Sesion;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class UsuarioService {

    public static final String ADMIN_INICIAL = "admin";
    public static final String CLAVE_INICIAL = "Admin123!";
    public static final String USUARIO_NORMAL_INICIAL = "usuario";
    public static final String CLAVE_NORMAL_INICIAL = "Usuario123!";
    public static final String SUPERADMIN_INICIAL_DEMO = "superadmin";
    public static final String CLAVE_SUPERADMIN_DEMO = "Super123!";

    private static boolean usarCredencialesEntorno = true;

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

    public void crearUsuarioNormalSiNoExiste() throws SQLException {
        if (usuarioDAO.obtenerPorUsername(USUARIO_NORMAL_INICIAL) != null) {
            return;
        }
        for (Rol r : rolDAO.listar()) {
            if (Rol.USUARIO_GENERICO.equals(r.getNombre())
                    || "Usuario Generico".equals(r.getNombre())) {
                usuarioDAO.insertar(construir(
                        USUARIO_NORMAL_INICIAL, CLAVE_NORMAL_INICIAL, r.getId()));
                return;
            }
        }
        throw new SQLException("No existe el rol Usuario Genérico. Ejecuta database/script.sql.");
    }

    public void crearSuperAdminInicialSiNoExiste() throws SQLException {
        int idSuper = buscarIdRol(Rol.SUPERADMINISTRADOR);
        if (idSuper == 0) {
            return;
        }
        List<Usuario> existentes = usuarioDAO.listar();
        for (Usuario u : existentes) {
            if (u.getIdRol() == idSuper) {
                return;
            }
        }
        String nombre = usarCredencialesEntorno ? System.getenv("BUSFLOW_SUPERADMIN_USER") : null;
        String clave = usarCredencialesEntorno ? System.getenv("BUSFLOW_SUPERADMIN_PASSWORD") : null;
        if (nombre != null && !nombre.isBlank() && clave != null && !clave.isBlank()) {
            try {
                validarUsername(nombre);
                validarPassword(clave);
            } catch (NegocioException e) {
                throw new SQLException(e.getMessage());
            }
            Usuario repetido = usuarioDAO.obtenerPorUsername(nombre.trim());
            if (repetido != null) {
                if (!Roles.esSuperadministrador(repetido.getRolNombre())) {
                    usuarioDAO.actualizar(repetido.getId(), idSuper, repetido.isActivo());
                }
                return;
            }
            usuarioDAO.insertar(construir(nombre.trim(), clave, idSuper));
            return;
        }
        Usuario candidato = null;
        for (Usuario u : existentes) {
            if (u.isActivo() && ADMIN_INICIAL.equals(u.getUsername())
                    && Roles.esAdministrador(u.getRolNombre())
                    && !Roles.esSuperadministrador(u.getRolNombre())) {
                candidato = u;
                break;
            }
        }
        if (candidato == null) {
            for (Usuario u : existentes) {
                if (u.isActivo() && Roles.esAdministrador(u.getRolNombre())
                        && !Roles.esSuperadministrador(u.getRolNombre())) {
                    candidato = u;
                    break;
                }
            }
        }
        if (candidato != null) {
            usuarioDAO.actualizar(candidato.getId(), idSuper, candidato.isActivo());
        }
    }

    public List<Rol> listarRoles() throws NegocioException {
        try {
            return rolDAO.listar();
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public List<Usuario> listar() throws NegocioException {
        Permisos.exigir(Permisos.Permiso.ADMINISTRAR_USUARIOS);
        try {
            return usuarioDAO.listar();
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public int crear(String username, String password, int idRol) throws NegocioException {
        Permisos.exigir(Permisos.Permiso.ADMINISTRAR_USUARIOS);
        validarUsername(username);
        validarPassword(password);
        validarRol(idRol);
        validarJerarquiaParaRol(idRol);
        try {
            int id = usuarioDAO.insertar(construir(username.trim(), password, idRol));
            BitacoraHelper.registrar("CREAR", "Usuario", id,
                    "Usuario " + username.trim() + "; Rol " + nombreRol(idRol), "ÉXITO");
            return id;
        } catch (SQLException e) {
            if (esDuplicado(e)) {
                BitacoraHelper.registrar("CREAR", "Usuario", 0,
                        "Usuario duplicado: " + username.trim(), "DENEGADO");
                throw new NegocioException("Ya existe un usuario con ese nombre.");
            }
            throw errorBD(e);
        }
    }

    public int crearSuperadministrador(String username, String password) throws NegocioException {
        Permisos.exigir(Permisos.Permiso.CONFIGURAR_SEGURIDAD);
        int idSuper = buscarIdRol(Rol.SUPERADMINISTRADOR);
        if (idSuper == 0) {
            throw new NegocioException("No existe el rol Superadministrador. Ejecuta la migración de la base.");
        }
        return crear(username, password, idSuper);
    }

    public void actualizar(Usuario u, int idRol, boolean activo) throws NegocioException {
        Permisos.exigir(Permisos.Permiso.ADMINISTRAR_USUARIOS);
        if (u == null) {
            throw new NegocioException("Selecciona un usuario de la tabla.");
        }
        Permisos.exigirGestion(u);
        validarRol(idRol);
        validarJerarquiaParaRol(idRol);
        Usuario yo = Sesion.getInstancia().getUsuario();
        if (u.getId() == yo.getId()) {
            if (!activo) {
                throw new NegocioException("No puedes desactivar tu propio usuario.");
            }
            if (u.getIdRol() != idRol) {
                throw new NegocioException("No puedes quitarte el rol de administrador a ti mismo.");
            }
        }
        if (esUltimoSuperadministrador(u, activo)) {
            throw new NegocioException("No se puede desactivar al último superadministrador.");
        }
        try {
            usuarioDAO.actualizar(u.getId(), idRol, activo);
            u.setIdRol(idRol);
            u.setActivo(activo);
            u.setEliminado(false);
            u.setEstadoCuenta(activo ? "Activo" : "Inactivo");
            u.setFechaModificacion(LocalDateTime.now());
            u.setRolNombre(nombreRol(idRol));
            BitacoraHelper.registrar("ACTUALIZAR", "Usuario", u.getId(),
                    "Usuario " + u.getUsername() + "; Rol " + u.getRolNombre()
                            + "; Estado " + u.getEstadoCuenta(), "ÉXITO");
            Sesion.getInstancia().invalidarSi(u);
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public void restablecerPassword(Usuario u, String nueva) throws NegocioException {
        Permisos.exigir(Permisos.Permiso.ADMINISTRAR_USUARIOS);
        if (u == null) {
            throw new NegocioException("Selecciona un usuario de la tabla.");
        }
        Permisos.exigirGestion(u);
        validarPassword(nueva);
        String salt = Seguridad.generarSalt();
        try {
            usuarioDAO.cambiarPassword(u.getId(), Seguridad.hash(nueva, salt), salt);
            u.setSalt(salt);
            u.setPasswordHash(Seguridad.hash(nueva, salt));
            u.setFechaModificacion(LocalDateTime.now());
            BitacoraHelper.registrar("PASSWORD", "Usuario", u.getId(),
                    "Restablecida: " + u.getUsername(), "ÉXITO");
            Sesion.getInstancia().invalidarSi(u);
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public void cambiarPasswordPropio(String actual, String nueva) throws NegocioException {
        Usuario yo = Sesion.getInstancia().getUsuario();
        if (yo == null) {
            throw new NegocioException("No hay una sesión activa.");
        }
        if (actual == null || actual.isBlank()
                || !Seguridad.verificar(actual, yo.getSalt(), yo.getPasswordHash())) {
            BitacoraHelper.registrar("PASSWORD", "Usuario", yo.getId(),
                    "Contraseña actual incorrecta", "DENEGADO");
            throw new NegocioException("La contraseña actual es incorrecta.");
        }
        validarPassword(nueva);
        if (Seguridad.verificar(nueva, yo.getSalt(), yo.getPasswordHash())) {
            throw new NegocioException("La nueva contraseña debe ser diferente a la actual.");
        }
        String salt = Seguridad.generarSalt();
        String hash = Seguridad.hash(nueva, salt);
        try {
            usuarioDAO.cambiarPassword(yo.getId(), hash, salt);
            yo.setSalt(salt);
            yo.setPasswordHash(hash);
            yo.setFechaModificacion(LocalDateTime.now());
            BitacoraHelper.registrar("PASSWORD", "Usuario", yo.getId(),
                    "Cambio propio", "ÉXITO");
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public void desactivar(Usuario u) throws NegocioException {
        Permisos.exigir(Permisos.Permiso.ADMINISTRAR_USUARIOS);
        if (u == null) {
            throw new NegocioException("Selecciona un usuario de la tabla.");
        }
        if (u.getId() == Sesion.getInstancia().getUsuario().getId()) {
            throw new NegocioException("No puedes desactivar tu propio usuario.");
        }
        Permisos.exigirGestion(u);
        if (esUltimoSuperadministrador(u, false)) {
            throw new NegocioException("No se puede desactivar al último superadministrador.");
        }
        try {
            usuarioDAO.desactivar(u.getId());
            u.setActivo(false);
            u.setEstadoCuenta("Inactivo");
            u.setFechaModificacion(LocalDateTime.now());
            BitacoraHelper.registrar("DESACTIVAR", "Usuario", u.getId(),
                    "Usuario " + u.getUsername(), "ÉXITO");
            Sesion.getInstancia().invalidarSi(u);
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public void eliminar(Usuario u) throws NegocioException {
        Permisos.exigir(Permisos.Permiso.ADMINISTRAR_USUARIOS);
        if (u == null) {
            throw new NegocioException("Selecciona un usuario de la tabla.");
        }
        if (u.getId() == Sesion.getInstancia().getUsuario().getId()) {
            throw new NegocioException("No puedes eliminar tu propio usuario.");
        }
        Permisos.exigirGestion(u);
        if (esUltimoSuperadministrador(u, false)) {
            throw new NegocioException("No se puede eliminar al último superadministrador.");
        }
        try {
            usuarioDAO.eliminar(u.getId());
            u.setActivo(false);
            u.setEliminado(true);
            u.setEstadoCuenta("Eliminado");
            u.setFechaEliminacion(LocalDateTime.now());
            u.setFechaModificacion(LocalDateTime.now());
            BitacoraHelper.registrar("ELIMINAR", "Usuario", u.getId(),
                    "Baja lógica de " + u.getUsername() + "; historial conservado", "ÉXITO");
            Sesion.getInstancia().invalidarSi(u);
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public void reactivar(Usuario u) throws NegocioException {
        Permisos.exigir(Permisos.Permiso.ADMINISTRAR_USUARIOS);
        if (u == null) {
            throw new NegocioException("Selecciona un usuario de la tabla.");
        }
        Permisos.exigirGestion(u);
        try {
            usuarioDAO.reactivar(u.getId());
            u.setActivo(true);
            u.setEliminado(false);
            u.setEstadoCuenta("Activo");
            u.setFechaEliminacion(null);
            u.setFechaModificacion(LocalDateTime.now());
            BitacoraHelper.registrar("REACTIVAR", "Usuario", u.getId(),
                    "Cuenta reactivada: " + u.getUsername(), "ÉXITO");
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
        u.setFechaCreacion(LocalDateTime.now());
        u.setFechaModificacion(LocalDateTime.now());
        u.setEstadoCuenta("Activo");
        return u;
    }

    private void validarUsername(String username) throws NegocioException {
        if (username == null || !username.trim().matches("[A-Za-z0-9_]{3,30}")) {
            throw new NegocioException(
                    "El usuario debe tener entre 3 y 30 caracteres (letras, números o guion bajo).");
        }
    }

    private void validarRol(int idRol) throws NegocioException {
        if (idRol <= 0) {
            throw new NegocioException("Selecciona un rol válido.");
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

    private void validarJerarquiaParaRol(int idRol) throws NegocioException {
        int nivelSolicitado = Roles.nivel(nombreRol(idRol));
        if (nivelSolicitado == 0) {
            throw new NegocioException("El rol seleccionado no existe.");
        }
        if (nivelSolicitado > Permisos.nivel(Sesion.getInstancia().getUsuario())) {
            BitacoraHelper.registrar("DENEGADO", "Seguridad", 0,
                    "Intento de conceder el rol " + nombreRol(idRol), "DENEGADO");
            throw new NegocioException("No puedes conceder un rol de mayor jerarquía.");
        }
    }

    private int buscarIdRol(String nombre) {
        try {
            for (Rol r : rolDAO.listar()) {
                if (Roles.normalizar(r.getNombre()).equals(Roles.normalizar(nombre))) {
                    return r.getId();
                }
            }
        } catch (SQLException e) {
            return 0;
        }
        return 0;
    }

    private String nombreRol(int idRol) {
        try {
            for (Rol r : rolDAO.listar()) {
                if (r.getId() == idRol) {
                    return r.getNombre();
                }
            }
        } catch (SQLException e) {
            return "";
        }
        return "";
    }

    private boolean esUltimoSuperadministrador(Usuario u, boolean activoDestino) {
        if (activoDestino || !Roles.esSuperadministrador(u.getRolNombre())) {
            return false;
        }
        int total = 0;
        try {
            for (Usuario actual : usuarioDAO.listar()) {
                if (actual.isActivo() && !actual.isEliminado()
                        && Roles.esSuperadministrador(actual.getRolNombre())) {
                    total++;
                }
            }
        } catch (SQLException e) {
            return false;
        }
        return total <= 1;
    }

    private void validarPassword(String password) throws NegocioException {
        if (password == null || password.length() < 8 || password.length() > 64
                || !password.matches(".*[A-Z].*") || !password.matches(".*\\d.*")) {
            throw new NegocioException(
                    "La contraseña debe tener al menos 8 caracteres, una mayúscula y un número.");
        }
    }

    static void usarCredencialesEntorno(boolean valor) {
        usarCredencialesEntorno = valor;
    }

    static boolean esDuplicado(SQLException e) {
        return e.getErrorCode() == 2627 || e.getErrorCode() == 2601;
    }

    static NegocioException errorBD(SQLException e) {
        return new NegocioException("Error de base de datos: " + e.getMessage(), e);
    }
}
