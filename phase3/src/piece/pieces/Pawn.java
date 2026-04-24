package piece.pieces;

import board.BoardModel;
import piece.Piece;
import piece.PieceColor;
import piece.PieceType;
import position.Position;

public class Pawn extends Piece {
    public Pawn(PieceColor color, Position position) {
        super(PieceType.PAWN, color, position);
    }

    @Override
    public boolean isValid(BoardModel board, Position position) {
        return false;
    }
}
