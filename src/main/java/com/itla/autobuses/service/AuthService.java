package com.itla.autobuses.service;

import com.itla.autobuses.dao.UsuarioDAO;
import com.itla.autobuses.model.Usuario;
import com.itla.autobuses.util.NegocioException;
import com.itla.autobuses.util.Seguridad;
import com.itla.autobuses.util.Sesion;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {

    private static final int MAX_INTENTOS = 3;
    private static final Duration TIEMPO_BLOQUEO = Duration.ofMinutes(5);
    private static final Map<String, Registro> INTENTOS = new ConcurrentHashMap<>();

    private final UsuarioDAO usuarioDAO;

    public AuthService() {
        this(new UsuarioDAO());
    }

    public AuthService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Usuario login(String username, String password) throws NegocioException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new NegocioException("Ingresa usuario y contraseña.");
        }
        String clave = username.trim().toLowerCase();
        verificarBloqueo(clave);
        try {
            Usuario u = usuarioDAO.obtenerPorUsername(username.trim());
            if (u == null) {
                registrarFallo(clave, 0);
                throw new NegocioException("Usuario o contraseña incorrectos.");
            }
            if (!u.isActivo() || u.isEliminado() || "Eliminado".equalsIgnoreCase(u.getEstadoCuenta())) {
                BitacoraHelper.registrar(u.getId(), "LOGIN_DENEGADO", "Usuario", u.getId(),
                        "Intento de acceso con cuenta desactivada.", "DENEGADO");
                registrarFallo(clave, u.getId());
                throw new NegocioException("Este usuario está desactivado.");
            }
            if (!Seguridad.verificar(password, u.getSalt(), u.getPasswordHash())) {
                BitacoraHelper.registrar(u.getId(), "LOGIN_FALLIDO", "Usuario", u.getId(),
                        "Contraseña incorrecta.", "FALLIDO");
                registrarFallo(clave, u.getId());
                throw new NegocioException("Usuario o contraseña incorrectos.");
            }
            INTENTOS.remove(clave);
            usuarioDAO.registrarAcceso(u.getId());
            u.setUltimoAcceso(LocalDateTime.now());
            Sesion.getInstancia().iniciar(u);
            BitacoraHelper.registrar("LOGIN", "Usuario", u.getId(), "Inicio de sesión", "ÉXITO");
            return u;
        } catch (SQLException e) {
            throw new NegocioException("Error al acceder a la base de datos: " + e.getMessage(), e);
        }
    }

    static synchronized void reiniciarIntentos() {
        INTENTOS.clear();
    }

    /** Indica si la cuenta está bloqueada actualmente por intentos fallidos. */
    public static synchronized boolean estaBloqueado(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        Registro r = INTENTOS.get(username.trim().toLowerCase());
        return r != null && r.bloqueadoHasta != null
                && Instant.now().isBefore(r.bloqueadoHasta);
    }

    private static synchronized void verificarBloqueo(String clave) throws NegocioException {
        Registro r = INTENTOS.get(clave);
        if (r == null || r.bloqueadoHasta == null) {
            return;
        }
        if (Instant.now().isBefore(r.bloqueadoHasta)) {
            long minutos = Duration.between(Instant.now(), r.bloqueadoHasta).toMinutes() + 1;
            throw new NegocioException("Cuenta bloqueada. Intenta de nuevo en " + minutos + " minutos.");
        }
        INTENTOS.remove(clave);
    }

    private static synchronized void registrarFallo(String clave, int idUsuario) throws NegocioException {
        Registro r = INTENTOS.computeIfAbsent(clave, k -> new Registro());
        r.intentos++;
        if (r.intentos >= MAX_INTENTOS) {
            r.bloqueadoHasta = Instant.now().plus(TIEMPO_BLOQUEO);
            BitacoraHelper.registrar(idUsuario, "BLOQUEO", "Usuario", idUsuario,
                    "Cuenta bloqueada por tres intentos fallidos.", "DENEGADO");
            throw new NegocioException("Cuenta bloqueada por 5 minutos por intentos fallidos.");
        }
    }

    private static class Registro {
        private int intentos;
        private Instant bloqueadoHasta;
    }

    public void logout() {
        Sesion s = Sesion.getInstancia();
        if (s.hayUsuario()) {
            BitacoraHelper.registrar("LOGOUT", "Usuario", s.getUsuario().getId(),
                    "Cierre de sesión", "ÉXITO");
        }
        s.cerrar();
    }
}
