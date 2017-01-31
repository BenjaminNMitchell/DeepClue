package Model;
import java.util.ArrayList;

/**
 * This object contains all the data for a game of clue
 */
public class Data {
    private int playerNum;                      // The number of players in the game.
    private GuessMatrix[] guesses;              // Holds all the information about the guesses.
    private ArrayList<GuessMatrix> specialCases;// Holds the cases where a guess passes around the table.
    private Player[] players;                   // Holds the information about what cards each player can have player 0 -> envelope
    private ArrayList<Card> hasList;            // The queue of cards we know the location of for processing.
    private ArrayList<Card> strikeList;         // The queue of cards that we have to strike.
    private ArrayList<Card> strickenList;       // Keeps track of the cards we have stricken already.
    private ArrayList<Card> foundList;          // Keeps track of the cards we know the location of.

    // Constructor fot the Data object.
    public Data(int playerNum) {
        this.playerNum = playerNum;
        int handSize = (21 - 3) / playerNum;
        initGuesses(playerNum);
        players = new Player[playerNum + 1];
        players[0] = new Envelope(0, 3);
        for (int i = 1; i < playerNum + 1; i++)
            players[i] = new Player(i, handSize);
        hasList = new ArrayList<>();      // We add cards that we infer the location of to this.
        strikeList = new ArrayList<>();   // We add cards that we know some player cannot have to this list
        strickenList = new ArrayList<>();  // a list of the cards we have stricken so we don't repeat work.
        foundList = new ArrayList<>();     // a list of the cards we have marked the location of so we don't repeat work.
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

    // Sets the cards as strikes the public cards for all players
    public void initPubCards(Card[] cards) throws ClueException {
        Card temp;
        for (Card c : cards) {
            for (int i = 0; i < playerNum + 1; i++) {
                temp = c.clone();
                temp.setPlayerID(i);
                addToStrikeList(temp);
            }
        }
        clean();
    }

    // Marks our cards as in our hand and strikes it for all other players
    public void initOurCards(Card[] cardsWeHave) throws ClueException {
        int ourNum = cardsWeHave[0].getPlayerID();
        Card card;
        String name;
        ArrayList<Card> cardsWeDontHave = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            name = CardProperties.getCardString(i);
            card = new Card(name);
            card.setPlayerID(ourNum);
            cardsWeDontHave.add(card);
        }
        for (Card c : cardsWeHave) {
            cardsWeDontHave.remove(c);
            addToHasList(c);
        }
        for (Card c : cardsWeDontHave)
            addToStrikeList(c);
        clean();
    }

    // This method is called by the playTurn method when a player says they have one or more of the cards in the turns guess.
    public int positive(Guess guess, int playerID) throws ClueException {
        positiveCheck(guess, playerID);
        int guessIndex = CardProperties.getGuessIndex(guess);
        players[playerID].addGuessIndex(guessIndex);
        addGuess(guess, playerID);
        clean();
        return getSolutionNum();
    }

    private void positiveCheck(Guess guess, int playerID) throws ClueException {
        int[] values = getValues(guess, playerID);
        if (values[0] == -1 && values[1] == -1 && values[2] == -1)
            throw new ClueException(String.format("Positive - Inconsistent Information: player %d can't have any cards in %s", playerID, guess));
    }

    // This method is called by the playTurn method when a player says they do not have any of the cards in the turns guess
    public int negative(Guess guess, int playerID) throws ClueException {
        negativeCheck(guess, playerID);
        int guessIndex = CardProperties.getGuessIndex(guess);
        guesses[guessIndex].rejectID(playerID);
        players[playerID].addStrikeIndex(guessIndex);
        Card[] strikeCards = guess.getCards();
        for (Card c : strikeCards) {
            c.setPlayerID(playerID);
            strikeList.add(c);
        }
        clean();
        return getSolutionNum();
    }

    // Checks that the player : playerID rejection the Guess : guess is valid
    private void negativeCheck(Guess guess, int playerID) throws ClueException {
        for (int val : getValues(guess, playerID))
            if (val == 1)
                throw new ClueException(String.format("Negative - Inconsistent information: player %d hand one of the card in %s", playerID, guess));
    }

