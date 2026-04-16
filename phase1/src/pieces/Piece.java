package pieces;

import board.Board;
import utils.Position;
import java.util.List;

/**
 * Abstract base class for every chess piece.
 *
 * Each concrete subclass (Pawn, Rook, Knight, Bishop, Queen, King) must
 * implement {@link #possibleMoves(Board)} to encode its own movement rules,
 * and {@link #getDisplayCode()} to return the two-character symbol shown on
 * the board (e.g. "wp", "bR").
 *
 * Written by Manish — Team ANG, CS 3354 Section 255
 */
public abstract class Piece {

    /** The color of this piece (WHITE or BLACK). */
    private final Color color;

    /**
     * The current square this piece occupies.
     * Updated by {@link #move(Position)} each time the piece is moved.
     */
    private Position position;

    /**
     * Creates a piece with the given color and starting position.
     *
     * @param color    piece color (WHITE or BLACK)
     * @param position starting square
     */
    public Piece(Color color, Position position) {
        this.color = color;
        this.position = position;
    }

    // ------------------------------------------------------------------ //
    // Abstract methods — subclasses must implement these //
    // ------------------------------------------------------------------ //

    /**
     * Returns all squares this piece can legally move to from its current
     * position, given the current state of the board.
     *
     * Phase 1 note: implementations do NOT need to filter out moves that
     * would leave their own king in check.
     *
     * @param board the current board state
     * @return list of reachable {@link Position} objects (may be empty)
     */
    public abstract List<Position> possibleMoves(Board board);

    /**
     * Returns the two-character string used to render this piece on the
     * console board (e.g. {@code "wp"}, {@code "bR"}, {@code "wK"}).
     *
     * @return display symbol
     */
    public abstract String getDisplayCode();

    // ------------------------------------------------------------------ //
    // Concrete methods shared by all pieces //
    // ------------------------------------------------------------------ //

    /**
     * Moves this piece to a new square.
     *
     * Call this <em>after</em> the board has already validated the move
     * and updated its own 2-D array. This method just keeps the piece's
     * internal position in sync.
     *
     * @param newPosition the square to move to
     */
    public void move(Position newPosition) {
        this.position = newPosition;
    }

    /**
     * Returns the color of this piece.
     *
     * @return piece color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Returns the current position of this piece.
     *
     * @return current square
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Returns a readable description useful for debugging.
     *
     * @return e.g. "White Pawn at E2"
     */
    @Override
    public String toString() {
        return color + " " + getClass().getSimpleName() + " at " + position;
    }
}
