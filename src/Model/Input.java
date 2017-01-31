package Model;
/**
 * Created by benji on 9/30/2016.
 */
public abstract class Input {
    public abstract Guess inputGuess();
    public abstract Card[] getPublicCards(int num);
    public abstract Card[] getOurCards(int num, int ourNum);
    public abstract int getOurNum(int playerNum);
    public abstract int getPlayerNum();
    public abstract boolean getResponse(int playerNum, Guess guess);
    public abstract Card seeCard(Guess guess, int playerNum) throws ClueException;
    public void consistencyCheck(int playerID, int[] list) throws InconsistentDataException {}
}