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
        char file = (char) ('A' + position.column());
        int rank = 8 - position.row();
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
