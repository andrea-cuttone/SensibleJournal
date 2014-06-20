package dk.dtu.imm.sensiblejournal2013.cards.tutorial;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.component.CardThumbnailView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import dk.dtu.imm.sensiblejournal2013.R;
import dk.dtu.imm.sensiblejournal2013.cards.tools.CustomHeader;
import dk.dtu.imm.sensiblejournal2013.cards.tools.CustomThumbCard;
import dk.dtu.imm.sensiblejournal2013.utilities.Constants;

public class TutorialCard3 extends Card {
	
	private TextView tutorialText;
    private Context context;
    private Button dismissButton;
    private CustomThumbCard thumbnail;

    public TutorialCard3(Context context) {
    	super(context, R.layout.tutorial_card_content);
        this.context = context;
        init();
    }

	private void init(){        	       
        //Create a CardHeader
    	CustomHeader header = new CustomHeader(context, getType());
        addCardHeader(header);                                 
                               
        thumbnail = new CustomThumbCard (getContext());                   	 	       	 	                     	
        thumbnail.setDrawableResource(R.drawable.screen3);
        
        //You can set a SwipeListener.
        this.setOnSwipeListener(new Card.OnSwipeListener() {
            @SuppressLint("CommitPrefEdits")
			@Override
            public void onSwipe(Card card) {
            	// We need an Editor object to make preference changes.
                // All objects are from android.context.Context
                SharedPreferences settings = context.getSharedPreferences("com.sensibleDTU.settings", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("tutorial3Gone", true);
                editor.commit();
            }
        });        
        
        //Add ClickListener
        setOnClickListener(new OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {           	
            	Toast.makeText(context, "Tutorial card...Swipe or tap \"Got it!\" to dismiss! :)", Toast.LENGTH_SHORT).show();
            }
        });
        
        setSwipeable(true);
    }
    
    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

    	setBackgroundResourceId(R.color.tutorial_card_color);
    	
    	CardThumbnailView cardThumb = (CardThumbnailView) parent.findViewById(R.id.card_thumbnail_layout);
        cardThumb.addCardThumbnail(thumbnail);
        
        dismissButton = (Button) parent.findViewById(R.id.got_it);        

        dismissButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// We need an Editor object to make preference changes.
                // All objects are from android.context.Context
                SharedPreferences settings = context.getSharedPreferences("com.sensibleDTU.settings", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("tutorial3Gone", true);
                editor.commit();
                Toast.makeText(context, "OK! You won't see this card again!", Toast.LENGTH_SHORT).show();
                
                Constants.cards.remove(Constants.tutorialCard3);
    		    Constants.mCardArrayAdapter = new CardArrayAdapter(context, Constants.cards);
    		    Constants.mCardArrayAdapter.setInnerViewTypeCount(10);
                Constants.mListView.setAdapter(Constants.mCardArrayAdapter);
			}
        });
        
    	tutorialText = (TextView) parent.findViewById(R.id.tutorial_text);
	    if (tutorialText != null) tutorialText.setText(R.string.tutorial3_text);    
	           
    }

    @Override
    public int getType() {
        //Very important with different inner layouts
        return 8;
    }
}
