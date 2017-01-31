package Model;
import java.util.ArrayList;

/**
 * This class represents a game of clue it continuously accepts new guesses and the results of each guess handling
 * the turn order, and guess response order. It terminates when a unique solution to this game of clue is found.
 * Created by Ben on 2016-12-03.
 */
public class Game {

    private Input input;                // The source of input into the system.
    private Data data;                  // The state of the game.
    private int playerNum;              // The number of players in this game of clue.
    private int turnPointer;            // Keeps track of what number turn it is.
                                            // Turn's playerID can be found using turnPointer % playerNum.
                                            // Players indexed from 1.
    private int playerPointer;          // Keeps track of who is responding to guesses.
    private int solvedNum;              // Keeps track of the number of cards in the envelop which have been solved.
    private int ourNum;                 // Stores playerID is ours.
    private ArrayList<String> guessLog; // Keeps track of all the guesses made during this game.


    public Game(int inputMode) throws ClueException {
        if (inputMode == 0) {
            input = new ConsoleInput();
        } else
            input = new CompGenInput();
        setUp();
    }

    // Secondary constructor for the Game object used for debugging.
    // Initializes card locations based on the result of getCardLocations() from another Game object.
    public Game(ArrayList<String> cardLocations) throws ClueException {
        input = new CompGenInput(cardLocations);
        setUp();
    }

    // Initializes the attributes of this Game, from the input.
    public void setUp() throws ClueException {
        turnPointer = 1;
        playerNum = input.getPlayerNum();
        ourNum = input.getOurNum(playerNum);
        data =  new Data(playerNum);
        int handSize = (21 - 3) / playerNum;
        int leftOverNum = (21 - 3) % playerNum;
        data.initPubCards(input.getPublicCards(leftOverNum));
        data.initOurCards(input.getOurCards(handSize, ourNum));
        guessLog = new ArrayList<>();
    }

    // Runs the a game of clue.
    public Guess playGame() throws ClueException {
        while (true) {
            if (turnPointer == 1000) { // This is clearly a unreasonably large number of turns.
                throw new ClueException("turnPointer over 1000");
            }
            playTurn();
            if (solvedNum == 3)
                break;
        }
        return data.getSolution();
    }

    // A secondary version of playGame which runs a set of guesses, used in conjunction with the
    // Secondary constructor for debugging
    public Guess playGame(Guess[] order) throws ClueException {
        for (Guess guess : order) {
            playTurn(guess);
            if (solvedNum == 3)
                break;
        }
        return data.getSolution();
    }

    // Plays a single turn on a guess from input.
    private void playTurn() throws ClueException {
        Guess guess = input.inputGuess();
        playTurn(guess);
    }

    // Plays one turn and stores the result.
    private void playTurn(Guess guess) throws ClueException {
        // System.out.printf("Starting turn Number %d guess : %s\n", turnPointer, guess.toString());
        playerPointer = (turnPointer % playerNum) + 1;
        boolean response = false;
        guessLog.add(guess.toString() + String.format(" guessed by %d", turnPointer));
        for (int i = 0; i < playerNum - 1; i++) {
            response = input.getResponse(playerPointer, guess);
            processResponse(response, guess);
            if (response) {
                break;
            }
        }
        if (! response) {
            passedAround(guess, turnPointer);
        }
        turnPointer++;
        if (input instanceof CompGenInput)
            consistencyCheck(data.getAllValues());
    }

    // Checks if there are any inconsistencies i.e. information that contradicts itself.
    // This occurs when uses enter incorrect information.
    private void consistencyCheck(int[][] lists) throws InconsistentDataException {
        for (int i = 0; i <= playerNum; i++) {
            input.consistencyCheck(i, lists[i]);
        }
    }

    // Called when everybody responds negatively to the guess.
    private void passedAround(Guess guess, int turnPointer) throws ClueException {
        int playerID = turnPointer % playerNum;
        data.passedAround(guess, playerID);
    }

    // Enters the response by the playerID in playerPointer to the supplied guess into data.
    private void processResponse(boolean response, Guess guess) throws ClueException {
        if (response) {
            if (playerPointer != ourNum)
                solvedNum = data.positive(guess, playerPointer);
            if (turnPointer == ourNum)
                data.hasCard(input.seeCard(guess, playerPointer));
        }
        else {
            if (playerPointer != ourNum)
                solvedNum = data.negative(guess, playerPointer);
            playerPointer = incrementPlayerPointer(playerPointer);
        }
    }

    // Moves playerPointer to the next player
    private int incrementPlayerPointer(int prevPointer) {
        return (prevPointer % playerNum) + 1;
    }

    // Returns the number of turns played in this game
    public int getTurnNumber() {
        return turnPointer - 1;
    }

    // Returns the guesses made so far in this game
    public ArrayList<String> getGuesses() {
       return guessLog;
    }

    // This method is used for debugging with a known dealing of cards -> The secondary constructor
    // Because we know where they are we can check the guess returned from play game to make sure its right.
    public boolean checkAnswer(Guess guess) throws ClueException {
        if (input instanceof CompGenInput) {
            return (((CompGenInput) input).getAnswer().equals(guess));
        } else {
            throw new ClueException("Cannot get the answer for a Console Input");
        }
    }

    // Returns the locations of the cards in input provided input is an instance of CompGenInput and store such info.
    public ArrayList<String> getCardLocations() {
        if (input instanceof CompGenInput) {
            CompGenInput input = (CompGenInput) this.input;
            return input.getLocations();
        } else {
            return null;
        }
    }
}

