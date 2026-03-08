import java.util.ArrayList;
import java.util.List;

/**
 * Minimax AI with alpha-beta pruning and adaptive search depth.
 *
 * Depth schedule (based on remaining empty cells):
 *   > 48 cells  → depth 1  (opening – branching factor too large)
 *   > 36 cells  → depth 2
 *   > 24 cells  → depth 3
 *   > 12 cells  → depth 4
 *   otherwise   → depth 5
 *
 * Leaf heuristic (line-based, contested lines ignored):
 *   +100  per line with 3 AI pieces + 1 empty
 *   +10   per line with 2 AI pieces + 2 empty
 *   +1    per line with 1 AI piece  + 3 empty
 *   mirror for opponent
 */
public class MinimaxAIPlayer extends Player {

    private static final int WIN_SCORE  =  1_000_000;
    private static final int LOSE_SCORE = -1_000_000;

    private final char          oppLetter;
    private final Location[][]  winLines;

    public MinimaxAIPlayer(char letter, String name, char oppLetter) {
        super(letter, name);
        this.oppLetter = oppLetter;
        this.winLines  = GameState.buildWinLines();
    }

    // -------------------------------------------------------------------------
    // PlayerInt
    // -------------------------------------------------------------------------

    @Override
    public Location getMove(char[][][] board) {
        int empty    = countEmpty(board);
        int maxDepth = adaptiveDepth(empty);

        Location bestMove  = null;
        int      bestScore = Integer.MIN_VALUE;
        int      alpha     = Integer.MIN_VALUE;
        int      beta      = Integer.MAX_VALUE;

        for (Location move : emptyCells(board)) {
            board[move.getSheet()][move.getRow()][move.getCol()] = getLetter();
            int score = minimax(board, maxDepth - 1, false, alpha, beta);
            board[move.getSheet()][move.getRow()][move.getCol()] = ' ';

            if (score > bestScore) {
                bestScore = score;
                bestMove  = move;
            }
            alpha = Math.max(alpha, bestScore);
        }
        return bestMove;
    }

    @Override public void reset() { /* stateless */ }

    // -------------------------------------------------------------------------
    // Adaptive depth
    // -------------------------------------------------------------------------

    private static int adaptiveDepth(int emptyCells) {
        if (emptyCells > 48) return 1;
        if (emptyCells > 36) return 2;
        if (emptyCells > 24) return 3;
        if (emptyCells > 12) return 4;
        return 5;
    }

    // -------------------------------------------------------------------------
    // Minimax with alpha-beta
    // -------------------------------------------------------------------------

    private int minimax(char[][][] board, int depth,
                        boolean isMaximizing, int alpha, int beta) {
        if (checkWinBoard(board, getLetter()))  return WIN_SCORE  + depth;
        if (checkWinBoard(board, oppLetter))    return LOSE_SCORE - depth;
        if (isBoardFull(board) || depth == 0)   return evaluate(board);

        List<Location> moves = emptyCells(board);

        if (isMaximizing) {
            int best = Integer.MIN_VALUE;
            for (Location m : moves) {
                board[m.getSheet()][m.getRow()][m.getCol()] = getLetter();
                best  = Math.max(best, minimax(board, depth - 1, false, alpha, beta));
                board[m.getSheet()][m.getRow()][m.getCol()] = ' ';
                alpha = Math.max(alpha, best);
                if (beta <= alpha) break;
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (Location m : moves) {
                board[m.getSheet()][m.getRow()][m.getCol()] = oppLetter;
                best = Math.min(best, minimax(board, depth - 1, true, alpha, beta));
                board[m.getSheet()][m.getRow()][m.getCol()] = ' ';
                beta = Math.min(beta, best);
                if (beta <= alpha) break;
            }
            return best;
        }
    }

    // -------------------------------------------------------------------------
    // Heuristic evaluation
    // -------------------------------------------------------------------------

    private int evaluate(char[][][] board) {
        int score    = 0;
        char myLetter = getLetter();
        for (Location[] line : winLines) {
            int mine = 0, theirs = 0;
            for (Location loc : line) {
                char cell = board[loc.getSheet()][loc.getRow()][loc.getCol()];
                if      (cell == myLetter)  mine++;
                else if (cell == oppLetter) theirs++;
            }
            if (mine > 0 && theirs == 0)   score += lineValue(mine);
            else if (theirs > 0 && mine == 0) score -= lineValue(theirs);
        }
        return score;
    }

    private static int lineValue(int count) {
        switch (count) {
            case 3: return 100;
            case 2: return 10;
            default: return 1;
        }
    }

    // -------------------------------------------------------------------------
    // Board utilities
    // -------------------------------------------------------------------------

    private boolean checkWinBoard(char[][][] board, char letter) {
        for (Location[] line : winLines) {
            boolean win = true;
            for (Location loc : line)
                if (board[loc.getSheet()][loc.getRow()][loc.getCol()] != letter) { win = false; break; }
            if (win) return true;
        }
        return false;
    }

    private static boolean isBoardFull(char[][][] board) {
        for (int s = 0; s < GameState.SIZE; s++)
            for (int r = 0; r < GameState.SIZE; r++)
                for (int c = 0; c < GameState.SIZE; c++)
                    if (board[s][r][c] == ' ') return false;
        return true;
    }

    private static int countEmpty(char[][][] board) {
        int n = 0;
        for (int s = 0; s < GameState.SIZE; s++)
            for (int r = 0; r < GameState.SIZE; r++)
                for (int c = 0; c < GameState.SIZE; c++)
                    if (board[s][r][c] == ' ') n++;
        return n;
    }

    private static List<Location> emptyCells(char[][][] board) {
        List<Location> cells = new ArrayList<>();
        for (int s = 0; s < GameState.SIZE; s++)
            for (int r = 0; r < GameState.SIZE; r++)
                for (int c = 0; c < GameState.SIZE; c++)
                    if (board[s][r][c] == ' ')
                        cells.add(new Location(c, r, s));
        return cells;
    }
}
