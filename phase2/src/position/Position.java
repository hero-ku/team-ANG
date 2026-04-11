package position;

/**
 * Represents a board coordinate (row 0-7, col 0-7).
 * Row 0 = rank 1 (White's back rank), Col 0 = file A.
 *
 * @author Gaurav Paneru
 */
public class Position {

    private final int row;
    private final int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    public boolean isValid() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Position)) return false;
        Position o = (Position) obj;
        return row == o.row && col == o.col;
    }

    @Override
    public int hashCode() { return 31 * row + col; }

    @Override
    public String toString() {
        return "" + (char)('A' + col) + (row + 1);
    }
}