package Tests;
import org.junit.*;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.runner.RunWith;
import Model.*;

import static org.junit.Assert.*;

/**
 * Created by Ben on 2016-11-22.
 */
@RunWith(Arquillian.class)
public class GuessTest {
    Guess g;
    Card c1, c2, c3;
    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(Model.Guess.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Before
    public void setUp() throws Exception {
        c1 = new Card("Mr. Green");
        c2 = new Card("Hall");
        c3 = new Card("Knife");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void validOrderTest() throws Exception {
        g = new Guess(c1, c2, c3);
    }

    @Test (expected=GuessException.class)
    public void invalidOrderTest() throws Exception {
        g = new Guess(c2, c1, c3);
    }

    @Test
    public void deepCopyCheck() throws Exception {
        g = new Guess(c1, c2, c3);
        Card[] cards = g.getCards();
        assertTrue(c1.exactlyEquals(cards[0]) && c1 != cards[0]);
        assertTrue(c2.exactlyEquals(cards[1]) && c2 != cards[1]);
        assertTrue(c3.exactlyEquals(cards[2]) && c3 != cards[2]);
        c1.setPlayerID(0);
        assertTrue(cards[0].getPlayerID() != 0);
    }

    @Test
    public void instatiatioDeepCopytest() throws Exception {
        g = new Guess(c1, c2, c3);
        c1.setPlayerID(0);
        assertTrue(g.getPerson().getPlayerID() != 0);
    }

    @Test
    public void equalsTest() throws Exception {
        Card c = new Card("Mrs. White");
        g = new Guess(c1, c2, c3);
        Guess g2 = new Guess(c, c2, c3);
        assertFalse(g.equals(g2));
        assertTrue(g.equals(g.clone()));
    }

    @Test public void cloneTest() throws Exception {
        g = new Guess(c1, c2, c3);
        Guess g2 = g.clone();
        assertTrue(g.equals(g2));
        assertFalse(g == g2);
    }

}