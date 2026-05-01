package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * GUI Feature 2 – Settings Window for Customization.
 *
 * A modal JDialog opened from the Settings menu that lets players choose:
 *   • Board color theme  (Classic Wood / Green Felt / Ocean Blue / Monochrome)
 *   • Piece color theme  (Classic B&W / Ivory & Charcoal / Gold & Navy)
 *   • Board size         (Small 60px / Medium 80px / Large 100px per square)
 *
 * Pressing "Apply" pushes the selections to {@link ChessBoardPanel} immediately
 * so the board updates in real-time.  "Cancel" closes without saving.
 *
 * @author Nischal Rimal
 */
public class SettingsWindow extends JDialog {

    // -----------------------------------------------------------------------
    // Board theme data
    // -----------------------------------------------------------------------

    /** Pairs of (light square color, dark square color) for each board theme. */
    public enum BoardTheme {
        CLASSIC_WOOD ("Classic Wood",
                new Color(240, 217, 181), new Color(181, 136, 99)),
        GREEN_FELT   ("Green Felt",
                new Color(234, 233, 210), new Color(75,  115, 74)),
        OCEAN_BLUE   ("Ocean Blue",
                new Color(200, 220, 240), new Color(70,  100, 160)),
        MONOCHROME   ("Monochrome",
                new Color(220, 220, 220), new Color(80,  80,  80));

        public final String  label;
        public final Color   light;
        public final Color   dark;

        BoardTheme(String label, Color light, Color dark) {
            this.label = label; this.light = light; this.dark = dark;
        }

        @Override public String toString() { return label; }
    }

    // -----------------------------------------------------------------------
    // Piece color theme data
    // -----------------------------------------------------------------------

    /** Pairs of (white-piece color, black-piece color) for each piece theme. */
    public enum PieceTheme {
        CLASSIC   ("Classic B&W",
                new Color(255, 255, 255), new Color(30,  30,  30)),
        IVORY     ("Ivory & Charcoal",
                new Color(255, 248, 220), new Color(54,  54,  54)),
        GOLD_NAVY ("Gold & Navy",
                new Color(255, 215, 0),   new Color(0,   0,   128));

        public final String label;
        public final Color  whiteColor;
        public final Color  blackColor;

        PieceTheme(String label, Color w, Color b) {
            this.label = label; this.whiteColor = w; this.blackColor = b;
        }

        @Override public String toString() { return label; }
    }

    // -----------------------------------------------------------------------
    // Board size data
    // -----------------------------------------------------------------------

    /** Square size in pixels for each size option. */
    public enum BoardSize {
        SMALL  ("Small  (60 px)",  60),
        MEDIUM ("Medium (80 px)",  80),
        LARGE  ("Large  (100 px)", 100);

        public final String label;
        public final int    squareSize;

        BoardSize(String label, int sq) { this.label = label; this.squareSize = sq; }

        @Override public String toString() { return label; }
    }

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private final ChessBoardPanel boardPanel;

    // Current selections (start at whatever the board currently uses)
    private BoardTheme selectedBoardTheme;
    private PieceTheme selectedPieceTheme;
    private BoardSize  selectedBoardSize;

    // UI controls
    private JComboBox<BoardTheme> boardThemeBox;
    private JComboBox<PieceTheme> pieceThemeBox;
    private JComboBox<BoardSize>  boardSizeBox;
    private JPanel                previewLight;
    private JPanel                previewDark;
    private JLabel                previewWhitePiece;
    private JLabel                previewBlackPiece;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates and displays the Settings dialog.
     *
     * @param owner      the parent JFrame
     * @param boardPanel the panel to apply settings to
     */
    public SettingsWindow(JFrame owner, ChessBoardPanel boardPanel) {
        super(owner, "Settings – Customize Board & Pieces", true); // modal
        this.boardPanel = boardPanel;

        // Read current settings from the board panel
        this.selectedBoardTheme = boardPanel.getBoardTheme();
        this.selectedPieceTheme = boardPanel.getPieceTheme();
        this.selectedBoardSize  = boardPanel.getBoardSize();

        buildUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    // -----------------------------------------------------------------------
    // UI construction
    // -----------------------------------------------------------------------

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(14, 16, 10, 16));

        content.add(buildFormPanel(),    BorderLayout.CENTER);
        content.add(buildPreviewPanel(), BorderLayout.EAST);
        content.add(buildButtonPanel(),  BorderLayout.SOUTH);

