package dk.dtu.imm.sensible.cards.basic;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.component.CardThumbnailView;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import dk.dtu.imm.sensible.R;
import dk.dtu.imm.sensible.cards.tools.CustomHeader;
import dk.dtu.imm.sensible.cards.tools.CustomThumbCard;
import dk.dtu.imm.sensible.detailedViews.MostVisitedDetailedView;
import dk.dtu.imm.sensible.utilities.Constants;
import dk.dtu.imm.sensible.utilities.AppFunctions;

/** MOST VISITED PLACES CARD **/
public class MostVisitedPlacesCard extends Card{

    private TextView text1;
    private TextView text2;
    private TextView text3;
    private TextView text4;
    private TextView text5;
    private TextView text6;
    private Context context;
    
    private LinkedList<Integer> POI_id;
    private LinkedList<Location> locations;			
    private LinkedList<Date> arrivals;
    private LinkedList<Date> departures;
    private DisplayMetrics metrics;
    private AppFunctions functions;
    
    private String[] final_addresses;  	
    private String[] final_durations;
    private LinkedList<Location> final_locations;     
    
    @SuppressLint("UseSparseArrays")
	private Map<Integer, Long> POI_durations = new HashMap<Integer, Long>();
    @SuppressLint("UseSparseArrays")
	private Map<Integer, Location> POI_locations = new HashMap<Integer, Location>();
    private Geocoder geocoder;
    private CustomThumbCard thumbnail;
    private LinearLayout awesomeLayout;
    private TextView awesomeTextView;
    private boolean[] awesomeClicked = {false};

    public MostVisitedPlacesCard(Context context, LinkedList<Integer> POI_id,
    		LinkedList<Location> locations, LinkedList<Date> arrivals, LinkedList<Date> departures) {
        super(context, R.layout.most_visited_places_card_content);
        this.context = context;
        this.metrics = context.getResources().getDisplayMetrics();
        this.POI_id = POI_id;
        this.locations = locations;
        this.arrivals = arrivals;
        this.departures = departures;
        functions = new AppFunctions(context);
        init();
    }

    private void init() {
    	  
    	if (locations.size() > 0) {
	    	// Get the POIs, the total time spent at them and their actual location
	    	Long tmpEpoch;    	
	        for(int i=0; i<POI_id.size(); i++) {
	        	if (POI_durations.containsKey(POI_id.get(i))) {
	        		tmpEpoch = POI_durations.get(POI_id.get(i));
	        		tmpEpoch += (departures.get(i).getTime()/1000) - (arrivals.get(i).getTime()/1000);
	        		POI_durations.put(POI_id.get(i), tmpEpoch);
	        	}
	        	else {
	        		POI_durations.put(POI_id.get(i), 
	        				(departures.get(i).getTime()/1000) - (arrivals.get(i).getTime()/1000));
	        		POI_locations.put(POI_id.get(i), locations.get(i));
	        	}
	        }
	        		 
			Long[] durations = (Long[]) POI_durations.values()
											.toArray(new Long[POI_durations.values().size()]);
			Integer[] POIs = (Integer[]) POI_durations.keySet()	
											.toArray(new Integer[POI_durations.keySet().size()]);
			
	        for(int i=0; i<POIs.length-1; i++) {
	        	for(int j=0; j<POIs.length-i-1; j++) {
	        		
		        	if(durations[j+1] > durations[j]) {
		        		Long dur = durations[j];
		        		int poi = POIs[j];        		
		        		durations[j] = durations[j+1];
		        		POIs[j] = POIs[j+1];
		        		durations[j+1] = dur;
		        		POIs[j+1] = poi;
		        	}
		        	
	        	}
	        }      
	        
	    	final_addresses = new String[Constants.NO_OF_MIN_MOST_VISITED];
	        final_durations = new String[durations.length];
	        final_locations = new LinkedList<Location>();        
	    	geocoder = new Geocoder(context, Locale.getDefault());
	    	
	        // Only the 3 addresses for this card need to be fetched here
	        for(int i=0; i<Constants.NO_OF_MIN_MOST_VISITED; i++) {
	        	try {
	        		final_addresses[i] = geocoder.getFromLocation(POI_locations.get(POIs[i]).getLatitude(),
		        						POI_locations.get(POIs[i]).getLongitude(), 1).get(0).getAddressLine(0);
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
	        
	        // The rest will be also sent to detailed view at their entirety
	        for(int i=0; i<POIs.length; i++) {
	        	final_locations.add(POI_locations.get(POIs[i]));        	        	        	        	
	        	final_durations[i] = functions.getDaysHoursMinutes(durations[i]);        	        
	        }
	            	
	        //Create a CardHeader
	    	CustomHeader header = new CustomHeader(context, getType());
	        addCardHeader(header);          
	
	        //Add ClickListener
	        setOnClickListener(new OnCardClickListener() {
				@Override
				public void onClick(Card card, View view) {
					Intent intent = new Intent(context, MostVisitedDetailedView.class);
	                intent.putExtra(Constants.MOST_VISITED_POIs, final_addresses);
	                intent.putExtra(Constants.MOST_VISITED_POIs_DURATIONS, final_durations);
	                intent.putExtra(Constants.MOST_VISITED_POIs_LOCATIONS, final_locations);
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
	        	        
			thumb_resource += "&markers=color:green|"
							+ final_locations.get(0).getLatitude() + ","
							+ final_locations.get(0).getLongitude();
			       
			thumb_resource += "&markers=color:0x29A3CC|" 
							+ final_locations.get(1).getLatitude() + ","
							+ final_locations.get(1).getLongitude();    	
				
			thumb_resource += "&markers=color:red|"
							+ final_locations.get(2).getLatitude() + "," 
							+ final_locations.get(2).getLongitude();	        
	        
	        thumbnail.setUrlResource(thumb_resource);
    	}
    }


    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
    	
        CardThumbnailView cardThumb = (CardThumbnailView) parent.findViewById(R.id.card_thumbnail_layout);
        cardThumb.addCardThumbnail(thumbnail);
        
        awesomeLayout = (LinearLayout) view.findViewById(R.id.awesome_layout);
        awesomeTextView = (TextView) view.findViewById(R.id.awesome);
        functions.setupAwesomeButton(view, awesomeLayout, awesomeTextView, awesomeClicked, getType());

    	if (final_addresses.length > 0) {
	        //Retrieve elements
	    	text1 = (TextView) parent.findViewById(R.id.place1);
	        if (text1 != null)
	        	text1.setText(final_addresses[0]);
	                    
	        text2 = (TextView) parent.findViewById(R.id.place2);
	        if (text2 != null)
	        	text2.setText(final_addresses[1]);
	        
	        text3 = (TextView) parent.findViewById(R.id.place3);
	        if (text3 != null)
	        	text3.setText(final_addresses[2]);
	        
	        text4 = (TextView) parent.findViewById(R.id.time1);
	        if (text4 != null)
	        	text4.setText(final_durations[0]);
	        
	        text5 = (TextView) parent.findViewById(R.id.time2);
	        if (text5 != null)
	        	text5.setText(final_durations[1]);
	        
	        text6 = (TextView) parent.findViewById(R.id.time3);
	        if (text6 != null)
	        	text6.setText(final_durations[2]);
    	}
    }

    @Override
    public int getType() {
        //Very important with different inner layouts
        return 0;
    }
}
