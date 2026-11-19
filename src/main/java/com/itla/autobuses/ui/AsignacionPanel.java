package com.itla.autobuses.ui;

import com.itla.autobuses.model.Asignacion;
import com.itla.autobuses.model.Autobus;
import com.itla.autobuses.model.Conductor;
import com.itla.autobuses.model.Ruta;
import com.itla.autobuses.service.AsignacionService;
import com.itla.autobuses.service.AutobusService;
import com.itla.autobuses.service.ConductorService;
import com.itla.autobuses.service.RutaService;
import com.itla.autobuses.util.NegocioException;
import com.itla.autobuses.util.Sesion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
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
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class AsignacionPanel extends JPanel {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AsignacionService servicio = new AsignacionService();
    private final ConductorService conductores = new ConductorService();
    private final AutobusService autobuses = new AutobusService();
    private final RutaService rutas = new RutaService();

    private List<Asignacion> datos = new ArrayList<>();
    private Asignacion actual = new Asignacion();

    private final JTextField txtBuscar = Tema.campo("Buscar asignacion");
    private final JComboBox<Conductor> cmbConductor = new JComboBox<>();
    private final JComboBox<Autobus> cmbAutobus = new JComboBox<>();
    private final JComboBox<Ruta> cmbRuta = new JComboBox<>();
    private final JTextField txtInicio = Tema.campo("aaaa-mm-dd HH:mm");
    private final JTextField txtFin = Tema.campo("aaaa-mm-dd HH:mm");
    private final JComboBox<String> cmbEstado = new JComboBox<>(
            new String[]{Asignacion.PROGRAMADA, Asignacion.COMPLETADA});

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Id", "Conductor", "Autobus", "Ruta", "Inicio", "Fin", "Estado"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final javax.swing.JTable tabla = Tema.tabla(modelo);

    private JDialog dialogo;
    private JButton btnCancelarAsignacion;

    public AsignacionPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Tema.FONDO);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filtros.setBackground(Tema.FONDO);
        if (Sesion.getInstancia().esAdmin()) {
            JButton btnNuevo = Tema.boton("Asignacion", Iconos.de("nuevo", Color.WHITE));
            btnNuevo.addActionListener(e -> abrirDialogo(true));
            filtros.add(btnNuevo);
            filtros.add(Tema.separador());
        }
        filtros.add(txtBuscar);
        JButton btnBuscar = Tema.botonSecundario("Buscar", Iconos.de("buscar", Tema.VERDE));
        JButton btnExportar = Tema.botonSecundario("Exportar CSV");
        filtros.add(btnBuscar);
        filtros.add(btnExportar);
        add(Tema.contenidoSuperior("Asignaciones",
                "Programa viajes y controla la disponibilidad de recursos.", filtros), BorderLayout.NORTH);

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
        btnExportar.addActionListener(e -> exportar());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionar();
            }
        });

        cargarCombos();
        cargar("");
    }

    private void abrirDialogo(boolean nuevo) {
        if (nuevo) {
            limpiar();
        }
        if (dialogo == null) {
            dialogo = crearDialogo();
        }
        dialogo.setTitle(nuevo ? "Nueva asignacion" : "Editar asignacion");
        btnCancelarAsignacion.setVisible(!nuevo && actual.getId() > 0);
        dialogo.setVisible(true);
    }

    private JDialog crearDialogo() {
        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        form.setBackground(Color.WHITE);
        form.add(etiqueta("Conductor")); form.add(cmbConductor);
        form.add(etiqueta("Autobus")); form.add(cmbAutobus);
        form.add(etiqueta("Ruta")); form.add(cmbRuta);
        form.add(etiqueta("Inicio")); form.add(txtInicio);
        form.add(etiqueta("Fin")); form.add(txtFin);
        form.setBorder(BorderFactory.createEmptyBorder(4, 4, 10, 4));

        JPanel estado = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        estado.setOpaque(false);
        estado.add(etiqueta("Estado"));
        estado.add(cmbEstado);

        JPanel contenido = new JPanel(new BorderLayout(0, 8));
        contenido.setBackground(Color.WHITE);
        contenido.add(form, BorderLayout.CENTER);
        contenido.add(estado, BorderLayout.SOUTH);

        btnCancelarAsignacion = Tema.botonPeligro("Cancelar asignacion", Iconos.de("alerta", Tema.ERROR));
        btnCancelarAsignacion.addActionListener(e -> cancelar());
        JButton btnCancelar = Tema.botonSecundario("Cancelar");
        btnCancelar.addActionListener(e -> dialogo.dispose());
        JButton btnGuardar = Tema.boton("Guardar", Iconos.de("check", Color.WHITE));
        btnGuardar.addActionListener(e -> guardar());

        Frame padre = (Frame) SwingUtilities.getWindowAncestor(this);
        return Tema.dialogo(padre, "Nueva asignacion",
                "Selecciona los recursos y el horario del viaje.", contenido,
                btnCancelarAsignacion, btnCancelar, btnGuardar);
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.fuente(11, Font.BOLD));
        l.setForeground(Tema.TEXTO);
        return l;
    }

    private void cargarCombos() {
        try {
            for (Conductor k : conductores.listar()) {
                if (k.isActivo()) {
                    cmbConductor.addItem(k);
                }
            }
            for (Autobus b : autobuses.listarOperativos()) {
                cmbAutobus.addItem(b);
            }
            for (Ruta r : rutas.listarActivas()) {
                cmbRuta.addItem(r);
            }
        } catch (NegocioException ex) {
            aviso(ex.getMessage());
        }
    }

    private void cargar(String texto) {
        try {
            datos = servicio.buscar(texto);
            modelo.setRowCount(0);
            for (Asignacion a : datos) {
                modelo.addRow(new Object[]{a.getId(), a.getNombreConductor(), a.getPlacaAutobus(),
                        a.getCodigoRuta(), texto(a.getFechaInicio()), texto(a.getFechaFin()), a.getEstado()});
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
        for (Asignacion a : datos) {
            if (a.getId() == id) {
                actual = a;
                seleccionarConductor(a.getIdConductor());
                seleccionarAutobus(a.getIdAutobus());
                seleccionarRuta(a.getIdRuta());
                txtInicio.setText(texto(a.getFechaInicio()));
                txtFin.setText(texto(a.getFechaFin()));
                cmbEstado.setSelectedItem(Asignacion.COMPLETADA.equals(a.getEstado())
                        ? Asignacion.COMPLETADA : Asignacion.PROGRAMADA);
                return;
            }
        }
    }

    private void limpiar() {
        actual = new Asignacion();
        tabla.clearSelection();
        if (cmbConductor.getItemCount() > 0) cmbConductor.setSelectedIndex(0);
        if (cmbAutobus.getItemCount() > 0) cmbAutobus.setSelectedIndex(0);
        if (cmbRuta.getItemCount() > 0) cmbRuta.setSelectedIndex(0);
        txtInicio.setText("");
        txtFin.setText("");
        cmbEstado.setSelectedItem(Asignacion.PROGRAMADA);
    }

    private void guardar() {
        try {
            actual.setIdConductor(seleccionId(cmbConductor.getSelectedItem()));
            actual.setIdAutobus(seleccionId(cmbAutobus.getSelectedItem()));
            actual.setIdRuta(seleccionId(cmbRuta.getSelectedItem()));
            actual.setFechaInicio(parsear(txtInicio.getText()));
            actual.setFechaFin(parsear(txtFin.getText()));
            actual.setEstado((String) cmbEstado.getSelectedItem());
            servicio.guardar(actual);
            dialogo.dispose();
            cargar(txtBuscar.getText());
        } catch (NegocioException | DateTimeParseException ex) {
            aviso(ex.getMessage());
        }
    }

    private void cancelar() {
        if (actual.getId() == 0) {
            aviso("Selecciona una asignacion de la tabla.");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this, "Cancelar la asignacion seleccionada?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            servicio.cancelar(actual);
            dialogo.dispose();
            cargar(txtBuscar.getText());
        } catch (NegocioException ex) {
            aviso(ex.getMessage());
        }
    }

    private void exportar() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("asignaciones.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File destino = chooser.getSelectedFile();
        try (BufferedWriter w = Files.newBufferedWriter(destino.toPath(), StandardCharsets.UTF_8)) {
            w.write("Id;Conductor;Autobus;Ruta;Inicio;Fin;Estado\n");
            for (Asignacion a : datos) {
                w.write(a.getId() + ";" + a.getNombreConductor() + ";" + a.getPlacaAutobus()
                        + ";" + a.getCodigoRuta() + ";" + texto(a.getFechaInicio())
                        + ";" + texto(a.getFechaFin()) + ";" + a.getEstado() + "\n");
            }
            JOptionPane.showMessageDialog(this, "Archivo exportado correctamente.");
        } catch (Exception ex) {
            aviso("No se pudo exportar: " + ex.getMessage());
        }
    }

    private LocalDateTime parsear(String valor) throws DateTimeParseException {
        return LocalDateTime.parse(valor.trim(), FORMATO);
    }

    private String texto(LocalDateTime valor) {
        return valor == null ? "" : FORMATO.format(valor);
    }

    private int seleccionId(Object item) {
        if (item instanceof Conductor k) return k.getId();
        if (item instanceof Autobus b) return b.getId();
        if (item instanceof Ruta r) return r.getId();
        return 0;
    }

    private void seleccionarConductor(int id) {
        for (int i = 0; i < cmbConductor.getItemCount(); i++) {
            if (cmbConductor.getItemAt(i).getId() == id) {
                cmbConductor.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarAutobus(int id) {
        for (int i = 0; i < cmbAutobus.getItemCount(); i++) {
            if (cmbAutobus.getItemAt(i).getId() == id) {
                cmbAutobus.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarRuta(int id) {
        for (int i = 0; i < cmbRuta.getItemCount(); i++) {
            if (cmbRuta.getItemAt(i).getId() == id) {
                cmbRuta.setSelectedIndex(i);
                return;
            }
        }
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
}
