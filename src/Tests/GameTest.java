package Tests;
import Model.*;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.*;
import java.util.ArrayList;
import java.util.jar.Pack200;

/**
 * Created by Ben on 2016-12-15.
 */
@RunWith(Arquillian.class)
public class GameTest {
    final static int LARGE_NUMBER = 100000000;
    private final static int TRIAL_NUM = 50000;
    private final static String FOLDER_PATH = "/Users/Ben/Desktop/Deep Clue Testing Data/";
    private final static String VERSION_FILE = FOLDER_PATH + "versionNumber.txt";
    private Game game;
    private int[] counts;
    private int[] turnNumLog;
    private int[] typeLog;
    private int minTurnNum;
    private ArrayList<ArrayList<String>> inconsistantGuesses;

    @Before
    public void setUp() throws Exception {
        game = new Game(1);
        minTurnNum = LARGE_NUMBER;
        typeLog = new int[TRIAL_NUM];
        turnNumLog = new int[TRIAL_NUM];
        counts = new int[3];
        inconsistantGuesses = new ArrayList<>();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(Model.Game.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void gameTest1() throws ClueException {
        Guess[] guesses = readGuesses(FOLDER_PATH + "minInconsistent.txt");
        System.out.printf("Guess Number: %d\n", guesses.length);
        Guess answer = game.playGame(guesses);
        System.out.println(answer);
        System.out.printf("Completed in %d turns\n", game.getTurnNumber());
    }

    @Test
    public void getGameMetrics() throws Exception {
        String versionID = getVersionID();
        System.out.println("Starting trial: " + versionID);
        for (int i = 0; i < TRIAL_NUM; i++) {
            runTrial(i);
        }
        ArrayList<String> data = new ArrayList<>();
        data.add(String.format("Success percentage   %%%.2f", 100 * (float) counts[0] / TRIAL_NUM));
        data.add(String.format("Failure percentage   %%%.2f", 100 * (float) counts[1] / TRIAL_NUM));
        data.add(String.format("Exception percentage %%%.2f", 100 * (float) counts[2] / TRIAL_NUM));
        data.add(String.format("Min Turn Number : %d", minTurnNum));
        float turnNumSum = 0;
        for (int i = 0; i < TRIAL_NUM; i++) {
            if (typeLog[i] == 1)
                turnNumSum += turnNumLog[i];
        }
        data.add(String.format("Average Turn Number of Successful game: %.2f", turnNumSum / counts[0]));
        String resultsFile = String.format("%sData/%s_results.txt", FOLDER_PATH, versionID);
        //String errorFile = String.format("%sData/%s_inconsistent.txt", FOLDER_PATH, versionID);
        //writeTo(errorFile, inconsistantGuesses.get(0));
        writeTo(resultsFile, data);
        rewriteVersionID(versionID);
    }

    private void runTrial(int itNum) {
        if (itNum % 1000 == 0)
            System.out.printf("Iteration %d\n", itNum);
        try {
            game = new Game(1);
            game.playGame();
            typeLog[itNum] = 1;
            turnNumLog[itNum] = game.getTurnNumber();
            counts[0]++;
        } catch (ClueException e) {
            System.out.println(e.getMessage());
            if (e instanceof InconsistentDataException) {
                counts[1]++;
                typeLog[itNum] = -1;
                turnNumLog[itNum] = game.getTurnNumber();
                inconsistantGuesses.add(game.getGuesses());
            } else {
                typeLog[itNum] = 0;
                turnNumLog[itNum] = game.getTurnNumber();
                counts[2]++;
            }
        }
        if (turnNumLog[itNum] < minTurnNum && typeLog[itNum] == 1)
            minTurnNum = turnNumLog[itNum];
    }

    @Test
    public void fileTest() throws Exception {
        ArrayList<String> data = new ArrayList<>();
        String versionID = getVersionID();
        String resultsFile = String.format("%sData/%s_results.txt", FOLDER_PATH, versionID);
        writeTo(resultsFile, data);
        rewriteVersionID(versionID);
    }

    @Test
    public void getSmallInconsistent() throws Exception {
        int minTN= 1001;
        int turnNum;
        ArrayList<String> guesses = null;
        Game game = null;
        for (int i = 0; i < TRIAL_NUM; i++) {
            if (i % 1000 == 0)
                System.out.println("Iteration " + i);
            try {
                game = new Game(1);
                game.playGame();
            } catch (InconsistentDataException e) {
                turnNum = game.getTurnNumber();
                if (turnNum < minTN) {
                    minTN = turnNum;
                    guesses = game.getGuesses();
                }
            } catch (ClueException e) {
                // do nothing ie continue
            }
        }
        System.out.println("Min Turn Number: " + minTN);
        writeTo(FOLDER_PATH + "minInconsistent.txt", guesses);
    }

    @Test
    public void get1000GuessOrder() throws Exception {
        Game game = null;
        int counter = 0;
        while (true) {
            System.out.println("Counter: " + counter);
            try {
                game = new Game(1);
                game.playGame();
            } catch (ClueException e) {
                System.out.print(e.getMessage());
                if (e.getMessage().equals("turnPointer over 1000")) {
                    writeTo(FOLDER_PATH + "1000Guesses.txt", game.getGuesses());
                }
            }
            counter++;
        }
    }

    private void rewriteVersionID(String versionID) throws IOException {
        BufferedWriter writer = genWriter(VERSION_FILE);
        System.out.println("Version ID " + versionID);
        String[] parts = versionID.split("-");
        System.out.printf("Part 1: %s Part 2: %s\n", parts[0], parts[1]);
        int num = Integer.parseInt(parts[1]);
        String newVersion = String.format("version-%d", ++num);
        writer.write(newVersion);
        writer.close();
    }
    public static boolean writeTo(String fileName, String line) {
        ArrayList<String> wrapper = new ArrayList<>();
        wrapper.add(line);
        return writeTo(fileName, wrapper);
    }

    public static boolean writeTo(String fileName, ArrayList<String> lines) {
        BufferedWriter writer = genWriter(fileName);
        boolean result;
        try {
            if (writer != null) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
                writer.close();
                result = true;
            } else
                result = false;
        } catch (IOException e) {
            return false;
        }
        return result;
    }

    private static String getVersionID() throws IOException {
        BufferedReader reader = genReader(FOLDER_PATH + "versionNumber.txt");
        String line = reader.readLine();
        System.out.println("Line: " + line);
        return line;
    }



    private static BufferedWriter genWriter(String fileName) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false));
            return writer;
        } catch (IOException e) {
            return null;
        }
    }

    private static BufferedReader genReader(String fileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            return reader;
        } catch (IOException e) {
            return null;
        }
    }



    private Guess[] readGuesses(String filePath) {
        ArrayList<String> lines = new ArrayList<>();
        ArrayList<Guess> guesses = new ArrayList<>();
        String line;
        BufferedReader reader;
        try {
            reader = genReader(filePath);
            while ((line = reader.readLine()) != null)
                lines.add(line);

        } catch (IOException e) {
            e.printStackTrace();
        }
        try{

            for (String str : lines) {
                if (str.matches("Guess:(.*)")) {
                    str = str.substring(7);
                    String[] part1 = str.split(" with the ");
                    String[] part2 = part1[1].split(" in the ");
                    guesses.add(new Guess(new Card(part1[0]), new Card(part2[1]), new Card(part2[0])));
                }
            }
        } catch (ClueException e) {
            System.out.println("Error parsing data");
        }
        Guess[] gArray = new Guess[guesses.size()];
        guesses.toArray(gArray);
        return gArray;
    }
}
