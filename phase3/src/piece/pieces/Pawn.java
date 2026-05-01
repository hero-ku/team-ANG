package piece.pieces;

import board.BoardModel;
import piece.Piece;
import piece.PieceColor;
import piece.PieceType;
import position.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Pawn piece — advances forward, captures diagonally.
 * isValid() was already correct from Phase 1.
 *
 * Phase 3 (Manish Bishwakarma):
 *   Added getPseudoLegalMoves() to support check/checkmate detection.
 */
public class Pawn extends Piece {

    public Pawn(PieceColor color, Position position) {
        super(PieceType.PAWN, color, position);
    }

    @Override
    public boolean isValid(BoardModel board, Position to) {
        Position from = this.getPosition();
        int rowDiff = to.getRow() - from.getRow();
        int colDiff = to.getCol() - from.getCol();
        int direction = (this.getColor() == PieceColor.WHITE) ? 1 : -1;
        int startRank  = (this.getColor() == PieceColor.WHITE) ? 1 : 6;

        // Diagonal capture
        if (rowDiff == direction && Math.abs(colDiff) == 1) {
            Piece occupant = board.getPiece(to);
            return occupant != null && occupant.getColor() != this.getColor();
        }

        if (colDiff != 0) return false;

        // Single step forward
        if (rowDiff == direction) return board.isEmpty(to);

        // Double step from start rank
        if (rowDiff == 2 * direction && from.getRow() == startRank) {
            Position mid = new Position(from.getRow() + direction, from.getCol());
            return board.isEmpty(mid) && board.isEmpty(to);
        }

        return false;
    }

    /**
     * Returns all squares this pawn can legally reach from its current position,
     * including forward moves and diagonal captures.
     *
     * @param board the current board state
     * @return list of candidate destination positions
     * @author Manish Bishwakarma
     */
    public List<Position> getPseudoLegalMoves(BoardModel board) {
        List<Position> moves = new ArrayList<>();
        Position from = this.getPosition();
        int direction = (this.getColor() == PieceColor.WHITE) ? 1 : -1;

        // Single step
        Position oneAhead = new Position(from.getRow() + direction, from.getCol());
        if (oneAhead.isValid() && isValid(board, oneAhead)) moves.add(oneAhead);

        // Double step
        Position twoAhead = new Position(from.getRow() + 2 * direction, from.getCol());
        if (twoAhead.isValid() && isValid(board, twoAhead)) moves.add(twoAhead);

        // Diagonal captures
        for (int dc : new int[]{-1, 1}) {
            Position diag = new Position(from.getRow() + direction, from.getCol() + dc);
            if (diag.isValid() && isValid(board, diag)) moves.add(diag);
        }

        return moves;
    }
}
