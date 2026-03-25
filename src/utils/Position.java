package utils;

/**
 * Represents a square on the chessboard using row and column indices.
 *
 * <p>Rows run from 0 (top / black's back rank) to 7 (bottom / white's back rank).
 * Columns run from 0 (file A) to 7 (file H).</p>
 *
 * <p>Written by Andrew — Team ANG, CS 3354 Section 255</p>
 */
public record Position(int row, int column) {
    /**
     * Adds another position to this position component-wise.
     *
     * @param other The other position to add to this position.
     *
     * @return A Position that is the sum of the two positions.
     */
    public Position add(Position other) {
        return new Position(this.row + other.row(), this.column + other.column());
    }

    /**
     * Returns a human-readable chess-notation string (e.g. "E2").
     *
     * @return algebraic square label
     */
    @Override
    public String toString() {
        char file = (char) ('A' + column);
        int  rank = 8 - row;
        return "" + file + rank;
    }
}
