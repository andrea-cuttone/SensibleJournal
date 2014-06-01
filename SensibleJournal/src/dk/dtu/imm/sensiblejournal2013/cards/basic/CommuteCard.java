package dk.dtu.imm.sensible.cards.basic;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.component.CardThumbnailView;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import dk.dtu.imm.sensible.R;
import dk.dtu.imm.sensible.cards.tools.CustomHeader;
import dk.dtu.imm.sensible.cards.tools.CustomThumbCard;
import dk.dtu.imm.sensible.detailedViews.CommuteDetailedView;
import dk.dtu.imm.sensible.utilities.Constants;
import dk.dtu.imm.sensible.utilities.AppFunctions;
import dk.dtu.imm.sensible.utilities.TripDetails;

/** COMMUTE (LATEST JOURNEY) CARD **/
public class CommuteCard extends Card {
	
	private TextView route_from;
	private TextView route_to;
	private TextView route_from_address;
	private TextView route_to_address;
	private TextView the_speed;
    private TextView the_distance;
	private TextView speed;
    private TextView distance;
    private ImageView meansOfTransp;
    private TripDetails tripDetails;
    private Context context;
    private AppFunctions functions;
    private DisplayMetrics metrics;
    private List<String> addresses;
    private LinkedList<Location> locations = new LinkedList<Location>();			
    private LinkedList<Date> arrivals = new LinkedList<Date>();
    private LinkedList<Date> departures = new LinkedList<Date>();
    private CustomThumbCard thumbnail;
    private LinearLayout awesomeLayout;
    private TextView awesomeTextView;
    private boolean[] awesomeClicked = {false};


    public CommuteCard(Context context, LinkedList<Location> locations,
						LinkedList<Date> arrivals, LinkedList<Date> departures) {
        super(context, R.layout.commute_card_content);
        this.context = context;
        this.metrics = context.getResources().getDisplayMetrics();
        this.functions = new AppFunctions(context);
        this.locations = locations;
        this.arrivals = arrivals;
        this.departures = departures;
        this.addresses = new  LinkedList<String>();
        
        try {
			init();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }

	private void init() throws IOException, JSONException{        	       
        //Create a CardHeader
    	CustomHeader header = new CustomHeader(context, getType());
        addCardHeader(header);
        
        //Add ClickListener
        setOnClickListener(new OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {           	
            	Intent intent = new Intent(context, CommuteDetailedView.class);
            	intent.putExtra(Constants.STOP_LOCATIONS, locations);                    
            	intent.putExtra(Constants.STOP_LOCATIONS_ARRIVALS, arrivals);
            	intent.putExtra(Constants.STOP_LOCATIONS_DEPARTURES, departures);
            	intent.putExtra(Constants.DISTANCE, tripDetails.getTotalDistance());                    
            	intent.putExtra(Constants.SPEED, tripDetails.getSpeed(0));
            	intent.putExtra(Constants.VEHICLE, tripDetails.getVehicle(0));
            	context.startActivity(intent);
            }
        });            
                               
        thumbnail = new CustomThumbCard(getContext());
            	
        String thumb_resource = "http://maps.googleapis.com/maps/api/staticmap?&size=" 
				+ (int) (Constants.THUMB_WIDTH/2 - (10*metrics.density))
				+ "x" + Constants.THUMB_HEIGHT/2 + "&maptype=roadmap"
				+ "&sensor=false&scale=2&visual_refresh=true";
        
        if (locations.size() > 0) {
	        thumb_resource += "&markers=color:green%7Clabel:S%7C"
					+ locations.get(0).getLatitude() + ","
					+ locations.get(0).getLongitude();
	    	
	    	thumb_resource += "&markers=color:red%7Clabel:F%7C"
					+ locations.get(1).getLatitude() + ","
					+ locations.get(1).getLongitude();
	    	
	    	thumb_resource += "&path=color:0x29A3CC|weight:4";
	    	
	    	thumb_resource += "|" + locations.get(0).getLatitude() + ","
					+ locations.get(0).getLongitude();
	    	
	    	thumb_resource += "|" + locations.get(1).getLatitude() + ","
					+ locations.get(1).getLongitude();        
    	
	        thumbnail.setUrlResource(thumb_resource);
	                
	        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
	        try {
				addresses.add(0, geocoder.getFromLocation(locations.get(0).getLatitude(),
							locations.get(0).getLongitude(), 1).get(0).getAddressLine(0));
				addresses.add(1, geocoder.getFromLocation(locations.get(1).getLatitude(),
							locations.get(1).getLongitude(), 1).get(0).getAddressLine(0));
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
	        
	        tripDetails = functions.calculateTripDetails(locations, arrivals, departures);
        }
    }
    
    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
    	
    	setBackgroundResourceId(R.color.main_card_color);
    	
    	CardThumbnailView cardThumb = (CardThumbnailView) parent.findViewById(R.id.card_thumbnail_layout);
        cardThumb.addCardThumbnail(thumbnail);
        
        awesomeLayout = (LinearLayout) view.findViewById(R.id.awesome_layout);
        awesomeTextView = (TextView) view.findViewById(R.id.awesome);
        functions.setupAwesomeButton(view, awesomeLayout, awesomeTextView, awesomeClicked, getType());    	                        
        
    	if (addresses.size() > 0) {
	    	route_from = (TextView) parent.findViewById(R.id.route_from);
	    	if (route_from != null && addresses != null)
	    		route_from.setText("From");
	    	
	    	route_to = (TextView) parent.findViewById(R.id.route_to);
	    	if (route_to != null && addresses != null)
	    		route_to.setText("To");
	    	 
	    	route_from_address = (TextView) parent.findViewById(R.id.route_from_address);
	        if (route_from_address != null && addresses != null)
	        	route_from_address.setText(addresses.get(0));
	        
	        route_to_address = (TextView) parent.findViewById(R.id.route_to_address);
	        if (route_to_address != null && addresses != null)
	        	route_to_address.setText(addresses.get(1));
	        
	        speed = (TextView) parent.findViewById(R.id.speed);
	        if (speed != null)
	        	speed.setText("Speed");        
	        
	        distance = (TextView) parent.findViewById(R.id.distance);
	        if (distance != null)        	
	        	distance.setText("Distance");
	        
	        the_speed = (TextView) parent.findViewById(R.id.the_speed);
	        if (the_speed != null)
	        	the_speed.setText(tripDetails.getSpeed(0) + " km/h");        
	        
	        the_distance = (TextView) parent.findViewById(R.id.the_distance);
	        if (the_distance != null) {       	
	        	the_distance.setText(tripDetails.getTotalDistance() + " Km");
	        }
	        
	        meansOfTransp = (ImageView) parent.findViewById(R.id.vehicle);
	        if (tripDetails.getVehicle(0).equals("car")) meansOfTransp.setImageResource(R.drawable.driving_high);
	        else if (tripDetails.getVehicle(0).equals("bike")) meansOfTransp.setImageResource(R.drawable.biking_high);
	        else if (tripDetails.getVehicle(0).equals("plane")) meansOfTransp.setImageResource(R.drawable.plane_high);
	        else meansOfTransp.setImageResource(R.drawable.walking_high);
    	}
    }

    @Override
    public int getType() {
        //Very important with different inner layouts
        return 4;
    }
}
