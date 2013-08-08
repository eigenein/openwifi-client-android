package info.eigenein.openwifi.tests.helpers;

import info.eigenein.openwifi.helpers.*;
import junit.framework.*;

public class InternetTest extends TestCase {

    public static void testCheck() {
        Assert.assertTrue("Internet is not available.", Internet.check());
    }
}
