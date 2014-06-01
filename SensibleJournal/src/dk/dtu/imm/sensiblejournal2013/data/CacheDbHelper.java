package dk.dtu.imm.sensiblejournal2013.data;

import dk.dtu.imm.sensiblejournal2013.utilities.Constants;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CacheDbHelper extends SQLiteOpenHelper {

	public CacheDbHelper(Context context) {
		super(context, Constants.DATA_DB_FILENAME, null, Constants.DATA_DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(Constants.SQL_CREATE_DATA_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		// This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(Constants.SQL_DELETE_DATA_ENTRIES);
        onCreate(db);
	}	
}
