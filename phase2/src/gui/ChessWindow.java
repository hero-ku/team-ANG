package gui;

import board.BoardModel;
import board.MoveRecord;
import pieces.Piece;
import pieces.PieceColor;
import pieces.PieceType;
import position.Position;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Deque;
import java.util.List;
import java.util.ArrayList;
import java.util.ArrayDeque;

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
 * Phase 2 additions (Manish Bishwakarma):
 *   Endgame Notification — capturing the opponent's King immediately shows a
 *     winner dialog (JOptionPane). The player chooses to start a new game or
 *     quit; no further moves are processed after the King is captured.
 *
 *   Extra Feature 3: Game History Panel with Undo
 *     A side panel (EAST) displays:
 *       • Every move in readable notation (e.g. "1. WHITE PAWN  A2 → A4")
 *       • Captured pieces as Unicode glyphs under each player's name
 *       • An Undo button that reverts the last half-move, including restoring
 *         any captured piece to the board
 *
 * @author Gaurav Paneru (base framework),
 *         Manish Bishwakarma (endgame + history panel + undo)
 */
public class ChessWindow extends JFrame {

    // ── Core components ─────────────────────────────────────────────────────
    private final BoardModel      boardModel;
    private final ChessBoardPanel boardPanel;

    /** Whose turn it currently is. WHITE always goes first. */
    private PieceColor currentTurn;

    /** The square chosen by the first click of a click-to-move gesture. */
    private Position selectedPosition;

    // ── Status bar ──────────────────────────────────────────────────────────
    private JLabel statusBar;

    // ── History & undo state (Manish) ────────────────────────────────────────
    /** Stack of every half-move played; top = most recent. */
    private final Deque<MoveRecord>        history         = new ArrayDeque<>();
    /** Pieces captured by White, in order. */
    private final List<Piece>              capturedByWhite = new ArrayList<>();
    /** Pieces captured by Black, in order. */
    private final List<Piece>              capturedByBlack = new ArrayList<>();
    /** Backing model for the JList in the history panel. */
    private final DefaultListModel<String> historyModel    = new DefaultListModel<>();

    /** Labels updated in real time to show captured glyphs. */
    private JLabel  whiteCapturedLabel;
    private JLabel  blackCapturedLabel;
    /** Disabled until the first move is made; re-disabled when history empties. */
    private JButton undoButton;

    /** Incremented after Black moves, to prefix "1. White …" / "   Black …". */
    private int moveNumber = 1;

    // -----------------------------------------------------------------------
    //  Constructor
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
                    // Empty square or enemy piece → execute move.
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
            if (dest != null && dest.getColor()   == currentTurn) return;
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
     * Applies the move on the model, records it in the history stack, updates
     * the history panel, checks for King capture (endgame), and switches turns.
     *
     * Endgame logic (Manish): if the captured piece is a King, the game ends
     * immediately — declareWinner() shows a dialog and no turn switch occurs.
     */
    private void executeMove(Position from, Position to) {
        Piece moving   = boardModel.getPiece(from);
        Piece captured = boardModel.movePiece(from, to);

        // Record the move before any endgame check so history is accurate.
        MoveRecord record = new MoveRecord(from, to, moving, captured);
        history.push(record);
        recordCapture(captured);
        addHistoryEntry(record);
        undoButton.setEnabled(true);
        boardPanel.repaint();

        // ── Endgame check (Manish) ───────────────────────────────────────────
        // If the captured piece is a King, the active player wins immediately.
        if (captured != null && captured.getType() == PieceType.KING) {
            declareWinner(currentTurn);
            return; // Do NOT switch turn; the game is over.
        }

        switchTurn();
    }

    // -----------------------------------------------------------------------
    //  Endgame notification (Manish)
    // -----------------------------------------------------------------------

    /**
     * Displays a JOptionPane declaring the winner after a King capture.
     * The player chooses "New Game" (YES) or "Quit" (NO).
     *
     * @param winner the color that captured the King
     */
    private void declareWinner(PieceColor winner) {
        String name = (winner == PieceColor.WHITE) ? "White" : "Black";
        int choice = JOptionPane.showConfirmDialog(
                this,
                name + " wins by capturing the King!\n\nWould you like to start a new game?",
                "Game Over — " + name + " Wins!",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            startNewGame();
        } else {
            System.exit(0);
        }
    }

    // -----------------------------------------------------------------------
    //  New Game / full reset
    // -----------------------------------------------------------------------

    /**
     * Resets the board model, clears all history and captured-piece lists,
     * and restarts from White's first move.
     */
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

