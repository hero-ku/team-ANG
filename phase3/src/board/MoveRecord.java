package board;

import pieces.Piece;
import position.Position;

/**
 * Immutable snapshot of one half-move (ply). Stored in a stack inside
 * ChessWindow so the Undo feature can reverse any move, including captures.
 *
 * @author Manish Bishwakarma
 */
public class MoveRecord {

    private final Position from;
    private final Position to;
    private final Piece    movedPiece;
    private final Piece    capturedPiece; // null when the destination was empty

    public MoveRecord(Position from, Position to,
                      Piece movedPiece, Piece capturedPiece) {
        this.from          = from;
        this.to            = to;
        this.movedPiece    = movedPiece;
        this.capturedPiece = capturedPiece;
    }

    public Position getFrom()          { return from;          }
    public Position getTo()            { return to;            }
    public Piece    getMovedPiece()    { return movedPiece;    }
    public Piece    getCapturedPiece() { return capturedPiece; }

    /**
     * Human-readable entry used in the history panel's move list.
     * Example: "WHITE KNIGHT  B1 → C3  ✕ PAWN"
     */
    @Override
    public String toString() {
        String base = movedPiece.getColor() + " " + movedPiece.getType()
                + "  " + from + " \u2192 " + to;
        if (capturedPiece != null)
            base += "  \u2715 " + capturedPiece.getType();
        return base;
    }
}