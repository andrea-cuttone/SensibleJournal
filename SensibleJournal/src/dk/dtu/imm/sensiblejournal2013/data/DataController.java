package dk.dtu.imm.sensiblejournal2013.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dk.dtu.imm.sensiblejournal2013.data.CacheDatabaseContract.CacheEntry;
import dk.dtu.imm.sensiblejournal2013.login.AuthActivity;
import dk.dtu.imm.sensiblejournal2013.login.RegistrationHandler;
import dk.dtu.imm.sensiblejournal2013.usageLog.LogDbHelper;
import dk.dtu.imm.sensiblejournal2013.utilities.Constants;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

public class DataController {
	private static final String LAST_UPLOADED_TIMESTAMP = "LAST_UPLOADED_TIMESTAMP";
	private static final String BASE_URL_FETCH = "https://www.sensible.dtu.dk/sensible-dtu/connectors/connector_answer/v1/stops_question/stops_answer/";
	private static final String BASE_URL_UPLOAD = "https://sensible.dtu.dk/sensible-dtu/connectors/connector_funf/usage/";
	private Context context;
	
	public DataController(Context context) {
		this.context = context;		
	}
	
	////////METHOD THAT FETCHES USER'S DATA FROM THE SENSIBLE DTU SERVER //////////
	@SuppressWarnings("deprecation")
	public void getDataFromServer() throws IOException, JSONException {
				
		// Fetching new data, reset feed counter and set list as refreshed
		Constants.FEED_COUNTER = 1;
		Constants.refreshed = true;
		
		String token = AuthActivity.getSystemPrefs(context).getString(RegistrationHandler.PROPERTY_SENSIBLE_TOKEN, "");		
		int pageCounter = 0;
								
		// Source 1: http://simpleprogrammer.com/2011/06/04/oauth-and-rest-in-android-part-2/
		// Source 2: http://www.makeurownrules.com/secure-rest-web-service-mobile-application-android.html#.U2uA7_mSxlc
		// Source 3: Sensible Journal V1 (Andrea Cuttone)		
		
		// Get an SSL secured HTTP client from the HttpUtils
		Constants.httpClient = HttpUtils.getNewHttpClient();
						
		String requestUrl = BASE_URL_FETCH + "?bearer_token="+ token + "&page=" + pageCounter;				
		HttpGet httpGet = new HttpGet(requestUrl);
		httpGet.setHeader("content-type", "application/json");
		HttpResponse response = Constants.httpClient.execute(httpGet);
		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));									
		String json = reader.readLine();
		pageCounter++;
		
		while (true) {
			requestUrl = BASE_URL_FETCH + "?bearer_token="+ token + "&page=" + pageCounter;
			httpGet = new HttpGet(requestUrl);
			httpGet.setHeader("content-type", "application/json");			
			response = Constants.httpClient.execute(httpGet);
			reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			String jsonNext = reader.readLine();
			if (jsonNext.equals("[]")) break;
			else {
				json = json.replace("]", ", ");
				jsonNext = jsonNext.replace("[", "");
				json = json + jsonNext;
				pageCounter++;
			}							
		}
		
        String resultJson = "{\"results\": " + json + "}";      
        JSONObject jObject = new JSONObject(resultJson);
		JSONArray finalResult = jObject.getJSONArray("results");		
		context.deleteDatabase(Constants.DATA_DB_FILENAME);
		CacheDbHelper mDbHelper = new CacheDbHelper(context);
		
		// Gets the data repository in write mode
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
		int dayCounter = 0;
		String currDay = "";
		for(int i = finalResult.length()-1; i > -1; i--) {
			
			int POI_id = finalResult.getJSONObject(i).getInt("label");
			float latitude = (float) finalResult.getJSONObject(i).getDouble("lat");
			float longtitude = (float) finalResult.getJSONObject(i).getDouble("lon");
			long arrival = finalResult.getJSONObject(i).getLong("arrival") * 1000;
			long departure = finalResult.getJSONObject(i).getLong("departure") * 1000;
						
			Date tmpDateArr = new Date(arrival);
			Date tmpDateDep = new Date(departure);			
			
			if (!currDay.equals(tmpDateDep.toString().substring(0, 10) + " " + Integer.toString(tmpDateDep.getYear()+1900))) {
				dayCounter++;
				currDay = tmpDateDep.toString().substring(0, 10) + " " + Integer.toString(tmpDateDep.getYear()+1900);
			}			
			
			// Create a new map of values, where column names are the keys
			ContentValues values = new ContentValues();
			values.put(CacheEntry.COLUMN_POI_ID, POI_id);
			values.put(CacheEntry.COLUMN_DAY_NUMBER, dayCounter);
			values.put(CacheEntry.COLUMN_DAY, currDay);
			values.put(CacheEntry.COLUMN_LATITUDE, latitude);
			values.put(CacheEntry.COLUMN_LONGTITUDE, longtitude);
			values.put(CacheEntry.COLUMN_ARRIVAL, arrival);
			values.put(CacheEntry.COLUMN_DEPARTURE, departure);
			
			// Insert the new row
	        db.replace(CacheEntry.TABLE_NAME, null, values);
			
			// If an arrival an a departure have a different day, include the entries twice
			// so that they will be used as the start point of the next day
			if (tmpDateArr.getDay() != tmpDateDep.getDay()) {				
				values = new ContentValues();
				String tmpDay = tmpDateArr.toString().substring(0, 10) + " " + Integer.toString(tmpDateArr.getYear()+1900);
				values.put(CacheEntry.COLUMN_POI_ID, POI_id);
				values.put(CacheEntry.COLUMN_DAY_NUMBER, dayCounter+1);
				values.put(CacheEntry.COLUMN_DAY, tmpDay);
				values.put(CacheEntry.COLUMN_LATITUDE, latitude);
				values.put(CacheEntry.COLUMN_LONGTITUDE, longtitude);
				values.put(CacheEntry.COLUMN_ARRIVAL, arrival);
				values.put(CacheEntry.COLUMN_DEPARTURE, departure);
		
				// Insert the new row
		        db.replace(CacheEntry.TABLE_NAME, null, values);
			}                			     
		}
		db.close(); 
	}

	////////METHOD THAT UPLOADS USER'S USAGE LOG DATA TO THE SENSIBLE DTU SERVER //////////
	public void uploadUsageLog() throws IOException {

		SharedPreferences sharedPrefs = context.getSharedPreferences("com.sensibleDTU.settings", android.content.Context.MODE_PRIVATE);
		Long lastUploadedTimestamp = sharedPrefs.getLong(LAST_UPLOADED_TIMESTAMP, 0);
		
		LogDbHelper logDbHelper = new LogDbHelper(context);

		// Gets the data repository in read mode
		SQLiteDatabase db = logDbHelper.getReadableDatabase();

		// 1. Get the total time in application
		String query = "SELECT * FROM application_log WHERE timestamp > ?";
		String[] sqlParams = {lastUploadedTimestamp.toString()};
		Cursor c = db.rawQuery(query, sqlParams);
		c.moveToFirst();
		
		StringBuffer eventBuffer = new StringBuffer();
		eventBuffer.append("[");
		while (!c.isAfterLast()) {
			String event = c.getString(1);
			lastUploadedTimestamp = c.getLong(2);
			eventBuffer.append(String.format("[%d,\"%s\"],", lastUploadedTimestamp, event));
			c.moveToNext();
		}
		db.close();
		if (eventBuffer.length() == 1) {
			Log.d("Usage log", "No new log entries");
			return;
		}
		eventBuffer = eventBuffer.replace(eventBuffer.length() - 1, eventBuffer.length(), "]");
		
		// just upload the raw events
		String token = AuthActivity.getSystemPrefs(context).getString(RegistrationHandler.PROPERTY_SENSIBLE_TOKEN, "");
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
        nameValuePairs.add(new BasicNameValuePair("bearer_token", token));
        nameValuePairs.add(new BasicNameValuePair("appid", "sensiblejournal"));
        nameValuePairs.add(new BasicNameValuePair("events", eventBuffer.toString()));			
		
		// Get an SSL secured HTTP client from the HttpUtils
		Constants.httpClient = HttpUtils.getNewHttpClient();
		HttpPost httpPost = new HttpPost(BASE_URL_UPLOAD);
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		HttpResponse response = Constants.httpClient.execute(httpPost);
		if (response.getStatusLine().getStatusCode() == org.apache.http.HttpStatus.SC_OK) {
			Log.d(Constants.APP_NAME, "Usage log uploaded successfully");
			Editor editor = sharedPrefs.edit();
			editor.putLong(LAST_UPLOADED_TIMESTAMP, lastUploadedTimestamp);
			editor.commit();
		} else {
			Log.e(Constants.APP_NAME, "Usage log failed:" + response.getStatusLine());
		}
		
	}
	
	////////METHOD THAT READ'S THE USER'S MOVEMENT DATA FROM THE SQLITE DATABASE //////////
	@SuppressWarnings("deprecation")
	public void getDataFromCache(int dayNumber, boolean gitFirstEntry,boolean getLastJourney, boolean getFirstWeek, boolean getAll,  
								LinkedList<Integer> POI_id,	LinkedList<Location> locations,	LinkedList<Date> arrivals,
																LinkedList<Date> departures, LinkedList<String> days) {
		
		POI_id.clear();
		locations.clear();
		arrivals.clear();
		departures.clear();
		if (days != null) days.clear();
		
		// Gets the data repository in read mode
		CacheDbHelper mDbHelper = new CacheDbHelper(context);				
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		
		///////////////// ONLY GET FIRST WEEK ////////////////////
		if(getFirstWeek) {
			LinkedList<Integer> tmpPOI_id = new LinkedList<Integer>();
			LinkedList<Location> tmpLocations = new LinkedList<Location>();
			LinkedList<Date> tmpArrivals = new LinkedList<Date>();
			LinkedList<Date> tmpDepartures = new LinkedList<Date>();
			List<Integer> indices = new LinkedList<Integer>();
			
			String query = "SELECT * FROM daily_entries WHERE " + CacheEntry.COLUMN_DAY_NUMBER + "=1"
																+ " OR " + CacheEntry.COLUMN_DAY_NUMBER + "=2"
																+ " OR " + CacheEntry.COLUMN_DAY_NUMBER + "=3"
																+ " OR " + CacheEntry.COLUMN_DAY_NUMBER + "=4"
																+ " OR " + CacheEntry.COLUMN_DAY_NUMBER + "=5"
																+ " OR " + CacheEntry.COLUMN_DAY_NUMBER + "=6"
																+ " OR " + CacheEntry.COLUMN_DAY_NUMBER + "=7"
																+ " ORDER BY " + CacheEntry.COLUMN_DEPARTURE + " DESC";
			
			Cursor c = db.rawQuery(query, null);
			c.moveToFirst();
			
			Location location;
			Date tmpDateDep;
			Date tmpDateArr;
			boolean firstLoop = true;
			int weekBuff = 0;		
			Calendar cal;
			
			while (!c.isAfterLast()) {
				tmpPOI_id.add(Integer.parseInt(c.getString(1)));		   		   
				location = new Location("temporary");
				location.setLatitude(c.getFloat(4));
				location.setLongitude(c.getFloat(5));
				tmpLocations.add(location);
				tmpDateArr = new Date(c.getLong(6));
				tmpDateDep = new Date(c.getLong(7));
				tmpArrivals.add(tmpDateArr);
				tmpDepartures.add(tmpDateDep);
								
				// Get the week's number
				cal = Calendar.getInstance();
				cal.setTime(tmpDateDep);
				if (tmpDateDep.toString().substring(0, 3).equals("Sun")) cal.add(Calendar.DAY_OF_YEAR, -1);
				
		       	if(firstLoop) {	        		
					weekBuff = cal.get(Calendar.WEEK_OF_YEAR);
		           	firstLoop = false;
		        }
		       	
		       	// If the day number is 0 (Sunday), we got the first week, remove the Sunday's added
		       	// entries from the lists and break
		       	if (cal.get(Calendar.WEEK_OF_YEAR) != weekBuff) {
		       		tmpPOI_id.removeLast();
		       		tmpLocations.removeLast();
		       		tmpArrivals.removeLast();
		       		tmpDepartures.removeLast();
		       		break;	        	
		       	}
		       	c.moveToNext();
			}
			
			c.close();
			db.close();
			
			// Remove consequent duplicates from the lists //
			for (int i = 1; i < tmpLocations.size(); i++) {
				if ((tmpLocations.get(i).getLatitude() == tmpLocations.get(i - 1).getLatitude())
						&& (tmpLocations.get(i).getLongitude() == tmpLocations.get(i - 1).getLongitude()))
					indices.add(i);
			}

			for (int i = 0; i < tmpLocations.size(); i++) {
				if (indices.contains(i))
					continue;
				else {
					POI_id.add(tmpPOI_id.get(i));
					locations.add(tmpLocations.get(i));
					arrivals.add(tmpArrivals.get(i));
					departures.add(tmpDepartures.get(i));
				}
			}
		}
		//////////////////////////////////////////////////////////
		
		/////////////////// GET EVERYTHING ///////////////////////
		else if(getAll) {
			LinkedList<Integer> tmpPOI_id = new LinkedList<Integer>();
			LinkedList<String> tmpDays = new LinkedList<String>();
			LinkedList<Location> tmpLocations = new LinkedList<Location>();
			LinkedList<Date> tmpArrivals = new LinkedList<Date>();
			LinkedList<Date> tmpDepartures = new LinkedList<Date>();
			List<Integer> indices = new LinkedList<Integer>();
			
			String query = "SELECT * FROM daily_entries ORDER BY " + CacheEntry.COLUMN_DEPARTURE + " DESC";
			Cursor c = db.rawQuery(query, null);
			c.moveToFirst();
			
			Location location;
			Date tmpDateDep;
			Date tmpDateArr;						
			
			while (!c.isAfterLast()) {
				tmpPOI_id.add(Integer.parseInt(c.getString(1)));
				tmpDays.add(c.getString(3));
				location = new Location("temporary");
				location.setLatitude(c.getFloat(4));
				location.setLongitude(c.getFloat(5));
				tmpLocations.add(location);
				tmpDateArr = new Date(c.getLong(6));
				tmpDateDep = new Date(c.getLong(7));
				tmpArrivals.add(tmpDateArr);
				tmpDepartures.add(tmpDateDep);								
				c.moveToNext();
			}
			
			c.close();
			db.close();
			
			// For the Most Visited Places card
			if (days == null) {
				// Remove consequent duplicates from the lists //
				for (int i = 1; i < tmpLocations.size(); i++) {
					if ((tmpLocations.get(i).getLatitude() == tmpLocations.get(i - 1).getLatitude())
						&& (tmpLocations.get(i).getLongitude() == tmpLocations.get(i - 1).getLongitude()))
						indices.add(i);
				}
	
				for (int i = 0; i < tmpLocations.size(); i++) {
					if (indices.contains(i))
						continue;
					else {
						POI_id.add(tmpPOI_id.get(i));
						locations.add(tmpLocations.get(i));
						arrivals.add(tmpArrivals.get(i));
						departures.add(tmpDepartures.get(i));
					}
				}
			}
			// For the archive creation
			else {
				for (int i = 0; i < tmpLocations.size(); i++) {
					POI_id.add(tmpPOI_id.get(i));
					days.add(tmpDays.get(i));
					locations.add(tmpLocations.get(i));
					arrivals.add(tmpArrivals.get(i));
					departures.add(tmpDepartures.get(i));
				}		
			}
		}
		//////////////////////////////////////////////////////////
		
		/////////////// ONLY GET LATEST JOURNEY //////////////////
		else if(getLastJourney) {
			
			String query = "SELECT * FROM daily_entries WHERE " + CacheEntry.COLUMN_ENTRY_ID + "=1";
			Cursor c = db.rawQuery(query, null);
			c.moveToFirst();
			
			Location location;
			Date tmpDateDep;
			Date tmpDateArr;
			c.moveToFirst();
			
			if (c.getCount() > 0) {
				POI_id.add(Integer.parseInt(c.getString(1)));
				location = new Location("temporary");
				location.setLatitude(c.getFloat(4));
				location.setLongitude(c.getFloat(5));
				locations.add(location);
				tmpDateArr = new Date(c.getLong(6));
				tmpDateDep = new Date(c.getLong(7));
				arrivals.add(tmpDateArr);
				departures.add(tmpDateDep);			
				
				if(tmpDateArr.getDay() != tmpDateDep.getDay()){
					query = "SELECT * FROM daily_entries WHERE " + CacheEntry.COLUMN_ENTRY_ID + "=3";
				}
				else {
					query = "SELECT * FROM daily_entries WHERE " + CacheEntry.COLUMN_ENTRY_ID + "=2";
				}
				
				c = db.rawQuery(query, null);
				c.moveToFirst();
				
				POI_id.add(Integer.parseInt(c.getString(1)));
				location = new Location("temporary");
				location.setLatitude(c.getFloat(4));
				location.setLongitude(c.getFloat(5));
				locations.add(location);
				tmpDateArr = new Date(c.getLong(6));
				tmpDateDep = new Date(c.getLong(7));
				arrivals.add(tmpDateArr);
				departures.add(tmpDateDep);
			}
			
			c.close();
			db.close();
		}
		//////////////////////////////////////////////////////////
		
		//////////////// ONLY GET FIRST ENTRY ////////////////////
		else if(gitFirstEntry) {
			String query = "SELECT * FROM daily_entries WHERE " + CacheEntry.COLUMN_ENTRY_ID + "=1"
																	+ " ORDER BY " + CacheEntry.COLUMN_DEPARTURE + " DESC";
			
			Cursor c = db.rawQuery(query, null);
			c.moveToFirst();
			
			Location location;
			Date tmpDateDep;
			Date tmpDateArr;
			c.moveToFirst();
			
			if (c.getCount() > 0) {
				POI_id.add(Integer.parseInt(c.getString(1)));
				location = new Location("temporary");
				location.setLatitude(c.getFloat(4));
				location.setLongitude(c.getFloat(5));
				locations.add(location);
				tmpDateArr = new Date(c.getLong(6));
				tmpDateDep = new Date(c.getLong(7));
				arrivals.add(tmpDateArr);
				departures.add(tmpDateDep);
			}
			
			c.close();
			db.close();
		}
		//////////////////////////////////////////////////////////
		
		////////////////// GET SPECIFIC DAY //////////////////////
		else {
			String query = "SELECT * FROM daily_entries WHERE " + CacheEntry.COLUMN_DAY_NUMBER + "=" + dayNumber
																	+ " ORDER BY " + CacheEntry.COLUMN_DEPARTURE + " DESC";
			
			Cursor c = db.rawQuery(query, null);
			c.moveToFirst();
			
			Location location;
			Date tmpDateDep;
			Date tmpDateArr;			
			c.moveToFirst();
			
			while (!c.isAfterLast()) {
				POI_id.add(Integer.parseInt(c.getString(1)));
				days.add(c.getString(3));
				location = new Location("temporary");
				location.setLatitude(c.getFloat(4));
				location.setLongitude(c.getFloat(5));
				locations.add(location);
				tmpDateArr = new Date(c.getLong(6));
				tmpDateDep = new Date(c.getLong(7));
				arrivals.add(tmpDateArr);
				departures.add(tmpDateDep);								
				c.moveToNext();
			}
			
			c.close();
			db.close();
		}
		//////////////////////////////////////////////////////////
	}
}
