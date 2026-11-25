package com.itla.autobuses.ui;

import com.itla.autobuses.service.FabricaServicios;

import com.itla.autobuses.model.Conductor;
import com.itla.autobuses.model.EntradaBitacora;
import com.itla.autobuses.model.Resumen;
import com.itla.autobuses.service.BitacoraService;
import com.itla.autobuses.service.PanelService;
import com.itla.autobuses.util.NegocioException;
import com.itla.autobuses.util.Sesion;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InicioPanel extends JPanel {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final PanelService servicio = FabricaServicios.panel();
    private final BitacoraService bitacora = FabricaServicios.bitacora();
    private final JPanel areas = new JPanel(new GridLayout(1, 2, 12, 12));
    private final JPanel tarjetasPanel = tarjetas();
    private final JLabel subtitulo = new JLabel("Cargando indicadores...");

    private JList<String> actividad;
    private JList<String> alertas;

    public InicioPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Tema.FONDO);

        subtitulo.setFont(Tema.fuente(11));
        subtitulo.setForeground(Tema.TEXTO_SECUNDARIO);
        JPanel titulo = new JPanel(new java.awt.GridLayout(2, 1, 0, 3));
        titulo.setBackground(Tema.FONDO);
        JLabel nombre = new JLabel(Sesion.getInstancia().esAdmin()
                ? "Panel de administración" : "Panel de consulta");
        nombre.setFont(Tema.fuente(18, Font.BOLD));
        nombre.setForeground(Tema.VERDE);
        titulo.add(nombre);
        titulo.add(subtitulo);
        add(titulo, BorderLayout.NORTH);

        JPanel medio = new JPanel(new BorderLayout(0, 12));
        medio.setBackground(Tema.FONDO);
        medio.add(tarjetasPanel, BorderLayout.NORTH);
        areas.setBackground(Tema.FONDO);
        medio.add(areas, BorderLayout.CENTER);
        add(medio, BorderLayout.CENTER);

        cargar();
    }

    private JPanel tarjetas() {
        JPanel p = new JPanel(new GridLayout(1, 4, 12, 12));
        p.setBackground(Tema.FONDO);
        p.add(Tema.tarjeta("Conductores activos", 0, "Registros vigentes",
                Iconos.de("conductores", Tema.VERDE)));
        p.add(Tema.tarjeta("Autobuses operativos", 0, "Unidades disponibles",
                Iconos.de("autobuses", Tema.VERDE)));
        p.add(Tema.tarjeta("Asignaciones de hoy", 0, "Viajes programados",
                Iconos.de("asignaciones", Tema.VERDE)));
        p.add(Tema.tarjeta("Licencias por vencer", 0, "Proximos 30 dias",
                Iconos.de("alerta", Tema.ADVERTENCIA)));
        return p;
    }

    private void cargar() {
        new SwingWorker<DatosInicio, Void>() {
            @Override
            protected DatosInicio doInBackground() {
                DatosInicio datos = new DatosInicio();
                try {
                    datos.resumen = servicio.resumen();
                    datos.licencias = servicio.licenciasPorVencer();
                } catch (Exception e) {
                    datos.error = "No se pudieron cargar los indicadores. Revisa la conexión a la base de datos.";
                }
                if (Sesion.getInstancia().esAdmin()) {
                    try {
                        datos.actividad = bitacora.listar(null, null, null, null);
                    } catch (NegocioException e) {
                        datos.errorActividad = e.getMessage();
                    }
                } else {
                    datos.errorActividad = "La actividad reciente está disponible solo para el administrador.";
                }
                return datos;
            }

            @Override
            protected void done() {
                try {
                    pintar(get());
                } catch (Exception e) {
                    subtitulo.setText("No se pudo actualizar el panel.");
                }
            }
        }.execute();
    }

    private void pintar(DatosInicio datos) {
        if (datos.error != null) {
            subtitulo.setText(datos.error);
        } else {
            subtitulo.setText("Indicadores actualizados con la información registrada.");
        }

        actualizarTarjeta(tarjetasPanel, 0, datos.resumen.getConductoresActivos());
        actualizarTarjeta(tarjetasPanel, 1, datos.resumen.getAutobusesOperativos());
        actualizarTarjeta(tarjetasPanel, 2, datos.resumen.getAsignacionesHoy());
        actualizarTarjeta(tarjetasPanel, 3, datos.resumen.getLicenciasPorVencer());

        actividad = lista();
        DefaultListModel<String> modeloActividad = new DefaultListModel<>();
        if (datos.errorActividad != null) {
            modeloActividad.addElement(datos.errorActividad);
        } else if (datos.actividad.isEmpty()) {
            modeloActividad.addElement("Todavia no hay acciónes registradas.");
        } else {
            for (EntradaBitacora e : datos.actividad) {
                String fecha = e.getFecha() == null ? "--" : FORMATO.format(e.getFecha());
                modeloActividad.addElement(fecha + "  " + e.getAccion() + "  " + e.getTabla()
                        + "  " + resumen(e.getDetalle()));
            }
        }
        actividad.setModel(modeloActividad);

        alertas = lista();
        DefaultListModel<String> modeloAlertas = new DefaultListModel<>();
        if (datos.error != null) {
            modeloAlertas.addElement(datos.error);
        } else if (datos.licencias.isEmpty()) {
            modeloAlertas.addElement("No hay licencias que venzan en los proximos 30 dias.");
        } else {
            for (Conductor k : datos.licencias) {
                modeloAlertas.addElement(k.getNombre() + " " + k.getApellido()
                        + " - licencia " + k.getNumeroLicencia()
                        + " vence " + k.getVencimientoLicencia());
            }
        }
        alertas.setModel(modeloAlertas);

        areas.removeAll();
        areas.add(seccion(actividad, "Actividad reciente", Iconos.de("bitácora", Tema.VERDE)));
        areas.add(seccion(alertas, "Alertas", Iconos.de("alerta", Tema.ADVERTENCIA)));
        areas.revalidate();
        areas.repaint();
    }

    private void actualizarTarjeta(JPanel tarjetas, int indice, int valor) {
        JPanel tarjeta = (JPanel) tarjetas.getComponent(indice);
        for (java.awt.Component c : tarjeta.getComponents()) {
            if (c instanceof JLabel etiqueta && etiqueta.getFont() != null
                    && etiqueta.getFont().getSize() == 28) {
                etiqueta.setText(String.valueOf(valor));
            }
        }
    }

    private JList<String> lista() {
        JList<String> l = new JList<>();
        l.setFont(Tema.fuente(12));
        l.setForeground(Tema.TEXTO);
        l.setBackground(Color.WHITE);
        l.setSelectionBackground(Tema.SELECCION);
        l.setSelectionForeground(Tema.TEXTO);
        l.setFixedCellHeight(34);
        l.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        l.setCellRenderer((javax.swing.JList<? extends String> list, String value,
                           int index, boolean selected, boolean focus) -> {
            JLabel celda = new JLabel(value == null ? "" : value);
            celda.setFont(Tema.fuente(12));
            celda.setForeground(selected ? Tema.VERDE : Tema.TEXTO);
            celda.setBackground(selected ? Tema.SELECCION : Color.WHITE);
            celda.setOpaque(true);
            celda.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return celda;
        });
        return l;
    }

    private JPanel seccion(JList<String> lista, String titulo, javax.swing.Icon icono) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JPanel cabecera = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        cabecera.setOpaque(false);
        cabecera.add(new JLabel(icono));
        JLabel etiqueta = new JLabel(titulo);
        etiqueta.setFont(Tema.fuente(13, Font.BOLD));
        etiqueta.setForeground(Tema.VERDE);
        cabecera.add(etiqueta);

        p.add(cabecera, BorderLayout.NORTH);
        p.add(new JScrollPane(lista), BorderLayout.CENTER);
        p.setPreferredSize(new Dimension(320, 220));
        return p;
    }

    private String resumen(String detalle) {
        if (detalle == null || detalle.isBlank()) {
            return "";
        }
        return detalle.length() > 45 ? detalle.substring(0, 42) + "..." : detalle;
    }

    private static class DatosInicio {
        private Resumen resumen = new Resumen();
        private List<Conductor> licencias = new ArrayList<>();
        private List<EntradaBitacora> actividad = new ArrayList<>();
        private String error;
        private String errorActividad;
    }
}
