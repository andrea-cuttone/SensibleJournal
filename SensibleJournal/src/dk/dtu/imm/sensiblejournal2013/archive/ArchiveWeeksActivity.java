package dk.dtu.imm.sensiblejournal2013.archive;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import dk.dtu.imm.sensiblejournal2013.R;
import dk.dtu.imm.sensiblejournal2013.detailedViews.WeeklyItineraryDetailedView;
import dk.dtu.imm.sensiblejournal2013.usageLog.LogDbHelper;
import dk.dtu.imm.sensiblejournal2013.utilities.Constants;
import dk.dtu.imm.sensiblejournal2013.utilities.AppFunctions;
import android.location.Location;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class ArchiveWeeksActivity extends Activity {
	
	private Calendar cal;
	private DateFormat formatter;
	private Date date;
	private AppFunctions functions;

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_archive_grid);
		setupActionBar();
		functions = new AppFunctions(this);
		
		Intent intent = getIntent();
		final String selectedYear = (String) intent.getExtras().get(Constants.SELECTED_YEAR);
		final List<?> POI_ids = (List<?>) intent.getExtras().get(Constants.POIs);
	    final List<?> locations = (List<?>) intent.getExtras().get(Constants.STOP_LOCATIONS);
	    final List<?> arrivals = (List<?>) intent.getExtras().get(Constants.STOP_LOCATIONS_ARRIVALS);
	    final List<?> departures = (List<?>) intent.getExtras().get(Constants.STOP_LOCATIONS_DEPARTURES);
	    final List<?> days = (List<?>) intent.getExtras().get(Constants.DAYS);	    
	    		
	    setTitle(selectedYear);		
		GridView gridView = (GridView) findViewById(R.id.gridview);
		
		// Reverse the lists in order to achieve chronological sequence of the events
		Collections.reverse(POI_ids);
		Collections.reverse(locations);
		Collections.reverse(arrivals);
		Collections.reverse(departures);
		Collections.reverse(days);
		
		List<String> weeks = new LinkedList<String>();
		cal = Calendar.getInstance();
		formatter = new SimpleDateFormat("EEE MMM dd yyyy");
		date = null;
		String tmpWeekNumber = "";
		for (int i=0; i<days.size(); i++){						
			try {
				date = formatter.parse((String) days.get(i));
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			cal.setTime(date);
			
			if (((String) days.get(i)).substring(0, 3).equals("Sun")) {
				cal.add(Calendar.DAY_OF_YEAR, -1);
			}
			
			if (!Integer.toString(cal.get(Calendar.WEEK_OF_YEAR)).equals(tmpWeekNumber)) {												
				tmpWeekNumber = Integer.toString(cal.get(Calendar.WEEK_OF_YEAR));
				// Get the starting and ending dates of the week
	    		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.ROOT);
	    		Calendar cal = Calendar.getInstance();
	    		cal.clear();
	    		cal.setFirstDayOfWeek(Calendar.MONDAY);
	    		cal.set(Calendar.WEEK_OF_YEAR, Integer.parseInt(tmpWeekNumber)); 
	    		cal.set(Calendar.YEAR, Integer.parseInt(selectedYear));	
	    		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
	    		String date1 = sdf.format(cal.getTime());
	    		cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
	    		String date2 = sdf.format(cal.getTime());
				weeks.add("Week " + tmpWeekNumber + "\n" + date1 + " - " + date2);
			}
	
		}
		gridView.setAdapter(new ImageAdapter(this, weeks));
		    		    		   
		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Toast.makeText(getApplicationContext(), "" + ((TextView) v.findViewById(R.id.item_label))
						   .getText(), Toast.LENGTH_SHORT).show();
				
				LinkedList<Integer> selectedPOI_ids = new LinkedList<Integer>();
				LinkedList<Location> selectedLocations = new LinkedList<Location>();			
				LinkedList<Date> selectedArrivals = new LinkedList<Date>();
				LinkedList<Date> selectedDepartures = new LinkedList<Date>();				
							
				cal = Calendar.getInstance();
				formatter = new SimpleDateFormat("EEE MMM dd yyyy");
				date = null;
				for(int i=0; i<days.size(); i++){
																
					try {
						date = formatter.parse((String) days.get(i));
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
										
					cal.setTime(date);
					
					if (((String) days.get(i)).substring(0, 3).equals("Sun")) {
						cal.add(Calendar.DAY_OF_YEAR, -1);
					}
					
					String tmp = (String) ((TextView) v.findViewById(R.id.item_label)).getText();
					int end = tmp.indexOf("\n");
					if(((TextView) v.findViewById(R.id.item_label)).getText().subSequence(0, end).equals("Week " + Integer.toString(cal.get(Calendar.WEEK_OF_YEAR)))){
						selectedPOI_ids.add((Integer) POI_ids.get(i));
						selectedLocations.add((Location) locations.get(i));
						selectedArrivals.add((Date) arrivals.get(i));
						selectedDepartures.add((Date) departures.get(i));						
					}
				}

				/****** Remove consequent duplicates from the lists *******/
				LinkedList<Integer> resultPOI_ids = new LinkedList<Integer>();
				LinkedList<Location> resulLocations = new LinkedList<Location>();			
				LinkedList<Date> resulArrivals = new LinkedList<Date>();
				LinkedList<Date> resulDepartures = new LinkedList<Date>();
				
				List<Integer> indices = new LinkedList<Integer>();
				for (int i=1; i<selectedPOI_ids.size(); i++) {
					if ( (selectedLocations.get(i).getLatitude() == selectedLocations.get(i-1).getLatitude()) &&
						 (selectedLocations.get(i).getLongitude() == selectedLocations.get(i-1).getLongitude()) )
						indices.add(i);
				}				
				
				for (int i=0; i<selectedPOI_ids.size(); i++) {
					if ( indices.contains(i) ) continue;
					else {
						resultPOI_ids.add(selectedPOI_ids.get(i));
						resulLocations.add(selectedLocations.get(i));
						resulArrivals.add(selectedArrivals.get(i));
						resulDepartures.add(selectedDepartures.get(i));
					}
				}
				/***********************************************************/

			    Intent intent = new Intent(parent.getContext(), WeeklyItineraryDetailedView.class);
			    intent.putExtra(Constants.POIs, resultPOI_ids);
			    intent.putExtra(Constants.STOP_LOCATIONS, resulLocations);                    
			    intent.putExtra(Constants.STOP_LOCATIONS_ARRIVALS, resulArrivals);
			    intent.putExtra(Constants.STOP_LOCATIONS_DEPARTURES, resulDepartures);
			    intent.putExtra(Constants.SELECTED_WEEK, ((TextView) v.findViewById(R.id.item_label)).getText());
			    startActivity(intent);
			}
		});
		
		if (Constants.progressDialog != null)
			Constants.progressDialog.dismiss();
	}

	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Constants.appVisible = 1;
		LogDbHelper logDbHelper = new LogDbHelper(this);
		logDbHelper.log(Constants.logComponents.WEEK_ARCHIVE, System.currentTimeMillis());
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.archive_days, menu);
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
	
	public class ImageAdapter extends BaseAdapter {
	    private Context mContext;
	    private LayoutInflater layoutInflater;
	    private List<?> itineraryWeeks;

	    public ImageAdapter(Context c, List<?> itineraryWeeks) {
	        mContext = c;
	        layoutInflater = LayoutInflater.from(mContext);
	        this.itineraryWeeks = itineraryWeeks;	        
	    }

	    public Object getItem(int position) {
	        return null;
	    }

	    public long getItemId(int position) {
	        return 0;
	    }

	    // create a new ImageView for each item referenced by the Adapter
	    public View getView(int position, View convertView, ViewGroup parent) {
	        
	    	View list;
	    	if(convertView==null){
	    		list = new View(mContext);
	    		list = layoutInflater.inflate(R.layout.archive_item_layout, null); 
	    	}else{
	    		list = (View)convertView; 
	    	}
		  
	    	TextView textView = (TextView)list.findViewById(R.id.item_label);
	    	textView.setText(String.valueOf(itineraryWeeks.get(position)));	    	    
	    	return list;
	    }

		@Override
		public int getCount() {			
			return itineraryWeeks.size();
		}	
	}

}
