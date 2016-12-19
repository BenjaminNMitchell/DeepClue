package Model;

/**
 * This object contains all the data for a game of clue
 */
import java.util.ArrayList;

public class Data {
    private int playerNum; // The number of players in the game.
    private GuessMatrix[] guesses; // Holds all the information about the guesses.
    private ArrayList<GuessMatrix> specialCases; // Holds guesses which nobody has information on
    private Player[] players; // Holds the information about what cards each player could have indexed by playerID - 1.
    private int solutionNum; // A counter for how many cards have been entered into the solution.
    private Card[] solution; // The solution to the game of clue this represents.
    private int[] suspectLocations; // Keep track of the known locations of cards of each type.
    private int[] roomLocations;    // 0 -> No information, -1 -> it cannot be in the envelope. 1 -> its in the envelope
    private int[] weaponLocations;  // any other number is the playerID of the player who has the card.
    private int suspectNum, roomNum, weaponNum; // Counters for how many of each type of card have a known location.
    private ArrayList<Integer> envelopeIndices; // A list of the guess indices that the envelope is in
    private ArrayList<Card> hasList;  // The queue of cards we know the location of for processing.
    private ArrayList<Card> strikeList; // The queue of cards that we have to strike.
    private ArrayList<Card> strickenList; // Keeps track of the cards we have stricken already.
    private ArrayList<Card> foundList; // Keeps track of the cards we know the location of.
    private int handSize; // number of hands in each players hand


