package com.uptimesoftware.uptime.erdc.database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SearchableQuery extends RemoteLoader<Boolean> {

        private final String remoteSql;
        private final String needle;
        ///////////////////////////////////////////
        // MOD START
        private String rowstr;
        
        // allow us to return the row string
        public String getRow() {
            return rowstr;
        }
        public long getRowAsLong() {
            long l = 0;
            try{
                l = Long.parseLong(rowstr);
            } catch (Exception e){
                // do nothing; skip
            }
            return l;
        }
        // MOD END
        ///////////////////////////////////////////

        public SearchableQuery(RemoteConnection remoteConnection, String remoteSql, String needle) {
                super(remoteConnection);
                this.remoteSql = remoteSql;
                this.needle = needle;
        }

        @Override
        protected String getRemoteSql() {
                return remoteSql;
        }

        @Override
        protected Boolean extractResultFrom(ResultSet rs) throws SQLException {
                int columnCount = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                        rowstr = getRowAsString(rs, columnCount);
                        if (matches(needle, rowstr)) {
                                return true;
                        }
                }
                return false;

        }

        private boolean matches(final String needle, String row) {
                return row.matches(".*" + needle + ".*");
        }

        private String getRowAsString(ResultSet rs, int columnCount) throws SQLException {
                StringBuilder rowString = new StringBuilder();
                for (int count = 1; count <= columnCount; count++) {
                        rowString.append(rs.getString(count));
                        rowString.append(" ");
                }
                return rowString.toString();
        }

}
