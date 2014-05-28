package dk.dtu.imm.sensible.receivers;

import dk.dtu.imm.sensible.utilities.AppFunctions;
import dk.dtu.imm.sensible.utilities.Constants;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
	
	private AppFunctions functions;
	
	@Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
        	Log.w(Constants.APP_NAME, "Booted...Reseting alarms...");
        	functions = new AppFunctions(context);
        	functions.setAlarms(context);
        }
    }	
}
