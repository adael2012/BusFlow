package com.itla.autobuses.ui;

import com.itla.autobuses.service.FabricaServicios;

import com.itla.autobuses.model.EntradaBitacora;
import com.itla.autobuses.service.BitacoraService;
import com.itla.autobuses.util.NegocioException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class BitacoraPanel extends JPanel {

    private final BitacoraService servicio = FabricaServicios.bitacora();
    private final JTextField txtUsuario = Tema.campo("Usuario");
    private final JTextField txtAccion = Tema.campo("Acción");
    private final CampoFechaHora txtDesde = new CampoFechaHora(false);
    private final CampoFechaHora txtHasta = new CampoFechaHora(false);

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Id", "Fecha", "Usuario", "Acción", "Tabla", "Id registro", "Detalle"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = Tema.tabla(modelo);

    public BitacoraPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Tema.FONDO);

        JButton btnBuscar = Tema.boton("Buscar", Iconos.de("buscar", java.awt.Color.WHITE));
        JButton btnLimpiar = Tema.botonSecundario("Limpiar");
        JPanel norte = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        norte.setBackground(Tema.FONDO);
        norte.add(new JLabel("Usuario:"));
        norte.add(txtUsuario);
        norte.add(new JLabel("Acción:"));
        norte.add(txtAccion);
        norte.add(new JLabel("Desde:"));
        norte.add(txtDesde);
        norte.add(new JLabel("Hasta:"));
        norte.add(txtHasta);
        norte.add(btnBuscar);
        norte.add(btnLimpiar);
        add(Tema.contenidoSuperior("Bitácora",
                "Registro de operaciones y auditoría del sistema.", norte), BorderLayout.NORTH);
        add(Tema.scroll(tabla), BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> buscar());
        btnLimpiar.addActionListener(e -> {
            txtUsuario.setText("");
            txtAccion.setText("");
            txtDesde.setText("");
            txtHasta.setText("");
            modelo.setRowCount(0);
        });

        Tema.aplicar(this);
        buscar();
    }

    private void buscar() {
        LocalDate desde = fecha(txtDesde.getText());
        LocalDate hasta = fecha(txtHasta.getText());
        if (!txtDesde.getText().isBlank() && desde == null) {
            aviso("La fecha desde no es valida. Usa aaaa-mm-dd.");
            return;
        }
        if (!txtHasta.getText().isBlank() && hasta == null) {
            aviso("La fecha hasta no es valida. Usa aaaa-mm-dd.");
            return;
        }
        try {
            List<EntradaBitacora> datos = servicio.listar(txtUsuario.getText(), txtAccion.getText(),
                    desde, hasta);
            modelo.setRowCount(0);
            for (EntradaBitacora e : datos) {
                modelo.addRow(new Object[]{e.getId(), e.getFecha(), e.getUsername(), e.getAccion(),
                        e.getTabla(), e.getIdRegistro(), e.getDetalle()});
            }
        } catch (NegocioException ex) {
            aviso(ex.getMessage());
        }
    }

    private LocalDate fecha(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
}
