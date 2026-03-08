import java.util.Arrays;

/**
 * Core game state for 4×4×4 3D Tic Tac Toe.
 *
 * Board indexed board[sheet][row][col], all dimensions 0–3.
 * All 76 winning lines are precomputed at startup for O(76) win checks.
 *
 * Extra features vs. original:
 *   – lastMove    : most recently played Location (TPanel highlights it)
 *   – winningLine : 4 cells that caused the win (TPanel highlights them)
 *   – saveToString / restoreFrom : pause-and-resume persistence
 *   – offerHumanMove : forwards TPanel clicks to the current HumanPlayer
 */
public class GameState {

    public static final int SIZE = 4;

    // board[sheet][row][col]
    private final char[][][] board = new char[SIZE][SIZE][SIZE];

    private final PlayerInt[] players;
    private int currentPlayerIndex = 0;

    private char      winner      = ' ';  // ' '=none, 'X', 'O', 'D'=draw
    private boolean   gameOver    = false;
    private volatile boolean resetRequested = false;

    private Location   lastMove    = null;
    private Location[] winningLine = null;

    private final Location[][] winLines;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    public GameState(PlayerInt player1, PlayerInt player2) {
        players  = new PlayerInt[]{ player1, player2 };
        winLines = buildWinLines();
        clearBoard();
    }

    // -------------------------------------------------------------------------
    // Board access
    // -------------------------------------------------------------------------

    public synchronized char[][][] getBoardCopy() {
        char[][][] copy = new char[SIZE][SIZE][SIZE];
        for (int s = 0; s < SIZE; s++)
            for (int r = 0; r < SIZE; r++)
                System.arraycopy(board[s][r], 0, copy[s][r], 0, SIZE);
        return copy;
    }

    public synchronized char      getCell(int sheet, int row, int col) { return board[sheet][row][col]; }
    public synchronized Location   getLastMove()    { return lastMove;    }
    public synchronized Location[] getWinningLine() { return winningLine; }

    // -------------------------------------------------------------------------
    // Game flow
    // -------------------------------------------------------------------------

    public synchronized PlayerInt getCurrentPlayer()       { return players[currentPlayerIndex]; }
    public synchronized int        getCurrentPlayerIndex() { return currentPlayerIndex; }
    public synchronized PlayerInt  getPlayer(int idx)      { return players[idx]; }

    public synchronized boolean makeMove(Location loc) {
        if (gameOver || loc == null) return false;

        int s = loc.getSheet(), r = loc.getRow(), c = loc.getCol();
        if (s < 0 || s >= SIZE || r < 0 || r >= SIZE || c < 0 || c >= SIZE) return false;
        if (board[s][r][c] != ' ') return false;

        char letter = players[currentPlayerIndex].getLetter();
        board[s][r][c] = letter;
        lastMove = loc;

        Location[] line = findWinningLine(letter);
        if (line != null) {
            winner      = letter;
            winningLine = line;
            gameOver    = true;
        } else if (isBoardFull()) {
            winner   = 'D';
            gameOver = true;
        } else {
            currentPlayerIndex = 1 - currentPlayerIndex;
        }
        return true;
    }

    /** Forwards a click to the current player if it is a HumanPlayer. */
    public synchronized void offerHumanMove(Location loc) {
        PlayerInt p = players[currentPlayerIndex];
        if (!gameOver && p instanceof HumanPlayer)
            ((HumanPlayer) p).offerMove(loc);
    }

    public synchronized boolean isGameOver()  { return gameOver; }
    public synchronized char    getWinner()   { return winner;   }

    public synchronized int emptyCellCount() {
        int n = 0;
        for (int s = 0; s < SIZE; s++)
            for (int r = 0; r < SIZE; r++)
                for (int c = 0; c < SIZE; c++)
                    if (board[s][r][c] == ' ') n++;
        return n;
    }

    // -------------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------------

    public synchronized void reset() {
        clearBoard();
        currentPlayerIndex = 0;
        winner      = ' ';
        winningLine = null;
        lastMove    = null;
        gameOver    = false;
        resetRequested = true;
        players[0].reset();
        players[1].reset();
    }

    public synchronized boolean consumeResetRequest() {
        boolean r = resetRequested;
        resetRequested = false;
        return r;
    }

    // -------------------------------------------------------------------------
    // Save / restore  (pause feature)
    // -------------------------------------------------------------------------

