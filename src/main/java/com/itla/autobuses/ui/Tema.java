package com.itla.autobuses.ui;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.Rectangle;
import java.util.List;

public final class Tema {

    public static final Color VERDE = new Color(10, 107, 77);
    public static final Color VERDE_OSCURO = new Color(17, 59, 45);
    public static final Color VERDE_SECUNDARIO = new Color(22, 133, 93);
    public static final Color FONDO = new Color(244, 246, 244);
    public static final Color BLANCO = Color.WHITE;
    public static final Color TEXTO = new Color(30, 41, 59);
    public static final Color TEXTO_SECUNDARIO = new Color(100, 116, 139);
    public static final Color BORDE = new Color(226, 232, 240);
    public static final Color EXITO = new Color(22, 163, 106);
    public static final Color ADVERTENCIA = new Color(233, 162, 59);
    public static final Color ERROR = new Color(220, 84, 84);
    public static final Color ROJO = ERROR;
    public static final Color SELECCION = new Color(219, 239, 228);
    public static final Color FILA_ALTERNA = new Color(248, 249, 249);
    public static final Color VERDE_CLARO = new Color(182, 219, 202);
    public static final Color MENU_TEXTO = new Color(210, 225, 218);

    private static final String FAMILIA = "Inter";
    private static final String RESPALDO = "Segoe UI";

    private Tema() { }

    public static Font fuente(int tamano) {
        return new Font(FAMILIA, Font.PLAIN, tamano);
    }

    public static Font fuente(int tamano, int estilo) {
        return new Font(FAMILIA, estilo, tamano);
    }

    public static JButton boton(String texto) {
        JButton b = new JButton(texto);
        b.setFont(fuente(12, Font.BOLD));
        b.setForeground(BLANCO);
        b.setBackground(VERDE);
        b.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton boton(String texto, Icon icono) {
        JButton b = boton(texto);
        b.setIcon(icono);
        b.setIconTextGap(8);
        return b;
    }

    public static JButton botonSecundario(String texto) {
        JButton b = new JButton(texto);
        b.setFont(fuente(12));
        b.setForeground(VERDE);
        b.setBackground(BLANCO);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton botonSecundario(String texto, Icon icono) {
        JButton b = botonSecundario(texto);
        b.setIcon(icono);
        b.setIconTextGap(8);
        return b;
    }

    public static JButton botonPeligro(String texto) {
        JButton b = new JButton(texto);
        b.setFont(fuente(12));
        b.setForeground(ERROR);
        b.setBackground(BLANCO);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ERROR),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton botonPeligro(String texto, Icon icono) {
        JButton b = botonPeligro(texto);
        b.setIcon(icono);
        b.setIconTextGap(8);
        return b;
    }

    public static JButton botonPlano(String texto) {
        JButton b = new JButton(texto);
        b.setFont(fuente(11));
        b.setForeground(BLANCO);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setMargin(new Insets(4, 8, 4, 8));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JTable tabla(DefaultTableModel modelo) {
        JTable t = new JTable(modelo);
        t.setFont(fuente(12));
        t.setRowHeight(28);
        t.setGridColor(BORDE);
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        t.setSelectionBackground(SELECCION);
        t.setSelectionForeground(TEXTO);
        t.setAutoCreateRowSorter(true);
        t.getTableHeader().setFont(fuente(11, Font.BOLD));
        t.getTableHeader().setBackground(FONDO);
        t.getTableHeader().setForeground(TEXTO_SECUNDARIO);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setDefaultRenderer(cabeceraRenderer());
        TableCellRenderer r = filaRenderer();
        t.setDefaultRenderer(Object.class, r);
        t.setDefaultRenderer(Boolean.class, r);
        t.setDefaultRenderer(Number.class, r);
        for (int i = 0; i < modelo.getColumnCount(); i++) {
            if ("Estado".equals(modelo.getColumnName(i))) {
                t.getColumnModel().getColumn(i).setCellRenderer(estadoRenderer());
            }
        }
        return t;
    }

    public static JScrollPane scroll(JTable tabla) {
        JScrollPane s = new JScrollPane(tabla);
        s.setBackground(BLANCO);
        s.getViewport().setBackground(BLANCO);
        s.setBorder(BorderFactory.createLineBorder(BORDE));
        return s;
    }

    public static JPanel panelFormulario(String titulo) {
        JPanel p = new JPanel();
        p.setBackground(BLANCO);
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDE),
                titulo, TitledBorder.LEFT, TitledBorder.TOP,
                fuente(12, Font.BOLD), VERDE));
        return p;
    }

