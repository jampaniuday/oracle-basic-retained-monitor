package com.uptimesoftware.uptime.erdc;

import java.sql.SQLException;

import com.uptimesoftware.uptime.erdc.baseclass.OracleQueryingMonitor;
import com.uptimesoftware.uptime.erdc.database.RemoteConnection;
import com.uptimesoftware.uptime.erdc.database.SearchableQuery;

public class MonitorOracleRetained extends OracleQueryingMonitor {
    
    protected Boolean hasErrors = false;
    
    public MonitorOracleRetained() {
            super();
            setSimpleMonitor();
    }

    @Override
    protected void doWork(RemoteConnection connection) throws SQLException {
        if (isUseScript()) {
            Boolean matches = queryResultHasMatch(connection, sqlScript, matchTerm);
            if ( ! hasErrors ) {
                if (matches) {
                    setMessage("Expression was matched");
                } else {
                    // do nothing
                    //setResultStatus(ErdcTransientState.CRIT, "No matches in result");
                }
            }
        } else {
            setMessage("Connected to Oracle server on port " + getPort());
        }
    }

    private Boolean queryResultHasMatch(RemoteConnection connection, String script, String needle) {
        String sql = formatForOracle(script);
        SearchableQuery searchableQuery = new SearchableQuery(connection, sql, needle);
        Boolean result = searchableQuery.loadRemotely();

        int errors = 0;
        String row_str = searchableQuery.getRow().trim();
        //long row_long = searchableQuery.getRowAsLong();
        long row_long = 0;
        try {
            row_long = Integer.parseInt(row_str.trim());
        } catch (Exception ex) {
            // do nothing
            //errors++;
            //setState(ErdcTransientState.CRIT);
            //setMessage("Error: Output must be a number. \rQuery output: " + row_str);
        }
        
        addVariable("textoutput", row_str);
        addVariable("numberoutput", row_long);

        if (errors == 0) {
            setMessage("Query completed successfully.");
        }
        else {
            hasErrors = true;
            setMessage("Query completed with errors.");
        }
        return result;
    }

    private String formatForOracle(String sql) {
        String query = sql.replace(';', ' ');
        return query.trim();
    }
}
