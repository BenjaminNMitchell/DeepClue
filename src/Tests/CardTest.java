package Tests;
import Model.*;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Created by Ben on 2016-11-21.
 */
@RunWith(Arquillian.class)
public class CardTest {
    Card c1, c2, c3;
    Card[] cards;
    String t1 = "suspect";
    String t2 = "room";
    String t3 = "weapon";
    String s1 = "Mr. Green";
    String s2 = "Hall";
    String s3 = "Knife";
    String[] names;
    @Before
    public void setUp() throws Exception {
        names = new String[3];
        names[0] = s1;
        names[1] = s2;
        names[2] = s3;
        c1 = new Card(s1);
        c2 = new Card(s2);
        c3 = new Card(s3);
        cards = new Card[3];
        cards[0] = c1;
        cards[1] = c2;
        cards[2] = c3;
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getPlayerID() throws Exception {
        for (Card c : cards) {
            assertTrue(c.getPlayerID() == -1);
        }
    }

    @Test
    public void Card() throws Exception {
        Card c = new Card(s1);
        assertTrue(c.getPlayerID() == -1);
        assertTrue(c.getName().equals(s1));
        assertTrue(c.getType() == t1);
    }

    @Test (expected=CardException.class)
    public void CardBadName() throws Exception {
        Card c = new Card("Invalid Name");
    }


    @Test
    public void setPlayerID() throws Exception {
        int newNum = 0;
        c1.setPlayerID(newNum);
        assertTrue(c1.getPlayerID() == newNum);
    }

    @Test
    public void equalsTrue() throws Exception {
        Card c = new Card("Mr. Green");
        c.setPlayerID(1);
        assertTrue(c1.equals(c));
        assertTrue(c.equals(c1));
    }

    @Test
    public void equalsFalse() throws Exception {
        Card c = new Card("Knife");
        assertFalse(c1.equals(c));
    }

    @Test
    public void exactlyEqualsTrue() throws Exception {
        Card c = new Card("Mr. Green");
        assertTrue(c.exactlyEquals(c1));
        assertTrue(c1.exactlyEquals(c));
    }

    @Test
    public void exactlyEqualsFalse() throws Exception {
        Card c = new Card("Mr. Green");
        c.setPlayerID(0);
        assertFalse(c.exactlyEquals(c1));
        assertFalse(c1.exactlyEquals(c));
        assertFalse(c2.exactlyEquals(c1));
        assertFalse(c1.exactlyEquals(c2));
        assertFalse(c.exactlyEquals(c2));
        assertFalse(c2.exactlyEquals(c));
    }

    @Test
    public void cloneTest() throws Exception {
        Card c = c1.clone();
        assertTrue(c.equals(c1));
        assertTrue(c.exactlyEquals(c1));
        assertFalse(c == c1);
        c.setPlayerID(1);
        assertTrue(c1.getPlayerID() != 1);
    }

    @Test
    public void isWeapon() throws Exception {
        assertFalse(c1.isWeapon());
        assertFalse(c2.isWeapon());
        assertTrue(c3.isWeapon());
    }

    @Test
    public void isRoom() throws Exception {
        assertFalse(c1.isRoom());
        assertTrue(c2.isRoom());
        assertFalse(c3.isRoom());
    }

    @Test
    public void isSuspect() throws Exception {
        assertTrue(c1.isSuspect());
        assertFalse(c2.isSuspect());
        assertFalse(c3.isSuspect());
    }

    @Test
    public void toStringTest() throws Exception {
        for (int i = 0; i < 3; i++)
            assertTrue(cards[i].toString().equals(names[i]));
    }

    @Deployment
    public static Archive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(Card.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

}

