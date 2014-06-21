package dk.dtu.imm.sensiblejournal2013.cards.basic;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.component.CardThumbnailView;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import dk.dtu.imm.sensiblejournal2013.R;
import dk.dtu.imm.sensiblejournal2013.cards.tools.CustomHeader;
import dk.dtu.imm.sensiblejournal2013.cards.tools.CustomThumbCard;
import dk.dtu.imm.sensiblejournal2013.detailedViews.PastStopDetailedView;
import dk.dtu.imm.sensiblejournal2013.utilities.Constants;
import dk.dtu.imm.sensiblejournal2013.utilities.AppFunctions;

/** PAST STOP (LAST VISITED PLACE) CARD **/
public class PastStopCard extends Card {
	
	private TextView address;
	private TextView timeSpent;
    private String timeSpentStr;
    private Context context;
    private DisplayMetrics metrics;
    private AppFunctions functions;
    private List<Address> addresses;
    private LinkedList<Location> locations = new LinkedList<Location>();			
    private LinkedList<Date> arrivals = new LinkedList<Date>();
    private LinkedList<Date> departures = new LinkedList<Date>();
    private int days;
    private long hours;
    private long minutes; 				
    private long seconds;
    private CustomThumbCard thumbnail;
    private Button awesomeButton;
    private boolean[] awesomeClicked = {false};

    public PastStopCard(Context context, LinkedList<Location> locations,
			LinkedList<Date> arrivals, LinkedList<Date> departures) {
        super(context, R.layout.past_stop_card_content);
        this.context = context;
        this.metrics = context.getResources().getDisplayMetrics();
        this.locations = locations;
        this.arrivals = arrivals;
        this.departures = departures;
        functions = new AppFunctions(context);
        init();
    }

	private void init(){        	       
        //Create a CardHeader
    	CustomHeader header = new CustomHeader(context, getType());
        addCardHeader(header);
        
        if (locations.size() > 0) {
	        //Add ClickListener
	        setOnClickListener(new OnCardClickListener() {
	            @Override
	            public void onClick(Card card, View view) {
	            	Intent intent = new Intent(context, PastStopDetailedView.class);
	                intent.putExtra(Constants.PAST_LOCATION, locations.get(0));
	                intent.putExtra(Constants.PAST_LOCATION_TIME_SPENT, timeSpentStr);
	                intent.putExtra(Constants.STOP_LOCATIONS_ARRIVALS, arrivals);
	                intent.putExtra(Constants.STOP_LOCATIONS_DEPARTURES, departures);
	                Constants.paused = false;
	                context.startActivity(intent);
	            }
	        });            
	                               
	        thumbnail = new CustomThumbCard (getContext());
	        // Get the latitude and logtitude of the first place appearing on the file, 
	        // which is the last place that the user visited
	    	double longt = locations.get(0).getLongitude();
	    	double lat = locations.get(0).getLatitude();           	
	        thumbnail.setUrlResource("http://maps.googleapis.com/maps/api/staticmap?zoom=15&size="
	        						+ (int) (Constants.THUMB_WIDTH/2 - (10*metrics.density)) + "x" + Constants.THUMB_HEIGHT/2 + 
	        																	"&maptype=roadmap&markers=color:red%7Clabel:C%7C"+ lat +","
	        																	+ longt + "&sensor=false&scale=2&visual_refresh=true");
	        addCardThumbnail(thumbnail);
	        
	        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
	        try {
				addresses = geocoder.getFromLocation(lat, longt, 1);
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
	        
	        // Calculate the time that the user stayed at the last stop location
	        Long duration = (departures.get(0).getTime()/1000) - (arrivals.get(0).getTime()/1000);    	        	
	    	days = (int)TimeUnit.SECONDS.toDays(duration);        
	    	hours = TimeUnit.SECONDS.toHours(duration) - (days *24);
	    	minutes = TimeUnit.SECONDS.toMinutes(duration)
	    						- (TimeUnit.SECONDS.toHours(duration)* 60);
	    	seconds = TimeUnit.SECONDS.toSeconds(duration)
	    						- (TimeUnit.SECONDS.toMinutes(duration) *60);
	    	
	    	timeSpentStr = days + "d " + hours + "h " + minutes + "m " + seconds + "s";
        }
    }
    
    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
    	
    	setBackgroundResourceId(R.color.main_card_color);
    	
    	CardThumbnailView cardThumb = (CardThumbnailView) parent.findViewById(R.id.card_thumbnail_layout);
        cardThumb.addCardThumbnail(thumbnail);
        
        awesomeButton = (Button) view.findViewById(R.id.awesome);
        functions.setupAwesomeButton(view, awesomeButton, awesomeClicked, getType());
         
    	if (addresses!=null) {
	    	address = (TextView) parent.findViewById(R.id.address);
	        if (address != null && addresses.size() > 0)
	        	address.setText(addresses.get(0).getAddressLine(0));
	        
	        timeSpent = (TextView) parent.findViewById(R.id.time_spent);
	        if (timeSpent != null && timeSpent != null)
	        	timeSpent.setText(timeSpentStr);
    	}           
    }

    @Override
    public int getType() {
        //Very important with different inner layouts
        return 3;
    }
}
