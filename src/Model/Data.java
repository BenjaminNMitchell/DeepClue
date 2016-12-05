package Model;

/**
 * This object contains all the data for a game of clue
 */
import java.util.ArrayList;

public class Data {
    private int playerNum; // The number of players in the game.
    private GuessMatrix[] guesses; // Holds all the information about the guesses.
    private Player[] players; // Holds the information about what cards each player could have indexed by playerID - 1.
    private int solutionNum; // A counter for how many cards have been entered into the solution.
    private Card[] solution; // The solution to the game of clue this represents.
    private int[] suspectLocations; // Keep track of the known locations of cards of each type.
    private int[] roomLocations;    // 0 -> No information, -1 -> it cannot be in the envelope. 1 -> its in the envelope
    private int[] weaponLocations;  // any other number is the playerID of the player who has the card.
    private int suspectNum, roomNum, weaponNum; // Counters for how many of each type of card have a known location.
    private ArrayList<Card> hasList;
    private ArrayList<Card> strikeList;
    private ArrayList<Card> strickenList; // Keeps track of the cards we have stricken already

    // Constructor fot the Data object.
    public Data(int playerNum) {
        this.playerNum = playerNum;
        initGuesses(playerNum);
        // initializes players with there id number (i + 1) because of 0 based addressing and 1st based player addressing
        players = new Player[playerNum];
        for (int i = 0; i < playerNum; i++)
            players[i] = new Player(i + 1);
        solutionNum = 0;
        solution = new Card[3];
        suspectLocations = new int[6];
        suspectNum = 0;
        roomLocations = new int[9];
        roomNum = 0;
        weaponLocations = new int[6];
        weaponNum = 0;
        hasList = new ArrayList<>();      // We add cards that we infer the location of to this.
        strikeList = new ArrayList<>();   // We add cards that we know some player cannot have to this list
        strickenList = new ArrayList<>();  // a list of the cards we have stricken so we don't repeat work.
    }

