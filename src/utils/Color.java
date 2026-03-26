package utils;

/**
 * Represents one of the colors a player can be, black or white.
 */
public enum Color {
    WHITE,
    BLACK,
    ;

    /**
     * Returns the opposite color.
     * @return Black if the color is white, white if the color is black.
     */
    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
