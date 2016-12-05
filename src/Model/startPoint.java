package Model;


/**
 * Created by benji on 10/3/2016.
 */
import java.util.ArrayList;
public class startPoint {
    public static void main(String[] args) {
        Guess answer = null;
        while (true) {
            try {
                Game g = new Game(1);
                answer = g.playGame();
                System.out.printf("The murder was committed by %s", answer.toString());
            } catch (ClueException e) {
                System.err.println("Error");
                System.err.println(e.getMessage());
            }
        }

    }
}
