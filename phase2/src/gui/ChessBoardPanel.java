package gui;

import board.BoardModel;
import pieces.Piece;
import pieces.PieceColor;
import position.Position;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Renders the 8x8 chess board and pieces.
 *
 * Input methods supported:
 *   Click-to-move : click a piece, then click the destination.
 *   Drag-to-move  : press on a piece and drag it to the destination square.
 * Both methods share the same validation logic in ChessWindow.
 *
 * The two are mutually exclusive per gesture: if the mouse travels more than
 * DRAG_THRESHOLD pixels before release the gesture is treated as a drag and
 * the click listener is never fired, and vice-versa.
 *
 * @author Gaurav Paneru
 * Teammate 2: use addSquareClickListener() for click events,
 *             addDragDropListener() for drag events.
 * Teammate 3: use setSelectedSquare(null) to clear selection after game ends.
 */
public class ChessBoardPanel extends JPanel {

    private static final int SQUARE_SIZE    = 80;
    private static final int MARGIN         = 30;
    /** Pixel distance the mouse must travel before a press becomes a drag. */
    private static final int DRAG_THRESHOLD = 6;

    private static final Color LIGHT_SQUARE = new Color(240, 217, 181);
    private static final Color DARK_SQUARE  = new Color(181, 136,  99);
    private static final Color HIGHLIGHT    = new Color(100, 149, 237, 180);
    /** Tint drawn over the square the dragged piece is hovering above. */
    private static final Color DRAG_HOVER   = new Color(100, 200, 100, 160);

