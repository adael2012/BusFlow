package com.itla.autobuses.demo;

import com.itla.autobuses.model.Asignacion;
import com.itla.autobuses.model.Autobus;
import com.itla.autobuses.model.Conductor;
import com.itla.autobuses.model.EntradaBitacora;
import com.itla.autobuses.model.Rol;
import com.itla.autobuses.model.Ruta;
import com.itla.autobuses.model.Usuario;
import com.itla.autobuses.service.UsuarioService;
import com.itla.autobuses.util.Seguridad;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class DemoAlmacen {

    final List<Rol> roles = new ArrayList<>();
    final List<Usuario> usuarios = new ArrayList<>();
    final List<Conductor> conductores = new ArrayList<>();
    final List<Autobus> autobuses = new ArrayList<>();
    final List<Ruta> rutas = new ArrayList<>();
    final List<Asignacion> asignaciones = new ArrayList<>();
    final List<EntradaBitacora> bitacora = new ArrayList<>();

    private int secRol;
    private int secUsuario;
    private int secConductor;
    private int secAutobus;
    private int secRuta;
    private int secAsignacion;
    private int secBitacora;

    public DemoAlmacen() {
        sembrar();
    }

    static SQLException duplicado(String mensaje) {
        return new SQLException(mensaje, "23000", 2627);
    }

    int siguienteRol() { return ++secRol; }
    int siguienteUsuario() { return ++secUsuario; }
    int siguienteConductor() { return ++secConductor; }
    int siguienteAutobus() { return ++secAutobus; }
    int siguienteRuta() { return ++secRuta; }
    int siguienteAsignacion() { return ++secAsignacion; }
    int siguienteBitacora() { return ++secBitacora; }

    Rol rolPorId(int id) {
        for (Rol r : roles) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    Usuario usuarioPorId(int id) {
        for (Usuario u : usuarios) {
            if (u.getId() == id) {
                return u;
            }
        }
        return null;
    }

    Usuario usuarioPorUsername(String username) {
        if (username == null) {
            return null;
        }
        for (Usuario u : usuarios) {
            if (username.equalsIgnoreCase(u.getUsername())) {
                return u;
            }
        }
        return null;
    }

    void aplicarRol(Usuario u) {
        Rol r = rolPorId(u.getIdRol());
        u.setRolNombre(r == null ? "" : r.getNombre());
    }

    void enriquecer(Asignacion a) {
        Conductor c = conductorPorId(a.getIdConductor());
        Autobus b = autobusPorId(a.getIdAutobus());
        Ruta r = rutaPorId(a.getIdRuta());
        a.setNombreConductor(c == null ? "" : c.getNombre() + " " + c.getApellido());
        a.setPlacaAutobus(b == null ? "" : b.getPlaca());
        a.setCodigoRuta(r == null ? "" : r.getCodigo());
    }

    private Conductor conductorPorId(int id) {
        for (Conductor c : conductores) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    private Autobus autobusPorId(int id) {
        for (Autobus b : autobuses) {
            if (b.getId() == id) {
                return b;
            }
        }
        return null;
    }

    private Ruta rutaPorId(int id) {
        for (Ruta r : rutas) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    private void sembrar() {
        Rol admin = new Rol(siguienteRol(), Rol.ADMINISTRADOR);
        Rol generico = new Rol(siguienteRol(), Rol.USUARIO_GENERICO);
        Rol superadmin = new Rol(siguienteRol(), Rol.SUPERADMINISTRADOR);
        roles.add(admin);
        roles.add(generico);
        roles.add(superadmin);

        usuarios.add(crearUsuario(UsuarioService.ADMIN_INICIAL,
                UsuarioService.CLAVE_INICIAL, admin.getId()));
        usuarios.add(crearUsuario(UsuarioService.USUARIO_NORMAL_INICIAL,
                UsuarioService.CLAVE_NORMAL_INICIAL, generico.getId()));
        usuarios.add(crearUsuario(UsuarioService.SUPERADMIN_INICIAL_DEMO,
                UsuarioService.CLAVE_SUPERADMIN_DEMO, superadmin.getId()));

        LocalDate hoy = LocalDate.now();
        conductores.add(crearConductor("00112345671", "Juan", "Pérez",
                "8095551001", "LIC-1001", hoy.plusDays(120), true));
        conductores.add(crearConductor("00112345672", "María", "Gómez",
                "8095551002", "LIC-1002", hoy.plusDays(20), true));
        conductores.add(crearConductor("00112345673", "Carlos", "Ruiz",
                "8095551003", "LIC-1003", hoy.plusDays(300), true));
        conductores.add(crearConductor("00112345674", "Ana", "Díaz",
                "8095551004", "LIC-1004", hoy.minusDays(10), false));

        autobuses.add(crearAutobus("AB-1234", "Toyota", "Coaster", 2022, 30, Autobus.OPERATIVO));
        autobuses.add(crearAutobus("CD-5678", "Mercedes", "Sprinter", 2021, 25, Autobus.OPERATIVO));
        autobuses.add(crearAutobus("EF-9012", "Volvo", "9800", 2020, 45, Autobus.MANTENIMIENTO));

        rutas.add(crearRuta("R-01", "Santo Domingo", "Santiago", "170.50", 180));
        rutas.add(crearRuta("R-02", "Santo Domingo", "Punta Cana", "190.00", 150));
        rutas.add(crearRuta("R-03", "Santiago", "Puerto Plata", "75.00", 95));

        LocalDateTime manana = hoy.plusDays(1).atTime(8, 0);
        asignaciones.add(crearAsignacion(1, 1, 1, manana, manana.plusHours(3),
                Asignacion.PROGRAMADA, 1));
        asignaciones.add(crearAsignacion(2, 2, 2, manana.plusHours(5), manana.plusHours(8),
                Asignacion.PROGRAMADA, 1));
        asignaciones.add(crearAsignacion(3, 2, 3, manana.plusHours(9), manana.plusHours(11),
                Asignacion.PROGRAMADA, 1));
        asignaciones.add(crearAsignacion(1, 2, 2, hoy.minusDays(1).atTime(9, 0),
                hoy.minusDays(1).atTime(12, 0), Asignacion.COMPLETADA, 1));

        bitacora.add(crearEntrada(1, "LOGIN", "Usuario", 1, "Inicio de sesión",
                LocalDateTime.now().minusHours(2)));
        bitacora.add(crearEntrada(1, "CREAR", "Asignación", 1, "AB-1234 / R-01",
                LocalDateTime.now().minusHours(1)));
        bitacora.add(crearEntrada(2, "LOGIN", "Usuario", 2, "Inicio de sesión",
                LocalDateTime.now().minusMinutes(30)));
    }

    private Usuario crearUsuario(String username, String password, int idRol) {
        Usuario u = new Usuario();
        u.setId(siguienteUsuario());
        u.setUsername(username);
        u.setSalt(Seguridad.generarSalt());
        u.setPasswordHash(Seguridad.hash(password, u.getSalt()));
        u.setIdRol(idRol);
        u.setActivo(true);
        u.setEstadoCuenta("Activo");
        u.setFechaCreacion(LocalDateTime.now().minusDays(1));
        u.setFechaModificacion(u.getFechaCreacion());
        aplicarRol(u);
        return u;
    }

    private Conductor crearConductor(String cedula, String nombre, String apellido,
                                     String telefono, String licencia,
                                     LocalDate vencimiento, boolean activo) {
        Conductor c = new Conductor();
        c.setId(siguienteConductor());
        c.setCedula(cedula);
        c.setNombre(nombre);
        c.setApellido(apellido);
        c.setTelefono(telefono);
        c.setNumeroLicencia(licencia);
        c.setVencimientoLicencia(vencimiento);
        c.setActivo(activo);
        return c;
    }

    private Autobus crearAutobus(String placa, String marca, String modelo,
                                 int anio, int capacidad, String estado) {
        Autobus b = new Autobus();
        b.setId(siguienteAutobus());
        b.setPlaca(placa);
        b.setMarca(marca);
        b.setModelo(modelo);
        b.setAnio(anio);
        b.setCapacidad(capacidad);
        b.setEstado(estado);
        return b;
    }

    private Ruta crearRuta(String codigo, String origen, String destino,
                           String distancia, int duracion) {
        Ruta r = new Ruta();
        r.setId(siguienteRuta());
        r.setCodigo(codigo);
        r.setOrigen(origen);
        r.setDestino(destino);
        r.setDistanciaKm(new BigDecimal(distancia));
        r.setDuracionMin(duracion);
        r.setActivo(true);
        return r;
    }

    private Asignacion crearAsignacion(int idConductor, int idAutobus, int idRuta,
                                       LocalDateTime inicio, LocalDateTime fin,
                                       String estado, int idUsuario) {
        Asignacion a = new Asignacion();
        a.setId(siguienteAsignacion());
        a.setIdConductor(idConductor);
        a.setIdAutobus(idAutobus);
        a.setIdRuta(idRuta);
        a.setFechaInicio(inicio);
        a.setFechaFin(fin);
        a.setEstado(estado);
        a.setIdUsuarioRegistro(idUsuario);
        enriquecer(a);
        return a;
    }

    private EntradaBitacora crearEntrada(int idUsuario, String accion, String tabla,
                                         int idRegistro, String detalle, LocalDateTime fecha) {
        EntradaBitacora e = new EntradaBitacora();
        e.setId(siguienteBitacora());
        Usuario u = usuarioPorId(idUsuario);
        e.setUsername(u == null ? "" : u.getUsername());
        e.setAccion(accion);
        e.setTabla(tabla);
        e.setIdRegistro(idRegistro);
        e.setDetalle(detalle);
        e.setResultado("ÉXITO");
        e.setFecha(fecha);
        return e;
    }
}
