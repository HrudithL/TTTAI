import java.awt.*;

/**
 * Centralised colour / font constants shared by all UI components.
 *
 * Dark-blue theme:
 *   BG        – deep navy background
 *   CELL_BG   – slightly lighter cell fill
 *   GRID_LINE – medium blue-grey grid lines
 *   FG        – near-white text
 *   ACCENT    – bright teal for headings / new-game button
 *   X_COLOR   – coral red for X pieces
 *   O_COLOR   – sky blue for O pieces
 *   LAST_MOVE – amber highlight for last-played cell
 *   WIN_LINE  – bright green highlight for winning cells
 */
public final class Theme {
    private Theme() {}

    public static final Color BG        = new Color(0x1A, 0x1A, 0x2E);
    public static final Color CELL_BG   = new Color(0x16, 0x21, 0x3E);
    public static final Color GRID_LINE = new Color(0x4A, 0x55, 0x80);
    public static final Color FG        = new Color(0xE0, 0xE0, 0xE8);
    public static final Color ACCENT    = new Color(0x0F, 0xC4, 0xC4);
    public static final Color X_COLOR   = new Color(0xFF, 0x6B, 0x6B);
    public static final Color O_COLOR   = new Color(0x4E, 0xCD, 0xC4);
    public static final Color LAST_MOVE = new Color(0xFF, 0xD9, 0x3D, 120);
    public static final Color WIN_LINE  = new Color(0x6B, 0xCB, 0x77, 160);
    public static final Color HOVER     = new Color(0xFF, 0xFF, 0xFF, 30);
    public static final Color LAYER_BG  = new Color(0x0D, 0x13, 0x26);

    public static final Font LABEL_FONT  = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font STATUS_FONT = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font PIECE_FONT  = new Font("Segoe UI", Font.BOLD, 22);
}
