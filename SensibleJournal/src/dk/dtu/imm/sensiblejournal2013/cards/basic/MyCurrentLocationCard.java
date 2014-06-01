package dk.dtu.imm.sensiblejournal2013.cards.basic;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.component.CardThumbnailView;

import java.util.List;
import java.util.Locale;

import dk.dtu.imm.sensiblejournal2013.R;
import dk.dtu.imm.sensiblejournal2013.cards.tools.CustomHeader;
import dk.dtu.imm.sensiblejournal2013.cards.tools.CustomThumbCard;
import dk.dtu.imm.sensiblejournal2013.detailedViews.MyLocationDetailedView;
import dk.dtu.imm.sensiblejournal2013.utilities.Constants;
import dk.dtu.imm.sensiblejournal2013.utilities.AppFunctions;
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
import android.widget.LinearLayout;
import android.widget.TextView;

/** MY CURRENT LOCATION CARD **/
public class MyCurrentLocationCard extends Card {

	private TextView address;
	private TextView city;
    private Context context;
    private DisplayMetrics metrics;
    private AppFunctions functions;
    private Location myLocation;
    private List<Address> addresses;
    private CustomThumbCard thumbnail;
    private LinearLayout awesomeLayout;
    private TextView awesomeTextView;
    private boolean[] awesomeClicked = {false};

    public MyCurrentLocationCard(Context context, Location myLocation) {
        super(context, R.layout.my_current_location_card_content);
        this.context = context;
        this.myLocation = myLocation;
        this.metrics = context.getResources().getDisplayMetrics();
        functions = new AppFunctions(context);
        init();
    }

	private void init(){        	       
        //Create a CardHeader
    	CustomHeader header = new CustomHeader(context, getType());
        addCardHeader(header);              

        //Add ClickListener
        setOnClickListener(new OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
            	Intent intent = new Intent(context, MyLocationDetailedView.class);
                intent.putExtra(Constants.MY_LOCATION, myLocation);
                context.startActivity(intent);
            }
        });            
                               
        thumbnail = new CustomThumbCard (getContext());                   	 	       	 	           
    	double longt = myLocation.getLongitude();
    	double lat = myLocation.getLatitude();             	
        thumbnail.setUrlResource("http://maps.googleapis.com/maps/api/staticmap?zoom=15&size="
        						+ (int) (Constants.THUMB_WIDTH/2 - (10*metrics.density)) + "x" + Constants.THUMB_HEIGHT/2 + 
        																	"&maptype=roadmap&markers=color:red%7Clabel:C%7C"+ lat +","
        																	+ longt + "&sensor=false&scale=2&visual_refresh=true");
        
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
    }
    
    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

    	setBackgroundResourceId(R.color.main_card_color);
    	
    	CardThumbnailView cardThumb = (CardThumbnailView) parent.findViewById(R.id.card_thumbnail_layout);
        cardThumb.addCardThumbnail(thumbnail);
        
        awesomeLayout = (LinearLayout) view.findViewById(R.id.awesome_layout);
        awesomeTextView = (TextView) view.findViewById(R.id.awesome);
        functions.setupAwesomeButton(view, awesomeLayout, awesomeTextView, awesomeClicked, getType());
    	
    	if (addresses!=null) {
	        //Retrieve elements        	
	    	city = (TextView) parent.findViewById(R.id.city);
	        if (city != null && addresses.size() > 0)
	         	city.setText(addresses.get(0).getAddressLine(1));    
	         
	    	address = (TextView) parent.findViewById(R.id.address);
	        if (address != null && addresses.size() > 0)
	        	address.setText(addresses.get(0).getAddressLine(0));
    	}    
    }

    @Override
    public int getType() {
        //Very important with different inner layouts
        return 1;
    }
}