package piece;

import board.BoardModel;
import position.Position;

/**
 * Represents a chess piece with a type, color, and position.
 * Rendering uses Unicode glyphs — no image files needed.
 *
 * @author Gaurav Paneru
 */
public abstract class Piece {

    private final PieceType type;
    private final PieceColor color;
    private Position position;

    public Piece(PieceType type, PieceColor color, Position position) {
        this.type = type;
        this.color = color;
        this.position = position;
    }

    public PieceType getType()    { return type; }
    public PieceColor getColor()  { return color; }
    public Position getPosition() { return position; }

    public void setPosition(Position p) { this.position = p; }
    public abstract boolean isValid(BoardModel board, Position position);

    /**
     * Returns the Unicode chess glyph for this piece.
     * White: ♔♕♖♗♘♙  Black: ♚♛♜♝♞♟
     */
    public String getUnicodeSymbol() {
        if (color == PieceColor.WHITE) {
            switch (type) {
                case KING:   return "\u2654";
                case QUEEN:  return "\u2655";
                case ROOK:   return "\u2656";
                case BISHOP: return "\u2657";
                case KNIGHT: return "\u2658";
                case PAWN:   return "\u2659";
            }
        } else {
            switch (type) {
                case KING:   return "\u265A";
                case QUEEN:  return "\u265B";
                case ROOK:   return "\u265C";
                case BISHOP: return "\u265D";
                case KNIGHT: return "\u265E";
                case PAWN:   return "\u265F";
            }
        }
        return "?";
    }

    @Override
    public String toString() {
        return color + " " + type + " at " + position;
    }
}