package dk.dtu.imm.sensiblejournal2013.detailedViews;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
import dk.dtu.imm.sensiblejournal2013.usageLog.LogDbHelper;
import dk.dtu.imm.sensiblejournal2013.utilities.AppFunctions;
import dk.dtu.imm.sensiblejournal2013.utilities.Constants;
import dk.dtu.imm.sensiblejournal2013.utilities.CustomInfoWindowAdpater;
import dk.dtu.imm.sensiblejournal2013.utilities.DetailsListAdapter;
import dk.dtu.imm.sensiblejournal2013.utilities.TripDetails;
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
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v4.app.FragmentActivity;

public class WeeklyItineraryDetailedView extends FragmentActivity implements OnMarkerClickListener {

	private GoogleMap map;
	private float total_distance_travelled;
	private AppFunctions functions;
	private List<?> itineraryLocations;
	private List<?> itineraryArrivals;
	private List<?> itineraryDepartures; 
	private List<?> itineraryPOI_ids;
	private List<Marker> markers;
	private ListView itineraryList;
	private int noOfUniquePlaces;
	private int noOfStops;
	private int curr = 0; // used for the animation
	private int rotate_move = 0;
	private LatLng tmpStart = null;
	private LatLng tmpFinish = null;
	private boolean animating = false;
	private CancelableCallback simpleAnimationCancelableCallback;
	private TripDetails tripDetails;
	private ImageButton animate;
	private List<String> addresses;
	private long totalDurationStationary = 0;
	private long totalDurationWalking = 0;
	private long totalDurationBike = 0;
	private long totalDurationVehicle = 0;
	private long enter_timestamp;
	private boolean detailsDrawn[] = {false};
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weekly_itinerary_detailed_view);
		setupActionBar();       
		functions = new AppFunctions(this);
		    
		// Gets the Map fragment from the XML layout and creates it
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();   
	    map.setMyLocationEnabled(true);
	    map.setInfoWindowAdapter(new CustomInfoWindowAdpater(this));
	    map.setOnMarkerClickListener(this);
	     
	    // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
	    try {
	    	MapsInitializer.initialize(this);
	    } catch (GooglePlayServicesNotAvailableException e) {
	    	e.printStackTrace();
	    }
	    
	    Intent intent = getIntent();	    
	    itineraryLocations = (List<?>) intent.getExtras().get(Constants.STOP_LOCATIONS);
	    itineraryArrivals = (List<?>) intent.getExtras().get(Constants.STOP_LOCATIONS_ARRIVALS);
	    itineraryDepartures = (List<?>) intent.getExtras().get(Constants.STOP_LOCATIONS_DEPARTURES);
	    itineraryPOI_ids = (List<?>) intent.getExtras().get(Constants.POIs);
	    String selectedWeek = (String) intent.getExtras().get(Constants.SELECTED_WEEK);
	    setTitle(selectedWeek);
		
	    itineraryList = (ListView) findViewById(R.id.itinerary_list);	    
	    final ArrayList<TwoStringListObject> itineraryArray =  new ArrayList<TwoStringListObject>();
	    final ArrayList<Integer> listIcons =  new ArrayList<Integer>();
    	markers = new LinkedList<Marker>();    	
    	
    	// Determine the number of stops and...
	    noOfStops = itineraryPOI_ids.size();
	    // ...the number of unique visited places
	    Set<Integer> tmpSet = new HashSet<Integer>((List<Integer>) itineraryPOI_ids);
	    noOfUniquePlaces = tmpSet.size();
    	
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
			    functions.animateToBounds((List<Location>) itineraryLocations, map, itineraryLocations.size());
	 		}
	 	};
	 	Constants.progressDialog = ProgressDialog.show(WeeklyItineraryDetailedView.this, "", "Loading. Please wait...");
	 		
		// This is run by the main (UI) thread due to the fact that it
		// updates the UI (only main thread can do that)
		final Runnable updateMap = new Runnable() {
			@Override
			public void run() {
				
				if ((itineraryLocations.size() > 0) && (addresses.size() > 0)) {				
					// Disable the details and play buttons
					ImageButton showDetails = (ImageButton) findViewById(R.id.show_details_button);
					final ImageButton moreDetails = (ImageButton) findViewById(R.id.show_more);
					animate = (ImageButton) findViewById(R.id.animate_itinerary);
					showDetails.setEnabled(false);
					animate.setEnabled(false);
													
			    	TwoStringListObject locationDetails = new TwoStringListObject();
			    	TwoStringListObject commuteDetails = new TwoStringListObject();
					for ( int i=0; i<itineraryLocations.size(); i++ ) {
						
						LatLng tmpLocation = new LatLng(((Location) itineraryLocations.get(i)).getLatitude(),
								((Location) itineraryLocations.get(i)).getLongitude());
						options.add(tmpLocation);						
						
						locationDetails = new TwoStringListObject();
						locationDetails.setString1(addresses.get(i).substring(0, addresses.get(i).indexOf("\n")));
						locationDetails.setString2("From: " + ((Date) itineraryArrivals.get(i)).toString().substring(11, 20) +
													" To: " +  ((Date) itineraryDepartures.get(i)).toString().substring(11, 20));
						itineraryArray.add(locationDetails);		    				    	
						listIcons.add(R.drawable.ic_action_place);
							
						if (i < itineraryLocations.size()-1) {
							commuteDetails = new TwoStringListObject();
							commuteDetails.setString1("Travel for: " + tripDetails.getDistance(i) + " km");
							commuteDetails.setString2("Average speed: " + tripDetails.getSpeed(i) + " km/h");
							itineraryArray.add(commuteDetails);
							
							if (tripDetails.getVehicle(i).equals("car")) {
								listIcons.add(R.drawable.driving);
								totalDurationVehicle += tripDetails.getDuration(i);
							}
							else if (tripDetails.getVehicle(i).equals("bike")) {
								listIcons.add(R.drawable.biking);
								totalDurationBike += tripDetails.getDuration(i);
							}
							else if (tripDetails.getVehicle(i).equals("plane")) {
								listIcons.add(R.drawable.plane);
								totalDurationVehicle += tripDetails.getDuration(i);
							}
							else {
								listIcons.add(R.drawable.walking);
								totalDurationWalking += tripDetails.getDuration(i);
							}
						}
																			    		 
				    	if ((itineraryArrivals.get(i) instanceof Date) && (itineraryDepartures.get(i) instanceof Date)) {     	
					    	if (i == 0) {			    		 
					    		markers.add(map.addMarker(new MarkerOptions()
					    							.position(new LatLng(tmpLocation.latitude, tmpLocation.longitude))
					    							.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
					    							.title(addresses.get(i))
					    		    				.snippet("From: " + ((Date) itineraryArrivals.get(i)).toString().substring(0, 20) +
					    		    						"\nTo: " +  ((Date) itineraryDepartures.get(i)).toString().substring(0, 20))));		    				    				    				    	
					    	}
					    	
					    	else if (i == itineraryLocations.size()-1) {		    		
					    		markers.add(map.addMarker(new MarkerOptions()
													.position(new LatLng(tmpLocation.latitude, tmpLocation.longitude))										
													.title(addresses.get(i))
					    		    				.snippet("From: " + ((Date) itineraryArrivals.get(i)).toString().substring(0, 20) +
					    		    						"\nTo: " +  ((Date) itineraryDepartures.get(i)).toString().substring(0, 20))));		    		
					    	}
					    	
					    	else {   		
					    		markers.add(map.addMarker(new MarkerOptions()
													.position(new LatLng(tmpLocation.latitude, tmpLocation.longitude))
													.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
													.title(addresses.get(i))
					    		    				.snippet("From: " + ((Date) itineraryArrivals.get(i)).toString().substring(0, 20) +
					    		    						"\nTo: " +  ((Date) itineraryDepartures.get(i)).toString().substring(0, 20))));		    		
					    	}
				    	}
						
					}
					
					// Total stationary time for the day is 24 hours - total travelling time
					// 86400 in epoch time equals 24 hours, 7*86400 equals 1 week
					totalDurationStationary = 7*86400 - totalDurationVehicle - totalDurationBike - totalDurationWalking;
					functions.fillItineraryDetails(noOfUniquePlaces, noOfStops, total_distance_travelled, 
							totalDurationStationary, totalDurationWalking, totalDurationBike,
							totalDurationVehicle, WeeklyItineraryDetailedView.this);
				    				
					DetailsListAdapter adapter = new  DetailsListAdapter(WeeklyItineraryDetailedView.this, itineraryArray, listIcons, R.layout.details_list_row);
				    itineraryList.setAdapter(adapter);
			        
				    itineraryList.setOnItemClickListener(new OnItemClickListener() {
				    	LatLng location;
				    	
				    	@Override
				        public void onItemClick(AdapterView<?> parent, View view,
				            int position, long id) {
				    		
				    		// Only evaluate location entries from the list, not commute entries
				    		// The location entries are in even numbered places in the list
				    		if (position%2 == 0) {
				    			location = new LatLng((((Location) itineraryLocations.get(position/2)).getLatitude() + 0.003),
			         					((Location) itineraryLocations.get(position/2)).getLongitude());
				    			
				    			CameraPosition cameraPos = new CameraPosition(location, 15, 0, 0);
				    			map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
				    			
				    			// Kill animation
								final ImageButton animate = (ImageButton) findViewById(R.id.animate_itinerary);
								animating = false;
					            animate.setImageResource(R.drawable.ic_action_play);
					            markers.get(position/2).showInfoWindow();
				    		}
				        }
	
				    });
	
				    //Add the polyline to the map
				    map.addPolyline(options);	   			    
				         
			        animate = (ImageButton) findViewById(R.id.animate_itinerary);
			        animate.setOnClickListener(new Button.OnClickListener() {
			        	
			            public void onClick(View v) {
			            	if (!animating) {
			            		animating = true;
			            		animate.setImageResource(R.drawable.ic_action_stop);
			            		itineraryList.setItemChecked(0, true);
			            		itineraryList.smoothScrollToPosition(0);    							    				   
			            		startAnimation();
			            	}
			            	else {
			            		animating = false;
			            		functions.animateToBounds((List<Location>) itineraryLocations, map, itineraryLocations.size());
			            		animate.setImageResource(R.drawable.ic_action_play);
			            	}
			            }
			        });
			        
			        moreDetails.setOnClickListener(new Button.OnClickListener() {		        	
			        	LinearLayout detailsLayout = (LinearLayout) findViewById(R.id.details_layout);
			            public void onClick(View v) {
			            	LinearLayout detailsLayoutPage1 = (LinearLayout) findViewById(R.id.details_layout_page1);
			            	LinearLayout detailsLayoutPage2 = (LinearLayout) findViewById(R.id.details_layout_page2);
			            	if(detailsLayoutPage2.getVisibility() == View.GONE){
			            		moreDetails.setImageResource(R.drawable.up_arrow);
			            		detailsLayoutPage1.setVisibility(View.GONE);
			            		detailsLayoutPage2.setVisibility(View.VISIBLE);
			            		
			            		if (itineraryArray.size() < 2){
			            			LayoutParams params = detailsLayout.getLayoutParams();
			            			params.height = LayoutParams.WRAP_CONTENT;
			            			detailsLayout.setLayoutParams(params);
			            		}			            			
			            		else {
			            			LayoutParams params = detailsLayout.getLayoutParams();
			            			final float scale = getResources().getDisplayMetrics().density;
			            			params.height = (int) (Constants.DETAILS_HEIGHT * scale + 0.5f);
			            			detailsLayout.setLayoutParams(params);
			            		}
			            	}
			            	else {			        
			            		LayoutParams params = detailsLayout.getLayoutParams();
			            		params.height = LayoutParams.WRAP_CONTENT;
			            		detailsLayout.setLayoutParams(params);
			            		
			            		moreDetails.setImageResource(R.drawable.down_arrow);
			            		detailsLayoutPage1.setVisibility(View.VISIBLE);
			            		detailsLayoutPage2.setVisibility(View.GONE);		            					            		
			            	}
			            }
			        });
			              
			        Constants.progressDialog.dismiss();
			        animate.setEnabled(true);
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
				
			    tripDetails = functions.calculateTripDetails(itineraryLocations, 
											itineraryArrivals, itineraryDepartures);
				total_distance_travelled = tripDetails.getTotalDistance();
			    
			    addresses = new LinkedList<String>();
			    
			    try {
				    for ( int i=0; i<itineraryLocations.size(); i++ ) {
						
						LatLng tmpLocation = new LatLng(((Location) itineraryLocations.get(i)).getLatitude(),
								((Location) itineraryLocations.get(i)).getLongitude());											
						Geocoder geocoder = new Geocoder(WeeklyItineraryDetailedView.this, Locale.getDefault());
						List<Address> address = null;				
						
						address = geocoder.getFromLocation(tmpLocation.latitude, tmpLocation.longitude, 1);
						addresses.add(i, address.get(0).getAddressLine(0) + "\n" +	address.get(0).getAddressLine(1));								
				    }
				    runOnUiThread(updateMap);
				} catch (Exception e) {
					e.printStackTrace();
					final AlertDialog.Builder builder = new AlertDialog.Builder(WeeklyItineraryDetailedView.this);
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
	
	
	////////Method that animates the map according to the selected route /////////
	private void startAnimation() {
		
		simpleAnimationCancelableCallback =
				new CancelableCallback(){
	
					@Override
					public void onCancel() {
					}
	
					@SuppressWarnings("unchecked")
					@Override
					public void onFinish() {
						itineraryList.setItemChecked(curr*2, true);
						itineraryList.smoothScrollToPosition(curr*2);
						
						if (rotate_move == 0) { 
							if (curr == itineraryLocations.size()-1) {
								// Animation finished
								final ImageButton animate = (ImageButton) findViewById(R.id.animate_itinerary);
								animating = false;
					            functions.animateToBounds((List<Location>) itineraryLocations, map, itineraryLocations.size());
					            animate.setImageResource(R.drawable.ic_action_play);
					            return;
							}						
						}
						else curr++;		
						if(curr < itineraryLocations.size()){						
										
							if (detailsDrawn[0] == false) markers.get(curr).showInfoWindow();
							LatLng location = new LatLng(((Location) itineraryLocations.get(curr)).getLatitude(),
									((Location) itineraryLocations.get(curr)).getLongitude());					
							
	
							if ((rotate_move==0) && (curr<itineraryLocations.size()-1)){
								tmpStart = location;						
									tmpFinish = new LatLng(((Location) itineraryLocations.get(curr+1)).getLatitude(),
												((Location) itineraryLocations.get(curr+1)).getLongitude());
								rotate_move = 1;
							}
							else {
								itineraryList.setItemChecked(curr*2-1, true);
								itineraryList.smoothScrollToPosition(curr*2-1);
								rotate_move = 0;
							}
							
							if (itineraryLocations.size()>1) {
								double heading = SphericalUtil.computeHeading(tmpStart, tmpFinish);
																		
								CameraPosition cameraPosition =
										new CameraPosition.Builder()
												.target(location)
												.tilt(curr<itineraryLocations.size()-1 ? 90 : 0)
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
		
		LatLng location = new LatLng(((Location) itineraryLocations.get(0)).getLatitude(),
					((Location) itineraryLocations.get(0)).getLongitude());
		
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
		enter_timestamp = System.currentTimeMillis();
	}
	
	@Override
	public void onPause() {	
		super.onPause();
		Constants.appVisible = 0;
		
		// Add the time spent in the activity to the log
		LogDbHelper logDbHelper = new LogDbHelper(this);
		logDbHelper.log(Constants.logComponents.WEEKLY_ITIN, System.currentTimeMillis()-enter_timestamp);
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
		getMenuInflater().inflate(R.menu.todays_itinerary_detailed_view, menu);
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
