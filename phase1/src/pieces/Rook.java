package pieces;

import board.Board;
import utils.Position;
import java.util.ArrayList;
import java.util.List;

/*
 * Represents a Rook chess piece.
 *  Written by Manish — Team ANG, CS 3354 Section 255 
 */
public class Rook extends Piece {

    /**
     * Creates a new Rook.
     *
     * @param color    piece color (WHITE or BLACK)
     * @param position starting square
     */
    public Rook(Color color, Position position) {
        super(color, position);
    }

    /**
     * {@inheritDoc}
     *
     * Scans outward in all four axis-aligned directions (up, down,
     * left, right), adding squares until a piece or the board edge is hit.
     * An opponent's square is included (capture); a friendly square stops
     * the ray without being added.
     */
    @Override
    public List<Position> possibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();

        // The four cardinal directions: {rowDelta, colDelta}
        int[][] directions = {
                { -1, 0 }, // up
                { 1, 0 }, // down
                { 0, -1 }, // left
                { 0, 1 } // right
        };

        for (int[] dir : directions) {
            scanRay(board, moves, dir[0], dir[1]);
        }

        return moves;
    }

    /**
     * Walks one ray outward from the rook's current position, adding every
     * reachable square to {@code moves}.
     *
     * @param board    current board state
     * @param moves    accumulator list to add valid squares to
     * @param rowDelta row step per iteration (+1, -1, or 0)
     * @param colDelta column step per iteration (+1, -1, or 0)
     */
    private void scanRay(Board board, List<Position> moves, int rowDelta, int colDelta) {
        int row = getPosition().getRow() + rowDelta;
        int col = getPosition().getColumn() + colDelta;

        while (true) {
            Position next = new Position(row, col);

            // Stop if we've left the board
            if (!board.isWithinBounds(next))
                break;

            if (board.isEmpty(next)) {
                // Empty square — add it and keep going
                moves.add(next);
            } else if (board.isOpponentPiece(next, getColor())) {
                // Opponent's piece — we can capture here, then stop
                moves.add(next);
                break;
            } else {
                // Friendly piece — blocked, stop without adding
                break;
            }

            row += rowDelta;
            col += colDelta;
        }
    }

    /**
     * Returns the console display code for this rook.
     *
     * @return {@code "wR"} for white, {@code "bR"} for black
     */
    @Override
    public String getDisplayCode() {
        return (getColor() == Color.WHITE) ? "wR" : "bR";
    }
}
