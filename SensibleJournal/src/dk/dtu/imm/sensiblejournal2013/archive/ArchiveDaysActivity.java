package dk.dtu.imm.sensible.archive;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import dk.dtu.imm.sensible.R;
import dk.dtu.imm.sensible.detailedViews.TodaysItineraryDetailedView;
import dk.dtu.imm.sensible.usageLog.LogDbHelper;
import dk.dtu.imm.sensible.utilities.Constants;
import dk.dtu.imm.sensible.utilities.AppFunctions;
import android.location.Location;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class ArchiveDaysActivity extends Activity {
	
	private AppFunctions functions;
	private long enter_timestamp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_archive_grid);
		setupActionBar();
		functions = new AppFunctions(this);
		
		Intent intent = getIntent();
		final String selectedMonth = (String) intent.getExtras().get(Constants.SELECTED_MONTH);
	    final List<?> monthLocations = (List<?>) intent.getExtras().get(Constants.STOP_LOCATIONS);
	    final List<?> monthArrivals = (List<?>) intent.getExtras().get(Constants.STOP_LOCATIONS_ARRIVALS);
	    final List<?> monthDepartures = (List<?>) intent.getExtras().get(Constants.STOP_LOCATIONS_DEPARTURES);
	    final List<?> monthDays = (List<?>) intent.getExtras().get(Constants.DAYS);
	    final List<?> monthPOI_ids = (List<?>) intent.getExtras().get(Constants.POIs);
	    
	    setTitle(selectedMonth);
		
		GridView gridview = (GridView) findViewById(R.id.gridview);
		
		// Reverse the lists in order to achieve chronological sequence of the events
		Collections.reverse(monthPOI_ids);
		Collections.reverse(monthLocations);
		Collections.reverse(monthArrivals);
		Collections.reverse(monthDepartures);
		Collections.reverse(monthDays);
		List<String> days = new LinkedList<String>();
		String tmpDay = "";
		for (int i=0; i<monthDays.size(); i++){
			if (!monthDays.get(i).equals(tmpDay)) days.add((String) monthDays.get(i));
			tmpDay = (String) monthDays.get(i);
		}
		gridview.setAdapter(new ImageAdapter(this, days));
		    		    		   
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Toast.makeText(getApplicationContext(), "" + ((TextView) v.findViewById(R.id.item_label))
						   .getText(), Toast.LENGTH_SHORT).show();
				
				LinkedList<Location> selectedItineraryLocations = new LinkedList<Location>();			
				LinkedList<Date> selectedArrivals = new LinkedList<Date>();
				LinkedList<Date> selectedDepartures = new LinkedList<Date>();
				LinkedList<Integer> selectedPOI_ids = new LinkedList<Integer>();
			
				for(int i=0; i<monthDays.size(); i++){
					if(((TextView) v.findViewById(R.id.item_label)).getText().equals(monthDays.get(i))){
						selectedItineraryLocations.add((Location) monthLocations.get(i));
						selectedArrivals.add((Date) monthArrivals.get(i));
						selectedDepartures.add((Date) monthDepartures.get(i));
						selectedPOI_ids.add((Integer) monthPOI_ids.get(i));
					}
				}

			    Intent intent = new Intent(parent.getContext(), TodaysItineraryDetailedView.class);
			    intent.putExtra(Constants.POIs, selectedPOI_ids);
			    intent.putExtra(Constants.STOP_LOCATIONS, selectedItineraryLocations);                    
			    intent.putExtra(Constants.STOP_LOCATIONS_ARRIVALS, selectedArrivals);
			    intent.putExtra(Constants.STOP_LOCATIONS_DEPARTURES, selectedDepartures);
			    intent.putExtra(Constants.SELECTED_DAY, ((TextView) v.findViewById(R.id.item_label)).getText());
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
		enter_timestamp = System.currentTimeMillis();
	}
	
	@Override
	public void onPause() {	
		super.onPause();
		Constants.appVisible = 0;
		
		// Add the time spent in the activity to the log
		LogDbHelper logDbHelper = new LogDbHelper(this);
		logDbHelper.log(Constants.logComponents.DAY_ARCHIVE, System.currentTimeMillis()-enter_timestamp);
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
	    private List<?> itineraryDays;

	    public ImageAdapter(Context c, List<?> itineraryDays) {
	        mContext = c;
	        layoutInflater = LayoutInflater.from(mContext);
	        this.itineraryDays = itineraryDays;	        
	    }

	    public Object getItem(int position) {
	        return null;
	    }

	    public long getItemId(int position) {
	        return 0;
	    }

	    // create a new ImageView for each item referenced by the Adapter
	    public View getView(int position, View convertView, ViewGroup parent) {
	        
	    	View grid;
	    	if(convertView==null){
	    		grid = new View(mContext);
	    		grid = layoutInflater.inflate(R.layout.archive_item_layout, null); 
	    	}else{
	    		grid = (View)convertView; 
	    	}
		  
	    	TextView textView = (TextView)grid.findViewById(R.id.item_label);
	    	textView.setText(String.valueOf(itineraryDays.get(position)));	    	    
	    	return grid;
	    }

		@Override
		public int getCount() {			
			return itineraryDays.size();
		}	
	}
}
