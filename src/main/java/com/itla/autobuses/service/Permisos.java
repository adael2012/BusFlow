package com.itla.autobuses.service;

import com.itla.autobuses.model.Usuario;
import com.itla.autobuses.util.NegocioException;
import com.itla.autobuses.util.Roles;
import com.itla.autobuses.util.Sesion;

public final class Permisos {

    public enum Permiso {
        CONSULTAR,
        GESTIONAR_OPERACION,
        ADMINISTRAR_USUARIOS,
        CONSULTAR_BITACORA,
        CONFIGURAR_SEGURIDAD,
        EXPORTAR
    }

    private Permisos() { }

    public static int nivel(Usuario usuario) {
        return usuario == null ? 0 : Roles.nivel(usuario.getRolNombre());
    }

    public static boolean esAdministrador(Usuario usuario) {
        return Roles.esAdministrador(usuario == null ? null : usuario.getRolNombre());
    }

    public static boolean esSuperadministrador(Usuario usuario) {
        return Roles.esSuperadministrador(usuario == null ? null : usuario.getRolNombre());
    }

    public static boolean tiene(Usuario usuario, Permiso permiso) {
        if (usuario == null || !usuario.isActivo() || permiso == null) {
            return false;
        }
        int nivel = nivel(usuario);
        switch (permiso) {
            case CONSULTAR:
                return nivel >= 1;
            case GESTIONAR_OPERACION:
            case ADMINISTRAR_USUARIOS:
            case CONSULTAR_BITACORA:
            case EXPORTAR:
                return nivel >= 2;
            case CONFIGURAR_SEGURIDAD:
                return nivel >= 3;
            default:
                return false;
        }
    }

    public static void exigir(Permiso permiso) throws NegocioException {
        Usuario actor = Sesion.getInstancia().getUsuario();
        if (tiene(actor, permiso)) {
            return;
        }
        String detalle = "Permiso solicitado: " + permiso.name();
        BitacoraHelper.registrar("DENEGADO", "Seguridad",
                actor == null ? 0 : actor.getId(), detalle, "DENEGADO");
        throw new NegocioException(mensaje(permiso));
    }

    public static boolean puedeGestionar(Usuario actor, Usuario objetivo) {
        if (!esAdministrador(actor) || objetivo == null) {
            return false;
        }
        int nivelActor = nivel(actor);
        int nivelObjetivo = nivel(objetivo);
        if (nivelObjetivo <= 0) {
            return false;
        }
        return nivelObjetivo <= nivelActor;
    }

    public static void exigirGestion(Usuario objetivo) throws NegocioException {
        Usuario actor = Sesion.getInstancia().getUsuario();
        if (puedeGestionar(actor, objetivo)) {
            return;
        }
        BitacoraHelper.registrar("DENEGADO", "Seguridad",
                actor == null ? 0 : actor.getId(),
                "Cuenta objetivo: " + (objetivo == null ? "desconocida" : objetivo.getUsername()),
                "DENEGADO");
        throw new NegocioException("No tienes permiso para modificar una cuenta de mayor jerarquía.");
    }

    private static String mensaje(Permiso permiso) {
        if (permiso == Permiso.CONFIGURAR_SEGURIDAD) {
            return "Solo el Superadministrador puede realizar esta acción.";
        }
        if (permiso == Permiso.CONSULTAR_BITACORA) {
            return "Solo el Administrador puede consultar la bitácora.";
        }
        return "Solo el Administrador puede realizar esta acción.";
    }
}
