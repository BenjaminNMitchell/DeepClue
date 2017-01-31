package Model;

import java.util.ArrayList;

/**
 * This class stores the information about the cards that could be in the envelope (the solution to this game of clue)
 * It functions as a player with some extra restrictions on the number of cards and number of cards of each type.
 *
 */
public class Envelope extends Player {
    private int solutionNum; // A counter for how many cards have been entered into the solution.
    private Card[] solution; // The solution to the game of clue this represents.

    public Envelope(int playerNum, int handSize) {
        super(playerNum, handSize);
        solutionNum = 0;
        solution = new Card[3];
    }

    public int getSolutionNum() {
        return solutionNum;
    }

    public Guess getSolution() throws ClueException {
        if (solutionNum == 3)
            return new Guess(solution[0], solution[1], solution[2]);
        else
            return null;
    }

    @Override
    public void hasCard(Card card) throws ClueException {
        addToSolution(card);
        super.hasCard(card);
    }

    private void addToSolution(Card card) throws ClueException {
        String type = card.getType();
        int index = -1;
        switch (type) {
            case "suspect" :
                index = 0;
                break;
            case "room" :
                index = 1;
                break;
            case "weapon" :
                index = 2;
                break;
        }
        if (solution[index] == null) {
            solution[index] = card;
            solutionNum++;
        }
        else
            throw new InconsistentDataException(String.format("Trying to reset %s card from %s to %s", type, solution[index], card));
    }


    @Override
    public Card[] clean() throws ClueException {
        ArrayList<Card> cards = new ArrayList<>();
        Card temp;
        for (int i = 1; i < 4; i ++) {
            temp = inferCard(i);
            if (temp != null)
                cards.add(temp.clone());
        }
        Card[] cardsArray = new Card[cards.size()];
        return cards.toArray(cardsArray);
    }

    private Card inferCard(int type) throws ClueException {
        int count;
        Card card = null;
        int startIndex = 0;
        int endIndex = 0;
        if (type != 1 && type != 2 && type != 3)
            throw new ClueException("Calling infer Card with type not in [1, 2, 3]");
        if (type == 1) {
            startIndex = 0;
            endIndex = 6;
        }
        if (type == 2) {
            startIndex = 6;
            endIndex = 12;
        }
        if (type == 3) {
            startIndex = 12;
            endIndex = 21;
        }
        count = getCount(startIndex, endIndex);
        if (count == -1)
            return null;
        int value;
        if (count == endIndex - startIndex - 1)
        for (int i = startIndex; i < endIndex; i++) {
            value = possibleCards[i];
            if (value == 0) {
                card = new Card(CardProperties.getCardString(i));
                card.setPlayerID(0);
            }
        }
        return card;
    }

    private int getCount(int startIndex, int endIndex) {
        int count = 0;
        int value;
        for (int i = startIndex; i < endIndex; i++) {
            value = possibleCards[i];
            if (value == -1)
                count++;
            if (value == 1) {
                count = -1;
                break;
            }
        }
        return count;
    }
}