    public static JPanel encabezado(String titulo, String descripcion) {
        JPanel p = new JPanel(new java.awt.BorderLayout(0, 3));
        p.setBackground(FONDO);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        JLabel t = new JLabel(titulo);
        t.setFont(fuente(18, Font.BOLD));
        t.setForeground(VERDE);
        JLabel d = new JLabel(descripcion);
        d.setFont(fuente(12));
        d.setForeground(TEXTO_SECUNDARIO);
        p.add(t, java.awt.BorderLayout.NORTH);
        p.add(d, java.awt.BorderLayout.CENTER);
        return p;
    }

    public static JPanel contenidoSuperior(String titulo, String descripcion, JPanel filtros) {
        JPanel p = new JPanel(new java.awt.BorderLayout(0, 6));
        p.setBackground(FONDO);
        p.add(encabezado(titulo, descripcion), java.awt.BorderLayout.NORTH);
        p.add(filtros, java.awt.BorderLayout.SOUTH);
        return p;
    }

    public static JPanel barraLateral() {
        JPanel p = new JPanel();
        p.setBackground(VERDE_OSCURO);
        p.setPreferredSize(new java.awt.Dimension(235, 0));
        p.setBorder(BorderFactory.createEmptyBorder(18, 12, 18, 12));
        return p;
    }

