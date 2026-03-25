package piece.pieces;

import board.Board;
import piece.Piece;
import utils.Color;
import utils.Position;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {
    public King(Color color, Position position) {
        super(color, position);
    }

    @Override
    public String getPieceLetter() {
        return "K";
    }

    @Override
    protected List<Position> candidateMoves(Board board) {
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
            Position candidate = new Position(
                    position.row()    + direction[0],
                    position.column() + direction[1]
            );

            candidates.add(candidate);
        }

        return candidates;
    }
}
