package piece.pieces;

import board.BoardModel;
import piece.Piece;
import piece.PieceColor;
import piece.PieceType;
import position.Position;

public class Queen extends Piece {
    public Queen(PieceColor color, Position position) {
        super(PieceType.QUEEN, color, position);
    }

    @Override
    public boolean isValid(BoardModel board, Position position) {
        return true;
    }
}
