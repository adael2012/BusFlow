package com.itla.autobuses.service;

import com.itla.autobuses.model.Rol;
import com.itla.autobuses.model.Usuario;
import com.itla.autobuses.util.NegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PerfilUsuarioNormalTest extends PruebaBase {

    @Test
    @DisplayName("UT-73 El perfil normal se crea una sola vez y puede iniciar sesión")
    void crearPerfilNormal() throws Exception {
        usuarios.crearAdminInicialSiNoExiste();
        usuarios.crearUsuarioNormalSiNoExiste();
        usuarios.crearUsuarioNormalSiNoExiste();

        assertEquals(2, usuarioDAO.contar());
        Usuario u = auth.login(UsuarioService.USUARIO_NORMAL_INICIAL,
                UsuarioService.CLAVE_NORMAL_INICIAL);
        assertEquals(Rol.USUARIO_GENERICO, u.getRolNombre());
        assertFalse(u.esAdmin());
    }

    @Test
    @DisplayName("UT-74 El perfil normal consulta pero no puede editar")
    void perfilNormalSoloConsulta() throws Exception {
        usuarios.crearAdminInicialSiNoExiste();
        usuarios.crearUsuarioNormalSiNoExiste();
        auth.login(UsuarioService.USUARIO_NORMAL_INICIAL,
                UsuarioService.CLAVE_NORMAL_INICIAL);

        assertTrue(conductores.listar().isEmpty());
        NegocioException ex = assertThrows(NegocioException.class,
                () -> conductores.guardar(conductorValido()));
        assertEquals("Solo el Administrador puede realizar esta acción.", ex.getMessage());
        assertThrows(NegocioException.class, () -> usuarios.listar());
    }

    @Test
    @DisplayName("UT-75 Acepta bases creadas con el rol sin acento")
    void aceptaRolAnterior() throws Exception {
        RolDAOFalso anterior = new RolDAOFalso() {
            @Override
            public List<Rol> listar() {
                return List.of(new Rol(1, "Administrador"), new Rol(2, "Usuario Generico"));
            }
        };
        UsuarioService servicio = new UsuarioService(usuarioDAO, anterior);

        servicio.crearUsuarioNormalSiNoExiste();

        assertNotNull(usuarioDAO.obtenerPorUsername(UsuarioService.USUARIO_NORMAL_INICIAL));
    }
}
