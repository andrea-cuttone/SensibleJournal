package dk.dtu.imm.sensiblejournal2013.utilities;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;
import android.app.ProgressDialog;
import android.location.Location;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import dk.dtu.imm.sensiblejournal2013.cards.basic.CommuteCard;
import dk.dtu.imm.sensiblejournal2013.cards.basic.MostVisitedPlacesCard;
import dk.dtu.imm.sensiblejournal2013.cards.basic.MyCurrentLocationCard;
import dk.dtu.imm.sensiblejournal2013.cards.basic.PastStopCard;
import dk.dtu.imm.sensiblejournal2013.cards.basic.TodaysItineraryCard;
import dk.dtu.imm.sensiblejournal2013.cards.basic.WeeklyItineraryCard;
import dk.dtu.imm.sensiblejournal2013.data.CacheDatabaseContract.CacheEntry;
import dk.dtu.imm.sensiblejournal2013.usageLog.LogDatabaseContract.LogEntry;

public class Constants {	
	
	public final static String APP_NAME = "SensibleJournal";
	public static final int NOTIFICATION_ID = 1234;
	public static String DATA_DB_FILENAME = "sensible_journal_cache.db";
	public static final int DATA_DB_VERSION = 41;
	public static String LOG_DB_FILENAME = "sensible_journal_log.db";
	public static final int LOG_DB_VERSION = 3;
	private static final String TEXT_TYPE = " TEXT";
	private static final String LONG_TYPE = " LONG";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String FLOAT_TYPE = " FLOAT";
	private static final String COMMA_SEP = ", ";
	public static final String SQL_CREATE_DATA_ENTRIES =
	    "CREATE TABLE IF NOT EXISTS " + CacheEntry.TABLE_NAME + "(" +	
	    CacheEntry.COLUMN_POI_ID + TEXT_TYPE + COMMA_SEP +
	    CacheEntry.COLUMN_LATITUDE + FLOAT_TYPE + COMMA_SEP +
	    CacheEntry.COLUMN_LONGTITUDE + FLOAT_TYPE + COMMA_SEP +
	    CacheEntry.COLUMN_ARRIVAL + LONG_TYPE + COMMA_SEP + 
	    CacheEntry.COLUMN_DEPARTURE + LONG_TYPE + COMMA_SEP +
	    "PRIMARY KEY (" + CacheEntry.COLUMN_ARRIVAL + "))";
	
	public static final String SQL_CREATE_LOG_ENTRIES =
		    "CREATE TABLE IF NOT EXISTS " + LogEntry.TABLE_NAME + "(" +
		    LogEntry.COLUMN_ENTRY_ID + INTEGER_TYPE + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
		    LogEntry.COLUMN_USER_ID + TEXT_TYPE + COMMA_SEP +
		    LogEntry.COLUMN_EVENT + TEXT_TYPE + COMMA_SEP +
		    LogEntry.COLUMN_TIMESTAMP + LONG_TYPE + ")";
	
	public enum logComponents {
	    MAIN, WEEK_ARCHIVE, DAY_ARCHIVE, CURRENT_LOC, LAST_PLACE, LATEST_JOURNEY, DAILY_ITIN, WEEKLY_ITIN, MOST_VISITED,
	    AWESOME_CURRENT_LOC, AWESOME_LAST_PLACE, AWESOME_LATEST_JOURNEY, AWESOME_DAILY_ITIN,
	    AWESOME_WEEKLY_ITIN, AWESOME_MOST_VISITED
	}
	
	public static boolean newDataFetched = true;
	public static final String SQL_DELETE_DATA_ENTRIES = "DROP TABLE IF EXISTS " + CacheEntry.TABLE_NAME;
	public static final String SQL_DELETE_LOG_ENTRIES = "DROP TABLE IF EXISTS " + LogEntry.TABLE_NAME;
	
	public static CardListView mListView;	
	public static CardArrayAdapter mCardArrayAdapter;
	public static boolean feedLoading = false;
	public static boolean refreshed = false;
	public static int FEED_COUNTER = 0;
	public static int FEED_FETCH_NUMBER = 10;
	public static int ANIMATION_DELAY = 3000;
	public static Location myLocation;
	public static ProgressDialog progressDialog;
	public static int threadNumber = 0;
	public static int appVisible = 0;
	
