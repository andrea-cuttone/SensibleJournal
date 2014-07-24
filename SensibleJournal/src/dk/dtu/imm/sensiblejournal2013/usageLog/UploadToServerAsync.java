package dk.dtu.imm.sensiblejournal2013.usageLog;

import java.io.IOException;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import dk.dtu.imm.sensiblejournal2013.data.DataController;
import dk.dtu.imm.sensiblejournal2013.receivers.UsageUploadReceiver;
import dk.dtu.imm.sensiblejournal2013.utilities.Constants;

public class UploadToServerAsync extends AsyncTask<Context, Void, Void> {
	private PendingIntent repeatUploadPI;
	private AlarmManager alarmManager;
	
	@Override
    protected Void doInBackground(Context... params) {
    	DataController rClient = new DataController(params[0]);
		try {
			rClient.uploadUsageLog();					
		} catch (IOException e) {
			Log.e(Constants.APP_NAME, e.toString());
			// If something goes wrong while fetching the data, retry in 10 minutes
			Long repeatIn = System.currentTimeMillis() + 600000;
			Intent dataFetch = new Intent(params[0], UsageUploadReceiver.class);
			repeatUploadPI = PendingIntent.getBroadcast(params[0], 0, dataFetch, PendingIntent.FLAG_CANCEL_CURRENT);
			alarmManager = (AlarmManager) params[0].getSystemService(Context.ALARM_SERVICE);
			alarmManager.set(AlarmManager.RTC_WAKEUP, repeatIn, repeatUploadPI);
		} finally {
			if (Constants.httpClient != null) {
				Constants.httpClient.getConnectionManager().shutdown();
			}
		}
		return null;
    }
}	
