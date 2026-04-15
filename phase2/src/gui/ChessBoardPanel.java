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
 *   Click-to-move : click a piece, then click the destination square.
 *   Drag-to-move  : press on a piece and drag it to the destination square.
 * Both methods share the same validation logic in ChessWindow.
 *
 * The two are mutually exclusive per gesture: if the mouse travels more than
 * DRAG_THRESHOLD pixels before release the gesture is treated as a drag and
 * the click listener is never fired, and vice-versa.
 *
 * Feature 2 additions:
 *   applySettings() – called by SettingsWindow to update colors and square size.
 *   getBoardTheme() / getPieceTheme() / getBoardSize() – expose current settings.
 *
 * @author Gaurav Paneru
 * Teammate 2: use addSquareClickListener() for click events,
 *             addDragDropListener() for drag events.
 * Teammate 3: use setSelectedSquare(null) to clear selection after game ends.
 */
public class ChessBoardPanel extends JPanel {

    // -----------------------------------------------------------------------
    // Defaults (match the original hard-coded values)
    // -----------------------------------------------------------------------

    private static final int DEFAULT_SQUARE_SIZE = 80;
    private static final int MARGIN              = 30;
    private static final int DRAG_THRESHOLD      = 6;

    private static final Color HIGHLIGHT  = new Color(100, 149, 237, 180);
    private static final Color DRAG_HOVER = new Color(100, 200, 100, 160);

