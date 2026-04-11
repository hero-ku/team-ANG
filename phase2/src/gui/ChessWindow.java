package gui;

import board.BoardModel;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window. Extends JFrame following the professor's
 * "Using Inheritance to Customize Frames" pattern.
 * Teammates access the board via getBoardPanel() and getBoardModel().
 *
 * @author Gaurav Paneru
 */
public class ChessWindow extends JFrame {

    private final BoardModel boardModel;
    private final ChessBoardPanel boardPanel;

    public ChessWindow() {
        super("Chess Game — Phase 2");
        boardModel = new BoardModel();
        boardPanel = new ChessBoardPanel(boardModel);

        setLayout(new BorderLayout());
        add(boardPanel, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private JLabel buildStatusBar() {
        JLabel bar = new JLabel("  White's turn", SwingConstants.LEFT);
        bar.setFont(new Font("SansSerif", Font.PLAIN, 13));
        bar.setOpaque(true);
        bar.setBackground(new Color(40, 24, 14));
        bar.setForeground(new Color(220, 200, 170));
        bar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        return bar;
    }

    /** Returns the board panel (teammate 2 registers click listener here). */
    public ChessBoardPanel getBoardPanel() { return boardPanel; }

    /** Returns the board model (teammate 2 calls movePiece here). */
    public BoardModel getBoardModel() { return boardModel; }
}