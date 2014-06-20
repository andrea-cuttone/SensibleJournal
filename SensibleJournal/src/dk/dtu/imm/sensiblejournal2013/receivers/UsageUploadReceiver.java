package dk.dtu.imm.sensiblejournal2013.receivers;

import dk.dtu.imm.sensiblejournal2013.usageLog.UploadToServerAsync;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UsageUploadReceiver extends BroadcastReceiver {	

	@Override
    public void onReceive(Context context, Intent intent) {
		new UploadToServerAsync().execute(context);
    }
	
}
