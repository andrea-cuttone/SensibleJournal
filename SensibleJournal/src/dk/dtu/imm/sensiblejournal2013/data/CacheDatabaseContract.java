package dk.dtu.imm.sensiblejournal2013.data;

import android.provider.BaseColumns;

public class CacheDatabaseContract {

    public CacheDatabaseContract() {}

    /* Inner class that defines the table contents */
    public static abstract class CacheEntry implements BaseColumns {
        public static final String TABLE_NAME = "daily_entries";
        public static final String COLUMN_ENTRY_ID = "entry_id";
        public static final String COLUMN_POI_ID = "poi_id";
        public static final String COLUMN_DAY_NUMBER = "day_number";
        public static final String COLUMN_DAY = "day";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGTITUDE = "longtitude";
        public static final String COLUMN_ARRIVAL = "arrival";
        public static final String COLUMN_DEPARTURE = "departure";
    }
}
