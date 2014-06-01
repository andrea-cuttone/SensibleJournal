package dk.dtu.imm.sensible.login;

import dk.dtu.imm.sensible.R;

import dk.dtu.imm.sensible.MainActivity;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class StartActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_layout);
		Intent intent = new Intent(this, RegistrationHandler.class);
		startService(intent);
		SharedPreferences prefs = AuthActivity.getSystemPrefs(this);
		String token = prefs.getString(RegistrationHandler.PROPERTY_SENSIBLE_TOKEN, "");
		if (token.isEmpty() == false) {
			intent = new Intent(getBaseContext(), MainActivity.class);
			startActivity(intent);
		}
	}
}
