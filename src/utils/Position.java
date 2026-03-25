package utils;

public record Position(int row, int column) {
    public Position add(Position other) {
        return new Position(this.row + other.row(), this.column + other.column());
    }
}
