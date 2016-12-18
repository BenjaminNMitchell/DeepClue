package Model;

import java.util.ArrayList;

/**
 * This class contains the information about a player in the game clue
 */
public class Player {
    private int playerID;
    private int[] possibleCards; // 0 -> no information, -1 -> cannot have, 1 -> has
    private ArrayList<Integer> guessIndices; // a record of the guesses the player has said yes to.
    private ArrayList<Integer> strikeIndices;

    public Player(int playerIdNum) {
        possibleCards = new int[21];
        this.playerID = playerIdNum;
        guessIndices = new ArrayList<>();
        strikeIndices = new ArrayList<>();
    }

    public int getPlayerID() {
        return playerID;
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
        if (! guessIndices.contains(index))
        guessIndices.add(index);
    }

    public void addStrikeIndex(int index) {
        if (! strikeIndices.contains(index))
            strikeIndices.add(index);
    }


    public int[] getCardValues(Guess g) {
        int[] values = new int[3];
        values[0] = getCardValue(g.getPerson().getName());
        values[1] = getCardValue(g.getPlace().getName());
        values[2] = getCardValue(g.getThing().getName());
        return values;

    }

    public int[] getIntValues() {
        return possibleCards.clone();
    }

    public int getCardValue(String name) {
        int index = CardProperties.getCardCode(name);
        return possibleCards[index];
    }

    public int getCardValue(int index) {
        return possibleCards[index];
    }

    public void strikeCard(Card card) {
        possibleCards[CardProperties.getCardCode(card.getName())] = -1;
    }

    // Has prints in side
    public void hasCard(Card c) throws ClueException {
        int index = CardProperties.getCardCode(c.getName());
        if (possibleCards[index] == -1)
            throw new ClueException(String.format("Inconsistant Information player %d could not of had the card %s", playerID, c.toString()));
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

    @Override
    public String toString() {
        String returnString = "";
        returnString += String.format("Card values for player %d:\n", playerID);
        for (int i  = 0; i < possibleCards.length; i++)
            returnString += String.format("Card: %s, Value: %d\n", CardProperties.getCardString(i), possibleCards[i]);
        return returnString;
    }

    public String getCardValues() {
        String str = String.format("Player %d Card Values:", playerID);
        for (int i = 0; i < possibleCards.length; i++) {
            str += String.format("Card: %s, Status: %d", CardProperties.getCardString(i), possibleCards[i]);
        }
        return str;
    }

    // Checks if we can deduce more cards from this player.
    // returns null if there are no deductions to be made.
    public Card[] clean(int handSize) throws ClueException {
        Card[] cards = null;
        // The number of unknown cards are in their hand
        int remainingHandCards = handSize;
        // The number of cards that could be in their hand ie. zeros
        int possibleNum = 0;
        for (int value : possibleCards) {
            if (value == 1)
                remainingHandCards--;
            if (value == 0)
                possibleNum++;
        }
        // We know that all the cards with 0 values must be in their hand
        if (remainingHandCards == possibleNum) {
            cards = inferCards(possibleNum);
        }

        return cards;
    }

    // This method makes the array of cards that have zero values in possibleCards
    // and sets their playerID to this playerID
    public Card[] inferCards(int cardNum) throws ClueException {
        Card[] cards = new Card[cardNum];
        Card infered;
        int cardsIndex = 0;
        for (int i = 0; i < possibleCards.length; i++) {
            if (possibleCards[i] == 0) {
                infered = new Card(CardProperties.getCardString(i));
                infered.setPlayerID(playerID);
                cards[cardsIndex] = infered;
                cardsIndex++;
            }
        }
        return cards;
    }

    public int[] getNegatives() {
        ArrayList<Integer> negatives = new ArrayList<>();
        for (int i = 0; i < possibleCards.length; i++) {
            if (possibleCards[i] == -1) {
                negatives.add(i);
            }
        }
        int[] negIndices = new int[negatives.size()];
        for (int i = 0; i < negatives.size(); i++)
            negIndices[i] = negatives.get(i);
        return negIndices;
    }
}
