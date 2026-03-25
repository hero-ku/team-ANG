package piece.pieces;

import board.Board;
import piece.Piece;
import utils.Color;
import utils.Position;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Pawn extends Piece {
    public Pawn(Color color, Position position) {
        super(color, position);
    }

    @Override
    protected Collection<Position> candidateMoves(Board board) {
        Position position = this.getPosition();
        List<Position> candidates = new ArrayList<>(List.of(
                position.add(new Position(this.getDirection(), 0))
        ));

        if (this.getColor() == Color.WHITE && position.row() == 1 || this.getColor() == Color.BLACK && position.row() == 6) {
            candidates.add(
                    position.add(new Position(this.getDirection() * 2, 0))
            );
        }

        return candidates;
    }
}