        switchTurn();
    }

    // -----------------------------------------------------------------------
    //  Undo (Manish — Extra Feature 3)
    // -----------------------------------------------------------------------

    /**
     * Reverts the most recent half-move.
     * Restores the moved piece to its origin square and replaces any captured
     * piece on the destination square. The history panel and captured-piece
     * labels are updated accordingly.
     */
    private void undoLastMove() {
        if (history.isEmpty()) return;

        MoveRecord last = history.pop();

        // Reverse the board state via the new BoardModel.undoMove().
        boardModel.undoMove(last.getFrom(), last.getTo(), last.getCapturedPiece());

        // Remove the last entry from the displayed list.
        if (!historyModel.isEmpty())
            historyModel.remove(historyModel.size() - 1);

        // Un-record the capture if there was one.
        if (last.getCapturedPiece() != null) {
            if (last.getMovedPiece().getColor() == PieceColor.WHITE)
                capturedByWhite.remove(last.getCapturedPiece());
            else
                capturedByBlack.remove(last.getCapturedPiece());
            updateCapturedLabels();
        }

        // Rewind the move counter when undoing White's move (a full round).
        if (currentTurn == PieceColor.WHITE && moveNumber > 1)
            moveNumber--;

        // Switch turn back to whoever just moved.
        currentTurn = currentTurn.opposite();
        updateStatusBar();

        undoButton.setEnabled(!history.isEmpty());
        boardPanel.repaint();
    }

    // -----------------------------------------------------------------------
    //  History panel helpers (Manish)
    // -----------------------------------------------------------------------

    /**
     * Formats a move record and appends it to the history JList.
     * White moves are prefixed with the move number ("1. "); Black moves are
     * indented so pairs line up visually.
     */
    private void addHistoryEntry(MoveRecord record) {
        String prefix;
        if (currentTurn == PieceColor.WHITE) {
            prefix = moveNumber + ". ";
        } else {
            prefix = "   ";     // Black's reply is indented under White's
            moveNumber++;        // Increment AFTER Black plays (full round done)
        }
        historyModel.addElement(prefix + record);
    }

    /**
     * Adds a captured piece to the appropriate player's list and refreshes
     * the Unicode-glyph labels in the history panel.
     */
    private void recordCapture(Piece captured) {
        if (captured == null) return;
        if (currentTurn == PieceColor.WHITE)
            capturedByWhite.add(captured);
        else
            capturedByBlack.add(captured);
        updateCapturedLabels();
    }

    /** Rebuilds both captured-piece label texts from the current lists. */
    private void updateCapturedLabels() {
        whiteCapturedLabel.setText("White captured: " + glyphs(capturedByWhite));
        blackCapturedLabel.setText("Black captured: " + glyphs(capturedByBlack));
    }

    /** Converts a list of pieces into a space-separated string of Unicode glyphs. */
    private String glyphs(List<Piece> pieces) {
        if (pieces.isEmpty()) return "\u2014";   // em-dash when nothing captured
        StringBuilder sb = new StringBuilder();
        for (Piece p : pieces) sb.append(p.getUnicodeSymbol()).append(' ');
        return sb.toString().trim();
    }

    // -----------------------------------------------------------------------
    //  Build history panel UI (Manish — Extra Feature 3) this with extra
    // -----------------------------------------------------------------------

    /**
     * Constructs the EAST side panel containing:
     *   • A scrollable move-history list
     *   • Captured-pieces labels for each player
     *   • An Undo button
     */
    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(new Color(40, 24, 14));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(250, 0));

        // Title
        JLabel title = new JLabel("Move History");
        title.setForeground(new Color(220, 200, 170));
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setBorder(new EmptyBorder(0, 0, 4, 0));
        panel.add(title, BorderLayout.NORTH);

        // Scrollable move list
        JList<String> list = new JList<>(historyModel);
        list.setBackground(new Color(28, 16, 8));
        list.setForeground(new Color(200, 185, 155));
        list.setFont(new Font("Monospaced", Font.PLAIN, 11));
        list.setSelectionBackground(new Color(80, 55, 30));
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(80, 55, 30)));
        // Auto-scroll to the latest move.
        historyModel.addListDataListener(new javax.swing.event.ListDataListener() {
            public void intervalAdded(javax.swing.event.ListDataEvent e) {
                int last = historyModel.size() - 1;
                list.ensureIndexIsVisible(last);
            }
            public void intervalRemoved(javax.swing.event.ListDataEvent e) {}
            public void contentsChanged(javax.swing.event.ListDataEvent e) {}
        });
        panel.add(scroll, BorderLayout.CENTER);

        // Bottom section: captured pieces + undo button
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

    /** Creates a styled label for the captured-pieces display. */
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

    private void clearSelection() {
        selectedPosition = null;
        boardPanel.setSelectedSquare(null);
    }

    public ChessBoardPanel getBoardPanel() { return boardPanel; }
    public BoardModel      getBoardModel() { return boardModel; }
}