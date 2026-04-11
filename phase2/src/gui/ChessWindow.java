package gui;

import board.BoardModel;
import pieces.Piece;
import pieces.PieceColor;
import position.Position;

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
 * Movement (Phase 2 — no legality checking, no captures):
 *   Click a friendly piece to select it, then click any empty square to move.
 *   Clicking a different friendly piece re-selects. Clicking an occupied square
 *   does nothing. Selection is cleared after every successful move.
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

    /**
     * The square the player has selected (first click).
     * Null means no piece is currently selected.
     */
    private Position selectedPosition;

    public ChessWindow() {
        super("Chess Game — Phase 2");
        currentTurn      = PieceColor.WHITE;
        selectedPosition = null;

        boardModel = new BoardModel();
        boardPanel = new ChessBoardPanel(boardModel);

        setLayout(new BorderLayout());
        add(boardPanel, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        registerClickListener();

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    // -----------------------------------------------------------------------
    //  Movement logic
    // -----------------------------------------------------------------------

    /**
     * Wires up the two-click move flow to ChessBoardPanel.
     * First click  → selects a friendly piece.
     * Second click → moves it to an empty square, then switches turns.
     */
    private void registerClickListener() {
        boardPanel.addSquareClickListener((row, col) -> {
            Position clicked = new Position(row, col);
            Piece    target  = boardModel.getPiece(clicked);

            if (selectedPosition == null) {
                // ── Phase 1: nothing selected yet ──────────────────────────
                // Only accept a click on a piece that belongs to the active player.
                if (target != null && target.getColor() == currentTurn) {
                    selectedPosition = clicked;
                    boardPanel.setSelectedSquare(clicked);
                }
                // Clicking an empty square or an enemy piece does nothing.

            } else {
                // ── Phase 2: a piece is already selected ───────────────────
                if (clicked.equals(selectedPosition)) {
                    // Clicked the same square again → deselect.
                    clearSelection();

                } else if (target != null && target.getColor() == currentTurn) {
                    // Clicked a different friendly piece → re-select it.
                    selectedPosition = clicked;
                    boardPanel.setSelectedSquare(clicked);

                } else if (target != null) {
                    // Clicked an occupied square (enemy piece) → not allowed yet,
                    // just deselect so the player can try again.
                    clearSelection();

                } else {
                    // Clicked an empty square → execute the move.
                    boardModel.movePiece(selectedPosition, clicked);
                    clearSelection();
                    switchTurn();
                    boardPanel.repaint();
                }
            }
        });
    }

    /** Removes the current selection from both this class and the board panel. */
    private void clearSelection() {
        selectedPosition = null;
        boardPanel.setSelectedSquare(null);
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
        clearSelection();
        updateStatusBar();
    }

    // -----------------------------------------------------------------------
    //  Internal helpers
    // -----------------------------------------------------------------------

    /** Builds and stores the status bar; must be called once during construction. */
    private JLabel buildStatusBar() {
        statusBar = new JLabel("");
        statusBar.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusBar.setOpaque(true);
        statusBar.setBackground(new Color(40, 24, 14));
        statusBar.setForeground(new Color(220, 200, 170));
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        updateStatusBar();
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