        setContentPane(content);
    }

    /** Three labelled combo-boxes for board theme, piece theme, and size. */
    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Options"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(6, 8, 6, 8);
        gbc.anchor  = GridBagConstraints.WEST;

        // Row 0 – Board theme
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Board Color:"), gbc);
        gbc.gridx = 1;
        boardThemeBox = new JComboBox<>(BoardTheme.values());
        boardThemeBox.setSelectedItem(selectedBoardTheme);
        boardThemeBox.addActionListener(e -> {
            selectedBoardTheme = (BoardTheme) boardThemeBox.getSelectedItem();
            refreshPreview();
        });
        panel.add(boardThemeBox, gbc);

        // Row 1 – Piece theme
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Piece Color:"), gbc);
        gbc.gridx = 1;
        pieceThemeBox = new JComboBox<>(PieceTheme.values());
        pieceThemeBox.setSelectedItem(selectedPieceTheme);
        pieceThemeBox.addActionListener(e -> {
            selectedPieceTheme = (PieceTheme) pieceThemeBox.getSelectedItem();
            refreshPreview();
        });
        panel.add(pieceThemeBox, gbc);

        // Row 2 – Board size
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Board Size:"), gbc);
        gbc.gridx = 1;
        boardSizeBox = new JComboBox<>(BoardSize.values());
        boardSizeBox.setSelectedItem(selectedBoardSize);
        boardSizeBox.addActionListener(e -> {
            selectedBoardSize = (BoardSize) boardSizeBox.getSelectedItem();
        });
        panel.add(boardSizeBox, gbc);

        return panel;
    }

    /** Small live preview of the two square colors and piece glyphs. */
    private JPanel buildPreviewPanel() {
        JPanel outer = new JPanel(new BorderLayout(0, 6));
        outer.setBorder(BorderFactory.createTitledBorder("Preview"));

        // 2x2 miniature board
        JPanel miniBoard = new JPanel(new GridLayout(2, 2, 2, 2));
        miniBoard.setPreferredSize(new Dimension(110, 110));
        miniBoard.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        previewLight       = new JPanel(new BorderLayout());
        previewDark        = new JPanel(new BorderLayout());
        JPanel darkLight   = new JPanel(new BorderLayout());
        JPanel lightDark   = new JPanel(new BorderLayout());

        // White piece on light square
        previewWhitePiece = new JLabel("\u2654", SwingConstants.CENTER); // ♔
        previewWhitePiece.setFont(previewWhitePiece.getFont().deriveFont(Font.PLAIN, 30f));
        previewLight.add(previewWhitePiece, BorderLayout.CENTER);

        // Black piece on dark square
        previewBlackPiece = new JLabel("\u265A", SwingConstants.CENTER); // ♚
        previewBlackPiece.setFont(previewBlackPiece.getFont().deriveFont(Font.PLAIN, 30f));
        previewDark.add(previewBlackPiece, BorderLayout.CENTER);

        miniBoard.add(previewLight);
        miniBoard.add(darkLight);
        miniBoard.add(lightDark);
        miniBoard.add(previewDark);

        // Store references to the plain panels too
        final JPanel[] panels = {previewLight, darkLight, lightDark, previewDark};
        // We'll repaint them all in refreshPreview()
        outer.putClientProperty("miniPanels", panels);

        outer.add(miniBoard, BorderLayout.CENTER);
        refreshPreview();
        return outer;
    }

    /** Apply / Cancel buttons. */
    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        JButton apply = new JButton("Apply");
        apply.addActionListener(e -> applySettings());

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());

        panel.add(apply);
        panel.add(cancel);
        return panel;
    }

    // -----------------------------------------------------------------------
    // Logic
    // -----------------------------------------------------------------------

    /** Refreshes the mini preview squares and piece colors without touching the board. */
    private void refreshPreview() {
        if (selectedBoardTheme == null || selectedPieceTheme == null) return;

        previewLight.setBackground(selectedBoardTheme.light);
        previewLight.setOpaque(true);
        previewDark.setBackground(selectedBoardTheme.dark);
        previewDark.setOpaque(true);

        previewWhitePiece.setForeground(selectedPieceTheme.whiteColor);
        previewBlackPiece.setForeground(selectedPieceTheme.blackColor);

        // Also repaint the other two plain squares
        // (access them via the parent container)
        Container miniBoard = previewLight.getParent();
        if (miniBoard != null) {
            for (Component c : miniBoard.getComponents()) {
                if (c instanceof JPanel && c != previewLight && c != previewDark) {
                    JPanel p = (JPanel) c;
                    boolean isLight = (miniBoard.getComponentZOrder(c) % 2 == 0);
                    // index 0=light,1=dark,2=light,3=dark per GridLayout row-major
                    int idx = java.util.Arrays.asList(miniBoard.getComponents()).indexOf(c);
                    p.setBackground(idx % 2 == 0 ? selectedBoardTheme.dark
                                                 : selectedBoardTheme.light);
                    p.setOpaque(true);
                }
            }
            miniBoard.repaint();
        }
    }

    /**
     * Pushes the selected settings to the board panel so changes appear
     * in real-time, then closes the dialog.
     */
    private void applySettings() {
        boardPanel.applySettings(selectedBoardTheme, selectedPieceTheme, selectedBoardSize);
        dispose();
    }
}