    // Constructor fot the Data object.
    public Data(int playerNum) {
        this.playerNum = playerNum;
        initGuesses(playerNum);
        specialCases = new ArrayList<>();
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
        foundList = new ArrayList<>();     // a list of the cards we have marked the location of so we don't repeat work.
        envelopeIndices = new ArrayList<>();
        handSize = (21 - 3) / playerNum;
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
            hasList.add(c);
        }
        for (Card c : cardsWeDontHave)
            strikeList.add(c);
        clean();
    }

    // This method is called by the playTurn method when a player says they have one or more of the cards in the turns guess.
    public int positive(Guess guess, int playerID) throws ClueException {
        positiveCheck(guess, playerID);
        int guessIndex = CardProperties.getGuessIndex(guess);
        getPlayer(playerID).addGuessIndex(guessIndex);
        int[] values = getValues(guess, playerID);
        addGuess(guess, playerID, values);
        clean();
        return solutionNum;
    }

    // This method is called by the playTurn method when a player says they do not have any of the cards in the turns guess
    public int negative(Guess guess, int playerID) throws ClueException {
        negativeCheck(guess, playerID);
        int guessIndex = CardProperties.getGuessIndex(guess);
        getPlayer(playerID).addStrikeIndex(guessIndex);
        guesses[guessIndex].rejectID(playerID);
        Card[] strikeCards = guess.getCards();
        for (Card c : strikeCards) {
            c.setPlayerID(playerID);
            strikeList.add(c);
        }
        clean();
        return solutionNum;
    }

    public void addSpecialCase(Guess guess, int PlayerID) {
//        System.out.println("Added special Case");
    }
    private void positiveCheck(Guess guess, int playerID) throws ClueException {
        int[] values = getValues(guess, playerID);
        if (values[0] == -1 && values[1] == -1 && values[2] == -1)
            throw new ClueException(String.format("Positive - Inconsistent Information: player %d can't have any cards in %s", playerID, guess));
    }


    private void negativeCheck(Guess guess, int playerID) throws ClueException {
        for (int val : getValues(guess, playerID))
            if (val == 1)
                throw new ClueException(String.format("Negative - Inconsistent information: player %d hand one of the card in %s", playerID, guess));
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
        if (flags[2]) {
            envelopeIndices.add(guessIndex);
            addGuess(guess, 0, getValues(guess, 0));
        }
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
                if (actionItem != null)
                    if (hasCheck(actionItem))
                        hasCard(actionItem);
            } else {
                actionItem = getNextStrike();
                if (actionItem != null)
                    if (strikeCheck(actionItem)) ;
                    strikeCard(actionItem, actionItem.getPlayerID());
            }
            cleanPlayers();
            cleanEnvelope();
        }
    }

    // Makes deductions from the state of each player
    private void cleanPlayers() throws ClueException {
        for (Player player : players)
            internalClean(player);
        //crossReference();
    }

    private void internalClean(Player player) throws ClueException {
        Card[] hasCards = player.clean(handSize);
        if (hasCards != null)
            for (Card c : hasCards)
                hasList.add(c);
    }


    private void corssReference() throws ClueException {
        Card infered;
        int unknownSums[] = new int[21];
        for (Player player : players) {
            unknownSums = sumNegs(unknownSums, player.getNegatives());
        }
        for (int i = 0; i < unknownSums.length; i++) {
            if (getEnvVal(i) == -1)
                unknownSums[i]++;
        }
        for (int i = 0; i < unknownSums.length; i++) {
            int sum = unknownSums[i];
            if (sum == playerNum - 1) {
                infered = inferCard(i);
                if (infered != null) {
                    hasList.add(infered);
                }
            }
        }

    }

    private Card inferCard(int cardIndex) throws ClueException {
        int playerNum = -1;
        if (getEnvVal(cardIndex) == 0) {
            playerNum = 0;
        } else {
            for (Player player : players) {
                if (player.getCardValue(cardIndex) == 0)
                    playerNum = player.getPlayerID();
            }
        }
        Card infered = null;
        if (playerNum != -1) {
            infered = new Card(CardProperties.getCardString(cardIndex));
        }
        return infered;
    }

    private int[] sumNegs(int[] sums, int[] negativeIndices) {
        for (int negIndex : negativeIndices) {
            sums[negIndex]++;
        }
        return sums;
    }

    // Makes deductions from the state of the envelope
    private void cleanEnvelope() throws ClueException {
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

    private void inferLast(int[] list, String type) throws ClueException {
        Card card;
        int zeroNum = 0;
        for (int val : list)
            if (val == 0)
                zeroNum++;
        if (zeroNum != 1)
            throw new ClueException(String.format("Data.inferLast() Error called when list has multiple zero entries type %s", type));
        int index = 0;
        for (int i = 0; i < 6; i++) {
            if (list[i] == 0) {
                index = i;
                break;
            }
        }
        try {
            switch (type) {
                case "suspect":
                    card = new Card(CardProperties.getSuspectString(index));
                    break;
                case "room":
                    card = new Card(CardProperties.getRoomString(index));
                    break;
                case "weapon":
                    card = new Card(CardProperties.getWeaponString(index));
                    break;
                default:
                    throw new ClueException(String.format("Data.inferLast: Error type is invalid %s", type));
            }
            card.setPlayerID(0);
            hasList.add(card);
        } catch (CardException e) {
            // unreachable
        }

    }



    private boolean hasCheck(Card c) throws ClueException {
        int playerID = c.getPlayerID();
        if (playerID == -2)
            return true;
        if (playerID > playerNum || playerNum < 0) {
            throw new ClueException(String.format("Error card %s reached Card Already Updated Has with playerID = %d", c, playerID));
        }
        if (playerID != 0) {
            if (getPlayer(playerID).getCardValue(c.getName()) == -1)
                throw new ClueException(String.format("Error hasCheck: Trying to mark: %s for player: %d when it was previously marked as -1", c, playerID));
            return ! inFoundList(c);
        }
        else {
            int value = getCardEnvelopeValue(c);
            if (value == -1)
                throw new ClueException(String.format("Error hascheck: Trying to mark %s in envolope as hand when it was -1", c));
            return value == 0;
        }
    }

    public boolean inFoundList(Card target) throws ClueException {
        for (Card card : foundList) {
            if (card.equals(target)) {
                if (!card.exactlyEquals(target))
                    throw new InconsistentDataException(String.format("Inconsistent information: player %d has %s and player %d originally had it", target.getPlayerID(), target, card.getPlayerID()));
                return true;
            }
        }
        return false;
    }
    // Checks to see if marking this card a has is consistent with our information if not it throws an exception
    // Returns true if we haven't marked this card before, false otherwise
//    private boolean hasCheck(Card c) throws ClueException {
//        boolean alreadyUpdated;
//        int playerID = c.getPlayerID();
//        if (playerID == -1)
//            throw new ClueException(String.format("Error card %s reached cardAlreadyUpdatedHas with playerID = -1", c.toString()));
//        int value;
//        if (c.isSuspect())
//            value = suspectLocations[CardProperties.getSuspectCode(c.getName())];
//        else
//        if (c.isRoom())
//            value = roomLocations[CardProperties.getRoomCode(c.getName())];
//        else
//            value = weaponLocations[CardProperties.getWeaponCode(c.getName())];
//        if (value != playerID && value != 0)
//            throw new ClueException(String.format("Error inconsistent information trying to change found location card: %s old id: %d, new id: %d"
//                    , c.toString(), value, playerID));
//        else
//            alreadyUpdated = value != 0;
//        return alreadyUpdated;
//    }

    // Checks to see if we have stricken this card before
    // And if the attribute c.playerID is valid
    private boolean strikeCheck(Card c) throws ClueException {
        int playerID = c.getPlayerID();
        if (playerID == -1)
            throw new ClueException(String.format("Error card %s reached cardAlreadyUpdatedHas with playerID = -1", c));
        if (getPlayer(playerID).getCardValue(c.getName()) == 1)
            throw new ClueException(String.format("Error trying to strike card %s from player %d when it was previously marked a had", c, playerID));
        else
            return ! inStrickenList(c);
    }

    // Checks the lists of stricken cards to see if the supplied card has been struck before for the same playerID
    private boolean inStrickenList(Card card) {
        for (Card c : strickenList)
            if (card.exactlyEquals(c))
                return true;
        return false;

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

    // updates the players lists of possible cards and all of the relevant guess matrices
    public void hasCard(Card card) throws ClueException {
        if (inFoundList(card))
            return;
        foundList.add(card);
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
        int playerNum = card.getPlayerID();
        int[] indices;
        if (playerNum != -2) {
            if (playerNum != 0)
                indices = getPlayer(playerNum).getGuessIndices();
            else
                indices = getEnvelopeIndices();
            for (int index : indices) {
                GuessMatrix matrix = guesses[index];
                if (matrix.isSolved())
                    continue;
                else {
                    if (matrix.cardIsIn(card)) {
                        boolean[] flags = matrix.updateCardHas(card);
                        addNewCards(index, flags[0], flags[1]);
                    }
                }
            }
        }
    }

    private int[] getEnvelopeIndices() {
        int[] indices = new int[envelopeIndices.size()];
        int i = 0;
        for (int index : envelopeIndices) {
            indices[i] = index;
            i++;
        }
        return indices;
    }

    private void strikeCard(Card card, int playerNum) throws ClueException {
        strickenList.add(card);
        updateListStrike(card);
        updateMatricesStrike(card, playerNum);
    }

    // marks the card in playerID's list as has (1) and does not have in all other lists
    private void updateListHas(Card card) throws ClueException {
        int playerID = card.getPlayerID();
        if (playerID == 0)
            addToSolution(card);
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

    public int[][] getAllValues() {
        int[][] playerData = new int[playerNum + 1][21];
        playerData[0] = getAllEnvVal();
        for (int i = 1; i < playerNum + 1; i++) {
            playerData[i] = getPlayer(i).getIntValues();
        }
        return playerData;
    }


    private void addToSolution(Card card) {
        switch (card.getType()) {
            case "suspect":
                solution[0] = card;
                solutionNum++;
                break;
            case "room":
                solution[1] = card;
                solutionNum++;
                break;
            case "weapon":
                solution[2] = card;
                solutionNum++;
                break;
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
        if ((playerID < 0 || playerID > playerNum) && playerID != -2)
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
        for (int index : getPlayer(playerNum).getGuessIndices()) {
            GuessMatrix matrix = guesses[index];
            if (matrix.isSolved())
                continue;
            else {
                if (matrix.cardIsIn(card)) {
                    boolean[] flags = matrix.updateCardStrike(card);
                    addNewCards(index, flags[0], flags[1]);
                }
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
        int index = CardProperties.getCardCode(c.getName());
        return getEnvVal(index);
    }

    private int[] getAllEnvVal() {
        int[] values = new int[21];
        for (int i = 0; i < 21; i++)
            values[i] = getEnvVal(i);
        return values;
    }
    private int getEnvVal(int index) {
        int val;
        if (index < 6)
            val = weaponLocations[index];
        else {
            if (index < 12)
                val = suspectLocations[index - 6];
            else
                val = roomLocations[index - 12];
        }
        return val;
    }

    public Guess[] getTouchedGuesses() {
        ArrayList<Guess> modedGuesses = new ArrayList<>();
        for (GuessMatrix gm : guesses) {
            if (gm.touched)
                modedGuesses.add(gm.getGuess());
        }
        Guess[] gs = new Guess[modedGuesses.size()];
        return modedGuesses.toArray(gs);
    }

    public void errorPrint() {
        System.out.println("Printing State of Data");
        System.out.println("Global Values:");
        System.out.println("Suspects-");
        for (int i = 0; i < suspectLocations.length; i++) {
            System.out.printf("Suspect %s value %d\n", CardProperties.getSuspectString(i), suspectLocations[i]);
        }
        System.out.println("\nWeapons-");
        for (int i = 0; i < weaponLocations.length; i++) {
            System.out.printf("Weapon %s value %d\n", CardProperties.getWeaponString(i), weaponLocations[i]);
        }
        System.out.println("\nRooms-");
        for (int i = 0; i < roomLocations.length; i++) {
            System.out.printf("room %s value %d\n", CardProperties.getRoomString(i), roomLocations[i]);
        }
        System.out.println("\nPlayer information");
        for (Player p : players) {
            System.out.println(p);
        }

        System.out.println("\n\nGuess matrices:");
        boolean multipleFlag = false;
        for (GuessMatrix gm : guesses) {
            if (gm.touched) {
                if (gm.getAcceptedNum() > 1)
                    multipleFlag = true;
                System.out.println(gm);
            }
        }
        if (! multipleFlag)
            System.out.println("No GMs with multiple accepted IDs");
    }
}
