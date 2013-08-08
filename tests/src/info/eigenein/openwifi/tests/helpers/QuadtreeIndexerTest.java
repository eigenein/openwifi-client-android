package info.eigenein.openwifi.tests.helpers;

import info.eigenein.openwifi.helpers.*;
import junit.framework.*;

public class QuadtreeIndexerTest extends TestCase {

    public static void testGetIndex11() {
        Assert.assertEquals(0L, QuadtreeIndexer.getIndex(-45000000, -90000000, 1));
    }

    public static void testGetIndex12() {
        Assert.assertEquals(2L, QuadtreeIndexer.getIndex(45000000, -90000000, 1));
    }

    public static void testGetIndex13() {
        Assert.assertEquals(1L, QuadtreeIndexer.getIndex(-45000000, 90000000, 1));
    }

    public static void testGetIndex14() {
        Assert.assertEquals(3L, QuadtreeIndexer.getIndex(45000000, 90000000, 1));
    }

    public static void testGetIndex21() {
        Assert.assertEquals(7L, QuadtreeIndexer.getIndex(-22500000, 180000000, 2));
    }
}