    // Initializes the 324 guess matrices one for each possible guess
    private void initGuesses(int playerNum) {
        guesses = new GuessMatrix[324];
        Card person, place, thing;
        Guess g;
        int index;
        try {
            for (String suspect : CardProperties.suspects) {
                person = new Card(suspect);
                for (String room : CardProperties.rooms) {
                    place = new Card(room);
                    for (String weapon : CardProperties.weapons) {
                        thing = new Card(weapon);
                        try {
                            g = new Guess(person, place, thing);
                            index = CardProperties.getGuessIndex(g);
                            guesses[index] = new GuessMatrix(g, playerNum);
                        } catch (GuessException e) {
                            e.printStackTrace();
                        } catch (ClueException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (CardException e) {
            e.printStackTrace();
        }
    }

    public void initPubCards(Card[] cards) throws ClueException {
        for (Card c : cards) {
            c.setPlayerID(-2);
            hasList.add(c);
        }
        clean();
    }

    public void initOurCards(Card[] cards) throws ClueException {
        for (Card c : cards)
            hasList.add(c);
        clean();
    }

    // This method is called by the playTurn method when a player says they have one or more of the cards in the turns guess.
    public int positive(Guess guess, int playerID) throws ClueException {
        getPlayer(playerID).addGuessIndex(CardProperties.getGuessIndex(guess));
        int[] values = getValues(guess, playerID);
        addGuess(guess, playerID, values);
        int guessIndex = CardProperties.getGuessIndex(guess);
        getPlayer(playerID).addGuessIndex(guessIndex);
        clean();
        return solutionNum;
    }

    // This method is called by the playTurn method when a player says they do not have any of the cards in the turns guess

    public int negative(Guess guess, int playerID) throws ClueException {
        getPlayer(playerID).addStrikeIndex(CardProperties.getGuessIndex(guess));
        int guessIndex = CardProperties.getGuessIndex(guess);
        guesses[guessIndex].rejectID(playerID);
        Card[] strikeCards = guess.getCards();
        for (Card c : strikeCards) {
            c.setPlayerID(playerID);
            strikeList.add(c);
        }
        clean();
        return solutionNum;
    }

    // returns the solution the the game of clue this data object represents.
    // Throws an exception if the solution does not make sense or is asked for at an invalid time
    public Guess getSolution() throws ClueException {
        if (solutionNum != 3)
            throw new ClueException("Error attempting to get solution when solution num != 3");
        else
            try {
                return new Guess(solution[0], solution[1], solution[2]);
            } catch (Exception e) {
                throw new ClueException("Error getting solution: " + e.getMessage());
            }
    }

    // returns the supplied players possibility values for the supplied guess. -1 -> cant have 0 -> no information 1- > has
    private int[] getValues(Guess guess, int playerID) throws ClueException {
        int[] values;
        if (playerID > playerNum || playerID < 0) {
            throw new ClueException(String.format("Error trying to get values in Data for guess: %s, player: %d", guess.toString(), playerID));
        } else {
            if (playerID == 0) {
                values = getEnvelopeValues(guess);
            } else {
                values = getPlayer(playerID).getCardValues(guess);
            }
        }
        return values;
    }

    // This method returns the position in players for the player with id num playerID.
    private Player getPlayer(int playerID) {
        return players[playerID - 1];
    }

    // This method adds a guess to the relevant guess matrix based on the players possibility values.
    // It is recursive because if the added guess causes us to know that the envelope must contain one of the cards
    // in the guess matrix
    private void addGuess(Guess guess, int playerID, int[] values) throws ClueException {
        int guessIndex = CardProperties.getGuessIndex(guess);
        // stores the result of adding playerID to this guess matrix, flags indicate if new information is known.
        boolean[] flags = guesses[guessIndex].addID(playerID, values);
        addNewCards(guessIndex, flags[0], flags[1]);
        if (flags[2])
            addGuess(guess, 0, getValues(guess, 0));
    }

    // This method adds the newly inferred cards from operations that can cause new cards to be inferred.
    private void addNewCards(int guessIndex, boolean newAddCards, boolean newStrikeCards) {
        if (newAddCards) {
            Card[] newACs = guesses[guessIndex].getSolvedArray();
            for (Card c : newACs)
                hasList.add(c);
        }
        if (newStrikeCards) {
            Card[] newSCs = guesses[guessIndex].getStrikeArray();
            for (Card c : newSCs)
                strikeList.add(c);
        }
    }

    // updates data by processing all cards in hasList and strikeList. It assumes that all cards in those lists have an
    // a playerID of who has or does not have that card depending on the list.
    private void clean() throws ClueException {
        Card actionItem;
        while (hasList.size() != 0 || strikeList.size() != 0) {
            if (hasList.size() != 0) {
                actionItem = getNextHas();
                if (hasCheck(actionItem))
                    continue;
                hasCard(actionItem);
            } else {
                actionItem = getNextStrike();
                strikeCheck(actionItem);
                strikeCard(actionItem, actionItem.getPlayerID());
            }
        }
        boolean flag = false;
        if (suspectNum == 5) {
            inferLast(suspectLocations, "suspect");
            flag = true;
        }
        if (weaponNum == 5) {
            inferLast(weaponLocations, "weapon");
            flag = true;
        }
        if (roomNum == 8) {
            inferLast(roomLocations, "room");
            flag = true;
        }
        if (flag)
            clean();
    }

    private void inferLast(int[] list, String type) {
        Card card;
        int index = 0;
        for (int i = 0; i < 6; i++) {
            if (list[i] == 0)
                index = i;
        }
        try {
            if (type.equals("suspect"))
                card = new Card(CardProperties.getSuspectString(index));
            else {
                if (type.equals("room"))
                    card = new Card(CardProperties.getRoomString(index));
                else
                    card = new Card(CardProperties.getWeaponString(index));
            }
            card.setPlayerID(0);
            hasList.add(card);
        } catch (CardException e) {
            // unreachable
        }

    }

    // Checks to see if marking this card a has is consistent with our information if not it throws an exception
    // Returns true if we haven't marked this card before, false otherwise
    private boolean hasCheck(Card c) throws ClueException {
        boolean alreadyUpdated;
        int playerID = c.getPlayerID();
        if (playerID == -1)
            throw new ClueException(String.format("Error card %s reached cardAlreadyUpdatedHas with playerID = -1", c.toString()));
        int value;
        if (c.isSuspect())
            value = suspectLocations[CardProperties.getSuspectCode(c.getName())];
        else
        if (c.isRoom())
            value = roomLocations[CardProperties.getRoomCode(c.getName())];
        else
            value = weaponLocations[CardProperties.getWeaponCode(c.getName())];
        if (value != playerID && value != 0)
            throw new ClueException(String.format("Error inconsistent information trying to change found location card: %s old id: %d, new id: %d"
                    , c.toString(), value, playerID));
        else
            alreadyUpdated = value != 0;
        return alreadyUpdated;
    }

    // Checks to see if we have stricken this card before
    // And if the attribute c.playerID is valid
    private boolean strikeCheck(Card c) throws ClueException {
        int playerID = c.getPlayerID();
        if (playerID == -1)
            throw new ClueException(String.format("Error card %s reached cardAlreadyUpdatedHas with playerID = -1", c.toString()));
        if (getPlayer(playerID).getCardValue(c.getName()) == 1)
            throw new ClueException(String.format("Error trying to strike card %s from player %d when it was previously marked a had", c.toString(), playerID));
        else
            return inStrickenList(c);
    }

    // Checks the lists of stricken cards to see if the supplied card has been struck before for the same playerID
    private boolean inStrickenList(Card card) {
        for (Card c : strickenList)
            if (card.exactlyEquals(c))
                return true;
        return false;

    }

    // gets the next card we know the location of from the hasList and deletes it.
    private Card getNextHas() {
        Card next = hasList.get(0);
        hasList.remove(0);
        return next;
    }

    // gets the next card we know that a player cannot have from strikeList and deletes it.
    private Card getNextStrike() {
        Card next = strikeList.get(0);
        strikeList.remove(0);
        return next;
    }

    // updates the players lists of possible cards and all of the relevant guess matrices
    private void hasCard(Card card) throws ClueException {
        String type = card.getType();
        switch (type) {
            case "suspect" : suspectNum++;
                break;
            case "room" : roomNum++;
                break;
            case "weapon" : weaponNum++;
                break;
        }
        updateListHas(card);
        updateMatricesHas(card);
    }

    // Updates the matrices
    private void updateMatricesHas(Card card) throws ClueException {
        for (int index = 0; index < 324; index ++) {
            GuessMatrix matrix = guesses[index];
            if (matrix.isSolved())
                continue;
            else {
                boolean[] flags = matrix.updateCardHas(card);
                addNewCards(index, flags[0], flags[1]);
            }
        }
    }

    private void strikeCard(Card card, int playerNum) throws ClueException {
        strickenList.add(card);
        updateListStrike(card);
        updateMatricesStrike(card, playerNum);
    }

    // marks the card in playerID's list as has (1) and does not have in all other lists
    private void updateListHas(Card card) throws ClueException {
        int playerID = card.getPlayerID();
        if (playerID != -2) {
            for (int i = 0; i <= playerNum; i++) {
                if (i == playerID)
                    updateList(card, i, 1);
                else
                    updateList(card, i, -1);
            }
        } else {
            for (int i = 0; i <= playerNum; i++)
                updateList(card, i, -1);
        }
    }

    // strikes card's position in card.playerID's list. i.e. sets it to -1 which represents cannot have.
    private void updateListStrike(Card card) throws ClueException {
        int playerID = card.getPlayerID();
        updateList(card, playerID, -1);
    }

    // Updates cards position to be type in playerID's list. Assumes only ever used with type == 1 or -1
    private void updateList(Card card, int playerID, int type) throws ClueException {
        if (type != -1 && type != 1)
            throw new ClueException("Error trying to use updateList with type other then 1 or -1");
        if (playerID < 0 || playerID > playerNum && playerID != -2)
            throw new ClueException(String.format("Error trying to updateListHas %s for player%d's list", card.toString(), playerID));
        else {
            if (playerID > 0) {
                if (type == 1)
                    getPlayer(playerID).hasCard(card);
                else
                    getPlayer(playerID).strikeCard(card);
            } else {
                int[] list;
                int index;
                if (card.isSuspect()) {
                    index = CardProperties.getSuspectCode(card.getName());
                    list = suspectLocations;
                } else {
                    if (card.isRoom()) {
                        index = CardProperties.getRoomCode(card.getName());
                        list = roomLocations;
                    } else {
                        index = CardProperties.getWeaponCode(card.getName());
                        list = weaponLocations;
                    }
                }

                if (list[index] == -type)
                    throw new ClueException(String.format("Error trying to update envelope possibilities to 1 from -1 for card %s", card.toString()));
                list[index] = type;

            }
        }
    }

    private void updateMatricesStrike(Card card, int playerNum) throws ClueException {
        for (int index = 0; index < 324; index ++) {
            GuessMatrix matrix = guesses[index];
            if (matrix.isSolved())
                continue;
            else {
                boolean[] flags = matrix.updateCardStrike(card);
                addNewCards(index, flags[0], flags[1]);
            }
        }
    }

    private int[] getEnvelopeValues(Guess guess) throws ClueException {
        int[] values = new int[3];
        Card[] cards = guess.getCards();
        for (int i = 0; i < cards.length; i++) {
            values[i] = getCardEnvelopeValue(cards[i]);
        }
        return values;
    }

    private int getCardEnvelopeValue(Card c) throws ClueException {
        // initialize to -3 because compiler wants it, and it is a unique value so i will know if its getting sent out.
        // Val will always be set in the switch.
        // Note should I change to an objective way to do this? could be hard
        int val = -3;
        switch (c.getType()) {
            case "suspect": val = suspectLocations[CardProperties.getSuspectCode(c.getName())];
                break;
            case "weapon": val = weaponLocations[CardProperties.getWeaponCode(c.getName())];
                break;
            case "room" : val = roomLocations[CardProperties.getRoomCode(c.getName())];
                break;
        }
        if (val == -3)
            throw new ClueException("getCadEnvelopeValue returning -3");
        if (val == 0 || val == -1 || val == 1)
            return val;
        else
            return -1;


    }
}
