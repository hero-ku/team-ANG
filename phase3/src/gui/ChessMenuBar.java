package gui;

import board.BoardModel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * GUI Feature 1 – Menu Bar with Game Controls.
 *
 * Provides a JMenuBar with:
 *   Game menu  → New Game (Ctrl+N), Save Game (Ctrl+S), Load Game (Ctrl+O), Exit
 *   Settings menu → Customize Board & Pieces (opens SettingsWindow)
 *   Help menu  → How to Play, About
 *
 * The host window must implement {@link MenuCallbacks} so this bar can
 * delegate actions back to it.
 *
 * Save / Load format: plain text serialization of the board grid.
 * Each line: row col colorName typeName  (for occupied squares only)
 * First line: currentTurn (WHITE or BLACK)
 *
 * @author Nischal Rimal
 */
public class ChessMenuBar extends JMenuBar {

    // -----------------------------------------------------------------------
    // Callback interface
    // -----------------------------------------------------------------------

    /**
     * Implemented by the main window ({@link ChessWindow}) so the menu bar
     * can trigger game-level actions.
     */
    public interface MenuCallbacks {
        /** Reset board and turn to initial state. */
        void onNewGame();

        /**
         * Returns a SaveData snapshot of the current game for serialization.
         * Returns null if nothing should be saved (caller aborted).
         */
        SaveData onSaveRequested();

        /** Restore the game from a previously saved snapshot. */
        void onGameLoaded(SaveData data);

        /** Open the settings / customization window. */
        void onOpenSettings();
    }

    // -----------------------------------------------------------------------
    // Simple save-data container (no Java serialization needed)
    // -----------------------------------------------------------------------

    /**
     * Plain-data container exchanged between ChessMenuBar and ChessWindow
     * during save / load operations.
     */
    public static class SaveData {
        /** Whose turn: "WHITE" or "BLACK". */
        public final String currentTurn;
        /**
         * Board cell descriptions.
         * Each entry: "row col COLOR TYPE"
         * e.g. "0 4 WHITE KING"
         */
        public final java.util.List<String> cells;

        public SaveData(String currentTurn, java.util.List<String> cells) {
            this.currentTurn = currentTurn;
            this.cells       = java.util.Collections.unmodifiableList(cells);
        }
    }

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private final JFrame        owner;
    private final MenuCallbacks callbacks;
    private final JFileChooser  fileChooser;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Constructs the menu bar and wires all menu items.
     *
     * @param owner     the JFrame that hosts this bar (used as dialog parent)
     * @param callbacks the ChessWindow that handles each menu action
     */
    public ChessMenuBar(JFrame owner, MenuCallbacks callbacks) {
        this.owner     = owner;
        this.callbacks = callbacks;

        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(
                new FileNameExtensionFilter("Chess save files (*.chess)", "chess"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        add(buildGameMenu());
        add(buildSettingsMenu());
        add(buildHelpMenu());
    }

    // -----------------------------------------------------------------------
    // Menu construction
    // -----------------------------------------------------------------------

    private JMenu buildGameMenu() {
        JMenu game = new JMenu("Game");
        game.setMnemonic(KeyEvent.VK_G);

        // New Game
        JMenuItem newGame = new JMenuItem("New Game");
        newGame.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newGame.addActionListener(e -> handleNewGame());

        // Save Game
        JMenuItem saveGame = new JMenuItem("Save Game");
        saveGame.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveGame.addActionListener(e -> handleSaveGame());

        // Load Game
        JMenuItem loadGame = new JMenuItem("Load Game");
        loadGame.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        loadGame.addActionListener(e -> handleLoadGame());

        // Exit
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(owner,
                    "Exit the game?", "Confirm Exit",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) System.exit(0);
        });

        game.add(newGame);
        game.addSeparator();
        game.add(saveGame);
        game.add(loadGame);
        game.addSeparator();
        game.add(exit);
        return game;
    }

    private JMenu buildSettingsMenu() {
        JMenu settings = new JMenu("Settings");
        settings.setMnemonic(KeyEvent.VK_S);

        JMenuItem customize = new JMenuItem("Customize Board & Pieces…");
        customize.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.CTRL_DOWN_MASK));
        customize.addActionListener(e -> callbacks.onOpenSettings());

        settings.add(customize);
        return settings;
    }

    private JMenu buildHelpMenu() {
        JMenu help = new JMenu("Help");
        help.setMnemonic(KeyEvent.VK_H);

        JMenuItem howTo = new JMenuItem("How to Play");
        howTo.addActionListener(e -> JOptionPane.showMessageDialog(owner,
                "<html><b>How to Play</b><br><br>"
                + "• Click a piece to select it, then click a destination square.<br>"
                + "• Or drag a piece directly to its destination.<br>"
                + "• Capturing: move onto an opponent's piece to capture it.<br>"
                + "• The game ends when a King is captured.<br><br>"
                + "Shortcuts:<br>"
                + "  Ctrl+N  New Game<br>"
                + "  Ctrl+S  Save Game<br>"
                + "  Ctrl+O  Load Game<br>"
                + "  Ctrl+,  Settings</html>",
                "How to Play", JOptionPane.INFORMATION_MESSAGE));

        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> JOptionPane.showMessageDialog(owner,
                "<html><b>Chess – Team ANG</b><br>CS 3354 · Section 255<br><br>"
                + "Andrew Lynch · Gaurav Paneru<br>"
                + "Nischal Rimal · Manish Bishwakarma</html>",
                "About", JOptionPane.INFORMATION_MESSAGE));

        help.add(howTo);
        help.addSeparator();
        help.add(about);
        return help;
    }

    // -----------------------------------------------------------------------
    // Handlers
    // -----------------------------------------------------------------------

    private void handleNewGame() {
        int choice = JOptionPane.showConfirmDialog(owner,
                "Start a new game? The current game will be lost.",
                "New Game", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            callbacks.onNewGame();
        }
    }

    private void handleSaveGame() {
        SaveData data = callbacks.onSaveRequested();
        if (data == null) return;

        fileChooser.setDialogTitle("Save Chess Game");
        if (fileChooser.showSaveDialog(owner) != JFileChooser.APPROVE_OPTION) return;

        File file = fileChooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".chess")) {
            file = new File(file.getAbsolutePath() + ".chess");
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println(data.currentTurn);
            for (String cell : data.cells) {
                pw.println(cell);
            }
            JOptionPane.showMessageDialog(owner,
                    "Game saved to:\n" + file.getName(),
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(owner,
                    "Failed to save:\n" + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleLoadGame() {
        fileChooser.setDialogTitle("Load Chess Game");
        if (fileChooser.showOpenDialog(owner) != JFileChooser.APPROVE_OPTION) return;

        File file = fileChooser.getSelectedFile();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String turn = br.readLine();
            if (turn == null) throw new IOException("Empty save file.");

            java.util.List<String> cells = new java.util.ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) cells.add(line.trim());
            }

            SaveData data = new SaveData(turn.trim(), cells);
            callbacks.onGameLoaded(data);
            JOptionPane.showMessageDialog(owner,
                    "Game loaded from:\n" + file.getName(),
                    "Loaded", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(owner,
                    "Failed to load:\n" + ex.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}