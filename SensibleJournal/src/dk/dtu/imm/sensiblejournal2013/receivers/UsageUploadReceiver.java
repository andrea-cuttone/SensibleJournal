package dk.dtu.imm.sensible.receivers;

import java.io.IOException;

import dk.dtu.imm.sensible.data.DataController;
import dk.dtu.imm.sensible.utilities.Constants;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

public class UsageUploadReceiver extends BroadcastReceiver {
	
	private PendingIntent repeatUploadPI;
	private AlarmManager alarmManager;

	@Override
    public void onReceive(Context context, Intent intent) {
		new Request().execute(context);
    }
    
    // Asynchronously upload usage log to server
	class Request extends AsyncTask<Context, Void, Void> {
		@Override
	    protected Void doInBackground(Context... params) {
	    	DataController rClient = new DataController(params[0]);
			try {
				rClient.uploadUsageLog();					
			} catch (IOException e) {
				e.printStackTrace();
				// If something goes wrong while fetching the data, retry in 10 minutes
				Long repeatIn = System.currentTimeMillis() + 600000;
				Intent dataFetch = new Intent(params[0], UsageUploadReceiver.class);
				repeatUploadPI = PendingIntent.getBroadcast(params[0], 0, dataFetch, PendingIntent.FLAG_CANCEL_CURRENT);
				alarmManager = (AlarmManager) params[0].getSystemService(Context.ALARM_SERVICE);
				alarmManager.set(AlarmManager.RTC_WAKEUP, repeatIn, repeatUploadPI);
			} finally {
				Constants.httpClient.getConnectionManager().shutdown();
			}
			
			return null;
	    }
	}	
}
