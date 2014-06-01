package dk.dtu.imm.sensiblejournal2013.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dk.dtu.imm.sensiblejournal2013.data.CacheDatabaseContract.CacheEntry;
import dk.dtu.imm.sensiblejournal2013.usageLog.LogDbHelper;
import dk.dtu.imm.sensiblejournal2013.utilities.AppFunctions;
import dk.dtu.imm.sensiblejournal2013.utilities.Constants;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

public class DataController {
	//private static final String BASE_URL = "https://www.sensible.dtu.dk/sensible-dtu/connectors/connector_raw/v1/location/";
	private static final String BASE_URL_FETCH = "http://raman.imm.dtu.dk:8083/andrea/sensible-dtu/connectors/connector_answer/v1/stops_question/stops_answer/";
	private static final String BASE_URL_UPLOAD = "http://raman.imm.dtu.dk:8083/andrea/sensible-dtu/connectors/connector_funf/usage/";
	private Context context;
	private AppFunctions functions;
	
	public DataController(Context context) {
		this.context = context;		
	}
	
	////////METHOD THAT FETCHES USER'S DATA FROM THE SENSIBLE DTU SERVER //////////
	public void getDataFromServer() throws IOException, JSONException {
				
		// Fetching new data, reset feed counter and set list as refreshed
		Constants.FEED_COUNTER = 0;
		Constants.refreshed = true;
		
		//String token = AuthActivity.getSystemPrefs(context).getString(RegistrationHandler.PROPERTY_SENSIBLE_TOKEN, "");		
		//Log.d(GlobalVariables.APP_NAME, String.format("Token: " + token));
		int pageCounter = 0;
		String token = "8c093d0a2b563a89ab43290f9a47f2";
								
		// Source 1: http://simpleprogrammer.com/2011/06/04/oauth-and-rest-in-android-part-2/
		// Source 2: http://www.makeurownrules.com/secure-rest-web-service-mobile-application-android.html#.U2uA7_mSxlc
		// Source 3: Sensible Journal V1 (Andrea Cuttone)
		
		//?bearer_token=8c093d0a2b563a89ab43290f9a47f2&page=1
		
		// Get an SSL secured HTTP client from the HttpUtils
		Constants.httpClient = HttpUtils.getNewHttpClient();
		/*				
		String requestUrl = BASE_URL + "?bearer_token="+ token + "&page=" + pageCounter;				
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
			response = httpClient.execute(httpGet);
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
        */
		String json2 = "{\"results\": [{\"arrival\": 1384012800, \"lat\": 55.6961945, \"lon\": 12.443706500000001, \"departure\": 1384082100, \"label\": 2}, {\"arrival\": 1384083000, \"lat\": 55.6962153, \"lon\": 12.4458983, \"departure\": 1384084800, \"label\": 8}, {\"arrival\": 1384085700, \"lat\": 55.6961945, \"lon\": 12.443706800000001, \"departure\": 1384166700, \"label\": 2}, {\"arrival\": 1384168500, \"lat\": 55.68998915, \"lon\": 12.45545035, \"departure\": 1384169400, \"label\": 40}, {\"arrival\": 1384171200, \"lat\": 55.6984167, \"lon\": 12.44270195, \"departure\": 1384172100, \"label\": 67}, {\"arrival\": 1384173900, \"lat\": 55.7024788, \"lon\": 12.4375003, \"departure\": 1384174800, \"label\": 24}, {\"arrival\": 1384175700, \"lat\": 55.6961945, \"lon\": 12.4437066, \"departure\": 1384328700, \"label\": 2}, {\"arrival\": 1384331400, \"lat\": 55.7895187, \"lon\": 12.425067905, \"departure\": 1384336800, \"label\": 13}, {\"arrival\": 1384338600, \"lat\": 55.78947105, \"lon\": 12.42577665, \"departure\": 1384357500, \"label\": 13}, {\"arrival\": 1384360200, \"lat\": 55.6961944, \"lon\": 12.443707, \"departure\": 1384436700, \"label\": 2}, {\"arrival\": 1384439400, \"lat\": 55.6961946, \"lon\": 12.4437069, \"departure\": 1384499700, \"label\": 2}, {\"arrival\": 1384502400, \"lat\": 55.6961945, \"lon\": 12.443706500000001, \"departure\": 1384516800, \"label\": 2}, {\"arrival\": 1384520400, \"lat\": 55.6961554, \"lon\": 12.4437073, \"departure\": 1384553700, \"label\": 2}, {\"arrival\": 1384554600, \"lat\": 55.6880093, \"lon\": 12.4603076, \"departure\": 1384556400, \"label\": 48}, {\"arrival\": 1384557300, \"lat\": 55.6961945, \"lon\": 12.4437064, \"departure\": 1384595100, \"label\": 2}, {\"arrival\": 1384596000, \"lat\": 55.6879179625, \"lon\": 12.4601860475, \"departure\": 1384596900, \"label\": 48}, {\"arrival\": 1384598700, \"lat\": 55.68341245, \"lon\": 12.4655139, \"departure\": 1384603200, \"label\": 73}, {\"arrival\": 1384604100, \"lat\": 55.68376055, \"lon\": 12.4697839, \"departure\": 1384605000, \"label\": 37}, {\"arrival\": 1384605900, \"lat\": 55.6820069, \"lon\": 12.47508105, \"departure\": 1384606800, \"label\": 16}, {\"arrival\": 1384612200, \"lat\": 55.6961941, \"lon\": 12.4437084, \"departure\": 1384635600, \"label\": 2}, {\"arrival\": 1384637400, \"lat\": 55.6913298, \"lon\": 12.4448557, \"departure\": 1384646400, \"label\": 65}, {\"arrival\": 1384647300, \"lat\": 55.6933767, \"lon\": 12.4475782, \"departure\": 1384650900, \"label\": 47}, {\"arrival\": 1384651800, \"lat\": 55.6961945, \"lon\": 12.443706500000001, \"departure\": 1384688700, \"label\": 2}, {\"arrival\": 1384689600, \"lat\": 55.69479334, \"lon\": 12.441908245, \"departure\": 1384695000, \"label\": 50}, {\"arrival\": 1384695900, \"lat\": 55.6961945, \"lon\": 12.4437066, \"departure\": 1384772400, \"label\": 2}, {\"arrival\": 1384776000, \"lat\": 55.7819663, \"lon\": 12.4216795, \"departure\": 1384787700, \"label\": 45}, {\"arrival\": 1384788600, \"lat\": 55.76980893, \"lon\": 12.42324041, \"departure\": 1384789500, \"label\": 46}, {\"arrival\": 1384791300, \"lat\": 55.69616715, \"lon\": 12.4437063, \"departure\": 1384849800, \"label\": 2}, {\"arrival\": 1384851600, \"lat\": 55.7005657, \"lon\": 12.438314700000001, \"departure\": 1384852500, \"label\": 74}, {\"arrival\": 1384856100, \"lat\": 55.785565, \"lon\": 12.41870125, \"departure\": 1384867800, \"label\": 1}, {\"arrival\": 1384870500, \"lat\": 55.70108195, \"lon\": 12.43869765, \"departure\": 1384871400, \"label\": 4}, {\"arrival\": 1384874100, \"lat\": 55.6961945, \"lon\": 12.44370905, \"departure\": 1384933500, \"label\": 2}, {\"arrival\": 1384936200, \"lat\": 55.78948625, \"lon\": 12.425020400000001, \"departure\": 1384949700, \"label\": 13}, {\"arrival\": 1384953300, \"lat\": 55.7026083, \"lon\": 12.437788000000001, \"departure\": 1384954200, \"label\": 24}, {\"arrival\": 1384960500, \"lat\": 55.6960997, \"lon\": 12.443703000000001, \"departure\": 1384976700, \"label\": 2}, {\"arrival\": 1384977600, \"lat\": 55.6913533, \"lon\": 12.4448136, \"departure\": 1384979400, \"label\": 65}, {\"arrival\": 1384982100, \"lat\": 55.6774076, \"lon\": 12.4696143, \"departure\": 1384989300, \"label\": 44}, {\"arrival\": 1384991100, \"lat\": 55.6817469, \"lon\": 12.48489005, \"departure\": 1385001000, \"label\": 52}, {\"arrival\": 1385002800, \"lat\": 55.6960987, \"lon\": 12.4437026, \"departure\": 1385044200, \"label\": 2}, {\"arrival\": 1385046900, \"lat\": 55.6960987, \"lon\": 12.4437046, \"departure\": 1385109000, \"label\": 2}, {\"arrival\": 1385111700, \"lat\": 55.68965675, \"lon\": 12.455991985, \"departure\": 1385112600, \"label\": 60}, {\"arrival\": 1385113500, \"lat\": 55.696467, \"lon\": 12.45082605, \"departure\": 1385114400, \"label\": 23}, {\"arrival\": 1385116200, \"lat\": 55.69611735, \"lon\": 12.443718950000001, \"departure\": 1385118900, \"label\": 2}, {\"arrival\": 1385120700, \"lat\": 55.6845387, \"lon\": 12.46966155, \"departure\": 1385125200, \"label\": 20}, {\"arrival\": 1385128800, \"lat\": 55.7062021, \"lon\": 12.4500635, \"departure\": 1385129700, \"label\": 9}, {\"arrival\": 1385130600, \"lat\": 55.6960985, \"lon\": 12.443702700000001, \"departure\": 1385189100, \"label\": 2}, {\"arrival\": 1385190900, \"lat\": 55.6728851, \"lon\": 12.466243500000001, \"departure\": 1385191800, \"label\": 54}, {\"arrival\": 1385198100, \"lat\": 54.76703175, \"lon\": 11.776021100000001, \"departure\": 1385199000, \"label\": 32}, {\"arrival\": 1385199900, \"lat\": 54.7680166, \"lon\": 11.748016100000001, \"departure\": 1385214300, \"label\": 6}, {\"arrival\": 1385216100, \"lat\": 54.7666342575, \"lon\": 11.7774383, \"departure\": 1385217000, \"label\": 34}, {\"arrival\": 1385225100, \"lat\": 55.69615135, \"lon\": 12.4436841, \"departure\": 1385226000, \"label\": 2}, {\"arrival\": 1385229600, \"lat\": 55.786425375, \"lon\": 12.42554425, \"departure\": 1385255700, \"label\": 12}, {\"arrival\": 1385258400, \"lat\": 55.6960985, \"lon\": 12.4437019, \"departure\": 1385281800, \"label\": 2}, {\"arrival\": 1385283600, \"lat\": 55.65549495, \"lon\": 12.436138525, \"departure\": 1385291700, \"label\": 10}, {\"arrival\": 1385293500, \"lat\": 55.66172351, \"lon\": 12.427674, \"departure\": 1385294400, \"label\": 69}, {\"arrival\": 1385295300, \"lat\": 55.65525105, \"lon\": 12.43675, \"departure\": 1385296200, \"label\": 10}, {\"arrival\": 1385299800, \"lat\": 55.6960986, \"lon\": 12.443703000000001, \"departure\": 1385368200, \"label\": 2}, {\"arrival\": 1385369100, \"lat\": 55.6920136, \"lon\": 12.444607105000001, \"departure\": 1385370900, \"label\": 55}, {\"arrival\": 1385371800, \"lat\": 55.6961125, \"lon\": 12.4438014, \"departure\": 1385378100, \"label\": 2}, {\"arrival\": 1385379000, \"lat\": 55.6884235, \"lon\": 12.4680023, \"departure\": 1385388000, \"label\": 3}, {\"arrival\": 1385388900, \"lat\": 55.68729985, \"lon\": 12.4624842, \"departure\": 1385389800, \"label\": 71}, {\"arrival\": 1385390700, \"lat\": 55.69051415, \"lon\": 12.45476375, \"departure\": 1385391600, \"label\": 61}, {\"arrival\": 1385393400, \"lat\": 55.706053625, \"lon\": 12.4498365, \"departure\": 1385394300, \"label\": 9}, {\"arrival\": 1385395200, \"lat\": 55.6961407, \"lon\": 12.4437517, \"departure\": 1385451000, \"label\": 2}, {\"arrival\": 1385457300, \"lat\": 55.78549685, \"lon\": 12.4195437, \"departure\": 1385466300, \"label\": 30}, {\"arrival\": 1385473500, \"lat\": 55.687562, \"lon\": 12.471255900000001, \"departure\": 1385480700, \"label\": 56}, {\"arrival\": 1385482500, \"lat\": 55.6961199, \"lon\": 12.4437573, \"departure\": 1385551800, \"label\": 2}, {\"arrival\": 1385552700, \"lat\": 55.6958853, \"lon\": 12.446401400000001, \"departure\": 1385555400, \"label\": 8}, {\"arrival\": 1385556300, \"lat\": 55.6961299, \"lon\": 12.4437526, \"departure\": 1385634600, \"label\": 2}, {\"arrival\": 1385636400, \"lat\": 55.6891225, \"lon\": 12.4584665, \"departure\": 1385638200, \"label\": 17}, {\"arrival\": 1385639100, \"lat\": 55.6961357, \"lon\": 12.443707525, \"departure\": 1385676900, \"label\": 2}, {\"arrival\": 1385677800, \"lat\": 55.6960996, \"lon\": 12.4437002, \"departure\": 1385814600, \"label\": 2}, {\"arrival\": 1385815500, \"lat\": 55.7089344, \"lon\": 12.4382354, \"departure\": 1385833500, \"label\": 76}, {\"arrival\": 1385835300, \"lat\": 55.6961216, \"lon\": 12.4436999, \"departure\": 1385889300, \"label\": 2}, {\"arrival\": 1385891100, \"lat\": 55.7011193, \"lon\": 12.4388158, \"departure\": 1385894700, \"label\": 4}, {\"arrival\": 1385896500, \"lat\": 55.6961145, \"lon\": 12.44370115, \"departure\": 1385980200, \"label\": 2}, {\"arrival\": 1385984700, \"lat\": 55.7819595, \"lon\": 12.4216443, \"departure\": 1385999100, \"label\": 45}, {\"arrival\": 1386000900, \"lat\": 55.696154, \"lon\": 12.443698900000001, \"departure\": 1386064800, \"label\": 2}, {\"arrival\": 1386068400, \"lat\": 55.7855499, \"lon\": 12.4185123, \"departure\": 1386081000, \"label\": 1}, {\"arrival\": 1386082800, \"lat\": 55.7023882, \"lon\": 12.4377568, \"departure\": 1386083700, \"label\": 24}, {\"arrival\": 1386085500, \"lat\": 55.6961542, \"lon\": 12.4437008, \"departure\": 1386149400, \"label\": 2}, {\"arrival\": 1386153000, \"lat\": 55.789152175, \"lon\": 12.42567245, \"departure\": 1386157500, \"label\": 13}, {\"arrival\": 1386165600, \"lat\": 55.6961541, \"lon\": 12.4436993, \"departure\": 1386234900, \"label\": 2}, {\"arrival\": 1386236700, \"lat\": 55.681513195, \"lon\": 12.4344833, \"departure\": 1386237600, \"label\": 51}, {\"arrival\": 1386238500, \"lat\": 55.6961544, \"lon\": 12.443702700000001, \"departure\": 1386323100, \"label\": 2}, {\"arrival\": 1386326700, \"lat\": 55.6922565, \"lon\": 12.45210765, \"departure\": 1386329400, \"label\": 58}, {\"arrival\": 1386332100, \"lat\": 55.6866596, \"lon\": 12.458517025, \"departure\": 1386333000, \"label\": 35}, {\"arrival\": 1386334800, \"lat\": 55.6961377, \"lon\": 12.443736925, \"departure\": 1386418500, \"label\": 2}, {\"arrival\": 1386420300, \"lat\": 55.6890805, \"lon\": 12.4574294, \"departure\": 1386422100, \"label\": 68}, {\"arrival\": 1386423900, \"lat\": 55.696152325, \"lon\": 12.44370975, \"departure\": 1386594000, \"label\": 2}, {\"arrival\": 1386594900, \"lat\": 55.6946667, \"lon\": 12.44843325, \"departure\": 1386595800, \"label\": 21}, {\"arrival\": 1386596700, \"lat\": 55.6961505, \"lon\": 12.4436742, \"departure\": 1386675000, \"label\": 2}, {\"arrival\": 1386677700, \"lat\": 55.68014175, \"lon\": 12.46183255, \"departure\": 1386679500, \"label\": 43}, {\"arrival\": 1386683100, \"lat\": 55.696155275, \"lon\": 12.4437157, \"departure\": 1386748800, \"label\": 2}, {\"arrival\": 1386749700, \"lat\": 55.6934092, \"lon\": 12.44196235, \"departure\": 1386756000, \"label\": 59}, {\"arrival\": 1386759600, \"lat\": 55.6968512, \"lon\": 12.4436144, \"departure\": 1386760500, \"label\": 62}, {\"arrival\": 1386774000, \"lat\": 55.7061779, \"lon\": 12.449837200000001, \"departure\": 1386775800, \"label\": 9}]}";
		JSONObject jObject = new JSONObject(json2);
		//JSONObject jObject = new JSONObject(resultJson);
		JSONArray finalResult = jObject.getJSONArray("results");
				
		context.deleteDatabase(Constants.DATA_DB_FILENAME);
		CacheDbHelper mDbHelper = new CacheDbHelper(context);		
		// Gets the data repository in write mode
		SQLiteDatabase db = mDbHelper.getWritableDatabase();		
		for(int i = 0; i < finalResult.length(); i++) {
			
			int POI_id = finalResult.getJSONObject(i).getInt("label");
			float latitude = (float) finalResult.getJSONObject(i).getDouble("lat");
			float longtitude = (float) finalResult.getJSONObject(i).getDouble("lon");
			long arrival = finalResult.getJSONObject(i).getLong("arrival") * 1000;
			long departure = finalResult.getJSONObject(i).getLong("departure") * 1000;			
							
			// Create a new map of values, where column names are the keys
	        ContentValues values = new ContentValues();
	        values.put(CacheEntry.COLUMN_POI_ID, POI_id);
	        values.put(CacheEntry.COLUMN_LATITUDE, latitude);
	        values.put(CacheEntry.COLUMN_LONGTITUDE, longtitude);
	        values.put(CacheEntry.COLUMN_ARRIVAL, arrival);
	        values.put(CacheEntry.COLUMN_DEPARTURE, departure);
	                		
	        // Insert the new row
	        db.replace(CacheEntry.TABLE_NAME, null, values);
		}
		db.close(); 
	}

