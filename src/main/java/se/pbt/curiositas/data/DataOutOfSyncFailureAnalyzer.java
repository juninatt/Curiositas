package se.pbt.curiositas.data;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * Explains a startup stopped by {@link DataOutOfSyncException} in plain words: what differs and
 * which command fixes it, instead of a stack trace. Registered in {@code META-INF/spring.factories}.
 */
public class DataOutOfSyncFailureAnalyzer extends AbstractFailureAnalyzer<DataOutOfSyncException> {

    /**
     * Describes the differences and how to fix them.
     *
     * @param rootFailure the whole startup failure
     * @param cause       the differences that stopped the startup
     * @return the description and the action shown in the startup failure report
     */
    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, DataOutOfSyncException cause) {
        String description = "The database does not match the files in the data folder, "
                + "which are the source of truth.\n\n" + cause.getDifferences().describe();
        return new FailureAnalysis(description, DataDifferences.HOW_TO_FIX, cause);
    }
}
