package dk.dtu.imm.sensiblejournal2013.usageLog;

import android.provider.BaseColumns;

public class LogDatabaseContract {
	
	public LogDatabaseContract() {}

    /* Inner class that defines the table contents */
    public static abstract class LogEntry implements BaseColumns {
        public static final String TABLE_NAME = "application_log";
        public static final String COLUMN_ENTRY_ID = "entry_id";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_EVENT = "event";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        
    }
}
