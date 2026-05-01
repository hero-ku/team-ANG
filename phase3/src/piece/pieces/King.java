package piece.pieces;

import board.BoardModel;
import piece.Piece;
import piece.PieceColor;
import piece.PieceType;
import position.Position;

/**
 * King chess piece.
 *
 * Movement rules:
 *   • Moves exactly one square in any of the eight directions
 *     (horizontal, vertical, or diagonal).
 *   • Cannot move to a square occupied by a friendly piece.
 *   • Cannot move off the board (isValid() on Position handles this).
 *
 * Note: check/checkmate detection is handled separately by the teammate
 * responsible for that feature; isValid() here enforces only movement geometry.
 *
 * @author Nischal Rimal
 */
public class King extends Piece {

    public King(PieceColor color, Position position) {
        super(PieceType.KING, color, position);
    }

    /**
     * Returns true if moving this King to {@code to} is geometrically legal.
     *
     * @param board the current board state (used to check occupancy)
     * @param to    the destination square
     * @return true if the move is valid
     */
    @Override
    public boolean isValid(BoardModel board, Position to) {
        // Destination must be on the board
        if (!to.isValid()) return false;

        Position from = getPosition();

        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getCol() - from.getCol());

        // King moves exactly one square in any direction
        // rowDiff and colDiff must both be 0 or 1, but not both 0 (staying in place)
        if (rowDiff > 1 || colDiff > 1) return false;
        if (rowDiff == 0 && colDiff == 0) return false;

        // Cannot capture a friendly piece
        Piece target = board.getPiece(to);
        if (target != null && target.getColor() == getColor()) return false;

        return true;
    }
}