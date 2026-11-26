package com.itla.autobuses.ui;

import com.itla.autobuses.model.Rol;
import com.itla.autobuses.model.Usuario;
import com.itla.autobuses.service.AuthService;
import com.itla.autobuses.service.FabricaServicios;
import com.itla.autobuses.service.Permisos;
import com.itla.autobuses.service.UsuarioService;
import com.itla.autobuses.util.NegocioException;
import com.itla.autobuses.util.Roles;
import com.itla.autobuses.util.Sesion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UsuarioPanel extends JPanel {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UsuarioService servicio = FabricaServicios.usuarios();
    private final List<Rol> roles = new ArrayList<>();
    private List<Usuario> datos = new ArrayList<>();
    private List<Usuario> visibles = new ArrayList<>();
    private Usuario actual;
    private boolean modoSuper;
    private boolean actualizandoFiltros;

    private final JTextField txtBuscar = Tema.campo("Usuario, rol o estado");
    private final JComboBox<String> cmbEstado = new JComboBox<>(
            new String[]{"Todos", "Activo", "Inactivo", "Bloqueado", "Eliminado"});
    private final JComboBox<String> cmbRolFiltro = new JComboBox<>();
    private final JComboBox<String> cmbOrden = new JComboBox<>(new String[]{
            "Usuario (A-Z)", "Usuario (Z-A)", "Rol", "Estado", "Fecha de creación", "Último acceso"});

    private final JTextField txtUsername = Tema.campo("Nombre de usuario");
    private final JPasswordField txtClave = Tema.clave("Mínimo 8 caracteres");
    private final JComboBox<Rol> cmbRolForm = new JComboBox<>();
    private final JCheckBox chkActivo = new JCheckBox("Activo", true);

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Usuario", "Rol", "Estado", "Fecha creación", "Último acceso", "Acciones"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final JTable tabla = Tema.tabla(modelo);

    private final JPanel tarjetas = new JPanel(new GridLayout(1, 5, 10, 10));
    private JDialog dialogo;
    private JButton btnNuevo;
    private JButton btnNuevoSuper;
    private JButton btnRestablecer;
    private JButton btnDesactivar;
    private JButton btnEliminar;
    private JButton btnReactivar;
    private JButton btnGuardar;

    public UsuarioPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Tema.FONDO);

        tarjetas.setBackground(Tema.FONDO);
        JPanel superior = new JPanel(new BorderLayout(0, 10));
        superior.setBackground(Tema.FONDO);
        superior.add(tarjetas, BorderLayout.NORTH);
        superior.add(Tema.contenidoSuperior("Usuarios",
                "Administra cuentas, roles, estados de acceso y bajas lógicas.", filtros()),
                BorderLayout.SOUTH);
        add(superior, BorderLayout.NORTH);
        add(Tema.scroll(tabla), BorderLayout.CENTER);

        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int fila = tabla.rowAtPoint(e.getPoint());
                    if (fila >= 0) {
                        tabla.setRowSelectionInterval(fila, fila);
                        abrirDialogo(false, false);
                    }
                }
            }
        });
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionar();
            }
        });

        cargarRoles();
        Tema.aplicar(cmbEstado);
        Tema.aplicar(cmbRolFiltro);
        Tema.aplicar(cmbOrden);
        Tema.aplicar(cmbRolForm);

        cmbEstado.addActionListener(e -> aplicarFiltros());
        cmbRolFiltro.addActionListener(e -> aplicarFiltros());
        cmbOrden.addActionListener(e -> aplicarFiltros());
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { aplicarFiltros(); }
            @Override public void removeUpdate(DocumentEvent e) { aplicarFiltros(); }
            @Override public void changedUpdate(DocumentEvent e) { aplicarFiltros(); }
        });
        cargar();
    }

    private JPanel filtros() {
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filtros.setBackground(Tema.FONDO);

        btnNuevo = Tema.boton("Crear usuario", Iconos.de("nuevo", Color.WHITE));
        btnNuevo.addActionListener(e -> abrirDialogo(true, false));
        btnNuevoSuper = Tema.botonSecundario("Crear superadministrador",
                Iconos.de("usuarios", Tema.VERDE));
        btnNuevoSuper.addActionListener(e -> abrirDialogo(true, true));

        JButton btnBuscar = Tema.botonSecundario("Buscar", Iconos.de("buscar", Tema.VERDE));
        btnBuscar.addActionListener(e -> cargar());
        JButton btnLimpiar = Tema.botonPlano("Limpiar");
        btnLimpiar.addActionListener(e -> {
            txtBuscar.setText("");
            cmbEstado.setSelectedIndex(0);
            cmbRolFiltro.setSelectedIndex(0);
            cmbOrden.setSelectedIndex(0);
            aplicarFiltros();
        });

        filtros.add(btnNuevo);
        filtros.add(btnNuevoSuper);
        filtros.add(Tema.separador());
        filtros.add(txtBuscar);
        filtros.add(cmbEstado);
        filtros.add(cmbRolFiltro);
        filtros.add(cmbOrden);
        filtros.add(btnBuscar);
        filtros.add(btnLimpiar);
        return filtros;
    }

    private void cargarRoles() {
        try {
            /* Unifica variantes (Usuario Generico / Usuario Genérico) y prefiere la canónica. */
            Map<String, Rol> unicos = new LinkedHashMap<>();
            for (Rol r : servicio.listarRoles()) {
                String clave = Roles.normalizar(r.getNombre());
                Rol previo = unicos.get(clave);
                if (previo == null || (Rol.USUARIO_GENERICO.equals(r.getNombre())
                        && !Rol.USUARIO_GENERICO.equals(previo.getNombre()))) {
                    unicos.put(clave, r);
                }
            }
            roles.addAll(unicos.values());
            cmbRolFiltro.addItem("Todos los roles");
            for (Rol r : roles) {
                cmbRolFiltro.addItem(r.getNombre());
            }
        } catch (NegocioException ex) {
            aviso(ex.getMessage());
        }
    }

    private void cargar() {
        int nivelActor = Permisos.nivel(Sesion.getInstancia().getUsuario());
        boolean permiteAdministrar = Permisos.tiene(Sesion.getInstancia().getUsuario(),
                Permisos.Permiso.ADMINISTRAR_USUARIOS);
        btnNuevo.setEnabled(permiteAdministrar);
        btnNuevoSuper.setVisible(permiteAdministrar
                && Permisos.tiene(Sesion.getInstancia().getUsuario(),
                        Permisos.Permiso.CONFIGURAR_SEGURIDAD));

        List<Usuario> lista;
        try {
            lista = new ArrayList<>(servicio.listar());
        } catch (NegocioException ex) {
            lista = new ArrayList<>();
            aviso(ex.getMessage());
        }
        datos = lista;
        aplicarFiltros();
    }

    /** Filtra sobre la lista ya cargada; no consulta la BD en cada tecla o cambio de combo. */
    private void aplicarFiltros() {
        if (actualizandoFiltros) {
            return;
        }
        actualizandoFiltros = true;
        try {
            int nivelActor = Permisos.nivel(Sesion.getInstancia().getUsuario());
            String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
            Object valorEstado = cmbEstado.getSelectedItem();
            String estadoFiltro = valorEstado == null ? "Todos" : String.valueOf(valorEstado);
            Object valorRol = cmbRolFiltro.getSelectedItem();
            String rolFiltro = valorRol == null ? "Todos los roles" : String.valueOf(valorRol);

            visibles = new ArrayList<>();
            for (Usuario u : datos) {
                if (!texto.isEmpty() && !coincide(u, texto)) {
                    continue;
                }
                if (!"Todos".equals(estadoFiltro) && !estado(u).equals(estadoFiltro)) {
                    continue;
                }
                if (!"Todos los roles".equals(rolFiltro) && !rolFiltro.equals(u.getRolNombre())) {
                    continue;
                }
                visibles.add(u);
            }
            visibles.sort(comparador());
            modelo.setRowCount(0);
            for (Usuario u : visibles) {
                modelo.addRow(new Object[]{u.getUsername(), u.getRolNombre(), estado(u),
                        fecha(u.getFechaCreacion()), fecha(u.getUltimoAcceso()), acciones(u, nivelActor)});
            }
            actualizarTarjetas(datos);
            if (!visibles.isEmpty()) {
                tabla.setRowSelectionInterval(0, 0);
            } else {
                tabla.clearSelection();
                actual = null;
            }
        } finally {
            actualizandoFiltros = false;
        }
    }

    private boolean coincide(Usuario u, String texto) {
        return u.getUsername().toLowerCase().contains(texto)
                || (u.getRolNombre() != null && u.getRolNombre().toLowerCase().contains(texto))
                || estado(u).toLowerCase().contains(texto);
    }

    private Comparator<Usuario> comparador() {
        String orden = String.valueOf(cmbOrden.getSelectedItem());
        Comparator<Usuario> c;
        if (orden.startsWith("Usuario (Z")) {
            c = Comparator.comparing(Usuario::getUsername, String.CASE_INSENSITIVE_ORDER).reversed();
        } else if ("Rol".equals(orden)) {
            c = Comparator.comparing(Usuario::getRolNombre, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("Estado".equals(orden)) {
            c = Comparator.comparing(this::estado);
        } else if ("Fecha de creación".equals(orden)) {
            c = Comparator.comparing(Usuario::getFechaCreacion,
                    Comparator.nullsLast(Comparator.reverseOrder()));
        } else if ("Último acceso".equals(orden)) {
            c = Comparator.comparing(Usuario::getUltimoAcceso,
                    Comparator.nullsLast(Comparator.reverseOrder()));
        } else {
            c = Comparator.comparing(Usuario::getUsername, String.CASE_INSENSITIVE_ORDER);
        }
        return c.thenComparing(Usuario::getUsername, String.CASE_INSENSITIVE_ORDER);
    }

    private void actualizarTarjetas(List<Usuario> lista) {
        int activos = 0;
        int inactivos = 0;
        int superadministradores = 0;
        int eliminados = 0;
        for (Usuario u : lista) {
            if (u.isEliminado()) {
                eliminados++;
            } else if (u.isActivo()) {
                activos++;
            } else {
                inactivos++;
            }
            if (Roles.esSuperadministrador(u.getRolNombre())) {
                superadministradores++;
            }
        }
        tarjetas.removeAll();
        tarjetas.add(Tema.tarjeta("Usuarios registrados", lista.size(), "Cuentas en el sistema",
                Iconos.de("usuarios", Tema.VERDE)));
        tarjetas.add(Tema.tarjeta("Activos", activos, "Acceso habilitado",
                Iconos.de("check", Tema.VERDE)));
        tarjetas.add(Tema.tarjeta("Inactivos", inactivos, "Acceso suspendido",
                Iconos.de("alerta", Tema.ADVERTENCIA)));
        tarjetas.add(Tema.tarjeta("Superadministradores", superadministradores, "Jerarquía máxima",
                Iconos.de("llave", Tema.VERDE_OSCURO)));
        tarjetas.add(Tema.tarjeta("Bajas lógicas", eliminados, "Historial conservado",
                Iconos.de("alerta", Tema.ERROR)));
        tarjetas.revalidate();
        tarjetas.repaint();
    }

    private String estado(Usuario u) {
        if (u.isEliminado()) {
            return "Eliminado";
        }
        if (!u.isActivo()) {
            return "Inactivo";
        }
        if (AuthService.estaBloqueado(u.getUsername())) {
            return "Bloqueado";
        }
        return u.getEstadoCuenta() == null ? "Activo" : u.getEstadoCuenta();
    }

    private String fecha(LocalDateTime valor) {
        return valor == null ? "—" : valor.format(FECHA);
    }

    private String acciones(Usuario u, int nivelActor) {
        if (!Permisos.puedeGestionar(Sesion.getInstancia().getUsuario(), u)) {
            return "Solo lectura";
        }
        if (u.isEliminado()) {
            return "Reactivar";
        }
        if (!u.isActivo()) {
            return "Editar · Reactivar";
        }
        return "Editar · Clave · Baja";
    }

    private void abrirDialogo(boolean nuevo, boolean superadministrador) {
        if (nuevo) {
            limpiar();
        }
        modoSuper = superadministrador;
        if (dialogo == null) {
            dialogo = crearDialogo();
        }
        actualizarFormulario(nuevo);
        dialogo.setTitle(superadministrador ? "Crear superadministrador"
                : nuevo ? "Crear usuario" : "Editar usuario");
        dialogo.setVisible(true);
    }

    private void actualizarFormulario(boolean nuevo) {
        boolean editable = !nuevo && actual != null;
        Usuario actor = Sesion.getInstancia().getUsuario();
        boolean puedeGestionar = Permisos.tiene(actor, Permisos.Permiso.ADMINISTRAR_USUARIOS)
                && (!editable || Permisos.puedeGestionar(actor, actual));

        txtUsername.setText(editable ? actual.getUsername() : "");
        txtClave.setText("");
        chkActivo.setSelected(!editable || actual.isActivo());
        cargarRolesFormulario(editable);
        txtUsername.setEnabled(!editable && puedeGestionar);
        txtClave.setEnabled(!editable && puedeGestionar);
        chkActivo.setEnabled(editable && puedeGestionar);
        if (btnGuardar != null) {
            btnGuardar.setVisible(puedeGestionar);
        }
        if (btnRestablecer != null) {
            btnRestablecer.setVisible(editable && puedeGestionar && actual.isActivo());
        }
        if (btnDesactivar != null) {
            btnDesactivar.setVisible(editable && puedeGestionar
                    && actual.isActivo() && !actual.isEliminado());
        }
        if (btnEliminar != null) {
            btnEliminar.setVisible(editable && puedeGestionar && !actual.isEliminado());
        }
        if (btnReactivar != null) {
            btnReactivar.setVisible(editable && puedeGestionar
                    && (!actual.isActivo() || actual.isEliminado()));
        }
        dialogo.pack();
    }

    private void cargarRolesFormulario(boolean editable) {
        cmbRolForm.removeAllItems();
        int nivelActor = Permisos.nivel(Sesion.getInstancia().getUsuario());
        if (modoSuper) {
            for (Rol r : roles) {
                if (Roles.esSuperadministrador(r.getNombre())) {
                    cmbRolForm.addItem(r);
                    break;
                }
            }
            cmbRolForm.setEnabled(false);
            return;
        }
        for (Rol r : roles) {
            if (Roles.nivel(r.getNombre()) <= nivelActor) {
                cmbRolForm.addItem(r);
            }
        }
        cmbRolForm.setEnabled(!editable);
        if (editable) {
            for (int i = 0; i < cmbRolForm.getItemCount(); i++) {
                if (cmbRolForm.getItemAt(i).getId() == actual.getIdRol()) {
                    cmbRolForm.setSelectedIndex(i);
                    break;
                }
            }
        } else if (cmbRolForm.getItemCount() > 0) {
            cmbRolForm.setSelectedIndex(0);
        }
    }

    private JDialog crearDialogo() {
        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBackground(Color.WHITE);
        form.add(etiqueta("Usuario"));
        form.add(txtUsername);
        form.add(etiqueta("Contraseña"));
        form.add(txtClave);
        form.add(etiqueta("Rol"));
        form.add(cmbRolForm);
        form.add(etiqueta("Estado"));
        form.add(chkActivo);
        form.setBorder(BorderFactory.createEmptyBorder(4, 4, 10, 4));
        Tema.aplicar(chkActivo);

        btnRestablecer = Tema.botonSecundario("Restablecer clave", Iconos.de("llave", Tema.VERDE));
        btnRestablecer.addActionListener(e -> restablecer());
        btnDesactivar = Tema.botonSecundario("Desactivar", Iconos.de("alerta", Tema.ADVERTENCIA));
        btnDesactivar.addActionListener(e -> desactivar(false));
        btnEliminar = Tema.botonPeligro("Eliminar", Iconos.de("alerta", Tema.ERROR));
        btnEliminar.addActionListener(e -> desactivar(true));
        btnReactivar = Tema.boton("Reactivar", Iconos.de("check", Color.WHITE));
        btnReactivar.addActionListener(e -> reactivar());
        JButton btnCancelar = Tema.botonSecundario("Cancelar");
        btnCancelar.addActionListener(e -> dialogo.dispose());
        btnGuardar = Tema.boton("Guardar", Iconos.de("check", Color.WHITE));
        btnGuardar.addActionListener(e -> guardar());

        Frame padre = (Frame) SwingUtilities.getWindowAncestor(this);
        return Tema.dialogo(padre, "Crear usuario",
                "Define el acceso, el perfil y el estado de la cuenta.", form,
                btnRestablecer, btnDesactivar, btnEliminar, btnReactivar, btnCancelar, btnGuardar);
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.fuente(11, Font.BOLD));
        l.setForeground(Tema.TEXTO);
        return l;
    }

    private void seleccionar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= visibles.size()) {
            return;
        }
        actual = visibles.get(tabla.convertRowIndexToModel(fila));
    }

    private void limpiar() {
        actual = null;
        tabla.clearSelection();
        txtUsername.setText("");
        txtClave.setText("");
        chkActivo.setSelected(true);
    }

    private void guardar() {
        Rol rol = (Rol) cmbRolForm.getSelectedItem();
        if (rol == null) {
            aviso("No hay roles disponibles para tu jerarquía.");
            return;
        }
        try {
            if (actual == null) {
                if (modoSuper) {
                    servicio.crearSuperadministrador(txtUsername.getText(),
                            new String(txtClave.getPassword()));
                } else {
                    servicio.crear(txtUsername.getText(), new String(txtClave.getPassword()), rol.getId());
                }
            } else {
                servicio.actualizar(actual, rol.getId(), chkActivo.isSelected());
            }
            dialogo.dispose();
            cargar();
        } catch (NegocioException ex) {
            aviso(ex.getMessage());
        }
    }

    private void restablecer() {
        if (actual == null) {
            aviso("Selecciona un usuario de la tabla.");
            return;
        }
        JPasswordField campo = Tema.clave("Mínimo 8 caracteres");
        int r = JOptionPane.showConfirmDialog(dialogo, campo,
                "Nueva contraseña para " + actual.getUsername(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            servicio.restablecerPassword(actual, new String(campo.getPassword()));
            JOptionPane.showMessageDialog(dialogo, "Contraseña restablecida.");
            cargar();
        } catch (NegocioException ex) {
            aviso(ex.getMessage());
        }
    }

    private void desactivar(boolean eliminar) {
        if (actual == null) {
            aviso("Selecciona un usuario de la tabla.");
            return;
        }
        String mensaje = eliminar
                ? "¿Eliminar al usuario " + actual.getUsername()
                        + "?\nLa cuenta quedará inactiva y conservará su historial en la bitácora."
                : "¿Desactivar al usuario " + actual.getUsername() + "?";
        int r = JOptionPane.showConfirmDialog(dialogo, mensaje, "Confirmar",
                JOptionPane.YES_NO_OPTION, eliminar
                        ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (eliminar) {
                servicio.eliminar(actual);
            } else {
                servicio.desactivar(actual);
            }
            dialogo.dispose();
            cargar();
        } catch (NegocioException ex) {
            aviso(ex.getMessage());
        }
    }

    private void reactivar() {
        if (actual == null) {
            aviso("Selecciona un usuario de la tabla.");
            return;
        }
        int r = JOptionPane.showConfirmDialog(dialogo,
                "¿Reactivar la cuenta de " + actual.getUsername() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            servicio.reactivar(actual);
            dialogo.dispose();
            cargar();
        } catch (NegocioException ex) {
            aviso(ex.getMessage());
        }
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
}
