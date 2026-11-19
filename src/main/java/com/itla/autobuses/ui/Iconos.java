package com.itla.autobuses.ui;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

public final class Iconos {

    private Iconos() { }

    public static Icon de(String nombre, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(1.4f));
                g2.setColor(color);
                g2.translate(x, y);
                pintar(g2, nombre);
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return 16; }

            @Override
            public int getIconHeight() { return 16; }
        };
    }

    private static void pintar(Graphics2D g, String nombre) {
        switch (nombre) {
            case "inicio" -> {
                Polygon techo = new Polygon(new int[]{1, 8, 15}, new int[]{7, 1, 7}, 3);
                g.draw(techo);
                g.drawRect(3, 6, 10, 9);
                g.drawRect(6, 10, 4, 5);
            }
            case "conductores" -> {
                g.fillOval(5, 1, 6, 6);
                g.drawRoundRect(2, 8, 12, 7, 5, 5);
            }
            case "autobuses" -> {
                g.drawRoundRect(1, 2, 14, 10, 3, 3);
                g.drawLine(2, 6, 14, 6);
                g.fillOval(3, 11, 3, 3);
                g.fillOval(10, 11, 3, 3);
            }
            case "rutas" -> {
                g.fillOval(2, 11, 4, 4);
                g.fillOval(10, 1, 4, 4);
                g.drawLine(5, 12, 10, 5);
                g.drawLine(6, 4, 10, 4);
            }
            case "asignaciones" -> {
                g.drawRoundRect(1, 3, 14, 12, 2, 2);
                g.drawLine(1, 7, 15, 7);
                g.drawLine(4, 1, 4, 5);
                g.drawLine(12, 1, 12, 5);
                g.drawLine(4, 10, 8, 10);
            }
            case "disponibilidad" -> {
                g.drawOval(1, 1, 14, 14);
                g.drawLine(8, 8, 8, 4);
                g.drawLine(8, 8, 11, 10);
            }
            case "usuarios" -> {
                g.drawRoundRect(2, 2, 12, 12, 3, 3);
                g.fillOval(6, 4, 4, 4);
                g.drawArc(4, 9, 8, 6, 180, 180);
            }
            case "bitacora" -> {
                g.drawRoundRect(3, 1, 10, 14, 2, 2);
                g.drawLine(5, 5, 11, 5);
                g.drawLine(5, 8, 11, 8);
                g.drawLine(5, 11, 9, 11);
            }
            case "buscar" -> {
                g.drawOval(2, 1, 10, 10);
                g.drawLine(10, 10, 15, 15);
            }
            case "nuevo" -> {
                g.drawOval(1, 1, 14, 14);
                g.drawLine(8, 4, 8, 12);
                g.drawLine(4, 8, 12, 8);
            }
            case "alerta" -> {
                Polygon triangulo = new Polygon(new int[]{8, 1, 15}, new int[]{1, 15, 15}, 3);
                g.draw(triangulo);
                g.drawLine(8, 6, 8, 10);
                g.fillOval(7, 12, 2, 2);
            }
            case "check" -> {
                g.drawOval(1, 1, 14, 14);
                g.drawLine(4, 8, 7, 11);
                g.drawLine(7, 11, 12, 5);
            }
            case "llave" -> {
                g.drawOval(1, 2, 7, 7);
                g.drawLine(6, 7, 15, 14);
                g.drawLine(11, 10, 13, 8);
            }
            case "salir" -> {
                g.drawLine(7, 1, 2, 1);
                g.drawLine(2, 1, 2, 15);
                g.drawLine(2, 15, 7, 15);
                g.drawLine(6, 8, 15, 8);
                g.drawLine(12, 5, 15, 8);
                g.drawLine(15, 8, 12, 11);
            }
            default -> g.fillOval(5, 5, 6, 6);
        }
    }
}
