package Model;


/**
 * The start point for the back end of the deep clue object.
 */

public class startPoint {
    public static void main(String[] args) {
        try {
            Game game = new Game(1);
            Guess answer = game.playGame();
            System.out.printf("The answer is %s\n", answer);
            System.out.println(game.getCardLocations());
        } catch (ClueException e) {
            System.out.println(e.getMessage());
        }
    }
}


