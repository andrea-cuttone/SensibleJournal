package dk.dtu.imm.sensiblejournal2013.cards.tools;

import dk.dtu.imm.sensiblejournal2013.utilities.Constants;
import dk.dtu.imm.sensiblejournal2013.utilities.AppFunctions;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.AbsListView;
import it.gmariotti.cardslib.library.view.listener.SwipeOnScrollListener;

public class CardRefreshTask extends AsyncTask<Void, Void, Void> {
	
	private AppFunctions functions;
	private Context context;
	
	public CardRefreshTask(Context context) {
		this.context = context;
		functions = new AppFunctions(context);
	}
	
	@Override
	protected void onPostExecute(Void result) {
		if (Constants.progressDialog != null) Constants.progressDialog.dismiss();
		
		// 1. Create card array adapter
		// 2. Add scroll listener for continuous feed
		// 3. Set the card array adapter to the card list
		
		if (Constants.mListView != null){
		    Constants.mCardArrayAdapter = new CardArrayAdapter(context, Constants.cards);
		    Constants.mCardArrayAdapter.setInnerViewTypeCount(11);
		    Constants.feedLoading = false;
		    
		    Constants.mListView.setOnScrollListener( new SwipeOnScrollListener() {
		    	@Override
		        public void onScrollStateChanged(AbsListView view, int scrollState) {
		    		//It is very important to call the super method here to preserve built-in functions
		            super.onScrollStateChanged(view,scrollState);
		    	}

		        @Override
		        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		        	if (totalItemCount > 2) {			    		
		        		/** Source: http://mobile.dzone.com/news/android-tutorial-dynamicaly **/
		        		int lastInScreen = firstVisibleItem + visibleItemCount;
		     			//is the bottom item visible & not loading more already ? Load more !
		     			if((lastInScreen == totalItemCount) && !(Constants.feedLoading)){			
		     				if (!Constants.firstRun) {
		     					Constants.FEED_COUNTER++;				         
		     					functions.fetchNextCards(Constants.FEED_COUNTER);						
		     				}
		     				else Constants.firstRun = false;
		     			}
		        	}
		        }
		     });
		    
			Constants.mListView.setAdapter(Constants.mCardArrayAdapter);						
			if (Constants.refreshed == false) Constants.mListView.setSelectionFromTop(Constants.index, Constants.top);
			Constants.refreshed = false;
		}
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		//Initialize the card array and refresh the card list    	
		functions.refreshCards();					
		return null;
	}
}