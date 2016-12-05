package Model;

/**
 * Created by Ben on 2016-12-03.
 */
public class Game {

    private Input io;
    private Data data;
    private int playerNum;
    private int turnPointer;
    private int solvedNum;
    private int ourNum;
    private int handSize;

    public Game(int inputMode) throws ClueException {
        turnPointer = 1;
        if (inputMode == 0) {
            io = new ConsoleInput();
        } else
            io = new CompGenInput();
        playerNum = io.getPlayerNum();
        ourNum = io.getOurNum(playerNum);
        data =  new Data(playerNum);
        handSize = (21 - 3) / playerNum;
        int leftOverNum = (21 - 3) % playerNum;
        data.initPubCards(io.getPublicCards(leftOverNum));
        data.initOurCards(io.getOurCards(handSize, ourNum));
    }

    public Guess playGame() throws ClueException {
        Guess answer;
        while (true) {
            try {
                playTurn();
                if (solvedNum == 3) {
                    break;
                }
            } catch (ClueException e) {
                System.err.println(e.getMessage());
                continue;
            }
        }
        return data.getSolution();
    }

    public void playTurn() throws ClueException {
        System.out.printf("Starting Turn Number %d\n", turnPointer);
        int playerPointer = (turnPointer % playerNum) + 1;
        boolean flag = true;
        boolean response;
        Guess guess = io.inputGuess();
        while (flag) {
            response = io.getResponse(playerPointer, guess);
            // they have one of these cards check if that information is valid, add to guess matrix, add guess to players, guess indices
            if (response) {
                solvedNum = data.positive(guess, playerPointer);
                flag = false;
            } else {
                solvedNum = data.negative(guess, playerPointer);
                playerPointer++;
                if (playerPointer > playerNum)
                    playerPointer = 1;
            }
        }

        turnPointer++;
    }
}

