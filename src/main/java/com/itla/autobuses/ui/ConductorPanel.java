package com.itla.autobuses.ui;

import com.itla.autobuses.model.Conductor;
import com.itla.autobuses.service.ConductorService;
import com.itla.autobuses.util.NegocioException;
import com.itla.autobuses.util.Sesion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ConductorPanel extends JPanel {

    private final ConductorService servicio = new ConductorService();
    private List<Conductor> datos = new ArrayList<>();
    private Conductor actual = new Conductor();

    private final JTextField txtBuscar = Tema.campo("Escribe para buscar");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Id", "Cedula", "Nombre", "Apellido", "Telefono", "Licencia", "Vence", "Estado"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final javax.swing.JTable tabla = Tema.tabla(modelo);

    private final JTextField txtCedula = Tema.campo("000-0000000");
    private final JTextField txtNombre = Tema.campo("Nombre");
    private final JTextField txtApellido = Tema.campo("Apellido");
    private final JTextField txtTelefono = Tema.campo("809-000-0000");
    private final JTextField txtLicencia = Tema.campo("Numero de licencia");
    private final CampoFechaHora txtVence = new CampoFechaHora(false);
    private final JCheckBox chkActivo = new JCheckBox("Activo", true);

    private JDialog dialogo;
    private JButton btnDesactivar;

    public ConductorPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Tema.FONDO);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filtros.setBackground(Tema.FONDO);
        if (Sesion.getInstancia().esAdmin()) {
            JButton btnNuevo = Tema.boton("Nuevo", Iconos.de("nuevo", java.awt.Color.WHITE));
            btnNuevo.addActionListener(e -> abrirDialogo(true));
            filtros.add(btnNuevo);
            filtros.add(Tema.separador());
        }
        filtros.add(txtBuscar);
        JButton btnBuscar = Tema.botonSecundario("Buscar", Iconos.de("buscar", Tema.VERDE));
        filtros.add(btnBuscar);
        add(Tema.contenidoSuperior("Conductores",
                "Administra y consulta los registros del sistema.", filtros), BorderLayout.NORTH);

        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && Sesion.getInstancia().esAdmin()) {
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

        cargar("");
    }

    private void abrirDialogo(boolean nuevo) {
        if (nuevo) {
            limpiar();
        }
        if (dialogo == null) {
            dialogo = crearDialogo();
        }
        dialogo.setTitle(nuevo ? "Nuevo registro" : "Editar registro");
        btnDesactivar.setVisible(!nuevo && actual.getId() > 0);
        dialogo.setVisible(true);
    }

    private JDialog crearDialogo() {
        JPanel form = new JPanel(new GridLayout(7, 2, 10, 10));
        form.setBackground(java.awt.Color.WHITE);
        form.add(etiqueta("Cedula")); form.add(txtCedula);
        form.add(etiqueta("Nombre")); form.add(txtNombre);
        form.add(etiqueta("Apellido")); form.add(txtApellido);
        form.add(etiqueta("Telefono")); form.add(txtTelefono);
        form.add(etiqueta("Licencia")); form.add(txtLicencia);
        form.add(etiqueta("Vencimiento")); form.add(txtVence);
        form.add(etiqueta("Estado")); form.add(chkActivo);
        form.setBorder(BorderFactory.createEmptyBorder(4, 4, 10, 4));

        btnDesactivar = Tema.botonPeligro("Desactivar", Iconos.de("alerta", Tema.ERROR));
        btnDesactivar.addActionListener(e -> desactivar());
        JButton btnCancelar = Tema.botonSecundario("Cancelar");
        btnCancelar.addActionListener(e -> dialogo.dispose());
        JButton btnGuardar = Tema.boton("Guardar", Iconos.de("check", java.awt.Color.WHITE));
        btnGuardar.addActionListener(e -> guardar());

        Frame padre = (Frame) SwingUtilities.getWindowAncestor(this);
        return Tema.dialogo(padre, "Nuevo registro",
                "Complete los datos del conductor.", form,
                btnDesactivar, btnCancelar, btnGuardar);
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.fuente(11, Font.BOLD));
        l.setForeground(Tema.TEXTO);
        return l;
    }

    private void cargar(String texto) {
        try {
            datos = servicio.buscar(texto);
            modelo.setRowCount(0);
            for (Conductor k : datos) {
                modelo.addRow(new Object[]{k.getId(), k.getCedula(), k.getNombre(), k.getApellido(),
                        k.getTelefono(), k.getNumeroLicencia(), k.getVencimientoLicencia(),
                        k.isActivo() ? "Activo" : "Inactivo"});
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
        for (Conductor k : datos) {
            if (k.getId() == id) {
                actual = k;
                txtCedula.setText(k.getCedula());
                txtNombre.setText(k.getNombre());
                txtApellido.setText(k.getApellido());
                txtTelefono.setText(k.getTelefono());
                txtLicencia.setText(k.getNumeroLicencia());
                txtVence.setText(k.getVencimientoLicencia().toString());
                chkActivo.setSelected(k.isActivo());
                return;
            }
        }
    }

    private void limpiar() {
        actual = new Conductor();
        tabla.clearSelection();
        for (JTextField t : new JTextField[]{txtCedula, txtNombre, txtApellido, txtTelefono,
                txtLicencia, txtVence}) {
            t.setText("");
        }
        chkActivo.setSelected(true);
        txtCedula.requestFocus();
    }

    private void guardar() {
        try {
            actual.setCedula(txtCedula.getText().trim());
            actual.setNombre(txtNombre.getText().trim());
            actual.setApellido(txtApellido.getText().trim());
            actual.setTelefono(txtTelefono.getText().trim());
            actual.setNumeroLicencia(txtLicencia.getText().trim());
            actual.setActivo(chkActivo.isSelected());
            try {
                actual.setVencimientoLicencia(LocalDate.parse(txtVence.getText().trim()));
            } catch (DateTimeParseException ex) {
                throw new NegocioException("La fecha debe tener el formato aaaa-mm-dd (ej. 2028-05-30).");
            }
            servicio.guardar(actual);
            dialogo.dispose();
            cargar(txtBuscar.getText());
        } catch (NegocioException ex) {
            aviso(ex.getMessage());
        }
    }

    private void desactivar() {
        if (actual.getId() == 0) {
            aviso("Selecciona un conductor de la tabla.");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Desactivar a " + actual.getNombre() + " " + actual.getApellido() + "?",
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
