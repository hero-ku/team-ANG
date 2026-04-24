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
        return false;
    }
}
