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
        Position current = this.getPosition();

        int rowDiff = position.getRow() - current.getRow();
        int colDiff = position.getCol() - current.getCol();

        // Direction pawns advance: White goes up (+1), Black goes down (-1)
        int direction = (this.getColor() == PieceColor.WHITE) ? 1 : -1;

        // Starting rank where the double-step is allowed
        int startRank = (this.getColor() == PieceColor.WHITE) ? 1 : 6;

        // --- Diagonal capture ---
        // One step forward diagonally, target must hold an enemy piece
        if (rowDiff == direction && Math.abs(colDiff) == 1) {
            Piece occupant = board.getPiece(position);
            return occupant != null && occupant.getColor() != this.getColor();
        }

        // --- Forward moves (must stay in same column) ---
        if (colDiff != 0) return false;

        // Single step forward — target must be empty
        if (rowDiff == direction) {
            return board.isEmpty(position);
        }

        // Double step forward — only from starting rank, both squares must be clear
        if (rowDiff == 2 * direction && current.getRow() == startRank) {
            Position intermediate = new Position(current.getRow() + direction, current.getCol());
            return board.isEmpty(intermediate) && board.isEmpty(position);
        }

        return false;
    }
}
