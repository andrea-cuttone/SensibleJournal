package dk.dtu.imm.sensible.cards.tools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import dk.dtu.imm.sensible.R;
import it.gmariotti.cardslib.library.internal.CardHeader;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/** OTHER USEFUL CLASSES **/
public class CustomHeader extends CardHeader {

	private int type;
	private String moreDetails;
	private boolean archived = false;
	private String year = "";
	
    public CustomHeader(Context context, int type) {
        super(context, R.layout.card_header_layout);
        this.type = type;
    }
    
    public CustomHeader(Context context, int type, String dateString, boolean archived) {
        super(context, R.layout.card_header_layout);
        this.type = type;
        this.moreDetails = dateString;
        this.archived = archived;
    }
    
    public CustomHeader(Context context, int type, String dateString, boolean archived, String year) {
        super(context, R.layout.card_header_layout);
        this.type = type;
        this.moreDetails = dateString;
        this.archived = archived;
        this.year = year;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
    	
    	if (view!=null){
            TextView t1 = (TextView) view.findViewById(R.id.header_title);
            
            if (type == 0){
            	TextView t2 = (TextView) view.findViewById(R.id.header_date);
            	t2.setVisibility(View.GONE);
            	if (t1!=null)
                    t1.setText(getContext().getString(R.string.most_visited_header_title));
            }
            
            else if ((type == 1) || (type == 2)){
            	// Get today's date
                Date date = new Date();
            	
            	if (t1!=null)
                    if (type == 1) t1.setText(getContext().getString(R.string.my_current_location_card_title));
                    else if (type == 2) t1.setText(getContext().getString(R.string.todays_itinerary_card_title));
            	
            	TextView t2 = (TextView) view.findViewById(R.id.header_date);
            	if (t2!=null)
            		if (type == 1) t2.setText(date.toString());
            		else if (type == 2) {          			
            			if (archived) t2.setText(this.moreDetails + " (" + getContext().getString(R.string.archived) + ")");
            			else t2.setText(this.moreDetails);
            		}
            }
            
            else if (type == 3){
            	TextView t2 = (TextView) view.findViewById(R.id.header_date);
            	t2.setVisibility(View.GONE);
            	if (t1!=null)
                    t1.setText(getContext().getString(R.string.past_stop_header_title));
            }
            
            else if (type == 4){
            	TextView t2 = (TextView) view.findViewById(R.id.header_date);
            	t2.setVisibility(View.GONE);
            	if (t1!=null)
                    t1.setText(getContext().getString(R.string.commute_header_title));
            }
            
            else if (type == 5){
            	TextView t2 = (TextView) view.findViewById(R.id.header_date);
            	if (t2!=null) {
            		// Get the starting and ending dates of the week
            		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.ROOT);
            		Calendar cal = Calendar.getInstance();
            		cal.clear();
            		cal.setFirstDayOfWeek(Calendar.MONDAY);
            		cal.set(Calendar.WEEK_OF_YEAR, Integer.parseInt(moreDetails)); 
            		cal.set(Calendar.YEAR, Integer.parseInt(year));            		           	
            		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            		String date1 = sdf.format(cal.getTime());
            		cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            		String date2 = sdf.format(cal.getTime());
            		
            		if (archived) t2.setText("Week " + moreDetails + ": " + date1 + " - " + date2 + " (" + getContext().getString(R.string.archived) + ")");
            		else t2.setText("Week " + moreDetails + ": " + date1 + " - " + date2);
            	}            		
            	if (t1!=null)
                    t1.setText(getContext().getString(R.string.weekly_itinerary_card_title));
            }
            
            else if (type == 6) {
            	TextView t2 = (TextView) view.findViewById(R.id.header_date);
            	t2.setVisibility(View.GONE);
            	if (t1!=null)
                    t1.setText(getContext().getString(R.string.tutorial1_title));
            }
            
            else if (type == 7) {
            	TextView t2 = (TextView) view.findViewById(R.id.header_date);
            	t2.setVisibility(View.GONE);
            	if (t1!=null)
                    t1.setText(getContext().getString(R.string.tutorial2_title));
            }
            
            else if (type == 8) {
            	TextView t2 = (TextView) view.findViewById(R.id.header_date);
            	t2.setVisibility(View.GONE);
            	if (t1!=null)
                    t1.setText(getContext().getString(R.string.tutorial3_title));
            }
            
            else if (type == 9) {
            	TextView t2 = (TextView) view.findViewById(R.id.header_date);
            	t2.setVisibility(View.GONE);
            	if (t1!=null)
                    t1.setText(getContext().getString(R.string.tutorial4_title));
            }
            
            else if (type == 10) {
            	TextView t2 = (TextView) view.findViewById(R.id.header_date);
            	t2.setVisibility(View.GONE);
            	if (t1!=null)
                    t1.setText(getContext().getString(R.string.tutorial5_title));
            }                       
        }        
    }
}
