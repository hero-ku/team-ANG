package piece.pieces;

import board.Board;
import piece.Piece;
import utils.Color;
import utils.Position;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Queen extends Piece {
    public Queen(Color color, Position position) {
        super(color, position);
    }

    @Override
    public String getPieceLetter() {
        return "Q";
    }

    @Override
    protected Collection<Position> candidateMoves(Board board) {
        List<Position> candidates = new ArrayList<>();

        int[][] directions = {
                {-1,  0}, // north
                { 1,  0}, // south
                { 0, -1}, // west
                { 0,  1}, // east
                {-1, -1}, // north-west
                {-1,  1}, // north-east
                { 1, -1}, // south-west
                { 1,  1}  // south-east
        };

        for (int[] direction : directions) {
            int row    = position.row()    + direction[0];
            int column = position.column() + direction[1];

            while (board.isWithinBounds(new Position(row, column))) {
                Position candidate = new Position(row, column);

                candidates.add(candidate);
                row    += direction[0];
                column += direction[1];
            }
        }

        return candidates;
    }
}
