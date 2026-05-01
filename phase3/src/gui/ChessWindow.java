package gui;

import board.BoardModel;
import board.MoveRecord;
import piece.Piece;
import piece.PieceColor;
import piece.PieceType;
import position.Position;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

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
 * Phase 2 additions (Manish Bishwakarma):
 *   Endgame Notification — King capture shows winner dialog.
 *   Extra Feature 3: Game History Panel with Undo.
 *
 * Phase 3 additions (Manish Bishwakarma):
 *   Move validation — only legal moves (those that don't leave own King in
 *     check) are accepted. Illegal attempts are silently ignored.
 *   Check detection — status bar updates to "⚠ Check!" when the current
 *     player's King is under attack after the opponent moves.
 *   Checkmate detection — after each move, if the opponent has no legal
 *     moves and is in check, a checkmate dialog is shown declaring the winner.
 *   Stalemate detection — if the opponent has no legal moves but is NOT in
 *     check, a draw dialog is shown.
 *
 * @author Gaurav Paneru (base framework),
 *         Manish Bishwakarma (endgame, history/undo, check/checkmate/stalemate)
 */
public class ChessWindow extends JFrame {

    // ── Core components ─────────────────────────────────────────────────────
    private final BoardModel      boardModel;
    private final ChessBoardPanel boardPanel;

    private PieceColor currentTurn;
    private Position   selectedPosition;

    // ── Status bar ──────────────────────────────────────────────────────────
    private JLabel statusBar;

    // ── History & undo state (Manish) ────────────────────────────────────────
    private final Deque<MoveRecord>        history         = new ArrayDeque<>();
    private final List<Piece>              capturedByWhite = new ArrayList<>();
    private final List<Piece>              capturedByBlack = new ArrayList<>();
    private final DefaultListModel<String> historyModel    = new DefaultListModel<>();

    private JLabel  whiteCapturedLabel;
    private JLabel  blackCapturedLabel;
    private JButton undoButton;
    private int     moveNumber = 1;

    // -----------------------------------------------------------------------
    //  Constructor
    // -----------------------------------------------------------------------

