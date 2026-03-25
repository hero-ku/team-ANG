package utils;

import board.Board;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class containing static helper methods shared across the chess
 * engine.
 *
 * This class is not meant to be instantiated — all methods are static.
 * It provides reusable logic so piece subclasses do not duplicate code.
 * Written by Manish — Team ANG, CS 3354 Section 255
 */
public final class ChessUtils {

    /**
     * Private constructor — this class should never be instantiated.
     */
    private ChessUtils() {
        throw new UnsupportedOperationException("ChessUtils is a utility class.");
    }

    // ------------------------------------------------------------------ //
    // Ray scanning //
    // ------------------------------------------------------------------ //

    /**
     * Walks one directional ray from {@code origin}, collecting every square
     * reachable by a sliding piece (Rook, Bishop, or Queen).
     *
     * The ray continues outward, square by square, until it hits the edge
     * of the board, a friendly piece, or an opponent's piece (which is
     * included as a capture target before stopping).
     *
     * @param board    current board state
     * @param origin   starting square (the piece's current position)
     * @param rowDelta row step per iteration: -1 (up), 0, or +1 (down)
     * @param colDelta col step per iteration: -1 (left), 0, or +1 (right)
     * @param color    color of the moving piece (used to detect friendly pieces)
     * @return ordered list of reachable squares along this ray
     */
    public static List<Position> scanRay(Board board,
            Position origin,
            int rowDelta,
            int colDelta,
            pieces.Color color) {
        List<Position> moves = new ArrayList<>();

        int row = origin.getRow() + rowDelta;
        int col = origin.getColumn() + colDelta;

        while (true) {
            Position next = new Position(row, col);

            if (!board.isWithinBounds(next))
                break;

            if (board.isEmpty(next)) {
                moves.add(next); // open square — keep going
            } else if (board.isOpponentPiece(next, color)) {
                moves.add(next); // capture — stop after
                break;
            } else {
                break; // friendly piece — blocked
            }

            row += rowDelta;
            col += colDelta;
        }

        return moves;
    }

    /**
     * Applies {@link #scanRay} across multiple directions and combines the results.
     * 
     * @param board      current board state
     * @param origin     starting square
     * @param directions array of {rowDelta, colDelta} pairs
     * @param color      color of the moving piece
     * @return combined list of all reachable squares across every direction
     */
    public static List<Position> scanAllRays(Board board,
            Position origin,
            int[][] directions,
            pieces.Color color) {
        List<Position> moves = new ArrayList<>();
        for (int[] dir : directions) {
            moves.addAll(scanRay(board, origin, dir[0], dir[1], color));
        }
        return moves;
    }

    // ------------------------------------------------------------------ //
    // Algebraic notation helpers //
    // ------------------------------------------------------------------ //

    /**
     * Converts a {@link Position} to its standard algebraic label (e.g. "E4").
     *
     * Row 0 maps to rank 8; column 0 maps to file A.
     *
     * @param position square to convert
     * @return two-character algebraic label, or {@code "??"} if null
     */
    public static String toAlgebraic(Position position) {
        if (position == null)
            return "??";
        char file = (char) ('A' + position.getColumn());
        int rank = 8 - position.getRow();
        return "" + file + rank;
    }

    /**
     * Parses an algebraic label (e.g. {@code "E4"}) into a {@link Position}.
     *
     * Accepts upper and lower case. Returns {@code null} for any input
     * that is not exactly two characters, where the first is A–H and the
     * second is 1–8.
     *
     * @param label algebraic square label (e.g. {@code "E4"} or {@code "e4"})
     * @return corresponding Position, or {@code null} if the label is invalid
     */
    public static Position fromAlgebraic(String label) {
        if (label == null || label.length() != 2)
            return null;

        char file = Character.toUpperCase(label.charAt(0));
        char rank = label.charAt(1);

        if (file < 'A' || file > 'H')
            return null;
        if (rank < '1' || rank > '8')
            return null;

        int column = file - 'A';
        int row = '8' - rank;
        return new Position(row, column);
    }

    /**
     * Returns {@code true} if the given algebraic label is syntactically valid
     * (i.e. would not return {@code null} from {@link #fromAlgebraic(String)}).
     *
     * @param label label to validate
     * @return true if the label represents a real board square
     */
    public static boolean isValidLabel(String label) {
        return fromAlgebraic(label) != null;
    }
}