    // This method used when a guess makes it all the way back to the person who made the guess.
    void passedAround(Guess guess, int playerID) throws ClueException {
//        Card[] cards = guess.getCards();
//        Card card;
//        int[][] vals = new int[2][];
//        int val;
//        vals[0] = getValues(guess, playerID);
//        for (int i = 0; i < vals[0].length; i++) {
//            val = vals[0][i];
//            if (val == 1) {
//                card = cards[i].clone();
//                card.setPlayerID(0);
//                addToStrikeList(card);
//            }
//            if (val == )
//        }
//        vals[1] = getValues(guess, playerID);
//
//        }
    }

    // This method adds a guess to the relevant guess matrix based on the players possibility values.
    // It is recursive because if the added guess causes us to know that the envelope must contain one of the cards
    // in the guess matrix
    private void addGuess(Guess guess, int playerID) throws ClueException {
        int guessIndex = CardProperties.getGuessIndex(guess);
        int[] values = getValues(guess, playerID);
        // stores the result of adding playerID to this guess matrix, flags indicate if new information is known.
        boolean[] flags = guesses[guessIndex].addID(playerID, values);
        addNewCards(guessIndex, flags[0], flags[1]);
        if (flags[2]) {
            players[0].addGuessIndex(guessIndex);
            addGuess(guess, 0);
        }
    }

    // This method adds the newly inferred cards from operations that can cause new cards to be inferred.
    private void addNewCards(int guessIndex, boolean newAddCards, boolean newStrikeCards) throws ClueException {
        if (newAddCards) {
            Card[] newACs = guesses[guessIndex].getSolvedArray();
            for (Card c : newACs)
                addToHasList(c);
        }
        if (newStrikeCards) {
            Card[] newSCs = guesses[guessIndex].getStrikeArray();
            for (Card c : newSCs)
                addToStrikeList(c);
        }
    }

    // Returns the number cards that we know are in the envelope
    private int getSolutionNum() {
        Envelope env = (Envelope) players[0];
        return env.getSolutionNum();
    }

    // returns the solution the the game of clue this data object represents.
    // Throws an exception if the solution does not make sense or is asked for at an invalid time
    Guess getSolution() throws ClueException {
        Envelope env = (Envelope) players[0];
        return env.getSolution();
    }

    // updates data by processing all cards in hasList and strikeList. It assumes that all cards in those lists have an
    // a playerID of who has or does not have that card depending on the list.
    private void clean() throws ClueException {
        Card actionItem;
        int counter = 0;
        while (hasList.size() != 0 || strikeList.size() != 0) {
            if (counter == 100)
                throw new ClueException("100 loops in clean");
            if (hasList.size() != 0) {
                actionItem = getNextHas();
                if (actionItem != null)
                    if (hasCheck(actionItem))
                        hasCard(actionItem);
            } else {
                actionItem = getNextStrike();
                if (actionItem != null)
                    if (strikeCheck(actionItem))
                        strikeCard(actionItem);
            }
            cleanPlayers();
            crossReference();
            counter++;
        }
    }

    // Makes deductions from the state of each player.
    private void cleanPlayers() throws ClueException {
        for (Player player : players)
            internalClean(player);
    }

    // Checks for card inferences internal to each player.
    private void internalClean(Player player) throws ClueException {
        Card[] hasCards = player.clean();
        if (hasCards != null)
            for (Card c : hasCards)
                addToHasList(c);
    }

    // Checks for card inferences by cross referencing players possible card values.
    private void crossReference() throws ClueException {
        int[][] allVals = getAllValues();
        int sum;
        int zeroTracker = -1;
        int val;
        Card infered;
        for (int i = 0; i < 21; i++) {
            sum = 0;
            for (int j = 0; j < playerNum + 1; j++) {
                val = allVals[j][i];
                if (val == -1) {
                    sum++;
                }
                if (val == 0)
                    zeroTracker = j;
                if (val == 1) {
                    sum = -1;
                    break;
                }
            }
            if (sum == playerNum) {
                if (zeroTracker == -1)
                    throw new ClueException("Problem with crossRefernce() should be one zero and zeroTracker is -1");
                else {
                    infered = new Card(CardProperties.getCardString(i));
                    infered.setPlayerID(zeroTracker);
                    addToHasList(infered);
                }
            }
        }
    }

