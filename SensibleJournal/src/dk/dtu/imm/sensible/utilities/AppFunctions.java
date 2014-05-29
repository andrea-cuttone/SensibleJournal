package dk.dtu.imm.sensible.utilities;

import it.gmariotti.cardslib.library.internal.Card;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLngBounds.Builder;

import dk.dtu.imm.sensible.R;
import dk.dtu.imm.sensible.cards.basic.CommuteCard;
import dk.dtu.imm.sensible.cards.basic.MostVisitedPlacesCard;
import dk.dtu.imm.sensible.cards.basic.MyCurrentLocationCard;
import dk.dtu.imm.sensible.cards.basic.PastStopCard;
import dk.dtu.imm.sensible.cards.basic.TodaysItineraryCard;
import dk.dtu.imm.sensible.cards.basic.WeeklyItineraryCard;
import dk.dtu.imm.sensible.cards.tutorial.TutorialCard1;
import dk.dtu.imm.sensible.cards.tutorial.TutorialCard2;
import dk.dtu.imm.sensible.cards.tutorial.TutorialCard3;
import dk.dtu.imm.sensible.cards.tutorial.TutorialCard4;
import dk.dtu.imm.sensible.cards.tutorial.TutorialCard5;
import dk.dtu.imm.sensible.data.DataController;
import dk.dtu.imm.sensible.data.CacheDbHelper;
import dk.dtu.imm.sensible.data.CacheDatabaseContract.CacheEntry;
import dk.dtu.imm.sensible.receivers.DataFetchReceiver;
import dk.dtu.imm.sensible.receivers.NotificationReceiver;
import dk.dtu.imm.sensible.usageLog.LogDbHelper;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Geocoder;
import android.location.Location;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AppFunctions {
	
	private float total_distance_travelled;
	private Context context;
	private Geocoder geocoder;
	private LinkedList<Integer> POI_id;
	private LinkedList<Location> locations;			
	private LinkedList<Date> arrivals;
	private LinkedList<Date> departures;
	private LinkedList<String> days;
	private DecimalFormat decFormat;
	private View footerView;
	private LinkedList<Integer> tmpPOI_id;
	private LinkedList<Location> tmpItineraryLocations;		
	private LinkedList<Date> tmpArrivals;
	private LinkedList<Date> tmpDepartures;
	private String address = "";
	private int earthRadius = 6371;
	private List<Thread> threads;
	private boolean dayFound = false;
	private DataController rClient;
	private PendingIntent notificationPendingIntent;
	private PendingIntent fetchingPendingIntent;	
	private AlarmManager alarmManager;	
	
	public AppFunctions(Context context){
		this.context = context;
		geocoder = new Geocoder(context, Locale.getDefault());
		threads = new LinkedList<Thread>();
		rClient = new DataController(context);
		
		POI_id = new LinkedList<Integer>();        
		locations = new LinkedList<Location>();	
		arrivals = new LinkedList<Date>();            
		departures = new LinkedList<Date>();          
		days = new LinkedList<String>();
		
		tmpPOI_id = new LinkedList<Integer>();
		tmpItineraryLocations = new LinkedList<Location>();			
		tmpArrivals = new LinkedList<Date>();
		tmpDepartures = new LinkedList<Date>();
	}
			
	/** TRAVEL DISTANCE CALCULATION **/
	/*********************************/
	public TripDetails calculateTripDetails(List<?> itineraryLocations, List<?> arrivals, List<?> departures) {
		    		
		TripDetails tripDetails = new TripDetails();
        for (int i=0; i<itineraryLocations.size()-1; i++){        	        	        	        	
        	float distance = calculateHaversineDistance((Location) itineraryLocations.get(i), (Location) itineraryLocations.get(i+1));
            tripDetails.addDistance((float) distance);
            Long duration = (((Date) arrivals.get(i+1)).getTime()/1000) - (((Date) departures.get(i)).getTime()/1000);
            tripDetails.addDuration(duration);
            float routeSpeed = Float.parseFloat(calculateSpeed((float) distance, duration));
            tripDetails.addSpeed(Float.toString(routeSpeed));            
            tripDetails.addVehicle(determineMeansOfTransport(routeSpeed));
            total_distance_travelled += distance;      	
        }
        tripDetails.setTotalDistance(total_distance_travelled);
        return tripDetails;
	}
	
	public float calculateHaversineDistance(Location location1, Location location2) {
		
		//*** Compute the distance between two points using the haversine formula ***//
    	/********* Source http://www.movable-type.co.uk/scripts/latlong.html *********/
    	double latitudeDiffRad = Math.toRadians(location2.getLatitude() - location1.getLatitude());
    	double longtitudeDiffRad = Math.toRadians(location2.getLongitude() - location1.getLongitude());
    	double latitudeRadOrigin = Math.toRadians(location1.getLatitude());
    	double latitudeRadDestin = Math.toRadians(location2.getLatitude());

    	double haversine = Math.sin(latitudeDiffRad/2) * Math.sin(latitudeDiffRad/2) 
    						+ Math.sin(longtitudeDiffRad/2) * Math.sin(longtitudeDiffRad/2)
    						* Math.cos(latitudeRadOrigin) * Math.cos(latitudeRadDestin); 
    	double angularDist = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1-haversine)); 
    	double distance = earthRadius * angularDist;
    	/******************************************************************************/
    	
    	decFormat = new DecimalFormat("#.#");
    	distance = Float.valueOf(decFormat.format(distance));
    	return (float) distance;
	}
	
	// Method to calculate the travel speed for a specified distance
	public String calculateSpeed(float total_distance, Long duration) {
		float total_metres;
		Double route_speed;
		
        // Calculate the speed, first in km/h
        total_metres = total_distance*1000;
        route_speed = (total_metres/duration)*3.6;
        decFormat = new DecimalFormat("#.00");
		return decFormat.format(route_speed);  
	}
	
	// Method to determine the means of transport of a journey.
	public String determineMeansOfTransport(Float routeSpeed) {
		if ((routeSpeed>9.0) && (routeSpeed<20.0)) return "bike";
		if ((routeSpeed>=25.0) && (routeSpeed<200.0)) return "car";
		if (routeSpeed>=200.0) return "plane";
		else return "walk";
	}
	
	public void fillItineraryDetails(int noOfUniquePlaces, int noOfStops, float total_distance_travelled, 
									long totalDurationStationary, long totalDurationWalking, 
									long totalDurationBike, long totalDurationVehicle, Activity activity) {
		
		TextView total_distance_text;
		TextView no_of_unique_places_text;
		TextView no_of_stops_text;
		TextView stationary_text;
		TextView walking_text;
		TextView biking_text;
		TextView vehicle_text;
		
    	TextView total_distance;        
        TextView no_of_unique_places;        
        TextView no_of_stops;        
    	TextView stationary_time;        
        TextView walking_time;        
        TextView biking_time;        
        TextView vehicle_time;
		
		//Retrieve elements
    	total_distance_text = (TextView) activity.findViewById(R.id.total_distance_title);
        if (total_distance_text != null)
        	total_distance_text.setText(R.string.total_distance_text);
        
        no_of_unique_places_text = (TextView) activity.findViewById(R.id.no_of_unique_places_title);
        if (no_of_unique_places_text != null)
        	no_of_unique_places_text.setText(R.string.no_of_unique_places_text);
        
    	no_of_stops_text = (TextView) (TextView) activity.findViewById(R.id.no_of_stops_title);
    	if (no_of_stops_text != null)
    		no_of_stops_text.setText(R.string.no_of_stops_text);
    	
    	stationary_text = (TextView) activity.findViewById(R.id.stationary_title);
        if (stationary_text != null)
        	stationary_text.setText(R.string.stationary_title);
        
        walking_text = (TextView) activity.findViewById(R.id.walking_title);
        if (walking_text != null)
        	walking_text.setText(R.string.walking_title);
        
        biking_text = (TextView) (TextView) activity.findViewById(R.id.biking_title);
    	if (biking_text != null)
    		biking_text.setText(R.string.biking_title);
    	
    	vehicle_text = (TextView) (TextView) activity.findViewById(R.id.vehicle_title);
    	if (vehicle_text != null)
    		vehicle_text.setText(R.string.vehicle_title);
                    
        total_distance = (TextView) activity.findViewById(R.id.distance);
        if (total_distance != null) {
        	decFormat = new DecimalFormat("#.#");
            total_distance_travelled = Float.valueOf(decFormat.format(total_distance_travelled));
        	total_distance.setText(Float.toString(total_distance_travelled) + " Km");
        }              
        
        no_of_unique_places = (TextView) activity.findViewById(R.id.no_of_unique_places);
        if (no_of_unique_places != null)
        	no_of_unique_places.setText(Integer.toString(noOfUniquePlaces));
    		    		
    	no_of_stops = (TextView) (TextView) activity.findViewById(R.id.no_of_stops);
    	if (no_of_stops != null)
    		no_of_stops.setText(Integer.toString(noOfStops));
    	
    	stationary_time = (TextView) (TextView) activity.findViewById(R.id.time_stationary);
    	if (stationary_time != null)    		
        	stationary_time.setText(getHoursMinutes(totalDurationStationary, false));    	
    		    	
    	walking_time = (TextView) (TextView) activity.findViewById(R.id.time_walking);
    	if (walking_time != null)
    		walking_time.setText(getHoursMinutes(totalDurationWalking, false));
    	
    	biking_time = (TextView) (TextView) activity.findViewById(R.id.time_biking);
    	if (biking_time != null)
    		biking_time.setText(getHoursMinutes(totalDurationBike, false));
    	
    	vehicle_time = (TextView) (TextView) activity.findViewById(R.id.time_vehicle);
    	if (vehicle_time != null)
    		vehicle_time.setText(getHoursMinutes(totalDurationVehicle, false));
        
	}
	
	public String getDaysHoursMinutes(long duration) {
		int days = (int)TimeUnit.SECONDS.toDays(duration);        
    	long hours = TimeUnit.SECONDS.toHours(duration) - (days *24);
    	long minutes = TimeUnit.SECONDS.toMinutes(duration)
    						- (TimeUnit.SECONDS.toHours(duration)* 60);
    	
    	return days + "d " + String.format("%02d", hours) + "h " + String.format("%02d", minutes) + "m ";  			
	}
	
	public String getHoursMinutes(long duration, boolean getSeconds) {
		long hours = TimeUnit.SECONDS.toHours(duration);
    	long minutes = TimeUnit.SECONDS.toMinutes(duration)
    						- (TimeUnit.SECONDS.toHours(duration)* 60);
    	long seconds = TimeUnit.SECONDS.toSeconds(duration)
							- (TimeUnit.SECONDS.toMinutes(duration) *60);
    	    
    	return getSeconds ? hours + "h " + String.format("%02d", minutes) + "m " + String.format("%02d", seconds) + "s":
    						hours + "h " + String.format("%02d", minutes) + "m ";    			
	}
	
	public void fillCommuteDetails(float total_distance_travelled, String routeSpeed,
													String vehicle,	Activity activity) {

		TextView speed_text;
		TextView speed;
		TextView total_distance_text;
		TextView total_distance;
		ImageView meansOfTransp;
		
		//Retrieve elements
		speed_text = (TextView) activity.findViewById(R.id.speed_title);
		if (speed_text != null)
			speed_text.setText("Speed");
		
		speed = (TextView) activity.findViewById(R.id.speed);
		if (speed != null)
			speed.setText(routeSpeed + " km/h");		
		
		total_distance_text = (TextView) activity.findViewById(R.id.distance_title);
		if (total_distance_text != null)
		total_distance_text.setText("Distance");
		
		total_distance = (TextView) activity.findViewById(R.id.distance);
		if (total_distance != null) {
			decFormat = new DecimalFormat("#.#");
			total_distance_travelled = Float.valueOf(decFormat.format(total_distance_travelled));
			total_distance.setText(Float.toString(total_distance_travelled) + " Km");
		}
			
		 meansOfTransp = (ImageView) activity.findViewById(R.id.vehicle);
	     if (vehicle.equals("car")) meansOfTransp.setImageResource(R.drawable.driving_high);
	     else if (vehicle.equals("bike")) meansOfTransp.setImageResource(R.drawable.biking_high);
	     else meansOfTransp.setImageResource(R.drawable.walking_high);
	}
	
	// TODO Temporary method to read from CSV and store to the sqlite database
	public void fromCSVtoSqlite(String filename) {
		CacheDbHelper mDbHelper = new CacheDbHelper(context);
		
		// Gets the data repository in write mode
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
	           
		AssetManager assetManager = context.getAssets();
		InputStreamReader is = null;
		try {
			is = new InputStreamReader(assetManager.open(filename));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// Read first useless line
        BufferedReader reader = new BufferedReader(is);            
        String line;        
        int tmpPOI_id = 0;
        float tmpLatitude = 0;
        float tmpLongtitude = 0;
        long tmpArrival;
        long tmpDeparture;
        try { 
        	while ((line = reader.readLine()) != null) {
        		
        		int start = 0, end = 0;
        		end = line.indexOf(",");
        		tmpPOI_id = Integer.parseInt(line.substring(start, end));
        		        		
        		start = end+1;
        		end = line.indexOf(",", start);
        		tmpLatitude = Float.parseFloat(line.substring(start, end));
        		
        		start = end+1;
        		end = line.indexOf(",", start);
        		tmpLongtitude = Float.parseFloat(line.substring(start, end));        		
        		
        		start = end+1;
        		end = line.indexOf(",", start);
        		tmpArrival = Long.parseLong(line.substring(start, end)) * 1000;        		
        		        		
        		start = end+1;
        		end = line.length();
        		tmpDeparture = Long.parseLong(line.substring(start, end)) * 1000;
        		
        		
        		// Create a new map of values, where column names are the keys
        		ContentValues values = new ContentValues();
        		values.put(CacheEntry.COLUMN_POI_ID, tmpPOI_id);
        		values.put(CacheEntry.COLUMN_LATITUDE, tmpLatitude);
        		values.put(CacheEntry.COLUMN_LONGTITUDE, tmpLongtitude);
        		values.put(CacheEntry.COLUMN_ARRIVAL, tmpArrival);
        		values.put(CacheEntry.COLUMN_DEPARTURE, tmpDeparture);
        		        		
            	// Insert the new row, returning the primary key value of the new row
        		db.replace(CacheEntry.TABLE_NAME, null, values);
        	}
        } catch (IOException e) {
			e.printStackTrace();
		}
        db.close();      
	}
			
	// Function that creates the details button for the detailed views
	public void createDetailsButton(final boolean[] detailsDrawn, final List<Marker> markers) {		
		final ImageButton showDetails = (ImageButton) ((Activity) context)
									.findViewById(R.id.show_details_button);
										
		showDetails.setEnabled(true);
        showDetails.setImageResource(R.drawable.details);
        showDetails.setOnClickListener(new Button.OnClickListener() {
        	               
            public void onClick(View v) {
            	LinearLayout detailsLayout = (LinearLayout) ((Activity) context)
            						.findViewById(R.id.details_layout);
            	RelativeLayout mapLayout = (RelativeLayout) ((Activity) context)
            						.findViewById(R.id.map_layout);

            	// If there are more than two items in the list, create a minimum height for the layout 
            	if(detailsLayout.getVisibility() == View.GONE){
            		detailsLayout.setVisibility(View.VISIBLE);
            		detailsLayout.bringToFront();
            		detailsLayout.buildDrawingCache();            		
            		detailsLayout.setDrawingCacheEnabled(true);
            		mapLayout.buildDrawingCache();
            		mapLayout.setDrawingCacheEnabled(true);
            		showDetails.setSelected(true);
            		detailsDrawn[0] = true;            		
            		for (Marker marker : markers) marker.hideInfoWindow();            			            		
            	}
            	
            	else {
            		detailsLayout.setVisibility(View.GONE);
            		detailsDrawn[0] = false;
            	}
            }            
        });		
	}
	
	// Method that adds most visited POIs to the map and the most visited POI list 
	@SuppressWarnings("unchecked")
	public void addPOIsToMap(final List<Marker> markers, final GoogleMap map,  final List<?> locations, final int markerNo,
								final String duration, final ArrayList<TwoStringListObject> mostVisitedArray) {
		
		final Runnable addToMap = new Runnable() {			
			@Override
			public void run() {
				if (address != "") {
					// Pick a random marker color
					Random randomGenerator = new Random();
					int index = randomGenerator.nextInt(Constants.markerColors.length);
					
					markers.add(map.addMarker(new MarkerOptions()
					.position(new LatLng(((Location) locations.get(markerNo)).getLatitude(),
											((Location) locations.get(markerNo)).getLongitude()))
					.icon(BitmapDescriptorFactory.defaultMarker(Constants.markerColors[index]))
					.title(address)
					.snippet("Stayed for: " + duration)));
					
					// Add to 2-string holding object so that they can be added to the list
					mostVisitedArray.add(new TwoStringListObject(address, duration));
					
					// Move camera to bounds determined by the locations list
					animateToBounds((List<Location>) locations, map, markerNo+1);
								
					ListView mostVisitedList = (ListView) ((Activity) context).findViewById(R.id.most_visited_list);	    
				    DetailsListAdapter adapter = new  DetailsListAdapter(context, mostVisitedArray, null,
				    														R.layout.details_list_row);
				    mostVisitedList.setAdapter(adapter);
				}
			}
		};
		
		Runnable getAddress = new Runnable() {			
			@Override
			public void run() {
				
				for(int i=0; i<threads.size(); i++) {
					if (!threads.get(i).getName().equals("Add " + Constants.threadNumber)) {
						try { threads.get(i).join(); } 
						catch (InterruptedException e) { e.printStackTrace(); }
					}
				}
				
				try {
					address = geocoder.getFromLocation(((Location) locations.get(markerNo)).getLatitude(),
							((Location) locations.get(markerNo)).getLongitude(), 1).get(0).getAddressLine(0);
					((Activity) context).runOnUiThread(addToMap);
				} catch (Exception e) {
					e.printStackTrace();
					final AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setMessage("Error! Check internet connection!")
				       .setCancelable(false)
				       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	 System.exit(0);
				           }
				       });
					((Activity) context).runOnUiThread(new Runnable() {
						  public void run() {
							  AlertDialog alert = builder.create();
							  if(!((Activity) context).isFinishing()) alert.show();
						  }
					});
				}
			}
		};
					    
		// The thread is started here
		Thread thread =  new Thread(null, getAddress);
		Constants.threadNumber++;
		thread.setName("Add " + Constants.threadNumber);		
		thread.start();
		threads.add(thread);
	}
	
	// Method that removes most visited POIs from the map and the most visited POI list 
	@SuppressWarnings("unchecked")
	public void removePOIsFromMap(final List<Marker> markers, final List<?> locations, final int markerNo, final GoogleMap map, 
																		final ArrayList<TwoStringListObject> mostVisitedArray) {
		
		final Runnable updatePOIs = new Runnable() {			
			@Override
			public void run() {
				try {
					markers.get(markerNo).remove();
					markers.remove(markerNo);
				}
				catch (Exception e) {
					e.printStackTrace();
					return;
				}
				
				// Move camera to bounds determined by the locations list
				animateToBounds((List<Location>) locations, map, markerNo);
				
				// Remove the POI from the list
				mostVisitedArray.remove(markerNo);
				
				ListView mostVisitedList = (ListView) ((Activity) context).findViewById(R.id.most_visited_list);	    
			    DetailsListAdapter adapter = new  DetailsListAdapter(context, mostVisitedArray, null, R.layout.details_list_row);
			    mostVisitedList.setAdapter(adapter);
			}
		};
		
		Runnable removePOIs = new Runnable() {			
			@Override
			public void run() {
				
				// Wait for other threads to finish, in order to avoid confusion with markers
				for(int i=0; i<threads.size(); i++) {
					if (!threads.get(i).getName().equals("Remove " + Constants.threadNumber)) {
						try { threads.get(i).join(); } 
						catch (InterruptedException e) { e.printStackTrace(); }
					}
				}				
				
				((Activity) context).runOnUiThread(updatePOIs);
			}
		};
		
		// The thread is started here
		Thread thread =  new Thread(null, removePOIs);
		Constants.threadNumber++;
		thread.setName("Remove " + Constants.threadNumber);
		thread.start();
		threads.add(thread);
	}
	
	// Method that refreshes the cards in the feed
	public void refreshCards() {
		
		if (Constants.cards == null) {
			rClient = new DataController(context);
			Constants.cards = new ArrayList<Card>();
			
			///////////////////////////// TUTORIAL CARD 1 ////////////////////////////////
			SharedPreferences settings = context.getSharedPreferences("com.sensibleDTU.settings", 0);
		    boolean tutorialCardGone = settings.getBoolean("tutorial1Gone", false);
		    if (!tutorialCardGone) {
		    	TutorialCard1 tutorialCard1 = new TutorialCard1(context);
		    	tutorialCard1.setId("tutorial_card1");
		    	Constants.cards.add(tutorialCard1);
		    }
		    
			///////////////////////////// TUTORIAL CARD 2 ////////////////////////////////			
			tutorialCardGone = settings.getBoolean("tutorial2Gone", false);
			if (!tutorialCardGone) {
				TutorialCard2 tutorialCard2 = new TutorialCard2(context);
				tutorialCard2.setId("tutorial_card2");
				Constants.cards.add(tutorialCard2);
			}
			
			///////////////////////////// TUTORIAL CARD 3 ////////////////////////////////			
			tutorialCardGone = settings.getBoolean("tutorial3Gone", false);
			if (!tutorialCardGone) {
				TutorialCard3 tutorialCard3 = new TutorialCard3(context);
				tutorialCard3.setId("tutorial_card3");
				Constants.cards.add(tutorialCard3);
			}
			
			///////////////////////////// TUTORIAL CARD 4 ////////////////////////////////			
			tutorialCardGone = settings.getBoolean("tutorial4Gone", false);
			if (!tutorialCardGone) {
				TutorialCard4 tutorialCard4 = new TutorialCard4(context);
				tutorialCard4.setId("tutorial_card4");
				Constants.cards.add(tutorialCard4);
			}
			
			///////////////////////////// TUTORIAL CARD 5 ////////////////////////////////			
			tutorialCardGone = settings.getBoolean("tutorial5Gone", false);
			if (!tutorialCardGone) {
				TutorialCard5 tutorialCard5 = new TutorialCard5(context);
				tutorialCard5.setId("tutorial_card5");
				Constants.cards.add(tutorialCard5);
			}
			
			/////////////////////// MY CURRENT LOCATION CARD /////////////////////////////
			if (Constants.myLocation != null) {
				if (Constants.myCurrentLocationCard == null){
					Constants.myCurrentLocationCard = new MyCurrentLocationCard(context, Constants.myLocation);
					Constants.myCurrentLocationCard.setId("my_current_place");
					if (Constants.myCurrentLocationCard != null) { Constants.cards.add(Constants.myCurrentLocationCard); }
				}	
			}
			
			//////////////////////////// PAST STOP CARD //////////////////////////////////
			if (Constants.pastStopCard == null){
				POI_id = new LinkedList<Integer>();        
				locations = new LinkedList<Location>();	
				arrivals = new LinkedList<Date>();            
				departures = new LinkedList<Date>();          
				days = new LinkedList<String>();
				rClient.getDataFromCache(false, false, 1, POI_id, locations, arrivals, departures, days);
				   	
				/*** Only create the card if the user has visited more than 0 places ***/
				/***********************************************************************/
				if (locations.size() > 0) {
					Constants.pastStopCard = new PastStopCard(context, locations, arrivals, departures);
					Constants.pastStopCard.setId("past_stop");
					Constants.cards.add(Constants.pastStopCard);
				}
			}			
			
			///////////////////////////// COMMUTE CARD ///////////////////////////////////
			if (Constants.commuteCard == null){
				POI_id = new LinkedList<Integer>();        
				locations = new LinkedList<Location>();	
				arrivals = new LinkedList<Date>();            
				departures = new LinkedList<Date>();          
				days = new LinkedList<String>();
				rClient.getDataFromCache(false, false, 2, POI_id, locations, arrivals, departures, days);
				  	
				/*** Only create the card if the user has visited more than 1 places ***/
				/***********************************************************************/
				if (locations.size() > 1) {
					// Reverse the lists in order to achieve chronological sequence of the events
					Collections.reverse(locations);
					Collections.reverse(arrivals);
					Collections.reverse(departures);
					Constants.commuteCard = new CommuteCard(context, locations, arrivals, departures);
					Constants.commuteCard.setId("commute");
					if (Constants.commuteCard != null) { Constants.cards.add(Constants.commuteCard); }
				}
			}		
	       	
			/////////////////////// TODAY'S ITINERARY CARD /////////////////////////////
			if (Constants.todaysItCard == null){
				POI_id = new LinkedList<Integer>();        
				locations = new LinkedList<Location>();	
				arrivals = new LinkedList<Date>();            
				departures = new LinkedList<Date>();          
				days = new LinkedList<String>();
				rClient.getDataFromCache(true, false, 0, POI_id, locations, arrivals, departures, days);
				   	
				/*** Only create the card if the user has visited more than 0 places ***/
				/***********************************************************************/
				if (locations.size() > 0) {
					// Reverse the lists in order to achieve chronological sequence of the events
					Collections.reverse(POI_id);
					Collections.reverse(locations);
					Collections.reverse(arrivals);
					Collections.reverse(departures);
					Collections.reverse(days);
					Constants.todaysItCard = new TodaysItineraryCard(context, days.get(0), POI_id, locations, arrivals, departures, false);
					Constants.todaysItCard.setId("todays_itinerary");
					if (Constants.todaysItCard != null) { Constants.cards.add(Constants.todaysItCard); }
				}
			}
			
			/////////////////////// WEEKLY ITINERARY CARD /////////////////////////////
			if (Constants.weeklyItCard == null){
				POI_id = new LinkedList<Integer>();        
				locations = new LinkedList<Location>();	
				arrivals = new LinkedList<Date>();            
				departures = new LinkedList<Date>();          
				days = new LinkedList<String>();
				rClient.getDataFromCache(false, true, 0, POI_id, locations, arrivals, departures, days);
				   	
				/*** Only create the card if the user has visited more than 0 places ***/
				/***********************************************************************/
				if (locations.size() > 0) {
					// Reverse the lists in order to achieve chronological sequence of the events
					Collections.reverse(POI_id);
					Collections.reverse(locations);
					Collections.reverse(arrivals);
					Collections.reverse(departures);
					
					// Get the week's number and create the card
					Calendar cal = Calendar.getInstance();
					cal.setTime(departures.get(0));
					if (departures.get(0).toString().substring(0, 3).equals("Sun")) {
						cal.add(Calendar.DAY_OF_YEAR, -1);
					}
					Constants.CURR_WEEK = Integer.toString(cal.get(Calendar.WEEK_OF_YEAR));
					Constants.CURR_YEAR = Integer.toString(cal.get(Calendar.YEAR));
					Constants.tmpWeekNumber = Constants.CURR_WEEK;
					Constants.tmpYearNumber = Constants.CURR_YEAR;
					Constants.FEED_WEEK = Constants.CURR_WEEK;
					Constants.FEED_YEAR = Constants.CURR_YEAR;
					Constants.weeklyItCard = new WeeklyItineraryCard(context, Constants.CURR_WEEK, Constants.CURR_YEAR, POI_id, locations, arrivals, departures, false);
					Constants.weeklyItCard.setId("weekly_itinerary");
					if (Constants.weeklyItCard != null) { Constants.cards.add(Constants.weeklyItCard); }
				}
			}

			/////////////////////// MOST VISITED PLACES CARD /////////////////////////////
			if (Constants.mostVisitedCard == null){
				POI_id = new LinkedList<Integer>();        
				locations = new LinkedList<Location>();	
				arrivals = new LinkedList<Date>();            
				departures = new LinkedList<Date>();          
				days = new LinkedList<String>();
				rClient.getDataFromCache(false, false, 0, POI_id, locations, arrivals, departures, null);
					
				/*** Only create the card if the user has visited more than 2 places ***/
				/***********************************************************************/
				if ((new HashSet<Integer>(POI_id).size()) >= Constants.NO_OF_MIN_MOST_VISITED) {
					Constants.mostVisitedCard = new MostVisitedPlacesCard(context, POI_id, locations, arrivals, departures);
					Constants.mostVisitedCard.setId("most_visited_places");
					if (Constants.mostVisitedCard != null) { Constants.cards.add(Constants.mostVisitedCard); }
				}
			}
		}
						                                                           
        if (Constants.progressDialog != null) Constants.progressDialog.dismiss();
		Constants.newDataFetched = false;
	}
	
	// Method that animates the map camera to specific bounds
	public void animateToBounds(List<Location> locations, GoogleMap map, int locationLimit) {
		if(locations.size()==1) map.animateCamera(CameraUpdateFactory
								.newLatLngZoom(new LatLng(((Location) locations.get(0)).getLatitude(),
								((Location) locations.get(0)).getLongitude()), 15));
	    
	    else {
	    	// If the points are too close to each other, zoom to 15
	    	float maxDistance = 0;
		    for (int i=0; i<locations.size()-1; i++) {
		    	float distance = calculateHaversineDistance((Location) locations.get(i), (Location) locations.get(i+1));
		    	if (distance > maxDistance) maxDistance = distance;
		    }
		    
		    if (maxDistance < 1.0) { 		    	
		    	map.animateCamera(CameraUpdateFactory
					.newLatLngZoom(new LatLng(((Location) locations.get(0)).getLatitude(),
					((Location) locations.get(0)).getLongitude()), 15));		    
		    	return;
		    }
	    	
			// Build bounds for visible portion of the map
		    Builder boundsBuilder = new LatLngBounds.Builder();
		    for(int i=0; i<locationLimit; i++) {
		    	LatLng point = new LatLng(((Location) locations.get(i)).getLatitude(), ((Location) locations.get(i)).getLongitude());
		    	boundsBuilder.include(point);
		    }		    	    
		    LatLngBounds bounds = boundsBuilder.build();
		    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, metrics.widthPixels, metrics.heightPixels, 280));		    		    
	    }
	}
	
	// Method that fetches the next cards in the card feed when the user scrolls at the
	// end of the list. It uses a separate thread for creating the extra cards and notifies
	// the user using a "loading" footer in the card list
	@SuppressLint("SimpleDateFormat")
	public void fetchNextCards(final int dayNumber) {
				
		dayFound = false;
    	footerView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
											.inflate(R.layout.card_feed_footer_view, null, false);
		
		final Runnable updateFooter = new Runnable() {
			@Override
			public void run() {						    					
	        	Constants.mListView.addFooterView(footerView);
	        	
	        	// Only for compatibility with < 19 Level API
	        	if (android.os.Build.VERSION.SDK_INT < 19) {
		    		Constants.index = Constants.mListView.getFirstVisiblePosition();
		    		View v = Constants.mListView.getChildAt(0);
		    		Constants.top = (v == null) ? 0 : v.getTop();
		        	Constants.mListView.setAdapter(Constants.mCardArrayAdapter);
		        	Constants.mListView.setSelectionFromTop(Constants.index, Constants.top);
	        	}
			}
		};
		
		/** Source: http://mobile.dzone.com/news/android-tutorial-dynamicaly **/
		// This is run by the main (UI) thread due to the fact that it
		// updates the UI (only main thread can do that)
		final Runnable returnRes = new Runnable() {
			@Override
			public void run() {
				try { Constants.mListView.removeFooterView(footerView); }
				catch (Exception e) {}
				if ((!Constants.tmpWeekNumber.equals(Constants.FEED_WEEK)) && (!Constants.FEED_WEEK.equals(Constants.CURR_WEEK))) {
					// Change the current feed week number
					Constants.FEED_WEEK = Constants.tmpWeekNumber;
					Constants.FEED_YEAR = Constants.tmpYearNumber;
					Constants.cards.add(Constants.weeklyItCard);
				}
				Constants.cards.add(Constants.todaysItCard);
				((ArrayAdapter<Card>) Constants.mCardArrayAdapter).notifyDataSetChanged();
				Constants.feedLoading = false;
				
				if (Constants.FEED_WEEK.equals(Constants.CURR_WEEK)) Constants.FEED_WEEK = Constants.tmpWeekNumber;
			}
		};
		
		// This is run by a separate thread and is responsible for creating the new cards
		Runnable loadMoreListItems = new Runnable() {
			@Override
			public void run() {
				
				// Create mutual exclusion for the threads
				synchronized(this) {				
					Constants.feedLoading = true;   
					rClient.getDataFromCache(false, false, 0, POI_id, locations, arrivals, departures, days);
				   			
					if (days.size() > 0) {
					   	String dateString = "";					   
						tmpPOI_id = new LinkedList<Integer>();
						tmpItineraryLocations = new LinkedList<Location>();			
						tmpArrivals = new LinkedList<Date>();
						tmpDepartures = new LinkedList<Date>();
						int dayCount = 0;
						String day = days.get(0);
						for(int i=0; i<days.size(); i++) {
							if (!day.equals(days.get(i))) {
								dayCount++;
								day = days.get(i);
							}
							if (dayCount == dayNumber) {
								dayFound = true;
								dateString = day;
								
								Calendar cal = Calendar.getInstance();
								DateFormat formatter = new SimpleDateFormat("EEE MMM dd yyyy");
								Date date = null;
								try {
									date = formatter.parse(dateString);
								} catch (ParseException e1) {
									e1.printStackTrace();
								}
								cal.setTime(date);
								
								if (date.toString().substring(0, 3).equals("Sun")) {
									cal.add(Calendar.DAY_OF_YEAR, -1);
								}
								
								Constants.tmpWeekNumber = Integer.toString(cal.get(Calendar.WEEK_OF_YEAR));
								Constants.tmpYearNumber = Integer.toString(cal.get(Calendar.YEAR));
								tmpPOI_id.add(POI_id.get(i));
								tmpItineraryLocations.add(locations.get(i));
								tmpArrivals.add(arrivals.get(i));
								tmpDepartures.add(departures.get(i));							
								if (!Constants.tmpWeekNumber.equals(Constants.CURR_WEEK)) {
									// Also add the data to the weekly lists
									Constants.weeklyPOI_ids.add(POI_id.get(i));
									Constants.weeklyLocations.add(locations.get(i));
									Constants.weeklyArrivals.add(arrivals.get(i));
									Constants.weeklyDepartures.add(departures.get(i));
								}
							}
						}
						
						if (dayFound == true) {
						   // If any of the below lists is empty, the card won't be created
						   if((tmpPOI_id.size()!=0) ||
						   	(tmpItineraryLocations.size()!=0) ||	
						   	(tmpArrivals.size()!=0) ||			    		
						   	(tmpDepartures.size()!=0)) {
						   				    
							   	// If the "loading" footer is not already there, place it
							   	if (Constants.mListView.getFooterViewsCount() == 0)
							   		((Activity) context).runOnUiThread(updateFooter);
						   				    							
								// Sleep for 1 sec (avoid Google's limit)
								try { Thread.sleep(1100); }
						        catch (InterruptedException e) { e.printStackTrace(); }
								// Reverse the lists in order to achieve chronological sequence of the events
								Collections.reverse(tmpPOI_id);
								Collections.reverse(tmpItineraryLocations);
								Collections.reverse(tmpArrivals);
								Collections.reverse(tmpDepartures);
								Constants.todaysItCard  = new TodaysItineraryCard(context, dateString, tmpPOI_id, 
																tmpItineraryLocations, tmpArrivals, tmpDepartures, true);
								Constants.todaysItCard.setId("todays_itinerary");
								
								List<Integer> indices = new LinkedList<Integer>();					
								
								// If the feed week has changed, also create the weeks summary card
								if ((!Constants.tmpWeekNumber.equals(Constants.FEED_WEEK)) && (!Constants.FEED_WEEK.equals(Constants.CURR_WEEK))) {						
									
									/****** Remove consequent duplicates from the lists *******/
									LinkedList<Integer> resultPOI_id = new LinkedList<Integer>();
									LinkedList<Location> resulLocations = new LinkedList<Location>();			
									LinkedList<Date> resulArrivals = new LinkedList<Date>();
									LinkedList<Date> resulDepartures = new LinkedList<Date>();
									
									for (int i=0; i<tmpPOI_id.size(); i++) {
										Constants.weeklyPOI_ids.removeLast();     
										Constants.weeklyLocations.removeLast();
										Constants.weeklyArrivals.removeLast();   
										Constants.weeklyDepartures.removeLast(); 
									}
									
									for (int i=1; i<Constants.weeklyPOI_ids.size(); i++) {
										if ( (Constants.weeklyLocations.get(i).getLatitude() == Constants.weeklyLocations.get(i-1).getLatitude()) &&
											 (Constants.weeklyLocations.get(i).getLongitude() == Constants.weeklyLocations.get(i-1).getLongitude()) )
											indices.add(i);
									}				
									
									for (int i=0; i<Constants.weeklyPOI_ids.size(); i++) {
										if ( indices.contains(i) ) continue;
										else {
											resultPOI_id.add(Constants.weeklyPOI_ids.get(i));
											resulLocations.add(Constants.weeklyLocations.get(i));
											resulArrivals.add(Constants.weeklyArrivals.get(i));
											resulDepartures.add(Constants.weeklyDepartures.get(i));
										}
									}
									/***********************************************************/
									
									// Sleep for 1 sec (avoid Google's limit)
									try { Thread.sleep(1100); }
							        catch (InterruptedException e) { e.printStackTrace(); }
												
									Collections.reverse(resultPOI_id);
									Collections.reverse(resulLocations);
									Collections.reverse(resulArrivals);
									Collections.reverse(resulDepartures);
									Constants.weeklyItCard = new WeeklyItineraryCard(context, Constants.FEED_WEEK, Constants.FEED_YEAR, resultPOI_id, 
																							resulLocations, resulArrivals, resulDepartures, true);
									Constants.weeklyItCard.setId("weekly_itinerary");				
									
									Constants.weeklyPOI_ids = new LinkedList<Integer>();
									Constants.weeklyLocations = new LinkedList<Location>();
									Constants.weeklyArrivals = new LinkedList<Date>();
									Constants.weeklyDepartures = new LinkedList<Date>();
									
									for (int i=0; i<tmpPOI_id.size(); i++) {
										Constants.weeklyPOI_ids.add(tmpPOI_id.get(i));        
										Constants.weeklyLocations.add(tmpItineraryLocations.get(i));	
										Constants.weeklyArrivals.add(tmpArrivals.get(i));            
										Constants.weeklyDepartures.add(tmpDepartures.get(i));
									}
									Collections.reverse(Constants.weeklyPOI_ids);
									Collections.reverse(Constants.weeklyLocations);
									Collections.reverse(Constants.weeklyArrivals);
									Collections.reverse(Constants.weeklyDepartures);
								}
						   	}						   
						   	((Activity) context).runOnUiThread(returnRes);
						}
						else Constants.feedLoading = false;						
					}
					else Constants.feedLoading = false;	
				}
			}
		};								

		// The thread is started here
		Thread thread =  new Thread(null, loadMoreListItems);
		thread.start();
	}
	
	
	// TODO Temporary method to reverse the entries in a csv file
	public void reverseCSV() {		
		AssetManager assetManager = context.getAssets();		
		InputStreamReader is = null;
		try {
			is = new InputStreamReader(assetManager.open("user_movements.csv"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		String line;
		List<String> lines = new LinkedList<String>();
		BufferedReader reader = new BufferedReader(is);	
	    try {
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}		
	        		
	    Collections.reverse(lines);
	    
	    File sdCard = Environment.getExternalStorageDirectory();
	    File dir = new File (sdCard.getAbsolutePath());
	    File file = new File(dir, "user_movements_new.csv");
	    if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	    
        PrintWriter writer = null;
		try {
			writer = new PrintWriter(file.getAbsoluteFile(), "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		for (int i=0; i<lines.size(); i++) {
			writer.println(lines.get(i));
		}
        
        writer.close();
	}
	
	// Method that creates the application's about dialog
	public void showAboutDialog() {
	     // Inflate the about message contents
        View messageView = ((Activity) context).getLayoutInflater().inflate(R.layout.about_dialog, null, false);
        AlertDialog.Builder builder = new AlertDialog.Builder((Activity) context);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.create();
        builder.show();
	}
	
	public String getLogData() {
		
		LogDbHelper logDbHelper = new LogDbHelper(context);
		
		// Gets the data repository in read mode
		SQLiteDatabase db = logDbHelper.getReadableDatabase();
		
		// 1. Get the total time in application
		String query = "SELECT * FROM application_log";
								
		Cursor c = db.rawQuery(query, null);
		c.moveToFirst();		
		String user_id = "";
		List<String> event = new LinkedList<String>();
		List<Float> timestamp = new LinkedList<Float>();
		long totalTimeInApp = 0;
		long totalTimeInWeeklyArchive = 0;
		long totalTimeInDailyArchive = 0;
		long totalTimeInCurrentLoc = 0;
		long totalTimeInLastPlace = 0;
		long totalTimeInLatestJourney = 0;
		long totalTimeInDailyItin = 0;
		long totalTimeInWeeklyItin = 0;
		long totalTimeInMostVisited = 0;
		int clicksForWeeklyArchive = 0;
		int clicksForDailyArchive = 0;
		int clicksForCurrentLoc = 0;
		int clicksForLastPlace = 0;
		int clicksForLatestJourney = 0;
		int clicksForDailyItin = 0;
		int clicksForWeeklyItin = 0;
		int clicksForMostVisited = 0;
		int likesForCurrentLoc = 0;
		int likesForLastPlace = 0;
		int likesForLatestJourney = 0;
		int likesForDailyItin = 0;
		int likesForWeeklyItin = 0;
		int likesForMostVisited = 0;
		String finalTotalTimeInApp = "";
		String finalTotalTimeInWeeklyArchive = "";
		String finalTotalTimeInDailyArchive = "";
		String finalTotalTimeInCurrentLoc = "";
		String finalTotalTimeInLastPlace = "";
		String finalTotalTimeInLatestJourney = "";
		String finalTotalTimeInDailyItin = "";
		String finalTotalTimeInWeeklyItin = "";
		String finalTotalTimeInMostVisited = "";
				
		while (!c.isAfterLast()) {
			user_id = c.getString(1);
			event.add(c.getString(2));
			timestamp.add(c.getFloat(3));
			c.moveToNext();
		}
				
		
		for (int i=0; i<timestamp.size(); i++) {			
						
			if (event.get(i).equals("TIME_IN_WEEK_ARCHIVE")) {
				clicksForWeeklyArchive++;
				if (timestamp.get(i) < 10000000) {
					totalTimeInApp += timestamp.get(i);
					totalTimeInWeeklyArchive += timestamp.get(i);
				}
			}
			else if (event.get(i).equals("TIME_IN_DAY_ARCHIVE")) {
				clicksForDailyArchive++;
				if (timestamp.get(i) < 10000000) {
					totalTimeInApp += timestamp.get(i);
					totalTimeInDailyArchive += timestamp.get(i);
				}
			}
			else if (event.get(i).equals("TIME_IN_CURRENT_LOC")) {
				clicksForCurrentLoc++;
				if (timestamp.get(i) < 10000000) {
					totalTimeInApp += timestamp.get(i);
					totalTimeInCurrentLoc += timestamp.get(i);
				}
			}
			else if (event.get(i).equals("TIME_IN_LAST_PLACE")) {
				clicksForLastPlace++;
				if (timestamp.get(i) < 10000000) {
					totalTimeInApp += timestamp.get(i);
					totalTimeInLastPlace += timestamp.get(i);
				}
			}
			else if (event.get(i).equals("TIME_IN_LATEST_JOURNEY")) {
				clicksForLatestJourney++;
				if (timestamp.get(i) < 10000000) {
					totalTimeInApp += timestamp.get(i);
					totalTimeInLatestJourney += timestamp.get(i);
				}
			}
			else if (event.get(i).equals("TIME_IN_DAILY_ITIN")) {
				clicksForDailyItin++;
				if (timestamp.get(i) < 10000000) {
					totalTimeInApp += timestamp.get(i);
					totalTimeInDailyItin += timestamp.get(i);
				}
			}
			else if (event.get(i).equals("TIME_IN_WEEKLY_ITIN")) {
				clicksForWeeklyItin++;
				if (timestamp.get(i) < 10000000) {
					totalTimeInApp += timestamp.get(i);
					totalTimeInWeeklyItin += timestamp.get(i);
				}
			}
			else if (event.get(i).equals("TIME_IN_MOST_VISITED")) {
				clicksForMostVisited++;
				if (timestamp.get(i) < 10000000) {
					totalTimeInApp += timestamp.get(i);
					totalTimeInMostVisited += timestamp.get(i);
				}
			}
			else if (event.get(i).equals("LIKED_CURRENT_LOC")) likesForCurrentLoc++;
			else if (event.get(i).equals("LIKED_LAST_PLACE")) likesForLastPlace++;
			else if (event.get(i).equals("LIKED_LATEST_JOURNEY")) likesForLatestJourney++;
			else if (event.get(i).equals("LIKED_DAILY_ITIN")) likesForDailyItin++;
			else if (event.get(i).equals("LIKED_WEEKLY_ITIN")) likesForWeeklyItin++;
			else if (event.get(i).equals("LIKED_MOST_VISITED")) likesForMostVisited++;
			
		}
		
		finalTotalTimeInApp = getHoursMinutes(totalTimeInApp/1000, true);
		finalTotalTimeInWeeklyArchive = getHoursMinutes(totalTimeInWeeklyArchive/1000, true);
		finalTotalTimeInDailyArchive = getHoursMinutes(totalTimeInDailyArchive/1000, true);
		finalTotalTimeInCurrentLoc = getHoursMinutes(totalTimeInCurrentLoc/1000, true);
		finalTotalTimeInLastPlace = getHoursMinutes(totalTimeInLastPlace/1000, true);
		finalTotalTimeInLatestJourney = getHoursMinutes(totalTimeInLatestJourney/1000, true);
		finalTotalTimeInDailyItin = getHoursMinutes(totalTimeInDailyItin/1000, true);
		finalTotalTimeInWeeklyItin = getHoursMinutes(totalTimeInWeeklyItin/1000, true);
		finalTotalTimeInMostVisited = getHoursMinutes(totalTimeInMostVisited/1000, true);
		
		String result = "######################## USAGE LOG ##########################" + "\n"
				+ "User ID: " + user_id + "\n"
				+ "Total time in app: " + finalTotalTimeInApp + "\n"
				+ "Total time in Weekly Itinerary Archive: " + finalTotalTimeInWeeklyArchive + "\n"
				+ "Total time in Daily Itinerary Archive: " + finalTotalTimeInDailyArchive + "\n"
				+ "Total time in Current Location detailed view: " + finalTotalTimeInCurrentLoc + "\n"
				+ "Total time in Last Place Visited detailed view: " + finalTotalTimeInLastPlace + "\n"
				+ "Total time in Latest Journey detailed view: " + finalTotalTimeInLatestJourney + "\n"
				+ "Total time in Daily Itinerary detailed view: " + finalTotalTimeInDailyItin + "\n"
				+ "Total time in Weekly Itinerary detailed view: " + finalTotalTimeInWeeklyItin + "\n"
				+ "Total time in Most Visited Places detailed view: " + finalTotalTimeInMostVisited + "\n"
				+ "Number of times in Weekly Itinerary archive: " + clicksForWeeklyArchive + "\n"
				+ "Number of times in Daily Itinerary archive: " + clicksForDailyArchive + "\n"
				+ "Number of times in Current Location detailed view: " + clicksForCurrentLoc + "\n"
				+ "Number of times in Last Place Visited detailed view: " + clicksForLastPlace + "\n"
				+ "Number of times in Latest Journey detailed view: " + clicksForLatestJourney + "\n"
				+ "Number of times in Daily Itinerary detailed view: " + clicksForDailyItin + "\n"
				+ "Number of times in Weekly Itinerary detailed view: " + clicksForWeeklyItin + "\n"
				+ "Number of times in Most Visited Places detailed view: " + clicksForMostVisited + "\n"
				+ "Current Location card thumbs up: " + likesForCurrentLoc + "\n"
				+ "Last Place Visited card thumbs up: " + likesForLastPlace + "\n"
				+ "Latest Journey card thumbs up: " + likesForLatestJourney + "\n"
				+ "Daily Itinerary card thumbs up: " + likesForDailyItin + "\n"
				+ "Weekly Itinerary card thumbs up: " + likesForWeeklyItin + "\n"
				+ "Most Visited Places card thumbs up: " + likesForMostVisited + "\n"
				+ "#############################################################";
		
		c.close();
		db.close();
		return result;		
	}
	
	public void setupAwesomeButton(View view, final LinearLayout awesomeLayout, final TextView awesomeTextView, final boolean[] awesomeClicked, final int type) {
		
		if (awesomeClicked[0] == false) {	        
	        awesomeLayout.setBackgroundResource(R.color.awesome_unclicked);
	        awesomeLayout.setClickable(true);		
			awesomeTextView.setText(context.getString(R.string.awesome));
			awesomeTextView.setTextColor(context.getResources().getColor(R.color.white_font));
			awesomeTextView.setTextSize(14);
			awesomeTextView.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.awesome), null, null, null);
        }
        else {       	
        	awesomeLayout.setBackgroundResource(R.color.semi_transparent_theme_color);
        	awesomeLayout.setClickable(false);      				
        	awesomeTextView.setText("Thank you for your feedback!");		
			awesomeTextView.setTextColor(context.getResources().getColor(R.color.white_font));
			awesomeTextView.setTextSize(14);
			awesomeTextView.setCompoundDrawables(null, null, null, null);
        }
		
		awesomeLayout.setOnClickListener(new LinearLayout.OnClickListener() {
		    public void onClick(View v) {
		    	awesomeLayout.setBackgroundResource(R.color.semi_transparent_theme_color);		    	
				awesomeLayout.setClickable(false);
				awesomeTextView.setText("Thank you for your feedback!");
				awesomeTextView.setCompoundDrawables(null, null, null, null);
				
				LogDbHelper logDbHelper = new LogDbHelper(context);
				long awesome_timestamp = System.currentTimeMillis();
				if (type == 0) logDbHelper.log(Constants.logComponents.AWESOME_MOST_VISITED, awesome_timestamp);
				else if (type == 1) logDbHelper.log(Constants.logComponents.AWESOME_CURRENT_LOC, awesome_timestamp);
				else if (type == 2) logDbHelper.log(Constants.logComponents.AWESOME_DAILY_ITIN, awesome_timestamp);
				else if (type == 3) logDbHelper.log(Constants.logComponents.AWESOME_LAST_PLACE, awesome_timestamp);
				else if (type == 4) logDbHelper.log(Constants.logComponents.AWESOME_LATEST_JOURNEY, awesome_timestamp);
				else if (type == 5) logDbHelper.log(Constants.logComponents.AWESOME_WEEKLY_ITIN, awesome_timestamp);
				awesomeClicked[0] = true;
		    }
		});
	}
	
	public void setAlarms(Context context) {
		Calendar calendar = Calendar.getInstance();
		
		// Set repeating alarm for notifying the user
		calendar.set(Calendar.HOUR_OF_DAY, 12);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);			
		Intent notification = new Intent(context, NotificationReceiver.class);		
	    notificationPendingIntent = PendingIntent.getBroadcast(context, 0, notification, PendingIntent.FLAG_CANCEL_CURRENT);
	    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);			    			
		alarmManager.cancel(notificationPendingIntent);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()+AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, notificationPendingIntent);
		//alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 10000, 10000, notificationPendingIntent);
		
		// Set repeating alarm for fetching data from server
		int hours = 10 + (int)(Math.random() * ((12 - 10) + 1));
		int minutes = 0 + (int)(Math.random() * ((60 - 0) + 1));
		calendar.set(Calendar.HOUR_OF_DAY, hours);
		calendar.set(Calendar.MINUTE, minutes);
		calendar.set(Calendar.SECOND, 0);
		Intent dataFetch = new Intent(context, DataFetchReceiver.class);		
		fetchingPendingIntent = PendingIntent.getBroadcast(context, 0, dataFetch, PendingIntent.FLAG_CANCEL_CURRENT);		    			
		alarmManager.cancel(fetchingPendingIntent);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()+AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, fetchingPendingIntent);
	}
}