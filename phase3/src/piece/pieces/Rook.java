package piece.pieces;

import board.BoardModel;
import piece.Piece;
import piece.PieceColor;
import piece.PieceType;
import position.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Rook piece — slides horizontally or vertically any number of squares.
 *
 * Phase 3 (Manish Bishwakarma):
 *   isValid() and getPseudoLegalMoves() implemented.
 */
public class Rook extends Piece {

    private static final int[][] DIRECTIONS = {{1,0},{-1,0},{0,1},{0,-1}};

    public Rook(PieceColor color, Position position) {
        super(PieceType.ROOK, color, position);
    }

    /**
     * A Rook move is valid when it stays on the same row or column,
     * no piece blocks the path, and the destination is empty or enemy.
     */
    @Override
    public boolean isValid(BoardModel board, Position to) {
        Position from = this.getPosition();
        int rowDiff = to.getRow() - from.getRow();
        int colDiff = to.getCol() - from.getCol();

        // Must move along one axis only
        if (rowDiff != 0 && colDiff != 0) return false;
        if (rowDiff == 0 && colDiff == 0) return false;

        // Walk towards destination, checking for blockers
        int rowStep = Integer.signum(rowDiff);
        int colStep = Integer.signum(colDiff);
        int r = from.getRow() + rowStep;
        int c = from.getCol() + colStep;
        while (r != to.getRow() || c != to.getCol()) {
            if (!board.isEmpty(new Position(r, c))) return false;
            r += rowStep;
            c += colStep;
        }

        // Destination must be empty or hold an enemy
        Piece occupant = board.getPiece(to);
        return occupant == null || occupant.getColor() != this.getColor();
    }

    /**
     * Slides in all four straight directions until blocked or board edge.
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
                    if (occupant.getColor() != this.getColor()) moves.add(to); // capture
                    break; // blocked either way
                }
                r += dir[0];
                c += dir[1];
            }
        }
        return moves;
    }
}
