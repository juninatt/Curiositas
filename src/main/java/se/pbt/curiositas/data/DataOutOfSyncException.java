package se.pbt.curiositas.data;

/**
 * Thrown at startup when the database does not match the data files, which are the source of
 * truth, so the application never serves data that disagrees with them.
 */
public class DataOutOfSyncException extends RuntimeException {

    private final DataDifferences differences;

    /**
     * Creates the exception.
     *
     * @param differences what differs, at least one difference
     */
    public DataOutOfSyncException(DataDifferences differences) {
        super("The database does not match the data files:\n" + differences.describe());
        this.differences = differences;
    }

    /** Returns what differs. */
    public DataDifferences getDifferences() {
        return differences;
    }
}