	public static ArrayList<Card> cards;	
	public static MostVisitedPlacesCard mostVisitedCard;
	public static TodaysItineraryCard todaysItCard;
	public static WeeklyItineraryCard weeklyItCard;
	public static MyCurrentLocationCard myCurrentLocationCard;
	public static PastStopCard pastStopCard;
	public static CommuteCard commuteCard;
	
	public static LinkedList<Integer> weeklyPOI_ids = new LinkedList<Integer>();
	public static LinkedList<Location> weeklyLocations = new LinkedList<Location>();
	public static LinkedList<Date> weeklyArrivals = new LinkedList<Date>();
	public static LinkedList<Date> weeklyDepartures = new LinkedList<Date>();

	public final static String SELECTED_MONTH = "dk.dtu.imm.sensible.SELECTED_MONTH";
	public final static String SELECTED_YEAR = "dk.dtu.imm.sensible.SELECTED_YEAR";
	public final static String STOP_LOCATIONS = "dk.dtu.imm.sensible.STOP_LOCATIONS";
    public final static String STOP_LOCATIONS_ARRIVALS = "dk.dtu.imm.sensible.STOP_LOCATIONS_ARRIVALS";
    public final static String STOP_LOCATIONS_DEPARTURES = "dk.dtu.imm.sensible.STOP_LOCATIONS_DEPARTURES";
    public final static String DAYS = "dk.dtu.imm.sensible.DAYS";
    public final static String POIs = "dk.dtu.imm.sensible.POIs";
    public final static String TRIP_DETAILS = "dk.dtu.imm.sensible.TRIP_DETAILS";
    public final static String MY_LOCATION = "dk.dtu.imm.sensible.MY_LOCATION";
    public final static String PAST_LOCATION = "dk.dtu.imm.sensible.PAST_LOCATION";
    public final static String PAST_LOCATION_TIME_SPENT = "dk.dtu.imm.sensible.PAST_LOCATION_TIME_SPENT";
    public final static String SELECTED_DAY = "dk.dtu.imm.sensible.SELECTED_DAY";
    public final static String SELECTED_WEEK = "dk.dtu.imm.sensible.SELECTED_WEEK";
    public final static String MOST_VISITED_POIs = "dk.dtu.imm.sensible.MOST_VISITED_POIs";
    public final static String MOST_VISITED_POIs_DURATIONS = "dk.dtu.imm.sensible.MOST_VISITED_POIs_DURATIONS";
    public final static String MOST_VISITED_POIs_LOCATIONS = "dk.dtu.imm.sensible.MOST_VISITED_POIs_LOCATIONS";
    public final static String DISTANCE = "dk.dtu.imm.sensible.DISTANCE";
    public final static String SPEED = "dk.dtu.imm.sensible.SPEED";
    public final static String VEHICLE = "dk.dtu.imm.sensible.VEHICLE";
    
    public static int NO_OF_MAX_MOST_VISITED = 10;
    public static int NO_OF_CURR_MOST_VISITED = 3;
    public static int NO_OF_MIN_MOST_VISITED = 3;
    public static int THUMB_WIDTH = 0;
    public static int THUMB_HEIGHT = 0;
    public static int DETAILS_HEIGHT = 195;
    public static String CURR_WEEK = "";
    public static String CURR_YEAR = "";
    public static String FEED_WEEK = "";
    public static String FEED_YEAR = "";
    public static boolean firstRun = true;
    public static String tmpWeekNumber = "";
    public static String tmpYearNumber = "";
    
    public static int index = 0;
    public static int top = 0;
    
    public static float[] markerColors = {BitmapDescriptorFactory.HUE_GREEN,
    									  BitmapDescriptorFactory.HUE_AZURE,
    									  BitmapDescriptorFactory.HUE_RED,
    									  BitmapDescriptorFactory.HUE_BLUE,
    									  BitmapDescriptorFactory.HUE_CYAN,
    									  BitmapDescriptorFactory.HUE_MAGENTA,
    									  BitmapDescriptorFactory.HUE_ORANGE,
    									  BitmapDescriptorFactory.HUE_RED,
    									  BitmapDescriptorFactory.HUE_ROSE,
    									  BitmapDescriptorFactory.HUE_VIOLET,
    									  BitmapDescriptorFactory.HUE_YELLOW};
    
    public static LinkedList<Integer> POI_id;
    public static LinkedList<Location> locations;			
    public static LinkedList<Date> arrivals;
    public static LinkedList<Date> departures;
    public static LinkedList<String> days;
	
}
