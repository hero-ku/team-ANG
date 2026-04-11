package gui;

import javax.swing.SwingUtilities;

/**
 * Entry point for Phase 2 GUI.
 * Separate from Phase 1's Main.java.
 *
 * @author Gaurav Paneru
 */
public class MainGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChessWindow window = new ChessWindow();
            window.setVisible(true);
        });
    }
}