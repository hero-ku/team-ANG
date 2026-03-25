package piece;

import board.Board;
import utils.Color;
import utils.Position;

import java.util.Collection;

public abstract class Piece {
    protected final Position position;
    protected final Color color;

    public Piece(Color color, Position position)  {
        this.position = position;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
    public Position getPosition() {
        return position;
    }

    protected int getDirection() {
        return this.color == Color.WHITE ? 1 : -1;
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
        return this.candidateMoves(board).stream().filter(board::isWithinBounds).toList();
    }

    protected abstract Collection<Position> candidateMoves(Board board);
}
