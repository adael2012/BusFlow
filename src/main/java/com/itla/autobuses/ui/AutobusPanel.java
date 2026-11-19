package com.itla.autobuses.ui;

import com.itla.autobuses.model.Autobus;
import com.itla.autobuses.service.AutobusService;
import com.itla.autobuses.util.NegocioException;
import com.itla.autobuses.util.Sesion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
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

public class AutobusPanel extends JPanel {

    private final AutobusService servicio = new AutobusService();
    private List<Autobus> datos = new ArrayList<>();
    private Autobus actual = new Autobus();

    private final JTextField txtBuscar = Tema.campo("Escribe para buscar");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Id", "Placa", "Marca", "Modelo", "Anio", "Capacidad", "Estado"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final javax.swing.JTable tabla = Tema.tabla(modelo);

    private final JTextField txtPlaca = Tema.campo("I123456");
    private final JTextField txtMarca = Tema.campo("Marca");
    private final JTextField txtModelo = Tema.campo("Modelo");
    private final JTextField txtAnio = Tema.campo("2024");
    private final JTextField txtCapacidad = Tema.campo("30");
    private final JComboBox<String> cmbEstado = new JComboBox<>(
            new String[]{Autobus.OPERATIVO, Autobus.MANTENIMIENTO, Autobus.BAJA});

    private JDialog dialogo;
    private JButton btnMantenimiento;
    private JButton btnBaja;

    public AutobusPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Tema.FONDO);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filtros.setBackground(Tema.FONDO);
        if (Sesion.getInstancia().esAdmin()) {
            JButton btnNuevo = Tema.boton("Nuevo", Iconos.de("nuevo", Color.WHITE));
            btnNuevo.addActionListener(e -> abrirDialogo(true));
            filtros.add(btnNuevo);
            filtros.add(Tema.separador());
        }
        filtros.add(txtBuscar);
        JButton btnBuscar = Tema.botonSecundario("Buscar", Iconos.de("buscar", Tema.VERDE));
        filtros.add(btnBuscar);
        add(Tema.contenidoSuperior("Autobuses",
                "Administra y consulta la flota de unidades.", filtros), BorderLayout.NORTH);

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
        boolean editable = !nuevo && actual.getId() > 0;
        btnMantenimiento.setVisible(editable);
        btnBaja.setVisible(editable);
        dialogo.setVisible(true);
    }

    private JDialog crearDialogo() {
        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setBackground(Color.WHITE);
        form.add(etiqueta("Placa")); form.add(txtPlaca);
        form.add(etiqueta("Marca")); form.add(txtMarca);
        form.add(etiqueta("Modelo")); form.add(txtModelo);
        form.add(etiqueta("Anio")); form.add(txtAnio);
        form.add(etiqueta("Capacidad")); form.add(txtCapacidad);
        form.add(etiqueta("Estado")); form.add(cmbEstado);
        form.setBorder(BorderFactory.createEmptyBorder(4, 4, 10, 4));

        btnMantenimiento = Tema.botonSecundario("Mantenimiento");
        btnMantenimiento.addActionListener(e -> cambiarEstado(Autobus.MANTENIMIENTO));
        btnBaja = Tema.botonPeligro("Baja");
        btnBaja.addActionListener(e -> cambiarEstado(Autobus.BAJA));
        JButton btnCancelar = Tema.botonSecundario("Cancelar");
        btnCancelar.addActionListener(e -> dialogo.dispose());
        JButton btnGuardar = Tema.boton("Guardar", Iconos.de("check", Color.WHITE));
        btnGuardar.addActionListener(e -> guardar());

        Frame padre = (Frame) SwingUtilities.getWindowAncestor(this);
        return Tema.dialogo(padre, "Nuevo registro",
                "Complete los datos del autobus.", form,
                btnMantenimiento, btnBaja, btnCancelar, btnGuardar);
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
            for (Autobus a : datos) {
                modelo.addRow(new Object[]{a.getId(), a.getPlaca(), a.getMarca(), a.getModelo(),
                        a.getAnio(), a.getCapacidad(), a.getEstado()});
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
        for (Autobus a : datos) {
            if (a.getId() == id) {
                actual = a;
                txtPlaca.setText(a.getPlaca());
                txtMarca.setText(a.getMarca());
                txtModelo.setText(a.getModelo());
                txtAnio.setText(String.valueOf(a.getAnio()));
                txtCapacidad.setText(String.valueOf(a.getCapacidad()));
                cmbEstado.setSelectedItem(a.getEstado());
                return;
            }
        }
    }

    private void limpiar() {
        actual = new Autobus();
        tabla.clearSelection();
        txtPlaca.setText("");
        txtMarca.setText("");
        txtModelo.setText("");
        txtAnio.setText("");
        txtCapacidad.setText("");
        cmbEstado.setSelectedItem(Autobus.OPERATIVO);
        txtPlaca.requestFocus();
    }

    private void guardar() {
        try {
            actual.setPlaca(txtPlaca.getText().trim());
            actual.setMarca(txtMarca.getText().trim());
            actual.setModelo(txtModelo.getText().trim());
            actual.setAnio(Integer.parseInt(txtAnio.getText().trim()));
            actual.setCapacidad(Integer.parseInt(txtCapacidad.getText().trim()));
            actual.setEstado((String) cmbEstado.getSelectedItem());
            servicio.guardar(actual);
            dialogo.dispose();
            cargar(txtBuscar.getText());
        } catch (NegocioException | NumberFormatException ex) {
            aviso(ex.getMessage());
        }
    }

    private void cambiarEstado(String estado) {
        if (actual.getId() == 0) {
            aviso("Selecciona un autobus de la tabla.");
            return;
        }
        try {
            servicio.cambiarEstado(actual, estado);
            dialogo.dispose();
            cargar(txtBuscar.getText());
        } catch (NegocioException ex) {
            aviso(ex.getMessage());
        }
    }

    private void aviso(String msg) {
        javax.swing.JOptionPane.showMessageDialog(this, msg, "Aviso", javax.swing.JOptionPane.WARNING_MESSAGE);
    }
}
