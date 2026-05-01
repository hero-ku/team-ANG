
package piece.pieces;

import board.BoardModel;
import piece.Piece;
import piece.PieceColor;
import piece.PieceType;
import position.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Queen piece — combines Rook (straight) and Bishop (diagonal) movement.
 * Previous implementation incorrectly returned {@code true} for every move.
 *
 * Phase 3 (Manish Bishwakarma):
 *   isValid() and getPseudoLegalMoves() properly implemented.
 */
public class Queen extends Piece {

    private static final int[][] DIRECTIONS = {
            {1,0},{-1,0},{0,1},{0,-1},   // straight (rook)
            {1,1},{1,-1},{-1,1},{-1,-1}  // diagonal (bishop)
    };

    public Queen(PieceColor color, Position position) {
        super(PieceType.QUEEN, color, position);
    }

    /**
     * A Queen move is valid when it moves along a straight line or diagonal
     * with no blocking pieces, landing on an empty or enemy square.
     */
    @Override
    public boolean isValid(BoardModel board, Position to) {
        Position from = this.getPosition();
        int rowDiff = to.getRow() - from.getRow();
        int colDiff = to.getCol() - from.getCol();

        if (rowDiff == 0 && colDiff == 0) return false;

        // Must be straight or diagonal
        boolean straight = (rowDiff == 0 || colDiff == 0);
        boolean diagonal = (Math.abs(rowDiff) == Math.abs(colDiff));
        if (!straight && !diagonal) return false;

        // Check path for blockers
        int rowStep = Integer.signum(rowDiff);
        int colStep = Integer.signum(colDiff);
        int r = from.getRow() + rowStep;
        int c = from.getCol() + colStep;
        while (r != to.getRow() || c != to.getCol()) {
            if (!board.isEmpty(new Position(r, c))) return false;
            r += rowStep;
            c += colStep;
        }

        Piece occupant = board.getPiece(to);
        return occupant == null || occupant.getColor() != this.getColor();
    }

    /**
     * Slides in all eight directions until blocked or board edge.
     *
     * @param board the current board state
     * @return list of reachable destination positions
     * @author Manish Bishwakarma
     */
    public List<Position> getPseudoLegalMoves(BoardModel board) {
        List<Position> moves = new ArrayList<>();
        Position from = this.getPosition();
        for (int[] dir : DIRECTIONS) {
            int r = from.getRow() + dir[0];
            int c = from.getCol() + dir[1];
            while (true) {
                Position to = new Position(r, c);
                if (!to.isValid()) break;
                Piece occupant = board.getPiece(to);
                if (occupant == null) {
                    moves.add(to);
                } else {
                    if (occupant.getColor() != this.getColor()) moves.add(to);
                    break;
                }
                r += dir[0];
                c += dir[1];
            }
        }
        return moves;
    }
}