    public ChessWindow() {
        super("Chess Game — Phase 3");
        currentTurn      = PieceColor.WHITE;
        selectedPosition = null;

        boardModel = new BoardModel();
        boardPanel = new ChessBoardPanel(boardModel);

        setLayout(new BorderLayout());
        add(boardPanel,          BorderLayout.CENTER);
        add(buildHistoryPanel(), BorderLayout.EAST);
        add(buildStatusBar(),    BorderLayout.SOUTH);

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
                    // Phase 3: validate before executing
                    if (isMoveAllowed(selectedPosition, clicked)) {
                        executeMove(selectedPosition, clicked);
                    }
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

            // Phase 3: validate before executing
            if (isMoveAllowed(from, to)) {
                clearSelection();
                executeMove(from, to);
            }
        });
    }

    // -----------------------------------------------------------------------
    //  Move validation (Phase 3 — Manish Bishwakarma)
    // -----------------------------------------------------------------------

    /**
     * Returns true when moving the piece on {@code from} to {@code to} is
     * a legal chess move — i.e., the piece's movement rules are satisfied
     * AND the move does not leave the moving side's King in check.
     *
     * Delegates legality computation entirely to {@link BoardModel#getLegalMovesFrom}.
     *
     * @param from source square
     * @param to   destination square
     * @return true if the move is fully legal
     * @author Manish Bishwakarma
     */
    private boolean isMoveAllowed(Position from, Position to) {
        List<Position> legal = boardModel.getLegalMovesFrom(from);
        for (Position p : legal) {
            if (p.equals(to)) return true;
        }
        return false;
    }

    // -----------------------------------------------------------------------
    //  Shared move execution
    // -----------------------------------------------------------------------

    /**
     * Applies the move on the model, records it in history, checks for
     * checkmate/stalemate, and switches turns.
     *
     * Check detection (Phase 3, Manish): after switching to the next player,
     * if they are in check the status bar highlights it. If they are in
     * checkmate or stalemate an appropriate dialog is shown.
     */
    private void executeMove(Position from, Position to) {
        Piece moving   = boardModel.getPiece(from);
        Piece captured = boardModel.movePiece(from, to);

        MoveRecord record = new MoveRecord(from, to, moving, captured);
        history.push(record);
        recordCapture(captured);
        addHistoryEntry(record);
        undoButton.setEnabled(true);
        boardPanel.repaint();

        // Switch to the next player
        switchTurn();

        // ── Phase 3: check / checkmate / stalemate (Manish) ─────────────────
        if (boardModel.isCheckmate(currentTurn)) {
            // The player who just moved wins
            PieceColor winner = currentTurn.opposite();
            showCheckmateDialog(winner);
        } else if (boardModel.isStalemate(currentTurn)) {
            showStalemateDialog();
        } else if (boardModel.isInCheck(currentTurn)) {
            // Still playing — just warn the current player they are in check
            updateStatusBar(true);
        }
    }

    // -----------------------------------------------------------------------
    //  Endgame dialogs (Manish)
    // -----------------------------------------------------------------------

    /**
     * Shows a checkmate dialog declaring the winner and offers to start a
     * new game or quit.
     *
     * @param winner the color that delivered checkmate
     * @author Manish Bishwakarma
     */
    private void showCheckmateDialog(PieceColor winner) {
        String name = (winner == PieceColor.WHITE) ? "White" : "Black";
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Checkmate! " + name + " wins!\n\nWould you like to start a new game?",
                "Checkmate — " + name + " Wins!",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) startNewGame();
        else System.exit(0);
    }

    /**
     * Shows a stalemate dialog and offers to start a new game or quit.
     *
     * @author Manish Bishwakarma
     */
    private void showStalemateDialog() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Stalemate! The game is a draw.\n\nWould you like to start a new game?",
                "Stalemate — Draw!",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) startNewGame();
        else System.exit(0);
    }

    // -----------------------------------------------------------------------
    //  New Game / full reset
    // -----------------------------------------------------------------------

    private void startNewGame() {
        boardModel.reset();
        history.clear();
        capturedByWhite.clear();
        capturedByBlack.clear();
        historyModel.clear();
        moveNumber = 1;
        undoButton.setEnabled(false);
        updateCapturedLabels();
        resetTurn();
        boardPanel.repaint();
    }

    // -----------------------------------------------------------------------
    //  Undo (Manish — Extra Feature 3)
    // -----------------------------------------------------------------------

    private void undoLastMove() {
        if (history.isEmpty()) return;

        MoveRecord last = history.pop();
        boardModel.undoMove(last.getFrom(), last.getTo(), last.getCapturedPiece());

        if (!historyModel.isEmpty())
            historyModel.remove(historyModel.size() - 1);

        if (last.getCapturedPiece() != null) {
            if (last.getMovedPiece().getColor() == PieceColor.WHITE)
                capturedByWhite.remove(last.getCapturedPiece());
            else
                capturedByBlack.remove(last.getCapturedPiece());
            updateCapturedLabels();
        }

        if (currentTurn == PieceColor.WHITE && moveNumber > 1) moveNumber--;

        currentTurn = currentTurn.opposite();
        updateStatusBar(false);

        undoButton.setEnabled(!history.isEmpty());
        boardPanel.repaint();
    }

    // -----------------------------------------------------------------------
    //  History panel helpers (Manish)
    // -----------------------------------------------------------------------

    private void addHistoryEntry(MoveRecord record) {
        String prefix;
        if (currentTurn == PieceColor.WHITE) {
            prefix = moveNumber + ". ";
        } else {
            prefix = "   ";
            moveNumber++;
        }
        historyModel.addElement(prefix + record);
    }

    private void recordCapture(Piece captured) {
        if (captured == null) return;
        if (currentTurn == PieceColor.WHITE)
            capturedByWhite.add(captured);
        else
            capturedByBlack.add(captured);
        updateCapturedLabels();
    }

    private void updateCapturedLabels() {
        whiteCapturedLabel.setText("White captured: " + glyphs(capturedByWhite));
        blackCapturedLabel.setText("Black captured: " + glyphs(capturedByBlack));
    }

    private String glyphs(List<Piece> pieces) {
        if (pieces.isEmpty()) return "\u2014";
        StringBuilder sb = new StringBuilder();
        for (Piece p : pieces) sb.append(p.getUnicodeSymbol()).append(' ');
        return sb.toString().trim();
    }

    // -----------------------------------------------------------------------
    //  Build history panel UI (Manish — Extra Feature 3)
    // -----------------------------------------------------------------------

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(new Color(40, 24, 14));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(250, 0));

        JLabel title = new JLabel("Move History");
        title.setForeground(new Color(220, 200, 170));
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setBorder(new EmptyBorder(0, 0, 4, 0));
        panel.add(title, BorderLayout.NORTH);

        JList<String> list = new JList<>(historyModel);
        list.setBackground(new Color(28, 16, 8));
        list.setForeground(new Color(200, 185, 155));
        list.setFont(new Font("Monospaced", Font.PLAIN, 11));
        list.setSelectionBackground(new Color(80, 55, 30));
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(80, 55, 30)));
        historyModel.addListDataListener(new javax.swing.event.ListDataListener() {
            public void intervalAdded(javax.swing.event.ListDataEvent e) {
                list.ensureIndexIsVisible(historyModel.size() - 1);
            }
            public void intervalRemoved(javax.swing.event.ListDataEvent e) {}
            public void contentsChanged(javax.swing.event.ListDataEvent e) {}
        });
        panel.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBackground(new Color(40, 24, 14));
        bottom.setBorder(new EmptyBorder(8, 0, 0, 0));

        whiteCapturedLabel = makeCapturedLabel("White captured: \u2014");
        blackCapturedLabel = makeCapturedLabel("Black captured: \u2014");
        bottom.add(whiteCapturedLabel);
        bottom.add(Box.createVerticalStrut(3));
        bottom.add(blackCapturedLabel);
        bottom.add(Box.createVerticalStrut(12));

        undoButton = new JButton("\u21A9 Undo");
        undoButton.setEnabled(false);
        undoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        undoButton.setMaximumSize(new Dimension(200, 30));
        undoButton.setBackground(new Color(80, 50, 20));
        undoButton.setForeground(new Color(220, 200, 170));
        undoButton.setFocusPainted(false);
        undoButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        undoButton.addActionListener(e -> undoLastMove());
        bottom.add(undoButton);

        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel makeCapturedLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(175, 160, 135));
        lbl.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 12));
        return lbl;
    }

    // -----------------------------------------------------------------------
    //  Turn management — public API for teammates
    // -----------------------------------------------------------------------

    public PieceColor getCurrentTurn() { return currentTurn; }

    public void switchTurn() {
        currentTurn = currentTurn.opposite();
        updateStatusBar(false);
    }

    public void resetTurn() {
        currentTurn = PieceColor.WHITE;
        clearSelection();
        updateStatusBar(false);
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
        updateStatusBar(false);
        return statusBar;
    }

    /**
     * Updates the status bar text.
     *
     * @param inCheck when true, appends a "⚠ Check!" warning in red
     * @author Manish Bishwakarma (check warning added in Phase 3)
     */
    private void updateStatusBar(boolean inCheck) {
        String player = (currentTurn == PieceColor.WHITE) ? "White" : "Black";
        if (inCheck) {
            statusBar.setForeground(new Color(255, 80, 80));
            statusBar.setText("  \u26A0 " + player + " is in Check!");
        } else {
            statusBar.setForeground(new Color(220, 200, 170));
            statusBar.setText("  " + player + "'s turn");
        }
    }

    /** Kept for backward compatibility with teammates who call updateStatusBar(). */
    private void updateStatusBar() { updateStatusBar(false); }

    private void clearSelection() {
        selectedPosition = null;
        boardPanel.setSelectedSquare(null);
    }

    // -----------------------------------------------------------------------
    //  Accessors for teammates
    // -----------------------------------------------------------------------

    public ChessBoardPanel getBoardPanel() { return boardPanel; }
    public BoardModel      getBoardModel() { return boardModel; }
}
