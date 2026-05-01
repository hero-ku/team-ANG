
package piece.pieces;

import board.BoardModel;
import piece.Piece;
import piece.PieceColor;
import piece.PieceType;
import position.Position;

/**
 * Queen chess piece.
 *
 * Movement rules:
 *   • Moves any number of squares in any of the eight directions
 *     (horizontal, vertical, or diagonal) — combining Rook and Bishop movement.
 *   • Cannot jump over pieces; the path between from and to must be clear.
 *   • Cannot move to a square occupied by a friendly piece.
 *   • Cannot stay on the same square.
 *
 * @author Nischal Rimal
 */
public class Queen extends Piece {

    public Queen(PieceColor color, Position position) {
        super(PieceType.QUEEN, color, position);
    }

    /**
     * Returns true if moving this Queen to {@code to} is geometrically legal.
     *
     * @param board the current board state (used to check path and occupancy)
     * @param to    the destination square
     * @return true if the move is valid
     */
    @Override
    public boolean isValid(BoardModel board, Position to) {
        // Destination must be on the board
        if (!to.isValid()) return false;

        Position from = getPosition();

        int rowDiff = to.getRow() - from.getRow();
        int colDiff = to.getCol() - from.getCol();

        // Must actually move somewhere
        if (rowDiff == 0 && colDiff == 0) return false;

        // The Queen moves along a straight line:
        // horizontal  → rowDiff == 0
        // vertical    → colDiff == 0
        // diagonal    → |rowDiff| == |colDiff|
        boolean horizontal = (rowDiff == 0);
        boolean vertical   = (colDiff == 0);
        boolean diagonal   = (Math.abs(rowDiff) == Math.abs(colDiff));

        if (!horizontal && !vertical && !diagonal) return false;

        // Step direction: normalise rowDiff and colDiff to -1, 0, or +1
        int rowStep = Integer.signum(rowDiff);
        int colStep = Integer.signum(colDiff);

        // Walk from the square just after 'from' up to (but not including) 'to'.
        // Every intermediate square must be empty.
        int currentRow = from.getRow() + rowStep;
        int currentCol = from.getCol() + colStep;

        while (currentRow != to.getRow() || currentCol != to.getCol()) {
            Position intermediate = new Position(currentRow, currentCol);
            if (board.getPiece(intermediate) != null) {
                // A piece is blocking the path
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        // Cannot capture a friendly piece on the destination
        Piece target = board.getPiece(to);
        if (target != null && target.getColor() == getColor()) return false;

        return true;
    }
}