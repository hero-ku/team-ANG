package game;

import board.Board;
import utils.Color;
import utils.Position;

import java.util.Scanner;

/**
 * Represents one of the two human players in the chess game.
 * Written by Manish Team ANG, CS 3354 Section 255
 */
public class Player {

    /** Display name shown in prompts (e.g. "White" or "Black"). */
    private final String name;

    /** This player's piece color. */
    private final Color color;

    /**
     * Creates a new Player.
     *
     * @param name  display name for this player (e.g. "White")
     * @param color piece color assigned to this player
     */
    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    // ------------------------------------------------------------------ //
    // Getters //
    // ------------------------------------------------------------------ //

    /**
     * Returns this player's display name.
     *
     * @return player name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns this player's piece color.
     *
     * @return player color
     */
    public Color getColor() {
        return color;
    }

    // ------------------------------------------------------------------ //
    // Move handling //
    // ------------------------------------------------------------------ //

    /**
     * Prompts this player to enter a move, then attempts to execute it.
     *
     */
    public boolean makeMove(Board board, Scanner scanner) {
        while (true) {
            System.out.print(name + "'s turn — enter move (e.g. E2 E4): ");
            String input = scanner.nextLine().trim();

            // --- Quit commands ---
            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                System.out.println(name + " has quit the game.");
                return false;
            }

            // --- Parse the move ---
            Position[] parsed = parseInput(input);
            if (parsed == null) {
                System.out.println("  Invalid format. Please use 'FROM TO' — example: E2 E4");
                continue;
            }

            Position from = parsed[0];
            Position to = parsed[1];

            // --- Attempt the move on the board ---
            boolean success = board.movePiece(from, to, color);
            if (!success) {
                System.out.println("  Illegal move. Try again.");
                continue;
            }

            // Move succeeded
            return true;
        }
    }

    // ------------------------------------------------------------------ //
    // Input parsing (private helpers) //
    // ------------------------------------------------------------------ //

    /**
     * Parses a raw input string such as {@code "E2 E4"} into two
     * {@link Position} objects.
     * 
     * @return a two-element array {@code [from, to]}, or {@code null} on error
     */
    private Position[] parseInput(String input) {
        // Normalise: uppercase, collapse multiple spaces
        String[] tokens = input.toUpperCase().trim().split("\\s+");

        if (tokens.length != 2) {
            return null;
        }

        Position from = parseSquare(tokens[0]);
        Position to = parseSquare(tokens[1]);

        if (from == null || to == null) {
            return null;
        }

        return new Position[] { from, to };
    }

    /**
     * Converts a single algebraic square label (e.g. {@code "E2"}) into a
     * {@link Position}.
     * 
     * @param label two-character square label (e.g. {@code "E2"})
     * @return the corresponding {@link Position}, or {@code null} if the label
     *         is malformed or out of range
     */
    private Position parseSquare(String label) {
        if (label == null || label.length() != 2) {
            return null;
        }

        char fileChar = label.charAt(0); // 'A'..'H'
        char rankChar = label.charAt(1); // '1'..'8'

        // Validate file letter
        if (fileChar < 'A' || fileChar > 'H') {
            return null;
        }

        // Validate rank digit
        if (rankChar < '1' || rankChar > '8') {
            return null;
        }

        int column = fileChar - 'A'; // A=0, B=1, ..., H=7
        int row = '8' - rankChar; // '8'→0, '7'→1, ..., '1'→7

        return new Position(row, column);
    }
}
