package pieces;

/**
 * Represents the two player colors in chess.
 *
 * Written by Manish — Team ANG, CS 3354 Section 255
 */
public enum Color {

    /** The white player (moves first, starts at ranks 1–2). */
    WHITE,

    /** The black player (starts at ranks 7–8). */
    BLACK;

    /**
     * Returns the opposing color.
     *
     * Used throughout the game loop to switch turns and check
     * whether a piece belongs to the current player or the opponent.
     *
     * @return {@code BLACK} if called on {@code WHITE}, and vice-versa
     */
    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }

    /**
     * Returns a display-friendly string ("White" or "Black").
     *
     * @return capitalized color name
     */
    @Override
    public String toString() {
        return this == WHITE ? "White" : "Black";
    }
}
