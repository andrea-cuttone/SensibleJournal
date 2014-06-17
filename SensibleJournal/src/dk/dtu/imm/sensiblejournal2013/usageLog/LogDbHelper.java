package dk.dtu.imm.sensiblejournal2013.usageLog;

import dk.dtu.imm.sensiblejournal2013.usageLog.LogDatabaseContract.LogEntry;
import dk.dtu.imm.sensiblejournal2013.utilities.Constants;
import dk.dtu.imm.sensiblejournal2013.utilities.Constants.logComponents;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class LogDbHelper extends SQLiteOpenHelper {
		
	public LogDbHelper(Context context) {
		super(context, Constants.LOG_DB_FILENAME, null, Constants.LOG_DB_VERSION);
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		sharedPrefs.getString("PREF_UID", null);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(Constants.SQL_CREATE_LOG_ENTRIES);		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(Constants.SQL_DELETE_LOG_ENTRIES);
        onCreate(db);
	}

	// Method to log the usage data into the database
	public void log(logComponents logComp, long value) {
		new AddToDatabase().execute(this, logComp, value);
	}
		
	// Using AsyncTask to add application log data to the log database in order not
	// to delay the UI thread in case the database becomes large
	public class AddToDatabase extends AsyncTask<Object, Void, Void> {
	    @Override
	    protected Void doInBackground(Object... params) {
	    	SQLiteOpenHelper helper = (SQLiteOpenHelper) params[0];
	    	logComponents logComp = (logComponents) params[1];
	        long value =  ((Long) params[2]) / 1000;
	        
	        ContentValues values = new ContentValues();
    		values.put(LogEntry.COLUMN_TIMESTAMP, value);
    		
	        SQLiteDatabase db = helper.getWritableDatabase();

	        //db.execSQL(Constants.SQL_DELETE_LOG_ENTRIES); db.execSQL(Constants.SQL_CREATE_LOG_ENTRIES);
			switch (logComp) {
		    	case MAIN:		    		
		    		values.put(LogEntry.COLUMN_EVENT, "MAIN");		    		
		    		db.insert(LogEntry.TABLE_NAME, null, values);
		    		db.close();
		    		return null;	    	
		    	case WEEK_ARCHIVE:
		    		values.put(LogEntry.COLUMN_EVENT, "WEEK_ARCHIVE");		    		
		    		db.insert(LogEntry.TABLE_NAME, null, values);
		    		db.close();
		    		return null;
		    	case DAY_ARCHIVE:
		    		values.put(LogEntry.COLUMN_EVENT, "DAY_ARCHIVE");		    		
		    		db.insert(LogEntry.TABLE_NAME, null, values);
		    		db.close();
		    		return null;
		    	case CURRENT_LOC:
		    		values.put(LogEntry.COLUMN_EVENT, "CURRENT_LOC");		    		
		    		db.insert(LogEntry.TABLE_NAME, null, values);
		    		db.close();
		    		return null;
		    	case LAST_PLACE:
		    		values.put(LogEntry.COLUMN_EVENT, "LAST_PLACE");		    		
		    		db.insert(LogEntry.TABLE_NAME, null, values);
		    		db.close();
		    		return null;
		    	case LATEST_JOURNEY:
		    		values.put(LogEntry.COLUMN_EVENT, "LATEST_JOURNEY");		    		
		    		db.insert(LogEntry.TABLE_NAME, null, values);
		    		db.close();
		    		return null;
		    	case DAILY_ITIN:
		    		values.put(LogEntry.COLUMN_EVENT, "DAILY_ITIN");		    		
		    		db.insert(LogEntry.TABLE_NAME, null, values);
		    		db.close();
		    		return null;
		    	case WEEKLY_ITIN:
		    		values.put(LogEntry.COLUMN_EVENT, "WEEKLY_ITIN");		    		
		    		db.insert(LogEntry.TABLE_NAME, null, values);
		    		db.close();
		    		return null;
		    	case MOST_VISITED:
		    		values.put(LogEntry.COLUMN_EVENT, "MOST_VISITED");		    		
		    		db.insert(LogEntry.TABLE_NAME, null, values);
		    		db.close();
		    		return null;
		    	case AWESOME_CURRENT_LOC:
		    		values.put(LogEntry.COLUMN_EVENT, "LIKED_CURRENT_LOC");		    		
		    		db.insert(LogEntry.TABLE_NAME, null, values);
		    		db.close();
		    		return null;
		    	case AWESOME_LAST_PLACE:
		    		values.put(LogEntry.COLUMN_EVENT, "LIKED_LAST_PLACE");		    		
		    		db.insert(LogEntry.TABLE_NAME, null, values);
		    		db.close();
		    		return null;
		    	case AWESOME_LATEST_JOURNEY:
		    		values.put(LogEntry.COLUMN_EVENT, "LIKED_LATEST_JOURNEY");		    		
		    		db.insert(LogEntry.TABLE_NAME, null, values);
		    		db.close();
		    		return null;
		    	case AWESOME_DAILY_ITIN:
		    		values.put(LogEntry.COLUMN_EVENT, "LIKED_DAILY_ITIN");		    		
		    		db.insert(LogEntry.TABLE_NAME, null, values);
		    		db.close();
		    		return null;
		    	case AWESOME_WEEKLY_ITIN:
		    		values.put(LogEntry.COLUMN_EVENT, "LIKED_WEEKLY_ITIN");		    		
		    		db.insert(LogEntry.TABLE_NAME, null, values);
		    		db.close();
		    		return null;
		    	case AWESOME_MOST_VISITED:
		    		values.put(LogEntry.COLUMN_EVENT, "LIKED_MOST_VISITED");		    		
		    		db.insert(LogEntry.TABLE_NAME, null, values);
		    		db.close();
		    		return null;
		    	case PAUSE:
		    		values.put(LogEntry.COLUMN_EVENT, "PAUSE");		    		
		    		db.insert(LogEntry.TABLE_NAME, null, values);
		    		db.close();
		    		return null;
		        default:
		        	db.close();
		        	break;
			}
			return null;
	    }
	 }
}
