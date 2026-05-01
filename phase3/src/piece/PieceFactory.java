package piece;

import piece.pieces.*;
import position.Position;

public class PieceFactory {
    public static Piece createPiece(PieceType pieceType, PieceColor color, Position position) {
        return switch (pieceType) {
            case KING -> new King(color, position);
            case PAWN -> new Pawn(color, position);
            case QUEEN -> new Queen(color, position);
            case BISHOP -> new Bishop(color, position);
            case ROOK -> new Rook(color, position);
            case KNIGHT -> new Knight(color, position);
        };
    }
}
