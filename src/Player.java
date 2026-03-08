public class Player implements PlayerInt
{
    private final char letter;
    private final String name;

    public Player(char letter, String name) {
        this.letter = letter;
        this.name   = name;
    }

    @Override
    public char getLetter() {
        return letter;
    }

    @Override
    public Location getMove(char[][][] board) {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void reset() {
    }
}
