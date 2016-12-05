package Model;

/**
 * Created by benji on 9/30/2016.
 */
public class CardProperties {
    public static String[] weapons = {"Candlestick", "Lead Pipe", "Revolver", "Rope", "Knife", "Wrench"};
    public static String[] suspects = {"Miss Scarlett", "Professor Plum", "Mrs. Peacock", "Mr. Green", "Colonel Mustard", "Mrs. White"};
    public static String[] rooms = {"Ballroom", "Billiards Room", "Conservatory", "Dining Room", "Hall", "Library", "Lounge", "Kitchen", "Study"};

    public static boolean isValid(Card card) {
        String target = card.getName();
        boolean foundFlag = isWeapon(target) || isSuspect(target) || isRoom(target);
        return foundFlag;
    }

    public static boolean isValid(String target) {
        boolean foundFlag = isWeapon(target) || isSuspect(target) || isRoom(target);
        return foundFlag;
    }

    public static boolean isWeapon(Card card) {
        String target = card.getName();
        return isWeapon(target);
    }

    public static boolean isWeapon(String target) {
        return isIn(target, weapons);
    }

    public static boolean isSuspect(Card card) {
        String target = card.getName();
        return isSuspect(target);
    }

    public static boolean isSuspect(String target) {
        return isIn(target, suspects);
    }

    public static boolean isRoom(Card card) {
        String target = card.getName();
        return isRoom(target);
    }

    public static boolean isRoom(String target) {
        return isIn(target, rooms);
    }

    public static int getGuessIndex(Guess guess) {
        int counter = 0;
        counter += getSuspectCode(guess.getPerson().getName()) *  9 * 6;
        counter += getRoomCode(guess.getPlace().getName()) * 6;
        counter += getWeaponCode(guess.getThing().getName());
        return counter;
    }

    private static boolean isIn(String target, String[] list) {
        for (String name : list)
            if (target.equals(name))
                return true;
        return false;
    }

    public static String getCardString(int code) {
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
        if (code == 5)
            return "Wrench";
        if (code == 6)
            return "Miss Scarlett";
        if (code == 7)
            return "Professor Plum";
        if (code == 8)
            return "Mrs. Peacock";
        if (code == 9)
            return "Mr. Green";
        if (code == 10)
            return "Colonel Mustard";
        if (code == 11)
            return "Mrs. White";
        if (code == 12)
            return "Ballroom";
        if (code == 13)
            return "Billiards Room";
        if (code == 14)
            return "Conservatory";
        if (code == 15)
            return "Dining Room";
        if (code == 16)
            return "Hall";
        if (code == 17)
            return "Library";
        if (code == 18)
            return "Lounge";
        if (code == 19)
            return "Kitchen";
        return "Study";
    }

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