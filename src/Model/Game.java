package Model;

/**
 * Created by Ben on 2016-12-03.
 */
import java.util.ArrayList;
public class Game {

    private Input io;
    private Data data;
    private int playerNum;
    private int turnPointer;
    private int playerPointer;
    private int solvedNum;
    private int ourNum;
    private int handSize;
    ArrayList<String> guessLog;



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
        guessLog = new ArrayList<>();
    }
    public Guess playGame(Guess[] order) throws ClueException {
        for (Guess guess : order) {
            try {
                playTurn(guess);
                if (solvedNum == 3)
                    break;
            } catch (ClueException e) {
                dumpData();
                throw e;
            }
        }
        return data.getSolution();
    }

    private void playTurn(Guess guess) throws ClueException {
        // System.out.printf("Starting turn Number %d guess : %s\n", turnPointer, guess.toString());
        playerPointer = (turnPointer % playerNum) + 1;
        boolean flag = false;
        boolean response;
        guessLog.add(guess.toString());
        for (int i = 0; i < playerNum - 1; i++) {
            response = io.getResponse(playerPointer, guess);
            // guessLog.add(String.format("Player # %d : result %s", playerPointer, response));
            flag = processResponse(response, guess);
            if (flag) {
                break;
            }
        }
        if (! flag) {
            passedAround(guess, turnPointer);
        }
        turnPointer++;
        if (io instanceof CompGenInput)
            consistencyCheck(data.getAllValues());
    }

    public void consistencyCheck(int[][] lists) throws InconsistentDataException {
        for (int i = 0; i <= playerNum; i++) {
            io.consistencyCheck(i, lists[i]);
        }
    }
    private void passedAround(Guess guess, int turnPointer) {
        int playerID = turnPointer % playerNum;
        data.addSpecialCase(guess, playerID);
    }


    private boolean processResponse(boolean response, Guess guess) throws ClueException {
        if (response) {
            if (playerPointer != ourNum)
                solvedNum = data.positive(guess, playerPointer);
            if (turnPointer == ourNum)
                data.hasCard(io.seeCard(guess, playerPointer));
        }
        else {
            if (playerPointer != ourNum)
                solvedNum = data.negative(guess, playerPointer);
            playerPointer = incrementPlayerPointer(playerPointer);
        }
        return response;
    }

    public int incrementPlayerPointer(int prevPointer) {
        return (prevPointer % playerNum) + 1;
    }

    public Guess playGame() throws ClueException {
        while (true) {
            if (turnPointer == 1000) {
               throw new ClueException("turnPointer over 1000");
            }
            try {
                playTurn();
                if (solvedNum == 3)
                    break;
            } catch (ClueException e) {
                throw e;
            }
        }
        return data.getSolution();
    }

    public int getTurnNumber() {
        return turnPointer - 1;
    }
    public void playTurn() throws ClueException {
        Guess guess = io.inputGuess();
        playTurn(guess);
    }

    public ArrayList<String> getGuesses() {
       return guessLog;
    }

    public void dumpData() {
        System.out.println("--Error Data--");
        System.out.println("Guess log");
        for (String s : guessLog)
            System.out.println(s);
        data.errorPrint();
    }
}

