package piece.pieces;

import board.BoardModel;
import piece.Piece;
import piece.PieceColor;
import piece.PieceType;
import position.Position;

public class King extends Piece {
    public King(PieceColor color, Position position) {
        super(PieceType.KING, color, position);
    }

    @Override
    public boolean isValid(BoardModel board, Position position) {
        return false;
    }
}
