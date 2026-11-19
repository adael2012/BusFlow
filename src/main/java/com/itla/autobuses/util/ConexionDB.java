package com.itla.autobuses.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexionDB {

    private static ConexionDB instancia;

    private final String url;
    private final String usuario;
    private final String clave;

    private ConexionDB() {
        Properties props = new Properties();
        File externo = localizarArchivo();
        try (InputStream in = externo != null && externo.exists()
                ? new FileInputStream(externo)
                : ConexionDB.class.getResourceAsStream("/db.properties")) {
            if (in == null) {
                throw new IllegalStateException("No se encontro db.properties");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer db.properties", e);
        }
        this.url = props.getProperty("db.url");
        this.usuario = props.getProperty("db.user");
        this.clave = props.getProperty("db.password");
        validar();
        DriverManager.setLoginTimeout(10);
    }

    private File localizarArchivo() {
        File actual = new File("db.properties");
        if (actual.exists()) {
            return actual;
        }
        try {
            File origen = Paths.get(ConexionDB.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).toFile();
            File carpeta = origen.isFile() ? origen.getParentFile() : origen;
            File juntoAJar = new File(carpeta, "db.properties");
            return juntoAJar.exists() ? juntoAJar : null;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private void validar() {
        if (vacio(url) || vacio(usuario) || vacio(clave)) {
            throw new IllegalStateException("db.properties incompleto: defina db.url, db.user y db.password");
        }
        if (clave.contains("TuPassword")) {
            throw new IllegalStateException("Defina su contraseña de SQL Server en db.properties");
        }
    }

    private boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }

    public static synchronized ConexionDB getInstancia() {
        if (instancia == null) {
            instancia = new ConexionDB();
        }
        return instancia;
    }

    public Connection getConexion() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    public CallableStatement preparar(Connection conexion, String sql) throws SQLException {
        CallableStatement cs = conexion.prepareCall(sql);
        cs.setQueryTimeout(15);
        return cs;
    }

    public boolean probar() {
        try (Connection c = getConexion()) {
            return c.isValid(5);
        } catch (SQLException | RuntimeException e) {
            return false;
        }
    }
}
