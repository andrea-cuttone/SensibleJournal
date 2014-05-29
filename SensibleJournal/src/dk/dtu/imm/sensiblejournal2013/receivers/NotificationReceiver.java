package dk.dtu.imm.sensiblejournal2013.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import dk.dtu.imm.sensiblejournal2013.R;
import dk.dtu.imm.sensiblejournal2013.MainActivity;
import dk.dtu.imm.sensiblejournal2013.utilities.Constants;

public class NotificationReceiver extends BroadcastReceiver {
	 @Override
	    public void onReceive(Context context, Intent intent) {
	    	sendNotification("Sensible Lifelog", "Hi! Touch to check your lifelog!", context);
	    }
	    
	    @SuppressWarnings("deprecation")
		private void sendNotification(String title, String msg, Context context) {
			if (Constants.appVisible == 1) return;
	        NotificationManager mNotificationManager = (NotificationManager)
	        				context.getSystemService(Context.NOTIFICATION_SERVICE);

	        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
	                				new Intent(context, MainActivity.class), 0);

	        NotificationCompat.Builder mBuilder =
	                new NotificationCompat.Builder(context)
	                        .setSmallIcon(R.drawable.ic_launcher_small)
	                        .setContentTitle(title)
	                        .setContentText(msg);

	        mBuilder.setAutoCancel(true);
	        mBuilder.setContentIntent(contentIntent);        
	        mNotificationManager.notify(Constants.NOTIFICATION_ID, mBuilder.getNotification());
	    }	 
}
