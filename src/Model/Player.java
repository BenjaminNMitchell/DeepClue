package Model;

import java.util.ArrayList;

/**
 * This class contains the information about a player in the game clue
 */
public class Player {
    private int playerIdNum;
    private int[] possibleCards; // 0 -> no information, -1 -> cannot have, 1 -> has
    private ArrayList<Integer> guessIndices; // a record of the guesses the player has said yes to.
    private ArrayList<Integer> strikeIndices;

    public Player(int playerIdNum) {
        possibleCards = new int[21];
        this.playerIdNum = playerIdNum;
        guessIndices = new ArrayList<>();
        strikeIndices = new ArrayList<>();
    }

    public int getPlayerIdNum() {
        return playerIdNum;
    }

    public int[] getGuessIndices() {
        int size = guessIndices.size();
        int[] indicies = new int[size];
        for (int i = 0; i < size; i++)
            indicies[i] = guessIndices.get(i);
        return indicies;
    }

    // What is this for?
    public boolean isInGuessIndices(int index) {
        boolean isIn = false;
        for (int i : guessIndices)
            if (i == index)
                isIn = true;
        return isIn;
    }

    public boolean isInStrikeIndices(int index) {
        boolean isIn = false;
        for (int i : strikeIndices)
            if (i == index)
                isIn = true;
        return isIn;
    }

    void addGuessIndex(int index) {
        guessIndices.add(index);
    }

    public void addStrikeIndex(int index) {
        strikeIndices.add(index);
    }

    public int[] getCardValues(Guess g) {
        int[] values = new int[3];
        values[0] = getCardValue(g.getPerson().getName());
        values[1] = getCardValue(g.getPlace().getName());
        values[2] = getCardValue(g.getThing().getName());
        return values;

    }

    public int getCardValue(String name) {
        int index = CardProperties.getCardCode(name);
        return possibleCards[index];
    }

    public void strikeCard(Card card) {
        possibleCards[CardProperties.getCardCode(card.getName())] = -1;
    }

    // Has prints in side
    public void hasCard(Card c) throws ClueException {
        int index = CardProperties.getCardCode(c.getName());
        if (possibleCards[index] == -1)
            throw new ClueException(String.format("Inconsistant Information player %d could not of had the card %s", playerIdNum, c.toString()));
        possibleCards[CardProperties.getCardCode(c.getName())] = 1;
//        System.out.printf("Card %s set to 1 in player %d's card list\n", c.getName(), playerIdNum);
    }

    public void strikeCards(Guess guess) {
        Card person, place, thing;
        person = guess.getPerson();
        place = guess.getPlace();
        thing = guess.getThing();
        strikeCard(person);
        strikeCard(place);
        strikeCard(thing);
    }

    public void showCardValues() {
        System.out.printf("Card values for player %d:\n", playerIdNum);
        for (int i  = 0; i < possibleCards.length; i++)
            System.out.printf("Card: %s, Value: %d\n", CardProperties.getCardString(i), possibleCards[i]);
    }

    public String getCardValues() {
        String str = String.format("Player %d Card Values:", playerIdNum);
        for (int i = 0; i < possibleCards.length; i++) {
            str += String.format("Card: %s, Status: %d", CardProperties.getCardString(i), possibleCards[i]);
        }
        return str;
    }
}