    private static final Font PIECE_FONT = new Font("Segoe UI Symbol", Font.PLAIN, 52);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 13);

    // -----------------------------------------------------------------------
    // Mutable appearance (Feature 2)
    // -----------------------------------------------------------------------

    /** Current board color theme – drives light / dark square colors. */
    private SettingsWindow.BoardTheme boardTheme =
            SettingsWindow.BoardTheme.CLASSIC_WOOD;

    /** Current piece color theme – drives white / black piece glyph colors. */
    private SettingsWindow.PieceTheme pieceTheme =
            SettingsWindow.PieceTheme.CLASSIC;

    /** Current square size in pixels (affects preferred size). */
    private int squareSize = DEFAULT_SQUARE_SIZE;

    // -----------------------------------------------------------------------
    // Model and selection state
    // -----------------------------------------------------------------------

    private final BoardModel boardModel;
    private Position selectedSquare;

    // ── Click listener ──────────────────────────────────────────────────────
    private SquareClickListener clickListener;

    // ── Drag state ──────────────────────────────────────────────────────────
    private DragDropListener dragListener;
    private int      pressX, pressY;
    private boolean  isDragging;
    private Position dragOrigin;
    private Piece    dragPiece;
    private int      dragCursorX, dragCursorY;
    private Position dragHoverSquare;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates the board panel backed by the given model.
     *
     * @param boardModel the model that holds piece positions
     */
    public ChessBoardPanel(BoardModel boardModel) {
        this.boardModel = boardModel;
        refreshPreferredSize();
        setBackground(new Color(50, 30, 20));

        MouseAdapter handler = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                pressX     = e.getX();
                pressY     = e.getY();
                isDragging = false;
                dragOrigin = null;
                dragPiece  = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - pressX;
                int dy = e.getY() - pressY;

                if (!isDragging) {
                    if (Math.sqrt(dx * dx + dy * dy) < DRAG_THRESHOLD) return;

                    Position origin = pixelToPosition(pressX, pressY);
                    if (origin == null) return;

                    Piece piece = boardModel.getPiece(origin);
                    if (piece == null) return;

                    isDragging     = true;
                    dragOrigin     = origin;
                    dragPiece      = piece;
                    selectedSquare = null;
                }

                dragCursorX     = e.getX();
                dragCursorY     = e.getY();
                dragHoverSquare = pixelToPosition(e.getX(), e.getY());
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging) {
                    Position dest = pixelToPosition(e.getX(), e.getY());
                    isDragging      = false;
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
    //  Feature 2 – settings API
    // -----------------------------------------------------------------------

    /**
     * Applies new appearance settings and repaints.
     * Called by {@link SettingsWindow} when the user presses "Apply".
     *
     * @param bt new board color theme
     * @param pt new piece color theme
     * @param bs new board size option
     */
    public void applySettings(SettingsWindow.BoardTheme bt,
                              SettingsWindow.PieceTheme pt,
                              SettingsWindow.BoardSize  bs) {
        this.boardTheme = bt;
        this.pieceTheme = pt;
        this.squareSize = bs.squareSize;
        refreshPreferredSize();

        // Inform the parent window to re-pack so it resizes to the new board
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w != null) w.pack();

        repaint();
    }

    /** Returns the currently active board theme (used by SettingsWindow). */
    public SettingsWindow.BoardTheme getBoardTheme() { return boardTheme; }

    /** Returns the currently active piece theme (used by SettingsWindow). */
    public SettingsWindow.PieceTheme getPieceTheme() { return pieceTheme; }

    /** Returns the currently active board size setting (used by SettingsWindow). */
    public SettingsWindow.BoardSize getBoardSize() {
        for (SettingsWindow.BoardSize bs : SettingsWindow.BoardSize.values()) {
            if (bs.squareSize == squareSize) return bs;
        }
        return SettingsWindow.BoardSize.MEDIUM;
    }

    // -----------------------------------------------------------------------
    //  Painting
    // -----------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawSquares(g2);
        drawLabels(g2);
        drawHighlight(g2);
        drawDragHover(g2);
        drawPieces(g2);
        drawDraggedPiece(g2);
    }

    private void drawSquares(Graphics2D g2) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                boolean light = (row + col) % 2 == 0;
                g2.setColor(light ? boardTheme.light : boardTheme.dark);
                g2.fillRect(toPixelX(col), toPixelY(row), squareSize, squareSize);
            }
        }
    }

    private void drawLabels(Graphics2D g2) {
        g2.setFont(LABEL_FONT);
        g2.setColor(new Color(220, 200, 170));
        for (int col = 0; col < 8; col++) {
            g2.drawString(String.valueOf((char) ('A' + col)),
                    toPixelX(col) + squareSize / 2 - 5,
                    MARGIN + squareSize * 8 + 20);
        }
        for (int row = 0; row < 8; row++) {
            g2.drawString(String.valueOf(row + 1),
                    MARGIN - 18,
                    toPixelY(row) + squareSize / 2 + 5);
        }
    }

    private void drawHighlight(Graphics2D g2) {
        if (selectedSquare == null) return;
        g2.setColor(HIGHLIGHT);
        g2.fillRect(toPixelX(selectedSquare.getCol()),
                    toPixelY(selectedSquare.getRow()),
                    squareSize, squareSize);
    }

    private void drawDragHover(Graphics2D g2) {
        if (!isDragging || dragHoverSquare == null) return;
        g2.setColor(DRAG_HOVER);
        g2.fillRect(toPixelX(dragHoverSquare.getCol()),
                    toPixelY(dragHoverSquare.getRow()),
                    squareSize, squareSize);
    }

    private void drawPieces(Graphics2D g2) {
        Font scaledFont = PIECE_FONT.deriveFont(Font.PLAIN, squareSize * 0.65f);
        g2.setFont(scaledFont);
        FontMetrics fm = g2.getFontMetrics();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = boardModel.getPiece(new Position(row, col));
                if (piece == null) continue;
                if (isDragging && dragOrigin != null
                        && dragOrigin.getRow() == row
                        && dragOrigin.getCol() == col) continue;
                drawGlyph(g2, fm, piece, toPixelX(col), toPixelY(row));
            }
        }
    }

    private void drawDraggedPiece(Graphics2D g2) {
        if (!isDragging || dragPiece == null) return;
        Font scaledFont = PIECE_FONT.deriveFont(Font.PLAIN, squareSize * 0.65f);
        g2.setFont(scaledFont);
        FontMetrics fm = g2.getFontMetrics();
        String glyph = dragPiece.getUnicodeSymbol();
        int x = dragCursorX - fm.stringWidth(glyph) / 2;
        int y = dragCursorY + (fm.getAscent() - fm.getDescent()) / 2;
        g2.setColor(new Color(0, 0, 0, 160));
        for (int dx = -2; dx <= 2; dx++)
            for (int dy = -2; dy <= 2; dy++)
                if (dx != 0 || dy != 0)
                    g2.drawString(glyph, x + dx, y + dy);
        g2.setColor(dragPiece.getColor() == PieceColor.WHITE
                ? pieceTheme.whiteColor : pieceTheme.blackColor);
        g2.drawString(glyph, x, y);
    }

    private void drawGlyph(Graphics2D g2, FontMetrics fm, Piece piece, int px, int py) {
        String glyph = piece.getUnicodeSymbol();
        int x = px + (squareSize - fm.stringWidth(glyph)) / 2;
        int y = py + (squareSize + fm.getAscent() - fm.getDescent()) / 2 - 2;
        g2.setColor(new Color(0, 0, 0, 120));
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++)
                if (dx != 0 || dy != 0)
                    g2.drawString(glyph, x + dx, y + dy);
        g2.setColor(piece.getColor() == PieceColor.WHITE
                ? pieceTheme.whiteColor : pieceTheme.blackColor);
        g2.drawString(glyph, x, y);
    }

    // -----------------------------------------------------------------------
    //  Coordinate helpers
    // -----------------------------------------------------------------------

    private int toPixelX(int col) { return MARGIN + col * squareSize; }

    private int toPixelY(int row) { return MARGIN + (7 - row) * squareSize; }

    /**
     * Converts pixel coordinates to a board Position.
     * Returns null if the point is outside the board area.
     */
    public Position pixelToPosition(int px, int py) {
        int col = (px - MARGIN) / squareSize;
        int row = 7 - (py - MARGIN) / squareSize;
        Position pos = new Position(row, col);
        return pos.isValid() ? pos : null;
    }

    // -----------------------------------------------------------------------
    //  Internal helpers
    // -----------------------------------------------------------------------

    /** Recalculates the preferred size whenever squareSize changes. */
    private void refreshPreferredSize() {
        int size = squareSize * 8 + MARGIN * 2;
        setPreferredSize(new Dimension(size, size));
    }

    // -----------------------------------------------------------------------
    //  Public API for teammates
    // -----------------------------------------------------------------------

    /** Returns the board model. */
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

    public void addSquareClickListener(SquareClickListener listener) {
        this.clickListener = listener;
    }

    public void addDragDropListener(DragDropListener listener) {
        this.dragListener = listener;
    }

    public int getSquareSize() { return squareSize; }
    public int getMargin()     { return MARGIN; }

    // -----------------------------------------------------------------------
    //  Listener interfaces
    // -----------------------------------------------------------------------

    /** Fired on mouseReleased when no drag occurred. */
    @FunctionalInterface
    public interface SquareClickListener {
        void onSquareClicked(int row, int col);
    }

    /** Fired on mouseReleased after a drag gesture. */
    @FunctionalInterface
    public interface DragDropListener {
        void onDragDrop(int fromRow, int fromCol, int toRow, int toCol);
    }
}