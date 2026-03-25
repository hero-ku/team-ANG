package piece.pieces;

import board.Board;
import piece.Piece;
import utils.Color;
import utils.Position;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Knight extends Piece {
    public Knight(Color color, Position position) {
        super(color, position);
    }

    @Override
    protected Collection<Position> candidateMoves(Board board) {
        List<Position> candidates = new ArrayList<>();

        int[][] directions = {
                {-2, -1}, {-2,  1},
                {-1, -2}, {-1,  2},
                { 1, -2}, { 1,  2},
                { 2, -1}, { 2,  1}
        };

        for (int[] direction : directions) {
            Position candidate = new Position(
                    position.row()    + direction[0],
                    position.column() + direction[1]
            );

            if (board.isOpponentPiece(candidate, color) || board.isEmpty(candidate)) {
                candidates.add(candidate);
            }
        }

        return candidates;
    }
}
