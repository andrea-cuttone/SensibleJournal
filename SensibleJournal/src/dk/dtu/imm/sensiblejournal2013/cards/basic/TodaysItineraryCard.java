package dk.dtu.imm.sensiblejournal2013.cards.basic;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.component.CardThumbnailView;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.json.JSONException;

import dk.dtu.imm.sensiblejournal2013.R;
import dk.dtu.imm.sensiblejournal2013.cards.tools.CustomHeader;
import dk.dtu.imm.sensiblejournal2013.cards.tools.CustomThumbCard;
import dk.dtu.imm.sensiblejournal2013.detailedViews.TodaysItineraryDetailedView;
import dk.dtu.imm.sensiblejournal2013.utilities.Constants;
import dk.dtu.imm.sensiblejournal2013.utilities.AppFunctions;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/** TODAY'S ITINERARY CARD **/
public class TodaysItineraryCard extends Card{
	
	private TextView total_distance_text;
	private TextView total_distance;
	private TextView no_of_unique_places_text;
	private TextView no_of_unique_places;
	private TextView no_of_stops_text;
	private TextView no_of_stops;
    private int noOfUniquePlaces;
	private int noOfStops;
    private float total_distance_travelled;
    private DisplayMetrics metrics;
    private Context context;
    private AppFunctions functions;
    private String date;
    private LinkedList<Integer> POI_id = new LinkedList<Integer>();
    private LinkedList<Location> itineraryLocations = new LinkedList<Location>();			
    private LinkedList<Date> arrivals = new LinkedList<Date>();
    private LinkedList<Date> departures = new LinkedList<Date>();
    private boolean archived;
    private CustomThumbCard thumbnail;
    private Button awesomeButton;
    private boolean[] awesomeClicked = {false};

    public TodaysItineraryCard(Context context, String date, LinkedList<Integer> POI_id, LinkedList<Location> itineraryLocations,
    							LinkedList<Date> arrivals, LinkedList<Date> departures, boolean archived) {
    	
        super(context, R.layout.todays_itinerary_card_content);
        this.context = context;
        this.metrics = context.getResources().getDisplayMetrics();
        this.functions = new AppFunctions(context);
        this.date = date;
        this.POI_id = POI_id;
        this.itineraryLocations = itineraryLocations;
        this.arrivals = arrivals;
        this.departures = departures;
        this.archived = archived;
            	
        try {
			init();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }

    private synchronized void init() throws IOException, JSONException{        	    	            
    	
        //Create a CardHeader
    	CustomHeader header = new CustomHeader(context, getType(), date, archived);
        addCardHeader(header);                    

        //Add ClickListener
        setOnClickListener(new OnCardClickListener() {
			@Override
            public void onClick(Card card, View view) {           	
            	Intent intent = new Intent(context, TodaysItineraryDetailedView.class);
            	intent.putExtra(Constants.STOP_LOCATIONS, itineraryLocations);                    
            	intent.putExtra(Constants.STOP_LOCATIONS_ARRIVALS, arrivals);
            	intent.putExtra(Constants.STOP_LOCATIONS_DEPARTURES, departures);
            	intent.putExtra(Constants.POIs, POI_id);
            	intent.putExtra(Constants.SELECTED_DAY, date);
            	Constants.paused = false;
            	context.startActivity(intent);
            }
        });            
                  
        /** MAP THUMBNAIL CREATION **/
        /****************************/     
        thumbnail = new CustomThumbCard (context);                   	 	       	 	                   	    
        String thumb_resource = "http://maps.googleapis.com/maps/api/staticmap?&size=" 
        							+ (int) (Constants.THUMB_WIDTH/2 - (10*metrics.density))
        							+ "x" + Constants.THUMB_HEIGHT/2 + "&maptype=roadmap"
        							+ "&sensor=false&scale=2&visual_refresh=true";
        
        if (itineraryLocations.size() > 0) {
	        thumb_resource += "&markers=color:green%7Clabel:S%7C"
	        					+ itineraryLocations.get(0).getLatitude() + ","
	        					+ itineraryLocations.get(0).getLongitude();
	        
	        for(int i=1; i<POI_id.size()-1; i++){
	        	thumb_resource += "&markers=color:0x29A3CC|" 
	        					+ itineraryLocations.get(i).getLatitude() + ","
	        					+ itineraryLocations.get(i).getLongitude();    	
	        }
	        thumb_resource += "&markers=color:red%7Clabel:F%7C"
	        					+ itineraryLocations.get(POI_id.size()-1).getLatitude() + "," 
	        					+ itineraryLocations.get(POI_id.size()-1).getLongitude();
	        
	        thumb_resource += "&path=color:0x29A3CC|weight:4";
	        for(int i=0; i<POI_id.size(); i++){
	        	thumb_resource += "|" + itineraryLocations.get(i).getLatitude() + ","
	        								+ itineraryLocations.get(i).getLongitude();    	
	        }
                
	        thumbnail.setUrlResource(thumb_resource);
			            
	        // Determine the number of stops and...
	        if (POI_id.size()>= 2) noOfStops = POI_id.size()-2;
	        else noOfStops = POI_id.size();
		    // ...the number of unique visited places
		    Set<Integer> tmpSet = new HashSet<Integer>(POI_id);
		    noOfUniquePlaces = tmpSet.size();
	        // Travel's distance calculation
	        total_distance_travelled = functions.calculateTripDetails(itineraryLocations, arrivals, departures).getTotalDistance();

        }
    }
    
    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {    	    	
    	
    	setBackgroundResourceId(R.color.main_card_color);
    	
    	CardThumbnailView cardThumb = (CardThumbnailView) parent.findViewById(R.id.card_thumbnail_layout);
        cardThumb.addCardThumbnail(thumbnail);
        
        awesomeButton = (Button) view.findViewById(R.id.awesome);
        functions.setupAwesomeButton(view, awesomeButton, awesomeClicked, getType());
    	
    	//Retrieve elements
    	total_distance_text = (TextView) parent.findViewById(R.id.total_distance_title);
    	if (total_distance_text != null)
    		total_distance_text.setText(R.string.total_distance_text);
    	            
    	total_distance = (TextView) parent.findViewById(R.id.distance);
    	if (total_distance != null) {
    	    DecimalFormat twoDForm = new DecimalFormat("#.#");
    	    total_distance_travelled = Float.valueOf(twoDForm.format(total_distance_travelled));
    		total_distance.setText(Float.toString(total_distance_travelled) + " Km");
    	}
    		
    	no_of_unique_places_text = (TextView) parent.findViewById(R.id.no_of_unique_places_title);
    	if (no_of_unique_places_text != null)
    		no_of_unique_places_text.setText(R.string.no_of_unique_places_text);
    		
    	no_of_unique_places = (TextView) parent.findViewById(R.id.no_of_unique_places);
    	if (no_of_unique_places != null)
    		no_of_unique_places.setText(Integer.toString(noOfUniquePlaces)); 
    		
    	no_of_stops_text = (TextView) parent.findViewById(R.id.no_of_stops_title);
    	if (no_of_stops_text != null)
    		no_of_stops_text.setText(R.string.no_of_stops_text);
    		
    	no_of_stops = (TextView) parent.findViewById(R.id.no_of_stops);
    	if (no_of_stops != null)
    		no_of_stops.setText(Integer.toString(noOfStops));           
        
    }

    @Override
    public int getType() {
        //Very important with different inner layouts
        return 2;
    }
}
