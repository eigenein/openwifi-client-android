package info.eigenein.openwifi.tests.helpers;

import info.eigenein.openwifi.helpers.*;
import junit.framework.*;

public class RandomHelperTest extends TestCase {

    public static void testRandomNumeric() {
        Assert.assertEquals(16, RandomHelper.randomNumeric(16).length());
    }
}