    // Returns true if the supplied card is in foundList, false otherwise.
    private boolean inFoundList(Card target) throws ClueException {
        for (Card card : foundList) {
            if (card.equals(target)) {
                if (!card.exactlyEquals(target))
                    throw new InconsistentDataException(String.format("Inconsistent information: player %d has %s and player %d originally had it", target.getPlayerID(), target, card.getPlayerID()));
                return true;
            }
        }
        return false;
    }

    // Checks the lists of stricken cards to see if the supplied card has been struck before for the same playerID
    private boolean inStrickenList(Card card) {
        for (Card c : strickenList)
            if (card.exactlyEquals(c))
                return true;
        return false;

    }

    // Adds the supplied card to the strike list provided it has not already been stricken
    private void addToStrikeList(Card c) throws ClueException {
        if (! inStrickenList(c))
            strikeList.add(c);
    }

    // adds the supplied card to the has list provided it has not already been maked as found
    private void addToHasList(Card c) throws ClueException {
        if (! inFoundList(c)) {
            hasList.add(c);
        }
    }

    // gets the next card we know the location of from the hasList and deletes it.
    private Card getNextHas() throws ClueException {
        Card next = null;
        while (hasList.size() > 0) {
            next = hasList.get(0);
            hasList.remove(0);
            if (! inFoundList(next) ) {
                break;
            }
        }
        return next;
    }

    // gets the next card we know that a player cannot have from strikeList and deletes it.
    private Card getNextStrike() {
        Card next = null;
        while (strikeList.size() > 0 ) {
            next = strikeList.get(0);
            strikeList.remove(0);
            if (! inStrickenList(next) ) {
                break;
            }
        }
        return next;
    }


    // updates this data -> card.playerID has the supplied card
    void hasCard(Card card) throws ClueException {
        if (card.getPlayerID() == 0) {
            strikeList.addAll(getOtherOfType(card));
        }
        if (inFoundList(card))
            return;
        foundList.add(card);
        updateListsHas(card);
        updateMatricesHas(card);
    }

    // Checks that c.playerID having Card c is consistent with our other information
    private boolean hasCheck(Card c) throws ClueException {
        int playerID = c.getPlayerID();
        if (playerID == -2)
            return true;
        if (playerID > playerNum || playerNum < 0) {
            throw new ClueException(String.format("Error card %s reached Card Already Updated Has with playerID = %d", c, playerID));
        }
        if (players[playerID].getCardValue(c.getName()) == -1)
            throw new ClueException(String.format("Error hasCheck: Trying to mark: %s for player: %d when it was previously marked as -1", c, playerID));
        return ! inFoundList(c);
    }

    // Updates the all relevant matrices that the playerID stored in the supplied card has that card.
    private void updateMatricesHas(Card card) throws ClueException {
        int playerID = card.getPlayerID();
        int[] indices;
        if (0 >= playerID && playerID <= playerNum) {
            indices = players[playerNum].getGuessIndices();
            for (int index : indices) {
                GuessMatrix matrix = guesses[index];
                if (! matrix.isSolved()) {
                    if (matrix.cardIsIn(card)) {
                        boolean[] flags = matrix.updateCardHas(card);
                        addNewCards(index, flags[0], flags[1]);
                    }
                }
            }
        }
    }

