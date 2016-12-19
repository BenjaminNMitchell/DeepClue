package Tests;
import Model.*;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * The testing class for CompGenInputTest
 */
@RunWith(Arquillian.class)
public class CompGenInputTest {
    // Attributes
    CompGenInput io;
    Guess guess1;
    Guess guess2;
    Card person, place, thing;

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(Model.CompGenInput.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Before
    public void setUp() throws Exception {
        io = new CompGenInput();
        person = new Card("Mr. Green");
        place = new Card("Dining Room");
        thing = new Card("Lead Pipe");
        guess1 = new Guess(person, place, thing);
        guess2 = new Guess(new Card("Miss Scarlett"), new Card("Ballroom"), new Card("Candlestick"));
    }

    @Test
    public void toStringTest() throws Exception {
        io = new CompGenInput();
        System.out.print(io);
    }

    @Test
    public void genComGenInputTest() throws Exception {
        int playerNum;
        int ourNum;
        for (int i = 0; i < 500; i++) {
            io = new CompGenInput();
            playerNum = io.getPlayerNum();
            ourNum = io.getOurNum(playerNum);
            assertTrue(playerNum >= 2 && playerNum <= 6);
            assertTrue(ourNum >= 1 && ourNum <= playerNum);
        }
    }
    @Test
    public void getResponsePositiveTest() throws Exception {
        for (int i = 1; i < 4; i++) {
            assertTrue(io.getResponse(i, guess1));
        }
    }

    @Test
    public void getResponseNegativeTest1() throws Exception {
        assertFalse(io.getResponse(4, guess1));
    }

    @Test
    public void getResponseNegativeTest2() throws Exception {
        for (int i  = 1; i < 5; i++)
            assertFalse(io.getResponse(i, guess2));
    }

    @Test
    public void seeCardPositveTest1() throws Exception {
        Card[] cards = {person, thing, place};
        for (int i = 1; i < 4; i++) {
            Card c = io.seeCard(guess1, i);
            System.out.printf("see Card returned %s expected %s\n", c.toString(), cards[i - 1].toString());
            assertTrue(c.equals(cards[i - 1]));
        }
    }

    @Test(expected= ClueException.class)
    public void seeCardNegativeTest2() throws Exception {
        Card c = io.seeCard(guess1, 4);
    }
}
