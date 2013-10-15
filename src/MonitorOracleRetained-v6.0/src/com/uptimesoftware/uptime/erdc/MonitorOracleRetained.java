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
        String row_str = searchableQuery.getRow();
        addVariable("textoutput", row_str);
        //long row_long = searchableQuery.getRowAsLong();
        try {
            long row_long = Integer.parseInt(row_str.trim());
            addVariable("numberoutput", row_long);
        } catch (Exception ex) {
            errors++;
            setState(ErdcTransientState.CRIT);
            setMessage("Error: Output must be a number. \rQuery output: " + row_str);
        }

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


/*
public class MonitorOracleRetainedold extends OracleQueryingMonitor {

    public Logger logger = LogManager.getLogger(MonitorOracleRetained.class);

    @Override
    protected void doWork(Connection conn) {

        String results = "";

        try {
            Statement stmt = conn.createStatement();

            // Added for Bug 2078 - GMH
            scriptString = scriptString.replace(';', ' ').trim();

            ResultSet rs = stmt.executeQuery(scriptString);
            ResultSetMetaData rsmd = rs.getMetaData();

            int columnCount = rsmd.getColumnCount();
            int records = 0;

            while (rs.next()) {

                for (int i = 1; i <= columnCount; i++) {
                    results += rs.getString(i) + " ";
                }
                records++;
            }

            if (records == 0) {
                setState(ErdcTransientState.WARN);
                setMessage("No results returned");
            } else {
                setState(ErdcTransientState.TBD);
                setMessage("Query completed");
                addVariable("textoutput", results);
            }

            try {
                Integer.parseInt(results.trim());
                addVariable("numberoutput", results);
            } catch (Exception ex) {
                setState(ErdcTransientState.CRIT);
                setMessage("Error: Output must be a number. \rQuery output: " + results.trim());
            }

        } catch (Exception e) {
            setState(ErdcTransientState.CRIT);
            setMessage("An exception was thrown executing the query. \r" + e.getMessage());
        }
    }
}
*/