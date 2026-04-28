package piece.pieces;

import board.BoardModel;
import piece.Piece;
import piece.PieceColor;
import piece.PieceType;
import position.Position;

/**
 * Knight moves in an L-shape: two squares in one direction,
 * one square perpendicular. Can jump over other pieces.
 *
 * @author Gaurav Paneru
 */
public class Knight extends Piece {

    public Knight(PieceColor color, Position position) {
        super(PieceType.KNIGHT, color, position);
    }

    @Override
    public boolean isValid(BoardModel board, Position to) {
        Position current = this.getPosition();

        int rowDiff = Math.abs(to.getRow() - current.getRow());
        int colDiff = Math.abs(to.getCol() - current.getCol());

        // Must be an L-shape: (2,1) or (1,2)
        boolean isLShape = (rowDiff == 2 && colDiff == 1)
                || (rowDiff == 1 && colDiff == 2);
        if (!isLShape) return false;

        // Destination must be empty or hold an enemy piece
        Piece occupant = board.getPiece(to);
        return occupant == null || occupant.getColor() != this.getColor();
    }
}