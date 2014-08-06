package dk.dtu.imm.sensiblejournal2013.cards.tools;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import dk.dtu.imm.sensiblejournal2013.R;
import dk.dtu.imm.sensiblejournal2013.utilities.Constants;
import it.gmariotti.cardslib.library.view.CardListView;

public class CardListFragment extends android.support.v4.app.Fragment implements
									GooglePlayServicesClient.ConnectionCallbacks,
									GooglePlayServicesClient.OnConnectionFailedListener{
	
	private LocationClient mLocationClient;
	
    public int getTitleResourceId() {
        return R.string.app_name;
    }
    
    protected void setTitle(){
        getActivity().setTitle(getTitleResourceId());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.card_list, container, false);
    }
    
	@Override
	public void onResume() {
		super.onResume();
		mLocationClient = new LocationClient(getActivity(), this, this);
	    mLocationClient.connect();
	}
	
	@Override
	public void onPause() {	
		super.onPause();
	}
 
	@Override
	public void onStop() {
		super.onStop();
		mLocationClient.disconnect();
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
    public void onActivityCreated(Bundle savedInstanceState) {
       super.onActivityCreated(savedInstanceState);
       Constants.mListView = (CardListView) getActivity().findViewById(R.id.card_list);            
       setTitle();       
    }
                             
	@SuppressWarnings("deprecation")
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		getActivity().showDialog(connectionResult.getErrorCode());
	}

	// The application starts when it is verified that it is connected to the Google Play Services
	@Override
	public void onConnected(Bundle arg0) {
		// http://stackoverflow.com/questions/23032966/not-connected-call-connect-or-wait-for-onconnected-to-be-called-exception-ins
		if(mLocationClient.isConnected()) {
			Constants.myLocation = mLocationClient.getLastLocation();
			
			if (Constants.newDataFetched) {
				Constants.cards = null;
				Constants.mostVisitedCard = null;
				Constants.todaysItCard = null;
				Constants.weeklyItCard = null;
				Constants.myCurrentLocationCard = null;
				Constants.pastStopCard = null;
				Constants.commuteCard = null;
				Constants.FEED_COUNTER = 1;
				Constants.refreshed = true;
				try {
					if(!getActivity().isFinishing()){ Constants.progressDialog = ProgressDialog.show(getActivity(), "", "Loading. Please wait..."); }
				} catch (Exception e) { e.printStackTrace(); }
			}
			CardRefreshTask rTask = new CardRefreshTask(getActivity());
			rTask.execute();	
		}
	}
		
	@Override
	public void onDisconnected() {
        Toast.makeText(getActivity(), "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();		
	}		
}