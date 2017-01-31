package Model;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by benji on 9/30/2016.
 *
 * This class automates game play input. It deals out the card and answers questions. Used for testing purposes.
 *
 */
public class CompGenInput extends Input {
    private static final int CARD_NUMBER = 21;
    private Random random;
    private Card[][] hands;
    private Card[] publicCards;
    private Card[] envelope;
    private int playerNum;
    private int handSize;
    private int ourNum;


    public CompGenInput() {
        random = new Random();
        playerNum = random.nextInt(5) + 2;
        ourNum = random.nextInt(playerNum) + 1;
        handSize = (CARD_NUMBER - 3) / playerNum;
        ArrayList<Card> suspects = new ArrayList<>();
        ArrayList<Card> rooms = new ArrayList<>();
        ArrayList<Card> weapons = new ArrayList<>();
        populateLists(suspects, rooms, weapons);
        dealCardsRandomly(suspects, rooms, weapons);
    }

    public CompGenInput(ArrayList<String> locations) throws ClueException {
        String[] parts = locations.get(0).split(" ");
        Card card;
        playerNum = Integer.parseInt(parts[1]);
        ourNum = Integer.parseInt(parts[3]);
        handSize = (CARD_NUMBER - 3) / playerNum;
        hands = new Card[playerNum][handSize];
        envelope = new Card[3];
        parts = locations.get(1).split("-");
        for (int i = 1; i < parts.length; i++) {
            envelope[i - 1] = new Card(parts[i]);
        }
        parts = locations.get(2).split("-");

        publicCards = new Card[parts.length - 1];
        for (int i = 1; i < parts.length; i++) {
            card = new Card(parts[i]);
            card.setPlayerID(-2);
            publicCards[i - 1] = card;
        }
        for (int i = 3; i < locations.size(); i++) {
            hands[i-3] = parseHand(handSize, locations.get(i), i - 2);
        }
    }

