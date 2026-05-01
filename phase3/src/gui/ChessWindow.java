package gui;

import board.BoardModel;
import board.MoveRecord;
import piece.Piece;
import piece.PieceColor;
import piece.PieceFactory;
import piece.PieceType;
import position.Position;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
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
 * Save / Load:
 *   Move history is persisted alongside board state. onSaveRequested() appends
 *   each MoveRecord as a "MOVE …" line after the piece-position lines.
 *   onGameLoaded() separates the two kinds of lines, restores the board, then
 *   replays the move lines to fully reconstruct the history panel, captured-
 *   piece labels, move counter, and undo stack.
 *
 * @author Gaurav Paneru (base framework),
 *         Manish Bishwakarma (endgame + history panel + undo)
 */
public class ChessWindow extends JFrame implements ChessMenuBar.MenuCallbacks {

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
     * Returns true if the king of the given color is currently attacked
     * by any enemy piece on the board.
     * Iterates every square; for each enemy piece it calls isValid() toward
     * the king's position. If any enemy can legally reach the king, the
     * king is in check.
     */
    private boolean isKingInCheck(PieceColor color) {
        // Find the king's current position
        Position kingPos = null;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = boardModel.getPiece(new Position(r, c));
                if (p != null && p.getType() == PieceType.KING && p.getColor() == color) {
                    kingPos = new Position(r, c);
                    break;
                }
            }
            if (kingPos != null) break;
        }
        if (kingPos == null) return false; // King already captured (endgame path)

        // Check if any enemy piece can reach the king
        PieceColor enemy = (color == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = boardModel.getPiece(new Position(r, c));
                if (p != null && p.getColor() == enemy) {
                    if (p.isValid(boardModel, kingPos)) return true;
                }
            }
        }
        return false;
    }

    /**
     * Applies the move on the model, switches the turn, and repaints.
     * BoardModel.movePiece handles captures automatically — if an enemy piece
     * occupies 'to' it is simply replaced.
     */
    private boolean executeMove(Position from, Position to) {
        Piece moving = boardModel.getPiece(from);
        if (moving == null || !moving.isValid(boardModel, to)) {
            return false;
        }

        // ── Check detection: simulate the move, reject if king left in check ──
        Piece captured = boardModel.movePiece(from, to);          // apply temporarily
        boolean selfInCheck = isKingInCheck(currentTurn);
        boardModel.undoMove(from, to, captured);                   // always revert

        if (selfInCheck) {
            // Illegal — own king would be in check after this move
            return false;
        }
        // ─────────────────────────────────────────────────────────────────────

        // Move is legal — commit it for real
        captured = boardModel.movePiece(from, to);

        MoveRecord record = new MoveRecord(from, to, moving, captured);
        history.push(record);
        recordCapture(captured);
        addHistoryEntry(record);
        undoButton.setEnabled(true);
        boardPanel.repaint();

        if (captured != null && captured.getType() == PieceType.KING) {
            declareWinner(currentTurn);
            return true;
        }

        switchTurn();
        return true;
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
     * Reads and may update {@code currentTurn} and {@code moveNumber}, so
     * callers must ensure those fields reflect the player who just moved.
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
     * Reads {@code currentTurn} to decide which list to update.
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
    //  Build history panel UI (Manish — Extra Feature 3)
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
     * Builds a SaveData snapshot of the current board and move history.
     *
     * The cells list contains two kinds of entries, in this order:
     *   1. Piece positions:  "row col COLOR TYPE"
     *   2. Move history:     "MOVE fromRow fromCol toRow toCol MCOLOR MTYPE [CCOLOR CTYPE]"
     *      — the optional captured-piece fields are present only when a capture occurred.
     *      — moves are stored in chronological order (oldest first).
     *
     * Called by ChessMenuBar when the user chooses Game → Save Game.
     */
    @Override
    public ChessMenuBar.SaveData onSaveRequested() {
        List<String> cells = new ArrayList<>();

        // ── 1. Piece positions ───────────────────────────────────────────
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece p = boardModel.getPiece(new Position(row, col));
                if (p != null) {
                    cells.add(row + " " + col + " "
                            + p.getColor().name() + " "
                            + p.getType().name());
                }
            }
        }

        // ── 2. Move history (chronological — oldest first) ───────────────
        // history is a stack (most recent on top), so reverse it before saving.
        List<MoveRecord> chronological = new ArrayList<>(history);
        Collections.reverse(chronological);
        for (MoveRecord r : chronological) {
            StringBuilder sb = new StringBuilder("MOVE ");
            sb.append(r.getFrom().getRow()).append(' ')
                    .append(r.getFrom().getCol()).append(' ')
                    .append(r.getTo().getRow()).append(' ')
                    .append(r.getTo().getCol()).append(' ')
                    .append(r.getMovedPiece().getColor().name()).append(' ')
                    .append(r.getMovedPiece().getType().name());
            if (r.getCapturedPiece() != null) {
                sb.append(' ')
                        .append(r.getCapturedPiece().getColor().name()).append(' ')
                        .append(r.getCapturedPiece().getType().name());
            }
            cells.add(sb.toString());
        }

        return new ChessMenuBar.SaveData(currentTurn.name(), cells);
    }

    /**
     * Restores the board and full move history from a previously saved SaveData.
     *
     * Steps:
     *   1. Split the cells list into piece-position lines and "MOVE …" lines.
     *   2. Blank the board and place pieces from the piece-position lines.
     *   3. Reset all history state, then replay each move line in chronological
     *      order — this rebuilds the undo stack, the history JList, the
     *      captured-piece labels, and the move counter exactly as they would
     *      look if the game had been played live up to that point.
     *   4. Restore currentTurn from the save file.
     *
     * Called by ChessMenuBar after a successful file load.
     */
    @Override
    public void onGameLoaded(ChessMenuBar.SaveData data) {

        // ── Step 1: separate piece lines from move lines ─────────────────
        List<String> pieceCells = new ArrayList<>();
        List<String> moveCells  = new ArrayList<>();
        for (String cell : data.cells) {
            if (cell.startsWith("MOVE ")) moveCells.add(cell);
            else                           pieceCells.add(cell);
        }

        // ── Step 2: restore board pieces ─────────────────────────────────
        boardModel.reset();
        clearBoardGrid();

        for (String cell : pieceCells) {
            String[] parts = cell.split(" ");
            if (parts.length != 4) continue;
            try {
                int        row   = Integer.parseInt(parts[0]);
                int        col   = Integer.parseInt(parts[1]);
                PieceColor color = PieceColor.valueOf(parts[2]);
                PieceType  type  = PieceType.valueOf(parts[3]);
                Position   pos   = new Position(row, col);
                boardModel.placePiece(PieceFactory.createPiece(type, color, pos), pos);
            } catch (Exception ignored) {
                JOptionPane.showMessageDialog(this,
                        "Move history failed to parse",
                        "Saved", JOptionPane.INFORMATION_MESSAGE);

                return;
            }
        }

        // ── Step 3: replay move history ───────────────────────────────────
        // Clear everything before replaying so no stale state remains.
        history.clear();
        capturedByWhite.clear();
        capturedByBlack.clear();
        historyModel.clear();
        moveNumber = 1;

        // Replay each move in chronological order (they were saved that way).
        // addHistoryEntry() and recordCapture() both read currentTurn, so we
        // temporarily drive currentTurn through the replay sequence here.
        PieceColor replayTurn = PieceColor.WHITE;

        for (String moveCell : moveCells) {
            // Format: "MOVE fromRow fromCol toRow toCol MCOLOR MTYPE [CCOLOR CTYPE]"
            String[] parts = moveCell.substring(5).split(" ");
            if (parts.length < 6) continue;
            try {
                Position   from    = new Position(
                        Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                Position   to      = new Position(
                        Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                PieceColor mColor  = PieceColor.valueOf(parts[4]);
                PieceType  mType   = PieceType.valueOf(parts[5]);
                Piece      moved   = PieceFactory.createPiece(mType, mColor, from);

                Piece captured = null;
                if (parts.length >= 8) {
                    PieceColor cColor = PieceColor.valueOf(parts[6]);
                    PieceType  cType  = PieceType.valueOf(parts[7]);
                    captured = PieceFactory.createPiece(cType, cColor, to);
                }

                MoveRecord record = new MoveRecord(from, to, moved, captured);

                // Push in chronological order — last push ends up on top of
                // the stack, which is correct (most recent = top).
                history.push(record);

                // Drive the helpers with the correct replay turn.
                currentTurn = replayTurn;
                recordCapture(captured);
                addHistoryEntry(record);

                replayTurn = replayTurn.opposite();

            } catch (Exception exception) {
                boardModel.reset();
                clearBoardGrid();

                history.clear();
                capturedByWhite.clear();
                capturedByBlack.clear();
                historyModel.clear();
                moveNumber = 1;
                clearSelection();
                updateStatusBar();
                undoButton.setEnabled(!history.isEmpty());
                boardPanel.repaint();
            }
        }

        // ── Step 4: restore live turn and UI state ────────────────────────
        try {
            currentTurn = PieceColor.valueOf(data.currentTurn);
        } catch (Exception ignored) {
            currentTurn = PieceColor.WHITE;
        }

        clearSelection();
        updateStatusBar();
        undoButton.setEnabled(!history.isEmpty());
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
     * Used before placing pieces from a save file.
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