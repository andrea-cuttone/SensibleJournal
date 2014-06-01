package dk.dtu.imm.sensible.receivers;

import java.io.IOException;
import org.json.JSONException;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import dk.dtu.imm.sensible.data.DataController;
import dk.dtu.imm.sensible.utilities.Constants;

public class DataFetchReceiver extends BroadcastReceiver {
	
	private PendingIntent repeatFetchingPI;
	private AlarmManager alarmManager;
	
	@Override
    public void onReceive(Context context, Intent intent) {
    	new Request().execute(context);
    }
    
    // Asynchronously request data from server
	class Request extends AsyncTask<Context, Void, Void> {
	    @Override
	    protected Void doInBackground(Context... params) {
	    	DataController rClient = new DataController(params[0]);
			try {
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
}
