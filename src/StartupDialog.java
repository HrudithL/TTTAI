import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Properties;

/**
 * Modal dialog shown on startup.
 *
 * If a save file exists the user is first asked whether to resume it.
 * Otherwise (or after declining) they choose:
 *   – Mode  : Human vs AI  |  Human vs Human
 *   – AI    : Minimax (Hard) |  Random (Easy)   [only for HvAI]
 *   – Names : optional display names for each player
 */
public class StartupDialog extends JDialog {

    // -------------------------------------------------------------------------
    // Result bean
    // -------------------------------------------------------------------------
    public static class Config {
        public final String  mode;      // "HVS_AI" or "HVS_H"
        public final String  aiType;    // "MINIMAX", "RANDOM", or ""
        public final String  p1Name;
        public final String  p2Name;
        /** Non-null when the user chose to resume a saved game. */
        public final Properties savedState;

        Config(String mode, String aiType, String p1Name, String p2Name, Properties saved) {
            this.mode       = mode;
            this.aiType     = aiType;
            this.p1Name     = p1Name;
            this.p2Name     = p2Name;
            this.savedState = saved;
        }
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private Config result = null;  // null = user closed without confirming

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    private StartupDialog(Frame owner, boolean hasSave) {
        super(owner, "4×4×4 3D Tic Tac Toe", true);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG);

        // -- Title banner ----------------------------------------------------
        JLabel title = new JLabel("4×4×4 Tic Tac Toe", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.ACCENT);
        title.setBorder(new EmptyBorder(22, 20, 10, 20));
        add(title, BorderLayout.NORTH);

        // -- Centre form -----------------------------------------------------
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG);
        form.setBorder(new EmptyBorder(8, 30, 8, 30));
        add(form, BorderLayout.CENTER);

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(6, 0, 6, 12);
        lc.gridx  = 0;
        GridBagConstraints fc = new GridBagConstraints();
        fc.anchor = GridBagConstraints.WEST;
        fc.fill   = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1;
        fc.insets  = new Insets(6, 0, 6, 0);
        fc.gridx   = 1;

        // Mode
        JLabel modeLabel = label("Game Model:");
        String[] modes   = { "Human vs AI", "Human vs Human" };
        JComboBox<String> modeBox = styledCombo(modes);
        lc.gridy = 0; fc.gridy = 0;
        form.add(modeLabel, lc); form.add(modeBox, fc);

        // AI difficulty (hidden when HvH)
        JLabel aiLabel = label("AI Difficulty:");
        String[] ais   = { "Hard (Minimax)", "Easy (Random)" };
        JComboBox<String> aiBox = styledCombo(ais);
        lc.gridy = 1; fc.gridy = 1;
        form.add(aiLabel, lc); form.add(aiBox, fc);

        modeBox.addActionListener(e -> {
            boolean showAI = modeBox.getSelectedIndex() == 0;
            aiLabel.setVisible(showAI);
            aiBox.setVisible(showAI);
            pack();
        });

        // Player 1 name
        JTextField p1Field = styledField("You");
        lc.gridy = 2; fc.gridy = 2;
        form.add(label("Player 1 (X):"), lc); form.add(p1Field, fc);

        // Player 2 name
        JTextField p2Field = styledField("AI");
        lc.gridy = 3; fc.gridy = 3;
        form.add(label("Player 2 (O):"), lc); form.add(p2Field, fc);

        // Update default name when mode changes
        modeBox.addActionListener(e -> {
            if (modeBox.getSelectedIndex() == 1) {
                if (p2Field.getText().equals("AI")) p2Field.setText("Player 2");
            } else {
                if (p2Field.getText().equals("Player 2")) p2Field.setText("AI");
            }
        });

