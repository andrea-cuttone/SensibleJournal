package dk.dtu.imm.sensiblejournal2013.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import dk.dtu.imm.sensiblejournal2013.data.RequestFromServerAsync;

public class DataFetchReceiver extends BroadcastReceiver {
			
	@Override
    public void onReceive(Context context, Intent intent) {
    	new RequestFromServerAsync().execute(context);
    }        
}
