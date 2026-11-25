package com.itla.autobuses.service;

import com.itla.autobuses.dao.AsignacionDAO;
import com.itla.autobuses.dao.AutobusDAO;
import com.itla.autobuses.dao.BitacoraDAO;
import com.itla.autobuses.dao.ConductorDAO;
import com.itla.autobuses.dao.RolDAO;
import com.itla.autobuses.dao.RutaDAO;
import com.itla.autobuses.dao.UsuarioDAO;
import com.itla.autobuses.demo.AsignacionDAODemo;
import com.itla.autobuses.demo.AutobusDAODemo;
import com.itla.autobuses.demo.BitacoraDAODemo;
import com.itla.autobuses.demo.ConductorDAODemo;
import com.itla.autobuses.demo.DemoAlmacen;
import com.itla.autobuses.demo.RolDAODemo;
import com.itla.autobuses.demo.RutaDAODemo;
import com.itla.autobuses.demo.UsuarioDAODemo;
import com.itla.autobuses.util.ConexionDB;

import java.util.Arrays;

public final class FabricaServicios {

    private static boolean inicializado;
    private static boolean demo;
    private static String motivo = "";
    private static AuthService auth;
    private static UsuarioService usuarios;
    private static ConductorService conductores;
    private static AutobusService autobuses;
    private static RutaService rutas;
    private static AsignacionService asignaciones;
    private static BitacoraService bitacora;
    private static PanelService panel;

    private FabricaServicios() { }

    public static synchronized boolean inicializar(String[] args) {
        if (inicializado) {
            return demo;
        }
        inicializado = true;
        boolean forzarDemo = args != null && Arrays.asList(args).contains("--demo");
        if (forzarDemo) {
            motivo = "Modo demostración solicitado.";
        } else if (!baseDisponible()) {
            motivo = "No fue posible conectar con SQL Server. Se abre el modo demostración.";
        } else {
            try {
                inicializarBD();
                return false;
            } catch (Exception e) {
                motivo = "No fue posible preparar la base de datos. Se abre el modo demostración. "
                        + e.getMessage();
            }
        }
        inicializarDemo();
        return true;
    }

    public static boolean baseDisponible() {
        try {
            return ConexionDB.getInstancia().probar();
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static boolean esDemo() {
        verificar();
        return demo;
    }

    public static String getMotivoDemo() {
        verificar();
        return motivo;
    }

    public static AuthService auth() {
        verificar();
        return auth;
    }

    public static UsuarioService usuarios() {
        verificar();
        return usuarios;
    }

    public static ConductorService conductores() {
        verificar();
        return conductores;
    }

    public static AutobusService autobuses() {
        verificar();
        return autobuses;
    }

    public static RutaService rutas() {
        verificar();
        return rutas;
    }

    public static AsignacionService asignaciones() {
        verificar();
        return asignaciones;
    }

    public static BitacoraService bitacora() {
        verificar();
        return bitacora;
    }

    public static PanelService panel() {
        verificar();
        return panel;
    }

    private static void verificar() {
        if (!inicializado) {
            throw new IllegalStateException("FabricaServicios no ha sido inicializado.");
        }
    }

    private static void inicializarBD() throws Exception {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        RolDAO rolDAO = new RolDAO();
        ConductorDAO conductorDAO = new ConductorDAO();
        AutobusDAO autobusDAO = new AutobusDAO();
        RutaDAO rutaDAO = new RutaDAO();
        AsignacionDAO asignacionDAO = new AsignacionDAO();
        BitacoraDAO bitacoraDAO = new BitacoraDAO();

        UsuarioService servicioUsuarios = new UsuarioService(usuarioDAO, rolDAO);
        servicioUsuarios.crearAdminInicialSiNoExiste();
        servicioUsuarios.crearUsuarioNormalSiNoExiste();

        BitacoraHelper.usar(bitacoraDAO);
        auth = new AuthService(usuarioDAO);
        usuarios = servicioUsuarios;
        conductores = new ConductorService(conductorDAO);
        autobuses = new AutobusService(autobusDAO);
        rutas = new RutaService(rutaDAO);
        asignaciones = new AsignacionService(
                asignacionDAO, conductorDAO, autobusDAO, rutaDAO);
        bitacora = new BitacoraService(bitacoraDAO);
        panel = new PanelService(conductorDAO, autobusDAO, asignacionDAO);
        demo = false;
    }

    private static void inicializarDemo() {
        DemoAlmacen almacen = new DemoAlmacen();
        UsuarioDAO usuarioDAO = new UsuarioDAODemo(almacen);
        RolDAO rolDAO = new RolDAODemo(almacen);
        ConductorDAO conductorDAO = new ConductorDAODemo(almacen);
        AutobusDAO autobusDAO = new AutobusDAODemo(almacen);
        RutaDAO rutaDAO = new RutaDAODemo(almacen);
        AsignacionDAO asignacionDAO = new AsignacionDAODemo(almacen);
        BitacoraDAO bitacoraDAO = new BitacoraDAODemo(almacen);

        BitacoraHelper.usar(bitacoraDAO);
        auth = new AuthService(usuarioDAO);
        usuarios = new UsuarioService(usuarioDAO, rolDAO);
        conductores = new ConductorService(conductorDAO);
        autobuses = new AutobusService(autobusDAO);
        rutas = new RutaService(rutaDAO);
        asignaciones = new AsignacionService(
                asignacionDAO, conductorDAO, autobusDAO, rutaDAO);
        bitacora = new BitacoraService(bitacoraDAO);
        panel = new PanelService(conductorDAO, autobusDAO, asignacionDAO);
        demo = true;
    }
}
