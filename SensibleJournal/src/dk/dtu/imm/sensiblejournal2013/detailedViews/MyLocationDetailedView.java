package dk.dtu.imm.sensiblejournal2013.detailedViews;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import dk.dtu.imm.sensiblejournal2013.R;
import dk.dtu.imm.sensiblejournal2013.detailedViews.utilities.CustomInfoWindowAdpater;
import dk.dtu.imm.sensiblejournal2013.usageLog.LogDbHelper;
import dk.dtu.imm.sensiblejournal2013.utilities.Constants;
import dk.dtu.imm.sensiblejournal2013.utilities.AppFunctions;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;

public class MyLocationDetailedView extends Activity {

	private MapView mapView;
	private GoogleMap map;
	private AppFunctions functions;
	private long enter_timestamp;
	private Geocoder geocoder;
	private List<Address> addresses;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_location_detailed_view);
        setupActionBar();
        setTitle("My Current Location");
        functions = new AppFunctions(this);
        
        // Gets the MapView from the XML layout and creates it
     	mapView = (MapView) this.findViewById(R.id.map);
     	mapView.onCreate(savedInstanceState);
      
     	// Gets to GoogleMap from the MapView and does initialization stuff
     	map = mapView.getMap();
     	map.getUiSettings().setMyLocationButtonEnabled(true);
	    map.setInfoWindowAdapter(new CustomInfoWindowAdpater(this));
	    	    	    
	 	Constants.progressDialog = ProgressDialog.show(MyLocationDetailedView.this, "", "Loading. Please wait...");
	     
	    // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
	    try {
	    	MapsInitializer.initialize(this);
	    } catch (GooglePlayServicesNotAvailableException e) {
	    	e.printStackTrace();
	    }
	    
	    Intent intent = getIntent();
	    final Location myLocation = (Location) intent.getExtras().get(Constants.MY_LOCATION);
	    geocoder = new Geocoder(this, Locale.getDefault());
	    final IconGenerator iconFactory = new IconGenerator(this);
	    
	    final Runnable updateMap = new Runnable() {
	 		@Override
	 		public void run() {
	 			if (addresses != null) {
		 			if (addresses.size() > 0) {
		 				map.addMarker(new MarkerOptions()
		 			    	.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
		 			    	.title(addresses.get(0).getAddressLine(0))
		 			        .position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
		 			        .anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV())
		 			        .snippet(addresses.get(0).getAddressLine(1))).showInfoWindow();
		 			     
		 			    
		 			    // Updates the location and zoom of the MapView
		 			    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 15);
		 			    map.animateCamera(cameraUpdate);
		 	    	}
		 			
		 			Constants.progressDialog.dismiss();
	 			}
	 		}
	    };
	    	    
	    // This is run by the main (UI) thread due to the fact that it
	 	// updates the UI (only main thread can do that)
	 	final Runnable getData = new Runnable() {
	 		@Override
	 		public void run() {		    
		    	try {
		    		addresses = geocoder.getFromLocation(myLocation.getLatitude(), myLocation.getLongitude(), 1);
		    	} catch (Exception e) {
		    		e.printStackTrace();
		    		final AlertDialog.Builder builder = new AlertDialog.Builder(MyLocationDetailedView.this);
					builder.setMessage("Error! Check internet connection!")
				       .setCancelable(false)
				       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	 System.exit(0);
				           }
				       });
					runOnUiThread(new Runnable() {
						  public void run() {
							  AlertDialog alert = builder.create();
							  if(!isFinishing()) alert.show();
						  }
					});	
		    		return;
		    	}
		    	
		    	runOnUiThread(updateMap);
	 		}
		};
    	    
		// The thread is started here
		Thread thread =  new Thread(null, getData);
		thread.start();
    }

    @Override
	public void onResume() {
		super.onResume();
		mapView.onResume();
		Constants.appVisible = 1;
		enter_timestamp = System.currentTimeMillis();
	}
	
	@Override
	public void onPause() {	
		super.onPause();
		mapView.onPause();
		Constants.appVisible = 0;
		
		// Add the time spent in the activity to the log
		LogDbHelper logDbHelper = new LogDbHelper(this);
		logDbHelper.log(Constants.logComponents.CURRENT_LOC, System.currentTimeMillis()-enter_timestamp);
	}
 
	@Override
	public void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}
 
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mapView.onLowMemory();
	}	

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        	getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_location_detailed_view, menu);
        return true;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				super.onBackPressed();
				return true;
			case R.id.action_settings:
	    		functions.showAboutDialog();
	    		return true;	    	
            default:
            	break;
		}
		return super.onOptionsItemSelected(item);
	}

}
