package info.eigenein.openwifi.tests.helpers.internal;

import info.eigenein.openwifi.helpers.internal.*;
import junit.framework.*;

public class RandomHelperTest extends TestCase {

    public static void testRandomNumeric() {
        Assert.assertEquals(16, RandomHelper.randomNumeric(16).length());
    }
}
