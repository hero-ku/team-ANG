package utils;

/**
 * Helper methods for reading chess moves from the console.
 */
public final class ChessNotation {
    private ChessNotation() {
    }

    /**
     * Checks whether a move is in the form "E2 E4".
     *
     * @param input user input
     * @return true if the format is valid
     */
    public static boolean isValidMoveFormat(String input) {
        return input != null && input.trim().matches("(?i)^[A-H][1-8]\\s+[A-H][1-8]$");
    }

    /**
     * Parses a move like "E2 E4" into two Position objects.
     *
     * @param input user input
     * @return an array containing from-position and to-position
     */
    public static Position[] parseMove(String input) {
        if (!isValidMoveFormat(input)) {
            throw new IllegalArgumentException("Use format like E2 E4.");
        }

        String[] parts = input.trim().split("\\s+");
        return new Position[] {
                Position.fromChessSquare(parts[0]),
                Position.fromChessSquare(parts[1])
        };
    }
}

 