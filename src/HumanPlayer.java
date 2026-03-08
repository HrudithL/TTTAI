import java.util.concurrent.LinkedBlockingQueue;

/**
 * A human player whose move is supplied by a mouse click forwarded from TPanel.
 *
 * {@link #getMove} blocks the game-loop thread until {@link #offerMove} is called
 * by the Swing event thread, or until {@link #cancel} aborts the wait (e.g. on reset).
 */
public class HumanPlayer extends Player {

    // null is used as the cancellation sentinel
    private final LinkedBlockingQueue<Location> queue = new LinkedBlockingQueue<>();

    public HumanPlayer(char letter, String name) {
        super(letter, name);
    }

    /** Called by TPanel's MouseListener when the user clicks a valid cell. */
    public void offerMove(Location loc) {
        queue.clear();
        queue.offer(loc);
    }

    /**
     * Unblocks {@link #getMove} by injecting a null sentinel.
     * Typically called when the user requests a game reset.
     */
    public void cancel() {
        queue.clear();
        queue.offer(new Location(-1, -1, -1)); // sentinel – game loop will ignore
    }

    // -------------------------------------------------------------------------
    // PlayerInt
    // -------------------------------------------------------------------------

    @Override
    public Location getMove(char[][][] board) {
        try {
            Location loc = queue.take();
            // Sentinel (negative coords) means the move was cancelled (e.g. reset)
            if (loc.getCol() < 0) return null;
            return loc;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public void reset() {
        queue.clear();
    }
}
