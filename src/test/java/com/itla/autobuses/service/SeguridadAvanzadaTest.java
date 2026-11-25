package com.itla.autobuses.service;

import com.itla.autobuses.model.EntradaBitacora;
import com.itla.autobuses.model.Rol;
import com.itla.autobuses.model.Usuario;
import com.itla.autobuses.util.NegocioException;
import com.itla.autobuses.util.Roles;
import com.itla.autobuses.util.Sesion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeguridadAvanzadaTest extends PruebaBase {

    private BitacoraService consultaBitacora;

    @BeforeEach
    void sinCredencialesDeEntorno() {
        consultaBitacora = new BitacoraService(bitacora);
        UsuarioService.usarCredencialesEntorno(false);
    }

    @AfterEach
    void restaurarCredencialesDeEntorno() {
        UsuarioService.usarCredencialesEntorno(true);
    }

    private void iniciarComoSuper() throws Exception {
        usuarios.crearAdminInicialSiNoExiste();
        usuarios.crearSuperAdminInicialSiNoExiste();
        auth.login(UsuarioService.ADMIN_INICIAL, UsuarioService.CLAVE_INICIAL);
    }

    @Test
    @DisplayName("SEG-01 La inicialización del superadministrador es repetible y no cambia la contraseña")
    void inicializacionSeguraDelPrimerSuperadministrador() throws Exception {
        usuarios.crearAdminInicialSiNoExiste();
        String hashOriginal = usuarioDAO.obtenerPorUsername("admin").getPasswordHash();

        usuarios.crearSuperAdminInicialSiNoExiste();
        usuarios.crearSuperAdminInicialSiNoExiste();

        assertEquals(1, usuarioDAO.contar());
        Usuario promovido = usuarioDAO.obtenerPorUsername("admin");
        assertTrue(Roles.esSuperadministrador(promovido.getRolNombre()));
        assertEquals(hashOriginal, promovido.getPasswordHash());
        assertEquals("admin", auth.login("admin", UsuarioService.CLAVE_INICIAL).getUsername());
    }

    @Test
    @DisplayName("SEG-02 El superadministrador conserva capacidades administrativas y obtiene las propias")
    void jerarquiaDelSuperadministrador() throws Exception {
        iniciarComoSuper();
        Usuario yo = Sesion.getInstancia().getUsuario();

        assertTrue(yo.esAdmin());
        assertTrue(yo.esSuperadministrador());
        assertTrue(Sesion.getInstancia().esSuperadministrador());
        assertTrue(Permisos.tiene(yo, Permisos.Permiso.CONFIGURAR_SEGURIDAD));
        assertTrue(Permisos.tiene(yo, Permisos.Permiso.ADMINISTRAR_USUARIOS));
    }

    @Test
    @DisplayName("SEG-03 El superadministrador crea administradores y usuarios genéricos")
    void superadministradorCreaOtrosPerfiles() throws Exception {
        iniciarComoSuper();

        assertTrue(usuarios.crear("admin2", "Clave1234", 1) > 0);
        assertTrue(usuarios.crear("operador2", "Clave1234", 2) > 0);

        assertEquals(Rol.ADMINISTRADOR, usuarioDAO.obtenerPorUsername("admin2").getRolNombre());
        assertEquals(Rol.USUARIO_GENERICO, usuarioDAO.obtenerPorUsername("operador2").getRolNombre());
    }

    @Test
    @DisplayName("SEG-04 Un administrador no puede crear superadministradores y la denegación se audita")
    void administradorNoPuedeCrearSuperadministrador() throws Exception {
        iniciarComoAdmin();

        NegocioException ex = assertThrows(NegocioException.class,
                () -> usuarios.crearSuperadministrador("super2", "Clave1234"));

        assertTrue(ex.getMessage().contains("Superadministrador"));
        assertNull(usuarioDAO.obtenerPorUsername("super2"));
        assertTrue(bitacora.acciones.contains("DENEGADO:Seguridad"));
    }

    @Test
    @DisplayName("SEG-05 Un administrador no puede modificar una cuenta de mayor jerarquía")
    void administradorNoPuedeModificarSuperadministrador() throws Exception {
        iniciarComoSuper();
        Usuario superusuario = usuarioDAO.obtenerPorUsername("admin");
        usuarios.crear("admin2", "Clave1234", 1);
        auth.logout();
        auth.login("admin2", "Clave1234");

        NegocioException ex = assertThrows(NegocioException.class,
                () -> usuarios.actualizar(superusuario, 2, true));

        assertTrue(ex.getMessage().contains("mayor jerarquía"));
        assertTrue(Roles.esSuperadministrador(superusuario.getRolNombre()));
    }

    @Test
    @DisplayName("SEG-06 El usuario genérico solo consulta y sus intentos de escritura se registran")
    void genericoSoloConsultaConAuditoria() throws Exception {
        iniciarComoGenerico();

        assertThrows(NegocioException.class, () -> usuarios.listar());
        assertThrows(NegocioException.class, () -> conductores.guardar(conductorValido()));
        assertThrows(NegocioException.class,
                () -> consultaBitacora.listar(null, null, null, null));
        assertDoesNotThrow(() -> conductores.listar());
        assertTrue(bitacora.acciones.contains("DENEGADO:Seguridad"));
        assertTrue(bitacora.entradas.stream().anyMatch(e -> "DENEGADO".equals(e.getResultado())));
    }

    @Test
    @DisplayName("SEG-07 Solo el superadministrador crea superadministradores y se registra el alta")
    void altaDeSuperadministradorAuditada() throws Exception {
        iniciarComoSuper();

        int id = usuarios.crearSuperadministrador("super2", "Clave1234");

        assertTrue(id > 0);
        assertEquals(Rol.SUPERADMINISTRADOR,
                usuarioDAO.obtenerPorUsername("super2").getRolNombre());
        assertTrue(bitacora.acciones.contains("CREAR:Usuario"));
    }

    @Test
    @DisplayName("SEG-08 El cambio de rol se valida y se registra en la bitácora")
    void cambioDeRolAuditado() throws Exception {
        iniciarComoAdmin();
        usuarios.crear("operador3", "Clave1234", 2);
        Usuario objetivo = usuarioDAO.obtenerPorUsername("operador3");

        usuarios.actualizar(objetivo, 1, true);

        assertEquals(Rol.ADMINISTRADOR, objetivo.getRolNombre());
        assertTrue(bitacora.acciones.contains("ACTUALIZAR:Usuario"));
        assertTrue(bitacora.entradas.stream()
                .anyMatch(e -> e.getDetalle() != null && e.getDetalle().contains("Administrador")));
    }

    @Test
    @DisplayName("SEG-09 La baja lógica conserva la cuenta y el historial sin guardar contraseñas")
    void bajaLogicaConservaHistorial() throws Exception {
        iniciarComoAdmin();
        usuarios.crear("operador4", "Clave1234", 2);
        Usuario objetivo = usuarioDAO.obtenerPorUsername("operador4");

        usuarios.eliminar(objetivo);

        assertNotNull(usuarioDAO.obtenerPorUsername("operador4"));
        assertTrue(objetivo.isEliminado());
        assertFalse(objetivo.isActivo());
        assertEquals("Eliminado", objetivo.getEstadoCuenta());
        assertTrue(bitacora.acciones.contains("ELIMINAR:Usuario"));
        assertTrue(bitacora.entradas.stream()
                .noneMatch(e -> e.getDetalle() != null && e.getDetalle().contains("Clave1234")));
    }

    @Test
    @DisplayName("SEG-10 No se puede dejar al último superadministrador activo sin cuenta de respaldo")
    void protegeAlUltimoSuperadministrador() throws Exception {
        iniciarComoSuper();
        usuarios.crearSuperadministrador("super2", "Clave1234");
        Usuario primero = usuarioDAO.obtenerPorUsername("admin");
        Usuario segundo = usuarioDAO.obtenerPorUsername("super2");
        usuarios.eliminar(segundo);

        NegocioException ex = assertThrows(NegocioException.class,
                () -> usuarios.desactivar(segundo));

        assertTrue(ex.getMessage().contains("último superadministrador"));
        assertTrue(primero.isActivo());
    }

    @Test
    @DisplayName("SEG-11 La reactivación restaura el acceso de una cuenta dada de baja")
    void reactivacionRestauraAcceso() throws Exception {
        iniciarComoAdmin();
        usuarios.crear("operador5", "Clave1234", 2);
        Usuario objetivo = usuarioDAO.obtenerPorUsername("operador5");
        usuarios.eliminar(objetivo);

        usuarios.reactivar(objetivo);

        assertTrue(objetivo.isActivo());
        assertFalse(objetivo.isEliminado());
        assertEquals("Activo", objetivo.getEstadoCuenta());
        assertTrue(bitacora.acciones.contains("REACTIVAR:Usuario"));
        auth.logout();
        assertEquals("operador5", auth.login("operador5", "Clave1234").getUsername());
    }

    @Test
    @DisplayName("SEG-12 Una cuenta eliminada no inicia sesión y el intento se audita")
    void loginDeCuentaEliminadaDenegado() throws Exception {
        iniciarComoAdmin();
        usuarios.crear("operador6", "Clave1234", 2);
        usuarios.eliminar(usuarioDAO.obtenerPorUsername("operador6"));
        auth.logout();

        NegocioException ex = assertThrows(NegocioException.class,
                () -> auth.login("operador6", "Clave1234"));

        assertTrue(ex.getMessage().contains("desactivado"));
        assertFalse(Sesion.getInstancia().hayUsuario());
        assertTrue(bitacora.acciones.contains("LOGIN_DENEGADO:Usuario"));
    }

    @Test
    @DisplayName("SEG-13 los fallos de contraseña y el bloqueo quedan registrados")
    void fallosDeLoginAuditados() throws Exception {
        usuarios.crearAdminInicialSiNoExiste();

        for (int i = 0; i < 2; i++) {
            assertThrows(NegocioException.class, () -> auth.login("admin", "Mala1234"));
        }
        NegocioException tercero = assertThrows(NegocioException.class,
                () -> auth.login("admin", "Mala1234"));

        assertTrue(tercero.getMessage().contains("bloqueada"));
        assertTrue(bitacora.acciones.contains("LOGIN_FALLIDO:Usuario"));
        assertTrue(bitacora.acciones.contains("BLOQUEO:Usuario"));
    }

    @Test
    @DisplayName("SEG-14 El acceso correcto actualiza la fecha de último acceso")
    void ultimoAccesoSeRegistra() throws Exception {
        usuarios.crearAdminInicialSiNoExiste();

        Usuario autenticado = auth.login("admin", UsuarioService.CLAVE_INICIAL);

        assertNotNull(autenticado.getUltimoAcceso());
        assertNotNull(usuarioDAO.obtenerPorUsername("admin").getUltimoAcceso());
        assertTrue(bitacora.acciones.contains("LOGIN:Usuario"));
    }

    @Test
    @DisplayName("SEG-15 La bitácoraExpone resultado y detalle sin incluir credenciales")
    void bitacoraConResultadoSinCredenciales() throws Exception {
        iniciarComoGenerico();

        assertThrows(NegocioException.class, () -> usuarios.crear("nuevo", "Clave1234", 2));

        EntradaBitacora denegacion = bitacora.entradas.stream()
                .filter(e -> "DENEGADO".equals(e.getAccion()))
                .findFirst().orElseThrow();
        assertEquals("DENEGADO", denegacion.getResultado());
        assertNotNull(denegacion.getDetalle());
        assertFalse(denegacion.getDetalle().contains("Clave1234"));
    }

    @Test
    @DisplayName("SEG-16 El modo demostración aplica la misma jerarquía que SQL Server")
    void paridadDeJerarquiaEnModoDemostracion() throws Exception {
        assertTrue(FabricaServicios.inicializar(new String[]{"--demo"}));
        Sesion.getInstancia().cerrar();
        AuthService.reiniciarIntentos();

        Usuario superadmin = FabricaServicios.auth().login(
                UsuarioService.SUPERADMIN_INICIAL_DEMO, UsuarioService.CLAVE_SUPERADMIN_DEMO);
        assertTrue(superadmin.esSuperadministrador());
        assertTrue(FabricaServicios.usuarios().crear("adminParidad", "Clave1234", 1) > 0);

        FabricaServicios.auth().logout();
        FabricaServicios.auth().login(UsuarioService.USUARIO_NORMAL_INICIAL,
                UsuarioService.CLAVE_NORMAL_INICIAL);
        assertThrows(NegocioException.class,
                () -> FabricaServicios.usuarios().crear("noPermitido", "Clave1234", 1));
    }
}
