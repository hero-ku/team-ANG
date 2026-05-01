package board;

import piece.Piece;
import piece.PieceColor;
import piece.PieceFactory;
import piece.PieceType;
import piece.pieces.*;
import position.Position;

import java.util.ArrayList;
import java.util.List;

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
 * Phase 3 addition (Manish Bishwakarma):
 *   getPseudoLegalMovesFor(Piece) — dispatches to each piece's own method.
 *   isInCheck(PieceColor)         — true if that color's King is attacked.
 *   getLegalMovesFrom(Position)   — filters pseudo-legal moves by simulating
 *                                   each move and verifying the king is safe.
 *   isCheckmate(PieceColor)       — true if in check with zero legal moves.
 *   isStalemate(PieceColor)       — true if NOT in check with zero legal moves.
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
            grid[0][col] = PieceFactory.createPiece(backRank[col], PieceColor.WHITE, new Position(0, col));
            grid[1][col] = new Pawn(PieceColor.WHITE, new Position(1, col));
            grid[7][col] = PieceFactory.createPiece(backRank[col], PieceColor.BLACK, new Position(7, col));
            grid[6][col] = new Pawn(PieceColor.BLACK, new Position(6, col));
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
     */
    public void placePiece(Piece piece, Position position) {
        if (!position.isValid()) return;
        grid[position.getRow()][position.getCol()] = piece;
        if (piece != null) piece.setPosition(position);
    }

    /**
     * Removes any piece from the given cell (sets it to null).
     * Used by the Load Game feature to wipe the board before restoring.
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

    // -----------------------------------------------------------------------
    //  Check & Checkmate (Phase 3 — Manish Bishwakarma)
    // -----------------------------------------------------------------------

    /**
     * Dispatches to each concrete piece class to collect its pseudo-legal
     * moves — squares it could reach ignoring whether the king ends up in check.
     *
     * @param piece the piece whose moves to generate
     * @return list of candidate destination squares
     * @author Manish Bishwakarma
     */
    public List<Position> getPseudoLegalMovesFor(Piece piece) {
        if (piece instanceof King)   return ((King)   piece).getPseudoLegalMoves(this);
        if (piece instanceof Queen)  return ((Queen)  piece).getPseudoLegalMoves(this);
        if (piece instanceof Rook)   return ((Rook)   piece).getPseudoLegalMoves(this);
        if (piece instanceof Bishop) return ((Bishop) piece).getPseudoLegalMoves(this);
        if (piece instanceof Knight) return ((Knight) piece).getPseudoLegalMoves(this);
        if (piece instanceof Pawn)   return ((Pawn)   piece).getPseudoLegalMoves(this);
        return new ArrayList<>();
    }

    /**
     * Returns true if the given color's King is currently attacked by any
     * enemy piece.
     *
     * Algorithm: find the King's square, then check every enemy piece to see
     * if any of its pseudo-legal moves lands on that square.
     *
     * @param color the side to check
     * @return true when that side's King is in check
     * @author Manish Bishwakarma
     */
    public boolean isInCheck(PieceColor color) {
        // Locate the King
        Position kingPos = null;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p != null && p.getType() == PieceType.KING && p.getColor() == color) {
                    kingPos = new Position(r, c);
                    break;
                }
            }
            if (kingPos != null) break;
        }
        if (kingPos == null) return false; // King already captured (endgame handled elsewhere)

        // Check if any enemy piece can reach the King's square
        PieceColor enemy = color.opposite();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p == null || p.getColor() != enemy) continue;
                for (Position dest : getPseudoLegalMovesFor(p)) {
                    if (dest.equals(kingPos)) return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the list of fully legal moves from a given square — that is,
     * pseudo-legal moves that do NOT leave the moving side's King in check.
     *
     * For each candidate move this method temporarily applies it on the board,
     * calls isInCheck(), and then immediately reverses it. No permanent state
     * is changed.
     *
     * @param from the square whose piece we are generating moves for
     * @return list of safe destination squares (may be empty)
     * @author Manish Bishwakarma
     */
    public List<Position> getLegalMovesFrom(Position from) {
        Piece piece = getPiece(from);
        if (piece == null) return new ArrayList<>();

        List<Position> legal = new ArrayList<>();
        for (Position to : getPseudoLegalMovesFor(piece)) {
            // Simulate the move
            Piece captured = movePiece(from, to);
            boolean safe   = !isInCheck(piece.getColor());
            // Reverse the simulation
            undoMove(from, to, captured);

            if (safe) legal.add(to);
        }
        return legal;
    }

    /**
     * Returns true when the given color is in checkmate:
     * they are currently in check AND every possible move still leaves
     * their King in check (i.e., no legal move escapes).
     *
     * @param color the side to evaluate
     * @return true if that side is checkmated
     * @author Manish Bishwakarma
     */
    public boolean isCheckmate(PieceColor color) {
        if (!isInCheck(color)) return false;
        return hasNoLegalMoves(color);
    }

    /**
     * Returns true when the given color is in stalemate:
     * they are NOT in check but have no legal moves available.
     *
     * @param color the side to evaluate
     * @return true if that side is stalemated
     * @author Manish Bishwakarma
     */
    public boolean isStalemate(PieceColor color) {
        if (isInCheck(color)) return false;
        return hasNoLegalMoves(color);
    }

    /**
     * Helper: returns true if the given color has zero legal moves on the board.
     *
     * @param color the side to check
     * @return true when no legal move is available
     * @author Manish Bishwakarma
     */
    private boolean hasNoLegalMoves(PieceColor color) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p != null && p.getColor() == color) {
                    if (!getLegalMovesFrom(new Position(r, c)).isEmpty()) return false;
                }
            }
        }
        return true;
    }
}
