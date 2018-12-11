package com.andb.apps.todo;

import com.jaredrummler.cyanea.Cyanea;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(0xEEEEEE, Utilities.lighterDarker(0xEAEAEA, 1.2f));
    }
}