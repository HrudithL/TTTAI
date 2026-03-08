import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * An AI player that picks a random empty cell each turn.
 * Serves as a simple baseline opponent and proof that the PlayerInt interface
 * works end-to-end.
 */
public class RandomAIPlayer extends Player {

    private final Random rng = new Random();

    public RandomAIPlayer(char letter, String name) {
        super(letter, name);
    }

    @Override
    public Location getMove(char[][][] board) {
        List<Location> valid = new ArrayList<>();
        for (int s = 0; s < GameState.SIZE; s++)
            for (int r = 0; r < GameState.SIZE; r++)
                for (int c = 0; c < GameState.SIZE; c++)
                    if (board[s][r][c] == ' ')
                        valid.add(new Location(c, r, s));

        if (valid.isEmpty()) return null;
        return valid.get(rng.nextInt(valid.size()));
    }

    @Override
    public void reset() { /* stateless – nothing to clear */ }
}
