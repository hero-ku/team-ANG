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
 * @author Gaurav Paneru
 * Teammate 2: use addSquareClickListener() to receive click events.
 * Teammate 3: use setSelectedSquare(null) to clear selection after game ends.
 */
public class ChessBoardPanel extends JPanel {

    private static final int SQUARE_SIZE = 80;
    private static final int MARGIN      = 30;

    private static final Color LIGHT_SQUARE = new Color(240, 217, 181);
    private static final Color DARK_SQUARE  = new Color(181, 136,  99);
    private static final Color HIGHLIGHT    = new Color(100, 149, 237, 180);

    private static final Font PIECE_FONT = new Font("Segoe UI Symbol", Font.PLAIN, 52);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 13);

    private final BoardModel boardModel;
    private Position selectedSquare;
    private SquareClickListener clickListener;

    public ChessBoardPanel(BoardModel boardModel) {
        this.boardModel = boardModel;
        int size = SQUARE_SIZE * 8 + MARGIN * 2;
        setPreferredSize(new Dimension(size, size));
        setBackground(new Color(50, 30, 20));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Position clicked = pixelToPosition(e.getX(), e.getY());
                if (clicked == null) return;
                selectedSquare = clicked;
                repaint();
                if (clickListener != null)
                    clickListener.onSquareClicked(clicked.getRow(), clicked.getCol());
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawSquares(g2);
        drawLabels(g2);
        drawHighlight(g2);
        drawPieces(g2);
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

    private void drawPieces(Graphics2D g2) {
        g2.setFont(PIECE_FONT);
        FontMetrics fm = g2.getFontMetrics();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = boardModel.getPiece(new Position(row, col));
                if (piece == null) continue;
                String glyph = piece.getUnicodeSymbol();
                int x = toPixelX(col) + (SQUARE_SIZE - fm.stringWidth(glyph)) / 2;
                int y = toPixelY(row) + (SQUARE_SIZE + fm.getAscent() - fm.getDescent()) / 2 - 2;
                // Shadow for readability
                g2.setColor(new Color(0, 0, 0, 120));
                for (int dx = -1; dx <= 1; dx++)
                    for (int dy = -1; dy <= 1; dy++)
                        if (dx != 0 || dy != 0)
                            g2.drawString(glyph, x + dx, y + dy);
                // Glyph
                g2.setColor(piece.getColor() == PieceColor.WHITE
                        ? new Color(255, 255, 255)
                        : new Color(30, 30, 30));
                g2.drawString(glyph, x, y);
            }
        }
    }

    // --- Coordinate helpers ---

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

    // --- Public API for teammates ---

    /** Returns the board model (for move execution and piece lookup). */
    public BoardModel getBoardModel() { return boardModel; }

    /**
     * Highlights a square. Pass null to clear.
     * Called by teammate 2 when a piece is selected.
     */
    public void setSelectedSquare(Position p) {
        selectedSquare = p;
        repaint();
    }

    public Position getSelectedSquare() { return selectedSquare; }

    /**
     * Register a click listener to receive (row, col) on square clicks.
     * Teammate 2 uses this to handle piece selection and movement.
     */
    public void addSquareClickListener(SquareClickListener listener) {
        this.clickListener = listener;
    }

    public int getSquareSize() { return SQUARE_SIZE; }
    public int getMargin()     { return MARGIN; }

    /** Callback interface for square click events. */
    @FunctionalInterface
    public interface SquareClickListener {
        void onSquareClicked(int row, int col);
    }
}