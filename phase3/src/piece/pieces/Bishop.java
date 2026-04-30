cdpackage piece.pieces;

import board.BoardModel;
import piece.Piece;
import piece.PieceColor;
import piece.PieceType;
import position.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Bishop piece — slides diagonally any number of squares.
 * isValid() was already correct from Phase 1.
 *
 * Phase 3 (Manish Bishwakarma):
 *   Added getPseudoLegalMoves() to support check/checkmate detection.
 */
public class Bishop extends Piece {

    private static final int[][] DIRECTIONS = {{1,1},{1,-1},{-1,1},{-1,-1}};

    public Bishop(PieceColor color, Position position) {
        super(PieceType.BISHOP, color, position);
    }

    /**
     * A Bishop move is valid when the destination is on the same diagonal,
     * the path between is clear, and the destination is empty or enemy.
     */
    @Override
    public boolean isValid(BoardModel board, Position to) {
        Position from = this.getPosition();
        int rowDiff = to.getRow() - from.getRow();
        int colDiff = to.getCol() - from.getCol();

        if (Math.abs(rowDiff) != Math.abs(colDiff) || rowDiff == 0) return false;

        int rowStep = (rowDiff > 0) ? 1 : -1;
        int colStep = (colDiff > 0) ? 1 : -1;
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
     * Slides in all four diagonal directions until blocked or board edge.
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
