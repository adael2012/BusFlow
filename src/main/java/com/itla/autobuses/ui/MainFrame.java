package com.itla.autobuses.ui;

import com.itla.autobuses.model.Usuario;
import com.itla.autobuses.service.AuthService;
import com.itla.autobuses.util.ConexionDB;
import com.itla.autobuses.util.Sesion;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {

    private final AuthService auth = new AuthService();
    private final Map<String, JPanel> panels = new HashMap<>();
    private final List<JButton> menu = new ArrayList<>();
    private final java.awt.CardLayout tarjetas = new java.awt.CardLayout();
    private final JPanel area = new JPanel(tarjetas);
    private JLabel tituloSeccion;

    public MainFrame() {
        super("Ruta Control - Sistema de Control de Autobuses");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Usuario u = Sesion.getInstancia().getUsuario();

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Tema.FONDO);
        raiz.add(crearBarraLateral(u), BorderLayout.WEST);
        raiz.add(crearBarraSuperior(u), BorderLayout.NORTH);

        area.setBackground(Tema.FONDO);
        area.setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));
        raiz.add(area, BorderLayout.CENTER);
        setContentPane(raiz);

        mostrar("inicio", "Panel de administracion", menu.get(0));
        setSize(1180, 760);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);
    }

    private JPanel crearBarraLateral(Usuario u) {
        JPanel sidebar = new JPanel(new BorderLayout(0, 18));
        sidebar.setBackground(Tema.VERDE_OSCURO);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(26, 16, 20, 16));

        JPanel marca = new JPanel(new java.awt.BorderLayout(10, 0));
        marca.setOpaque(false);
        JPanel simbolo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Tema.VERDE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(java.awt.Color.WHITE);
                g2.fillRoundRect(6, 10, getWidth() - 12, getHeight() - 20, 4, 4);
                g2.setColor(Tema.VERDE);
                g2.fillOval(9, getHeight() - 12, 5, 5);
                g2.fillOval(getWidth() - 14, getHeight() - 12, 5, 5);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(34, 30);
            }
        };
        simbolo.setOpaque(false);
        JPanel textos = new JPanel(new java.awt.GridLayout(2, 1));
        textos.setOpaque(false);
        JLabel nombre = new JLabel("RUTA CONTROL");
        nombre.setFont(Tema.fuente(13, Font.BOLD));
        nombre.setForeground(java.awt.Color.WHITE);
        JLabel lema = new JLabel("Gestion de transporte");
        lema.setFont(Tema.fuente(10));
        lema.setForeground(Tema.MENU_TEXTO);
        textos.add(nombre);
        textos.add(lema);
        marca.add(simbolo, java.awt.BorderLayout.WEST);
        marca.add(textos, java.awt.BorderLayout.CENTER);

        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setOpaque(false);

        seccion(nav, "GENERAL");
        agregarItem(nav, "inicio", "Inicio", "inicio", "Panel de administracion");
        agregarItem(nav, "conductores", "Conductores", "conductores", "Administra y consulta los registros del sistema");
        agregarItem(nav, "autobuses", "Autobuses", "autobuses", "Administra y consulta la flota de unidades");
        agregarItem(nav, "rutas", "Rutas", "rutas", "Administra y consulta los recorridos");

        seccion(nav, "OPERACION");
        agregarItem(nav, "asignaciones", "Asignaciones", "asignaciones", "Programa viajes y controla la disponibilidad");
        agregarItem(nav, "disponibilidad", "Disponibilidad", "disponibilidad", "Consulta los recursos libres por periodo");

        if (u.esAdmin()) {
            seccion(nav, "SISTEMA");
            agregarItem(nav, "usuarios", "Usuarios", "usuarios", "Administra cuentas, roles y acceso");
            agregarItem(nav, "bitacora", "Bitacora", "bitacora", "Consulta el historial de acciones registradas");
        }

        JPanel perfil = new JPanel(new java.awt.BorderLayout(10, 0));
        perfil.setOpaque(false);
        perfil.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255, 255, 255, 35)),
                BorderFactory.createEmptyBorder(14, 0, 0, 0)));
        perfil.add(avatar(iniciales(u.getUsername())), java.awt.BorderLayout.WEST);
        JPanel datos = new JPanel(new java.awt.GridLayout(2, 1));
        datos.setOpaque(false);
        JLabel nombreUsuario = new JLabel(u.getUsername());
        nombreUsuario.setFont(Tema.fuente(12, Font.BOLD));
        nombreUsuario.setForeground(java.awt.Color.WHITE);
        JLabel rol = new JLabel(u.getRolNombre());
        rol.setFont(Tema.fuente(10));
        rol.setForeground(Tema.MENU_TEXTO);
        datos.add(nombreUsuario);
        datos.add(rol);
        perfil.add(datos, java.awt.BorderLayout.CENTER);

        sidebar.add(marca, BorderLayout.NORTH);
        sidebar.add(nav, BorderLayout.CENTER);
        sidebar.add(perfil, BorderLayout.SOUTH);
        return sidebar;
    }

    private String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "US";
        }
        String[] partes = nombre.split("[._-]+");
        StringBuilder sb = new StringBuilder();
        for (String parte : partes) {
            if (!parte.isEmpty()) {
                sb.append(Character.toUpperCase(parte.charAt(0)));
            }
            if (sb.length() == 2) {
                break;
            }
        }
        return sb.length() == 0 ? "US" : sb.toString();
    }

    private JPanel avatar(String texto) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Tema.VERDE);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(java.awt.Color.WHITE);
                g2.setFont(Tema.fuente(12, Font.BOLD));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(texto, (getWidth() - fm.stringWidth(texto)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(36, 36);
            }
        };
        p.setOpaque(false);
        return p;
    }

    private void seccion(JPanel nav, String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.fuente(9, Font.BOLD));
        l.setForeground(new Color(140, 175, 160));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));
        nav.add(l);
    }

    private void agregarItem(JPanel nav, String clave, String texto, String icono, String descripcion) {
        JButton b = Tema.botonMenu(texto, Iconos.de(icono, Tema.MENU_TEXTO));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(208, 42));
        b.addActionListener(e -> mostrar(clave, descripcion, b));
        menu.add(b);
        nav.add(b);
    }

    private JPanel crearBarraSuperior(Usuario u) {
        tituloSeccion = new JLabel("Panel de administracion");
        tituloSeccion.setFont(Tema.fuente(16, Font.BOLD));
        tituloSeccion.setForeground(Tema.TEXTO);

        boolean conectado;
        try {
            conectado = ConexionDB.getInstancia().probar();
        } catch (RuntimeException ex) {
            conectado = false;
        }
        JLabel estado = Tema.chip(conectado ? "Sistema activo" : "Sin conexion",
                conectado ? Tema.EXITO : Tema.ERROR);

        JButton btnClave = Tema.botonPlano("Cambiar contrasena");
        btnClave.setIcon(Iconos.de("llave", Tema.VERDE));
        btnClave.setIconTextGap(6);
        btnClave.setForeground(Tema.VERDE);
        btnClave.addActionListener(e -> new CambiarContrasenaDialog(this).setVisible(true));

        JButton btnSalir = Tema.botonPlano("Cerrar sesion");
        btnSalir.setIcon(Iconos.de("salir", Tema.ERROR));
        btnSalir.setIconTextGap(6);
        btnSalir.setForeground(Tema.ERROR);
        btnSalir.addActionListener(e -> cerrarSesion());

        JPanel identidad = new JPanel(new java.awt.GridLayout(2, 1, 0, 1));
        identidad.setOpaque(false);
        JLabel nombreUsuario = new JLabel(u.getUsername());
        nombreUsuario.setFont(Tema.fuente(11, Font.BOLD));
        nombreUsuario.setForeground(Tema.TEXTO);
        JLabel perfil = new JLabel(u.getRolNombre());
        perfil.setFont(Tema.fuente(10));
        perfil.setForeground(Tema.TEXTO_SECUNDARIO);
        identidad.add(nombreUsuario);
        identidad.add(perfil);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        derecha.setOpaque(false);
        derecha.add(identidad);
        derecha.add(estado);
        derecha.add(btnClave);
        derecha.add(btnSalir);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setPreferredSize(new Dimension(0, 66));
        barra.setBackground(java.awt.Color.WHITE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE),
                BorderFactory.createEmptyBorder(0, 22, 0, 18)));
        barra.add(tituloSeccion, BorderLayout.WEST);
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    private void mostrar(String clave, String titulo, JButton boton) {
        JPanel panel = panels.computeIfAbsent(clave, this::crearPanel);
        if (panel.getParent() == null) {
            area.add(panel, clave);
        }
        tarjetas.show(area, clave);
        tituloSeccion.setText(titulo);
        Tema.activarMenu(boton, menu);
    }

    private JPanel crearPanel(String clave) {
        return switch (clave) {
            case "conductores" -> new ConductorPanel();
            case "autobuses" -> new AutobusPanel();
            case "rutas" -> new RutaPanel();
            case "asignaciones" -> new AsignacionPanel();
            case "disponibilidad" -> new DisponibilidadPanel();
            case "usuarios" -> new UsuarioPanel();
            case "bitacora" -> new BitacoraPanel();
            default -> new InicioPanel();
        };
    }

    private void cerrarSesion() {
        auth.logout();
        new LoginFrame().setVisible(true);
        dispose();
    }
}
