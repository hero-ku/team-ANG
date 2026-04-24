package piece.pieces;

import board.BoardModel;
import piece.Piece;
import piece.PieceColor;
import piece.PieceType;
import position.Position;

public class Rook extends Piece {
    public Rook(PieceColor color, Position position) {
        super(PieceType.ROOK, color, position);
    }

    @Override
    public boolean isValid(BoardModel board, Position position) {
        return false;
    }
}