        // Resume row (only shown when save exists)
        JButton resumeBtn = null;
        if (hasSave) {
            JLabel saveLabel = label("Saved game found!");
            saveLabel.setForeground(new Color(0xFF, 0xD9, 0x3D));
            lc.gridy = 4; lc.gridwidth = 2;
            form.add(saveLabel, lc);
            lc.gridwidth = 1;

            resumeBtn = styledButton("Resume Paused Game", new Color(0x6B, 0xCB, 0x77));
            GridBagConstraints rc = new GridBagConstraints();
            rc.gridy    = 5; rc.gridx = 0; rc.gridwidth = 2;
            rc.fill     = GridBagConstraints.HORIZONTAL;
            rc.insets   = new Insets(4, 0, 4, 0);
            form.add(resumeBtn, rc);

            final JButton rb = resumeBtn;
            rb.addActionListener(e -> {
                // Resume: load save data and keep whatever was saved
                Properties saved = SaveManager.load();
                String sMode   = saved.getProperty("mode",   "HVS_AI");
                String sAiType = saved.getProperty("aiType", "MINIMAX");
                String sP1     = saved.getProperty("p1Name", "You");
                String sP2     = saved.getProperty("p2Name", "AI");
                result = new Config(sMode, sAiType, sP1, sP2, saved);
                dispose();
            });
        }

        // -- Buttons ---------------------------------------------------------
        JButton startBtn = styledButton("New Game", Theme.ACCENT);
        JButton quitBtn  = styledButton("Quit",     new Color(0xE0, 0x5C, 0x5C));

        startBtn.addActionListener(e -> {
            String m  = modeBox.getSelectedIndex() == 0 ? "HVS_AI" : "HVS_H";
            String ai = (modeBox.getSelectedIndex() == 0)
                        ? (aiBox.getSelectedIndex() == 0 ? "MINIMAX" : "RANDOM")
                        : "";
            String n1 = p1Field.getText().trim().isEmpty() ? "Player 1" : p1Field.getText().trim();
            String n2 = p2Field.getText().trim().isEmpty()
                        ? (m.equals("HVS_AI") ? "AI" : "Player 2")
                        : p2Field.getText().trim();
            result = new Config(m, ai, n1, n2, null);
            // Starting a new game clears any old save
            SaveManager.deleteSave();
            dispose();
        });
        quitBtn.addActionListener(e -> { result = null; dispose(); System.exit(0); });

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setBackground(Theme.BG);
        buttons.setBorder(new EmptyBorder(8, 30, 20, 30));
        buttons.add(startBtn); buttons.add(quitBtn);
        add(buttons, BorderLayout.SOUTH);

        setResizable(false);
        pack();
        setLocationRelativeTo(owner);
    }

    // -------------------------------------------------------------------------
    // Factory method
    // -------------------------------------------------------------------------

    /**
     * Shows the dialog and returns the user's choice, or null if the window
     * was closed without confirming.
     */
    public static Config showDialog(boolean hasSave) {
        StartupDialog dlg = new StartupDialog(null, hasSave);
        dlg.setVisible(true);  // blocks until disposed
        return dlg.result;
    }

    // -------------------------------------------------------------------------
    // Widget helpers
    // -------------------------------------------------------------------------

    private static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Theme.FG);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }

    private static <T> JComboBox<T> styledCombo(T[] items) {
        JComboBox<T> box = new JComboBox<>(items);
        // Use a light background so the native renderer always shows dark text
        box.setBackground(new Color(0xF0, 0xF0, 0xF5));
        box.setForeground(new Color(0x1A, 0x1A, 0x2E));
        box.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        // Override the list cell renderer for the drop-down panel as well
        box.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    setBackground(Theme.ACCENT);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(new Color(0xF0, 0xF0, 0xF5));
                    setForeground(new Color(0x1A, 0x1A, 0x2E));
                }
                return this;
            }
        });
        return box;
    }

    private static JTextField styledField(String initial) {
        JTextField f = new JTextField(initial, 14);
        f.setBackground(new Color(0xF0, 0xF0, 0xF5));
        f.setForeground(new Color(0x1A, 0x1A, 0x2E));
        f.setCaretColor(new Color(0x1A, 0x1A, 0x2E));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.GRID_LINE, 1),
            BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        return f;
    }

    private static JButton styledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        return b;
    }
}
