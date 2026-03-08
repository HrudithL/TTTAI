import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Board panel – renders four 4×4 grids stacked vertically, Layer 1 at top.
 *
 * All layout dimensions are computed dynamically from the panel's actual
 * pixel size every time paintComponent runs, so the board scales smoothly
 * when the user resizes the window.
 *
 * Visual features:
 *   – Amber highlight on the last move made
 *   – Green highlight on the winning line (all 4 cells)
 *   – Subtle white hover glow when it is a human's turn
 *   – Piece letters rendered with a drop-shadow
 */
public class TPanel extends JPanel {

    // -------------------------------------------------------------------------
    // Preferred / minimum sizes  (starting size of the window)
    // -------------------------------------------------------------------------
    private static final int PREF_W = 300;
    private static final int PREF_H = 680;
    private static final int MIN_W  = 180;
    private static final int MIN_H  = 400;

    // Fixed proportions (relative to cell size)
    private static final int N      = GameState.SIZE;    // 4 cells per row/col
    private static final double PAD_FRAC  = 0.6;         // pad = PAD_FRAC * cell
    private static final double LBL_FRAC  = 0.55;        // label height = LBL_FRAC * cell
    private static final double GAP_FRAC  = 0.35;        // inter-board gap = GAP_FRAC * cell

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------
    private GameState gameState;
    private int hoverSheet = -1, hoverRow = -1, hoverCol = -1;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    public TPanel() {
        setPreferredSize(new Dimension(PREF_W, PREF_H));
        setMinimumSize(new Dimension(MIN_W, MIN_H));
        setBackground(Theme.BG);

        MouseAdapter ma = new MouseAdapter() {
            @Override public void mouseMoved (MouseEvent e) { updateHover(e.getX(), e.getY()); }
            @Override public void mouseExited(MouseEvent e) { clearHover(); }
            @Override public void mouseClicked(MouseEvent e) { handleClick(e.getX(), e.getY()); }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);

        // Repaint whenever the panel is resized so the board scale updates
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { repaint(); }
        });
    }

    // -------------------------------------------------------------------------
    // Wiring
    // -------------------------------------------------------------------------

    public void configure(GameState gs) {
        this.gameState = gs;
        repaint();
    }

    // -------------------------------------------------------------------------
    // Dynamic layout computation
    // -------------------------------------------------------------------------

    /**
     * Returns the cell size (px) that fits all 4 boards with their labels and
     * gaps inside the current panel dimensions.
     *
     *   Vertical: PAD + 4*(LBL + N*cell) + 3*GAP + PAD  ≤ height
     *   Horizontal: PAD + N*cell + PAD                   ≤ width
     *
     * Both constraints give a maximum cell size; we take the smaller and clamp
     * to a minimum of 1 so nothing crashes on tiny windows.
     */
    private int cellSize() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return 1;

        // From horizontal constraint: cell ≤ (w - 2*pad) / N
        // pad = PAD_FRAC * cell  → cell * (N + 2*PAD_FRAC) ≤ w
        double cellFromW = w / (N + 2.0 * PAD_FRAC);

        // From vertical constraint:
        // PAD + 4*(LBL + N*cell) + 3*GAP + PAD ≤ h
        // (PAD_FRAC*cell)*2 + 4*(LBL_FRAC*cell + N*cell) + 3*(GAP_FRAC*cell) ≤ h
        // cell * (2*PAD_FRAC + 4*LBL_FRAC + 4*N + 3*GAP_FRAC) ≤ h
        double vFactor = 2 * PAD_FRAC + 4 * LBL_FRAC + 4.0 * N + 3 * GAP_FRAC;
        double cellFromH = h / vFactor;

        return Math.max(1, (int) Math.min(cellFromW, cellFromH));
    }

    /** Returns {bx, by} for the given sheet, given cell size. */
    private int[] origin(int sheet, int cell) {
        int pad   = (int) (PAD_FRAC * cell);
        int lblH  = (int) (LBL_FRAC * cell);
        int gapV  = (int) (GAP_FRAC * cell);
        int board = N * cell;
        int slotH = lblH + board + gapV;

        // Centre board horizontally in the panel
        int bx = (getWidth() - board) / 2;
        int by = pad + lblH + sheet * slotH;
        return new int[]{ bx, by };
    }

    // -------------------------------------------------------------------------
    // Painting
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int cell = cellSize();

        for (int sheet = 0; sheet < N; sheet++) {
            int[] o  = origin(sheet, cell);
            int bx = o[0], by = o[1];

            drawLayerBackground(g2, bx, by, cell);
            drawLabel(g2, sheet, bx, by, cell);
            drawCells(g2, sheet, bx, by, cell);
            drawGridLines(g2, bx, by, cell);
        }
    }

    private void drawLayerBackground(Graphics2D g2, int bx, int by, int cell) {
        int board = N * cell;
        g2.setColor(Theme.LAYER_BG);
        g2.fillRoundRect(bx - 3, by - 3, board + 6, board + 6, 8, 8);
    }

    private void drawLabel(Graphics2D g2, int sheet, int bx, int by, int cell) {
        int lblH  = (int) (LBL_FRAC * cell);
        int board = N * cell;
        // Scale font with cell but keep it readable
        float fontSize = Math.max(9f, Math.min(cell * 0.3f, 14f));
        g2.setFont(Theme.LABEL_FONT.deriveFont(Font.BOLD, fontSize));
        g2.setColor(Theme.ACCENT);
        String text = "Layer " + (sheet + 1);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, bx + (board - fm.stringWidth(text)) / 2, by - 5);
    }

    private void drawCells(Graphics2D g2, int sheet, int bx, int by, int cell) {
        if (gameState == null) return;

        Location   lastMove    = gameState.getLastMove();
        Location[] winningLine = gameState.getWinningLine();
        Set<Location> winSet   = new HashSet<>();
        if (winningLine != null) winSet.addAll(Arrays.asList(winningLine));

        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                int cx = bx + col * cell;
                int cy = by + row * cell;
                Location here = new Location(col, row, sheet);

                boolean isWin  = winSet.contains(here);
                boolean isLast = here.equals(lastMove);
                boolean isHov  = (sheet == hoverSheet && row == hoverRow && col == hoverCol);

                g2.setColor(Theme.CELL_BG);
                g2.fillRect(cx, cy, cell, cell);

                if      (isWin)                          { g2.setColor(Theme.WIN_LINE);  g2.fillRect(cx, cy, cell, cell); }
                else if (isLast)                         { g2.setColor(Theme.LAST_MOVE); g2.fillRect(cx, cy, cell, cell); }
                else if (isHov && !gameState.isGameOver()){ g2.setColor(Theme.HOVER);    g2.fillRect(cx, cy, cell, cell); }

                drawPiece(g2, gameState.getCell(sheet, row, col), cx, cy, cell);
            }
        }
    }

    private void drawPiece(Graphics2D g2, char piece, int cx, int cy, int cell) {
        if (piece == ' ') return;
        Color c = (piece == 'X') ? Theme.X_COLOR : Theme.O_COLOR;
        float fontSize = Math.max(8f, cell * 0.48f);
        g2.setFont(Theme.PIECE_FONT.deriveFont(Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();
        String s = String.valueOf(piece);
        int tx = cx + (cell - fm.stringWidth(s)) / 2;
        int ty = cy + (cell + fm.getAscent() - fm.getDescent()) / 2;
        g2.setColor(new Color(0, 0, 0, 90));
        g2.drawString(s, tx + 1, ty + 1);
        g2.setColor(c);
        g2.drawString(s, tx, ty);
    }

    private void drawGridLines(Graphics2D g2, int bx, int by, int cell) {
        int board = N * cell;
        float strokeW = Math.max(0.8f, cell * 0.025f);
        g2.setColor(Theme.GRID_LINE);
        g2.setStroke(new BasicStroke(strokeW));
        for (int i = 0; i <= N; i++) {
            g2.drawLine(bx,           by + i * cell, bx + board,    by + i * cell);
            g2.drawLine(bx + i * cell, by,           bx + i * cell, by + board);
        }
    }

    // -------------------------------------------------------------------------
    // Mouse handling
    // -------------------------------------------------------------------------

    private void handleClick(int px, int py) {
        if (gameState == null || gameState.isGameOver()) return;
        if (!(gameState.getCurrentPlayer() instanceof HumanPlayer)) return;

        int[] hit = cellAt(px, py);
        if (hit == null) return;
        int sheet = hit[0], row = hit[1], col = hit[2];
        if (gameState.getCell(sheet, row, col) != ' ') return;
        gameState.offerHumanMove(new Location(col, row, sheet));
    }

    private void updateHover(int px, int py) {
        int[] hit = cellAt(px, py);
        if (hit != null) {
            if (hit[0] != hoverSheet || hit[1] != hoverRow || hit[2] != hoverCol) {
                hoverSheet = hit[0]; hoverRow = hit[1]; hoverCol = hit[2];
                repaint();
            }
        } else {
            clearHover();
        }
    }

    private void clearHover() {
        if (hoverSheet != -1) { hoverSheet = -1; repaint(); }
    }

    /** Maps pixel (px, py) → {sheet, row, col}, or null if outside all grids. */
    private int[] cellAt(int px, int py) {
        int cell  = cellSize();
        int board = N * cell;
        for (int sheet = 0; sheet < N; sheet++) {
            int[] o  = origin(sheet, cell);
            int bx = o[0], by = o[1];
            if (px >= bx && px < bx + board && py >= by && py < by + board) {
                return new int[]{ sheet, (py - by) / cell, (px - bx) / cell };
            }
        }
        return null;
    }
}