	////////METHOD THAT UPLOADS USER'S USAGE LOG DATA TO THE SENSIBLE DTU SERVER //////////
	public void uploadUsageLog() throws IOException {
		functions = new AppFunctions(context);
		String token = "8c093d0a2b563a89ab43290f9a47f2";
		List<String> event = new LinkedList<String>();
		List<Float> timestamp = new LinkedList<Float>();
		long totalTimeInApp = 0;
		long totalTimeInWeeklyArchive = 0;
		long totalTimeInDailyArchive = 0;
		long totalTimeInCurrentLoc = 0;
		long totalTimeInLastPlace = 0;
		long totalTimeInLatestJourney = 0;
		long totalTimeInDailyItin = 0;
		long totalTimeInWeeklyItin = 0;
		long totalTimeInMostVisited = 0;
		int clicksForWeeklyArchive = 0;
		int clicksForDailyArchive = 0;
		int clicksForCurrentLoc = 0;
		int clicksForLastPlace = 0;
		int clicksForLatestJourney = 0;
		int clicksForDailyItin = 0;
		int clicksForWeeklyItin = 0;
		int clicksForMostVisited = 0;
		int likesForCurrentLoc = 0;
		int likesForLastPlace = 0;
		int likesForLatestJourney = 0;
		int likesForDailyItin = 0;
		int likesForWeeklyItin = 0;
		int likesForMostVisited = 0;
		String finalTotalTimeInApp = "";
		String finalTotalTimeInWeeklyArchive = "";
		String finalTotalTimeInDailyArchive = "";
		String finalTotalTimeInCurrentLoc = "";
		String finalTotalTimeInLastPlace = "";
		String finalTotalTimeInLatestJourney = "";
		String finalTotalTimeInDailyItin = "";
		String finalTotalTimeInWeeklyItin = "";
		String finalTotalTimeInMostVisited = "";
		
		LogDbHelper logDbHelper = new LogDbHelper(context);

		// Gets the data repository in read mode
		SQLiteDatabase db = logDbHelper.getReadableDatabase();

		// 1. Get the total time in application
		String query = "SELECT * FROM application_log";

		Cursor c = db.rawQuery(query, null);
		c.moveToFirst();
		
		while (!c.isAfterLast()) {
			event.add(c.getString(1));
			timestamp.add(c.getFloat(2));
			c.moveToNext();
		}

		for (int i = 0; i < timestamp.size(); i++) {

			if (event.get(i).equals("TIME_IN_WEEK_ARCHIVE")) {
				clicksForWeeklyArchive++;
				if (timestamp.get(i) < 10000000) {
					totalTimeInApp += timestamp.get(i);
					totalTimeInWeeklyArchive += timestamp.get(i);
				}
			} else if (event.get(i).equals("TIME_IN_DAY_ARCHIVE")) {
				clicksForDailyArchive++;
				if (timestamp.get(i) < 10000000) {
					totalTimeInApp += timestamp.get(i);
					totalTimeInDailyArchive += timestamp.get(i);
				}
			} else if (event.get(i).equals("TIME_IN_CURRENT_LOC")) {
				clicksForCurrentLoc++;
				if (timestamp.get(i) < 10000000) {
					totalTimeInApp += timestamp.get(i);
					totalTimeInCurrentLoc += timestamp.get(i);
				}
			} else if (event.get(i).equals("TIME_IN_LAST_PLACE")) {
				clicksForLastPlace++;
				if (timestamp.get(i) < 10000000) {
					totalTimeInApp += timestamp.get(i);
					totalTimeInLastPlace += timestamp.get(i);
				}
			} else if (event.get(i).equals("TIME_IN_LATEST_JOURNEY")) {
				clicksForLatestJourney++;
				if (timestamp.get(i) < 10000000) {
					totalTimeInApp += timestamp.get(i);
					totalTimeInLatestJourney += timestamp.get(i);
				}
			} else if (event.get(i).equals("TIME_IN_DAILY_ITIN")) {
				clicksForDailyItin++;
				if (timestamp.get(i) < 10000000) {
					totalTimeInApp += timestamp.get(i);
					totalTimeInDailyItin += timestamp.get(i);
				}
			} else if (event.get(i).equals("TIME_IN_WEEKLY_ITIN")) {
				clicksForWeeklyItin++;
				if (timestamp.get(i) < 10000000) {
					totalTimeInApp += timestamp.get(i);
					totalTimeInWeeklyItin += timestamp.get(i);
				}
			} else if (event.get(i).equals("TIME_IN_MOST_VISITED")) {
				clicksForMostVisited++;
				if (timestamp.get(i) < 10000000) {
					totalTimeInApp += timestamp.get(i);
					totalTimeInMostVisited += timestamp.get(i);
				}
			} else if (event.get(i).equals("LIKED_CURRENT_LOC"))
				likesForCurrentLoc++;
			else if (event.get(i).equals("LIKED_LAST_PLACE"))
				likesForLastPlace++;
			else if (event.get(i).equals("LIKED_LATEST_JOURNEY"))
				likesForLatestJourney++;
			else if (event.get(i).equals("LIKED_DAILY_ITIN"))
				likesForDailyItin++;
			else if (event.get(i).equals("LIKED_WEEKLY_ITIN"))
				likesForWeeklyItin++;
			else if (event.get(i).equals("LIKED_MOST_VISITED"))
				likesForMostVisited++;

		}

		finalTotalTimeInApp = functions.getHoursMinutes(totalTimeInApp / 1000, true);
		finalTotalTimeInWeeklyArchive = functions.getHoursMinutes(totalTimeInWeeklyArchive / 1000, true);
		finalTotalTimeInDailyArchive = functions.getHoursMinutes(totalTimeInDailyArchive / 1000, true);
		finalTotalTimeInCurrentLoc = functions.getHoursMinutes(totalTimeInCurrentLoc / 1000, true);
		finalTotalTimeInLastPlace = functions.getHoursMinutes(totalTimeInLastPlace / 1000, true);
		finalTotalTimeInLatestJourney = functions.getHoursMinutes(totalTimeInLatestJourney / 1000, true);
		finalTotalTimeInDailyItin = functions.getHoursMinutes(totalTimeInDailyItin / 1000, true);
		finalTotalTimeInWeeklyItin = functions.getHoursMinutes(totalTimeInWeeklyItin / 1000, true);
		finalTotalTimeInMostVisited = functions.getHoursMinutes(totalTimeInMostVisited / 1000, true);
		
		StringEntity jsonEntity = null;
		try {
			jsonEntity = new StringEntity("{\"token\": " + token + ", \"appid\": sensiblejournal, \"events\": [" +
					"{\"Total time in app\": \"" + finalTotalTimeInApp + "\"}, " +
					"{\"Total time in Weekly Itinerary Archive\": \"" + finalTotalTimeInWeeklyArchive + "\"}, " +
					"{\"Total time in Daily Itinerary Archive\": \"" + finalTotalTimeInDailyArchive + "\"}, " +
					"{\"Total time in Current Location detailed view\": \"" + finalTotalTimeInCurrentLoc + "\"}, " +
					"{\"Total time in Last Place Visited detailed view\": \"" + finalTotalTimeInLastPlace + "\"}, " +
					"{\"Total time in Latest Journey detailed view\": \"" + finalTotalTimeInLatestJourney + "\"}, " +
					"{\"Total time in Daily Itinerary detailed view\": \"" + finalTotalTimeInDailyItin + "\"}, " +
					"{\"Total time in Weekly Itinerary detailed view\": \"" + finalTotalTimeInWeeklyItin + "\"}, " +
					"{\"Total time in Most Visited Places detailed view\": \"" + finalTotalTimeInMostVisited + "\"}, " +
					"{\"Number of times in Weekly Itinerary archive\": \"" + clicksForWeeklyArchive + "\"}, " +
					"{\"Number of times in Daily Itinerary archive\": \"" + clicksForDailyArchive + "\"}, " +
					"{\"Number of times in Current Location detailed view\": \"" + clicksForCurrentLoc + "\"}, " +
					"{\"Number of times in Last Place Visited detailed view\": \"" + clicksForLastPlace + "\"}, " +
					"{\"Number of times in Latest Journey detailed view\": \"" + clicksForLatestJourney + "\"}, " +
					"{\"Number of times in Daily Itinerary detailed view\": \"" + clicksForDailyItin + "\"}, " +
					"{\"Number of times in Weekly Itinerary detailed view\": \"" + clicksForWeeklyItin + "\"}, " +
					"{\"Number of times in Most Visited Places detailed view\": \"" + clicksForMostVisited + "\"}, " +
					"{\"Current Location card thumbs up\": \"" + likesForCurrentLoc + "\"}, " +
					"{\"Last Place Visited card thumbs up\": \"" + likesForLastPlace + "\"}, " +
					"{\"Latest Journey card thumbs up\": \"" + likesForLatestJourney + "\"}, " +
					"{\"Daily Itinerary card thumbs up\": \"" + likesForDailyItin + "\"}, " +
					"{\"Weekly Itinerary card thumbs up\": \"" + likesForWeeklyItin + "\"}, " +
					"{\"Most Visited Places card thumbs up\": \"" + likesForMostVisited + "\"}]}");
		} catch (UnsupportedEncodingException e) {
			Log.d("Sensible Journal usage upload", "Encoding error in JSON");
			e.printStackTrace();
		}
		
		// Get an SSL secured HTTP client from the HttpUtils
		Constants.httpClient = HttpUtils.getNewHttpClient();
									
		HttpPost httpPost = new HttpPost(BASE_URL_UPLOAD);
		httpPost.setHeader("content-type", "application/json");
		httpPost.setEntity(jsonEntity);
		HttpResponse response = Constants.httpClient.execute(httpPost);
		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		System.out.println("Usage log uploaded with message: " + reader.readLine());
	}
	
