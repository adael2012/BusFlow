package com.itla.autobuses.ui;

import com.itla.autobuses.service.FabricaServicios;

import com.itla.autobuses.model.Asignacion;
import com.itla.autobuses.model.Autobus;
import com.itla.autobuses.model.Conductor;
import com.itla.autobuses.service.AsignacionService;
import com.itla.autobuses.service.AutobusService;
import com.itla.autobuses.util.NegocioException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class DisponibilidadPanel extends JPanel {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AsignacionService asignaciones = FabricaServicios.asignaciones();
    private final AutobusService autobuses = FabricaServicios.autobuses();

    private final CampoFechaHora txtInicio = new CampoFechaHora();
    private final CampoFechaHora txtFin = new CampoFechaHora();

    private final DefaultTableModel modeloConductores = new DefaultTableModel(
            new String[]{"Id", "Cédula", "Conductor", "Licencia", "Vence"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel modeloAutobuses = new DefaultTableModel(
            new String[]{"Id", "Placa", "Marca", "Modelo", "Capacidad"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel modeloAsignaciones = new DefaultTableModel(
            new String[]{"Id", "Conductor", "Autobús", "Ruta", "Inicio", "Fin", "Estado"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    private final JTable tablaConductores = Tema.tabla(modeloConductores);
    private final JTable tablaAutobuses = Tema.tabla(modeloAutobuses);
    private final JTable tablaAsignaciones = Tema.tabla(modeloAsignaciones);

    public DisponibilidadPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Tema.FONDO);

        JButton btnConsultar = Tema.boton("Consultar", Iconos.de("buscar", java.awt.Color.WHITE));
        JButton btnLimpiar = Tema.botonSecundario("Limpiar");
        JPanel norte = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        norte.setBackground(Tema.FONDO);
        norte.add(new JLabel("Inicio:"));
        norte.add(txtInicio);
        norte.add(new JLabel("Fin:"));
        norte.add(txtFin);
        norte.add(btnConsultar);
        norte.add(btnLimpiar);
        add(Tema.contenidoSuperior("Disponibilidad",
                "Recursos libres y asignaciones programadas en un rango.", norte), BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(1, 2, 8, 8));
        centro.setBackground(Tema.FONDO);
        centro.add(seccion("Conductores disponibles", tablaConductores));
        centro.add(seccion("Autobuses disponibles", tablaAutobuses));
        add(centro, BorderLayout.CENTER);

        JPanel sur = Tema.panelFormulario("Asignaciones en el rango consultado");
        sur.setLayout(new BorderLayout());
        sur.add(Tema.scroll(tablaAsignaciones), BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);

        btnConsultar.addActionListener(e -> consultar());
        txtFin.addActionListener(e -> consultar());
        btnLimpiar.addActionListener(e -> {
            txtInicio.setText("");
            txtFin.setText("");
            limpiarTablas();
        });

        LocalDateTime ahora = LocalDateTime.now().withSecond(0).withNano(0);
        txtInicio.setText(FORMATO.format(ahora));
        txtFin.setText(FORMATO.format(ahora.plusHours(2)));
        Tema.aplicar(this);
        consultar();
    }

    private JPanel seccion(String titulo, JTable tabla) {
        JPanel p = Tema.panelFormulario(titulo);
        p.setLayout(new BorderLayout());
        p.add(Tema.scroll(tabla), BorderLayout.CENTER);
        return p;
    }

    private void consultar() {
        LocalDateTime inicio;
        LocalDateTime fin;
        try {
            inicio = LocalDateTime.parse(txtInicio.getText().trim(), FORMATO);
            fin = LocalDateTime.parse(txtFin.getText().trim(), FORMATO);
        } catch (DateTimeParseException ex) {
            aviso("Usa el formato aaaa-mm-dd HH:mm en ambas fechas.");
            return;
        }
        try {
            List<Conductor> conductores = asignaciones.conductoresDisponibles(inicio, fin);
            List<Autobus> buses = autobuses.listarDisponibles(inicio, fin);
            List<Asignacion> programadas = new ArrayList<>();
            for (Asignacion a : asignaciones.listar()) {
                if (Asignacion.PROGRAMADA.equals(a.getEstado()) && a.seSolapaCon(inicio, fin)) {
                    programadas.add(a);
                }
            }
            modeloConductores.setRowCount(0);
            for (Conductor k : conductores) {
                modeloConductores.addRow(new Object[]{k.getId(), k.getCedula(),
                        k.getNombre() + " " + k.getApellido(), k.getNumeroLicencia(),
                        k.getVencimientoLicencia()});
            }
            modeloAutobuses.setRowCount(0);
            for (Autobus b : buses) {
                modeloAutobuses.addRow(new Object[]{b.getId(), b.getPlaca(), b.getMarca(),
                        b.getModelo(), b.getCapacidad()});
            }
            modeloAsignaciones.setRowCount(0);
            for (Asignacion a : programadas) {
                modeloAsignaciones.addRow(new Object[]{a.getId(), a.getNombreConductor(),
                        a.getPlacaAutobus(), a.getCodigoRuta(), texto(a.getFechaInicio()),
                        texto(a.getFechaFin()), a.getEstado()});
            }
        } catch (NegocioException ex) {
            aviso(ex.getMessage());
        }
    }

    private void limpiarTablas() {
        modeloConductores.setRowCount(0);
        modeloAutobuses.setRowCount(0);
        modeloAsignaciones.setRowCount(0);
    }

    private String texto(LocalDateTime valor) {
        return valor == null ? "" : FORMATO.format(valor);
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
}
