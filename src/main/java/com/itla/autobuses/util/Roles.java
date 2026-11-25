package com.itla.autobuses.util;

import com.itla.autobuses.model.Rol;

import java.text.Normalizer;
import java.util.Locale;

public final class Roles {

    private Roles() { }

    public static String normalizar(String nombre) {
        if (nombre == null) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(nombre.trim(), Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "");
        return sinAcentos.toUpperCase(Locale.ROOT);
    }

    public static boolean esAdministrador(String nombre) {
        String rol = normalizar(nombre);
        return rol.equals(normalizar(Rol.ADMINISTRADOR))
                || rol.equals(normalizar(Rol.SUPERADMINISTRADOR));
    }

    public static boolean esSuperadministrador(String nombre) {
        return normalizar(nombre).equals(normalizar(Rol.SUPERADMINISTRADOR));
    }

    public static boolean esGenerico(String nombre) {
        String rol = normalizar(nombre);
        return rol.equals(normalizar(Rol.USUARIO_GENERICO))
                || rol.equals("USUARIO GENERICO");
    }

    public static int nivel(String nombre) {
        if (esSuperadministrador(nombre)) {
            return 3;
        }
        if (esAdministrador(nombre)) {
            return 2;
        }
        if (esGenerico(nombre)) {
            return 1;
        }
        return 0;
    }
}
