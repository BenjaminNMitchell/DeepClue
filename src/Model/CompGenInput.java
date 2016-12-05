package Model;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by benji on 9/30/2016.
 *
 * This class deals out a set of cards and answers games questions
 */
public class CompGenInput extends Input {
    private static final int CARD_NUMBER = 21;
    private Card[][] hands;
    private Card[] publicCards;
    private Card[] envelope;
    private int playerNum = 4;
    private int handSize;


    public CompGenInput() {
        ArrayList<Card> suspects = new ArrayList<>();
        ArrayList<Card> rooms = new ArrayList<>();
        ArrayList<Card> weapons = new ArrayList<>();
        populateLists(suspects, rooms, weapons);
        dealCards(suspects, rooms, weapons);
    }

    // populates the ArrayLists with the proper Cards
    private void populateLists(ArrayList<Card> suspects, ArrayList<Card> rooms, ArrayList<Card> weapons) {
        try {
            for (int i = 0; i < 9; i++) {
                if (i < 6) {
                    suspects.add(new Card(CardProperties.suspects[i]));
                    weapons.add(new Card(CardProperties.weapons[i]));
                }
                rooms.add(new Card(CardProperties.rooms[i]));
            }
        } catch (CardException e) {
            // unreachable
        }
    }

    // Deals out the cards in an arbitrary deterministic way to make testing easier
    private void dealCards(ArrayList<Card> suspects, ArrayList<Card> rooms, ArrayList<Card> weapons) {
        envelope = new Card[3];
        envelope[0] = pop(suspects);
        envelope[1] = pop(rooms);
        envelope[2] = pop(weapons);
        handSize = (CARD_NUMBER - 3) / playerNum;
        int publicCardNum = (CARD_NUMBER - 3) % playerNum;
        publicCards = new Card[publicCardNum];
        for (int i = 0; i < publicCardNum; i++) {
            publicCards[i] = pop(rooms);
        }
        hands = new Card[playerNum][handSize];
        Card c;
        for (int i = 0 ; i < playerNum; i++) {
            for (int j = 0; j < handSize; j++) {
                c = pop(suspects);
                if (c == null)
                    c = pop(weapons);
                if (c == null)
                    c = pop(rooms);
                hands[i][j] = c;
            }
        }
    }

    // returns the first element of an arrayList and removes it.
    private Card pop(ArrayList<Card> list) {
        if (list.size() > 0) {
            Card c = list.get(0);
            list.remove(0);
            return c;
        } else
            return null;
    }

    public void display() {
        System.out.println("Displaying Card Locations");
        System.out.print("Envelope:");
        for (int i = 0; i < 3; i++) {
            System.out.print(envelope[i].toString() + " ");
        }
        System.out.println("\n");
        System.out.print("Public Cards:");
        for (int i = 0; i < publicCards.length; i++)
            System.out.print(publicCards[i].toString() + " ");
        System.out.println("\n");
        System.out.println("Hands:");
        for (int i = 0; i < playerNum; i++) {
            System.out.printf("Player %s's Hand: ", i + 1);
            for (int j = 0; j < hands[0].length; j++)
                System.out.print(hands[i][j].toString() + " ");
            System.out.println("\n");
        }
    }

    public Guess inputGuess() {
        Random random = new Random();
        try {
            Card suspect = new Card(CardProperties.suspects[random.nextInt(6)]);
            Card room = new Card(CardProperties.rooms[random.nextInt(9)]);
            Card weapon = new Card(CardProperties.weapons[random.nextInt(6)]);
            return new Guess(suspect, room, weapon);
        } catch (CardException e) {
            System.err.println(e.getMessage());
            return null;
        } catch (GuessException e) {
            System.err.println(e.getMessage());
            return null;

        }

    }

    public Card[] getPublicCards(int num) {
        int len = publicCards.length;
        Card c;
        Card[] pCs = new Card[len];
        for (int i = 0; i < len; i++) {
            c = publicCards[i].clone();
            c.setPlayerID(-2);
            pCs[i] = c;
        }
        return pCs;
    }

    public Card[] getOurCards(int num, int ourNum) {
        Card[] ourCards = new Card[handSize];
        Card c;
        for (int i = 0; i < handSize; i++) {
            c = hands[ourNum - 1][i].clone();
            c.setPlayerID(ourNum);
            ourCards[i] = c;
        }
        return ourCards;
    }

    // stub for testing
    public int getOurNum(int playerNum) {
        return 1;
    }

    // stub for testing
    public int getPlayerNum() {
        return 4;
    }

    public boolean getResponse(int playerNum, Guess guess) {
        Card[] cards = guess.getCards();
        for (Card c : cards) {
            for (int i = 0; i < handSize; i++)
                if (hands[playerNum - 1][i].equals(c))
                    return true;
        }
        return false;
    }
}