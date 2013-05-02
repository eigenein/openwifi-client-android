package info.eigenein.openwifi.tests.helpers.io;

import info.eigenein.openwifi.helpers.io.Internet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestInternet {
    @Test
    public void testCheck() {
        Assert.assertTrue(Internet.check());
    }
}
