package piece.pieces;

import board.BoardModel;
import piece.Piece;
import piece.PieceColor;
import piece.PieceType;
import position.Position;

public class Bishop extends Piece {
    public Bishop(PieceColor color, Position position) {
        super(PieceType.BISHOP, color, position);
    }

    @Override
    public boolean isValid(BoardModel board, Position position) {
        Position current = this.getPosition();

        int rowDiff = position.getRow() - current.getRow();
        int colDiff = position.getCol() - current.getCol();

        // Must move diagonally (equal non-zero deltas)
        if (Math.abs(rowDiff) != Math.abs(colDiff) || rowDiff == 0) {
            return false;
        }

        // Step direction: +1 or -1 for each axis
        int rowStep = (rowDiff > 0) ? 1 : -1;
        int colStep = (colDiff > 0) ? 1 : -1;

        // Walk the diagonal, checking every square in between
        int r = current.getRow() + rowStep;
        int c = current.getCol() + colStep;

        while (r != position.getRow() || c != position.getCol()) {
            if (!board.isEmpty(new Position(r, c))) {
                return false; // Blocked by a piece in the path
            }
            r += rowStep;
            c += colStep;
        }

        return true;
    }
}
