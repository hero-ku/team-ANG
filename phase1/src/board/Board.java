package board;

import piece.*;
import piece.pieces.*;
import utils.Color;
import utils.Position;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the 8x8 chessboard and tracks captured pieces.
 * Responsibilities: holds the grid of Piece objects, enforces bounds checks,
 * applies moves, and renders the board to the CLI.
 * Check and checkmate detection are stubbed out for Phase 1.
 */
public class Board {

    /**
     *8x8 grid; null entries represent empty squares
     */
    private final Piece[][] squares;

    /**
     *Pieces removed from play, in the order they were captured
     */
    private final List<Piece> capturedPieces;

    /**
     *Creates a new board and places all pieces in their standard starting positions
     */
    public Board() {
        this.squares = new Piece[8][8];
        this.capturedPieces = new ArrayList<>();
        initialize();
    }

    /**
     * Resets the board to the standard chess starting position.
     * Clears all squares and the captured-piece history, then places
     * the back ranks (rows 0 and 7) and pawn ranks (rows 1 and 6).
     * Called automatically by the constructor; can be called again to restart.
     */
    public final void initialize() {
        // Clear every square
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                squares[row][column] = null;
            }
        }
        capturedPieces.clear();

        /**
         *Black back rank (row 0)
         */
        squares[0][0] = new Rook(Color.BLACK, new Position(0, 0));
        squares[0][1] = new Knight(Color.BLACK, new Position(0, 1));
        squares[0][2] = new Bishop(Color.BLACK, new Position(0, 2));
        squares[0][3] = new Queen(Color.BLACK, new Position(0, 3));
        squares[0][4] = new King(Color.BLACK, new Position(0, 4));
        squares[0][5] = new Bishop(Color.BLACK, new Position(0, 5));
        squares[0][6] = new Knight(Color.BLACK, new Position(0, 6));
        squares[0][7] = new Rook(Color.BLACK, new Position(0, 7));

        /**
         *Black pawns (row 1)
         */
        for (int column = 0; column < 8; column++) {
            squares[1][column] = new Pawn(Color.BLACK, new Position(1, column));
        }

        /**
         *White pawns (row 6)
         */
        for (int column = 0; column < 8; column++) {
            squares[6][column] = new Pawn(Color.WHITE, new Position(6, column));
        }

        /**
         *White back rank (row 7)
         */
        squares[7][0] = new Rook(Color.WHITE, new Position(7, 0));
        squares[7][1] = new Knight(Color.WHITE, new Position(7, 1));
        squares[7][2] = new Bishop(Color.WHITE, new Position(7, 2));
        squares[7][3] = new Queen(Color.WHITE, new Position(7, 3));
        squares[7][4] = new King(Color.WHITE, new Position(7, 4));
        squares[7][5] = new Bishop(Color.WHITE, new Position(7, 5));
        squares[7][6] = new Knight(Color.WHITE, new Position(7, 6));
        squares[7][7] = new Rook(Color.WHITE, new Position(7, 7));
    }

    /**
     *Returns the piece at the given position, or null if the square is empty or out of bounds
     */
    public Piece getPiece(Position position) {
        if (!isWithinBounds(position)) {
            return null;
        }
        return squares[position.row()][position.column()];
    }

    /**
     *Returns true if the position falls within the 8x8 grid (row and column both in [0, 7])
     */
    public boolean isWithinBounds(Position position) {
        return position != null
                && position.row() >= 0 && position.row() < 8
                && position.column() >= 0 && position.column() < 8;
    }

    /**
     *Returns true if the position is on the board and holds no piece
     */
    public boolean isEmpty(Position position) {
        return isWithinBounds(position) && getPiece(position) == null;
    }

    /**
     *Returns true if the square holds a piece belonging to the opponent of the given color
     */
    public boolean isOpponentPiece(Position position, Color color) {
        Piece piece = getPiece(position);
        return piece != null && piece.getColor() != color;
    }

    /**
     * Returns an unmodifiable view of captured pieces in the order they were captured.
     *
     * @return The unmodifiable list.
     * */
    public List<Piece> getCapturedPieces() {
        return Collections.unmodifiableList(capturedPieces);
    }

    /**
     * Attempts to move a piece from 'from' to 'to' for the given player color.
     * The move is rejected if:
     *   - either position is out of bounds
     *   - the source square is empty or holds an opponent's piece
     *   - the destination is not in the piece's legal-move list
     * If a capture occurs, the displaced piece is added to capturedPieces.
     * Returns true if the move was executed successfully.
     */
    public boolean movePiece(Position from, Position to, Color turn) {
        if (!isWithinBounds(from) || !isWithinBounds(to)) {
            return false;
        }

        Piece piece = getPiece(from);
        if (piece == null || piece.getColor() != turn) {
            return false;
        }

        if (!piece.possibleMoves(this).contains(to)) {
            return false;
        }

        Piece destinationPiece = getPiece(to);
        if (destinationPiece != null) {
            capturedPieces.add(destinationPiece);
        }

        squares[to.row()][to.column()] = piece;
        squares[from.row()][from.column()] = null;
        piece.move(to);
        return true;
    }

    /**
     * Prints the current board to standard output with file labels (A-H)
     * and rank labels (1-8) on all four sides.
     * Empty squares are shown as "##"; occupied squares use the piece's display code.
     *
     * AI: [Prompt] asked for a readable CLI board layout with rank and file labels;
     *     [Adopt] used the printf-based column loop and rank-inversion (rank = 8 - row);
     *     [Change] replaced the suggested "." empty-square symbol with "##" to match project style.
     */
    public void display() {
        System.out.println();
        System.out.print("    ");
        for (char file = 'A'; file <= 'H'; file++) {
            System.out.printf("%-3s", file);
        }
        System.out.println();

        for (int row = 0; row < 8; row++) {
            int rank = 8 - row;
            System.out.printf("%2d  ", rank);

            for (int column = 0; column < 8; column++) {
                Piece piece = squares[row][column];
                String cell = piece == null ? "##" : piece.getDisplayCode();
                System.out.printf("%-3s", cell);
            }

            System.out.printf(" %2d%n", rank);
        }

        System.out.print("    ");
        for (char file = 'A'; file <= 'H'; file++) {
            System.out.printf("%-3s", file);
        }
        System.out.println();
        System.out.println();
    }

    /**
     * Phase 1 stub - always returns false.
     * Full check detection (scanning opponent attack lines) will be added in Phase 2.
     */
    public boolean isCheck(Color color) {
        return false;
    }

    /**
     * Phase 1 stub - always returns false.
     * Full checkmate detection depends on isCheck() and will be added in Phase 2.
     */
    public boolean isCheckmate(Color color) {
        return false;
    }
}
