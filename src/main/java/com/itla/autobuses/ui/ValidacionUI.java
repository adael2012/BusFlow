package com.itla.autobuses.ui;

import com.itla.autobuses.util.NegocioException;

import javax.swing.JTextField;
import java.math.BigDecimal;

public final class ValidacionUI {

    private ValidacionUI() { }

    public static int entero(JTextField campo, String articulo, String nombre) throws NegocioException {
        String valor = campo.getText().trim();
        if (valor.isEmpty()) {
            throw new NegocioException("Indica " + articulo + " " + nombre + ".");
        }
        if (!valor.matches("\\d+")) {
            throw new NegocioException(mayuscula(nombre) + " debe ser un numero entero.");
        }
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new NegocioException(mayuscula(nombre) + " es demasiado grande.");
        }
    }

    public static BigDecimal decimal(JTextField campo, String articulo, String nombre) throws NegocioException {
        String valor = campo.getText().trim().replace(',', '.');
        if (valor.isEmpty()) {
            throw new NegocioException("Indica " + articulo + " " + nombre + ".");
        }
        if (!valor.matches("\\d+(\\.\\d{1,2})?")) {
            throw new NegocioException(mayuscula(nombre) + " debe ser un numero decimal.");
        }
        try {
            return new BigDecimal(valor);
        } catch (NumberFormatException e) {
            throw new NegocioException(mayuscula(nombre) + " no es un numero valido.");
        }
    }

    private static String mayuscula(String texto) {
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }
}
