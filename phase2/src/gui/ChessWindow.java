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
 *   Click-to-move: click a friendly piece to select it, then click the
 *                  destination square to move (or capture) there.
 *   Drag-to-move:  press on a friendly piece, drag to the destination, release.
 * Captures: moving onto a square occupied by the opponent removes their piece.
 * Moving onto a square occupied by a friendly piece is not allowed.
 *
 * @author Gaurav Paneru
 */
public class ChessWindow extends JFrame {

    private final BoardModel      boardModel;
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

    private void registerClickListener() {
        boardPanel.addSquareClickListener((row, col) -> {
            Position clicked = new Position(row, col);
            Piece    target  = boardModel.getPiece(clicked);

            if (selectedPosition == null) {
                // Nothing selected — accept only a friendly piece.
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

                } else {
                    // Empty square or enemy piece → execute move (captures included).
                    executeMove(selectedPosition, clicked);
                    clearSelection();
                }
            }
        });
    }

    // -----------------------------------------------------------------------
    //  Drag-to-move
    // -----------------------------------------------------------------------

    private void registerDragListener() {
        boardPanel.addDragDropListener((fromRow, fromCol, toRow, toCol) -> {
            Position from = new Position(fromRow, fromCol);
            Position to   = new Position(toRow,   toCol);

            Piece moving = boardModel.getPiece(from);
            Piece dest   = boardModel.getPiece(to);

            // Must be the active player's piece.
            if (moving == null || moving.getColor() != currentTurn) return;
            // Can't drop on a friendly piece.
            if (dest != null && dest.getColor() == currentTurn)     return;
            // Dropping on itself is a no-op.
            if (from.equals(to))                                     return;

            clearSelection();
            executeMove(from, to);
        });
    }

    // -----------------------------------------------------------------------
    //  Shared move execution
    // -----------------------------------------------------------------------

    /**
     * Applies the move on the model, switches the turn, and repaints.
     * BoardModel.movePiece handles captures automatically — if an enemy piece
     * occupies 'to' it is simply replaced.
     */
    private void executeMove(Position from, Position to) {
        boardModel.movePiece(from, to);
        switchTurn();
        boardPanel.repaint();
    }

    private void clearSelection() {
        selectedPosition = null;
        boardPanel.setSelectedSquare(null);
    }

    // -----------------------------------------------------------------------
    //  Turn management — public API for teammates
    // -----------------------------------------------------------------------

    public PieceColor getCurrentTurn() { return currentTurn; }

    public void switchTurn() {
        currentTurn = currentTurn.opposite();
        updateStatusBar();
    }

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

    public ChessBoardPanel getBoardPanel() { return boardPanel; }
    public BoardModel getBoardModel()      { return boardModel; }
}