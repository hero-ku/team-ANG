package pieces;

import board.Board;
import utils.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Pawn chess piece.
 * Written by Manish — Team ANG, CS 3354 Section 255
 */
public class Pawn extends Piece {

    /**
     * Starting row for this pawn (used to decide whether a two-square
     * advance is allowed).
     * White pawns start on row 6; black pawns start on row 1.
     */
    private final int startingRow;

    /**
     * Creates a new Pawn.
     *
     * @param color    piece color (WHITE or BLACK)
     * @param position starting square
     */
    public Pawn(Color color, Position position) {
        super(color, position);
        // White starts at row 6, black starts at row 1
        this.startingRow = (color == Color.WHITE) ? 6 : 1;
    }

    /**
     * {@inheritDoc}
     *
     * White pawns move toward lower row numbers (up the board);
     * black pawns move toward higher row numbers (down the board).
     */
    @Override
    public List<Position> possibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();

        int row = getPosition().getRow();
        int col = getPosition().getColumn();

        // White moves "up" (row decreases); Black moves "down" (row increases)
        int direction = (getColor() == Color.WHITE) ? -1 : 1;

        // --- One square forward ---
        Position oneStep = new Position(row + direction, col);
        if (board.isWithinBounds(oneStep) && board.isEmpty(oneStep)) {
            moves.add(oneStep);

            // --- Two squares forward (only from starting row, only if path clear) ---
            if (row == startingRow) {
                Position twoSteps = new Position(row + 2 * direction, col);
                if (board.isWithinBounds(twoSteps) && board.isEmpty(twoSteps)) {
                    moves.add(twoSteps);
                }
            }
        }

        // --- Diagonal captures ---
        int[] captureColumns = { col - 1, col + 1 };
        for (int captureCol : captureColumns) {
            Position diagonal = new Position(row + direction, captureCol);
            if (board.isWithinBounds(diagonal) && board.isOpponentPiece(diagonal, getColor())) {
                moves.add(diagonal);
            }
        }

        return moves;
    }

    /**
     * Returns the console display code for this pawn.
     *
     * @return {@code "wp"} for white, {@code "bp"} for black
     */
    @Override
    public String getDisplayCode() {
        return (getColor() == Color.WHITE) ? "wp" : "bp";
    }
}