    private static final Font PIECE_FONT = new Font("Segoe UI Symbol", Font.PLAIN, 52);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 13);

    private final BoardModel boardModel;
    private Position selectedSquare;

    // ── Click listener ──────────────────────────────────────────────────────
    private SquareClickListener clickListener;

    // ── Drag state ──────────────────────────────────────────────────────────
    private DragDropListener dragListener;
    /** Where the mouse button was first pressed this gesture. */
    private int      pressX, pressY;
    /** True once the mouse has travelled past DRAG_THRESHOLD. */
    private boolean  isDragging;
    /** Board square the drag originated from. */
    private Position dragOrigin;
    /** The piece being dragged (kept for rendering; not removed from model). */
    private Piece    dragPiece;
    /** Current cursor position used to draw the floating glyph. */
    private int      dragCursorX, dragCursorY;
    /** Board square currently under the cursor, or null if off-board. */
    private Position dragHoverSquare;

    public ChessBoardPanel(BoardModel boardModel) {
        this.boardModel = boardModel;
        int size = SQUARE_SIZE * 8 + MARGIN * 2;
        setPreferredSize(new Dimension(size, size));
        setBackground(new Color(50, 30, 20));

        MouseAdapter handler = new MouseAdapter() {

            // ── Press: record where the gesture started ──────────────────
            @Override
            public void mousePressed(MouseEvent e) {
                pressX = e.getX();
                pressY = e.getY();
                isDragging = false;
                dragOrigin = null;
                dragPiece  = null;
            }

            // ── Drag: once threshold crossed, enter drag mode ────────────
            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - pressX;
                int dy = e.getY() - pressY;

                if (!isDragging) {
                    // Check whether we have crossed the threshold yet.
                    if (Math.sqrt(dx * dx + dy * dy) < DRAG_THRESHOLD) return;

                    Position origin = pixelToPosition(pressX, pressY);
                    if (origin == null) return;

                    Piece piece = boardModel.getPiece(origin);
                    if (piece == null) return;   // can't drag an empty square

                    // Commit to drag mode.
                    isDragging    = true;
                    dragOrigin    = origin;
                    dragPiece     = piece;
                    // Clear any click-style selection so the board looks clean.
                    selectedSquare = null;
                }

                dragCursorX    = e.getX();
                dragCursorY    = e.getY();
                dragHoverSquare = pixelToPosition(e.getX(), e.getY());
                repaint();
            }

            // ── Release: complete drag or fire click ─────────────────────
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging) {
                    // Complete the drag gesture.
                    Position dest = pixelToPosition(e.getX(), e.getY());
                    isDragging    = false;
                    dragHoverSquare = null;

                    if (dest != null && dragListener != null) {
                        dragListener.onDragDrop(
                                dragOrigin.getRow(), dragOrigin.getCol(),
                                dest.getRow(),       dest.getCol());
                    }
                    dragOrigin = null;
                    dragPiece  = null;
                    repaint();

                } else {
                    // Short press with no meaningful movement → treat as click.
                    Position clicked = pixelToPosition(e.getX(), e.getY());
                    if (clicked == null) return;
                    selectedSquare = clicked;
                    repaint();
                    if (clickListener != null)
                        clickListener.onSquareClicked(clicked.getRow(), clicked.getCol());
                }
            }
        };

        addMouseListener(handler);
        addMouseMotionListener(handler);
    }

    // -----------------------------------------------------------------------
    //  Painting
    // -----------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawSquares(g2);
        drawLabels(g2);
        drawHighlight(g2);
        drawDragHover(g2);
        drawPieces(g2);          // skips the piece currently being dragged
        drawDraggedPiece(g2);    // paints that piece floating at the cursor
    }

    private void drawSquares(Graphics2D g2) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                g2.setColor((row + col) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE);
                g2.fillRect(toPixelX(col), toPixelY(row), SQUARE_SIZE, SQUARE_SIZE);
            }
        }
    }

    private void drawLabels(Graphics2D g2) {
        g2.setFont(LABEL_FONT);
        g2.setColor(new Color(220, 200, 170));
        for (int col = 0; col < 8; col++) {
            g2.drawString(String.valueOf((char)('A' + col)),
                    toPixelX(col) + SQUARE_SIZE / 2 - 5,
                    MARGIN + SQUARE_SIZE * 8 + 20);
        }
        for (int row = 0; row < 8; row++) {
            g2.drawString(String.valueOf(row + 1),
                    MARGIN - 18,
                    toPixelY(row) + SQUARE_SIZE / 2 + 5);
        }
    }

    private void drawHighlight(Graphics2D g2) {
        if (selectedSquare == null) return;
        g2.setColor(HIGHLIGHT);
        g2.fillRect(toPixelX(selectedSquare.getCol()),
                toPixelY(selectedSquare.getRow()),
                SQUARE_SIZE, SQUARE_SIZE);
    }

    /** Green tint on the square the dragged piece is hovering over. */
    private void drawDragHover(Graphics2D g2) {
        if (!isDragging || dragHoverSquare == null) return;
        g2.setColor(DRAG_HOVER);
        g2.fillRect(toPixelX(dragHoverSquare.getCol()),
                toPixelY(dragHoverSquare.getRow()),
                SQUARE_SIZE, SQUARE_SIZE);
    }

    /**
     * Draws all pieces except the one currently being dragged (which floats
     * at the cursor position via drawDraggedPiece instead).
     */
    private void drawPieces(Graphics2D g2) {
        g2.setFont(PIECE_FONT);
        FontMetrics fm = g2.getFontMetrics();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = boardModel.getPiece(new Position(row, col));
                if (piece == null) continue;
                // Skip the piece being dragged — it is painted by drawDraggedPiece.
                if (isDragging && dragOrigin != null
                        && dragOrigin.getRow() == row && dragOrigin.getCol() == col) continue;
                drawGlyph(g2, fm, piece, toPixelX(col), toPixelY(row));
            }
        }
    }

    /**
     * Paints the dragged piece centered on the current cursor position.
     * A slight drop-shadow gives it a "lifted" look.
     */
    private void drawDraggedPiece(Graphics2D g2) {
        if (!isDragging || dragPiece == null) return;
        g2.setFont(PIECE_FONT);
        FontMetrics fm = g2.getFontMetrics();
        String glyph = dragPiece.getUnicodeSymbol();
        // Centre the glyph on the cursor.
        int x = dragCursorX - fm.stringWidth(glyph) / 2;
        int y = dragCursorY + (fm.getAscent() - fm.getDescent()) / 2;
        // Larger shadow to emphasise the lifted effect.
        g2.setColor(new Color(0, 0, 0, 160));
        for (int dx = -2; dx <= 2; dx++)
            for (int dy = -2; dy <= 2; dy++)
                if (dx != 0 || dy != 0)
                    g2.drawString(glyph, x + dx, y + dy);
        g2.setColor(dragPiece.getColor() == PieceColor.WHITE
                ? new Color(255, 255, 255)
                : new Color(30, 30, 30));
        g2.drawString(glyph, x, y);
    }

    /** Shared helper: draws a single piece glyph at the given pixel origin. */
    private void drawGlyph(Graphics2D g2, FontMetrics fm, Piece piece, int px, int py) {
        String glyph = piece.getUnicodeSymbol();
        int x = px + (SQUARE_SIZE - fm.stringWidth(glyph)) / 2;
        int y = py + (SQUARE_SIZE + fm.getAscent() - fm.getDescent()) / 2 - 2;
        g2.setColor(new Color(0, 0, 0, 120));
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++)
                if (dx != 0 || dy != 0)
                    g2.drawString(glyph, x + dx, y + dy);
        g2.setColor(piece.getColor() == PieceColor.WHITE
                ? new Color(255, 255, 255)
                : new Color(30, 30, 30));
        g2.drawString(glyph, x, y);
    }

    // -----------------------------------------------------------------------
    //  Coordinate helpers
    // -----------------------------------------------------------------------

    private int toPixelX(int col) { return MARGIN + col * SQUARE_SIZE; }

    /** Row 0 (White's back rank) renders at the bottom. */
    private int toPixelY(int row) { return MARGIN + (7 - row) * SQUARE_SIZE; }

    /**
     * Converts mouse pixel coordinates to a board Position.
     * Returns null if outside the board area.
     */
    public Position pixelToPosition(int px, int py) {
        int col = (px - MARGIN) / SQUARE_SIZE;
        int row = 7 - (py - MARGIN) / SQUARE_SIZE;
        Position pos = new Position(row, col);
        return pos.isValid() ? pos : null;
    }

    // -----------------------------------------------------------------------
    //  Public API for teammates
    // -----------------------------------------------------------------------

    /** Returns the board model (for move execution and piece lookup). */
    public BoardModel getBoardModel() { return boardModel; }

    /**
     * Highlights a square. Pass null to clear.
     * Called by ChessWindow when a piece is selected via click.
     */
    public void setSelectedSquare(Position p) {
        selectedSquare = p;
        repaint();
    }

    public Position getSelectedSquare() { return selectedSquare; }

    /**
     * Register a listener to receive click-to-move events (row, col) on release.
     * ChessWindow uses this for the click-based selection flow.
     */
    public void addSquareClickListener(SquareClickListener listener) {
        this.clickListener = listener;
    }

    /**
     * Register a listener to receive completed drag-drop events.
     * ChessWindow uses this to execute drag-initiated moves.
     */
    public void addDragDropListener(DragDropListener listener) {
        this.dragListener = listener;
    }

    public int getSquareSize() { return SQUARE_SIZE; }
    public int getMargin()     { return MARGIN; }

    // -----------------------------------------------------------------------
    //  Listener interfaces
    // -----------------------------------------------------------------------

    /** Callback for click-to-move: fired on mouseReleased when no drag occurred. */
    @FunctionalInterface
    public interface SquareClickListener {
        void onSquareClicked(int row, int col);
    }

    /** Callback for drag-to-move: fired on mouseReleased after a drag gesture. */
    @FunctionalInterface
    public interface DragDropListener {
        void onDragDrop(int fromRow, int fromCol, int toRow, int toCol);
    }
}