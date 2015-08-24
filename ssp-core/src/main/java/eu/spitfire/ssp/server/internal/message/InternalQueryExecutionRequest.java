package eu.spitfire.ssp.server.internal.message;

import com.google.common.util.concurrent.SettableFuture;
import eu.spitfire.ssp.server.internal.QueryExecutionResults;
import org.apache.jena.query.Query;


/**
 * Wrapper class to combine a {@link org.apache.jena.query.Query} with a
 * {@link com.google.common.util.concurrent.SettableFuture} which contains the result of the
 * query after its execution.
 *
 * @author Oliver Kleine
 */
public class InternalQueryExecutionRequest {

    private Query query;
    private SettableFuture<QueryExecutionResults> resultsFuture;

    /**
     * Creates a new instance of {@link InternalQueryExecutionRequest}
     * @param query the {@link org.apache.jena.query.Query} to be executed
     */
    public InternalQueryExecutionRequest(Query query) {
        this.query = query;
        this.resultsFuture = SettableFuture.create();
    }

    /**
     * Returns the {@link org.apache.jena.query.Query} to be executed.
     * @return the {@link org.apache.jena.query.Query} to be executed.
     */
    public Query getQuery() {
        return query;
    }

    /**
     * Returns the {@link com.google.common.util.concurrent.SettableFuture} to be set with the result of the query
     * execution.
     * @return the {@link com.google.common.util.concurrent.SettableFuture} to be set with the result of the query
     * execution.
     */
    public SettableFuture<QueryExecutionResults> getResultsFuture() {
        return resultsFuture;
    }



}