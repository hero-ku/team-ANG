package board;

import pieces.*;
import utils.Position;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
  Represents the 8x8 chessboard and captured pieces.
*/
public class Board {
    /*
      8x8 grid chessboard squares.
     */
    private final Piece[][] squares;

    //Pieces that have been captured durig the game
    private final List<Piece> capturedPieces;

    /*
      Creates an empty board and initializes the starting position.
     */
    public Board() {
        this.squares = new Piece[8][8];
        this.capturedPieces = new ArrayList<>();
        initialize();
    }

    /*
      Clears the board and places all pieces in their starting squares.

     */
    public final void initialize() {
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                squares[row][column] = null;
            }
        }
        capturedPieces.clear();

        // Black back rank
        squares[0][0] = new Rook(Color.BLACK, new Position(0, 0));
        squares[0][1] = new Knight(Color.BLACK, new Position(0, 1));
        squares[0][2] = new Bishop(Color.BLACK, new Position(0, 2));
        squares[0][3] = new Queen(Color.BLACK, new Position(0, 3));
        squares[0][4] = new King(Color.BLACK, new Position(0, 4));
        squares[0][5] = new Bishop(Color.BLACK, new Position(0, 5));
        squares[0][6] = new Knight(Color.BLACK, new Position(0, 6));
        squares[0][7] = new Rook(Color.BLACK, new Position(0, 7));

        // Black pawns
        for (int column = 0; column < 8; column++) {
            squares[1][column] = new Pawn(Color.BLACK, new Position(1, column));
        }

        // White pawns
        for (int column = 0; column < 8; column++) {
            squares[6][column] = new Pawn(Color.WHITE, new Position(6, column));
        }

        // White back rank
        squares[7][0] = new Rook(Color.WHITE, new Position(7, 0));
        squares[7][1] = new Knight(Color.WHITE, new Position(7, 1));
        squares[7][2] = new Bishop(Color.WHITE, new Position(7, 2));
        squares[7][3] = new Queen(Color.WHITE, new Position(7, 3));
        squares[7][4] = new King(Color.WHITE, new Position(7, 4));
        squares[7][5] = new Bishop(Color.WHITE, new Position(7, 5));
        squares[7][6] = new Knight(Color.WHITE, new Position(7, 6));
        squares[7][7] = new Rook(Color.WHITE, new Position(7, 7));
    }

  /*
    Returns the piece at specified position, or null if square is empty.
   */
    public Piece getPiece(Position position) {
        if (!isWithinBounds(position)) {
            return null;
        }
        return squares[position.getRow()][position.getColumn()];
    }

      //Checks whether a position is on the board.
    public boolean isWithinBounds(Position position) {
        return position != null
                && position.getRow() >= 0 && position.getRow() < 8
                && position.getColumn() >= 0 && position.getColumn() < 8;
    }

   //checks whether square is empty
    public boolean isEmpty(Position position) {
        return isWithinBounds(position) && getPiece(position) == null;
    }

    /**
     * Checks whether a square holds an opponent's piece.
     *
     * @param position square to inspect
     * @param color player color
     * @return true if the square holds an opponent piece
     */
    public boolean isOpponentPiece(Position position, Color color) {
        Piece piece = getPiece(position);
        return piece != null && piece.getColor() != color;
    }

    //returns the captured pieces
    public List<Piece> getCapturedPieces() {
        return Collections.unmodifiableList(capturedPieces);
    }

    /**
     * Moves a piece from one square to another if the move is legal.
     *
     * @param from starting square
     * @param to ending square
     * @param turn current player color
     * @return true if the move succeeded
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

        squares[to.getRow()][to.getColumn()] = piece;
        squares[from.getRow()][from.getColumn()] = null;
        piece.move(to);
        return true;
    }

    //Displays current board
    // Created with AI help
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
     *
     * @param color color to inspect
     * @return always false in Phase 1
     */
    public boolean isCheck(Color color) {
        return false;
    }

    /**
     *
     * @param color color to inspect
     * @return always false in Phase 1
     */
    public boolean isCheckmate(Color color) {
        return false;
    }
}