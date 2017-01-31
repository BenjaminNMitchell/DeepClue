package Model;

/**
 * Contains the static properties of the card object.
 * Separated to make it easier to read.
 */
public class CardProperties {
    // Stores the names of Weapon cards
    public static String[] weapons = {"Candlestick", "Lead Pipe", "Revolver", "Rope", "Knife", "Wrench"};
    // Stores the names of suspect cards
    public static String[] suspects = {"Miss Scarlett", "Professor Plum", "Mrs. Peacock", "Mr. Green", "Colonel Mustard", "Mrs. White"};
    // Stores the names of room cards
    public static String[] rooms = {"Ballroom", "Billiards Room", "Conservatory", "Dining Room", "Hall", "Library", "Lounge", "Kitchen", "Study"};


    // Returns true if the String parameter is a weapon suspect or room name
    public static boolean isValid(String target) {
        return isWeapon(target) || isSuspect(target) || isRoom(target);
    }

    // Returns true if the supplied Card is is of type weapon,returns false otherwise
    public static boolean isWeapon(Card card) {
        return card.getType().equals("weapon");
    }
    // Returns true if the supplied String is a weapon name,returns false otherwise
    public static boolean isWeapon(String target) {
        return isIn(target, weapons);
    }

    // Returns true if the supplied Card is of type suspect,returns false otherwise
    public static boolean isSuspect(Card card) {
        return card.getType().equals("suspect");
    }

    // Returns true if the supplied String is a suspect name,returns false otherwise
    public static boolean isSuspect(String target) {
        return isIn(target, suspects);
    }

    // returns true if the supplied card is of type Room,returns false otherwise
    public static boolean isRoom(Card card) {
        return card.getType().equals("room");
    }

    // Returns true if the supplied String is a room name,returns false otherwise
    public static boolean isRoom(String target) {
        return isIn(target, rooms);
    }

    // Returns an integer representation of the supplied guess used for indexing
    public static int getGuessIndex(Guess guess) {
        int counter = 0;
        counter += getSuspectCode(guess.getPerson().getName()) *  9 * 6;
        counter += getRoomCode(guess.getPlace().getName()) * 6;
        counter += getWeaponCode(guess.getThing().getName());
        return counter;
    }

    // Returns true if the supplied string is in the supplied list
    private static boolean isIn(String target, String[] list) {
        for (String name : list)
            if (target.equals(name))
                return true;
        return false;
    }

    // Returns the name of the card corresponding to the code supplied code. Inverse of getCardCode(code)
    public static String getCardString(int code) throws ClueException {
        String result;
        if (0 <= code && code <= 5)
            result = getWeaponString(code);
        else {
            if (6 <= code && code < 11)
            result = getSuspectString(code - 6);
            else {
                if ( code <= 20)
                    result = getRoomString(code - 12);
                else
                    throw new ClueException("Error attempting string name codes must be in [0, 20]");

            }
        }
        return result;
    }

    // Returns the code for the card corresponding to the supplied name. Inverse of getCardString(name)
    public static int getCardCode(String name) {
        if (name.equals("Candlestick"))
            return 0;
        if (name.equals("Lead Pipe"))
            return 1;
        if (name.equals("Revolver"))
            return 2;
        if (name.equals("Rope"))
            return 3;
        if (name.equals("Knife"))
            return 4;
        if (name.equals("Wrench"))
            return 5;
        if (name.equals("Miss Scarlett"))
            return 6;
        if (name.equals("Professor Plum"))
            return 7;
        if (name.equals("Mrs. Peacock"))
            return 8;
        if (name.equals("Mr. Green"))
            return 9;
        if (name.equals("Colonel Mustard"))
            return 10;
        if (name.equals("Mrs. White"))
            return 11;
        if (name.equals("Ballroom"))
            return 12;
        if (name.equals("Billiards Room"))
            return 13;
        if (name.equals("Conservatory"))
            return 14;
        if (name.equals("Dining Room"))
            return 15;
        if (name.equals("Hall"))
            return 16;
        if (name.equals("Library"))
            return 17;
        if (name.equals("Lounge"))
            return 18;
        if (name.equals("Kitchen"))
            return 19;
        return 20;
    }

    // Converts the supplied weapon name to a numeric code.
    public static int getWeaponCode(String name) {
        if (name.equals("Candlestick"))
            return 0;
        if (name.equals("Lead Pipe"))
            return 1;
        if (name.equals("Revolver"))
            return 2;
        if (name.equals("Rope"))
            return 3;
        if (name.equals("Knife"))
            return 4;
        return 5;
    }

    // Converts the supplied room name to a numeric code.
    public static int getRoomCode(String name) {
        if (name.equals("Ballroom"))
            return 0;
        if (name.equals("Billiards Room"))
            return 1;
        if (name.equals("Conservatory"))
            return 2;
        if (name.equals("Dining Room"))
            return 3;
        if (name.equals("Hall"))
            return 4;
        if (name.equals("Library"))
            return 5;
        if (name.equals("Lounge"))
            return 6;
        if (name.equals("Kitchen"))
            return 7;
        return 8;
    }

    // Converts the supplied suspect name to a numeric code.
    public static int getSuspectCode(String name) {
        if (name.equals("Miss Scarlett"))
            return 0;
        if (name.equals("Professor Plum"))
            return 1;
        if (name.equals("Mrs. Peacock"))
            return 2;
        if (name.equals("Mr. Green"))
            return 3;
        if (name.equals("Colonel Mustard"))
            return 4;
        return 5;

    }

    // Converts the supplied weapon code to the corresponding name string.
    public static String getWeaponString(int code) {
        if (code == 0)
            return "Candlestick";
        if (code == 1)
            return "Lead Pipe";
        if (code == 2)
            return "Revolver";
        if (code == 3)
            return "Rope";
        if (code == 4)
            return "Knife";
        else
            return "Wrench";
    }

    // Converts the supplied room code to the corresponding name string.
    public static String getRoomString(int code) {
        if (code == 0)
            return "Ballroom";
        if (code == 1)
            return "Billiards Room";
        if (code == 2)
            return "Conservatory";
        if (code == 3)
            return "Dining Room";
        if (code == 4)
            return "Hall";
        if (code == 5)
            return "Library";
        if (code == 6)
            return "Lounge";
        if (code == 7)
            return "Kitchen";
        return "Study";
    }

    // Converts the supplied suspect code to the corresponding name string.
    public static String getSuspectString(int code) {
        if (code == 0)
            return "Miss Scarlett";
        if (code == 1)
            return "Professor Plum";
        if (code == 2)
            return "Mrs. Peacock";
        if (code == 3)
            return "Mr. Green";
        if (code == 4)
            return "Colonel Mustard";
        else
            return "Mrs. White";
    }
}