package piece.pieces;

import board.BoardModel;
import piece.Piece;
import piece.PieceColor;
import piece.PieceType;
import position.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Knight piece — moves in an L-shape (2+1 squares). Jumps over pieces.
 *
 * Phase 3 (Manish Bishwakarma):
 *   isValid() and getPseudoLegalMoves() implemented.
 */
public class Knight extends Piece {

    private static final int[][] OFFSETS = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            { 1, -2}, { 1, 2}, { 2, -1}, { 2,  1}
    };

    public Knight(PieceColor color, Position position) {
        super(PieceType.KNIGHT, color, position);
    }

    /**
     * A Knight move is valid when the destination is an L-shape away and
     * is either empty or holds an enemy piece. Knights jump — no path check needed.
     */
    @Override
    public boolean isValid(BoardModel board, Position to) {
        Position from = this.getPosition();
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getCol() - from.getCol());

        // Must be exactly an L-shape
        boolean isL = (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
        if (!isL) return false;

        // Cannot land on a friendly piece
        Piece occupant = board.getPiece(to);
        return occupant == null || occupant.getColor() != this.getColor();
    }

    /**
     * Returns all eight possible L-shape destinations that are on the board
     * and not occupied by a friendly piece.
     *
     * @param board the current board state
     * @return list of candidate destination positions
     * @author Manish Bishwakarma
     */
    public List<Position> getPseudoLegalMoves(BoardModel board) {
        List<Position> moves = new ArrayList<>();
        Position from = this.getPosition();
        for (int[] off : OFFSETS) {
            Position to = new Position(from.getRow() + off[0], from.getCol() + off[1]);
            if (to.isValid() && isValid(board, to)) moves.add(to);
        }
        return moves;
    }
}