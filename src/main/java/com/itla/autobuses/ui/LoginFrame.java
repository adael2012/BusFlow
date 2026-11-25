package com.itla.autobuses.ui;

import com.itla.autobuses.service.FabricaServicios;

import com.itla.autobuses.service.AuthService;
import com.itla.autobuses.util.NegocioException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

public class LoginFrame extends JFrame {

    private final JTextField txtUsuario = Tema.campo("Usuario");
    private final JPasswordField txtClave = Tema.clave("Contraseña");
    private final JCheckBox chkMostrar = new JCheckBox("Mostrar contraseña");
    private final JLabel lblError = new JLabel(" ");
    private final JButton btnIngresar = Tema.boton("Ingresar", Iconos.de("check", Color.WHITE));
    private final AuthService auth = FabricaServicios.auth();

    public LoginFrame() {
        super("BusFlow - Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(crearMarca(), BorderLayout.WEST);
        raiz.add(crearFormulario(), BorderLayout.CENTER);
        setContentPane(raiz);

        getRootPane().setDefaultButton(btnIngresar);
        btnIngresar.addActionListener(e -> intentarLogin());
        chkMostrar.addActionListener(e ->
                txtClave.setEchoChar(chkMostrar.isSelected() ? (char) 0 : '\u2022'));

        setSize(1000, 620);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(null);
    }

    private JPanel crearMarca() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 16));
                g2.fillOval(getWidth() - 180, -70, 280, 280);
                g2.fillOval(-130, getHeight() - 210, 270, 270);
                g2.dispose();
            }
        };
        p.setBackground(Tema.VERDE_OSCURO);
        p.setPreferredSize(new Dimension(420, 0));
        p.setBorder(BorderFactory.createEmptyBorder(46, 40, 36, 40));

        JPanel identidad = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        identidad.setOpaque(false);
        JLabel simbolo = new JLabel(Tema.logo(36, 36));
        identidad.add(simbolo);
        JLabel etiqueta = new JLabel("BUSFLOW");
        etiqueta.setFont(Tema.fuente(12, Font.BOLD));
        etiqueta.setForeground(Tema.VERDE_CLARO);
        identidad.add(etiqueta);

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);
        textos.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
        textos.add(linea("BUSFLOW", 30, Color.WHITE));
        textos.add(linea("Control de autobuses", 15, Tema.VERDE_CLARO));
        textos.add(linea("Administración de transporte", 13, Tema.VERDE_CLARO));
        textos.add(linea("Conductores, unidades y rutas", 13, Tema.VERDE_CLARO));

        JPanel centro = new JPanel(new BorderLayout(0, 16));
        centro.setOpaque(false);
        centro.add(textos, BorderLayout.NORTH);
        centro.add(logoCentral(), BorderLayout.CENTER);

        JLabel pie = new JLabel("Acceso protegido - Intentos controlados");
        pie.setFont(Tema.fuente(10));
        pie.setForeground(Tema.MENU_TEXTO);

        p.add(identidad, BorderLayout.NORTH);
        p.add(centro, BorderLayout.CENTER);
        p.add(pie, BorderLayout.SOUTH);
        return p;
    }

    private JLabel linea(String texto, int tamano, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.fuente(tamano, Font.BOLD));
        l.setForeground(color);
        l.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        return l;
    }

    private JPanel logoCentral() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false);
        JLabel logo = new JLabel(Tema.logo(210, 210));
        logo.setHorizontalAlignment(JLabel.CENTER);
        p.add(logo);
        return p;
    }

    private JPanel crearFormulario() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.anchor = GridBagConstraints.WEST;

        g.gridy = 0;
        g.insets = new Insets(0, 0, 4, 0);
        JLabel titulo = new JLabel("Bienvenido");
        titulo.setFont(Tema.fuente(28, Font.BOLD));
        titulo.setForeground(Tema.VERDE);
        p.add(titulo, g);

        g.gridy = 1;
        g.insets = new Insets(0, 0, 30, 0);
        JLabel subtitulo = new JLabel("Ingresa tus credenciales para continuar");
        subtitulo.setFont(Tema.fuente(12));
        subtitulo.setForeground(Tema.TEXTO_SECUNDARIO);
        p.add(subtitulo, g);

        g.gridy = 2;
        g.insets = new Insets(0, 0, 6, 0);
        p.add(etiquetaCampo("Usuario"), g);

        g.gridy = 3;
        g.insets = new Insets(0, 0, 16, 0);
        txtUsuario.setColumns(24);
        p.add(txtUsuario, g);

        g.gridy = 4;
        p.add(etiquetaCampo("Contraseña"), g);

        g.gridy = 5;
        g.insets = new Insets(0, 0, 8, 0);
        txtClave.setColumns(24);
        txtClave.setEchoChar('\u2022');
        p.add(txtClave, g);

        g.gridy = 6;
        g.insets = new Insets(0, 0, 14, 0);
        chkMostrar.setFont(Tema.fuente(11));
        chkMostrar.setForeground(Tema.TEXTO_SECUNDARIO);
        chkMostrar.setOpaque(false);
        p.add(chkMostrar, g);

        g.gridy = 7;
        g.insets = new Insets(0, 0, 6, 0);
        lblError.setFont(Tema.fuente(11));
        lblError.setForeground(Tema.ERROR);
        p.add(lblError, g);

        g.gridy = 8;
        g.insets = new Insets(4, 0, 0, 0);
        btnIngresar.setPreferredSize(new Dimension(280, 40));
        p.add(btnIngresar, g);

        if (FabricaServicios.esDemo()) {
            g.gridy = 9;
            g.insets = new Insets(14, 0, 0, 0);
            JLabel demostracion = new JLabel(
                    "<html><center>Modo demostración<br>admin / Admin123!</center></html>");
            demostracion.setFont(Tema.fuente(10));
            demostracion.setForeground(Tema.VERDE);
            demostracion.setHorizontalAlignment(JLabel.CENTER);
            p.add(demostracion, g);
        }
        return p;
    }

    private JLabel etiquetaCampo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.fuente(12, Font.BOLD));
        l.setForeground(Tema.TEXTO);
        return l;
    }

    private void intentarLogin() {
        lblError.setText(" ");
        btnIngresar.setEnabled(false);
        try {
            auth.login(txtUsuario.getText(), new String(txtClave.getPassword()));
            new MainFrame().setVisible(true);
            dispose();
        } catch (NegocioException ex) {
            lblError.setText(ex.getMessage());
            txtClave.setText("");
            txtClave.requestFocus();
        } finally {
            btnIngresar.setEnabled(true);
        }
    }
}
