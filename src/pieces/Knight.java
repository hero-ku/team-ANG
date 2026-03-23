package pieces;

import board.Board;
import utils.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Knight chess piece.
 *
 * Written by Manish — Team ANG, CS 3354 Section 255
 */
public class Knight extends Piece {

    /**
     * All eight possible L-shaped offsets a knight can reach.
     * Each entry is {rowOffset, colOffset}.
     */
    private static final int[][] MOVES = {
            { -2, -1 }, { -2, 1 }, // two up, one left/right
            { 2, -1 }, { 2, 1 }, // two down, one left/right
            { -1, -2 }, { -1, 2 }, // one up, two left/right
            { 1, -2 }, { 1, 2 } // one down, two left/right
    };

    /**
     * Creates a new Knight.
     *
     * @param color    piece color (WHITE or BLACK)
     * @param position starting square
     */
    public Knight(Color color, Position position) {
        super(color, position);
    }

    /**
     * {@inheritDoc}
     *
     * Checks each of the eight L-shaped offsets. A square is valid if it
     * is on the board and is either empty or holds an opponent's piece.
     */
    @Override
    public List<Position> possibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();

        int row = getPosition().getRow();
        int col = getPosition().getColumn();

        for (int[] offset : MOVES) {
            Position target = new Position(row + offset[0], col + offset[1]);

            if (!board.isWithinBounds(target))
                continue;

            // Can move to empty squares or capture opponent pieces
            if (board.isEmpty(target) || board.isOpponentPiece(target, getColor())) {
                moves.add(target);
            }
        }

        return moves;
    }

    /**
     * Returns the console display code for this knight.
     *
     * @return {@code "wN"} for white, {@code "bN"} for black
     */
    @Override
    public String getDisplayCode() {
        return (getColor() == Color.WHITE) ? "wN" : "bN";
    }
}