    // Returns an arrayList<Card> containing all cards of the type of the supplied card, without the supplied card.
    private ArrayList<Card> getOtherOfType(Card card) throws ClueException {
        ArrayList<Card> cardsOfType = new ArrayList<>();
        String[] names;
        switch (card.getType()) {
            case "suspect":
                names = CardProperties.suspects;
                break;
            case "room":
                names = CardProperties.rooms;
                break;
            case "weapon":
                names = CardProperties.weapons;
                break;
            default:
                throw new ClueException(String.format("error cannot generate cards of type %s", card.getType()));
        }
        Card c;
        for (String name : names) {
            c = new Card(name);
            c.setPlayerID(0);
            cardsOfType.add(c);
        }
        cardsOfType.remove(card);
        return cardsOfType;
    }

    // updates this data -> card.playerID does not have the supplied Card
    private void strikeCard(Card card) throws ClueException {
        strickenList.add(card);
        updateListStrike(card);
        updateMatricesStrike(card);
    }

    // Checks to see if we have stricken this card before
    // And if the attribute c.playerID is valid
    private boolean strikeCheck(Card c) throws ClueException {
        int playerID = c.getPlayerID();
        if (playerID == -2)
            return true;
        if (playerID == 0) {
            return ! inStrickenList(c);
        }
        if (playerID == -1)
            throw new ClueException(String.format("Error card %s reached cardAlreadyUpdatedHas with playerID = -1", c));
        //if (players[playerID].getCardValue(c.getName()) == 1)
            //throw new ClueException(String.format("Error trying to strike card %s from player %d when it was previously marked a had", c, playerID));
        //else
        return ! inStrickenList(c);
    }

    // strikes card's position in card.playerID's list. i.e. sets it to -1 which represents cannot have.
    private void updateListStrike(Card card) throws ClueException {
        updateList(card, -1);
    }

    // Strikes the card from all matrices and adds new cards to strike and found list if any.
    private void updateMatricesStrike(Card card) throws ClueException {
        for (int index : players[playerNum].getGuessIndices()) {
            GuessMatrix matrix = guesses[index];
            if (! matrix.isSolved()) {
                if (matrix.cardIsIn(card)) {
                    boolean[] flags = matrix.updateCardStrike(card);
                    addNewCards(index, flags[0], flags[1]);
                }
            }
        }
    }

    // marks the card in playerID's list as has (1) and does not have in all other lists
    private void updateListsHas(Card card) throws ClueException {
        Card tempCard;
        int playerID = card.getPlayerID();
        for (int i = 0; i <= playerNum; i++) {
            tempCard = new Card(card.getName());
            tempCard.setPlayerID(i);
            if (i == playerID)
                updateList(tempCard, 1);
            else
                updateList(tempCard, -1);
        }
    }

    // Updates cards position to be type in playerID's list. Assumes only ever used with type == 1 or -1
    private void updateList(Card card, int type) throws ClueException {
        int playerID = card.getPlayerID();
        if (type != -1 && type != 1)
            throw new ClueException("Error trying to use updateList with type other then 1 or -1");
        if ((playerID < 0 || playerID > playerNum) && playerID != -2)
            throw new ClueException(String.format("Error trying to updateListHas %s for player%d's list", card.toString(), playerID));
        if (type == 1)
            players[playerID].hasCard(card);
        else
            players[playerID].strikeCard(card);
    }

    // returns the supplied players possibility values for the supplied guess. -1 -> cant have 0 -> no information 1- > has
    private int[] getValues(Guess guess, int playerID) throws ClueException {
        int[] values = new int[3];
        Card[] cards = guess.getCards();
        for (int i = 0; i < 3; i++) {
            values[i] = getValue(cards[i], playerID);
        }
        return values;
    }

    // Returns all players possible card values.
    int[][] getAllValues() {
        int[][] playerData = new int[playerNum + 1][21];
        for (int i = 0; i < playerNum + 1; i++) {
            playerData[i] = players[i].getIntValues();
        }
        return playerData;
    }

    // Returns playerID's possible card value for the supplied card.
    private int getValue(Card card, int playerID) throws ClueException {
        if (playerID < 0 || playerID > playerNum) {
            throw new ClueException("Invalid playerID not in [0, playerNum]");
        }
        return players[playerID].getCardValue(card.getName());
    }
}
