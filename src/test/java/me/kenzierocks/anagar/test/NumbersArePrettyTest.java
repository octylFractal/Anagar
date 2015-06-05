package me.kenzierocks.anagar.test;

import static org.junit.Assert.*;
import me.kenzierocks.anagar.Utility.Numbers;

import org.junit.Test;

public class NumbersArePrettyTest {

    @Test
    public void noChange() {
        assertEquals("1", Numbers.prettify(1));
        assertEquals("10", Numbers.prettify(10));
        assertEquals("100", Numbers.prettify(100));
        assertEquals("4", Numbers.prettify(4));
        assertEquals("45", Numbers.prettify(45));
        assertEquals("867", Numbers.prettify(867));
    }

    @Test
    public void oneComma() {
        assertEquals("1,000", Numbers.prettify(1000));
        assertEquals("10,000", Numbers.prettify(10000));
        assertEquals("100,000", Numbers.prettify(100000));
        assertEquals("2,654", Numbers.prettify(2654));
        assertEquals("45,678", Numbers.prettify(45678));
        assertEquals("354,554", Numbers.prettify(354554));
    }

    @Test
    public void twoComma() {
        assertEquals("1,000,000", Numbers.prettify(1000000));
        assertEquals("10,000,000", Numbers.prettify(10000000));
        assertEquals("100,000,000", Numbers.prettify(100000000));
        assertEquals("4,786,235", Numbers.prettify(4786235));
        assertEquals("23,423,443", Numbers.prettify(23423443));
        assertEquals("832,472,483", Numbers.prettify(832472483));
    }

}
