package piece.pieces;

import board.BoardModel;
import piece.Piece;
import piece.PieceColor;
import piece.PieceType;
import position.Position;

/**
 * Rook moves any number of squares horizontally or vertically.
 * Cannot jump over pieces.
 *
 * @author Gaurav Paneru
 */
public class Rook extends Piece {

    public Rook(PieceColor color, Position position) {
        super(PieceType.ROOK, color, position);
    }

    @Override
    public boolean isValid(BoardModel board, Position to) {
        Position current = this.getPosition();

        int rowDiff = to.getRow() - current.getRow();
        int colDiff = to.getCol() - current.getCol();

        // Must move along a rank or file — not both, not neither
        boolean movingAlongRow = (rowDiff == 0 && colDiff != 0);
        boolean movingAlongCol = (colDiff == 0 && rowDiff != 0);
        if (!movingAlongRow && !movingAlongCol) return false;

        // Step direction: +1 or -1 along the axis of movement
        int rowStep = Integer.signum(rowDiff);
        int colStep = Integer.signum(colDiff);

        // Walk the path and check every square in between
        int r = current.getRow() + rowStep;
        int c = current.getCol() + colStep;

        while (r != to.getRow() || c != to.getCol()) {
            if (!board.isEmpty(new Position(r, c))) {
                return false; // Blocked by a piece in the path
            }
            r += rowStep;
            c += colStep;
        }

        // Destination must be empty or hold an enemy piece
        Piece occupant = board.getPiece(to);
        return occupant == null || occupant.getColor() != this.getColor();
    }
}