	////////METHOD THAT READ'S THE USER'S MOVEMENT DATA FROM THE SQLITE DATABASE //////////
	@SuppressWarnings("deprecation")
	public void getDataFromCache(boolean getOnlyFirstDay, boolean getOnlyFirstWeek, int getNoOfentries,
								LinkedList<Integer> POI_id,	LinkedList<Location> locations,	LinkedList<Date> arrivals,
								LinkedList<Date> departures, LinkedList<String> days) {
		
		POI_id.clear();
		locations.clear();
		arrivals.clear();
		departures.clear();
		if (days != null) days.clear();
		
		CacheDbHelper mDbHelper = new CacheDbHelper(context);
		
		// Gets the data repository in read mode
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		
		String query = "SELECT * FROM daily_entries ORDER BY " + CacheEntry.COLUMN_DEPARTURE + " DESC";
		Cursor c = db.rawQuery(query, null);
		
		Location location;
		Date tmpDateDep;
		Date tmpDateArr;
		boolean firstLoop = true;
		int dayBuff = 0;
		int weekBuff = 0;
		int counter = 0;
		c.moveToFirst();
		Calendar cal;
		
		while (!c.isAfterLast()) {			
			POI_id.add(Integer.parseInt(c.getString(0)));		   		   
			location = new Location("temporary");
			location.setLatitude(c.getFloat(1));
			location.setLongitude(c.getFloat(2));
			locations.add(location);
			tmpDateArr = new Date(c.getLong(3));
			tmpDateDep = new Date(c.getLong(4));
			arrivals.add(tmpDateArr);
			departures.add(tmpDateDep);
			
			if(getNoOfentries > 0) {
				days.add(tmpDateArr.toString().substring(0, 10) + " " + Integer.toString(tmpDateArr.getYear()+1900));
				counter++;
				if(counter == getNoOfentries) break;
				else {
					c.moveToNext();
					continue;
				}
			}
			
			// Break from the loop when the first day is fetched
			if(getOnlyFirstDay) {				
				if(firstLoop) {
					days.add(tmpDateDep.toString().substring(0, 10) + " " + Integer.toString(tmpDateDep.getYear()+1900));
					dayBuff = tmpDateDep.getDate();
					if (tmpDateArr.getDate() != dayBuff) break;
					firstLoop = false;
					c.moveToNext();
					continue;
				}
				if (tmpDateArr.getDate() != dayBuff) break;        			
				else days.add(tmpDateArr.toString().substring(0, 10) + " " + Integer.toString(tmpDateArr.getYear()+1900));
				c.moveToNext();
				continue;
			}
			
			// Break from the loop when the first week (most recent) in the database is fetched
			if(getOnlyFirstWeek) {    							        	
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
		       		POI_id.removeLast();
		       		locations.removeLast();
		       		arrivals.removeLast();
		       		departures.removeLast();
		       		break;	        	
		       	}
		       	c.moveToNext();
		       	continue;
			}
			
			// Detect the separate days
			if (days != null) {
				days.add(tmpDateDep.toString().substring(0, 10) + " " + Integer.toString(tmpDateDep.getYear()+1900));
				
				// If an arrival an a departure have a different day, include the entries twice
				// so that they will be used as the start point of the next day
				if (tmpDateArr.getDate() != tmpDateDep.getDate()) {
					POI_id.add(Integer.parseInt(c.getString(0)));		   		   
					location = new Location("temporary");
					location.setLatitude(c.getFloat(1));
					location.setLongitude(c.getFloat(2));
					locations.add(location);
					arrivals.add(tmpDateArr);
					departures.add(tmpDateDep);
					days.add(tmpDateArr.toString().substring(0, 10) + " " + Integer.toString(tmpDateArr.getYear()+1900));
				}
			}
			c.moveToNext();
		}
		c.close();
		db.close();
	}
}
