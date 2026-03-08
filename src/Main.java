import javax.swing.*;
import java.awt.Color;
import java.util.Properties;

/**
 * Entry point for 4×4×4 3D Tic Tac Toe.
 *
 * Boot sequence:
 *   1. Show StartupDialog (detects save file, lets user choose mode/AI/names).
 *   2. Build players and GameState from those choices.
 *   3. Open TFrame.
 *   4. If resuming: restore board from save data.
 *   5. Launch the game-loop thread.
 *
 * The game loop runs on a daemon thread and drives alternating turns.
 * Human turns block on the player's LinkedBlockingQueue until TPanel
 * forwards a click via GameState.offerHumanMove().
 * AI turns run synchronously on the same thread (status = "thinking…" is
 * shown to let the user know something is happening).
 */
public class Main {

    public static void main(String[] args) throws Exception {

        // Apply system Look & Feel for native window chrome
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) { }

        // ── 1. Startup dialog ────────────────────────────────────────────────
        boolean hasSave = SaveManager.hasSave();
        StartupDialog.Config cfg = StartupDialog.showDialog(hasSave);
        if (cfg == null) System.exit(0);   // user closed window

        // ── 2. Build players ─────────────────────────────────────────────────
        final PlayerInt p1, p2;

        if ("HVS_H".equals(cfg.mode)) {
            // Human vs Human
            p1 = new HumanPlayer('X', cfg.p1Name);
            p2 = new HumanPlayer('O', cfg.p2Name);
        } else {
            // Human vs AI
            p1 = new HumanPlayer('X', cfg.p1Name);
            if ("RANDOM".equals(cfg.aiType)) {
                p2 = new RandomAIPlayer('O', cfg.p2Name);
            } else {
                p2 = new MinimaxAIPlayer('O', cfg.p2Name, 'X');
            }
        }

        // ── 3. Game state ────────────────────────────────────────────────────
        final GameState gameState = new GameState(p1, p2);

        // ── 4. Open window (EDT) ─────────────────────────────────────────────
        final TFrame[] frameHolder = new TFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            TFrame frame = new TFrame("4×4×4 3D Tic Tac Toe");
            frame.configure(gameState, cfg.mode, cfg.aiType);
            frameHolder[0] = frame;
        });
        TFrame frame = frameHolder[0];

        // ── 5. Restore saved board (if resuming) ─────────────────────────────
        if (cfg.savedState != null) {
            // Rebuild board-state string from saved Properties
            StringBuilder sb = new StringBuilder();
            sb.append("currentPlayer=").append(cfg.savedState.getProperty("currentPlayer", "0")).append('\n');
            sb.append("board=").append(cfg.savedState.getProperty("board", "")).append('\n');
            gameState.restoreFrom(sb.toString());
            frame.repaintBoard();
        }

        // ── 6. Game-loop thread ───────────────────────────────────────────────
        Thread gameThread = buildGameLoop(gameState, frame, cfg.mode);
        frame.setGameThread(gameThread);
        gameThread.start();
    }

    // -------------------------------------------------------------------------
    // Game loop factory
    // -------------------------------------------------------------------------

    /**
     * Creates (but does not start) a daemon thread that drives alternating turns.
     * When interrupted it checks for a reset request and, if found, relaunches
     * itself recursively – this avoids needing a separate "reset" entry point.
     */
    static Thread buildGameLoop(GameState gameState, TFrame frame, String mode) {
        Thread t = new Thread(() -> {
            updateStatusForTurn(gameState, frame);

            while (!Thread.currentThread().isInterrupted()) {

                if (gameState.isGameOver()) {
                    // Sleep until interrupted (New Game button interrupts us)
                    try { Thread.sleep(Long.MAX_VALUE); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    continue;
                }

                PlayerInt current = gameState.getCurrentPlayer();

                // Show "thinking…" while AI computes
                if (!(current instanceof HumanPlayer)) {
                    frame.setStatus(current.getName() + " is thinking…");
                }

                char[][][] boardCopy = gameState.getBoardCopy();
                Location move = current.getMove(boardCopy);

                // null = move cancelled (human cancel on reset)
                if (move == null) {
                    handleReset(gameState, frame, mode);
                    Thread.interrupted(); // clear flag
                    continue;
                }

                boolean accepted = gameState.makeMove(move);
                if (accepted) {
                    frame.repaintBoard();

                    if (gameState.isGameOver()) {
                        announceResult(gameState, frame);
                    } else {
                        updateStatusForTurn(gameState, frame);
                    }
                }
            }

            // Thread was interrupted → if a reset was pending, relaunch
            if (gameState.consumeResetRequest()) {
                Thread fresh = buildGameLoop(gameState, frame, mode);
                frame.setGameThread(fresh);
                fresh.start();
            }
        });
        t.setDaemon(true);
        return t;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static void updateStatusForTurn(GameState gs, TFrame frame) {
        PlayerInt p = gs.getCurrentPlayer();
        boolean isHuman = p instanceof HumanPlayer;
        String who = isHuman ? p.getName() + "'s turn" : p.getName() + "'s turn";
        frame.setStatus(who + "  (" + p.getLetter() + ")");
        frame.setStatusColored(who + "  (" + p.getLetter() + ")",
            (p.getLetter() == 'X') ? Theme.X_COLOR : Theme.O_COLOR);
    }

    private static void announceResult(GameState gs, TFrame frame) {
        char winner = gs.getWinner();
        if (winner == 'D') {
            frame.setStatusColored("It's a draw!", Theme.FG);
        } else {
            // Find the winning player's name
            String name = gs.getPlayer(0).getLetter() == winner
                          ? gs.getPlayer(0).getName()
                          : gs.getPlayer(1).getName();
            Color  col  = (winner == 'X') ? Theme.X_COLOR : Theme.O_COLOR;
            frame.setStatusColored(name + " wins!  (" + winner + ")", col);
        }
        frame.repaintBoard(); // ensure winning line is painted
    }

    private static void handleReset(GameState gs, TFrame frame, String mode) {
        if (gs.consumeResetRequest()) {
            updateStatusForTurn(gs, frame);
            frame.repaintBoard();
        }
    }
}
