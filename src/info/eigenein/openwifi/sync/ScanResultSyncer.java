package info.eigenein.openwifi.sync;

public abstract class ScanResultSyncer extends Syncer {
    /**
     * Maximum allowed number of scan results to be processed at once.
     */
    protected static final int PAGE_SIZE = 128;

    protected long syncedEntitiesCount = 0;

    @Override
    public long getSyncedEntitiesCount() {
        return syncedEntitiesCount;
    }
}
