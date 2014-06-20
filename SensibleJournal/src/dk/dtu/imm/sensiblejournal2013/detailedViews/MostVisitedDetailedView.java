package dk.dtu.imm.sensiblejournal2013.detailedViews;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

import dk.dtu.imm.sensiblejournal2013.R;
import dk.dtu.imm.sensiblejournal2013.detailedViews.utilities.CustomInfoWindowAdpater;
import dk.dtu.imm.sensiblejournal2013.detailedViews.utilities.DetailsListAdapter;
import dk.dtu.imm.sensiblejournal2013.usageLog.LogDbHelper;
import dk.dtu.imm.sensiblejournal2013.utilities.Constants;
import dk.dtu.imm.sensiblejournal2013.utilities.AppFunctions;
import dk.dtu.imm.sensiblejournal2013.utilities.TwoStringListObject;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.support.v4.app.FragmentActivity;

public class MostVisitedDetailedView extends FragmentActivity implements OnMarkerClickListener {

	private GoogleMap map;
	private ListView mostVisitedList;
	private AppFunctions functions;
	private List<Marker> markers;
	private List<?> mostVistitedlocations;
	private int curr = 0; // used for the animation	
	private boolean animating = false;
	private int zoom_move = 0;
	private boolean detailsDrawn[] = {false};
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_most_visited_detailed_view);
		setupActionBar();
		setTitle("Most Visited Places");
		markers = new LinkedList<Marker>();		
		
		// Reset the current most visited places counter to the initial value
		Constants.NO_OF_CURR_MOST_VISITED = Constants.NO_OF_MIN_MOST_VISITED;
		
		Constants.progressDialog = ProgressDialog.show(MostVisitedDetailedView.this, "", "Loading. Please wait...");
		functions = new AppFunctions(this);
		// Gets the Map fragment from the XML layout and creates it
     	map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMap();
     	// Gets to GoogleMap from the MapView and does initialization stuff
     	map.getUiSettings().setMyLocationButtonEnabled(true);
	    map.setInfoWindowAdapter(new CustomInfoWindowAdpater(this));
	    map.setOnMarkerClickListener(this);
	     
	    // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
	    try {
	    	MapsInitializer.initialize(this);
	    } catch (GooglePlayServicesNotAvailableException e) {
	    	e.printStackTrace();
	    }
	    
	    Intent intent = getIntent();
	    final String[] addresses = intent.getExtras().getStringArray(Constants.MOST_VISITED_POIs);
	    final String[] durations = intent.getExtras().getStringArray(Constants.MOST_VISITED_POIs_DURATIONS);
	    mostVistitedlocations =  (List<?>) intent.getExtras().get(Constants.MOST_VISITED_POIs_LOCATIONS);
	    final ArrayList<TwoStringListObject> mostVisitedArray =  new ArrayList<TwoStringListObject>();	   
	    	    
    	for(int i=0; i<Constants.NO_OF_MIN_MOST_VISITED; i++) {
    		markers.add(map.addMarker(new MarkerOptions()
				.position(new LatLng(((Location) mostVistitedlocations.get(i)).getLatitude(),
									((Location) mostVistitedlocations.get(i)).getLongitude()))
				.icon(BitmapDescriptorFactory.defaultMarker(Constants.markerColors[i]))
				.title(addresses[i])
				.snippet("Stayed for: " + durations[i])));    		    		
    		
    		// Add to 2-string holding object so that they can be added to the list
    		mostVisitedArray.add(new TwoStringListObject(addresses[i], durations[i]));
    	}    		    	  
	    
	    // Move camera to bounds determined by the itineraryLocations list
	    functions.animateToBounds((List<Location>) mostVistitedlocations, map, Constants.NO_OF_MIN_MOST_VISITED);	    
	    
	    mostVisitedList = (ListView) findViewById(R.id.most_visited_list);	    
	    DetailsListAdapter adapter = new  DetailsListAdapter(this, mostVisitedArray, null, R.layout.details_list_row);
	    mostVisitedList.setAdapter(adapter);
	    
	    // Create the details button on the detailed view	    
	    functions.createDetailsButton(detailsDrawn, markers);
	    
	    mostVisitedList.setOnItemClickListener(new OnItemClickListener() {
	    	LatLng location;
	    	
	    	@Override
	        public void onItemClick(AdapterView<?> parent, View view,
	            int position, long id) { 	        	
	    			location = new LatLng(((Location) mostVistitedlocations.get(position)).getLatitude() + 0.003,
         					((Location) mostVistitedlocations.get(position)).getLongitude());
	    			
	    			CameraPosition cameraPos = new CameraPosition(location, 15, 0, 0);
	    			map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
	    			
	    			// Kill animation
					final ImageButton animate = (ImageButton) findViewById(R.id.animate_most_visited);
					animating = false;
            		animate.setImageResource(R.drawable.ic_action_play);
            		markers.get(position).showInfoWindow();
	        }
	    });
	    	    	    	   
	    final TextView numberOfVisible = (TextView) findViewById(R.id.no_of_most_visited);
	    numberOfVisible.setText(String.valueOf(Constants.NO_OF_CURR_MOST_VISITED));
	    ImageButton increase = (ImageButton) findViewById(R.id.increase);
	    ImageButton decrease = (ImageButton) findViewById(R.id.decrease);
	    
	    increase.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	if (Constants.NO_OF_CURR_MOST_VISITED < Constants.NO_OF_MAX_MOST_VISITED) {
	        		int tmpVar = Constants.NO_OF_CURR_MOST_VISITED;
	        		
	        		if(++tmpVar <= mostVistitedlocations.size()) {
		        		Constants.NO_OF_CURR_MOST_VISITED++;
		        		numberOfVisible.setText(String.valueOf(Constants.NO_OF_CURR_MOST_VISITED));
		        	
		        		// Add to the map using the addPOIsToMap function from MiscFunctions
		        		functions.addPOIsToMap(markers, map, mostVistitedlocations, Constants.NO_OF_CURR_MOST_VISITED-1,
		        						durations[Constants.NO_OF_CURR_MOST_VISITED-1], mostVisitedArray);
		        		
		        		// Kill animation
		        		final ImageButton animate = (ImageButton) findViewById(R.id.animate_most_visited);
						animating = false;
	            		animate.setImageResource(R.drawable.ic_action_play);
	        		}
	        	}
	        		        		        	
	        }});
	    
	    decrease.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	if (Constants.NO_OF_CURR_MOST_VISITED > Constants.NO_OF_MIN_MOST_VISITED) {	        	
	        		Constants.NO_OF_CURR_MOST_VISITED--;
	        		numberOfVisible.setText(String.valueOf(Constants.NO_OF_CURR_MOST_VISITED));
	        		
	        		// Remove from map using the removePOIsFromMap function from MiscFunctions	        		
	        		functions.removePOIsFromMap(markers, mostVistitedlocations, Constants.NO_OF_CURR_MOST_VISITED,
	        																					map, mostVisitedArray);
	        		
	        		// Kill animation
	        		final ImageButton animate = (ImageButton) findViewById(R.id.animate_most_visited);
					animating = false;
            		animate.setImageResource(R.drawable.ic_action_play);
	        	}
	        }});
	    	    
	    final ImageButton animate = (ImageButton) findViewById(R.id.animate_most_visited);
        animate.setOnClickListener(new Button.OnClickListener() {
        	
            public void onClick(View v) {
            	if (!animating) {
            		animating = true;
            		animate.setImageResource(R.drawable.ic_action_stop);
            		mostVisitedList.setItemChecked(0, true);
            		mostVisitedList.smoothScrollToPosition(0);    							    				   
            		startAnimation(); 
            	}
            	else {
            		animating = false;
            		functions.animateToBounds((List<Location>) mostVistitedlocations,
            									map, Constants.NO_OF_CURR_MOST_VISITED);
            		animate.setImageResource(R.drawable.ic_action_play);
            	}
            }
        });
        
        Constants.progressDialog.dismiss();
	}
	
	private void startAnimation() {
		
		LatLng location = new LatLng(((Location) mostVistitedlocations.get(0)).getLatitude(),
									((Location) mostVistitedlocations.get(0)).getLongitude());
		
		curr = 0;
		zoom_move = 0;
		if (detailsDrawn[0] == false) markers.get(curr).showInfoWindow();
		map.animateCamera(												
		        CameraUpdateFactory.newLatLngZoom(location, 15), 
		        Constants.ANIMATION_DELAY,
		        simpleAnimationCancelableCallback);
	}

	CancelableCallback simpleAnimationCancelableCallback =
		new CancelableCallback(){		
			CameraPosition cameraPosition;
			
			@Override
			public void onCancel() {
			}

			@SuppressWarnings("unchecked")
			@Override
			public void onFinish() {
				
				if (zoom_move == 0) { 
					if (curr == Constants.NO_OF_CURR_MOST_VISITED-1) {
						// Animation finished
						final ImageButton animate = (ImageButton) findViewById(R.id.animate_most_visited);
						animating = false;
	            		functions.animateToBounds((List<Location>) mostVistitedlocations,
	            									map, Constants.NO_OF_CURR_MOST_VISITED);
	            		animate.setImageResource(R.drawable.ic_action_play);
					}						
				}
				else curr++;	
				if(curr < Constants.NO_OF_CURR_MOST_VISITED){																

					mostVisitedList.setItemChecked(curr, true);
					mostVisitedList.smoothScrollToPosition(curr);							
										
					if (detailsDrawn[0] == false) markers.get(curr).showInfoWindow();
					LatLng location = new LatLng(((Location) mostVistitedlocations.get(curr)).getLatitude(),
												((Location) mostVistitedlocations.get(curr)).getLongitude());
					
					if (zoom_move == 0) {
						zoom_move = 1;
					    cameraPosition =
								new CameraPosition.Builder()
										.target(location)
						                .zoom(11)
						                .build();
					}
					else {
						cameraPosition =
								new CameraPosition.Builder()
										.target(location)
						                .zoom(14)
						                .build();
						zoom_move = 0;
					}									
																								
					map.animateCamera(
							CameraUpdateFactory.newCameraPosition(cameraPosition), 
							Constants.ANIMATION_DELAY,
							simpleAnimationCancelableCallback);
				}
			}
		};
	
	@Override
	public boolean onMarkerClick(final Marker marker) {

		LatLng location = new LatLng(marker.getPosition().latitude + 0.03, marker.getPosition().longitude);
		marker.showInfoWindow();
			
		CameraPosition cameraPos = new CameraPosition(location, 12, 0, 0);
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
		return true;
	}
		
	@Override
	public void onResume() {
		super.onResume();
		Constants.appVisible = 1;
		
		LogDbHelper logDbHelper = new LogDbHelper(this);
		logDbHelper.log(Constants.logComponents.MOST_VISITED, System.currentTimeMillis());
		
		// If the application was paused...
		if (Constants.paused) {
			// ...add the pause time-stamp to the log
			logDbHelper.log(Constants.logComponents.PAUSE, Constants.timestamp);
		}
	}
	
	@Override
	public void onPause() {	
		super.onPause();
		Constants.appVisible = 0;
		
		Constants.timestamp = System.currentTimeMillis();
		Constants.paused = true;
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
		getMenuInflater().inflate(R.menu.most_visited_detailed_view, menu);
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
