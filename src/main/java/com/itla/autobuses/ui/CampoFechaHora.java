package com.itla.autobuses.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CampoFechaHora extends JTextField {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] MESES = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
    private static final String[] DIAS = {"Lu", "Ma", "Mi", "Ju", "Vi", "Sa", "Do"};

    private final boolean conHora;
    private final JPopupMenu popup = new JPopupMenu();
    private final JPanel dias = new JPanel(new GridLayout(0, 7, 4, 4));
    private final JLabel titulo = new JLabel();
    private final JComboBox<Integer> hora = new JComboBox<>();
    private final JComboBox<Integer> minuto = new JComboBox<>();
    private YearMonth mes;
    private LocalDate dia;

    public CampoFechaHora() {
        this(true);
    }

    public CampoFechaHora(boolean conHora) {
        this.conHora = conHora;
        setEditable(false);
        setFocusable(true);
        setFont(Tema.fuente(12));
        setBackground(Color.WHITE);
        setForeground(Tema.TEXTO);
        setDisabledTextColor(Tema.TEXTO);
        setCaretColor(Tema.VERDE);
        setColumns(conHora ? 16 : 10);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE),
                BorderFactory.createEmptyBorder(5, 8, 5, 28)));
        dias.setOpaque(false);

        for (int i = 0; i < 24; i++) {
            hora.addItem(i);
        }
        for (int i = 0; i < 60; i++) {
            minuto.addItem(i);
        }

        LocalDateTime inicial = leer();
        if (inicial == null) {
            inicial = LocalDateTime.now().withSecond(0).withNano(0);
        }
        mes = YearMonth.from(inicial);
        dia = inicial.toLocalDate();
        hora.setSelectedItem(inicial.getHour());
        minuto.setSelectedItem(inicial.getMinute());

        construirPopup();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mostrar();
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    mostrar();
                }
            }
        });
    }

    private void construirPopup() {
        JPanel raiz = new JPanel(new BorderLayout(8, 8));
        raiz.setBackground(Color.WHITE);
        raiz.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel navegacion = new JPanel(new BorderLayout());
        navegacion.setOpaque(false);
        JButton anterior = Tema.botonSecundario("<");
        JButton siguiente = Tema.botonSecundario(">");
        anterior.addActionListener(e -> {
            mes = mes.minusMonths(1);
            pintarMes();
        });
        siguiente.addActionListener(e -> {
            mes = mes.plusMonths(1);
            pintarMes();
        });
        titulo.setFont(Tema.fuente(12, Font.BOLD));
        titulo.setForeground(Tema.VERDE);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        navegacion.add(anterior, BorderLayout.WEST);
        navegacion.add(titulo, BorderLayout.CENTER);
        navegacion.add(siguiente, BorderLayout.EAST);

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pie.setOpaque(false);
        if (conHora) {
            pie.add(new JLabel("Hora"));
            Tema.aplicar(hora);
            pie.add(hora);
            pie.add(new JLabel("Min"));
            Tema.aplicar(minuto);
            pie.add(minuto);
            hora.addActionListener(e -> actualizarTexto());
            minuto.addActionListener(e -> actualizarTexto());
        }
        JButton btnAhora = Tema.botonSecundario("Ahora");
        JButton btnLimpiar = Tema.botonSecundario("Limpiar");
        JButton btnAceptar = Tema.boton("Aceptar");
        btnAhora.addActionListener(e -> {
            LocalDateTime ahora = LocalDateTime.now().withSecond(0).withNano(0);
            mes = YearMonth.from(ahora);
            dia = ahora.toLocalDate();
            hora.setSelectedItem(ahora.getHour());
            minuto.setSelectedItem(ahora.getMinute());
            pintarMes();
            actualizarTexto();
            popup.setVisible(false);
        });
        btnLimpiar.addActionListener(e -> {
            setText("");
            popup.setVisible(false);
        });
        btnAceptar.addActionListener(e -> {
            actualizarTexto();
            popup.setVisible(false);
        });
        pie.add(btnAhora);
        pie.add(btnLimpiar);
        pie.add(btnAceptar);

        raiz.add(navegacion, BorderLayout.NORTH);
        raiz.add(dias, BorderLayout.CENTER);
        raiz.add(pie, BorderLayout.SOUTH);
        popup.setLayout(new BorderLayout());
        popup.add(raiz, BorderLayout.CENTER);
    }

    private void pintarMes() {
        dias.removeAll();
        titulo.setText(MESES[mes.getMonthValue() - 1] + " " + mes.getYear());
        for (String nombre : DIAS) {
            JLabel cabecera = new JLabel(nombre, SwingConstants.CENTER);
            cabecera.setFont(Tema.fuente(10, Font.BOLD));
            cabecera.setForeground(Tema.TEXTO_SECUNDARIO);
            dias.add(cabecera);
        }
        int inicio = mes.atDay(1).getDayOfWeek().getValue() - 1;
        for (int i = 0; i < inicio; i++) {
            dias.add(new JLabel());
        }
        for (int numero = 1; numero <= mes.lengthOfMonth(); numero++) {
            LocalDate fecha = mes.atDay(numero);
            boolean seleccionado = fecha.equals(dia);
            JButton boton = new JButton(String.valueOf(numero));
            boton.setFont(Tema.fuente(11, seleccionado ? Font.BOLD : Font.PLAIN));
            boton.setForeground(seleccionado ? Color.WHITE : Tema.TEXTO);
            boton.setBackground(seleccionado ? Tema.VERDE : Color.WHITE);
            boton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            boton.setFocusPainted(false);
            boton.setOpaque(true);
            boton.addActionListener(e -> {
                dia = fecha;
                pintarMes();
                actualizarTexto();
                popup.setVisible(false);
            });
            dias.add(boton);
        }
        dias.revalidate();
        dias.repaint();
        popup.pack();
    }

    private void actualizarTexto() {
        if (dia == null) {
            return;
        }
        int h = conHora ? (int) hora.getSelectedItem() : 0;
        int m = conHora ? (int) minuto.getSelectedItem() : 0;
        LocalDateTime valor = dia.atTime(h, m);
        setText(conHora ? FECHA_HORA.format(valor) : FECHA.format(valor));
    }

    private void mostrar() {
        LocalDateTime actual = leer();
        if (actual != null) {
            mes = YearMonth.from(actual);
            dia = actual.toLocalDate();
            if (conHora) {
                hora.setSelectedItem(actual.getHour());
                minuto.setSelectedItem(actual.getMinute());
            }
        }
        pintarMes();
        popup.show(this, 0, getHeight());
    }

    private LocalDateTime leer() {
        String valor = getText().trim();
        if (valor.isEmpty()) {
            return null;
        }
        try {
            if (conHora) {
                return LocalDateTime.parse(valor, FECHA_HORA);
            }
            return LocalDate.parse(valor, FECHA).atStartOfDay();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int y = (getHeight() - 14) / 2;
        int x = getWidth() - 24;
        g2.setColor(Tema.VERDE);
        g2.drawRoundRect(x, y, 13, 13, 3, 3);
        g2.drawLine(x, y + 4, x + 13, y + 4);
        g2.drawLine(x + 3, y + 1, x + 3, y + 5);
        g2.drawLine(x + 10, y + 1, x + 10, y + 5);
        if (getText().isEmpty() && !hasFocus()) {
            g2.setColor(Tema.TEXTO_SECUNDARIO);
            g2.setFont(Tema.fuente(11));
            g2.drawString(conHora ? "Clic para elegir fecha y hora" : "Clic para elegir fecha",
                    getInsets().left + 2, (getHeight() + g2.getFontMetrics().getAscent()
                    - g2.getFontMetrics().getDescent()) / 2);
        }
        g2.dispose();
    }
}
