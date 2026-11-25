package com.itla.autobuses;

import com.itla.autobuses.service.FabricaServicios;
import com.itla.autobuses.ui.LoginFrame;
import com.itla.autobuses.ui.Tema;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        configurarInterfaz();
        SwingUtilities.invokeLater(() -> {
            boolean demo = FabricaServicios.inicializar(args);
            if (demo) {
                JOptionPane.showMessageDialog(null,
                        FabricaServicios.getMotivoDemo()
                                + "\n\nPuedes entrar con admin / Admin123! o usuario / Usuario123!."
                                + "\nLos cambios en modo demostración se guardan solo mientras el programa está abierto.",
                        "Modo demostración", JOptionPane.INFORMATION_MESSAGE);
            }
            new LoginFrame().setVisible(true);
        });
    }

    public static void configurarInterfaz() {
        FlatLightLaf.setup();
        UIManager.put("defaultFont", Tema.fuente(12));
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("Component.focusWidth", 1);
    }
}
