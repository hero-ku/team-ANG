package piece.pieces;

import board.BoardModel;
import piece.Piece;
import piece.PieceColor;
import piece.PieceType;
import position.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * King piece — moves exactly one square in any direction.
 * Cannot move onto a square occupied by a friendly piece.
 *
 * Phase 3 (Manish Bishwakarma):
 *   isValid() and getPseudoLegalMoves() implemented to support
 *   check and checkmate detection in BoardModel.
 */
public class King extends Piece {

    public King(PieceColor color, Position position) {
        super(PieceType.KING, color, position);
    }

    /**
     * A King move is valid when the destination is exactly one step away
     * (horizontally, vertically, or diagonally) and is either empty or
     * holds an enemy piece. Whether the destination is safe (not attacked)
     * is handled by BoardModel.getLegalMovesFrom(), not here.
     */
    @Override
    public boolean isValid(BoardModel board, Position to) {
        Position from = this.getPosition();
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getCol() - from.getCol());

        // Must move exactly one square in any direction
        if (rowDiff > 1 || colDiff > 1 || (rowDiff == 0 && colDiff == 0)) return false;

        // Cannot land on a friendly piece
        Piece occupant = board.getPiece(to);
        return occupant == null || occupant.getColor() != this.getColor();
    }

    /**
     * Returns all squares the King could reach ignoring check exposure.
     * BoardModel filters these down to truly safe squares.
     *
     * @param board the current board state
     * @return list of candidate destination positions
     * @author Manish Bishwakarma
     */
    public List<Position> getPseudoLegalMoves(BoardModel board) {
        List<Position> moves = new ArrayList<>();
        Position from = this.getPosition();
        int[] deltas = {-1, 0, 1};
        for (int dr : deltas) {
            for (int dc : deltas) {
                if (dr == 0 && dc == 0) continue;
                Position to = new Position(from.getRow() + dr, from.getCol() + dc);
                if (to.isValid() && isValid(board, to)) moves.add(to);
            }
        }
        return moves;
    }
}
