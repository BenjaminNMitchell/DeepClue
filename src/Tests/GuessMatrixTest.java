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

import static org.junit.Assert.*;
import Model.*;

/**
 * Created by Ben on 2016-11-22.
 */
@RunWith(Arquillian.class)
public class GuessMatrixTest {
    int id1 = 1;
    int id2 = 2;
    int id3 = 3;
    Card c1, c2, c3;
    Guess guess;
    GuessMatrix gm;
    boolean[] results;
    Card[] solveds, strikes;
    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(Model.GuessMatrix.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Before
    public void setUp() throws Exception {
        c1 = new Card("Miss Scarlett");
        c2 = new Card("Ballroom");
        c3 = new Card("Candlestick");
        guess = new Guess(c1, c2, c3);
        gm = new GuessMatrix(guess, 3);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void singleNoChangeTest1() throws Exception {
        int[] values = {0, 0, 0};
        results = gm.addID(id1, values);
        resultsFalse(results);
        isOnlyInAccepted(id1);
        getArrays();
        bothArraysEmpty();
    }

    @Test
    public void singleNoChangeTest2() throws Exception {
        int[] values = {-1, 0, 0};
        results = gm.addID(id1, values);
        resultsFalse(results);
        isOnlyInAccepted(id1);

    }

    @Test
    public void singleChangeTest() throws Exception {
        int[] values = {-1, -1, 0};
        results = gm.addID(id1, values);
        isOnlyInAccepted(id1);
        oneCardSolved(results);
        solveds = gm.getSolvedArray();
        assertTrue(solveds.length == 1);
        assertTrue(solveds[0].getPlayerID() == id1);
        assertTrue(solveds[0].equals(c3));
    }

    @Test (expected=ClueException.class)
    public void singleInvalidTest() throws Exception {
        int[] values = {-1, -1, -1};
        gm.addID(id1, values);
    }


    @Test (expected=ClueException.class)
    public void doubleInvalidInit() throws Exception {
        int[] values = {1, 0, 0};
        gm.addID(id1, values);
        gm.addID(id2, values);
    }
    @Test
    public void doubleNoChangeTest1() throws Exception {
        int[] values1 = {0, 0, 0};
        int[] values2 = {0, 0, 0};
        doubleNoChange(id1, id2, values1, values2);
    }

    @Test
    public void doubleNoChangeTest2() throws Exception {
        int[] values1 = {-1, 0, 0};
        int[] values2 = {0, -1, 0};
        doubleNoChange(id1, id2, values1, values2);
    }

    @Test
    public void doubleNoChangeTest3() throws Exception {
        int[] values = {-1 ,0, 0};
        doubleNoChange(id1, id2, values, values);
    }

    @Test
    public void doubleSolvedCheck1() throws Exception {
        int[] values1 = {-1, 0, 0};
        int [] values2 = {0, 1, 0};
        boolean[] results1 = gm.addID(id1, values1);
        boolean[] results2 = gm.addID(id2, values2);
        strikes = gm.getStrikeArray();
        solveds = gm.getSolvedArray();
        resultsFalse(results1);
        assertTrue(results2[0]);
        assertTrue(results2[1]);
        arrayLengthCheck(strikes, 2);
        arrayLengthCheck(solveds, 1);
        Card x1, x2, x3;
        x1 = strikes[0];
        x2 = strikes[1];
        x3 = solveds[0];
        assertTrue(x1.getPlayerID() == id1);
        assertTrue(x2.getPlayerID() == id2);
        assertTrue(x3.getPlayerID() == id1);
        assertTrue(x1.equals(c2));
        assertTrue(x2.equals(c3));
        assertTrue(x3.equals(c3));
    }

    @Test
    public void doubleSolvedCheck2() throws Exception{
        int[] values1 = {1, 1, 0};
        int[] values2 = {-1, -1, 0};
        boolean[] results1 = gm.addID(id1, values1);
        boolean[] results2 = gm.addID(id2, values2);
        strikes = gm.getStrikeArray();
        solveds = gm.getSolvedArray();
        resultsFalse(results1);
        assertTrue(results2[0]);
        assertTrue(results2[1]);
        arrayLengthCheck(strikes, 1);
        arrayLengthCheck(solveds, 1);
        Card x1, x2;
        x1 = strikes[0];
        x2 = solveds[0];
        assertTrue(x1.equals(c3) && x1.getPlayerID() == id1);
        assertTrue(x2.equals(c3) && x2.getPlayerID() == id2);
    }

    @Test (expected=ClueException.class)
    public void invalidValuexsTest() throws Exception {
        int[] badValues = {2, 2, 2};
        gm.addID(id1, badValues);
    }

    @Test
    public void tripleNoChange() throws Exception {
        int[] values1 = {-1, 0 ,0};
        int[] values2 = {0, -1, 0};
        int[] values3 = {0, 0, -1};
        boolean[] results1, results2, results3;
        results1 = gm.addID(id1, values1);
        results2 = gm.addID(id2, values2);
        results3 = gm.addID(id3, values3);
        resultsFalse(results1);
        resultsFalse(results2);
        resultsFalse(results3);
        getArrays();
        bothArraysEmpty();
    }

    @Test
    public void tripleChange1() throws Exception {
        int[] values1 = {-1, 0 ,0};
        int[] values2 = {-1, 0, 0};
        int[] values3 = {0, 0, 0};
        boolean[] results1, results2, results3;
        results1 = gm.addID(id1, values1);
        results2 = gm.addID(id2, values2);
        results3 = gm.addID(id3, values3);
        resultsFalse(results1);
        resultsFalse(results2);
        assertTrue(results3[0]);
        assertTrue(results3[1]);
        assertFalse(results3[2]);
        getArrays();
        checkLengths(1, 2);
        assertTrue(strikes[0].equals(c2) && strikes[0].getPlayerID() == id3);
        assertTrue(strikes[1].equals(c3) && strikes[1].getPlayerID() == id3);
        assertTrue(solveds[0].equals(c1) && solveds[0].getPlayerID() == id3);
    }

    @Test (expected=ClueException.class)
    public void invalidInitTriple() throws Exception {
        int[] values1 = {-1, 0 ,0};
        int[] values2 = {-1, 0, 0};
        int[] values3 = {-1, 0, 0};
        gm.addID(id1, values1);
        gm.addID(id2, values2);
        gm.addID(id3, values3);
    }

    @Test
    public void bigInferenceTest() throws Exception {
        int[] values1 = {-1, 0 ,0};
        int[] values2 = {0, -1, 0};
        int[] values3 = {-1, 0, -1};
        boolean[] results1, results2, results3;
        results1 = gm.addID(id1, values1);
        results2 = gm.addID(id2, values2);
        results3 = gm.addID(id3, values3);
        resultsFalse(results1);
        resultsFalse(results2);
        assertTrue(results3[0]);
        assertTrue(results3[1]);
        assertFalse(results3[2]);
        getArrays();
        checkLengths(3, 2);
    }

    @Test
    public void strikeCheckTriple() throws Exception {
        int[] values1 = {-1, 0 ,0};
        int[] values2 = {0, -1, 0};
        int[] values3 = {0, 0, -1};
        gm.addID(id1, values1);
        gm.addID(id2, values2);
        gm.addID(id3, values3);
        Card card = new Card("Miss Scarlett");
        card.setPlayerID(3);
        results = gm.updateCardStrike(card);
        getArrays();
        checkLengths(3, 2);
        displayData(strikes, solveds);
    }


    private void doubleNoChange(int id1, int id2, int[] values1, int[] values2) throws Exception {
        gm.addID(id1, values1);
        results = gm.addID(id2, values2);
        resultsFalse(results);
        isOnlyInAccepted(id1);
        isOnlyInAccepted(id2);
        getArrays();
        bothArraysEmpty();
    }

    private void oneCardSolved(boolean[] results) throws Exception {
        assertTrue(results[0]);
        assertFalse(results[1]);
        assertFalse(results[2]);
    }

    private void isOnlyInAccepted(int idNum) {
        assertTrue(gm.inAccepted(idNum));
        assertFalse(gm.inRejected(idNum));

    }

    private void resultsFalse(boolean[] results) {
        for (boolean b : results) {
            assertFalse(b);
        }
    }

    private void getArrays() {
        strikes = gm.getStrikeArray();
        solveds = gm.getSolvedArray();
    }

    private void bothArraysEmpty() {
        checkLengths(0, 0);
    }
    private void checkLengths(int solvedLen, int struckLen) {
        arrayLengthCheck(solveds, solvedLen);
        arrayLengthCheck(strikes, struckLen);
    }

    private void arrayLengthCheck(Card[] array, int length) {
        assertTrue(array.length == length);
    }

    private void displayData(Card[] strikes, Card[] solved) {
        System.out.println(gm.toString());
        System.out.println("strikes");
        for (Card c : strikes) {
            System.out.printf("%s %d\n", c.toString(), c.getPlayerID());
        }
        System.out.println("Solved");
        for (Card c : solved) {
            System.out.printf("%s %d\n", c.toString(), c.getPlayerID());
        }
    }
}
