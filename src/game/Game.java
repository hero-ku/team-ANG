package game;

import board.Board;
import utils.Color;

import java.util.Scanner;

/**
 * Main game controller for the console chess game.
 * Responsibilities: owns the board and players, manages turn flow,
 * and handles the main game loop from start to end.
 */
public class Game {

    /** 
     *The chessboard for this game session
     */
    private final Board board;

    /**
     *The white and black players
     */
    private final Player whitePlayer;
    private final Player blackPlayer;

    /**
     *Color of the player whose turn it currently is
     */
    private Color currentTurn;

    /**
     *Scanner for reading player input from the console
     */
    private final Scanner scanner;

    /**
     *Whether the game loop is currently running
     */
    private boolean running;

    /**
     *Creates a new game with a fresh board, two players, and white to move first
     */
    public Game() {
        this.board = new Board();
        this.whitePlayer = new Player("White", Color.WHITE);
        this.blackPlayer = new Player("Black", Color.BLACK);
        this.currentTurn = Color.WHITE;
        this.scanner = new Scanner(System.in);
        this.running = false;
    }

    /**
     *Prints the welcome message and move format instructions before the game begins
     */
    public void start() {
        System.out.println("Console Chess Game");
        System.out.println("Move format: E2 E4");
        System.out.println("Type quit to exit.");
        System.out.println();
    }

    /**
     * Runs the main game loop.
     * Each iteration: displays the board, asks the current player for a move,
     * checks for check/checkmate on the opponent, then switches turns.
     * The loop ends when a player quits or checkmate is detected.
     */
    public void play() {
        start();
        while (true) {
            board.display();

            Player currentPlayer = currentTurn == Color.WHITE ? whitePlayer : blackPlayer;
            boolean moved = currentPlayer.makeMove(board, scanner);

            /**
             *Player typed "quit" or input ended
             */
            if (!moved) {
                end();
                break;
            }

            /**
             *Notify if the opponent is now in check
             */
            if (board.isCheck(currentTurn.opposite())) {
                System.out.println(currentTurn.opposite() + " is in check.");
            }

            /**
             *End the game if the opponent has no legal moves and is in check
             *
             */
            if (board.isCheckmate(currentTurn.opposite())) {
                board.display();
                System.out.println(currentTurn.opposite() + " is in checkmate.");
                end();
                break;
            }

            currentTurn = currentTurn.opposite();
        }
    }

    /**
     *Prints the game-over message when the game ends (quit or checkmate)
     */
    public void end() {
        System.out.println("Game over.");
    }
}
