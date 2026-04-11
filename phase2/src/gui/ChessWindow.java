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
 * Movement — two input methods, same validation rules:
 *   Click-to-move: click a friendly piece to select it, then click an empty
 *                  square to move it there.
 *   Drag-to-move:  press on a friendly piece, drag to an empty square, release.
 * In both cases the turn switches automatically after a successful move.
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
     * The square chosen by the first click of a click-to-move gesture.
     * Null when no piece is selected via clicking.
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
        registerDragListener();

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    // -----------------------------------------------------------------------
    //  Click-to-move
    // -----------------------------------------------------------------------

    /**
     * Two-click move flow.
     * First click  → select a friendly piece.
     * Second click → move it to an empty square and switch turns.
     */
    private void registerClickListener() {
        boardPanel.addSquareClickListener((row, col) -> {
            Position clicked = new Position(row, col);
            Piece    target  = boardModel.getPiece(clicked);

            if (selectedPosition == null) {
                // Nothing selected yet — accept only a friendly piece.
                if (target != null && target.getColor() == currentTurn) {
                    selectedPosition = clicked;
                    boardPanel.setSelectedSquare(clicked);
                }

            } else {
                if (clicked.equals(selectedPosition)) {
                    // Same square clicked again → deselect.
                    clearSelection();

                } else if (target != null && target.getColor() == currentTurn) {
                    // Different friendly piece → re-select it.
                    selectedPosition = clicked;
                    boardPanel.setSelectedSquare(clicked);

                } else if (target != null) {
                    // Occupied by an enemy → not allowed yet, deselect.
                    clearSelection();

                } else {
                    // Empty square → execute move.
                    executeMove(selectedPosition, clicked);
                    clearSelection();
                }
            }
        });
    }

    // -----------------------------------------------------------------------
    //  Drag-to-move
    // -----------------------------------------------------------------------

    /**
     * Single-gesture move flow.
     * Press on a friendly piece, drag to an empty square, release to move.
     * If the destination is invalid the move is silently cancelled.
     */
    private void registerDragListener() {
        boardPanel.addDragDropListener((fromRow, fromCol, toRow, toCal) -> {
            Position from = new Position(fromRow, fromCol);
            Position to   = new Position(toRow,   toCal);

            Piece moving = boardModel.getPiece(from);

            // Validate: must be the active player's piece dropped onto an empty square.
            if (moving == null)                           return;
            if (moving.getColor() != currentTurn)         return;
            if (from.equals(to))                          return;   // dropped on itself
            if (!boardModel.isEmpty(to))                  return;   // occupied

            // A drag always clears any pending click-selection first.
            clearSelection();
            executeMove(from, to);
        });
    }

    // -----------------------------------------------------------------------
    //  Shared move execution
    // -----------------------------------------------------------------------

    /**
     * Applies the move on the model, switches the turn, and repaints.
     * Called by both the click and drag handlers once their own validation
     * has passed.
     */
    private void executeMove(Position from, Position to) {
        boardModel.movePiece(from, to);
        switchTurn();
        boardPanel.repaint();
    }

    /** Clears the click-selection state in both this class and the board panel. */
    private void clearSelection() {
        selectedPosition = null;
        boardPanel.setSelectedSquare(null);
    }

    // -----------------------------------------------------------------------
    //  Turn management — public API for teammates
    // -----------------------------------------------------------------------

    /** Returns whose turn it currently is. */
    public PieceColor getCurrentTurn() {
        return currentTurn;
    }

    /**
     * Flips the active player and refreshes the status bar.
     * Called automatically after every successful move.
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

    private JLabel buildStatusBar() {
        statusBar = new JLabel("", SwingConstants.LEFT);
        statusBar.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusBar.setOpaque(true);
        statusBar.setBackground(new Color(40, 24, 14));
        statusBar.setForeground(new Color(220, 200, 170));
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        updateStatusBar();
        return statusBar;
    }

    private void updateStatusBar() {
        String player = (currentTurn == PieceColor.WHITE) ? "White" : "Black";
        statusBar.setText("  " + player + "'s turn");
    }

    // -----------------------------------------------------------------------
    //  Accessors for teammates
    // -----------------------------------------------------------------------

    /** Returns the board panel (teammate 2 registers listeners here). */
    public ChessBoardPanel getBoardPanel() { return boardPanel; }

    /** Returns the board model (teammate 2 calls movePiece here). */
    public BoardModel getBoardModel() { return boardModel; }
}