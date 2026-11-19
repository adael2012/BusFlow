package com.itla.autobuses.service;

import com.itla.autobuses.dao.UsuarioDAO;
import com.itla.autobuses.model.Usuario;
import com.itla.autobuses.util.NegocioException;
import com.itla.autobuses.util.Seguridad;
import com.itla.autobuses.util.Sesion;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
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

            if (u == null || !Seguridad.verificar(password, u.getSalt(), u.getPasswordHash())) {
                registrarFallo(clave);
                throw new NegocioException("Usuario o contraseña incorrectos.");
            }
            if (!u.isActivo()) {
                registrarFallo(clave);
                throw new NegocioException("Este usuario esta desactivado.");
            }
            INTENTOS.remove(clave);
            Sesion.getInstancia().iniciar(u);
            BitacoraHelper.registrar("LOGIN", "Usuario", u.getId(), "Inicio de sesión");
            return u;
        } catch (SQLException e) {
            throw new NegocioException("Error al acceder a la base de datos: " + e.getMessage(), e);
        }
    }

    static synchronized void reiniciarIntentos() {
        INTENTOS.clear();
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

    private static synchronized void registrarFallo(String clave) throws NegocioException {
        Registro r = INTENTOS.computeIfAbsent(clave, k -> new Registro());
        r.intentos++;
        if (r.intentos >= MAX_INTENTOS) {
            r.bloqueadoHasta = Instant.now().plus(TIEMPO_BLOQUEO);
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
            BitacoraHelper.registrar("LOGOUT", "Usuario", s.getUsuario().getId(), "Cierre de sesión");
        }
        s.cerrar();
    }
}
