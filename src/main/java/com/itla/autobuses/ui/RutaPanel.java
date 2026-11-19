package com.itla.autobuses.ui;

import com.itla.autobuses.model.Ruta;
import com.itla.autobuses.service.RutaService;
import com.itla.autobuses.util.NegocioException;
import com.itla.autobuses.util.Sesion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

public class RutaPanel extends JPanel {

    private final RutaService servicio = new RutaService();
    private List<Ruta> datos = new ArrayList<>();
    private Ruta actual = new Ruta();

    private final JTextField txtBuscar = Tema.campo("Escribe para buscar");
    private final JTextField txtCodigo = Tema.campo("R-001");
    private final JTextField txtOrigen = Tema.campo("Origen");
    private final JTextField txtDestino = Tema.campo("Destino");
    private final JTextField txtDistancia = Tema.campo("155.00");
    private final JTextField txtDuracion = Tema.campo("120");
    private final JCheckBox chkActivo = new JCheckBox("Activa", true);

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Id", "Codigo", "Origen", "Destino", "Distancia", "Duracion", "Estado"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final javax.swing.JTable tabla = Tema.tabla(modelo);

    private JDialog dialogo;
    private JButton btnDesactivar;

    public RutaPanel() {
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
        add(Tema.contenidoSuperior("Rutas",
                "Administra y consulta los recorridos.", filtros), BorderLayout.NORTH);

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
        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setBackground(Color.WHITE);
        form.add(etiqueta("Codigo")); form.add(txtCodigo);
        form.add(etiqueta("Origen")); form.add(txtOrigen);
        form.add(etiqueta("Destino")); form.add(txtDestino);
        form.add(etiqueta("Distancia km")); form.add(txtDistancia);
        form.add(etiqueta("Duracion min")); form.add(txtDuracion);
        form.add(etiqueta("Estado")); form.add(chkActivo);
        form.setBorder(BorderFactory.createEmptyBorder(4, 4, 10, 4));

        btnDesactivar = Tema.botonPeligro("Desactivar", Iconos.de("alerta", Tema.ERROR));
        btnDesactivar.addActionListener(e -> desactivar());
        JButton btnCancelar = Tema.botonSecundario("Cancelar");
        btnCancelar.addActionListener(e -> dialogo.dispose());
        JButton btnGuardar = Tema.boton("Guardar", Iconos.de("check", Color.WHITE));
        btnGuardar.addActionListener(e -> guardar());

        Frame padre = (Frame) SwingUtilities.getWindowAncestor(this);
        return Tema.dialogo(padre, "Nuevo registro",
                "Complete los datos de la ruta.", form,
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
            for (Ruta r : datos) {
                modelo.addRow(new Object[]{r.getId(), r.getCodigo(), r.getOrigen(), r.getDestino(),
                        r.getDistanciaKm(), r.getDuracionMin(), r.isActivo() ? "Activa" : "Inactiva"});
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
        for (Ruta r : datos) {
            if (r.getId() == id) {
                actual = r;
                txtCodigo.setText(r.getCodigo());
                txtOrigen.setText(r.getOrigen());
                txtDestino.setText(r.getDestino());
                txtDistancia.setText(r.getDistanciaKm().toPlainString());
                txtDuracion.setText(String.valueOf(r.getDuracionMin()));
                chkActivo.setSelected(r.isActivo());
                return;
            }
        }
    }

    private void limpiar() {
        actual = new Ruta();
        tabla.clearSelection();
        txtCodigo.setText("");
        txtOrigen.setText("");
        txtDestino.setText("");
        txtDistancia.setText("");
        txtDuracion.setText("");
        chkActivo.setSelected(true);
        txtCodigo.requestFocus();
    }

    private void guardar() {
        try {
            actual.setCodigo(txtCodigo.getText().trim());
            actual.setOrigen(txtOrigen.getText().trim());
            actual.setDestino(txtDestino.getText().trim());
            actual.setDistanciaKm(ValidacionUI.decimal(txtDistancia, "la", "distancia"));
            actual.setDuracionMin(ValidacionUI.entero(txtDuracion, "la", "duracion"));
            actual.setActivo(chkActivo.isSelected());
            servicio.guardar(actual);
            dialogo.dispose();
            cargar(txtBuscar.getText());
        } catch (NegocioException ex) {
            aviso(ex.getMessage());
        }
    }

    private void desactivar() {
        if (actual.getId() == 0) {
            aviso("Selecciona una ruta de la tabla.");
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
        javax.swing.JOptionPane.showMessageDialog(this, msg, "Aviso", javax.swing.JOptionPane.WARNING_MESSAGE);
    }
}
