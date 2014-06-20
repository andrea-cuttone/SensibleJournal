package dk.dtu.imm.sensiblejournal2013;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import dk.dtu.imm.sensiblejournal2013.R;
import dk.dtu.imm.sensiblejournal2013.archive.ArchiveDaysActivity;
import dk.dtu.imm.sensiblejournal2013.archive.ArchiveWeeksActivity;
import dk.dtu.imm.sensiblejournal2013.cards.tools.CardListFragment;
import dk.dtu.imm.sensiblejournal2013.cards.tools.CardRefreshTask;
import dk.dtu.imm.sensiblejournal2013.data.DataController;
import dk.dtu.imm.sensiblejournal2013.usageLog.LogDbHelper;
import dk.dtu.imm.sensiblejournal2013.utilities.Constants;
import dk.dtu.imm.sensiblejournal2013.utilities.AppFunctions;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {

	private DrawerLayout mDrawer;
	private CustomActionBarDrawerToggle mDrawerToggle;	
	private DisplayMetrics metrics;
	private AppFunctions functions;
	private SharedPreferences settings;
	private LinkedList<Integer> POI_ids = new LinkedList<Integer>();
	private LinkedList<Location> locations = new LinkedList<Location>();			
	private LinkedList<Date> arrivals = new LinkedList<Date>();
	private LinkedList<Date> departures = new LinkedList<Date>();
	private LinkedList<String> days = new LinkedList<String>();
	private CardListFragment cardFragment;
	private DataController rClient;	
	
	public static final String[] options = {
		"Current Location",
        "Today's Itinerary",
        "Most Visited",                
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		functions = new AppFunctions(this);
		//functions.reverseCSV();
		//functions.fromCSVtoSqlite("user_movements.csv");						
		settings = getSharedPreferences("com.sensibleDTU.settings", 0);
		settings.edit();
        		
		//Print the log data
		Log.d("Usage Log", functions.getLogData());
				
		metrics = getResources().getDisplayMetrics();
        Constants.THUMB_WIDTH = metrics.widthPixels;
        if (metrics.heightPixels < 1920) Constants.THUMB_HEIGHT = 400;
        else Constants.THUMB_HEIGHT = 520;
		
		// Enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
                
        mDrawerToggle = new CustomActionBarDrawerToggle(this, mDrawer);
        mDrawer.setDrawerListener(mDrawerToggle);
                        	 		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (isNetworkAvailable() == true) {
			if (savedInstanceState == null) {
				cardFragment = new CardListFragment();
				this.getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, cardFragment).commit();
			}
		}
		
		// Create a warning dialog and exit
		else {			
			builder.setMessage("This application requires internet connection!")
		       .setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	 System.exit(0);
		           }
		       });
			AlertDialog alert = builder.create();
			if(!isFinishing()) alert.show();
		}
		
		// Set the necessary alarms		
		SharedPreferences settings = getSharedPreferences("com.sensibleDTU.settings", 0);
		SharedPreferences.Editor editor = settings.edit();        
        //editor.clear();
        //editor.commit();
		boolean firstExec = settings.getBoolean("firstExec", true);
		if (firstExec) {
			functions.setAlarms(this);						
            editor.putBoolean("firstExec", false);            
            editor.commit();
		}
		
		// When the app opens, fetch data		
		new Request().execute(this);
		//new UploadToServerAsync().execute(this);
	}
	
	// Check for internet connection
	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	private void _initMenu() {
				
		rClient = new DataController(this);
		rClient.getDataFromCache(0, false, false, false, true, POI_ids, locations, arrivals, departures, days);			
                    
        SparseArray<Group> cardGroups = new SparseArray<Group>();
        Group dailyItineraryGroup = new Group("Daily Route");
        String tmpMonth = "";
        for (int i=0; i<days.size(); i++) { 
        	if (!(days.get(i).substring(4, 7) + " " + days.get(i).substring(11, 15)).equals(tmpMonth)) {
        		tmpMonth = days.get(i).substring(4, 7) + " " + days.get(i).substring(11, 15);            	                           	
        		dailyItineraryGroup.children.add(tmpMonth);
        	}        	
        }        
        cardGroups.append(0, dailyItineraryGroup);
        
        Group weeklyItineraryGroup = new Group("Weekly Route");
        String tmpYear = "";
        for (int i=0; i<days.size(); i++) { 
        	if (!(days.get(i).substring(11, 15)).equals(tmpYear)) {
        		tmpYear = days.get(i).substring(11, 15);            	                           	
        		weeklyItineraryGroup.children.add(tmpYear);
        	}        	
        }        
        cardGroups.append(1, weeklyItineraryGroup);
                                             
        ExpandableListView listView = (ExpandableListView) findViewById(R.id.archive_list);        
        ArchiveExpandableListAdapter adapter = new ArchiveExpandableListAdapter(this, cardGroups);
        listView.setAdapter(adapter);
 
	} 
	
	protected void onPostCreate(Bundle savedInstanceState) {
	    super.onPostCreate(savedInstanceState);
	    // Sync the toggle state after onRestoreInstanceState has occurred.
	    mDrawerToggle.syncState();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Constants.appVisible = 1;
		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(Constants.NOTIFICATION_ID);
		_initMenu();
		
		// Log the application launch		
		LogDbHelper logDbHelper = new LogDbHelper(this);
		logDbHelper.log(Constants.logComponents.MAIN, System.currentTimeMillis());
	}
	
	@Override
	public void onPause() {	
		super.onPause();
		Constants.appVisible = 0;
		try {
			// save index and top position
			Constants.index = Constants.mListView.getFirstVisiblePosition();
			View v = Constants.mListView.getChildAt(0);
			Constants.top = (v == null) ? 0 : v.getTop();									
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// If the application is paused...
		if (Constants.paused) {
			// ...add the pause time-stamp to the log
			LogDbHelper logDbHelper = new LogDbHelper(this);
			logDbHelper.log(Constants.logComponents.PAUSE, System.currentTimeMillis());
		}
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
    public void onStop() {
		super.onStop();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		 MenuInflater inflater = getMenuInflater();
	     inflater.inflate(R.menu.main, menu);
	     return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
	    }
	    switch (item.getItemId()) {
	    	case R.id.action_settings:
	    		functions.showAboutDialog();
	    		return true;
	    	case R.id.action_refresh:
	    		try {
					Constants.progressDialog = ProgressDialog.show(this, "", "Fetching data. Please wait...");
				} catch (Exception e) { e.printStackTrace(); }
	    		new Request().execute(this);
	            return true;
            default:
            	break;
	    }
        return super.onOptionsItemSelected(item);
	}
	
	/**** CustomActionBarDrawerToggle class ****/
	public class CustomActionBarDrawerToggle extends ActionBarDrawerToggle {

        public CustomActionBarDrawerToggle(Activity mActivity, DrawerLayout mDrawerLayout) {
        	super(  mActivity,
                    mDrawerLayout,
                    R.drawable.ic_drawer,
                    R.string.app_name,
                    R.string.app_name );
        }

        @Override
        public void onDrawerClosed(View view) {
            getActionBar().setTitle(getString(R.string.app_name));
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            getActionBar().setTitle(getString(R.string.archive));
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }
    }
	
	public class ArchiveExpandableListAdapter extends BaseExpandableListAdapter {    	
    	private final SparseArray<Group> cardGroups;
		public LayoutInflater inflater;
		public Activity activity;
    	
    	public ArchiveExpandableListAdapter(Activity act, SparseArray<Group> cardGroups){
    		activity = act;
			this.cardGroups = cardGroups;
			inflater = act.getLayoutInflater();
    	}

    	@Override
		public Object getChild(int groupPosition, int childPosition) {
			return cardGroups.get(groupPosition).children.get(childPosition);
		}

    	@Override
    	public long getChildId(int groupPosition, int childPosition) {
    		return childPosition;
    	}

    	@Override
    	public View getChildView(final int groupPosition, int childPosition,
    				boolean isLastChild, View convertView, ViewGroup parent) {
    		
    		final String tmpChild = (String) getChild(groupPosition, childPosition);
    		TextView text = null;
    		if (convertView == null) {
    			convertView = inflater.inflate(R.layout.archive_listrow_details, null);
    		}
    		text = (TextView) convertView.findViewById(R.id.archiveGroup);
    		text.setText(tmpChild);
    		convertView.setOnClickListener(new OnClickListener() {
		
				@Override
				public void onClick(View v) {
					
					if (groupPosition == 0) {
						Constants.progressDialog = ProgressDialog.show(MainActivity.this, "", "Loading. Please wait...");
						LinkedList<Location> selectedItineraryLocations = new LinkedList<Location>();			
						LinkedList<Date> selectedArrivals = new LinkedList<Date>();
						LinkedList<Date> selectedDepartures = new LinkedList<Date>();
						LinkedList<String> selectedDays = new LinkedList<String>();
						LinkedList<Integer> selectedPOIs = new LinkedList<Integer>();
						String selectedMonth = "";
							
						for(int i=0; i<days.size(); i++){
							if(tmpChild.equals(days.get(i).substring(4, 7) + " " + days.get(i).substring(11, 15))){
								selectedMonth = tmpChild;
								selectedItineraryLocations.add(locations.get(i));
								selectedArrivals.add(arrivals.get(i));
								selectedDepartures.add(departures.get(i));
								selectedDays.add(days.get(i));
								selectedPOIs.add(POI_ids.get(i));
							}
						}
							
						Intent intent = new Intent(activity, ArchiveDaysActivity.class);
						
						intent.putExtra(Constants.SELECTED_MONTH, selectedMonth); 
						intent.putExtra(Constants.STOP_LOCATIONS, selectedItineraryLocations);                    
						intent.putExtra(Constants.STOP_LOCATIONS_ARRIVALS, selectedArrivals);
						intent.putExtra(Constants.STOP_LOCATIONS_DEPARTURES, selectedDepartures);
						intent.putExtra(Constants.DAYS, selectedDays);
						intent.putExtra(Constants.POIs, selectedPOIs);
						Constants.paused = false;
						startActivity(intent);
					}
					else if (groupPosition == 1) {
						Constants.progressDialog = ProgressDialog.show(MainActivity.this, "", "Loading. Please wait...");
						LinkedList<Location> selectedItineraryLocations = new LinkedList<Location>();			
						LinkedList<Date> selectedArrivals = new LinkedList<Date>();
						LinkedList<Date> selectedDepartures = new LinkedList<Date>();
						LinkedList<String> selectedDays = new LinkedList<String>();
						LinkedList<Integer> selectedPOIs = new LinkedList<Integer>();
						String selectedYear = "";
							
						for(int i=0; i<days.size(); i++){
							if(tmpChild.equals(days.get(i).substring(11, 15))){
								selectedYear = tmpChild;
								selectedItineraryLocations.add(locations.get(i));
								selectedArrivals.add(arrivals.get(i));
								selectedDepartures.add(departures.get(i));
								selectedDays.add(days.get(i));
								selectedPOIs.add(POI_ids.get(i));
							}
						}
							
						Intent intent = new Intent(activity, ArchiveWeeksActivity.class);						
						intent.putExtra(Constants.SELECTED_YEAR, selectedYear); 
						intent.putExtra(Constants.STOP_LOCATIONS, selectedItineraryLocations);                    
						intent.putExtra(Constants.STOP_LOCATIONS_ARRIVALS, selectedArrivals);
						intent.putExtra(Constants.STOP_LOCATIONS_DEPARTURES, selectedDepartures);
						intent.putExtra(Constants.DAYS, selectedDays);
						intent.putExtra(Constants.POIs, selectedPOIs);
						Constants.paused = false;
						startActivity(intent);
					}
				}
	    	});
		
    		return convertView;
    	}

		@Override
		public int getChildrenCount(int groupPosition) {
			return cardGroups.get(groupPosition).children.size();
		}
	
		@Override
		public Object getGroup(int groupPosition) {
			return cardGroups.get(groupPosition);
		}
	
		@Override
		public int getGroupCount() {
			return cardGroups.size();
		}
	
		@Override
		public void onGroupCollapsed(int groupPosition) {
			super.onGroupCollapsed(groupPosition);
		}
	
		@Override
		public void onGroupExpanded(int groupPosition) {
			super.onGroupExpanded(groupPosition);
		}
	
		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}
	
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
									View convertView, ViewGroup parent) {
			if (convertView == null) {
				if (groupPosition == 0)
					convertView = inflater.inflate(R.layout.archive_listrow_group_daily, null);				
				else convertView = inflater.inflate(R.layout.archive_listrow_group_weekly, null);
			}
			
			Group group = (Group) getGroup(groupPosition);
			CheckedTextView chText = (CheckedTextView) convertView.findViewById(R.id.archiveGroup);
			chText.setText(group.string);
			chText.setChecked(isExpanded);
			return convertView;
		}
	
		@Override
		public boolean hasStableIds() {
			return false;
		}
	
		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}
    }
	
	public class Group {
		public String string;
		public final List<String> children = new ArrayList<String>();

		public Group(String string) {
			this.string = string;
		}
	}
	
	@Override
	public void onBackPressed(){
		super.onBackPressed();
		Constants.newDataFetched = true;
		finish();
	}
	
	// Asynchronously request data from server
	class Request extends AsyncTask<Context, Void, Void> {
	    @Override
	    protected Void doInBackground(final Context... params) {
	    	DataController rClient = new DataController(params[0]);	    	
			try {
				rClient.getDataFromServer();
				Constants.cards = null;
				Constants.mostVisitedCard = null;
				Constants.todaysItCard = null;
				Constants.weeklyItCard = null;
				Constants.myCurrentLocationCard = null;
				Constants.pastStopCard = null;
				Constants.commuteCard = null;
								
				CardRefreshTask rTask = new CardRefreshTask(params[0]);
				rTask.execute();
				((Activity) params[0]).runOnUiThread(new Runnable() {
					  public void run() {
						  _initMenu();
					  }
				});
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				e.printStackTrace();
				final AlertDialog.Builder builder = new AlertDialog.Builder(params[0]);		
				builder.setMessage("Data fetch error! Check internet connection or try again later.")
					.setCancelable(false)
				    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int id) {
				    	if (Constants.progressDialog != null) Constants.progressDialog.dismiss();
				    	return;
				    }
				});
				((Activity) params[0]).runOnUiThread(new Runnable() {
					  public void run() {
						  AlertDialog alert = builder.create();
						  if(!((Activity) params[0]).isFinishing()) alert.show();
					  }
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
	    }
	}
}
