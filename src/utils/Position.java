package utils;

/**
 * Represents a square on the chessboard using row and column indices.
 *
 * <p>Rows run from 0 (top / black's back rank) to 7 (bottom / white's back rank).
 * Columns run from 0 (file A) to 7 (file H).</p>
 *
 * <p>Written by Manish — Team ANG, CS 3354 Section 255</p>
 */
public class Position {

    /** Row index (0 = rank 8, 7 = rank 1). */
    private final int row;

    /** Column index (0 = file A, 7 = file H). */
    private final int column;

    /**
     * Creates a new Position with the given row and column.
     *
     * @param row    row index (0–7)
     * @param column column index (0–7)
     */
    public Position(int row, int column) {
        this.row    = row;
        this.column = column;
    }

    /**
     * Returns the row index of this position.
     *
     * @return row (0–7)
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns the column index of this position.
     *
     * @return column (0–7)
     */
    public int getColumn() {
        return column;
    }

    /**
     * Two positions are equal if they share the same row and column.
     *
     * @param obj object to compare
     * @return true if both positions represent the same square
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Position)) return false;
        Position other = (Position) obj;
        return this.row == other.row && this.column == other.column;
    }

    /**
     * Hash code consistent with {@link #equals(Object)}.
     *
     * @return hash code based on row and column
     */
    @Override
    public int hashCode() {
        return 31 * row + column;
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
