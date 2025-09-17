public interface PlayerInt
{
    /*Returns the letter the player is playing as*/
    public char getLetter();

    /*Returns the location that the player would like to move to.
    This method must receive a copy on the game board, to prevent players from cheating!*/
    public Location getMove(char[][][] board);

    /*Returns the player's name */
    public String getName();

    /*Resets all stored data other than player name and letter*/
    public void reset();
}