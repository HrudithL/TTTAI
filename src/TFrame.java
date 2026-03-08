import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Main application window.
 *
 * Layout  (BorderLayout):
 *   NORTH  – status bar (current player / result)
 *   CENTER – TPanel (the 2×2 board)
 *   SOUTH  – toolbar: New Game  |  Pause & Quit
 *
 * The Pause button saves game state and immediately exits the JVM.
 * On the next launch Main detects the save file and offers to resume.
 */
public class TFrame extends JFrame {

    private final TPanel  panel;
    private final JLabel  statusLabel;
    private final JButton pauseBtn;

    // These are set by configure() so we can save on pause
    private GameState gameState;
    private String    saveMode;
    private String    saveAiType;

    private Thread gameThread;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    public TFrame(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());

        // -- Status bar (NORTH) ----------------------------------------------
        statusLabel = new JLabel("Loading…", SwingConstants.CENTER);
        statusLabel.setFont(Theme.STATUS_FONT);
        statusLabel.setForeground(Theme.FG);
        statusLabel.setBackground(Theme.LAYER_BG);
        statusLabel.setOpaque(true);
        statusLabel.setBorder(new EmptyBorder(10, 16, 10, 16));
        add(statusLabel, BorderLayout.NORTH);

        // -- Board (CENTER) --------------------------------------------------
        panel = new TPanel();
        add(panel, BorderLayout.CENTER);

        // -- Toolbar (SOUTH) -------------------------------------------------
        JButton newGameBtn = toolbarButton("New Game", Theme.ACCENT);
        pauseBtn           = toolbarButton("Pause & Quit", new Color(0xE5, 0x89, 0x1F));

        JPanel toolbar = new JPanel(new GridLayout(1, 2, 10, 0));
        toolbar.setBackground(Theme.BG);
        toolbar.setBorder(new EmptyBorder(8, 24, 14, 24));
        toolbar.add(newGameBtn);
        toolbar.add(pauseBtn);
        add(toolbar, BorderLayout.SOUTH);

        // -- Actions ---------------------------------------------------------
        newGameBtn.addActionListener(e -> onNewGame());
        pauseBtn  .addActionListener(e -> onPause());

        // Window close → same as Pause & Quit
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent ev) { onPause(); }
        });

        pack();
        setResizable(true);
        setMinimumSize(new Dimension(220, 460));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // -------------------------------------------------------------------------
    // Wiring
    // -------------------------------------------------------------------------

    public void configure(GameState gs, String mode, String aiType) {
        this.gameState   = gs;
        this.saveMode    = mode;
        this.saveAiType  = aiType;
        panel.configure(gs);
    }

    public void setGameThread(Thread t) { this.gameThread = t; }

    // -------------------------------------------------------------------------
    // Status helpers (thread-safe)
    // -------------------------------------------------------------------------

    public void setStatus(String text) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(text));
    }

    public void setStatusColored(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(text);
            statusLabel.setForeground(color);
        });
    }

    public void repaintBoard() {
        SwingUtilities.invokeLater(panel::repaint);
    }

    // -------------------------------------------------------------------------
    // Button actions
    // -------------------------------------------------------------------------

    private void onNewGame() {
        if (gameState == null) return;
        SaveManager.deleteSave();

        // Reset state and restart the game thread
        gameState.reset();
        for (int i = 0; i < 2; i++) {
            if (gameState.getPlayer(i) instanceof HumanPlayer)
                ((HumanPlayer) gameState.getPlayer(i)).cancel();
        }
        if (gameThread != null) gameThread.interrupt();

        setStatus(gameState.getCurrentPlayer().getName() + "'s turn  (" +
                  gameState.getCurrentPlayer().getLetter() + ")");
        statusLabel.setForeground(Theme.FG);
        panel.repaint();
    }

    private void onPause() {
        if (gameState != null && !gameState.isGameOver()) {
            // Save board state
            SaveManager.save(gameState, saveMode, saveAiType,
                             gameState.getPlayer(0).getName(),
                             gameState.getPlayer(1).getName());
            JOptionPane.showMessageDialog(this,
                "Game saved. Resume where you left off next time!",
                "Paused", JOptionPane.INFORMATION_MESSAGE);
        }
        System.exit(0);
    }

    // -------------------------------------------------------------------------
    // Accessor
    // -------------------------------------------------------------------------

    public TPanel getPanel() { return panel; }

    // -------------------------------------------------------------------------
    // Widget helper
    // -------------------------------------------------------------------------

    private static JButton toolbarButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 0, 8, 0));
        return b;
    }
}