    private Card[] parseHand(int handSize, String line, int playerID) throws ClueException {
        Card[] hand = new Card[handSize];
        Card card;
        String[] parts = line.substring(11).split("-");
        if (parts.length - 1 != handSize)
            throw new ClueException("Invalid Comp Gen Input setup");
        else {
            for (int i = 0; i < handSize; i++) {
                card = new Card(parts[i + 1]);
                card.setPlayerID(playerID);
                hand[i] = card;
            }
        }
        return hand;
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


    // Deals out the cards randomly
    private void dealCardsRandomly(ArrayList<Card> suspects, ArrayList<Card> rooms, ArrayList<Card> weapons) {
        envelope = new Card[3];
        envelope[0] = getRandomCard(suspects);
        envelope[1] = getRandomCard(rooms);
        envelope[2] = getRandomCard(weapons);
        hands = new Card[playerNum][handSize];
        Card card;
        ArrayList<Card> remainingCards = new ArrayList<>();
        remainingCards.addAll(suspects);
        remainingCards.addAll(rooms);
        remainingCards.addAll(weapons);
        for (int i = 0 ; i < playerNum; i++) {
            for (int j = 0; j < handSize; j++) {
                card = getRandomCard(remainingCards);
                card.setPlayerID(i + 1);
                hands[i][j] = card;
            }
        }
        int publicNum = remainingCards.size();
        publicCards =  new Card[publicNum];
        for (int i = 0; i < publicNum; i++) {
            card = remainingCards.get(i);
            card.setPlayerID(-2);
            publicCards[i] = card;
        }
    }

    // Deals out the cards in an arbitrary deterministic way to make testing easier
    private void dealCardsFixed(ArrayList<Card> suspects, ArrayList<Card> rooms, ArrayList<Card> weapons) {
        envelope = new Card[3];
        envelope[0] = pop(suspects);
        envelope[1] = pop(rooms);
        envelope[2] = pop(weapons);
        int publicCardNum = (CARD_NUMBER - 3) % playerNum;
        publicCards = new Card[publicCardNum];
        for (int i = 0; i < publicCardNum; i++) {
            publicCards[i] = pop(rooms);
        }
        ArrayList<Card> remainingCards = new ArrayList<>();
        remainingCards.addAll(suspects);
        remainingCards.addAll(rooms);
        remainingCards.addAll(weapons);
        hands = new Card[playerNum][handSize];
        for (int i = 0 ; i < playerNum; i++) {
            for (int j = 0; j < handSize; j++) {
                hands[i][j] = pop(remainingCards);
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

    private Card getRandomCard(ArrayList<Card> cards) {
        Random random = new Random(System.currentTimeMillis());
        int index = random.nextInt(cards.size());
        Card card = cards.get(index);
        cards.remove(index);
        return card;
    }

    // When the player is the one asking the question they get to see the actual card
    public Card seeCard(Guess guess, int playerNum) throws ClueException {
        Card returnCard;
        Card[] playersHand = hands[playerNum - 1];
        Card[] targetCards = guess.getCards();
        for (Card handCard : playersHand) {
            for (Card targetCard : targetCards) {
                if (handCard.equals(targetCard)) {
                    returnCard = targetCard.clone();
                    returnCard.setPlayerID(playerNum);
                    return returnCard;
                }
            }
        }
        throw new ClueException(String.format("No card in %s is in player %d's hand", guess.toString(), playerNum));
    }

    // Displays the state of this hand object
    public void display() {
        System.out.println("Displaying Card Locations");
        System.out.print("Envelope:");
        for (int i = 0; i < 3; i++) {
            System.out.print(envelope[i].toString() + " ");
        }
        System.out.println("\n");
        System.out.print("Public Cards:");
        for (Card card : publicCards)
            System.out.print(card.toString() + " ");
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

    public Card[] getPublicCards(int DUMMY_VAR) {
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

    public Card[] getOurCards(int handSize, int ourNum) {
        Card[] ourCards = new Card[handSize];
        Card c;
        for (int i = 0; i < handSize; i++) {
            ourCards[i] = hands[ourNum - 1][i].clone();
//            c.setPlayerID(ourNum);
//            ourCards[i] = c;
        }
        return ourCards;
    }

    // stub for testing
    public int getOurNum(int dummy) {
        return ourNum;
    }

    // stub for testing
    public int getPlayerNum() {
        return playerNum;
    }

    public void consistencyCheck(int playerID, int[] list) throws InconsistentDataException {
        Card card = null;
        int val;
        Card[] values;
        if (playerID == 0) {
            values = envelope;
        } else {
            values = hands[playerID - 1];
        }
        for (int i = 0; i < 21; i++) {
            val = list[i];

            try {
                card = new Card(CardProperties.getCardString(i));
            } catch (ClueException e) {
                // unreachable
            }
            boolean isIn = isInHand(values, card);
            if (val == -1)
                if (isIn)
                    throw new InconsistentDataException(String.format("player ID %d has %s but possibleCards value is -1", playerID, card));
            if (val == 1) {
                if (! isIn)
                    throw new InconsistentDataException(String.format("player ID %d does not have %s but possibleCards value is 1", playerID, card));
            }
        }

    }

    public Guess getAnswer() throws ClueException {
        return new Guess(envelope[0], envelope[1], envelope[2]);
    }

    private boolean isInHand(Card[] list, Card target) {
        for (Card card : list)
            if (card.equals(target))
                return true;
        return false;
    }

    public boolean getResponse(int playerNum, Guess guess) {
        Card[] cards = guess.getCards();
        for (Card card : cards) {
            if(getResponse(playerNum, card))
                return true;
        }
        return false;
    }

    public boolean getResponse(int playerNum, Card card) {
        for (Card handCard : hands[playerNum - 1]) {
            if (handCard.equals(card))
                return true;
        }
        return false;
    }

    public ArrayList<String> getLocations() {
        ArrayList<String> locations = new ArrayList<>();
        locations.add(String.format("PlayerNum: %d OurNum: %d", playerNum, ourNum));
        locations.add(String.format("Envelope:-%s-%s-%s", envelope[0], envelope[1], envelope[2]));
        StringBuffer str = new StringBuffer();
        str.append("Public Cards:");
        for (Card pubCard : publicCards) {
            str.append("-" + pubCard);
        }
        locations.add(str.toString());
        for (int i =0; i < playerNum; i++) {
            str = new StringBuffer();
            str.append("Player : " + i + " ");
            for (int j = 0; j < handSize; j++) {
                str.append("-" + hands[i][j]);
            }
            locations.add(str.toString());
        }
        return locations;
    }
}