    public static JButton botonMenu(String texto) {
        JButton b = new JButton(texto);
        b.setFont(fuente(13));
        b.setForeground(new Color(210, 225, 218));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton botonMenu(String texto, Icon icono) {
        JButton b = botonMenu(texto);
        b.setIcon(icono);
        b.setIconTextGap(12);
        return b;
    }

    public static void activarMenu(JButton activo, List<JButton> botones) {
        for (JButton b : botones) {
            boolean seleccionado = b == activo;
            b.setOpaque(seleccionado);
            b.setContentAreaFilled(seleccionado);
            b.setBackground(seleccionado ? VERDE : VERDE_OSCURO);
            b.setForeground(seleccionado ? BLANCO : new Color(210, 225, 218));
            b.setFont(fuente(13, seleccionado ? Font.BOLD : Font.PLAIN));
        }
    }

    public static JLabel etiquetaEstado(String texto) {
        JLabel l = new JLabel(texto == null ? "" : texto, SwingConstants.CENTER);
        l.setFont(fuente(11, Font.BOLD));
        l.setBorder(BorderFactory.createEmptyBorder(3, 9, 3, 9));
        l.setOpaque(true);
        String v = texto == null ? "" : texto.toLowerCase();
        Color color = EXITO;
        if (v.contains("mantenimiento") || v.contains("advertencia")) {
            color = ADVERTENCIA;
        } else if (v.contains("baja") || v.contains("cancelada") || v.contains("inactivo")) {
            color = ERROR;
        } else if (v.contains("completada")) {
            color = VERDE_SECUNDARIO;
        }
        l.setForeground(color);
        l.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 28));
        return l;
    }

    public static JPanel tarjeta(String titulo, int valor) {
        JPanel p = new JPanel(new java.awt.BorderLayout());
        p.setBackground(BLANCO);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        JLabel v = new JLabel(String.valueOf(valor), SwingConstants.CENTER);
        v.setFont(fuente(28, Font.BOLD));
        v.setForeground(VERDE);
        JLabel t = new JLabel(titulo, SwingConstants.CENTER);
        t.setFont(fuente(11));
        t.setForeground(TEXTO_SECUNDARIO);
        p.add(v, java.awt.BorderLayout.CENTER);
        p.add(t, java.awt.BorderLayout.SOUTH);
        p.setPreferredSize(new java.awt.Dimension(160, 100));
        return p;
    }

    public static JTextField campo(String pista) {
        JTextField t = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                pintarPista(this, g, pista);
            }
        };
        aplicar(t);
        return t;
    }

    public static JPasswordField clave(String pista) {
        JPasswordField t = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                pintarPista(this, g, pista);
            }
        };
        aplicar(t);
        return t;
    }

    private static void pintarPista(JTextField campo, Graphics g, String pista) {
        if (!campo.getText().isEmpty() || campo.hasFocus()) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(TEXTO_SECUNDARIO);
        g2.setFont(fuente(12));
        FontMetrics fm = g2.getFontMetrics();
        Insets in = campo.getInsets();
        g2.drawString(pista, in.left + 3,
                (campo.getHeight() + fm.getAscent() - fm.getDescent()) / 2);
        g2.dispose();
    }

    public static JPanel tarjeta(String titulo, int valor, String detalle) {
        return tarjeta(titulo, valor, detalle, null);
    }

    public static JPanel tarjeta(String titulo, int valor, String detalle, Icon icono) {
        JPanel p = new JPanel(new java.awt.BorderLayout());
        p.setBackground(BLANCO);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JPanel coronacion = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        coronacion.setOpaque(false);
        JLabel i = new JLabel(icono == null ? " " : "");
        if (icono != null) {
            i.setIcon(icono);
        }
        i.setPreferredSize(new java.awt.Dimension(16, 16));
        coronacion.add(i);

        JLabel v = new JLabel(String.valueOf(valor), SwingConstants.CENTER);
        v.setFont(fuente(28, Font.BOLD));
        v.setForeground(VERDE);
        JPanel abajo = new JPanel(new java.awt.GridLayout(2, 1, 0, 2));
        abajo.setOpaque(false);
        JLabel t = new JLabel(titulo, SwingConstants.CENTER);
        t.setFont(fuente(11, Font.BOLD));
        t.setForeground(TEXTO);
        JLabel d = new JLabel(detalle, SwingConstants.CENTER);
        d.setFont(fuente(10));
        d.setForeground(TEXTO_SECUNDARIO);
        abajo.add(t);
        abajo.add(d);
        p.add(coronacion, java.awt.BorderLayout.NORTH);
        p.add(v, java.awt.BorderLayout.CENTER);
        p.add(abajo, java.awt.BorderLayout.SOUTH);
        p.setPreferredSize(new java.awt.Dimension(180, 118));
        return p;
    }

    public static JLabel chip(String texto, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(10, Font.BOLD));
        l.setForeground(color);
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 90)),
                BorderFactory.createEmptyBorder(3, 9, 3, 9)));
        l.setOpaque(true);
        l.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 22));
        return l;
    }

    public static JSeparator separador() {
        JSeparator s = new JSeparator(JSeparator.VERTICAL);
        s.setForeground(BORDE);
        s.setPreferredSize(new java.awt.Dimension(1, 24));
        return s;
    }

    public static JDialog dialogo(Frame padre, String titulo, String descripcion,
                                  JPanel contenido, JButton... acciones) {
        JDialog d = new JDialog(padre, titulo, true);
        JPanel raiz = new JPanel(new java.awt.BorderLayout(0, 12));
        raiz.setBackground(BLANCO);
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));

        JPanel cabecera = new JPanel(new java.awt.BorderLayout(0, 3));
        cabecera.setOpaque(false);
        JLabel t = new JLabel(titulo);
        t.setFont(fuente(18, Font.BOLD));
        t.setForeground(VERDE);
        JLabel s = new JLabel(descripcion);
        s.setFont(fuente(11));
        s.setForeground(TEXTO_SECUNDARIO);
        cabecera.add(t, java.awt.BorderLayout.NORTH);
        cabecera.add(s, java.awt.BorderLayout.CENTER);

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pie.setOpaque(false);
        for (JButton b : acciones) {
            pie.add(b);
        }

        raiz.add(cabecera, java.awt.BorderLayout.NORTH);
        raiz.add(contenido, java.awt.BorderLayout.CENTER);
        raiz.add(pie, java.awt.BorderLayout.SOUTH);
        d.setContentPane(raiz);
        d.pack();
        d.setLocationRelativeTo(padre);
        d.setResizable(false);
        return d;
    }

    public static void aplicar(Component c) {
        if (c instanceof CampoFechaHora) {
            return;
        }
        if (c instanceof JButton b) {
            if (b.getFont() == null || !FAMILIA.equals(b.getFont().getFamily())) {
                b.setFont(fuente(12));
            }
            b.setFocusPainted(false);
        } else if (c instanceof JTable t) {
            t.setFont(fuente(12));
            t.setRowHeight(28);
            t.setGridColor(BORDE);
            t.setSelectionBackground(SELECCION);
            t.setSelectionForeground(TEXTO);
            TableCellRenderer r = filaRenderer();
            t.setDefaultRenderer(Object.class, r);
            t.setDefaultRenderer(Boolean.class, r);
            t.setDefaultRenderer(Number.class, r);
        } else if (c instanceof JScrollPane s) {
            s.setBackground(BLANCO);
            s.getViewport().setBackground(BLANCO);
            s.setBorder(BorderFactory.createLineBorder(BORDE));
        } else if (c instanceof JTextField t) {
            t.setFont(fuente(12));
            t.setBackground(BLANCO);
            t.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDE),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)));
            t.setCaretColor(VERDE);
        } else if (c instanceof JComboBox<?> b) {
            b.setFont(fuente(12));
            b.setBackground(BLANCO);
            b.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value,
                                                              int index, boolean selected, boolean focus) {
                    super.getListCellRendererComponent(list, value, index, selected, focus);
                    setText(value == null ? "" : value.toString());
                    return this;
                }
            });
        } else if (c instanceof JSpinner s) {
            s.setFont(fuente(12));
        } else if (c instanceof JCheckBox k) {
            k.setFont(fuente(12));
            k.setForeground(TEXTO);
        } else if (c instanceof JLabel l) {
            if (l.getFont() == null || !FAMILIA.equals(l.getFont().getFamily())) {
                l.setFont(fuente(12));
            }
        }

        if (c instanceof Container cont) {
            for (Component hijo : cont.getComponents()) {
                aplicar(hijo);
            }
        }
    }

    public static void aplicarBarra(JPanel barra) {
        barra.setBackground(BLANCO);
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE));
        for (Component c : barra.getComponents()) {
            if (c instanceof JLabel l) {
                l.setForeground(TEXTO);
            }
        }
    }

    public static void tabs(JTabbedPane t) {
        t.setFont(fuente(11));
        t.setBackground(BLANCO);
        t.setForeground(VERDE);
        t.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(Graphics g, int placement, int index,
                                              int x, int y, int w, int h, boolean selected) {
                g.setColor(selected ? BLANCO : FONDO);
                g.fillRect(x + 1, y + 2, w - 2, h - 2);
            }

            @Override
            protected void paintTabBorder(Graphics g, int placement, int index,
                                          int x, int y, int w, int h, boolean selected) {
                g.setColor(selected ? VERDE : BORDE);
                g.drawLine(x + 6, y + h - 1, x + w - 6, y + h - 1);
            }

            @Override
            protected void paintContentBorder(Graphics g, int placement, int selectedIndex) {
                g.setColor(BORDE);
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int placement, Rectangle[] rects,
                                               int index, Rectangle iconRect,
                                               Rectangle textRect, boolean selected) {
            }
        });
    }

    private static TableCellRenderer cabeceraRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean selected, boolean focus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(table, value, selected, focus, row, column);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBackground(FONDO);
                setForeground(TEXTO_SECUNDARIO);
                setFont(fuente(11, Font.BOLD));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                setOpaque(true);
                return this;
            }
        };
    }

    private static TableCellRenderer estadoRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean selected, boolean focus,
                                                           int row, int column) {
                JLabel etiqueta = etiquetaEstado(value == null ? "" : value.toString());
                if (selected) {
                    etiqueta.setBackground(VERDE);
                    etiqueta.setForeground(BLANCO);
                }
                return etiqueta;
            }
        };
    }

    private static TableCellRenderer filaRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean selected, boolean focus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(table, value, selected, focus, row, column);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBackground(selected ? SELECCION
                        : (row % 2 == 0 ? BLANCO : FILA_ALTERNA));
                setForeground(TEXTO);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                setOpaque(true);
                return this;
            }
        };
    }
}
