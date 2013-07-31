package info.eigenein.openwifi.tests.helpers.location;

import info.eigenein.openwifi.helpers.location.*;
import junit.framework.*;

public class LocationIndexerTest extends TestCase {

    public void testGetIndex11() {
        Assert.assertEquals(0L, LocationIndexer.getIndex(-45000000, -90000000, 1));
    }

    public void testGetIndex12() {
        Assert.assertEquals(2L, LocationIndexer.getIndex(45000000, -90000000, 1));
    }

    public void testGetIndex13() {
        Assert.assertEquals(1L, LocationIndexer.getIndex(-45000000, 90000000, 1));
    }

    public void testGetIndex14() {
        Assert.assertEquals(3L, LocationIndexer.getIndex(45000000, 90000000, 1));
    }

    public void testGetIndex21() {
        Assert.assertEquals(7L, LocationIndexer.getIndex(-22500000, 180000000, 2));
    }
}