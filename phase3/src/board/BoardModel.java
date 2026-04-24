package board;

import pieces.Piece;
import pieces.PieceColor;
import pieces.PieceType;
import position.Position;

/**
 * Holds the 8x8 board state. No Swing code here.
 * Teammates call movePiece() and getPiece() to interact with the board.
 *
 * Added for Feature 1 (Save/Load):
 *   placePiece(Piece, Position) – places an arbitrary piece on the board.
 *   clearCell(Position)         – removes any piece from a cell.
 *
 * Phase 2 addition (Manish Bishwakarma):
 *   undoMove() — reverses a previously applied move for the Undo feature.
 *
 * @author Gaurav Paneru
 */
public class BoardModel {

    private Piece[][] grid;

    public BoardModel() {
        grid = new Piece[8][8];
        setupInitialPositions();
    }

    /** Places all 32 pieces in standard starting positions. */
    public void setupInitialPositions() {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                grid[r][c] = null;

        PieceType[] backRank = {
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        };

        for (int col = 0; col < 8; col++) {
            grid[0][col] = new Piece(backRank[col], PieceColor.WHITE, new Position(0, col));
            grid[1][col] = new Piece(PieceType.PAWN,  PieceColor.WHITE, new Position(1, col));
            grid[7][col] = new Piece(backRank[col], PieceColor.BLACK, new Position(7, col));
            grid[6][col] = new Piece(PieceType.PAWN,  PieceColor.BLACK, new Position(6, col));
        }
    }

    /**
     * Returns the piece at the given position, or null if empty.
     */
    public Piece getPiece(Position position) {
        if (!position.isValid()) return null;
        return grid[position.getRow()][position.getCol()];
    }

    /**
     * Moves a piece from {@code from} to {@code to}. Returns any captured piece,
     * or null. No legality checking — caller is responsible for validation.
     */
    public Piece movePiece(Position from, Position to) {
        Piece moving   = grid[from.getRow()][from.getCol()];
        Piece captured = grid[to.getRow()][to.getCol()];
        grid[to.getRow()][to.getCol()]     = moving;
        grid[from.getRow()][from.getCol()] = null;
        if (moving != null) moving.setPosition(to);
        return captured;
    }

    /**
     * Reverses a previously applied move.
     * Restores the moved piece to {@code from} and puts any captured piece
     * back on {@code to}. Called by ChessWindow when the Undo button is pressed.
     *
     * @param from          the square the piece was on before the move
     * @param to            the square the piece landed on
     * @param capturedPiece the piece that was on {@code to} before the move
     *                      (may be null if the square was empty)
     * @author Manish Bishwakarma
     */
    public void undoMove(Position from, Position to, Piece capturedPiece) {
        Piece movedPiece = grid[to.getRow()][to.getCol()];
        grid[from.getRow()][from.getCol()] = movedPiece;
        grid[to.getRow()][to.getCol()]     = capturedPiece;
        if (movedPiece    != null) movedPiece.setPosition(from);
        if (capturedPiece != null) capturedPiece.setPosition(to);
    }

    /**
     * Places the given piece directly on the board at the given position.
     * Used by the Load Game feature to restore a saved state.
     *
     * @param piece    the piece to place
     * @param position where to place it
     */
    public void placePiece(Piece piece, Position position) {
        if (!position.isValid()) return;
        grid[position.getRow()][position.getCol()] = piece;
        if (piece != null) piece.setPosition(position);
    }

    /**
     * Removes any piece from the given cell (sets it to null).
     * Used by the Load Game feature to wipe the board before restoring.
     *
     * @param position the cell to clear
     */
    public void clearCell(Position position) {
        if (!position.isValid()) return;
        grid[position.getRow()][position.getCol()] = null;
    }

    /** Returns whether a square is empty. */
    public boolean isEmpty(Position position) {
        return getPiece(position) == null;
    }

    /** Resets the board to the starting position. */
    public void reset() {
        setupInitialPositions();
    }
}