package info.eigenein.openwifi.sync;

public abstract class ScanResultSyncer extends Syncer {
    /**
     * Maximum allowed number of scan results to be processed at once.
     */
    protected static final int PAGE_SIZE = 128;

    protected int syncedEntitiesCount = 0;

    @Override
    public int getSyncedEntitiesCount() {
        return syncedEntitiesCount;
    }
}
