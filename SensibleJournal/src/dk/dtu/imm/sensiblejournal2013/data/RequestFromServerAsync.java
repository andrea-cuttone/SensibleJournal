package dk.dtu.imm.sensiblejournal2013.data;

import java.io.IOException;

import org.json.JSONException;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import dk.dtu.imm.sensiblejournal2013.receivers.DataFetchReceiver;
import dk.dtu.imm.sensiblejournal2013.utilities.Constants;

public class RequestFromServerAsync extends AsyncTask<Context, Void, Void> {
	private PendingIntent repeatFetchingPI;
	private AlarmManager alarmManager;
	
	@Override
	protected Void doInBackground(Context... params) {
		DataController rClient = new DataController(params[0]);
		try {
			// Asynchronously request data from server
			rClient.getDataFromServer();
			Constants.newDataFetched = true;				
		} catch (IOException e) {
			e.printStackTrace();
			// If something goes wrong while fetching the data, retry in 10 minutes
			Long repeatIn = System.currentTimeMillis() + 600000;
			Intent dataFetch = new Intent(params[0], DataFetchReceiver.class);
			repeatFetchingPI = PendingIntent.getBroadcast(params[0], 0, dataFetch, PendingIntent.FLAG_CANCEL_CURRENT);
			alarmManager = (AlarmManager) params[0].getSystemService(Context.ALARM_SERVICE);
			alarmManager.set(AlarmManager.RTC_WAKEUP, repeatIn, repeatFetchingPI);
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			Constants.httpClient.getConnectionManager().shutdown();
		}
		
		return null;
	}
}
