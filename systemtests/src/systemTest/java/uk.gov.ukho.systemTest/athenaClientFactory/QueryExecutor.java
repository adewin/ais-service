package uk.gov.ukho.systemTest.athenaClientFactory;

import com.amazonaws.services.athena.AmazonAthena;
import com.amazonaws.services.athena.model.*;

import java.util.List;

public class QueryExecutor {

    private static final String DEFAULT_DATABASE = "";
    private static final String ATHENA_OUTPUT_BUCKET = "";
    private static final String ATHENA_QUERY = "";
    private static final long TIMEOUT = 1000;

    public class StartQueryExample {

        public void startQuery() throws InterruptedException {

            AthenaClientFactory factory = new AthenaClientFactory();
            AmazonAthena athenaClient = factory.createClient();

            String queryExecutionId = submitAthenaQuery(athenaClient);

            waitForQueryToComplete(athenaClient, queryExecutionId);

            processResultRows(athenaClient, queryExecutionId);
        }

        private String submitAthenaQuery(AmazonAthena athenaClient) {
            // The QueryExecutionContext allows us to set the Database.
            QueryExecutionContext queryExecutionContext = new QueryExecutionContext().withDatabase(DEFAULT_DATABASE);

            ResultConfiguration resultConfiguration = new ResultConfiguration()
                    .withOutputLocation(ATHENA_OUTPUT_BUCKET);

            StartQueryExecutionRequest startQueryExecutionRequest = new StartQueryExecutionRequest()
                    .withQueryString(ATHENA_QUERY)
                    .withQueryExecutionContext(queryExecutionContext)
                    .withResultConfiguration(resultConfiguration);

            StartQueryExecutionResult startQueryExecutionResult = athenaClient.startQueryExecution(startQueryExecutionRequest);
            return startQueryExecutionResult.getQueryExecutionId();
        }

        private void waitForQueryToComplete(AmazonAthena athenaClient, String queryExecutionId) throws InterruptedException {
            GetQueryExecutionRequest getQueryExecutionRequest = new GetQueryExecutionRequest()
                    .withQueryExecutionId(queryExecutionId);

            GetQueryExecutionResult getQueryExecutionResult = null;
            boolean isQueryStillRunning = true;
            while (isQueryStillRunning) {
                getQueryExecutionResult = athenaClient.getQueryExecution(getQueryExecutionRequest);
                String queryState = getQueryExecutionResult.getQueryExecution().getStatus().getState();
                if (queryState.equals(QueryExecutionState.FAILED.toString())) {
                    throw new RuntimeException("Query Failed to run with Error Message: " + getQueryExecutionResult.getQueryExecution().getStatus().getStateChangeReason());
                } else if (queryState.equals(QueryExecutionState.CANCELLED.toString())) {
                    throw new RuntimeException("Query was cancelled.");
                } else if (queryState.equals(QueryExecutionState.SUCCEEDED.toString())) {
                    isQueryStillRunning = false;
                } else {
                    Thread.sleep(TIMEOUT);
                }
                System.out.println("Current Status is: " + queryState);
            }
        }

        private void processResultRows(AmazonAthena athenaClient, String queryExecutionId) {
            GetQueryResultsRequest getQueryResultsRequest = new GetQueryResultsRequest()
                    // Max Results can be set but if its not set,
                    // it will choose the maximum page size
                    // As of the writing of this code, the maximum value is 1000
                    // .withMaxResults(1000)
                    .withQueryExecutionId(queryExecutionId);

            GetQueryResultsResult getQueryResultsResult = athenaClient.getQueryResults(getQueryResultsRequest);
            List<ColumnInfo> columnInfoList = getQueryResultsResult.getResultSet().getResultSetMetadata().getColumnInfo();

            while (true) {
                List<Row> results = getQueryResultsResult.getResultSet().getRows();
                for (Row row : results) {
                    // Process the row. The first row of the first page holds the column names.
                    processRow(row, columnInfoList);
                }
                // If nextToken is null, there are no more pages to read. Break out of the loop.
                if (getQueryResultsResult.getNextToken() == null) {
                    break;
                }
                getQueryResultsResult = athenaClient.getQueryResults(
                        getQueryResultsRequest.withNextToken(getQueryResultsResult.getNextToken()));
            }
        }

        private void processRow(Row row, List<ColumnInfo> columnInfoList) {
            for (int i = 0; i < columnInfoList.size(); ++i) {
                switch (columnInfoList.get(i).getType()) {
                    case "varchar":
                        // Convert and Process as String
                        break;
                    case "tinyint":
                        // Convert and Process as tinyint
                        break;
                    case "smallint":
                        // Convert and Process as smallint
                        break;
                    case "integer":
                        // Convert and Process as integer
                        break;
                    case "bigint":
                        // Convert and Process as bigint
                        break;
                    case "double":
                        // Convert and Process as double
                        break;
                    case "boolean":
                        // Convert and Process as boolean
                        break;
                    case "date":
                        // Convert and Process as date
                        break;
                    case "timestamp":
                        // Convert and Process as timestamp
                        break;
                    default:
                        throw new RuntimeException("Unexpected Type is not expected" + columnInfoList.get(i).getType());
                }
            }
        }
    }
}