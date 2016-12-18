package Model;


/**
 * Created by benji on 10/3/2016.
 */
import java.util.ArrayList;
public class startPoint {
    public static void main(String[] args) {
        Guess answer;
        int turnNumber;
        int minTurnNum = 10000;
        ArrayList<String> minGuesses = null;
        for (int i = 0; i < 50; i++) {
            try {
                Game g = new Game(1);
                answer = g.playGame();
                System.out.printf("The murder was committed by %s\n", answer.toString());
                turnNumber = g.getTurnNumber();
                System.out.printf("Calculated in %d turns\n", turnNumber);
                if(turnNumber < minTurnNum) {
                    minTurnNum = turnNumber;
                    minGuesses = g.getGuesses();
                }
            } catch (ClueException e) {
                printData(minTurnNum, minGuesses);
            }
        }
        System.out.println("Completed 50 trials");
        printData(minTurnNum, minGuesses);
    }

    public static void printData(int num, ArrayList<String> guesses) {
        if (guesses != null) {
            System.out.printf("min Turn Num: %d", num);
            for (String s : guesses)
                System.out.println(s);
        } else {
            System.err.println("Problem");
        }
    }
}


