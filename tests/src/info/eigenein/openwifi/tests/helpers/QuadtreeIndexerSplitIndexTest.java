package info.eigenein.openwifi.tests.helpers;

import info.eigenein.openwifi.helpers.*;
import junit.framework.*;

/**
 * Tests {@link QuadtreeIndexer.SplitIndex}.
 */
public class QuadtreeIndexerSplitIndexTest extends TestCase {

    public static void testFromIndex() {
        Assert.assertEquals(
                new QuadtreeIndexer.SplitIndex(
                        Long.parseLong("10", 2),
                        Long.parseLong("10", 2)),
                QuadtreeIndexer.SplitIndex.fromIndex(
                        Long.parseLong("1100", 2)));
    }

    public static void testToIndex() {
        Assert.assertEquals(
                Long.parseLong("1110", 2),
                new QuadtreeIndexer.SplitIndex(
                        Long.parseLong("11", 2),
                        Long.parseLong("10", 2))
                        .toIndex());
    }

    /**
     * Shift tests for order 1.
     */
    public static void testShiftOrder1() {
        // No shift.
        Assert.assertEquals(
                Long.parseLong("00", 2),
                QuadtreeIndexer.SplitIndex.shift(
                        Long.parseLong("00", 2),
                        0, 0,
                        1));
        // Shift with wrapping.
        Assert.assertEquals(
                Long.parseLong("00", 2),
                QuadtreeIndexer.SplitIndex.shift(
                        Long.parseLong("10", 2),
                        -1, 0,
                        1));
    }

    /**
     * Shift tests for order 2.
     */
    public static void testShiftOrder2() {
        // Shift within the same quad.
        Assert.assertEquals(
                Long.parseLong("0011", 2),
                QuadtreeIndexer.SplitIndex.shift(
                        Long.parseLong("0110", 2),
                        0, -1,
                        2));
        Assert.assertEquals(
                Long.parseLong("1110", 2),
                QuadtreeIndexer.SplitIndex.shift(
                        Long.parseLong("1100", 2),
                        1, 0,
                        2));
        // Shift within adjacent quads.
        Assert.assertEquals(
                Long.parseLong("0110", 2),
                QuadtreeIndexer.SplitIndex.shift(
                        Long.parseLong("1100", 2),
                        -1, 0,
                        2));
        // Shift with wrapping.
        Assert.assertEquals(
                Long.parseLong("1111", 2),
                QuadtreeIndexer.SplitIndex.shift(
                        Long.parseLong("0101", 2),
                        -1, 0,
                        2));
    }
}
