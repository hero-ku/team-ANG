package piece.pieces;

import board.BoardModel;
import piece.Piece;
import piece.PieceColor;
import piece.PieceType;
import position.Position;

public class Knight extends Piece {
    public Knight(PieceColor color, Position position) {
        super(PieceType.KNIGHT, color, position);
    }

    @Override
    public boolean isValid(BoardModel board, Position position) {
        return false;
    }
}
