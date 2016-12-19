package Tests;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.ArrayList;
import static org.junit.Assert.*;
import Model.*;

import java.io.*;
/**
 * Created by Ben on 2016-12-14.
 */
@RunWith(Arquillian.class)
public class DataTest {

    static final int PLAYER_NUM = 4;
    Data data;
    Card person, place, thing;
    Guess guess, repeatLogGuess;

    @Before
    public void setUp() throws Exception {
        data = new Data(PLAYER_NUM);
        Input io = new CompGenInput();
        // the zeros are dummy variables in compGenInput
        data.initOurCards(io.getOurCards(0, 1));
        data.initPubCards(io.getPublicCards(0));
        person = new Card("Professor Plum");
        place = new Card("Dining Room");
        thing = new Card("Lead Pipe");
        guess = new Guess(person, place, thing);

    }

    @After
    public void tearDown() throws Exception {

    }

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(Model.Data.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void dataInitTest() throws Exception {
        Data testData = new Data(4);
        assertTrue(testData.getTouchedGuesses().length == 0);
        Input io = new CompGenInput();
        testData.initOurCards(io.getOurCards(0, 1));
        testData.initPubCards(io.getPublicCards(1));
        assertTrue(testData.getTouchedGuesses().length == 0);
    }

    @Test
    public void dataNegativeTest() throws Exception {

        data.negative(guess, 1);
    }

    @Test
    public void dataPositiveTest1() throws Exception {

        data.positive(guess, 1);
        Guess[] touched = data.getTouchedGuesses();
        System.out.printf("number of modified GMs: %d\n", touched.length);
        assertTrue(touched.length == 1);
    }


    @Test
    public void failureTest() throws Exception {
        repeatLog();
    }

    private void repeatLog() throws Exception {
        String[] actions = parseGuessLog();
        String[] parts;
        int playerNum;
        for (String str : actions) {
            if (str.substring(0, 1).matches("[0-9]")) {
                parts = str.split(" ");
                playerNum = Integer.parseInt(parts[0]);
                if (parts[1].equals("false"))
                    data.negative(repeatLogGuess, playerNum);
                else
                    data.positive(repeatLogGuess, playerNum);
            }
            else {

                parts = str.split("--");

                person = new Card(parts[0]);
                thing = new Card(parts[1]);
                place = new Card(parts[2]);
                repeatLogGuess = new Guess(person, place, thing);
            }

        }
    }

    public String[] parseGuessLog() {
        ArrayList<String> lines = new ArrayList<>();
        String line;
        try {
            FileReader inFile = new FileReader("/Users/Ben/Desktop/guessLog.txt");
            BufferedReader buffRead = new BufferedReader(inFile);
            while ((line = buffRead.readLine()) != null)
                lines.add(line);
        } catch (IOException e) {
            System.err.print(e.getMessage());
        }
        ArrayList<String> actions = new ArrayList<>();
        for (String str : lines) {
            line = parseLine(str);
            if (line != null)
                actions.add(line);
        }
        String[] returnStrings = new String[actions.size()];
        actions.toArray(returnStrings);
        return returnStrings;
    }

    public String parseLine(String line) {
        String[] parts = line.split(" ");
        if (parts[0].equals("Player")) {
            return parts[2] + " " +  parts[5];
        }
        if (parts[0].equals("Guess:") )
            line = line.split("Guess: ")[1];
            String[] a = line.split(" with the ");
            String[] b = a[1].split(" in the ");
            return a[0] + "--" + b[0] + "--" + b[1];
        }
    }

