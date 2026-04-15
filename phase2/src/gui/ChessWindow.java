package gui;

import board.BoardModel;
import pieces.Piece;
import pieces.PieceColor;
import pieces.PieceType;
import position.Position;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application window. Extends JFrame following the professor's
 * "Using Inheritance to Customize Frames" pattern.
 * Teammates access the board via getBoardPanel() and getBoardModel().
 *
 * Feature 1 (Menu Bar) and Feature 2 (Settings) are wired here via
 * {@link ChessMenuBar.MenuCallbacks}.
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
public class ChessWindow extends JFrame implements ChessMenuBar.MenuCallbacks {

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

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public ChessWindow() {
        super("Chess Game — Phase 2");
        currentTurn      = PieceColor.WHITE;
        selectedPosition = null;

        boardModel = new BoardModel();
        boardPanel = new ChessBoardPanel(boardModel);

        // ── Menu bar (Feature 1) ──────────────────────────────────────────
        setJMenuBar(new ChessMenuBar(this, this));

        // ── Layout ───────────────────────────────────────────────────────
        setLayout(new BorderLayout());
        add(boardPanel,       BorderLayout.CENTER);
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
                if (target != null && target.getColor() == currentTurn) {
                    selectedPosition = clicked;
                    boardPanel.setSelectedSquare(clicked);
                }

            } else {
                if (clicked.equals(selectedPosition)) {
                    clearSelection();

                } else if (target != null && target.getColor() == currentTurn) {
                    selectedPosition = clicked;
                    boardPanel.setSelectedSquare(clicked);

                } else {
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

            if (moving == null || moving.getColor() != currentTurn) return;
            if (dest   != null && dest.getColor()   == currentTurn) return;
            if (from.equals(to))                                     return;

            clearSelection();
            executeMove(from, to);
        });
    }

    // -----------------------------------------------------------------------
    //  Shared move execution
    // -----------------------------------------------------------------------

    /**
     * Applies the move on the model, checks for King capture,
     * switches the turn, and repaints.
     */
    private void executeMove(Position from, Position to) {
        Piece captured = boardModel.movePiece(from, to);
        boardPanel.repaint();

        // Endgame: if the captured piece is a King, declare the winner
        if (captured != null && captured.getType() == PieceType.KING) {
            String winner = (currentTurn == PieceColor.WHITE) ? "White" : "Black";
            JOptionPane.showMessageDialog(this,
                    winner + " wins by capturing the King!",
                    "Game Over", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }

        switchTurn();
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
    //  ChessMenuBar.MenuCallbacks — Feature 1
    // -----------------------------------------------------------------------

    /**
     * Resets the board and turn counter to the initial state.
     * Called when the user chooses Game → New Game.
     */
    @Override
    public void onNewGame() {
        boardModel.reset();
        resetTurn();
        boardPanel.repaint();
    }

    /**
     * Builds a SaveData snapshot of the current board state.
     * Called by ChessMenuBar when the user chooses Game → Save Game.
     */
    @Override
    public ChessMenuBar.SaveData onSaveRequested() {
        List<String> cells = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece p = boardModel.getPiece(new Position(row, col));
                if (p != null) {
                    // Format: "row col COLOR TYPE"
                    cells.add(row + " " + col + " "
                            + p.getColor().name() + " "
                            + p.getType().name());
                }
            }
        }
        return new ChessMenuBar.SaveData(currentTurn.name(), cells);
    }

    /**
     * Restores the board from a previously saved SaveData.
     * Called by ChessMenuBar after a successful file load.
     */
    @Override
    public void onGameLoaded(ChessMenuBar.SaveData data) {
        // Clear the grid manually by resetting then overwriting
        boardModel.reset();

        // Wipe all pieces (reset() sets standard positions; we need a blank board)
        // We achieve this by calling reset() and then overwriting every cell.
        // BoardModel.reset() re-places pieces, so we clear them first:
        clearBoardGrid();

        // Parse and place pieces from the save file
        for (String cell : data.cells) {
            String[] parts = cell.split(" ");
            if (parts.length != 4) continue;
            try {
                int        row   = Integer.parseInt(parts[0]);
                int        col   = Integer.parseInt(parts[1]);
                PieceColor color = PieceColor.valueOf(parts[2]);
                PieceType  type  = PieceType.valueOf(parts[3]);
                Position   pos   = new Position(row, col);
                boardModel.placePiece(new Piece(type, color, pos), pos);
            } catch (Exception ignored) { }
        }

        // Restore whose turn it is
        try {
            currentTurn = PieceColor.valueOf(data.currentTurn);
        } catch (Exception ignored) {
            currentTurn = PieceColor.WHITE;
        }

        clearSelection();
        updateStatusBar();
        boardPanel.repaint();
    }

    /**
     * Opens the Settings window (Feature 2).
     * Called by ChessMenuBar when the user chooses Settings → Customize.
     */
    @Override
    public void onOpenSettings() {
        new SettingsWindow(this, boardPanel);
    }

    // -----------------------------------------------------------------------
    //  Internal helpers
    // -----------------------------------------------------------------------

    /**
     * Clears every cell of the board without going through reset().
     * Uses BoardModel.movePiece in a way that simply removes everything
     * by placing null — we do this via a dedicated helper on BoardModel.
     */
    private void clearBoardGrid() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                boardModel.clearCell(new Position(row, col));
            }
        }
    }

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
    public BoardModel      getBoardModel() { return boardModel; }
}