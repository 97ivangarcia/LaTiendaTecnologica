package org.example;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TiendaApp app = new TiendaApp();
            app.setVisible(true);
        });
    }
}
