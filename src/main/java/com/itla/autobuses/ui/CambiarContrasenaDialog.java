package com.itla.autobuses.ui;

import com.itla.autobuses.service.UsuarioService;
import com.itla.autobuses.util.NegocioException;
import com.itla.autobuses.util.Sesion;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;

public class CambiarContrasenaDialog {

    private final JDialog dialogo;
    private final javax.swing.JPasswordField txtActual = Tema.clave("Contraseña actual");
    private final javax.swing.JPasswordField txtNueva = Tema.clave("Minimo 8 caracteres");
    private final javax.swing.JPasswordField txtRepetir = Tema.clave("Repite la contraseña");
    private final JCheckBox chkMostrar = new JCheckBox("Mostrar contraseñas");
    private final UsuarioService servicio = new UsuarioService();

    public CambiarContrasenaDialog(Frame padre) {
        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBackground(Color.WHITE);
        form.add(etiqueta("Contraseña actual"));
        form.add(txtActual);
        form.add(etiqueta("Nueva contraseña"));
        form.add(txtNueva);
        form.add(etiqueta("Repite la nueva"));
        form.add(txtRepetir);
        form.add(new JLabel());
        form.add(chkMostrar);

        chkMostrar.setFont(Tema.fuente(11));
        chkMostrar.setForeground(Tema.TEXTO_SECUNDARIO);
        chkMostrar.setOpaque(false);
        chkMostrar.addActionListener(e -> {
            char eco = chkMostrar.isSelected() ? (char) 0 : '\u2022';
            txtActual.setEchoChar(eco);
            txtNueva.setEchoChar(eco);
            txtRepetir.setEchoChar(eco);
        });

        JDialog[] referencia = new JDialog[1];
        JButton btnCancelar = Tema.botonSecundario("Cancelar");
        btnCancelar.addActionListener(e -> referencia[0].dispose());
        JButton btnGuardar = Tema.boton("Guardar", Iconos.de("check", Color.WHITE));
        btnGuardar.addActionListener(e -> guardar());

        String usuario = Sesion.getInstancia().hayUsuario()
                ? Sesion.getInstancia().getUsuario().getUsername() : "";
        dialogo = Tema.dialogo(padre, "Cambiar contraseña",
                "Actualiza la contraseña de " + usuario + ".", form,
                btnCancelar, btnGuardar);
        referencia[0] = dialogo;
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.fuente(11, Font.BOLD));
        l.setForeground(Tema.TEXTO);
        return l;
    }

    public void setVisible(boolean visible) {
        dialogo.setVisible(visible);
    }

    private void guardar() {
        String actual = new String(txtActual.getPassword());
        String nueva = new String(txtNueva.getPassword());
        String repetir = new String(txtRepetir.getPassword());
        if (!nueva.equals(repetir)) {
            JOptionPane.showMessageDialog(dialogo, "Las contraseñas nuevas no coinciden.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            servicio.cambiarPasswordPropio(actual, nueva);
            JOptionPane.showMessageDialog(dialogo, "Contraseña cambiada correctamente.");
            dialogo.dispose();
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(dialogo, ex.getMessage(),
                    "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }
}
