package game;

import board.Board;
import utils.Color;

import java.util.Scanner;


 // Main game controller for the console chess game.

public class Game {

    //chessboard
    private final Board board;

  //white player
    private final Player whitePlayer;

    //black player
    private final Player blackPlayer;

    //current turn to play
    private Color currentTurn;

    //input scanner
    private final Scanner scanner;

    //whether the game is still ongoing
    private boolean running;

   //creates a new game
    public Game() {
        this.board = new Board();
        this.whitePlayer = new Player("White", Color.WHITE);
        this.blackPlayer = new Player("Black", Color.BLACK);
        this.currentTurn = Color.WHITE;
        this.scanner = new Scanner(System.in);
        this.running = false;
    }

    //prints the welcome message for the new game
    public void start() {
        System.out.println("Console Chess Game");
        System.out.println("Move format: E2 E4");
        System.out.println("Type quit to exit.");
        System.out.println();
    }

    //Main game
    public void play() {
        start();

        while (true) {
            board.display();

            Player currentPlayer = currentTurn == Color.WHITE ? whitePlayer : blackPlayer;
            boolean moved = currentPlayer.makeMove(board, scanner);

            if (!moved) {
                end();
                break;
            }

            //checks if current player is in check
            if (board.isCheck(currentTurn.opposite())) {
                System.out.println(currentTurn.opposite() + " is in check.");
            }

            //check for checkmate
            if (board.isCheckmate(currentTurn.opposite())) {
                board.display();
                System.out.println(currentTurn.opposite() + " is in checkmate.");
                end();
                break;
            }

            currentTurn = currentTurn.opposite();
        }
    }

    //ends the game
    public void end() {
        System.out.println("Game over.");
    }
}