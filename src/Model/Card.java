package Model;

/**
 * Created by Ben on 2016-11-21.
 * This Model.Card object stores the information for a card object.
 * Name: The name of the card. One of the 21 cards from the board game clue. Immutable
 * Type: The type of the card. One of "suspect", "room" or "weapon" for the three types of cards in clue. Immutable
 * LocationID: The location of the Model.Card. -2 -> Public Model.Card, -1 -> No information,
 * 0 -> In the envelope, else in that players Hand.
 */
public class Card {
    // The name of this Model.Card.
    private String name;

    // The type of this Model.Card.
    private String type;

    // The location of this Model.Card.
    private int playerID;

    // Constructor for the Model.Card Object checks if the supplied name is valid if it is then it sets the name and type
    // Initializes the locationId to -1 (which signifies no information
    public Card(String name) throws CardException {
        if (! CardProperties.isValid(name))
            throw new CardException("Invalid Model.Card Name: " + name);
        else {
            this.name = name;
            this.type = setType(name);
            playerID = -1;
        }
    }

    // Sets the type of the card based on which set name belongs to in CardNameChecker.
    private String setType(String name) {
        String type;
        if (CardProperties.isRoom(name))
            type = "room";
        else {
            if (CardProperties.isSuspect(name))
                type = "suspect";
            else // it must be a weapon for the card to have goten past the valid check
                type = "weapon";
        }
        return type;
    }

    public int getPlayerID() {
        return playerID;
    }

    // A mutation method for the locationID attribute.
    public void setPlayerID (int playerID) {
        this.playerID = playerID;
    }

    // An accessor for the Name attribute.
    public String getName() {
        return name;
    }

    // An accessor for the Type attribute.
    public String getType() {
        return type;
    }

    // Overrides Object's equals method, returns true iff the supplied object is a Model.Card and its name matches this name
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Card) {
            Card otherCard = (Card) obj;
            return (name.equals(otherCard.name));
        }
        else
            return false;
    }

    public boolean exactlyEquals(Object obj) {
        if (obj instanceof Card) {
            Card otherCard = (Card) obj;
            return (name.equals(otherCard.name) && playerID == otherCard.playerID);
        }
        else
            return false;
    }

    // Overrides Object's Clone method, creates a deep copy of this Model.Card object.
    @Override
    public Card clone() {
        try {
            Card deepCopy = new Card(name);
            deepCopy.setPlayerID(playerID);
            return deepCopy;
        } catch (CardException e) {
            // unreachable name is already verified because were making a copy of an existing card.
        }
        return null;
    }

    // used to check if the type of this card is suspect
    public boolean isSuspect() {
        return type == "suspect";
    }

    // used to check if the type of this card is room
    public boolean isRoom() {
        return type == "room";
    }

    // used to check if the type of this card is weapon
    public boolean isWeapon() {
        return type == "weapon";
    }

    // Creates and returns a String representation of this Model.Guess object. Identical to getName().
    @Override
    public String toString() {
        return name;
    }
}

