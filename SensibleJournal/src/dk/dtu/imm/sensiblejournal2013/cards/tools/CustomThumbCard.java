package dk.dtu.imm.sensiblejournal2013.cards.tools;

import dk.dtu.imm.sensiblejournal2013.utilities.Constants;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

public class CustomThumbCard extends CardThumbnail {
	private DisplayMetrics metrics;

    public CustomThumbCard(Context context) {
        super(context);
        this.metrics = context.getResources().getDisplayMetrics();
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View viewImage) {
        if (viewImage!=null){                
            viewImage.getLayoutParams().width = (int) (Constants.THUMB_WIDTH - (20*metrics.density));
            viewImage.getLayoutParams().height = Constants.THUMB_HEIGHT;                         
        }
    }                
} 
