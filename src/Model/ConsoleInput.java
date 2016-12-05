package Model;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Created by benji on 9/30/2016.
 *
 * This class gets input from the console to run DeepClue
 */

public class ConsoleInput extends Input {

    private static Scanner screenInput = new Scanner(System.in);

    public Guess inputGuess() {
        Card[] cards = new Card[3];
        cards[0] = inputSuspect();
        cards[1] = inputRoom();
        cards[2] = inputWeapon();
        try {
            return new Guess(cards[0], cards[1], cards[2]);
        } catch (GuessException e) {
            e.printStackTrace();
            return null;
        } catch (CardException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Card inputCard() {
        String name = "";
        boolean flag = true;
        while (flag) {
            name = getString("Enter the name of a card: ");
            name.trim();
            if (CardProperties.isValid(name))
                flag = false;
            else
                System.out.printf("Warning %s was not a valid card\n", name);
        }
        try {
            return new Card(name);
        } catch (CardException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getPlayerNum() {
        return getInt(0, 6, "How many people are playing?: ");
    }

    public boolean getResponse(int playerNum, Guess g) {
        return getTrueFalse(String.format("Does player player %d know anything about %s", playerNum, g.toString()));
    }

    public Card[] getOurCards(int num, int ourNum) {
        System.out.printf("You have %d cards in your hand\n", num);
        Card[] cards = new Card[num];
        Card card;
        for (int i = 0; i < num; i++) {
            System.out.printf("Card %d, ", i + 1);
            card =  inputCard();
            card.setPlayerID(ourNum);
            cards[i] = card;
        }
        return cards;
    }

    public Card[] getPublicCards(int num) {
        System.out.printf("There are %d public cards\n", num);
        Card[] cards = new Card[num];
        Card card;
        for (int i = 0; i < num; i++) {
            System.out.printf("Card %d, ", i + 1);
            card = inputCard();
            card.setPlayerID(-2);
            cards[i] = card;
        }
        return cards;
    }

    public int getOurNum(int playerNum) {
        int ourNum = getInt(1, playerNum, "What player # are you from the beginning: ");
        return ourNum;
    }

    private Card inputSuspect() {
        boolean flag = true;
        String name = "";
        while (flag) {
            name = getString("Please enter a Suspect Name: ");
            name = name.trim();
            if (CardProperties.isSuspect(name))
                flag = false;
            else
                System.out.println(String.format("Warning %s is not a valid suspect name. Please try again\n", name));
        }
        try {
            return new Card(name);
        } catch (CardException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Card inputRoom() {
        boolean flag = true;
        String name = "somthing went wrong with the IOHelper getRoom() method";
        while (flag) {
            name = getString("Please enter a Room Name: ");
            name = name.trim();
            if (CardProperties.isRoom(name))
                flag = false;
            else
                System.out.println(String.format("Warning %s is not a valid room name. Please try again\n", name));
        }
        try {
            return new Card(name);
        } catch (CardException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Card inputWeapon() {
        boolean flag = true;
        String name = "somthing went wrong with the IOHelper getWeapon() method";
        while (flag) {
            name = getString("Please enter a Weapon Name: ");
            name = name.trim();
            if (CardProperties.isWeapon(name))
                flag = false;
            else
                System.out.println(String.format("Warning %s is not a valid weapon name. Please try again\n", name));
        }
        try {
            return new Card(name);
        } catch (CardException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean getTrueFalse(String prompt) {
        String str;
        boolean response = true;
        boolean flag = true;
        while (flag) {
            str = getString(prompt);
            if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("t") || str.equalsIgnoreCase("yes") || str.equalsIgnoreCase("y")) {
                flag = false;
                response = true;
            }
            else {
                if (str.equalsIgnoreCase("false") || str.equalsIgnoreCase("f") || str.equalsIgnoreCase("no") || str.equalsIgnoreCase("n")) {
                    flag = false;
                    response = false;
                }
                else {
                    System.out.println("Sorry response does not corespond to true or false. Please try again");
                }
            }
        }
        return response;
    }

    private String getString(String prompt) {
        String inputString;
        System.out.print(prompt);
        inputString = screenInput.nextLine();
        return inputString;
    }

    //  Keeps asking for and int until it gets a valid one in [lowBound, HighBound]
    private int getInt(int lowBound, int highBound, String prompt) {
        int inputNumber = 0; // Initialized because eclipse wants it to be
        String dump;
        boolean entryFlag;
        do {
            System.out.print(prompt);
            entryFlag = false;
            try {
                inputNumber = screenInput.nextInt();
                dump = screenInput.nextLine();
                entryFlag = true;
            } catch (InputMismatchException err) {
                dump = screenInput.nextLine();
                System.out.println(dump + " is not an integer!");

            } // end try catch
            if (inputNumber < lowBound || inputNumber > highBound) {
                entryFlag = false;
                System.out.println(String.format("Number is outside of bounds [%d, %d]\n", lowBound, highBound));
            }
        } while (!entryFlag);
        return inputNumber;
    }
}