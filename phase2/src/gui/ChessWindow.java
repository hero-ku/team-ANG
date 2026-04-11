package gui;

import board.BoardModel;
import pieces.PieceColor;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window. Extends JFrame following the professor's
 * "Using Inheritance to Customize Frames" pattern.
 * Teammates access the board via getBoardPanel() and getBoardModel().
 *
 * Turn management:
 *   getCurrentTurn()  — returns whose turn it is (WHITE or BLACK)
 *   switchTurn()      — advances play to the other player
 *   resetTurn()       — resets back to WHITE (call alongside BoardModel.reset())
 *
 * @author Gaurav Paneru
 */
public class ChessWindow extends JFrame {

    private final BoardModel boardModel;
    private final ChessBoardPanel boardPanel;

    /** Whose turn it currently is. WHITE always goes first. */
    private PieceColor currentTurn;

    /** Persistent reference so switchTurn() can update the label. */
    private JLabel statusBar;

    public ChessWindow() {
        super("Chess Game — Phase 2");
        currentTurn = PieceColor.WHITE;

        boardModel = new BoardModel();
        boardPanel = new ChessBoardPanel(boardModel);

        setLayout(new BorderLayout());
        add(boardPanel, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);   // stores ref in this.statusBar

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    // -----------------------------------------------------------------------
    //  Turn management — public API for teammates
    // -----------------------------------------------------------------------

    /**
     * Returns whose turn it currently is.
     * Teammate 2 calls this before executing a move to validate piece color.
     */
    public PieceColor getCurrentTurn() {
        return currentTurn;
    }

    /**
     * Flips the active player (WHITE → BLACK or BLACK → WHITE)
     * and refreshes the status bar.
     * Teammate 2 calls this after every successful move.
     */
    public void switchTurn() {
        currentTurn = currentTurn.opposite();
        updateStatusBar();
    }

    /**
     * Resets the active player back to WHITE and refreshes the status bar.
     * Teammate 3 calls this alongside BoardModel.reset() when starting a new game.
     */
    public void resetTurn() {
        currentTurn = PieceColor.WHITE;
        updateStatusBar();
    }

    // -----------------------------------------------------------------------
    //  Internal helpers
    // -----------------------------------------------------------------------

    /** Builds and stores the status bar; must be called once during construction. */
    private JLabel buildStatusBar() {
        statusBar = new JLabel(String.valueOf(SwingConstants.LEFT));
        statusBar.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusBar.setOpaque(true);
        statusBar.setBackground(new Color(40, 24, 14));
        statusBar.setForeground(new Color(220, 200, 170));
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        updateStatusBar();   // set the initial text via the shared helper
        return statusBar;
    }

    /** Synchronises the status bar label with currentTurn. */
    private void updateStatusBar() {
        String player = (currentTurn == PieceColor.WHITE) ? "White" : "Black";
        statusBar.setText("  " + player + "'s turn");
    }

    // -----------------------------------------------------------------------
    //  Accessors for teammates
    // -----------------------------------------------------------------------

    /** Returns the board panel (teammate 2 registers click listener here). */
    public ChessBoardPanel getBoardPanel() { return boardPanel; }

    /** Returns the board model (teammate 2 calls movePiece here). */
    public BoardModel getBoardModel() { return boardModel; }
}