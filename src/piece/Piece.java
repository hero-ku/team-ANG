package piece;

import board.Board;
import utils.Color;
import utils.Position;

import java.util.Collection;

/**
 * Represents a piece on the board or captured in the course of play.
 */
public abstract class Piece {
    /**
     * The current position of the piece.
     */
    protected Position position;
    /**
     * The color representing the player to whom the piece belongs.
     */
    protected final Color color;

    /**
     * Creates a new piece given color and initial position.
     */
    public Piece(Color color, Position position)  {
        this.position = position;
        this.color = color;
    }

    /**
     * Gets the color of the piece.
     * @return The color.
     */
    public Color getColor() {
        return color;
    }
    /**
     * Gets the current position of the piece.
     * @return The current position.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Gets the hypothetical direction away from the side of the player who owns the piece.
     * @return 1 if the piece's color is black, -1 if it is white.
     */
    protected int getDirection() {
        return this.color == Color.WHITE ? -1 : 1;
    }

    /**
     * Moves the piece to the given position.
     *
     * @param position The position to move to.
     */
    public void move(Position position) {
        this.position = position;
    }

    /**
     * Returns a letter that represents the piece (e.g. 'K' for King).
     *
     * @return The letter.
     */
    public abstract String getPieceLetter();

    /**
     * Returns a code that represents the piece (e.g. 'wK' for white King).
     *
     * @return The code.
     */
    public String getDisplayCode() {
        return (this.color == Color.WHITE ? "w" : "b") + getPieceLetter();
    }

    /**
     * Returns all legal destination squares for this piece.
     * Filters raw candidates down to moves that are within bounds,
     * not occupied by a friendly piece, do not leave
     * the moving side's king in check.
     *
     * @param board current board state
     * @return collection of legal destination positions
     */
    public Collection<Position> possibleMoves(Board board) {
        // TODO: Detect if move will put player in check
        return this.candidateMoves(board).stream().filter((move) -> board.isWithinBounds(move) && (board.getPiece(move) == null || board.getPiece(move).getColor() != this.color)).toList();
    }

    /**
     * Gets the candidate moves for the piece, i.e. all possible positions it could move to geometrically, without accounting for check or pieces in the way.
     *
     * @return A collection of positions representing the destinations of the possible moves.
     */
    protected abstract Collection<Position> candidateMoves(Board board);
}