    /**
     * Serialises board + turn index to a human-readable string.
     *   currentPlayer=0
     *   board=X.O... (64 chars, sheet-major, '.' = empty)
     */
    public synchronized String saveToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("currentPlayer=").append(currentPlayerIndex).append('\n');
        sb.append("board=");
        for (int s = 0; s < SIZE; s++)
            for (int r = 0; r < SIZE; r++)
                for (int c = 0; c < SIZE; c++) {
                    char ch = board[s][r][c];
                    sb.append(ch == ' ' ? '.' : ch);
                }
        sb.append('\n');
        return sb.toString();
    }

    public synchronized void restoreFrom(String data) {
        clearBoard();
        winner = ' '; winningLine = null; lastMove = null;
        gameOver = false; resetRequested = false;

        for (String line : data.split("\n")) {
            if (line.startsWith("currentPlayer=")) {
                currentPlayerIndex = Integer.parseInt(line.substring("currentPlayer=".length()).trim());
            } else if (line.startsWith("board=")) {
                String cells = line.substring("board=".length());
                int idx = 0;
                for (int s = 0; s < SIZE; s++)
                    for (int r = 0; r < SIZE; r++)
                        for (int c = 0; c < SIZE; c++) {
                            char ch = cells.charAt(idx++);
                            board[s][r][c] = (ch == '.') ? ' ' : ch;
                        }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Win / draw detection
    // -------------------------------------------------------------------------

    private Location[] findWinningLine(char letter) {
        for (Location[] line : winLines) {
            boolean win = true;
            for (Location loc : line) {
                if (board[loc.getSheet()][loc.getRow()][loc.getCol()] != letter) {
                    win = false; break;
                }
            }
            if (win) return line;
        }
        return null;
    }

    public synchronized boolean checkWin(char letter) {
        return findWinningLine(letter) != null;
    }

    private boolean isBoardFull() {
        for (int s = 0; s < SIZE; s++)
            for (int r = 0; r < SIZE; r++)
                for (int c = 0; c < SIZE; c++)
                    if (board[s][r][c] == ' ') return false;
        return true;
    }

    // -------------------------------------------------------------------------
    // Win-line precomputation (76 lines total)
    // -------------------------------------------------------------------------

    public static Location[][] buildWinLines() {
        Location[][] lines = new Location[76][4];
        int idx = 0;

        // Rows (16)
        for (int s = 0; s < SIZE; s++)
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) lines[idx][c] = new Location(c, r, s);
                idx++;
            }
        // Columns (16)
        for (int s = 0; s < SIZE; s++)
            for (int c = 0; c < SIZE; c++) {
                for (int r = 0; r < SIZE; r++) lines[idx][r] = new Location(c, r, s);
                idx++;
            }
        // In-layer diagonals (8)
        for (int s = 0; s < SIZE; s++) {
            for (int i = 0; i < SIZE; i++) lines[idx][i] = new Location(i, i, s);        idx++;
            for (int i = 0; i < SIZE; i++) lines[idx][i] = new Location(SIZE-1-i, i, s); idx++;
        }
        // Vertical (sheet-direction) lines (16)
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++) {
                for (int s = 0; s < SIZE; s++) lines[idx][s] = new Location(c, r, s);
                idx++;
            }
        // Sheet-col diagonals (8)
        for (int r = 0; r < SIZE; r++) {
            for (int i = 0; i < SIZE; i++) lines[idx][i] = new Location(i, r, i);        idx++;
            for (int i = 0; i < SIZE; i++) lines[idx][i] = new Location(SIZE-1-i, r, i); idx++;
        }
        // Sheet-row diagonals (8)
        for (int c = 0; c < SIZE; c++) {
            for (int i = 0; i < SIZE; i++) lines[idx][i] = new Location(c, i, i);        idx++;
            for (int i = 0; i < SIZE; i++) lines[idx][i] = new Location(c, SIZE-1-i, i); idx++;
        }
        // Space diagonals (4)
        for (int i = 0; i < SIZE; i++) lines[idx][i] = new Location(i,       i,       i); idx++;
        for (int i = 0; i < SIZE; i++) lines[idx][i] = new Location(SIZE-1-i,i,       i); idx++;
        for (int i = 0; i < SIZE; i++) lines[idx][i] = new Location(i,       SIZE-1-i,i); idx++;
        for (int i = 0; i < SIZE; i++) lines[idx][i] = new Location(SIZE-1-i,SIZE-1-i,i);

        return lines;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void clearBoard() {
        for (int s = 0; s < SIZE; s++)
            for (int r = 0; r < SIZE; r++)
                Arrays.fill(board[s][r], ' ');
    }

    public Location[][] getWinLines() { return winLines; }
}
