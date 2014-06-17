package dk.dtu.imm.sensiblejournal2013.detailedViews;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import dk.dtu.imm.sensiblejournal2013.R;
import dk.dtu.imm.sensiblejournal2013.detailedViews.utilities.CustomInfoWindowAdpater;
import dk.dtu.imm.sensiblejournal2013.detailedViews.utilities.DetailsListAdapter;
import dk.dtu.imm.sensiblejournal2013.usageLog.LogDbHelper;
import dk.dtu.imm.sensiblejournal2013.utilities.Constants;
import dk.dtu.imm.sensiblejournal2013.utilities.AppFunctions;
import dk.dtu.imm.sensiblejournal2013.utilities.TwoStringListObject;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v4.app.FragmentActivity;

public class CommuteDetailedView extends FragmentActivity implements OnMarkerClickListener  {

	private GoogleMap map;
	private float total_distance_travelled;
    private String routeSpeed;
    private String vehicle;
    private AppFunctions functions;
	private List<?> locations;
	private List<?> arrivals;
	private List<?> departures;
	private List<Marker> markers;
	private ListView routeList;
	private int curr = 0; // used for the animation
	private int rotate_move = 0;
	private LatLng tmpStart = null;
	private LatLng tmpFinish = null;
	private boolean animating = false;
	private CancelableCallback simpleAnimationCancelableCallback;
	private List<String> addresses;
	private long enter_timestamp;
	private boolean detailsDrawn[] = {false};
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_commute_detailed_view);
        setupActionBar();
        functions = new AppFunctions(this);
        
        // Gets the Map fragment from the XML layout and creates it
     	map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMap();   
	    map.setInfoWindowAdapter(new CustomInfoWindowAdpater(this));
	    map.setOnMarkerClickListener(this);
	     
	    // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
	    try {
	    	MapsInitializer.initialize(this);
	    } catch (GooglePlayServicesNotAvailableException e) {
	    	e.printStackTrace();
	    }
	    
	    Intent intent = getIntent();	    
	    locations = (List<?>) intent.getExtras().get(Constants.STOP_LOCATIONS);
	    arrivals = (List<?>) intent.getExtras().get(Constants.STOP_LOCATIONS_ARRIVALS);
	    departures = (List<?>) intent.getExtras().get(Constants.STOP_LOCATIONS_DEPARTURES);
	    total_distance_travelled = (Float) intent.getExtras().get(Constants.DISTANCE);
	    routeSpeed = (String) intent.getExtras().get(Constants.SPEED);
	    vehicle = (String) intent.getExtras().get(Constants.VEHICLE);
	    setTitle("Latest Journey");
		
	    final PolylineOptions options = new PolylineOptions();
	    options.color(Color.parseColor("#AA29A3CC"));
	    options.width(9);		    
	    options.visible(true);
	    
	    // This is run by the main (UI) thread due to the fact that it
	 	// updates the UI (only main thread can do that)
	 	final Runnable initializeMap = new Runnable() {
	 		@Override
	 		public void run() {
	 	 		// Move camera to bounds determined by the itineraryLocations list
	 		    functions.animateToBounds((List<Location>) locations, map, locations.size());
	 	 	}
	 	 };
	 	Constants.progressDialog = ProgressDialog.show(CommuteDetailedView.this, "", "Loading. Please wait...");
	 	 		
	 	// This is run by the main (UI) thread due to the fact that it
		// updates the UI (only main thread can do that)
		final Runnable updateMap = new Runnable() {
			@Override
			public void run() {
				
				if ((locations.size() > 0) && (addresses.size() > 0)) {
				    // Add the data to the map
				    routeList = (ListView) findViewById(R.id.route_list);	    
				    ArrayList<TwoStringListObject> routeArray =  new ArrayList<TwoStringListObject>();	    	   
			    	markers = new LinkedList<Marker>();
				    
			    	TwoStringListObject tmpDetails = new TwoStringListObject();
			
				   	LatLng tmpLocation = new LatLng(((Location) locations.get(0)).getLatitude(),
			     			((Location) locations.get(0)).getLongitude());	   	
				   	options.add(tmpLocation);
				   		 
					markers.add(map.addMarker(new MarkerOptions()
										.position(new LatLng(tmpLocation.latitude, tmpLocation.longitude))
										.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
										.title(addresses.get(0))
					    				.snippet("Left at: " + ((Date) departures.get(0)).toString().substring(0, 20))));		    				    		
					
					tmpDetails.setString1(addresses.get(0).substring(0, addresses.get(0).indexOf("\n")));
					tmpDetails.setString2("Left at: " + ((Date) departures.get(0)).toString().substring(11, 20));
					routeArray.add(tmpDetails);
					    				    				    
					tmpLocation = new LatLng(((Location) locations.get(1)).getLatitude(),
								((Location) locations.get(1)).getLongitude());
					options.add(tmpLocation);
						
					markers.add(map.addMarker(new MarkerOptions()
									.position(new LatLng(tmpLocation.latitude, tmpLocation.longitude))										
									.title(addresses.get(1))
					    			.snippet("Arrived at: " + ((Date) arrivals.get(1)).toString().substring(0, 20))));
					
					tmpDetails = new TwoStringListObject();
					tmpDetails.setString1(addresses.get(1).substring(0, addresses.get(1).indexOf("\n")));
					tmpDetails.setString2("Arrived at: " + ((Date) arrivals.get(1)).toString().substring(11, 20));
					routeArray.add(tmpDetails);
					   				    		 	    	    		   
				    DetailsListAdapter adapter = new  DetailsListAdapter(CommuteDetailedView.this, routeArray, null, R.layout.details_list_row);
				    routeList.setAdapter(adapter);
				    
				    routeList.setOnItemClickListener(new OnItemClickListener() {
				    	LatLng location;
				    	
				    	@Override
				        public void onItemClick(AdapterView<?> parent, View view,
				            int position, long id) { 	        	
				    			location = new LatLng(((Location) locations.get(position)).getLatitude() + 0.003,
			         					((Location) locations.get(position)).getLongitude());
				    			
				    			CameraPosition cameraPos = new CameraPosition(location, 15, 0, 0);
				    			map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
				    			
				    			// Kill animation
								final ImageButton animate = (ImageButton) findViewById(R.id.animate_route);
								animating = false;
					            animate.setImageResource(R.drawable.ic_action_play);
					            markers.get(position).showInfoWindow();
				        }
			
				    });
			
				    //Add the polyline to the map
				    map.addPolyline(options);	   
			     
			        final ImageButton animate = (ImageButton) findViewById(R.id.animate_route);
			        animate.setOnClickListener(new Button.OnClickListener() {
			        	
			            public void onClick(View v) {
			            	if (!animating) {
			            		animating = true;
			            		animate.setImageResource(R.drawable.ic_action_stop);
			            		routeList.setItemChecked(0, true);
			            		routeList.smoothScrollToPosition(0);    							    				   
			            		startAnimation();
			            	}
			            	else {
			            		animating = false;
			            		functions.animateToBounds((List<Location>) locations, map, locations.size());
			            		animate.setImageResource(R.drawable.ic_action_play);
			            	}
			            }
			        });
			           
			        Constants.progressDialog.dismiss();
			        functions.createDetailsButton(detailsDrawn, markers);        
				}
			}
		};
        
        // Add the data to the map
	    // This is run by a separate thread and is responsible for creating the new cards
		Runnable getTripDetails = new Runnable() {
			@Override
		 	public void run() {
				runOnUiThread(initializeMap);				

		    	Geocoder geocoder = new Geocoder(CommuteDetailedView.this, Locale.getDefault());
		    	addresses = new LinkedList<String>();
		    	
		    	try {
				    for ( int i=0; i<locations.size(); i++ ) {
				    	LatLng tmpLocation = new LatLng(((Location) locations.get(i)).getLatitude(),
								((Location) locations.get(i)).getLongitude());
				    	
					   	List<Address> address = null;				   	
				   		address = geocoder.getFromLocation(tmpLocation.latitude, tmpLocation.longitude, 1);
						addresses.add(i, address.get(0).getAddressLine(0) + "\n" +	address.get(0).getAddressLine(1));
					    functions.fillCommuteDetails(total_distance_travelled, routeSpeed, vehicle, CommuteDetailedView.this);						
				    }
				    runOnUiThread(updateMap);
				} catch (Exception e) {
					e.printStackTrace();
					final AlertDialog.Builder builder = new AlertDialog.Builder(CommuteDetailedView.this);
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
				}
			}			    
		};
		
		// The thread is started here
		Thread thread =  new Thread(null, getTripDetails);
		thread.start();	
	}
	
	//////// Method that animates the map according to the selected route /////////
	private void startAnimation() {
		
		 simpleAnimationCancelableCallback =
				new CancelableCallback(){

					@Override
					public void onCancel() {
					}

					@SuppressWarnings("unchecked")
					@Override
					public void onFinish() {

						if (rotate_move == 0) { 
							if (curr == locations.size()-1) {
								// Animation finished
								final ImageButton animate = (ImageButton) findViewById(R.id.animate_route);
								animating = false;
					            functions.animateToBounds((List<Location>) locations, map, locations.size());
					            animate.setImageResource(R.drawable.ic_action_play);
					            return;
							}						
						}
						else curr++;		
						if(curr < locations.size()){
							
							routeList.setItemChecked(curr, true);
							routeList.smoothScrollToPosition(curr);							
								
							if (detailsDrawn[0] == false) markers.get(curr).showInfoWindow();
							LatLng location = new LatLng(((Location) locations.get(curr)).getLatitude(),
									((Location) locations.get(curr)).getLongitude());					
							

							if ((rotate_move==0) && (curr<locations.size()-1)){
								tmpStart = location;						
									tmpFinish = new LatLng(((Location) locations.get(curr+1)).getLatitude(),
												((Location) locations.get(curr+1)).getLongitude());
								rotate_move = 1;
							}
							else rotate_move = 0;
							
							if (locations.size()>1) {
								double heading = SphericalUtil.computeHeading(tmpStart, tmpFinish);
																		
								CameraPosition cameraPosition =
										new CameraPosition.Builder()
												.target(location)
												.tilt(curr<locations.size()-1 ? 90 : 0)
							                    .bearing((float)heading)
							                    .zoom(map.getCameraPosition().zoom)
							                    .build();
																											
								map.animateCamera(
										CameraUpdateFactory.newCameraPosition(cameraPosition), 
										Constants.ANIMATION_DELAY,
										simpleAnimationCancelableCallback);
							}
						}
					}
				};	
		
		LatLng location = new LatLng(((Location) locations.get(0)).getLatitude(),
					((Location) locations.get(0)).getLongitude());
		
		curr = 0;
		if (detailsDrawn[0] == false) markers.get(curr).showInfoWindow();
		map.animateCamera(												
		        CameraUpdateFactory.newLatLngZoom(location, 15), 
		        Constants.ANIMATION_DELAY,
		        simpleAnimationCancelableCallback);
	}
	
	@Override
    public boolean onMarkerClick(final Marker marker) {

		LatLng location = new LatLng(marker.getPosition().latitude + 0.003, marker.getPosition().longitude);
		marker.showInfoWindow();
		
		CameraPosition cameraPos = new CameraPosition(location, 15, 0, 0);
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
		return true;
    }
		
	@Override
	public void onResume() {
		super.onResume();
		Constants.appVisible = 1;
		LogDbHelper logDbHelper = new LogDbHelper(this);
		logDbHelper.log(Constants.logComponents.LATEST_JOURNEY, System.currentTimeMillis());
	}
	
	@Override
	public void onPause() {	
		super.onPause();
		Constants.appVisible = 0;
		
		// Add the time spent in the activity to the log
		LogDbHelper logDbHelper = new LogDbHelper(this);
		logDbHelper.log(Constants.logComponents.PAUSE, System.currentTimeMillis());
	}
	 
	@Override
	public void onDestroy() {
		super.onDestroy();
	}	 
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
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
		getMenuInflater().inflate(R.menu.commute_detailed_view, menu);
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
