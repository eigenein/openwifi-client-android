package info.eigenein.openwifi.helpers;

/**
 * A generic pair.
 */
public abstract class Pair<TValue1, TValue2> {

    protected final TValue1 value1;

    protected final TValue2 value2;

    protected Pair(final TValue1 value1, final TValue2 value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;
        hashCode = hashCode * prime + value1.hashCode();
        hashCode = hashCode * prime + value2.hashCode();
        return hashCode;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        /* if (getClass() != object.getClass()) {
            return false;
        } */
        final Pair<TValue1, TValue2> other = (Pair<TValue1, TValue2>)object;
        return value1.equals(other.value1) && value2.equals(other.value2);
    }
}
