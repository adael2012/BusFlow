package com.itla.autobuses.ui;

import com.itla.autobuses.service.FabricaServicios;

import com.itla.autobuses.model.Rol;
import com.itla.autobuses.model.Usuario;
import com.itla.autobuses.service.UsuarioService;
import com.itla.autobuses.util.NegocioException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class UsuarioPanel extends JPanel {

    private final UsuarioService servicio = FabricaServicios.usuarios();
    private List<Usuario> datos = new ArrayList<>();
    private Usuario actual;

    private final JTextField txtBuscar = Tema.campo("Escribe para buscar");
    private final JTextField txtUsername = Tema.campo("Nombre de usuario");
    private final javax.swing.JPasswordField txtClave = Tema.clave("Mínimo 8 caracteres");
    private final JComboBox<Rol> cmbRol = new JComboBox<>();
    private final JCheckBox chkActivo = new JCheckBox("Activo", true);

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Id", "Usuario", "Rol", "Estado"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final javax.swing.JTable tabla = Tema.tabla(modelo);

    private JDialog dialogo;
    private JButton btnRestablecer;
    private JButton btnDesactivar;

    public UsuarioPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Tema.FONDO);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filtros.setBackground(Tema.FONDO);
        JButton btnNuevo = Tema.boton("Crear usuario", Iconos.de("nuevo", Color.WHITE));
        btnNuevo.addActionListener(e -> abrirDialogo(true));
        filtros.add(btnNuevo);
        filtros.add(Tema.separador());
        filtros.add(txtBuscar);
        JButton btnBuscar = Tema.botonSecundario("Buscar", Iconos.de("buscar", Tema.VERDE));
        filtros.add(btnBuscar);
        add(Tema.contenidoSuperior("Usuarios",
                "Administra cuentas, roles y estados de acceso.", filtros), BorderLayout.NORTH);

        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    abrirDialogo(false);
                }
            }
        });
        add(Tema.scroll(tabla), BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> cargar(txtBuscar.getText()));
        txtBuscar.addActionListener(e -> cargar(txtBuscar.getText()));
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionar();
            }
        });

        try {
            for (Rol r : servicio.listarRoles()) {
                cmbRol.addItem(r);
            }
        } catch (NegocioException ex) {
            aviso(ex.getMessage());
        }
        Tema.aplicar(cmbRol);
        cargar("");
    }

    private void abrirDialogo(boolean nuevo) {
        if (nuevo) {
            limpiar();
        }
        if (dialogo == null) {
            dialogo = crearDialogo();
        }
        dialogo.setTitle(nuevo ? "Crear usuario" : "Editar usuario");
        boolean editable = !nuevo && actual != null;
        txtUsername.setEnabled(!editable);
        txtClave.setEnabled(!editable);
        btnRestablecer.setVisible(editable);
        btnDesactivar.setVisible(editable);
        dialogo.setVisible(true);
    }

    private JDialog crearDialogo() {
        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setBackground(Color.WHITE);
        form.add(etiqueta("Usuario"));
        form.add(txtUsername);
        form.add(etiqueta("Contraseña"));
        form.add(txtClave);
        form.add(etiqueta("Rol"));
        form.add(cmbRol);
        form.setBorder(BorderFactory.createEmptyBorder(4, 4, 10, 4));

        btnRestablecer = Tema.botonSecundario("Restablecer contraseña",
                Iconos.de("llave", Tema.VERDE));
        btnRestablecer.addActionListener(e -> restablecer());
        btnDesactivar = Tema.botonPeligro("Desactivar", Iconos.de("alerta", Tema.ERROR));
        btnDesactivar.addActionListener(e -> desactivar());
        JButton btnCancelar = Tema.botonSecundario("Cancelar");
        btnCancelar.addActionListener(e -> dialogo.dispose());
        JButton btnGuardar = Tema.boton("Guardar", Iconos.de("check", Color.WHITE));
        btnGuardar.addActionListener(e -> guardar());

        Frame padre = (Frame) SwingUtilities.getWindowAncestor(this);
        return Tema.dialogo(padre, "Crear usuario",
                "Define el acceso y el perfil del usuario.", form,
                btnRestablecer, btnDesactivar, btnCancelar, btnGuardar);
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.fuente(11, Font.BOLD));
        l.setForeground(Tema.TEXTO);
        return l;
    }

    private void cargar(String texto) {
        try {
            datos = servicio.listar();
            modelo.setRowCount(0);
            String filtro = texto == null ? "" : texto.trim().toLowerCase();
            for (Usuario u : datos) {
                if (!filtro.isEmpty() && !u.getUsername().toLowerCase().contains(filtro)) {
                    continue;
                }
                modelo.addRow(new Object[]{u.getId(), u.getUsername(), u.getRolNombre(),
                        u.isActivo() ? "Activo" : "Inactivo"});
            }
        } catch (NegocioException ex) {
            aviso(ex.getMessage());
        }
    }

    private void seleccionar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        int id = (int) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        for (Usuario u : datos) {
            if (u.getId() == id) {
                actual = u;
                txtUsername.setText(u.getUsername());
                chkActivo.setSelected(u.isActivo());
                for (int i = 0; i < cmbRol.getItemCount(); i++) {
                    if (cmbRol.getItemAt(i).getId() == u.getIdRol()) {
                        cmbRol.setSelectedIndex(i);
                        break;
                    }
                }
                return;
            }
        }
    }

    private void limpiar() {
        actual = null;
        tabla.clearSelection();
        txtUsername.setText("");
        txtClave.setText("");
        chkActivo.setSelected(true);
        if (cmbRol.getItemCount() > 0) {
            cmbRol.setSelectedIndex(0);
        }
    }

    private void guardar() {
        Rol rol = (Rol) cmbRol.getSelectedItem();
        if (rol == null) {
            aviso("No hay roles disponibles.");
            return;
        }
        try {
            if (actual == null) {
                servicio.crear(txtUsername.getText(), new String(txtClave.getPassword()), rol.getId());
            } else {
                servicio.actualizar(actual, rol.getId(), chkActivo.isSelected());
            }
            dialogo.dispose();
            cargar(txtBuscar.getText());
        } catch (NegocioException ex) {
            aviso(ex.getMessage());
        }
    }

    private void restablecer() {
        if (actual == null) {
            aviso("Selecciona un usuario de la tabla.");
            return;
        }
        javax.swing.JPasswordField campo = Tema.clave("Mínimo 8 caracteres");
        int r = JOptionPane.showConfirmDialog(this, campo,
                "Nueva contraseña para " + actual.getUsername(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            servicio.restablecerPassword(actual, new String(campo.getPassword()));
            JOptionPane.showMessageDialog(this, "Contraseña restablecida.");
        } catch (NegocioException ex) {
            aviso(ex.getMessage());
        }
    }

    private void desactivar() {
        if (actual == null) {
            aviso("Selecciona un usuario de la tabla.");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Desactivar al usuario " + actual.getUsername() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            servicio.desactivar(actual);
            dialogo.dispose();
            cargar(txtBuscar.getText());
        } catch (NegocioException ex) {
            aviso(ex.getMessage());
        }
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
}
